package com.oasystem.approval.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oasystem.approval.entity.ApprovalInstance;
import com.oasystem.approval.entity.ApprovalTemplate;
import com.oasystem.approval.mapper.ApprovalInstanceMapper;
import com.oasystem.approval.mapper.ApprovalTemplateMapper;
import com.oasystem.approval.service.ApprovalTemplateService;
import com.oasystem.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;

/**
 * 审批模板管理 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalTemplateServiceImpl extends ServiceImpl<ApprovalTemplateMapper, ApprovalTemplate> implements ApprovalTemplateService {

    private final ApprovalTemplateMapper templateMapper;
    private final ApprovalInstanceMapper instanceMapper;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public List<ApprovalTemplate> listAll() {
        return templateMapper.selectList(
                new LambdaQueryWrapper<ApprovalTemplate>()
                        .orderByDesc(ApprovalTemplate::getCreateTime)
        );
    }

    @Override
    public ApprovalTemplate getById(Long id) {
        ApprovalTemplate t = templateMapper.selectById(id);
        if (t == null) {
            throw new BusinessException("审批模板不存在");
        }
        return t;
    }

    @Override
    public void create(ApprovalTemplate template) {
        // 模板编码不能为空
        if (template.getTemplateCode() == null || template.getTemplateCode().isBlank()) {
            throw new BusinessException("模板编码不能为空");
        }
        // 模板编码唯一性校验
        ApprovalTemplate exist = templateMapper.selectOne(
                new LambdaQueryWrapper<ApprovalTemplate>()
                        .eq(ApprovalTemplate::getTemplateCode, template.getTemplateCode())
        );
        if (exist != null) {
            throw new BusinessException("模板编码已存在");
        }
        // 校验并设置审批级数
        template.setApprovalLevels(validateApproversConfig(template.getApproversConfig()));
        templateMapper.insert(template);
        log.info("审批模板创建成功, id={}, code={}", template.getId(), template.getTemplateCode());
    }

    @Override
    public void update(ApprovalTemplate template) {
        ApprovalTemplate exist = templateMapper.selectById(template.getId());
        if (exist == null) {
            throw new BusinessException("审批模板不存在");
        }
        // 模板编码唯一性校验（排除自身）
        if (template.getTemplateCode() != null && !template.getTemplateCode().isBlank()) {
            ApprovalTemplate sameCode = templateMapper.selectOne(
                    new LambdaQueryWrapper<ApprovalTemplate>()
                            .eq(ApprovalTemplate::getTemplateCode, template.getTemplateCode())
                            .ne(ApprovalTemplate::getId, template.getId())
            );
            if (sameCode != null) {
                throw new BusinessException("模板编码已存在");
            }
        }
        // 校验并设置审批级数
        if (template.getApproversConfig() != null) {
            template.setApprovalLevels(validateApproversConfig(template.getApproversConfig()));
        }
        templateMapper.updateById(template);
        log.info("审批模板更新成功, id={}", template.getId());
    }

    @Override
    public void delete(Long id) {
        ApprovalTemplate t = templateMapper.selectById(id);
        if (t == null) {
            throw new BusinessException("审批模板不存在");
        }
        // 检查是否有审批实例使用了该模板
        long count = instanceMapper.selectCount(
                new LambdaQueryWrapper<ApprovalInstance>()
                        .eq(ApprovalInstance::getTemplateId, id)
        );
        if (count > 0) {
            throw new BusinessException("该模板已被审批实例使用，无法删除");
        }
        templateMapper.deleteById(id);
        log.info("审批模板删除成功, id={}", id);
    }

    /**
     * 校验 approversConfig JSON 格式，返回审批级数
     */
    private int validateApproversConfig(String config) {
        if (config == null || config.isBlank()) {
            throw new BusinessException("审批人配置不能为空");
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(config);
            if (!root.isArray() || root.isEmpty()) {
                throw new BusinessException("审批人配置格式错误：应为非空数组");
            }
            for (int i = 0; i < root.size(); i++) {
                JsonNode item = root.get(i);
                // level 校验
                if (!item.has("level")) {
                    throw new BusinessException("第" + (i + 1) + "个审批人缺少 level 字段");
                }
                int level = item.get("level").asInt();
                if (level < 1) {
                    throw new BusinessException("审批级别必须大于等于1");
                }
                // type 校验
                if (!item.has("type")) {
                    throw new BusinessException("第" + level + "级审批人缺少 type 字段");
                }
                String type = item.get("type").asText();
                if (!"DEPT_LEADER".equals(type) && !"ROLE".equals(type) && !"USER".equals(type)) {
                    throw new BusinessException("第" + level + "级审批人类型不支持：" + type + "（仅支持 DEPT_LEADER/ROLE/USER）");
                }
                // value 校验
                if ("ROLE".equals(type) || "USER".equals(type)) {
                    if (!item.has("value") || item.get("value").asText().isBlank()) {
                        throw new BusinessException("第" + level + "级审批人缺少 value 字段");
                    }
                }
            }
            // 返回去重后的审批级别数（支持同一级别配置多个审批人）
            return (int) IntStream.range(0, root.size())
                    .mapToObj(i -> root.get(i).get("level").asInt())
                    .distinct()
                    .count();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("审批人配置JSON格式错误：" + e.getMessage());
        }
    }
}
