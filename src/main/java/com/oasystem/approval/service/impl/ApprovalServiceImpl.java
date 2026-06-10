package com.oasystem.approval.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oasystem.approval.dto.StartApprovalDTO;
import com.oasystem.approval.entity.ApprovalInstance;
import com.oasystem.approval.entity.ApprovalRecord;
import com.oasystem.approval.entity.ApprovalTemplate;
import com.oasystem.approval.mapper.ApprovalInstanceMapper;
import com.oasystem.approval.mapper.ApprovalRecordMapper;
import com.oasystem.approval.mapper.ApprovalTemplateMapper;
import com.oasystem.approval.service.ApprovalService;
import com.oasystem.common.constant.Constants;
import com.oasystem.common.exception.BusinessException;
import com.oasystem.system.entity.Dept;
import com.oasystem.system.entity.Role;
import com.oasystem.system.entity.User;
import com.oasystem.system.entity.UserRole;
import com.oasystem.system.mapper.DeptMapper;
import com.oasystem.system.mapper.RoleMapper;
import com.oasystem.system.mapper.UserMapper;
import com.oasystem.system.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 审批中心 Service 实现 — 多级审批引擎
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalTemplateMapper templateMapper;
    private final ApprovalInstanceMapper instanceMapper;
    private final ApprovalRecordMapper recordMapper;
    private final UserMapper userMapper;
    private final DeptMapper deptMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // ==================== 发起审批 ====================

    @Override
    @Transactional
    public Long start(StartApprovalDTO dto, Long applicantId) {
        // 1. 加载模板
        ApprovalTemplate template = templateMapper.selectById(dto.getTemplateId());
        if (template == null) {
            throw new BusinessException("审批模板不存在");
        }
        if (template.getStatus() != null && template.getStatus() == 0) {
            throw new BusinessException("该审批模板已禁用");
        }

        // 2. 解析审批人配置
        List<ApproverConfig> approverConfigs = parseApproversConfig(template.getApproversConfig());
        if (approverConfigs.isEmpty()) {
            throw new BusinessException("审批模板未配置审批人");
        }

        // 3. 加载申请人信息
        User applicant = userMapper.selectById(applicantId);
        if (applicant == null) {
            throw new BusinessException("申请人不存在");
        }

        // 4. 逐级解析审批人
        List<ApproverSnapshot> snapshots = new ArrayList<>();
        for (ApproverConfig config : approverConfigs) {
            Long approverId = resolveApprover(config, applicant);
            snapshots.add(new ApproverSnapshot(config.getLevel(), approverId));
        }

        // 5. 创建审批实例
        ApprovalInstance instance = new ApprovalInstance();
        instance.setTemplateId(dto.getTemplateId());
        instance.setApplicantId(applicantId);
        instance.setTitle(dto.getTitle());
        instance.setContent(dto.getContent());
        instance.setTotalLevels(snapshots.size());
        instance.setCurrentLevel(1);
        instance.setStatus(Constants.APPROVAL_PENDING);
        try {
            instance.setApproversSnapshot(OBJECT_MAPPER.writeValueAsString(snapshots));
        } catch (Exception e) {
            throw new BusinessException("审批人快照序列化失败");
        }
        instanceMapper.insert(instance);

        // 6. 预创建每级审批记录
        for (ApproverSnapshot snap : snapshots) {
            ApprovalRecord record = new ApprovalRecord();
            record.setInstanceId(instance.getId());
            record.setLevel(snap.getLevel());
            record.setApproverId(snap.getApproverId());
            record.setResult(0); // 待审批
            recordMapper.insert(record);
        }

        log.info("审批发起成功, instanceId={}, applicantId={}, totalLevels={}",
                instance.getId(), applicantId, snapshots.size());
        return instance.getId();
    }

    // ==================== 待审批列表 ====================

    @Override
    public List<ApprovalInstance> getTodo(Long userId) {
        // 查询当前用户作为审批人且待审批的记录
        List<ApprovalRecord> pendingRecords = recordMapper.selectList(
                new LambdaQueryWrapper<ApprovalRecord>()
                        .eq(ApprovalRecord::getApproverId, userId)
                        .eq(ApprovalRecord::getResult, 0)
        );
        if (pendingRecords.isEmpty()) {
            return Collections.emptyList();
        }

        // 获取关联的审批实例，并过滤：记录级别必须等于实例当前级别
        Set<Long> instanceIds = pendingRecords.stream()
                .map(ApprovalRecord::getInstanceId)
                .collect(Collectors.toSet());

        List<ApprovalInstance> instances = instanceMapper.selectBatchIds(instanceIds);
        if (instances.isEmpty()) {
            return Collections.emptyList();
        }

        // 构建 instanceId -> instance 映射
        Map<Long, ApprovalInstance> instanceMap = instances.stream()
                .collect(Collectors.toMap(ApprovalInstance::getId, i -> i));

        // 过滤：记录级别 == 实例当前级别 且 实例状态为审批中
        return pendingRecords.stream()
                .filter(r -> {
                    ApprovalInstance inst = instanceMap.get(r.getInstanceId());
                    return inst != null
                            && inst.getStatus().equals(Constants.APPROVAL_PENDING)
                            && r.getLevel().equals(inst.getCurrentLevel());
                })
                .map(r -> instanceMap.get(r.getInstanceId()))
                .distinct()
                .sorted(Comparator.comparing(ApprovalInstance::getCreateTime).reversed())
                .collect(Collectors.toList());
    }

    // ==================== 已审批列表 ====================

    @Override
    public List<ApprovalInstance> getDone(Long userId) {
        // 查询当前用户已经审批过的记录（同意或驳回）
        List<ApprovalRecord> doneRecords = recordMapper.selectList(
                new LambdaQueryWrapper<ApprovalRecord>()
                        .eq(ApprovalRecord::getApproverId, userId)
                        .ne(ApprovalRecord::getResult, 0)
        );
        if (doneRecords.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> instanceIds = doneRecords.stream()
                .map(ApprovalRecord::getInstanceId)
                .collect(Collectors.toSet());

        List<ApprovalInstance> instances = instanceMapper.selectBatchIds(instanceIds);
        instances.sort(Comparator.comparing(ApprovalInstance::getCreateTime).reversed());
        return instances;
    }

    // ==================== 我的申请 ====================

    @Override
    public List<ApprovalInstance> getMy(Long userId) {
        return instanceMapper.selectList(
                new LambdaQueryWrapper<ApprovalInstance>()
                        .eq(ApprovalInstance::getApplicantId, userId)
                        .orderByDesc(ApprovalInstance::getCreateTime)
        );
    }

    // ==================== 审批操作 ====================

    @Override
    @Transactional
    public void approve(Long instanceId, int result, String comment, Long currentUserId) {
        // 校验审批结果
        if (result != Constants.APPROVAL_APPROVED && result != Constants.APPROVAL_REJECTED) {
            throw new BusinessException("无效的审批结果");
        }

        // 加载实例
        ApprovalInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new BusinessException("审批实例不存在");
        }

        // 校验实例状态
        if (!instance.getStatus().equals(Constants.APPROVAL_PENDING)) {
            if (instance.getStatus().equals(Constants.APPROVAL_APPROVED)) {
                throw new BusinessException("该审批已通过");
            } else if (instance.getStatus().equals(Constants.APPROVAL_REJECTED)) {
                throw new BusinessException("该审批已被驳回");
            } else if (instance.getStatus().equals(Constants.APPROVAL_CANCELLED)) {
                throw new BusinessException("该审批已被撤回");
            }
        }

        // 查找当前级别的审批记录
        ApprovalRecord record = recordMapper.selectOne(
                new LambdaQueryWrapper<ApprovalRecord>()
                        .eq(ApprovalRecord::getInstanceId, instanceId)
                        .eq(ApprovalRecord::getLevel, instance.getCurrentLevel())
        );
        if (record == null) {
            throw new BusinessException("当前审批级别的记录不存在");
        }

        // 校验记录状态
        if (record.getResult() != 0) {
            throw new BusinessException("该级别已被审批");
        }

        // 校验当前用户是否为指定审批人
        if (!record.getApproverId().equals(currentUserId)) {
            throw new BusinessException("您不是当前级别的审批人，无权审批");
        }

        // 更新审批记录
        record.setResult(result);
        record.setComment(comment);
        record.setApprovalTime(LocalDateTime.now());
        recordMapper.updateById(record);

        // 状态流转
        if (result == Constants.APPROVAL_REJECTED) {
            // 驳回：直接结束
            instance.setStatus(Constants.APPROVAL_REJECTED);
            instance.setFinishTime(LocalDateTime.now());
            instanceMapper.updateById(instance);
            log.info("审批驳回, instanceId={}, approverId={}", instanceId, currentUserId);
        } else {
            // 同意：检查是否还有下一级
            if (instance.getCurrentLevel() < instance.getTotalLevels()) {
                // 推进到下一级
                instance.setCurrentLevel(instance.getCurrentLevel() + 1);
                instanceMapper.updateById(instance);
                log.info("审批推进, instanceId={}, currentLevel={}", instanceId, instance.getCurrentLevel());
            } else {
                // 最后一级通过
                instance.setStatus(Constants.APPROVAL_APPROVED);
                instance.setFinishTime(LocalDateTime.now());
                instanceMapper.updateById(instance);
                log.info("审批通过, instanceId={}", instanceId);
            }
        }
    }

    // ==================== 审批记录查询 ====================

    @Override
    public List<ApprovalRecord> getRecords(Long instanceId) {
        return recordMapper.selectList(
                new LambdaQueryWrapper<ApprovalRecord>()
                        .eq(ApprovalRecord::getInstanceId, instanceId)
                        .orderByAsc(ApprovalRecord::getLevel)
        );
    }

    // ==================== 私有方法：审批人解析 ====================

    /**
     * 解析 approversConfig JSON 为配置列表
     */
    private List<ApproverConfig> parseApproversConfig(String configJson) {
        if (configJson == null || configJson.isBlank()) {
            throw new BusinessException("审批人配置为空");
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(configJson);
            if (!root.isArray() || root.isEmpty()) {
                throw new BusinessException("审批人配置格式错误");
            }
            List<ApproverConfig> configs = new ArrayList<>();
            for (JsonNode item : root) {
                ApproverConfig cfg = new ApproverConfig();
                cfg.setLevel(item.get("level").asInt());
                cfg.setType(item.get("type").asText());
                if (item.has("value") && !item.get("value").isNull()) {
                    cfg.setValue(item.get("value").asText());
                }
                configs.add(cfg);
            }
            return configs;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("审批人配置解析失败：" + e.getMessage());
        }
    }

    /**
     * 根据配置解析具体审批人ID
     */
    private Long resolveApprover(ApproverConfig config, User applicant) {
        int level = config.getLevel();
        String type = config.getType();

        switch (type) {
            case "DEPT_LEADER": {
                if (applicant.getDeptId() == null) {
                    throw new BusinessException("您尚未分配部门，无法提交审批");
                }
                Dept dept = deptMapper.selectById(applicant.getDeptId());
                if (dept == null) {
                    throw new BusinessException("您所属的部门不存在");
                }
                if (dept.getLeaderId() == null) {
                    throw new BusinessException("您所属的部门【" + dept.getDeptName() + "】未设置负责人，无法提交审批");
                }
                return dept.getLeaderId();
            }
            case "ROLE": {
                if (config.getValue() == null || config.getValue().isBlank()) {
                    throw new BusinessException("第" + level + "级审批人角色编码未配置");
                }
                // 查找角色
                Role role = roleMapper.selectOne(
                        new LambdaQueryWrapper<Role>()
                                .eq(Role::getRoleCode, config.getValue())
                );
                if (role == null) {
                    throw new BusinessException("角色编码【" + config.getValue() + "】不存在");
                }
                // 查找拥有该角色的用户
                List<UserRole> userRoles = userRoleMapper.selectList(
                        new LambdaQueryWrapper<UserRole>()
                                .eq(UserRole::getRoleId, role.getId())
                );
                if (userRoles.isEmpty()) {
                    throw new BusinessException("没有用户拥有角色【" + role.getRoleName() + "】，无法提交审批");
                }
                // 取第一个拥有该角色的用户作为审批人
                return userRoles.get(0).getUserId();
            }
            case "USER": {
                if (config.getValue() == null || config.getValue().isBlank()) {
                    throw new BusinessException("第" + level + "级审批人ID未配置");
                }
                Long approverId;
                try {
                    approverId = Long.valueOf(config.getValue());
                } catch (NumberFormatException e) {
                    throw new BusinessException("第" + level + "级审批人ID格式错误");
                }
                User approver = userMapper.selectById(approverId);
                if (approver == null) {
                    throw new BusinessException("第" + level + "级指定的审批人（ID=" + approverId + "）不存在");
                }
                return approverId;
            }
            default:
                throw new BusinessException("第" + level + "级审批人类型不支持：" + type);
        }
    }

    // ==================== 内部类 ====================

    @lombok.Data
    private static class ApproverConfig {
        private Integer level;
        private String type;
        private String value;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class ApproverSnapshot {
        private Integer level;
        private Long approverId;
    }
}
