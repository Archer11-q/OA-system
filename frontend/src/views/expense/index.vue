<template>
  <div class="page-container">
    <el-row :gutter="20">
      <!-- 左侧：列表 -->
      <el-col :span="18">
        <el-card>
          <template #header>
            <div class="page-header">
              <span>报销管理</span>
              <el-button type="primary" icon="Plus" @click="handleAdd">新增报销</el-button>
            </div>
          </template>

          <!-- 搜索栏 -->
          <el-form :model="searchParams" inline class="search-form">
            <el-form-item label="状态">
              <el-select v-model="searchParams.status" placeholder="全部" clearable style="width: 140px">
                <el-option label="审批中" :value="0" />
                <el-option label="已通过" :value="1" />
                <el-option label="已驳回" :value="2" />
                <el-option label="已撤回" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleSearch">查询</el-button>
              <el-button icon="Refresh" @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="tableData" border stripe v-loading="loading">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="title" label="标题" show-overflow-tooltip />
            <el-table-column label="金额" width="120">
              <template #default="{ row }">¥{{ formatAmount(row.amount) }}</template>
            </el-table-column>
            <el-table-column label="类型" width="80">
              <template #default="{ row }">
                <el-tag size="small">{{ expenseTypeLabel(row.expenseType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag size="small" :type="statusTag(row.status)">
                  {{ statusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="申请时间" width="170" />
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" link @click="handleView(row)">查看</el-button>
                <el-button v-if="row.status === 0" type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
                <el-button v-if="row.status === 0" type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 右侧：统计 -->
      <el-col :span="6">
        <el-card>
          <template #header><span>报销统计</span></template>
          <div v-loading="statsLoading">
            <el-statistic v-for="item in statsItems" :key="item.label" :value="item.value" class="stat-item">
              <template #title>
                <span :style="{ color: item.color }">{{ item.label }}</span>
              </template>
            </el-statistic>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="viewVisible" title="报销详情" width="560px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="标题">{{ viewData.title }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ expenseTypeLabel(viewData.expenseType) }}</el-descriptions-item>
        <el-descriptions-item label="金额">¥{{ formatAmount(viewData.amount) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag size="small" :type="statusTag(viewData.status)">{{ statusLabel(viewData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ viewData.description || '无' }}</el-descriptions-item>
        <el-descriptions-item label="附件" :span="2">
          <template v-if="viewAttachments.length > 0">
            <div v-for="(att, idx) in viewAttachments" :key="idx">
              <el-link type="primary" :href="att.url" target="_blank">{{ att.name }}</el-link>
            </div>
          </template>
          <span v-else>无附件</span>
        </el-descriptions-item>
        <el-descriptions-item label="申请时间" :span="2">{{ viewData.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入报销标题" />
        </el-form-item>
        <el-form-item label="类型" prop="expenseType">
          <el-select v-model="formData.expenseType" placeholder="请选择报销类型" style="width: 100%">
            <el-option label="差旅费" :value="1" />
            <el-option label="办公费" :value="2" />
            <el-option label="招待费" :value="3" />
            <el-option label="交通费" :value="4" />
            <el-option label="其他" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number v-model="formData.amount" :min="0.01" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入报销描述" />
        </el-form-item>
        <!-- 附件上传 -->
        <el-form-item label="附件">
          <div class="attachment-area">
            <el-upload
              :auto-upload="true"
              :show-file-list="false"
              :before-upload="beforeAttachmentUpload"
              :http-request="handleAttachmentUpload"
              accept=".jpg,.jpeg,.png,.gif,.pdf,.doc,.docx,.xls,.xlsx,.txt,.zip"
            >
              <el-button type="primary" plain icon="Upload" :loading="uploading">上传附件</el-button>
            </el-upload>
            <div v-if="attachments.length > 0" class="attachment-list">
              <div v-for="(att, idx) in attachments" :key="idx" class="attachment-item">
                <el-icon><Document /></el-icon>
                <a :href="att.url" target="_blank" class="attachment-name">{{ att.name }}</a>
                <el-button type="danger" size="small" link icon="Delete" @click="removeAttachment(idx)" />
              </div>
            </div>
          </div>
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
import { getExpenseList, getExpenseById, addExpense, updateExpense, deleteExpense, getExpenseStats } from '@/api/expense'
import { uploadFile } from '@/api/file'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const statsLoading = ref(false)
const tableData = ref([])
const searchParams = reactive({ status: null })

// 统计
const statsItems = ref([
  { label: '审批中', value: 0, color: '#E6A23C' },
  { label: '已通过', value: 0, color: '#67C23A' },
  { label: '已驳回', value: 0, color: '#F56C6C' },
  { label: '总金额', value: '¥0.00', color: '#409EFF' }
])

// 查看对话框
const viewVisible = ref(false)
const viewData = reactive({})

// 编辑对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

// 附件管理
const uploading = ref(false)
const attachments = ref([])

const defaultForm = () => ({
  title: '',
  expenseType: 1,
  amount: null,
  description: ''
})

const formData = reactive(defaultForm())

const formRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  expenseType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }]
}

function formatAmount(val) {
  if (val == null) return '0.00'
  return Number(val).toFixed(2)
}

function expenseTypeLabel(type) {
  const map = { 1: '差旅费', 2: '办公费', 3: '招待费', 4: '交通费', 5: '其他' }
  return map[type] || '未知'
}

function statusTag(s) {
  const map = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }
  return map[s] || 'info'
}

function statusLabel(s) {
  const map = { 0: '审批中', 1: '已通过', 2: '已驳回', 3: '已撤回' }
  return map[s] || '未知'
}

// 附件上传前校验
function beforeAttachmentUpload(file) {
  const maxSize = 10 * 1024 * 1024 // 10MB
  if (file.size > maxSize) {
    ElMessage.error('附件大小不能超过 10MB')
    return false
  }
  return true
}

// 执行附件上传
async function handleAttachmentUpload(options) {
  uploading.value = true
  try {
    const res = await uploadFile(options.file)
    attachments.value.push({
      name: res.data.originalName,
      url: res.data.url
    })
    ElMessage.success('附件上传成功')
  } catch (e) {
    ElMessage.error(e.message || '附件上传失败')
  } finally {
    uploading.value = false
  }
}

// 移除附件
function removeAttachment(idx) {
  attachments.value.splice(idx, 1)
}

// 解析已有附件（编辑时）
function parseAttachments(attachmentsJson) {
  try {
    if (attachmentsJson) {
      return JSON.parse(attachmentsJson)
    }
  } catch { /* ignore parse error */ }
  return []
}

async function fetchData() {
  loading.value = true
  try {
    const status = searchParams.status !== null && searchParams.status !== '' ? searchParams.status : undefined
    const res = await getExpenseList(status)
    tableData.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function fetchStats() {
  statsLoading.value = true
  try {
    const res = await getExpenseStats()
    const data = res.data
    if (data) {
      statsItems.value[0].value = data.pendingCount ?? 0
      statsItems.value[1].value = data.approvedCount ?? 0
      statsItems.value[2].value = data.rejectedCount ?? 0
      statsItems.value[3].value = '¥' + formatAmount(data.totalAmount ?? 0)
    }
  } finally {
    statsLoading.value = false
  }
}

function handleSearch() {
  fetchData()
}

function handleReset() {
  searchParams.status = null
  fetchData()
}

function handleView(row) {
  Object.assign(viewData, row)
  viewAttachments.value = parseAttachments(row.attachments)
  viewVisible.value = true
}

const viewAttachments = ref([])

function handleAdd() {
  dialogTitle.value = '新增报销'
  isEdit.value = false
  Object.assign(formData, defaultForm())
  attachments.value = []
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑报销'
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    title: row.title || '',
    expenseType: row.expenseType ?? 1,
    amount: row.amount,
    description: row.description || ''
  })
  attachments.value = parseAttachments(row.attachments)
  dialogVisible.value = true
}

function handleDialogClose() {
  formRef.value?.resetFields()
  attachments.value = []
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const payload = {
      ...formData,
      attachments: attachments.value.length > 0 ? JSON.stringify(attachments.value) : null
    }
    if (isEdit.value) {
      await updateExpense(payload)
      ElMessage.success('修改报销成功')
    } else {
      await addExpense(payload)
      ElMessage.success('提交报销成功')
    }
    dialogVisible.value = false
    fetchData()
    fetchStats()
  } catch {
    // 错误由拦截器处理
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除报销「${row.title}」？`, '删除确认', {
    type: 'warning'
  }).then(async () => {
    await deleteExpense(row.id)
    ElMessage.success('删除成功')
    fetchData()
    fetchStats()
  }).catch(() => {})
}

onMounted(() => {
  fetchData()
  fetchStats()
})
</script>

<style scoped>
.page-container { min-height: 100%; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
.stat-item { margin-bottom: 20px; }
.attachment-area { width: 100%; }
.attachment-list { margin-top: 8px; }
.attachment-item { display: flex; align-items: center; gap: 6px; padding: 4px 0; }
.attachment-name { font-size: 13px; color: #409EFF; text-decoration: none; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>
