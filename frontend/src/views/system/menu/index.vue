<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="page-header">
          <span>菜单管理</span>
          <el-button type="primary" icon="Plus" @click="handleAdd(null)">新增根菜单</el-button>
        </div>
      </template>

      <el-table
        :data="tableData"
        row-key="id"
        border
        stripe
        v-loading="loading"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        default-expand-all
      >
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="menuName" label="菜单名称" />
        <el-table-column prop="icon" label="图标" width="80" />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="menuTypeTag(row.menuType)">
              {{ menuTypeLabel(row.menuType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="perms" label="权限标识" show-overflow-tooltip />
        <el-table-column prop="path" label="路由路径" show-overflow-tooltip />
        <el-table-column prop="component" label="组件路径" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="60" />
        <el-table-column label="可见" width="60">
          <template #default="{ row }">
            <el-tag :type="row.visible === 1 ? 'success' : 'info'" size="small">
              {{ row.visible === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="success" size="small" link @click="handleAdd(row)">新增子</el-button>
            <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="formData.parentId"
            :data="menuTreeSelectData"
            :props="{ label: 'menuName', value: 'id', children: 'children' }"
            placeholder="无（根菜单）"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType">
          <el-select v-model="formData.menuType" placeholder="请选择菜单类型" style="width: 100%">
            <el-option label="目录" :value="0" />
            <el-option label="菜单" :value="1" />
            <el-option label="按钮" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="formData.menuName" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="图标" v-if="formData.menuType !== 2">
          <el-input v-model="formData.icon" placeholder="Element Plus 图标名，如 Setting" />
        </el-form-item>
        <el-form-item label="权限标识" prop="perms" v-if="formData.menuType === 2">
          <el-input v-model="formData.perms" placeholder="如 system:user:add" />
        </el-form-item>
        <el-form-item label="路由路径" v-if="formData.menuType !== 2">
          <el-input v-model="formData.path" placeholder="如 /system/user" />
        </el-form-item>
        <el-form-item label="组件路径" v-if="formData.menuType !== 2">
          <el-input v-model="formData.component" placeholder="如 system/user/index" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="是否可见">
          <el-switch v-model="formData.visible" :active-value="1" :inactive-value="0" />
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
import { getMenuTree, addMenu, updateMenu, deleteMenu } from '@/api/menu'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const defaultForm = () => ({
  parentId: null,
  menuType: 1,
  menuName: '',
  icon: '',
  perms: '',
  path: '',
  component: '',
  sort: 0,
  visible: 1
})

const formData = reactive(defaultForm())

const formRules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }]
}

// el-tree-select 使用的数据源（需要添加一个虚拟根节点）
const menuTreeSelectData = computed(() => {
  if (!tableData.value.length) return []
  return [{ id: 0, menuName: '根目录', children: tableData.value }]
})

function menuTypeTag(type) {
  const map = { 0: '', 1: 'success', 2: 'warning' }
  return map[type] || ''
}

function menuTypeLabel(type) {
  const map = { 0: '目录', 1: '菜单', 2: '按钮' }
  return map[type] || '未知'
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getMenuTree(true)
    tableData.value = res.data || []
  } finally {
    loading.value = false
  }
}

function handleAdd(parent) {
  dialogTitle.value = parent ? `新增「${parent.menuName}」的子菜单` : '新增根菜单'
  isEdit.value = false
  Object.assign(formData, defaultForm())
  if (parent) {
    formData.parentId = parent.id
    formData.menuType = 1
  }
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑菜单'
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    parentId: row.parentId || 0,
    menuType: row.menuType,
    menuName: row.menuName || '',
    icon: row.icon || '',
    perms: row.perms || '',
    path: row.path || '',
    component: row.component || '',
    sort: row.sort ?? 0,
    visible: row.visible ?? 1
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
    // parentId 为 0 时视为 null（根菜单）
    const submitData = { ...formData, parentId: formData.parentId || 0 }
    if (isEdit.value) {
      await updateMenu(submitData)
      ElMessage.success('更新菜单成功')
    } else {
      await addMenu(submitData)
      ElMessage.success('新增菜单成功')
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
  ElMessageBox.confirm(`确认删除菜单「${row.menuName}」？如有子菜单将一并处理。`, '删除确认', {
    type: 'warning'
  }).then(async () => {
    await deleteMenu(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { min-height: 100%; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
</style>
