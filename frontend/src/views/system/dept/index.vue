<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="page-header">
          <span>部门管理</span>
          <el-button type="primary" icon="Plus" @click="handleAdd(null)">新增部门</el-button>
        </div>
      </template>

      <el-table
        :data="tableData"
        row-key="id"
        border
        stripe
        v-loading="loading"
        :tree-props="{ children: 'children' }"
        default-expand-all
      >
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="deptName" label="部门名称" />
        <el-table-column label="本部门人数" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row._directCount ?? 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="总人数（含下级）" width="130" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="(row._totalCount ?? 0) > 0 ? 'primary' : 'info'">
              {{ row._totalCount ?? 0 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="60" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="success" size="small" link @click="handleAdd(row)">新增子</el-button>
            <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="上级部门">
          <el-tree-select
            v-model="formData.parentId"
            :data="deptTreeSelectData"
            :props="{ label: 'deptName', value: 'id', children: 'children' }"
            placeholder="无（顶级部门）"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="formData.deptName" placeholder="请输入部门名称" />
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
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getDeptTree, addDept, updateDept, deleteDept, getDeptUserStats } from '@/api/dept'
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
  deptName: '',
  sort: 0,
  status: 1,
  remark: ''
})

const formData = reactive(defaultForm())

const formRules = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

// el-tree-select 使用的数据源
const deptTreeSelectData = computed(() => {
  if (!tableData.value.length) return []
  return [{ id: 0, deptName: '顶级部门', children: tableData.value }]
})

// 递归合并用户统计到树节点
function mergeUserStats(tree, statsMap) {
  for (const node of tree) {
    const stat = statsMap[node.id]
    if (stat) {
      node._directCount = stat.directCount
      node._totalCount = stat.totalCount
    }
    if (node.children && node.children.length > 0) {
      mergeUserStats(node.children, statsMap)
    }
  }
}

async function fetchData() {
  loading.value = true
  try {
    const [treeRes, statsRes] = await Promise.all([
      getDeptTree(),
      getDeptUserStats()
    ])
    const tree = treeRes.data || []
    const stats = statsRes.data || []

    // 构建 deptId -> stat 映射
    const statsMap = {}
    for (const s of stats) {
      statsMap[s.deptId] = s
    }

    // 合并到树节点
    mergeUserStats(tree, statsMap)

    tableData.value = tree
  } finally {
    loading.value = false
  }
}

function handleAdd(parent) {
  dialogTitle.value = parent ? `新增「${parent.deptName}」的子部门` : '新增部门'
  isEdit.value = false
  Object.assign(formData, defaultForm())
  if (parent) {
    formData.parentId = parent.id
  }
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑部门'
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    parentId: row.parentId || 0,
    deptName: row.deptName || '',
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
    const submitData = { ...formData, parentId: formData.parentId || 0 }
    if (isEdit.value) {
      await updateDept(submitData)
      ElMessage.success('更新部门成功')
    } else {
      await addDept(submitData)
      ElMessage.success('新增部门成功')
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
  ElMessageBox.confirm(`确认删除部门「${row.deptName}」？删除后不可恢复。`, '删除确认', {
    type: 'warning'
  }).then(async () => {
    await deleteDept(row.id)
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
