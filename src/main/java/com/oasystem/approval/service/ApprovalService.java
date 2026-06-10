package com.oasystem.approval.service;

import com.oasystem.approval.entity.ApprovalInstance;
import com.oasystem.approval.entity.ApprovalRecord;

import java.util.List;

public interface ApprovalService {
    Long start(ApprovalInstance instance);

    List<ApprovalInstance> getTodo(Long userId);

    List<ApprovalInstance> getDone(Long userId);

    List<ApprovalInstance> getMy(Long userId);

    void approve(Long instanceId, int result, String comment, Long approverId);

    List<ApprovalRecord> getRecords(Long instanceId);
}

