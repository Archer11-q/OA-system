package com.oasystem.approval.service.impl;

import com.oasystem.approval.entity.ApprovalInstance;
import com.oasystem.approval.entity.ApprovalRecord;
import com.oasystem.approval.mapper.ApprovalInstanceMapper;
import com.oasystem.approval.mapper.ApprovalRecordMapper;
import com.oasystem.approval.mapper.ApprovalTemplateMapper;
import com.oasystem.approval.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalTemplateMapper templateMapper;
    private final ApprovalInstanceMapper instanceMapper;
    private final ApprovalRecordMapper recordMapper;

    @Override
    @Transactional
    public Long start(ApprovalInstance instance) {
        // 填充模板信息（如果 templateId 存在）
        if (instance.getTemplateId() != null) {
            var t = templateMapper.selectById(instance.getTemplateId());
            if (t != null) {
                instance.setTotalLevels(t.getApprovalLevels());
            }
        }
        if (instance.getTotalLevels() == null || instance.getTotalLevels() <= 0) {
            instance.setTotalLevels(1);
        }
        instance.setCurrentLevel(1);
        instance.setStatus(0); // 审批中
        instanceMapper.insert(instance);
        return instance.getId();
    }

    @Override
    public List<ApprovalInstance> getTodo(Long userId) {
        // 简化：返回所有正在审批且申请人不是当前用户
        return instanceMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ApprovalInstance>()
                .eq(ApprovalInstance::getStatus, 0)
                .ne(ApprovalInstance::getApplicantId, userId)
        );
    }

    @Override
    public List<ApprovalInstance> getDone(Long userId) {
        // 返回已完成或已驳回的实例
        return instanceMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ApprovalInstance>()
                .ne(ApprovalInstance::getStatus, 0)
        );
    }

    @Override
    public List<ApprovalInstance> getMy(Long userId) {
        return instanceMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ApprovalInstance>()
                .eq(ApprovalInstance::getApplicantId, userId)
        );
    }

    @Override
    @Transactional
    public void approve(Long instanceId, int result, String comment, Long approverId) {
        ApprovalInstance inst = instanceMapper.selectById(instanceId);
        if (inst == null) throw new RuntimeException("审批实例不存在");

        ApprovalRecord rec = new ApprovalRecord();
        rec.setInstanceId(instanceId);
        rec.setLevel(inst.getCurrentLevel());
        rec.setApproverId(approverId);
        rec.setResult(result);
        rec.setComment(comment);
        rec.setApprovalTime(LocalDateTime.now());
        recordMapper.insert(rec);

        if (result == 2) { // 驳回
            inst.setStatus(2);
            instanceMapper.updateById(inst);
            return;
        }

        // 同意
        if (inst.getCurrentLevel() < inst.getTotalLevels()) {
            inst.setCurrentLevel(inst.getCurrentLevel() + 1);
            instanceMapper.updateById(inst);
        } else {
            inst.setStatus(1);
            inst.setFinishTime(LocalDateTime.now());
            instanceMapper.updateById(inst);
        }
    }

    @Override
    public List<ApprovalRecord> getRecords(Long instanceId) {
        return recordMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getInstanceId, instanceId)
        );
    }
}

