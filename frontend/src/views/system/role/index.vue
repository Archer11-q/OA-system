<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="page-header">
          <span>角色管理</span>
          <el-button type="primary" icon="Plus" @click="handleAdd">新增角色</el-button>
        </div>
      </template>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="roleName" label="角色名称" />
        <el-table-column prop="roleCode" label="角色编码" />
        <el-table-column label="数据范围" width="120">
          <template #default="{ row }">
            <el-tag size="small" :type="dataScopeType(row.dataScope)">
              {{ dataScopeLabel(row.dataScope) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" size="small" link @click="handleAssignMenu(row)">分配菜单</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="formData.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="formData.roleCode" placeholder="请输入角色编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="数据范围">
          <el-select v-model="formData.dataScope" placeholder="请选择数据范围" style="width: 100%">
            <el-option label="全部数据" :value="1" />
            <el-option label="本部门及子部门" :value="2" />
            <el-option label="本部门" :value="3" />
            <el-option label="仅本人" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配菜单对话框 -->
    <el-dialog v-model="menuDialogVisible" title="分配菜单权限" width="400px">
      <el-tree
        ref="menuTreeRef"
        :data="menuTreeData"
        :props="{ label: 'menuName', children: 'children' }"
        node-key="id"
        show-checkbox
        default-expand-all
      />
      <template #footer>
        <el-button @click="menuDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="menuSubmitLoading" @click="handleMenuSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getRoleList, getRoleById, addRole, updateRole, deleteRole, assignRoleMenus } from '@/api/role'
import { getMenuTree } from '@/api/menu'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const defaultForm = () => ({
  roleName: '',
  roleCode: '',
  dataScope: 1,
  sort: 0,
  status: 1,
  remark: ''
})

const formData = reactive(defaultForm())

const formRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

// 分配菜单
const menuDialogVisible = ref(false)
const menuSubmitLoading = ref(false)
const menuTreeRef = ref(null)
const menuTreeData = ref([])
const currentRoleId = ref(null)

function dataScopeType(scope) {
  const map = { 1: '', 2: 'success', 3: 'warning', 4: 'info' }
  return map[scope] || ''
}

function dataScopeLabel(scope) {
  const map = { 1: '全部数据', 2: '本部门及子部门', 3: '本部门', 4: '仅本人' }
  return map[scope] || '未知'
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getRoleList()
    tableData.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function loadMenuTree() {
  try {
    const res = await getMenuTree(true)
    menuTreeData.value = res.data || []
  } catch { /* 忽略 */ }
}

function handleAdd() {
  dialogTitle.value = '新增角色'
  isEdit.value = false
  Object.assign(formData, defaultForm())
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑角色'
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    roleName: row.roleName || '',
    roleCode: row.roleCode || '',
    dataScope: row.dataScope ?? 1,
    sort: row.sort ?? 0,
    status: row.status ?? 1,
    remark: row.remark || ''
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
      await updateRole(formData)
      ElMessage.success('更新角色成功')
    } else {
      await addRole(formData)
      ElMessage.success('新增角色成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch {
    // 错误由拦截器处理
  } finally {
    submitLoading.value = false
  }
}

async function handleAssignMenu(row) {
  currentRoleId.value = row.id
  menuDialogVisible.value = true
  try {
    const roleRes = await getRoleById(row.id)
    // 查询角色已拥有的菜单ID并选中
    // 后端 /role/{id} 返回的 RoleVO 包含 menuIds 字段（如果后端有返回的话）
    // 这里通过 getMenuTree 获取全部菜单，角色拥有的菜单在 menuIds 里
    // 简化处理：后续可优化
  } catch { /* 忽略 */ }
  // 延迟设置选中状态，等待树渲染完成
  setTimeout(async () => {
    try {
      const roleRes = await getRoleById(currentRoleId.value)
      const menuIds = roleRes.data?.menuIds || []
      if (menuTreeRef.value && menuIds.length > 0) {
        menuTreeRef.value.setCheckedKeys(menuIds)
      }
    } catch { /* 忽略 */ }
  }, 200)
}

async function handleMenuSubmit() {
  const checkedKeys = menuTreeRef.value?.getCheckedKeys() || []
  const halfCheckedKeys = menuTreeRef.value?.getHalfCheckedKeys() || []
  const allKeys = [...checkedKeys, ...halfCheckedKeys]
  menuSubmitLoading.value = true
  try {
    await assignRoleMenus(currentRoleId.value, allKeys)
    ElMessage.success('菜单权限分配成功')
    menuDialogVisible.value = false
  } catch {
    // 错误由拦截器处理
  } finally {
    menuSubmitLoading.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除角色「${row.roleName}」？删除后不可恢复。`, '删除确认', {
    type: 'warning'
  }).then(async () => {
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

onMounted(() => {
  fetchData()
  loadMenuTree()
})
</script>

<style scoped>
.page-container { min-height: 100%; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
</style>
