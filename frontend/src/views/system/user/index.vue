<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="page-header">
          <span>用户管理</span>
          <el-button type="primary" icon="Plus" @click="handleAdd">新增用户</el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :model="searchParams" inline class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchParams.username" placeholder="输入用户名搜索" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchParams.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleSearch">查询</el-button>
          <el-button icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="真实姓名" />
        <el-table-column prop="employeeNo" label="工号" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" size="small" link @click="handleResetPwd(row)">重置密码</el-button>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="formData.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="formData.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="工号">
          <el-input v-model="formData.employeeNo" placeholder="请输入工号" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="formData.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="部门">
          <el-tree-select
            v-model="formData.deptId"
            :data="deptOptions"
            :props="{ label: 'deptName', value: 'id', children: 'children' }"
            placeholder="请选择部门"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" />
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
import { getUserPage, addUser, updateUser, deleteUser, resetPassword } from '@/api/user'
import { getDeptTree } from '@/api/dept'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const page = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const searchParams = reactive({ username: '', status: null })

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)
const deptOptions = ref([])

const defaultForm = () => ({
  username: '',
  password: '',
  realName: '',
  employeeNo: '',
  phone: '',
  email: '',
  deptId: null,
  status: 1
})

const formData = reactive(defaultForm())

const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }]
}

async function fetchData() {
  loading.value = true
  try {
    const params = { pageNum: page.pageNum, pageSize: page.pageSize }
    if (searchParams.username) params.username = searchParams.username
    if (searchParams.status !== null && searchParams.status !== '') params.status = searchParams.status
    const res = await getUserPage(params)
    if (res.data) {
      tableData.value = res.data.records || []
      page.total = res.data.total || 0
    }
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.pageNum = 1
  fetchData()
}

function handleReset() {
  searchParams.username = ''
  searchParams.status = null
  handleSearch()
}

async function loadDeptTree() {
  try {
    const res = await getDeptTree()
    deptOptions.value = res.data || []
  } catch { /* 忽略 */ }
}

function handleAdd() {
  dialogTitle.value = '新增用户'
  isEdit.value = false
  Object.assign(formData, defaultForm())
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑用户'
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    username: row.username,
    password: '',
    realName: row.realName || '',
    employeeNo: row.employeeNo || '',
    phone: row.phone || '',
    email: row.email || '',
    deptId: row.deptId,
    status: row.status
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
      await updateUser(formData)
      ElMessage.success('更新用户成功')
    } else {
      await addUser(formData)
      ElMessage.success('新增用户成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch {
    // 错误由拦截器处理
  } finally {
    submitLoading.value = false
  }
}

function handleResetPwd(row) {
  ElMessageBox.confirm(`确认将用户「${row.username}」的密码重置为 123456？`, '重置密码', {
    type: 'warning'
  }).then(async () => {
    await resetPassword(row.id)
    ElMessage.success('密码已重置为 123456')
  }).catch(() => {})
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除用户「${row.username}」？删除后不可恢复。`, '删除确认', {
    type: 'warning'
  }).then(async () => {
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

onMounted(() => {
  loadDeptTree()
  fetchData()
})
</script>

<style scoped>
.page-container { min-height: 100%; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
</style>
