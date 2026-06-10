package com.oasystem.approval.service;

import com.oasystem.approval.dto.StartApprovalDTO;
import com.oasystem.approval.entity.ApprovalInstance;
import com.oasystem.approval.entity.ApprovalRecord;

import java.util.List;

/**
 * 审批中心 Service 接口
 */
public interface ApprovalService {

    /**
     * 发起审批
     *
     * @param dto         发起审批请求
     * @param applicantId 申请人ID
     * @return 审批实例ID
     */
    Long start(StartApprovalDTO dto, Long applicantId);

    /**
     * 待审批列表（当前用户作为审批人且尚未审批的）
     */
    List<ApprovalInstance> getTodo(Long userId);

    /**
     * 已审批列表（当前用户已经审批过的）
     */
    List<ApprovalInstance> getDone(Long userId);

    /**
     * 我的申请
     */
    List<ApprovalInstance> getMy(Long userId);

    /**
     * 审批操作（同意/驳回）
     *
     * @param instanceId  审批实例ID
     * @param result      审批结果（1=同意，2=驳回）
     * @param comment     审批意见
     * @param currentUserId 当前操作用户ID
     */
    void approve(Long instanceId, int result, String comment, Long currentUserId);

    /**
     * 查询审批记录
     */
    List<ApprovalRecord> getRecords(Long instanceId);
}
