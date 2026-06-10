<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="page-header">
          <span>日程管理</span>
          <el-button type="primary" icon="Plus" @click="handleAdd">新增日程</el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :model="searchParams" inline class="search-form">
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

      <!-- 表格 -->
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
import { ref, reactive, onMounted } from 'vue'
import { getScheduleList, addSchedule, updateSchedule, deleteSchedule } from '@/api/schedule'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])

// 日期范围
const dateRange = ref([])
const searchParams = reactive({ startDate: '', endDate: '' })

// 对话框
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

async function fetchData() {
  loading.value = true
  try {
    const params = {}
    if (searchParams.startDate) params.startDate = searchParams.startDate
    if (searchParams.endDate) params.endDate = searchParams.endDate
    const res = await getScheduleList(params)
    tableData.value = res.data || []
  } finally {
    loading.value = false
  }
}

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
    fetchData()
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

onMounted(fetchData)
</script>

<style scoped>
.page-container { min-height: 100%; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
</style>
