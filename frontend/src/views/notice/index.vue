<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="page-header">
          <span>公告通知</span>
          <el-button v-if="isAdmin" type="primary" icon="Plus" @click="handleAdd">发布公告</el-button>
        </div>
      </template>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">{{ row.title }}</el-button>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="noticeTypeTag(row.noticeType)">
              {{ noticeTypeLabel(row.noticeType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="置顶" width="70">
          <template #default="{ row }">
            <el-tag v-if="row.isTop === 1" type="danger" size="small">置顶</el-tag>
            <span v-else style="color:#c0c4cc">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right" v-if="isAdmin">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page.pageNum"
        v-model:page-size="page.pageSize"
        :total="page.total"
        layout="total, prev, pager, next"
        @change="fetchData"
        style="margin-top: 16px; justify-content: flex-end;" />
    </el-card>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="viewVisible" title="公告详情" width="640px">
      <h2 style="margin-top:0">{{ viewData.title }}</h2>
      <div style="color:#909399;font-size:13px;margin-bottom:16px">
        <span>类型：{{ noticeTypeLabel(viewData.noticeType) }}</span>
        <span style="margin-left:16px">发布时间：{{ viewData.publishTime }}</span>
        <span v-if="viewData.isTop === 1" style="margin-left:16px"><el-tag type="danger" size="small">置顶</el-tag></span>
      </div>
      <el-divider />
      <div class="notice-content" v-html="viewData.content || '暂无内容'"></div>
    </el-dialog>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="类型" prop="noticeType">
          <el-select v-model="formData.noticeType" placeholder="请选择类型" style="width: 100%">
            <el-option label="通知" :value="1" />
            <el-option label="公告" :value="2" />
            <el-option label="制度" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="formData.content" type="textarea" :rows="6" placeholder="请输入公告内容（支持HTML）" />
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="formData.isTop" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">立即发布</el-radio>
            <el-radio :value="0">存为草稿</el-radio>
          </el-radio-group>
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
import { getNoticePage, getNoticeById, addNotice, updateNotice, deleteNotice } from '@/api/notice'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const isAdmin = computed(() => {
  const roles = userStore.roles || []
  return roles.some(r => r === 'admin' || r === 'ROLE_ADMIN')
})

const loading = ref(false)
const tableData = ref([])
const page = reactive({ pageNum: 1, pageSize: 10, total: 0 })

// 查看对话框
const viewVisible = ref(false)
const viewData = reactive({})

// 编辑对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const defaultForm = () => ({
  title: '',
  noticeType: 1,
  content: '',
  isTop: 0,
  status: 1
})

const formData = reactive(defaultForm())

const formRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  noticeType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

function noticeTypeTag(type) {
  const map = { 1: '', 2: 'success', 3: 'warning' }
  return map[type] || ''
}

function noticeTypeLabel(type) {
  const map = { 1: '通知', 2: '公告', 3: '制度' }
  return map[type] || '未知'
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getNoticePage({ pageNum: page.pageNum, pageSize: page.pageSize })
    // 后端 /notice/page 可能返回 List 或 PageResult
    if (Array.isArray(res.data)) {
      tableData.value = res.data
      page.total = res.data.length
    } else if (res.data) {
      tableData.value = res.data.records || []
      page.total = res.data.total || 0
    }
  } finally {
    loading.value = false
  }
}

function handleView(row) {
  Object.assign(viewData, row)
  viewVisible.value = true
}

function handleAdd() {
  dialogTitle.value = '发布公告'
  isEdit.value = false
  Object.assign(formData, defaultForm())
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑公告'
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    title: row.title || '',
    noticeType: row.noticeType ?? 1,
    content: row.content || '',
    isTop: row.isTop ?? 0,
    status: row.status ?? 1
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
      await updateNotice(formData)
      ElMessage.success('编辑公告成功')
    } else {
      await addNotice(formData)
      ElMessage.success('发布公告成功')
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
  ElMessageBox.confirm(`确认删除公告「${row.title}」？`, '删除确认', {
    type: 'warning'
  }).then(async () => {
    await deleteNotice(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { min-height: 100%; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
.notice-content { min-height: 100px; line-height: 1.8; }
</style>
