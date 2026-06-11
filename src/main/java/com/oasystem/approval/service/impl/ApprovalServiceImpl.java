package com.oasystem.approval.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
import com.oasystem.attendance.entity.LeaveRequest;
import com.oasystem.attendance.mapper.LeaveRequestMapper;
import com.oasystem.common.constant.Constants;
import com.oasystem.common.exception.BusinessException;
import com.oasystem.expense.entity.ExpenseRequest;
import com.oasystem.expense.mapper.ExpenseMapper;
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
    private final LeaveRequestMapper leaveRequestMapper;
    private final ExpenseMapper expenseMapper;

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

        // 4. 逐级解析审批人（支持同一级别多个审批人）
        List<ApproverSnapshot> snapshots = new ArrayList<>();
        for (ApproverConfig config : approverConfigs) {
            List<Long> approverIds = resolveApprovers(config, applicant);
            for (Long approverId : approverIds) {
                snapshots.add(new ApproverSnapshot(config.getLevel(), approverId));
            }
        }

        // 5. 计算去重后的审批级别数
        int distinctLevels = (int) snapshots.stream()
                .map(ApproverSnapshot::getLevel)
                .distinct()
                .count();

        // 6. 创建审批实例
        ApprovalInstance instance = new ApprovalInstance();
        instance.setTemplateId(dto.getTemplateId());
        instance.setApplicantId(applicantId);
        instance.setTitle(dto.getTitle());
        instance.setContent(dto.getContent());
        instance.setTotalLevels(distinctLevels);
        instance.setCurrentLevel(1);
        instance.setStatus(Constants.APPROVAL_PENDING);
        instance.setBusinessType(dto.getBusinessType());
        instance.setBusinessId(dto.getBusinessId());
        try {
            instance.setApproversSnapshot(OBJECT_MAPPER.writeValueAsString(snapshots));
        } catch (Exception e) {
            throw new BusinessException("审批人快照序列化失败");
        }
        instanceMapper.insert(instance);

        // 7. 预创建每级审批记录（同一级别可能有多条记录）
        for (ApproverSnapshot snap : snapshots) {
            ApprovalRecord record = new ApprovalRecord();
            record.setInstanceId(instance.getId());
            record.setLevel(snap.getLevel());
            record.setApproverId(snap.getApproverId());
            record.setResult(0); // 待审批
            recordMapper.insert(record);
        }

        log.info("审批发起成功, instanceId={}, applicantId={}, totalSnapshots={}, distinctLevels={}",
                instance.getId(), applicantId, snapshots.size(), distinctLevels);
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
        // 查询当前用户已经审批过的记录（同意或驳回，排除自动作废）
        List<ApprovalRecord> doneRecords = recordMapper.selectList(
                new LambdaQueryWrapper<ApprovalRecord>()
                        .eq(ApprovalRecord::getApproverId, userId)
                        .notIn(ApprovalRecord::getResult, 0, Constants.APPROVAL_AUTO_VOIDED)
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

        // 查找当前级别的所有待审批记录（支持并行审批：同一级别可能有多条记录）
        List<ApprovalRecord> currentLevelRecords = recordMapper.selectList(
                new LambdaQueryWrapper<ApprovalRecord>()
                        .eq(ApprovalRecord::getInstanceId, instanceId)
                        .eq(ApprovalRecord::getLevel, instance.getCurrentLevel())
                        .eq(ApprovalRecord::getResult, 0)  // 只查待审批的
        );
        if (currentLevelRecords.isEmpty()) {
            throw new BusinessException("当前审批级别没有待审批记录");
        }

        // 在当前级别的待审批记录中查找属于当前用户的记录
        ApprovalRecord record = currentLevelRecords.stream()
                .filter(r -> r.getApproverId().equals(currentUserId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("您不是当前级别的审批人，无权审批"));

        // 更新当前审批人的记录（条件更新：只有 result=0 时才更新，防止双击/并发重复审批）
        LambdaUpdateWrapper<ApprovalRecord> recordUpdateWrapper = new LambdaUpdateWrapper<>();
        recordUpdateWrapper.eq(ApprovalRecord::getId, record.getId())
                           .eq(ApprovalRecord::getResult, 0);  // 确保记录仍为待审批状态
        ApprovalRecord updateRecord = new ApprovalRecord();
        updateRecord.setResult(result);
        updateRecord.setComment(comment);
        updateRecord.setApprovalTime(LocalDateTime.now());
        int recordUpdated = recordMapper.update(updateRecord, recordUpdateWrapper);
        if (recordUpdated == 0) {
            throw new BusinessException("该审批记录已被处理，请刷新页面后重试");
        }

        // 作废同级别其他审批人的待审批记录（并行审批：一人处理后其余自动作废）
        // 同样使用条件更新，避免并发问题
        for (ApprovalRecord other : currentLevelRecords) {
            if (other.getApproverId().equals(currentUserId)) continue;
            LambdaUpdateWrapper<ApprovalRecord> voidUpdateWrapper = new LambdaUpdateWrapper<>();
            voidUpdateWrapper.eq(ApprovalRecord::getId, other.getId())
                             .eq(ApprovalRecord::getResult, 0);
            ApprovalRecord voidRecord = new ApprovalRecord();
            voidRecord.setResult(Constants.APPROVAL_AUTO_VOIDED);
            voidRecord.setComment("其他审批人已处理，本记录自动作废");
            voidRecord.setApprovalTime(LocalDateTime.now());
            recordMapper.update(voidRecord, voidUpdateWrapper);
        }

        // 状态流转（条件更新实例状态，防止并发重复操作）
        LambdaUpdateWrapper<ApprovalInstance> instanceUpdateWrapper = new LambdaUpdateWrapper<>();
        instanceUpdateWrapper.eq(ApprovalInstance::getId, instanceId)
                             .eq(ApprovalInstance::getStatus, Constants.APPROVAL_PENDING);

        if (result == Constants.APPROVAL_REJECTED) {
            // 驳回：直接结束
            ApprovalInstance updateInstance = new ApprovalInstance();
            updateInstance.setStatus(Constants.APPROVAL_REJECTED);
            updateInstance.setFinishTime(LocalDateTime.now());
            int instUpdated = instanceMapper.update(updateInstance, instanceUpdateWrapper);
            if (instUpdated == 0) {
                log.warn("审批实例状态更新失败（可能已被并发处理）, instanceId={}", instanceId);
            } else {
                log.info("审批驳回, instanceId={}, approverId={}", instanceId, currentUserId);
                // 刷新实例数据用于回调
                instance.setStatus(Constants.APPROVAL_REJECTED);
                instance.setFinishTime(LocalDateTime.now());
                syncBusinessStatus(instance);
            }
        } else {
            // 同意：检查是否还有下一级
            if (instance.getCurrentLevel() < instance.getTotalLevels()) {
                // 推进到下一级（只更新 currentLevel，不更新 status）
                LambdaUpdateWrapper<ApprovalInstance> advanceWrapper = new LambdaUpdateWrapper<>();
                advanceWrapper.eq(ApprovalInstance::getId, instanceId)
                              .eq(ApprovalInstance::getStatus, Constants.APPROVAL_PENDING)
                              .eq(ApprovalInstance::getCurrentLevel, instance.getCurrentLevel());
                ApprovalInstance advanceInstance = new ApprovalInstance();
                advanceInstance.setCurrentLevel(instance.getCurrentLevel() + 1);
                int advUpdated = instanceMapper.update(advanceInstance, advanceWrapper);
                if (advUpdated == 0) {
                    log.warn("审批推进失败（可能已被并发处理）, instanceId={}", instanceId);
                } else {
                    log.info("审批推进, instanceId={}, currentLevel={}", instanceId, instance.getCurrentLevel() + 1);
                }
            } else {
                // 最后一级通过
                ApprovalInstance approveInstance = new ApprovalInstance();
                approveInstance.setStatus(Constants.APPROVAL_APPROVED);
                approveInstance.setFinishTime(LocalDateTime.now());
                int instUpdated = instanceMapper.update(approveInstance, instanceUpdateWrapper);
                if (instUpdated == 0) {
                    log.warn("审批通过状态更新失败（可能已被并发处理）, instanceId={}", instanceId);
                } else {
                    log.info("审批通过, instanceId={}", instanceId);
                    // 刷新实例数据用于回调
                    instance.setStatus(Constants.APPROVAL_APPROVED);
                    instance.setFinishTime(LocalDateTime.now());
                    syncBusinessStatus(instance);
                }
            }
        }
    }

    // ==================== 撤回审批 ====================

    @Override
    @Transactional
    public void cancel(Long instanceId, Long userId) {
        ApprovalInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new BusinessException("审批实例不存在");
        }

        // 校验：只有申请人本人可以撤回
        if (!instance.getApplicantId().equals(userId)) {
            throw new BusinessException("只能撤回自己的审批申请");
        }

        // 校验：只有审批中的实例可以撤回
        if (!instance.getStatus().equals(Constants.APPROVAL_PENDING)) {
            if (instance.getStatus().equals(Constants.APPROVAL_APPROVED)) {
                throw new BusinessException("审批已通过，无法撤回");
            } else if (instance.getStatus().equals(Constants.APPROVAL_REJECTED)) {
                throw new BusinessException("审批已被驳回，无法撤回");
            } else if (instance.getStatus().equals(Constants.APPROVAL_CANCELLED)) {
                throw new BusinessException("审批已被撤回");
            }
        }

        // 设置为已撤回
        instance.setStatus(Constants.APPROVAL_CANCELLED);
        instance.setFinishTime(LocalDateTime.now());
        instanceMapper.updateById(instance);

        // 同步业务记录状态
        syncBusinessStatus(instance);

        log.info("审批已撤回, instanceId={}, applicantId={}", instanceId, userId);
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
     * 根据配置解析审批人ID列表
     * <p>
     * 支持并行审批：ROLE 类型会返回所有拥有该角色的用户ID，
     * DEPT_LEADER 和 USER 类型返回单个元素的列表。
     */
    private List<Long> resolveApprovers(ApproverConfig config, User applicant) {
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
                return Collections.singletonList(dept.getLeaderId());
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
                // 查找拥有该角色的所有用户（并行审批：不再只取第一个）
                List<UserRole> userRoles = userRoleMapper.selectList(
                        new LambdaQueryWrapper<UserRole>()
                                .eq(UserRole::getRoleId, role.getId())
                );
                if (userRoles.isEmpty()) {
                    throw new BusinessException("没有用户拥有角色【" + role.getRoleName() + "】，无法提交审批");
                }
                // 返回所有拥有该角色的用户ID
                return userRoles.stream()
                        .map(UserRole::getUserId)
                        .distinct()
                        .collect(Collectors.toList());
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
                return Collections.singletonList(approverId);
            }
            default:
                throw new BusinessException("第" + level + "级审批人类型不支持：" + type);
        }
    }

    // ==================== 业务回调：审批完成后同步关联业务状态 ====================

    /**
     * 审批终态（通过/驳回）时，同步更新关联业务记录的状态
     */
    private void syncBusinessStatus(ApprovalInstance instance) {
        String businessType = instance.getBusinessType();
        Long businessId = instance.getBusinessId();
        if (businessType == null || businessId == null) {
            return;
        }

        switch (businessType) {
            case Constants.BUSINESS_TYPE_LEAVE: {
                LeaveRequest leave = leaveRequestMapper.selectById(businessId);
                if (leave != null) {
                    leave.setStatus(instance.getStatus());
                    leave.setApprovalTime(instance.getFinishTime());
                    leaveRequestMapper.updateById(leave);
                    log.info("请假申请状态已同步, leaveId={}, status={}", businessId, instance.getStatus());
                }
                break;
            }
            case Constants.BUSINESS_TYPE_EXPENSE: {
                ExpenseRequest expense = expenseMapper.selectById(businessId);
                if (expense != null) {
                    expense.setStatus(instance.getStatus());
                    expense.setApprovalTime(instance.getFinishTime());
                    expenseMapper.updateById(expense);
                    log.info("报销申请状态已同步, expenseId={}, status={}", businessId, instance.getStatus());
                }
                break;
            }
            default:
                log.debug("未知业务类型, businessType={}, 跳过状态同步", businessType);
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
