package com.oasystem.approval.service;

import com.oasystem.approval.entity.ApprovalTemplate;

import java.util.List;

/**
 * 审批模板管理 Service 接口
 */
public interface ApprovalTemplateService {

    /**
     * 查询所有模板
     */
    List<ApprovalTemplate> listAll();

    /**
     * 根据ID查询模板
     */
    ApprovalTemplate getById(Long id);

    /**
     * 创建模板
     */
    void create(ApprovalTemplate template);

    /**
     * 更新模板
     */
    void update(ApprovalTemplate template);

    /**
     * 删除模板
     */
    void delete(Long id);
}
