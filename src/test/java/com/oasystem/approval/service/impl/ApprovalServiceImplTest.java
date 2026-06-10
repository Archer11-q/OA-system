package com.oasystem.approval.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.oasystem.system.entity.*;
import com.oasystem.system.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 审批中心 Service 单元测试 — 多级审批引擎核心逻辑
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("审批中心 Service 单元测试")
class ApprovalServiceImplTest {

    @Mock private ApprovalTemplateMapper templateMapper;
    @Mock private ApprovalInstanceMapper instanceMapper;
    @Mock private ApprovalRecordMapper recordMapper;
    @Mock private UserMapper userMapper;
    @Mock private DeptMapper deptMapper;
    @Mock private RoleMapper roleMapper;
    @Mock private UserRoleMapper userRoleMapper;
    @Mock private LeaveRequestMapper leaveRequestMapper;
    @Mock private ExpenseMapper expenseMapper;

    @InjectMocks
    private ApprovalServiceImpl approvalService;

    private ApprovalTemplate template;
    private User applicant;
    private Dept dept;
    private Role adminRole;
    private User approver;

    @BeforeEach
    void setUp() {
        // 构建 2 级审批模板（部门负责人 → 管理员）
        template = new ApprovalTemplate();
        template.setId(1L);
        template.setTemplateName("请假审批");
        template.setTemplateCode("LEAVE_APPROVAL");
        template.setStatus(1);
        template.setApproversConfig("[{\"level\":1,\"type\":\"DEPT_LEADER\"},{\"level\":2,\"type\":\"ROLE\",\"value\":\"ROLE_ADMIN\"}]");

        // 申请人
        applicant = new User();
        applicant.setId(10L);
        applicant.setUsername("zhangsan");
        applicant.setDeptId(2L);

        // 部门
        dept = new Dept();
        dept.setId(2L);
        dept.setDeptName("技术部");
        dept.setLeaderId(20L);

        // 管理员角色
        adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setRoleName("超级管理员");
        adminRole.setRoleCode("ROLE_ADMIN");

        // 审批人
        approver = new User();
        approver.setId(30L);
        approver.setUsername("admin");
    }

    // ==================== 发起审批测试 ====================

    @Test
    @DisplayName("发起审批 — 正常流程：创建实例+预创建审批记录")
    void testStart_Success() {
        // given
        StartApprovalDTO dto = new StartApprovalDTO();
        dto.setTemplateId(1L);
        dto.setTitle("请假申请 - 2天");
        dto.setContent("{\"days\":2}");
        dto.setBusinessType(Constants.BUSINESS_TYPE_LEAVE);
        dto.setBusinessId(100L);

        when(templateMapper.selectById(1L)).thenReturn(template);
        when(userMapper.selectById(10L)).thenReturn(applicant);
        when(deptMapper.selectById(2L)).thenReturn(dept);

        // ROLE 类型审批人解析
        when(roleMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(adminRole);
        UserRole ur = new UserRole();
        ur.setUserId(30L);
        ur.setRoleId(1L);
        when(userRoleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(ur));
        when(userMapper.selectById(30L)).thenReturn(approver);

        when(instanceMapper.insert(any(ApprovalInstance.class))).thenAnswer(inv -> {
            ApprovalInstance inst = inv.getArgument(0);
            inst.setId(200L);
            return 1;
        });
        when(recordMapper.insert(any(ApprovalRecord.class))).thenReturn(1);

        // when
        Long instanceId = approvalService.start(dto, 10L);

        // then
        assertNotNull(instanceId);
        assertEquals(200L, instanceId);

        // 验证实例创建时包含业务关联字段
        ArgumentCaptor<ApprovalInstance> instanceCaptor = ArgumentCaptor.forClass(ApprovalInstance.class);
        verify(instanceMapper).insert(instanceCaptor.capture());
        ApprovalInstance saved = instanceCaptor.getValue();
        assertEquals(Constants.BUSINESS_TYPE_LEAVE, saved.getBusinessType());
        assertEquals(100L, saved.getBusinessId());
        assertEquals(2, saved.getTotalLevels());
        assertEquals(1, saved.getCurrentLevel());
        assertEquals(Constants.APPROVAL_PENDING, saved.getStatus());

        // 验证预创建了 2 条审批记录
        verify(recordMapper, times(2)).insert(any(ApprovalRecord.class));
    }

    @Test
    @DisplayName("发起审批 — 模板不存在")
    void testStart_TemplateNotFound() {
        StartApprovalDTO dto = new StartApprovalDTO();
        dto.setTemplateId(999L);
        dto.setTitle("测试");

        when(templateMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> approvalService.start(dto, 10L));
        verify(instanceMapper, never()).insert(any());
    }

    @Test
    @DisplayName("发起审批 — 模板已禁用")
    void testStart_TemplateDisabled() {
        template.setStatus(0); // 禁用
        StartApprovalDTO dto = new StartApprovalDTO();
        dto.setTemplateId(1L);
        dto.setTitle("测试");

        when(templateMapper.selectById(1L)).thenReturn(template);

        assertThrows(BusinessException.class, () -> approvalService.start(dto, 10L));
    }

    @Test
    @DisplayName("发起审批 — 申请人未分配部门时抛出异常")
    void testStart_ApplicantNoDept() {
        applicant.setDeptId(null);
        StartApprovalDTO dto = new StartApprovalDTO();
        dto.setTemplateId(1L);
        dto.setTitle("测试");

        when(templateMapper.selectById(1L)).thenReturn(template);
        when(userMapper.selectById(10L)).thenReturn(applicant);

        assertThrows(BusinessException.class, () -> approvalService.start(dto, 10L));
    }

    // ==================== 审批操作测试 ====================

    @Test
    @DisplayName("审批操作 — 同意后推进到下一级")
    void testApprove_AdvanceToNextLevel() {
        // given: 第 1 级审批中
        ApprovalInstance instance = createInstance(1L, 2, 1, Constants.APPROVAL_PENDING);
        ApprovalRecord record = createRecord(1L, 1L, 1, 20L, 0);

        when(instanceMapper.selectById(1L)).thenReturn(instance);
        when(recordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(record);
        when(recordMapper.updateById(any(ApprovalRecord.class))).thenReturn(1);
        when(instanceMapper.updateById(any(ApprovalInstance.class))).thenReturn(1);

        // when: 第 1 级审批人同意
        approvalService.approve(1L, Constants.APPROVAL_APPROVED, "同意请假", 20L);

        // then: 推进到第 2 级，状态仍为审批中
        assertEquals(2, instance.getCurrentLevel());
        assertEquals(Constants.APPROVAL_PENDING, instance.getStatus());
        verify(instanceMapper).updateById(instance);
    }

    @Test
    @DisplayName("审批操作 — 最后一级同意后审批通过，回调同步请假状态")
    void testApprove_FinalApproval_WithLeaveCallback() {
        // given: 第 2 级（最后一级）审批中
        ApprovalInstance instance = createInstance(1L, 2, 2, Constants.APPROVAL_PENDING);
        instance.setBusinessType(Constants.BUSINESS_TYPE_LEAVE);
        instance.setBusinessId(100L);
        ApprovalRecord record = createRecord(2L, 1L, 2, 30L, 0);

        when(instanceMapper.selectById(1L)).thenReturn(instance);
        when(recordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(record);
        when(recordMapper.updateById(any(ApprovalRecord.class))).thenReturn(1);
        when(instanceMapper.updateById(any(ApprovalInstance.class))).thenReturn(1);

        // 业务回调：更新请假状态
        LeaveRequest leave = new LeaveRequest();
        leave.setId(100L);
        leave.setStatus(Constants.APPROVAL_PENDING);
        when(leaveRequestMapper.selectById(100L)).thenReturn(leave);
        when(leaveRequestMapper.updateById(any(LeaveRequest.class))).thenReturn(1);

        // when: 最后一级审批人同意
        approvalService.approve(1L, Constants.APPROVAL_APPROVED, "同意", 30L);

        // then: 审批通过
        assertEquals(Constants.APPROVAL_APPROVED, instance.getStatus());
        assertNotNull(instance.getFinishTime());
        // 验证请假状态被同步
        assertEquals(Constants.APPROVAL_APPROVED, leave.getStatus());
        verify(leaveRequestMapper).updateById(leave);
    }

    @Test
    @DisplayName("审批操作 — 驳回，回调同步请假状态")
    void testApprove_Reject_WithLeaveCallback() {
        // given: 第 1 级审批中
        ApprovalInstance instance = createInstance(1L, 2, 1, Constants.APPROVAL_PENDING);
        instance.setBusinessType(Constants.BUSINESS_TYPE_LEAVE);
        instance.setBusinessId(101L);
        ApprovalRecord record = createRecord(1L, 1L, 1, 20L, 0);

        when(instanceMapper.selectById(1L)).thenReturn(instance);
        when(recordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(record);
        when(recordMapper.updateById(any(ApprovalRecord.class))).thenReturn(1);
        when(instanceMapper.updateById(any(ApprovalInstance.class))).thenReturn(1);

        LeaveRequest leave = new LeaveRequest();
        leave.setId(101L);
        leave.setStatus(Constants.APPROVAL_PENDING);
        when(leaveRequestMapper.selectById(101L)).thenReturn(leave);
        when(leaveRequestMapper.updateById(any(LeaveRequest.class))).thenReturn(1);

        // when
        approvalService.approve(1L, Constants.APPROVAL_REJECTED, "不批准", 20L);

        // then: 驳回
        assertEquals(Constants.APPROVAL_REJECTED, instance.getStatus());
        assertEquals(Constants.APPROVAL_REJECTED, leave.getStatus());
        verify(leaveRequestMapper).updateById(leave);
    }

    @Test
    @DisplayName("审批操作 — 非指定审批人无权审批")
    void testApprove_NotAuthorized() {
        ApprovalInstance instance = createInstance(1L, 2, 1, Constants.APPROVAL_PENDING);
        ApprovalRecord record = createRecord(1L, 1L, 1, 20L, 0); // 审批人是 20L

        when(instanceMapper.selectById(1L)).thenReturn(instance);
        when(recordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(record);

        // 用户 999L 尝试审批 → 无权
        assertThrows(BusinessException.class,
                () -> approvalService.approve(1L, Constants.APPROVAL_APPROVED, "越权", 999L));
    }

    @Test
    @DisplayName("审批操作 — 已结束的审批不可再审批")
    void testApprove_AlreadyFinalized() {
        ApprovalInstance instance = createInstance(1L, 2, 1, Constants.APPROVAL_APPROVED); // 已通过

        when(instanceMapper.selectById(1L)).thenReturn(instance);

        assertThrows(BusinessException.class,
                () -> approvalService.approve(1L, Constants.APPROVAL_APPROVED, "重复审批", 20L));
    }

    // ==================== 待审批列表 ====================

    @Test
    @DisplayName("待审批列表 — 按当前用户+当前级别精确匹配")
    void testGetTodo_Success() {
        ApprovalRecord pendingRecord = createRecord(1L, 1L, 1, 20L, 0);
        ApprovalInstance instance = createInstance(1L, 2, 1, Constants.APPROVAL_PENDING);

        when(recordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(pendingRecord));
        when(instanceMapper.selectBatchIds(anySet())).thenReturn(List.of(instance));

        List<ApprovalInstance> todo = approvalService.getTodo(20L);

        assertNotNull(todo);
        assertEquals(1, todo.size());
        assertEquals(1L, todo.get(0).getId());
    }

    @Test
    @DisplayName("待审批列表 — 无待审批时返回空列表")
    void testGetTodo_Empty() {
        when(recordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        List<ApprovalInstance> todo = approvalService.getTodo(20L);

        assertTrue(todo.isEmpty());
    }

    // ==================== 业务回调测试 ====================

    @Test
    @DisplayName("业务回调 — 报销审批通过后同步报销状态")
    void testSyncBusinessStatus_Expense() {
        ApprovalInstance instance = createInstance(2L, 1, 1, Constants.APPROVAL_PENDING);
        instance.setBusinessType(Constants.BUSINESS_TYPE_EXPENSE);
        instance.setBusinessId(200L);
        ApprovalRecord record = createRecord(1L, 2L, 1, 30L, 0);

        // 单级审批模板
        template.setApproversConfig("[{\"level\":1,\"type\":\"USER\",\"value\":\"30\"}]");
        when(templateMapper.selectById(anyLong())).thenReturn(template);
        when(userMapper.selectById(anyLong())).thenReturn(applicant, approver);
        when(instanceMapper.selectById(2L)).thenReturn(instance);
        when(recordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(record);
        when(recordMapper.updateById(any(ApprovalRecord.class))).thenReturn(1);
        when(instanceMapper.updateById(any(ApprovalInstance.class))).thenReturn(1);

        // 报销实体
        ExpenseRequest expense = new ExpenseRequest();
        expense.setId(200L);
        expense.setStatus(Constants.APPROVAL_PENDING);
        when(expenseMapper.selectById(200L)).thenReturn(expense);
        when(expenseMapper.updateById(any(ExpenseRequest.class))).thenReturn(1);

        // when
        approvalService.approve(2L, Constants.APPROVAL_APPROVED, "批准报销", 30L);

        // then
        assertEquals(Constants.APPROVAL_APPROVED, expense.getStatus());
        verify(expenseMapper).updateById(expense);
    }

    // ==================== 辅助方法 ====================

    private ApprovalInstance createInstance(Long id, int totalLevels, int currentLevel, int status) {
        ApprovalInstance instance = new ApprovalInstance();
        instance.setId(id);
        instance.setTemplateId(1L);
        instance.setApplicantId(10L);
        instance.setTitle("测试审批");
        instance.setTotalLevels(totalLevels);
        instance.setCurrentLevel(currentLevel);
        instance.setStatus(status);
        instance.setCreateTime(LocalDateTime.now());
        return instance;
    }

    private ApprovalRecord createRecord(Long id, Long instanceId, int level, Long approverId, int result) {
        ApprovalRecord record = new ApprovalRecord();
        record.setId(id);
        record.setInstanceId(instanceId);
        record.setLevel(level);
        record.setApproverId(approverId);
        record.setResult(result);
        return record;
    }
}
