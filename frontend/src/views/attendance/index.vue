<template>
  <div class="page-container">
    <!-- 顶部操作区 -->
    <el-row :gutter="20" class="top-row">
      <el-col :span="8">
        <el-card>
          <template #header><span>今日考勤</span></template>
          <div class="today-center">
            <div class="today-time">{{ currentTime }}</div>
            <div class="today-date">{{ todayDate }}</div>
            <div class="today-actions">
              <el-button type="primary" size="large" :disabled="signedIn" :loading="signLoading" @click="handleSignIn">
                签到
              </el-button>
              <el-button type="success" size="large" :disabled="!signedIn || signedOut" :loading="signLoading" @click="handleSignOut">
                签退
              </el-button>
            </div>
            <div class="today-status" v-if="todayStatus">
              <el-tag :type="todayStatusTag(todayStatus)" size="small">{{ todayStatusLabel(todayStatus) }}</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header><span>请假申请</span></template>
          <div class="leave-area">
            <p style="color:#909399;margin-bottom:16px">提交请假申请后将自动创建审批流程</p>
            <el-button type="warning" size="large" @click="leaveDialogVisible = true">申请请假</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header><span>月度统计（{{ reportMonth }}）</span></template>
          <div v-loading="reportLoading" class="report-area">
            <el-row :gutter="12">
              <el-col :span="6"><div class="stat-box"><div class="stat-num">{{ report.attendDays }}</div><div class="stat-label">出勤</div></div></el-col>
              <el-col :span="6"><div class="stat-box"><div class="stat-num late">{{ report.lateDays }}</div><div class="stat-label">迟到</div></div></el-col>
              <el-col :span="6"><div class="stat-box"><div class="stat-num early">{{ report.earlyDays }}</div><div class="stat-label">早退</div></div></el-col>
              <el-col :span="6"><div class="stat-box"><div class="stat-num">{{ report.workHours }}</div><div class="stat-label">工时h</div></div></el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 考勤记录 -->
    <el-card style="margin-top:20px">
      <template #header>
        <div class="page-header">
          <span>考勤记录</span>
          <div>
            <el-month-picker v-model="queryMonth" placeholder="选择月份" value-format="YYYY-MM" style="width:160px" />
            <el-button type="primary" style="margin-left:8px" @click="fetchRecords">查询</el-button>
          </div>
        </div>
      </template>
      <el-table :data="recordsData" border stripe v-loading="recordsLoading">
        <el-table-column prop="attendanceDate" label="日期" width="120" />
        <el-table-column prop="signInTime" label="签到时间" width="170">
          <template #default="{ row }">{{ row.signInTime || '-' }}</template>
        </el-table-column>
        <el-table-column prop="signOutTime" label="签退时间" width="170">
          <template #default="{ row }">{{ row.signOutTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="recordStatusTag(row.status)">{{ recordStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="workHours" label="工时(h)" width="80" />
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      </el-table>
    </el-card>

    <!-- 请假对话框 -->
    <el-dialog v-model="leaveDialogVisible" title="请假申请" width="500px" @close="handleLeaveDialogClose">
      <el-form ref="leaveFormRef" :model="leaveForm" :rules="leaveRules" label-width="80px">
        <el-form-item label="请假类型" prop="leaveType">
          <el-select v-model="leaveForm.leaveType" placeholder="请选择类型" style="width:100%">
            <el-option label="年假" :value="1" />
            <el-option label="事假" :value="2" />
            <el-option label="病假" :value="3" />
            <el-option label="调休" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker v-model="leaveForm.startDate" type="date" placeholder="选择开始日期" value-format="YYYY-MM-DD" style="width:100%" @change="calcLeaveDays" />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker v-model="leaveForm.endDate" type="date" placeholder="选择结束日期" value-format="YYYY-MM-DD" style="width:100%" @change="calcLeaveDays" />
        </el-form-item>
        <el-form-item label="天数">
          <el-input-number v-model="leaveForm.days" :min="0.5" :step="0.5" style="width:100%" />
        </el-form-item>
        <el-form-item label="请假原因">
          <el-input v-model="leaveForm.reason" type="textarea" :rows="3" placeholder="请输入请假原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="leaveDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="leaveSubmitLoading" @click="handleLeaveSubmit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { signIn, signOut, getAttendanceRecords, submitLeave, getMonthlyReport, getDailyStatus } from '@/api/attendance'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

// 今日考勤
const currentTime = ref('')
const todayDate = ref('')
const signedIn = ref(false)
const signedOut = ref(false)
const todayStatus = ref(null)
const signLoading = ref(false)
let timer = null

function updateTime() {
  const now = dayjs()
  currentTime.value = now.format('HH:mm:ss')
  todayDate.value = now.format('YYYY年MM月DD日 dddd')
}

function todayStatusTag(s) {
  const map = { NORMAL: 'success', LATE: 'warning', EARLY: 'warning', ABSENT: 'danger' }
  return map[s] || 'info'
}

function todayStatusLabel(s) {
  const map = { NORMAL: '正常', LATE: '迟到', EARLY: '早退', ABSENT: '缺勤' }
  return map[s] || s || ''
}

// 月度统计
const reportMonth = ref(dayjs().format('YYYY-MM'))
const reportLoading = ref(false)
const report = reactive({ attendDays: 0, lateDays: 0, earlyDays: 0, absenceDays: 0, totalHours: 0 })

// 考勤记录
const queryMonth = ref(dayjs().format('YYYY-MM'))
const recordsLoading = ref(false)
const recordsData = ref([])

function recordStatusTag(s) {
  const map = { NORMAL: 'success', LATE: 'warning', EARLY: 'warning', ABSENT: 'danger' }
  return map[s] || 'info'
}

function recordStatusLabel(s) {
  const map = { NORMAL: '正常', LATE: '迟到', EARLY: '早退', ABSENT: '缺勤' }
  return map[s] || s
}

// 请假
const leaveDialogVisible = ref(false)
const leaveSubmitLoading = ref(false)
const leaveFormRef = ref(null)

const defaultLeaveForm = () => ({
  leaveType: 2,
  startDate: '',
  endDate: '',
  days: 1,
  reason: ''
})

const leaveForm = reactive(defaultLeaveForm())

const leaveRules = {
  leaveType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }]
}

function calcLeaveDays() {
  if (leaveForm.startDate && leaveForm.endDate) {
    const start = dayjs(leaveForm.startDate)
    const end = dayjs(leaveForm.endDate)
    const diff = end.diff(start, 'day') + 1
    leaveForm.days = Math.max(0.5, diff)
  }
}

function handleLeaveDialogClose() {
  leaveFormRef.value?.resetFields()
}

async function handleLeaveSubmit() {
  const valid = await leaveFormRef.value.validate().catch(() => false)
  if (!valid) return
  leaveSubmitLoading.value = true
  try {
    await submitLeave(leaveForm)
    ElMessage.success('请假申请已提交，请等待审批')
    leaveDialogVisible.value = false
  } catch {
    // 错误由拦截器处理
  } finally {
    leaveSubmitLoading.value = false
  }
}

// 查询今日状态
async function checkTodayStatus() {
  try {
    const month = dayjs().format('YYYY-MM')
    const res = await getDailyStatus(month)
    const today = dayjs().format('YYYY-MM-DD')
    const list = res.data || []
    const todayRecord = list.find(item => item.date === today || item.attendanceDate === today)
    if (todayRecord) {
      todayStatus.value = todayRecord.status
      signedIn.value = !!todayRecord.signInTime
      signedOut.value = !!todayRecord.signOutTime
    } else {
      todayStatus.value = null
      signedIn.value = false
      signedOut.value = false
    }
  } catch { /* 忽略 */ }
}

async function handleSignIn() {
  signLoading.value = true
  try {
    await signIn({ location: '办公室' })
    ElMessage.success('签到成功！')
    checkTodayStatus()
    fetchRecords()
    fetchReport()
  } catch {
    // 错误由拦截器处理
  } finally {
    signLoading.value = false
  }
}

async function handleSignOut() {
  signLoading.value = true
  try {
    await signOut({ location: '办公室' })
    ElMessage.success('签退成功！')
    checkTodayStatus()
    fetchRecords()
    fetchReport()
  } catch {
    // 错误由拦截器处理
  } finally {
    signLoading.value = false
  }
}

// 查询考勤记录
async function fetchRecords() {
  recordsLoading.value = true
  try {
    const res = await getAttendanceRecords(queryMonth.value)
    recordsData.value = res.data || []
  } finally {
    recordsLoading.value = false
  }
}

// 查询月度统计
async function fetchReport() {
  reportLoading.value = true
  try {
    const res = await getMonthlyReport(reportMonth.value)
    const data = res.data || {}
    report.attendDays = data.attendDays ?? 0
    report.lateDays = data.lateDays ?? 0
    report.earlyDays = data.earlyDays ?? 0
    report.absenceDays = data.absenceDays ?? 0
    report.totalHours = data.totalHours ?? 0
  } finally {
    reportLoading.value = false
  }
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
  checkTodayStatus()
  fetchRecords()
  fetchReport()
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.page-container { min-height: 100%; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
.top-row { margin-bottom: 0; }
.today-center { text-align: center; }
.today-time { font-size: 36px; font-weight: bold; color: #303133; }
.today-date { color: #909399; margin-bottom: 16px; }
.today-actions { display: flex; gap: 12px; justify-content: center; margin-bottom: 12px; }
.today-actions .el-button { flex: 1; }
.leave-area { text-align: center; }
.report-area .stat-box { text-align: center; padding: 8px 0; }
.report-area .stat-num { font-size: 24px; font-weight: bold; color: #409EFF; }
.report-area .stat-num.late { color: #E6A23C; }
.report-area .stat-num.early { color: #F56C6C; }
.report-area .stat-label { font-size: 12px; color: #909399; margin-top: 4px; }
</style>
