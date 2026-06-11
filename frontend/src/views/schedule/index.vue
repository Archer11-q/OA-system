<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="page-header">
          <span>日程管理</span>
          <div class="header-actions">
            <el-button-group style="margin-right: 12px">
              <el-button :type="viewMode === 'list' ? 'primary' : ''" size="small" @click="viewMode = 'list'">
                列表视图
              </el-button>
              <el-button :type="viewMode === 'calendar' ? 'primary' : ''" size="small" @click="switchToCalendar">
                日历视图
              </el-button>
            </el-button-group>
            <el-button type="primary" icon="Plus" @click="handleAdd">新增日程</el-button>
          </div>
        </div>
      </template>

      <!-- 搜索栏（列表视图专用） -->
      <el-form v-if="viewMode === 'list'" :model="searchParams" inline class="search-form">
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleSearch">查询</el-button>
          <el-button icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 列表视图 -->
      <template v-if="viewMode === 'list'">
        <el-table :data="tableData" border stripe v-loading="loading">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="title" label="标题" show-overflow-tooltip />
          <el-table-column label="开始时间" width="170">
            <template #default="{ row }">{{ formatDateTime(row.startTime) }}</template>
          </el-table-column>
          <el-table-column label="结束时间" width="170">
            <template #default="{ row }">{{ formatDateTime(row.endTime) }}</template>
          </el-table-column>
          <el-table-column label="类型" width="80">
            <template #default="{ row }">
              <el-tag size="small" :type="scheduleTypeTag(row.scheduleType)">
                {{ scheduleTypeLabel(row.scheduleType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="优先级" width="80">
            <template #default="{ row }">
              <el-tag size="small" :type="priorityTag(row.priority)">
                {{ priorityLabel(row.priority) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag size="small" :type="statusTag(row.status)">
                {{ statusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="location" label="地点" show-overflow-tooltip />
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
              <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <!-- 日历视图 -->
      <div v-else class="calendar-container">
        <FullCalendar ref="calendarRef" :options="calendarOptions" />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入日程标题" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="formData.startTime"
            type="datetime"
            placeholder="选择开始时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="formData.endTime"
            type="datetime"
            placeholder="选择结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="formData.scheduleType" placeholder="请选择类型" style="width: 100%">
            <el-option label="个人" :value="1" />
            <el-option label="部门" :value="2" />
            <el-option label="会议" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="formData.priority" placeholder="请选择优先级" style="width: 100%">
            <el-option label="普通" :value="1" />
            <el-option label="重要" :value="2" />
            <el-option label="紧急" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="地点">
          <el-input v-model="formData.location" placeholder="请输入地点" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.content" type="textarea" :rows="3" placeholder="请输入日程描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'
import { getScheduleList, addSchedule, updateSchedule, deleteSchedule } from '@/api/schedule'
import { ElMessage, ElMessageBox } from 'element-plus'

// ==================== 视图切换 ====================
const viewMode = ref('list')
const calendarRef = ref(null)

function switchToCalendar() {
  viewMode.value = 'calendar'
  // 切换后等 DOM 渲染完成再调整日历尺寸
  setTimeout(() => {
    calendarRef.value?.getApi()?.updateSize()
  }, 100)
}

// ==================== 列表视图 ====================
const loading = ref(false)
const tableData = ref([])

const dateRange = ref([])
const searchParams = reactive({ startDate: '', endDate: '' })

// ==================== 对话框 ====================
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const defaultForm = () => ({
  title: '',
  startTime: '',
  endTime: '',
  scheduleType: 1,
  priority: 1,
  location: '',
  content: '',
  status: 0
})

const formData = reactive(defaultForm())

const formRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

// ==================== 标签/格式化工具函数 ====================
function formatDateTime(str) {
  if (!str) return '-'
  return str.replace('T', ' ').substring(0, 16)
}

function scheduleTypeTag(type) {
  const map = { 1: '', 2: 'success', 3: 'warning' }
  return map[type] || ''
}

function scheduleTypeLabel(type) {
  const map = { 1: '个人', 2: '部门', 3: '会议' }
  return map[type] || '未知'
}

function priorityTag(p) {
  const map = { 1: 'info', 2: 'warning', 3: 'danger' }
  return map[p] || 'info'
}

function priorityLabel(p) {
  const map = { 1: '普通', 2: '重要', 3: '紧急' }
  return map[p] || '未知'
}

function statusTag(s) {
  const map = { 0: 'info', 1: 'success', 2: '', 3: 'danger' }
  return map[s] || 'info'
}

function statusLabel(s) {
  const map = { 0: '未开始', 1: '进行中', 2: '已完成', 3: '已取消' }
  return map[s] || '未知'
}

// ==================== 数据获取 ====================
async function fetchData(startDate, endDate) {
  loading.value = true
  try {
    const params = {}
    if (startDate) params.startDate = startDate
    else if (searchParams.startDate) params.startDate = searchParams.startDate
    if (endDate) params.endDate = endDate
    else if (searchParams.endDate) params.endDate = searchParams.endDate
    const res = await getScheduleList(params)
    tableData.value = res.data || []
    // 同时更新日历事件
    calendarEvents.value = (res.data || []).map(toCalendarEvent)
  } finally {
    loading.value = false
  }
}

// ==================== FullCalendar 配置 ====================
const calendarEvents = ref([])

// 将日程数据转换为 FullCalendar 事件格式
function toCalendarEvent(item) {
  const typeColors = { 1: '#409eff', 2: '#67c23a', 3: '#e6a23c' }
  return {
    id: String(item.id),
    title: item.title,
    start: item.startTime,
    end: item.endTime,
    backgroundColor: typeColors[item.scheduleType] || '#409eff',
    borderColor: typeColors[item.scheduleType] || '#409eff',
    extendedProps: {
      scheduleType: item.scheduleType,
      priority: item.priority,
      status: item.status,
      location: item.location,
      content: item.content
    }
  }
}

const calendarOptions = computed(() => ({
  plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
  initialView: 'dayGridMonth',
  headerToolbar: {
    left: 'prev,next today',
    center: 'title',
    right: 'dayGridMonth,timeGridWeek,timeGridDay'
  },
  buttonText: {
    today: '今天',
    month: '月',
    week: '周',
    day: '日'
  },
  locale: 'zh-cn',
  height: 'auto',
  events: calendarEvents.value,
  // 点击日程事件 → 编辑
  eventClick(info) {
    const event = info.event
    const props = event.extendedProps
    const row = {
      id: Number(event.id),
      title: event.title,
      startTime: event.startStr,
      endTime: event.endStr,
      scheduleType: props.scheduleType ?? 1,
      priority: props.priority ?? 1,
      status: props.status ?? 0,
      location: props.location || '',
      content: props.content || ''
    }
    handleEdit(row)
  },
  // 点击日期空白区域 → 新增（预填日期）
  dateClick(info) {
    dialogTitle.value = '新增日程'
    isEdit.value = false
    const dateStr = info.dateStr // YYYY-MM-DD
    Object.assign(formData, {
      ...defaultForm(),
      startTime: dateStr + ' 09:00:00',
      endTime: dateStr + ' 10:00:00'
    })
    dialogVisible.value = true
  },
  // 切换月份/周 → 重新加载该范围的数据
  datesSet(info) {
    const startStr = info.startStr.substring(0, 10)
    const endStr = info.endStr.substring(0, 10)
    fetchData(startStr, endStr)
  }
}))

// ==================== 搜索操作 ====================
function handleSearch() {
  if (dateRange.value && dateRange.value.length === 2) {
    searchParams.startDate = dateRange.value[0]
    searchParams.endDate = dateRange.value[1]
  } else {
    searchParams.startDate = ''
    searchParams.endDate = ''
  }
  fetchData()
}

function handleReset() {
  dateRange.value = []
  searchParams.startDate = ''
  searchParams.endDate = ''
  fetchData()
}

// ==================== CRUD 操作 ====================
function handleAdd() {
  dialogTitle.value = '新增日程'
  isEdit.value = false
  Object.assign(formData, defaultForm())
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑日程'
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    title: row.title || '',
    startTime: row.startTime || '',
    endTime: row.endTime || '',
    scheduleType: row.scheduleType ?? 1,
    priority: row.priority ?? 1,
    location: row.location || '',
    content: row.content || '',
    status: row.status ?? 0
  })
  dialogVisible.value = true
}

function handleDialogClose() {
  formRef.value?.resetFields()
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateSchedule(formData)
      ElMessage.success('更新日程成功')
    } else {
      await addSchedule(formData)
      ElMessage.success('新增日程成功')
    }
    dialogVisible.value = false
    // 刷新数据
    if (viewMode.value === 'calendar') {
      const api = calendarRef.value?.getApi()
      if (api) {
        const startStr = api.view.activeStart.toISOString().substring(0, 10)
        const endStr = api.view.activeEnd.toISOString().substring(0, 10)
        await fetchData(startStr, endStr)
      }
    } else {
      await fetchData()
    }
  } catch {
    // 错误由拦截器处理
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除日程「${row.title}」？`, '删除确认', {
    type: 'warning'
  }).then(async () => {
    await deleteSchedule(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-container { min-height: 100%; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; align-items: center; }
.search-form { margin-bottom: 16px; }
.calendar-container { margin-top: 4px; }
</style>

<!-- FullCalendar 全局样式（不使用 scoped，确保日历组件样式生效） -->
<style>
.calendar-container .fc {
  font-size: 14px;
}
.calendar-container .fc-toolbar-title {
  font-size: 1.2em !important;
}
.calendar-container .fc-button {
  font-size: 13px !important;
  padding: 4px 10px !important;
}
.calendar-container .fc-event {
  cursor: pointer;
  font-size: 12px;
  padding: 2px 4px;
}
.calendar-container .fc-daygrid-event {
  white-space: normal;
}
</style>
