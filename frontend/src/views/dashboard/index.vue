<template>
  <div class="dashboard">
    <h2>数据看板</h2>
    <!-- 系统概览卡片 -->
    <el-row :gutter="20" class="overview-row">
      <el-col :span="6" v-for="item in overviewCards" :key="item.label">
        <el-card shadow="hover">
          <div class="card-item">
            <el-icon :size="36" :color="item.color"><component :is="item.icon" /></el-icon>
            <div class="card-info">
              <p class="card-value">{{ item.value }}</p>
              <p class="card-label">{{ item.label }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 日程提醒 -->
    <el-row v-if="reminders.length > 0" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div style="display: flex; align-items: center; gap: 8px;">
              <el-icon color="#E6A23C"><Bell /></el-icon>
              <span>近期日程提醒（未来24小时）</span>
            </div>
          </template>
          <div v-for="r in reminders" :key="r.id" class="reminder-item">
            <el-tag :type="r.priority >= 3 ? 'danger' : r.priority >= 2 ? 'warning' : 'info'" size="small">
              {{ r.priority >= 3 ? '紧急' : r.priority >= 2 ? '重要' : '普通' }}
            </el-tag>
            <span class="reminder-title">{{ r.title }}</span>
            <span class="reminder-time">{{ formatReminderTime(r.startTime) }}</span>
            <span v-if="r.location" class="reminder-location">📍 {{ r.location }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>近7天考勤趋势</template>
          <div ref="attendanceChartRef" style="height: 320px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>审批状态分布</template>
          <div ref="approvalChartRef" style="height: 320px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>报销类型分布</template>
          <div ref="expenseChartRef" style="height: 320px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>模块快捷入口</template>
          <el-row :gutter="16">
            <el-col :span="8" v-for="m in modules" :key="m.name" style="margin-bottom: 16px;">
              <el-card shadow="hover" class="module-card" @click="navigateTo(m.path)">
                <el-icon :size="28" color="#409EFF"><component :is="m.icon" /></el-icon>
                <h4>{{ m.name }}</h4>
                <p class="module-desc">{{ m.desc }}</p>
              </el-card>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getOverview, getAttendanceTrend, getApprovalDistribution, getExpenseDistribution } from '@/api/dashboard'
import { getScheduleReminders } from '@/api/schedule'
import * as echarts from 'echarts'

const router = useRouter()

const overviewCards = ref([
  { label: '用户总数', value: '--', icon: 'User', color: '#409EFF' },
  { label: '部门总数', value: '--', icon: 'OfficeBuilding', color: '#67C23A' },
  { label: '角色总数', value: '--', icon: 'Avatar', color: '#E6A23C' },
  { label: '公告总数', value: '--', icon: 'Bell', color: '#F56C6C' }
])

const modules = [
  { name: '系统管理', icon: 'Setting', desc: '用户/角色/菜单/部门', path: '/system/user' },
  { name: '考勤管理', icon: 'Timer', desc: '签到签退/请假/统计', path: '/attendance' },
  { name: '审批中心', icon: 'DocumentChecked', desc: '多级审批/撤回', path: '/approval' },
  { name: '公告通知', icon: 'Bell', desc: '发布/编辑/删除', path: '/notice' },
  { name: '日程管理', icon: 'Calendar', desc: '个人日程/查询', path: '/schedule' },
  { name: '报销管理', icon: 'Money', desc: '报销申请/统计', path: '/expense' }
]

function navigateTo(path) {
  router.push(path)
}

// ECharts 实例
const attendanceChartRef = ref(null)
const approvalChartRef = ref(null)
const expenseChartRef = ref(null)
let attendanceChart = null
let approvalChart = null
let expenseChart = null

// 日程提醒
const reminders = ref([])

function formatReminderTime(str) {
  if (!str) return ''
  const d = new Date(str)
  const now = new Date()
  const diffMs = d.getTime() - now.getTime()
  const diffH = Math.round(diffMs / (1000 * 60 * 60) * 10) / 10
  const month = d.getMonth() + 1
  const day = d.getDate()
  const hour = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  if (diffH <= 1) return `${month}月${day}日 ${hour}:${min}（${Math.round(diffMs / 60000)}分钟后）`
  return `${month}月${day}日 ${hour}:${min}（约${diffH.toFixed(1)}小时后）`
}

async function loadReminders() {
  try {
    const res = await getScheduleReminders()
    reminders.value = res.data || []
  } catch { reminders.value = [] }
}

// 概览数据
async function loadOverview() {
  try {
    const res = await getOverview()
    if (res.data) {
      const sys = res.data.system || res.data
      overviewCards.value[0].value = sys.userCount ?? '--'
      overviewCards.value[1].value = sys.deptCount ?? '--'
      overviewCards.value[2].value = sys.roleCount ?? '--'
      overviewCards.value[3].value = sys.noticeCount ?? '--'
    }
  } catch { /* 使用默认值 */ }
}

// 考勤趋势图
async function loadAttendanceTrend() {
  if (!attendanceChartRef.value) return
  try {
    const res = await getAttendanceTrend()
    const data = res.data || []
    const dates = data.map(d => d.date || '')
    const normal = data.map(d => d.normal || 0)
    const late = data.map(d => d.late || 0)
    const early = data.map(d => d.early || 0)
    const absent = data.map(d => d.absent || 0)

    attendanceChart = echarts.init(attendanceChartRef.value)
    attendanceChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['正常', '迟到', '早退', '缺勤'], bottom: 0 },
      grid: { left: 40, right: 20, top: 20, bottom: 40 },
      xAxis: { type: 'category', data: dates },
      yAxis: { type: 'value' },
      series: [
        { name: '正常', type: 'line', data: normal, smooth: true, itemStyle: { color: '#67C23A' } },
        { name: '迟到', type: 'line', data: late, smooth: true, itemStyle: { color: '#E6A23C' } },
        { name: '早退', type: 'line', data: early, smooth: true, itemStyle: { color: '#F56C6C' } },
        { name: '缺勤', type: 'line', data: absent, smooth: true, itemStyle: { color: '#909399' } }
      ]
    })
  } catch { /* 忽略 */ }
}

// 审批状态分布
async function loadApprovalDistribution() {
  if (!approvalChartRef.value) return
  try {
    const res = await getApprovalDistribution()
    const data = res.data || []
    const pieData = data.map(d => ({ name: d.name, value: d.value }))

    approvalChart = echarts.init(approvalChartRef.value)
    approvalChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0 },
      series: [{
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['50%', '45%'],
        data: pieData,
        label: { show: true, formatter: '{b}\n{d}%' },
        itemStyle: {
          color: (params) => {
            const colors = { '审批中': '#E6A23C', '已通过': '#67C23A', '已驳回': '#F56C6C' }
            return colors[params.name] || '#409EFF'
          }
        }
      }]
    })
  } catch { /* 忽略 */ }
}

// 报销类型分布
async function loadExpenseDistribution() {
  if (!expenseChartRef.value) return
  try {
    const res = await getExpenseDistribution()
    const data = res.data || []
    const names = data.map(d => d.name || '')
    const amounts = data.map(d => d.amount || 0)
    const counts = data.map(d => d.count || 0)

    expenseChart = echarts.init(expenseChartRef.value)
    expenseChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['金额(元)', '数量(笔)'], bottom: 0 },
      grid: { left: 60, right: 60, top: 20, bottom: 40 },
      xAxis: { type: 'category', data: names },
      yAxis: [
        { type: 'value', name: '金额(元)' },
        { type: 'value', name: '数量(笔)' }
      ],
      series: [
        { name: '金额(元)', type: 'bar', data: amounts, itemStyle: { color: '#409EFF' }, barMaxWidth: 40 },
        { name: '数量(笔)', type: 'line', yAxisIndex: 1, data: counts, itemStyle: { color: '#67C23A' } }
      ]
    })
  } catch { /* 忽略 */ }
}

// 窗口 resize
function handleResize() {
  attendanceChart?.resize()
  approvalChart?.resize()
  expenseChart?.resize()
}

onMounted(async () => {
  loadOverview()
  loadReminders()
  // 等待 DOM 渲染后再初始化图表
  await new Promise(resolve => setTimeout(resolve, 300))
  loadAttendanceTrend()
  loadApprovalDistribution()
  loadExpenseDistribution()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  attendanceChart?.dispose()
  approvalChart?.dispose()
  expenseChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.dashboard h2 {
  margin-bottom: 20px;
}

.card-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  margin: 0;
}

.card-label {
  color: #909399;
  margin: 4px 0 0;
}

.module-card {
  text-align: center;
  cursor: pointer;
}

.module-card:hover {
  border-color: #409EFF;
}

.module-desc {
  color: #909399;
  font-size: 12px;
  margin-top: 8px;
}

.reminder-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
}
.reminder-item:last-child { border-bottom: none; }
.reminder-title { font-weight: 500; flex: 1; }
.reminder-time { color: #E6A23C; font-size: 13px; }
.reminder-location { color: #909399; font-size: 13px; }

</style>
