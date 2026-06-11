<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="page-header">
          <span>审批中心</span>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 待我审批 -->
        <el-tab-pane label="待我审批" name="todo">
          <el-table :data="todoList" border stripe v-loading="todoLoading">
            <el-table-column prop="id" label="编号" width="70" />
            <el-table-column prop="title" label="标题" show-overflow-tooltip />
            <el-table-column label="业务类型" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="bizTypeTag(row.businessType)">{{ bizTypeLabel(row.businessType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="currentLevel" label="当前级别" width="80">
              <template #default="{ row }">第{{ row.currentLevel }}级/共{{ row.totalLevels }}级</template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag size="small" :type="instanceStatusTag(row.status)">{{ instanceStatusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="申请时间" width="170" />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button type="success" size="small" @click="handleApprove(row, 1)">同意</el-button>
                <el-button type="danger" size="small" @click="handleApprove(row, 2)">驳回</el-button>
                <el-button type="primary" size="small" link @click="handleViewRecords(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 我的申请 -->
        <el-tab-pane label="我的申请" name="my">
          <el-table :data="myList" border stripe v-loading="myLoading">
            <el-table-column prop="id" label="编号" width="70" />
            <el-table-column prop="title" label="标题" show-overflow-tooltip />
            <el-table-column label="业务类型" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="bizTypeTag(row.businessType)">{{ bizTypeLabel(row.businessType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag size="small" :type="instanceStatusTag(row.status)">{{ instanceStatusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="currentLevel" label="当前级别" width="80">
              <template #default="{ row }">第{{ row.currentLevel }}级/共{{ row.totalLevels }}级</template>
            </el-table-column>
            <el-table-column prop="createTime" label="申请时间" width="170" />
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status === 0" type="warning" size="small" @click="handleCancel(row)">撤回</el-button>
                <el-button type="primary" size="small" link @click="handleViewRecords(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 已审批 -->
        <el-tab-pane label="已审批" name="done">
          <el-table :data="doneList" border stripe v-loading="doneLoading">
            <el-table-column prop="id" label="编号" width="70" />
            <el-table-column prop="title" label="标题" show-overflow-tooltip />
            <el-table-column label="业务类型" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="bizTypeTag(row.businessType)">{{ bizTypeLabel(row.businessType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag size="small" :type="instanceStatusTag(row.status)">{{ instanceStatusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="申请时间" width="170" />
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" link @click="handleViewRecords(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 审批模板 -->
        <el-tab-pane label="审批模板" name="template" v-if="isAdmin">
          <div style="margin-bottom:12px">
            <el-button type="primary" icon="Plus" @click="handleTemplateAdd">新增模板</el-button>
          </div>
          <el-table :data="templateList" border stripe v-loading="templateLoading">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="templateName" label="模板名称" />
            <el-table-column prop="templateCode" label="模板编码" />
            <el-table-column prop="approvalLevels" label="审批级别数" width="100" />
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" link @click="handleTemplateEdit(row)">编辑</el-button>
                <el-button type="danger" size="small" link @click="handleTemplateDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 审批操作对话框 -->
    <el-dialog v-model="approveVisible" title="审批操作" width="420px">
      <el-form ref="approveFormRef" :model="approveForm" label-width="80px">
        <el-form-item label="审批结果">
          <el-radio-group v-model="approveForm.result">
            <el-radio :value="1">同意</el-radio>
            <el-radio :value="2">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input v-model="approveForm.comment" type="textarea" :rows="3" placeholder="请输入审批意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveVisible = false">取消</el-button>
        <el-button type="primary" :loading="approveLoading" @click="handleApproveSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看审批记录对话框 -->
    <el-dialog v-model="recordsVisible" title="审批记录" width="560px">
      <el-descriptions :column="2" border style="margin-bottom:16px">
        <el-descriptions-item label="标题">{{ recordsInstance.title }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag size="small" :type="instanceStatusTag(recordsInstance.status)">{{ instanceStatusLabel(recordsInstance.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="级别">第{{ recordsInstance.currentLevel }}级/共{{ recordsInstance.totalLevels }}级</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ recordsInstance.createTime }}</el-descriptions-item>
      </el-descriptions>
      <el-timeline>
        <el-timeline-item
          v-for="record in approvalRecords"
          :key="record.id"
          :timestamp="record.approvalTime || '待审批'"
          placement="top"
          :type="recordResultType(record.result)"
        >
          <p>第{{ record.level }}级审批 — {{ recordResultLabel(record.result) }}</p>
          <p v-if="record.comment" style="color:#909399;font-size:13px">{{ record.comment }}</p>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>

    <!-- 审批模板对话框 -->
    <el-dialog v-model="templateDialogVisible" :title="templateDialogTitle" width="700px" @close="handleTemplateDialogClose">
      <el-form ref="templateFormRef" :model="templateForm" :rules="templateRules" label-width="90px">
        <el-form-item label="模板名称" prop="templateName">
          <el-input v-model="templateForm.templateName" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="模板编码" prop="templateCode">
          <el-input v-model="templateForm.templateCode" placeholder="请输入模板编码" :disabled="templateIsEdit" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="templateForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="templateForm.description" type="textarea" :rows="2" placeholder="请输入模板描述" />
        </el-form-item>
        <!-- 审批人配置（支持并行审批：同一级别可配置多个审批人） -->
        <el-form-item label="审批人配置" prop="approversConfigList">
          <div v-for="(item, index) in templateForm.approversConfigList" :key="index"
               style="margin-bottom:8px; display:flex; gap:8px; align-items:center; flex-wrap:wrap">
            <span style="white-space:nowrap;font-size:13px">第</span>
            <el-input-number v-model="item.level" :min="1" :max="5" size="small" style="width:70px" />
            <span style="white-space:nowrap;font-size:13px">级</span>
            <el-select v-model="item.type" size="small" style="width:140px" @change="onApproverTypeChange(item)">
              <el-option label="部门负责人" value="DEPT_LEADER" />
              <el-option label="角色" value="ROLE" />
              <el-option label="指定用户" value="USER" />
            </el-select>
            <el-input v-if="item.type === 'ROLE'" v-model="item.value" size="small"
                      placeholder="角色编码，如 ROLE_ADMIN" style="width:180px" />
            <el-input v-if="item.type === 'USER'" v-model="item.value" size="small"
                      placeholder="用户ID" style="width:120px" />
            <el-button type="danger" size="small" :icon="'Delete'" circle
                       @click="removeApproverConfig(index)"
                       :disabled="templateForm.approversConfigList.length <= 1" />
          </div>
          <el-button type="primary" size="small" plain icon="Plus" @click="addApproverConfig">
            添加审批人配置
          </el-button>
          <div style="margin-top:4px;font-size:12px;color:#909399">
            提示：同一级别配置多个审批人即启用并行审批（任一同意即可推进，任一驳回即终止）
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="templateDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="templateSubmitLoading" @click="handleTemplateSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import {
  getApprovalTodo, getApprovalDone, getApprovalMy, approveInstance, cancelInstance, getApprovalRecords,
  getTemplateList, getTemplateById, addTemplate, updateTemplate, deleteTemplate
} from '@/api/approval'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const isAdmin = computed(() => {
  const roles = userStore.roles || []
  return roles.some(r => r === 'admin' || r === 'ROLE_ADMIN')
})

const activeTab = ref('todo')

// 待审批
const todoList = ref([])
const todoLoading = ref(false)
// 我的申请
const myList = ref([])
const myLoading = ref(false)
// 已审批
const doneList = ref([])
const doneLoading = ref(false)
// 审批模板
const templateList = ref([])
const templateLoading = ref(false)

function bizTypeTag(type) {
  const map = { LEAVE: 'warning', EXPENSE: 'success' }
  return map[type] || 'info'
}

function bizTypeLabel(type) {
  const map = { LEAVE: '请假', EXPENSE: '报销' }
  return map[type] || type || '通用'
}

function instanceStatusTag(s) {
  const map = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }
  return map[s] || 'info'
}

function instanceStatusLabel(s) {
  const map = { 0: '审批中', 1: '已通过', 2: '已驳回', 3: '已撤回' }
  return map[s] || '未知'
}

function recordResultType(r) {
  const map = { 0: 'info', 1: 'success', 2: 'danger', 4: 'info' }
  return map[r] || 'info'
}

function recordResultLabel(r) {
  const map = { 0: '待审批', 1: '同意', 2: '驳回', 4: '自动作废' }
  return map[r] || '未知'
}

// 加载各 Tab 数据
async function loadTodo() {
  todoLoading.value = true
  try {
    const res = await getApprovalTodo()
    todoList.value = res.data || []
  } finally {
    todoLoading.value = false
  }
}

async function loadMy() {
  myLoading.value = true
  try {
    const res = await getApprovalMy()
    myList.value = res.data || []
  } finally {
    myLoading.value = false
  }
}

async function loadDone() {
  doneLoading.value = true
  try {
    const res = await getApprovalDone()
    doneList.value = res.data || []
  } finally {
    doneLoading.value = false
  }
}

async function loadTemplates() {
  templateLoading.value = true
  try {
    const res = await getTemplateList()
    templateList.value = res.data || []
  } finally {
    templateLoading.value = false
  }
}

function handleTabChange(tab) {
  switch (tab) {
    case 'todo': loadTodo(); break
    case 'my': loadMy(); break
    case 'done': loadDone(); break
    case 'template': loadTemplates(); break
  }
}

// 审批操作
const approveVisible = ref(false)
const approveLoading = ref(false)
const approveForm = reactive({ result: 1, comment: '' })
const approveFormRef = ref(null)
const currentApproveId = ref(null)

function handleApprove(row, result) {
  currentApproveId.value = row.id
  approveForm.result = result
  approveForm.comment = result === 1 ? '同意' : ''
  approveVisible.value = true
}

async function handleApproveSubmit() {
  approveLoading.value = true
  try {
    await approveInstance(currentApproveId.value, { result: approveForm.result, comment: approveForm.comment })
    ElMessage.success(approveForm.result === 1 ? '已同意' : '已驳回')
    approveVisible.value = false
    loadTodo()
  } catch {
    // 错误由拦截器处理
  } finally {
    approveLoading.value = false
  }
}

// 撤回
function handleCancel(row) {
  ElMessageBox.confirm(`确认撤回审批「${row.title}」？`, '撤回确认', {
    type: 'warning'
  }).then(async () => {
    await cancelInstance(row.id)
    ElMessage.success('已撤回')
    loadMy()
  }).catch(() => {})
}

// 查看审批记录
const recordsVisible = ref(false)
const recordsInstance = reactive({})
const approvalRecords = ref([])

async function handleViewRecords(row) {
  Object.assign(recordsInstance, row)
  recordsVisible.value = true
  try {
    const res = await getApprovalRecords(row.id)
    approvalRecords.value = res.data || []
  } catch { /* 忽略 */ }
}

// 模板管理
const templateDialogVisible = ref(false)
const templateDialogTitle = ref('')
const templateIsEdit = ref(false)
const templateSubmitLoading = ref(false)
const templateFormRef = ref(null)

const defaultTemplateForm = () => ({
  templateName: '',
  templateCode: '',
  approvalLevels: 2,
  status: 1,
  description: '',
  approversConfigList: [{ level: 1, type: 'DEPT_LEADER', value: '' }]
})

const templateForm = reactive(defaultTemplateForm())

const templateRules = {
  templateName: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  templateCode: [{ required: true, message: '请输入模板编码', trigger: 'blur' }],
  approversConfigList: [{
    validator: (_rule, _value, callback) => {
      const list = templateForm.approversConfigList
      if (!list || list.length === 0) {
        callback(new Error('请至少配置一个审批人'))
        return
      }
      for (let i = 0; i < list.length; i++) {
        const item = list[i]
        if (!item.level || item.level < 1) {
          callback(new Error(`第${i + 1}个审批人：级别必须大于等于1`))
          return
        }
        if (item.type === 'ROLE' && (!item.value || !item.value.trim())) {
          callback(new Error(`第${i + 1}个审批人：角色类型必须填写角色编码`))
          return
        }
        if (item.type === 'USER' && (!item.value || isNaN(Number(item.value)))) {
          callback(new Error(`第${i + 1}个审批人：用户类型必须填写有效的用户ID`))
          return
        }
      }
      callback()
    },
    trigger: 'change'
  }]
}

// 审批人配置操作
function addApproverConfig() {
  templateForm.approversConfigList.push({ level: 1, type: 'DEPT_LEADER', value: '' })
}

function removeApproverConfig(index) {
  templateForm.approversConfigList.splice(index, 1)
}

function onApproverTypeChange(item) {
  // 切换为部门负责人时清空value
  if (item.type === 'DEPT_LEADER') {
    item.value = ''
  }
}

function handleTemplateAdd() {
  templateDialogTitle.value = '新增模板'
  templateIsEdit.value = false
  Object.assign(templateForm, defaultTemplateForm())
  templateDialogVisible.value = true
}

function handleTemplateEdit(row) {
  templateDialogTitle.value = '编辑模板'
  templateIsEdit.value = true
  // 从 approversConfig JSON 反序列化审批人配置列表
  let configList = [{ level: 1, type: 'DEPT_LEADER', value: '' }]
  try {
    if (row.approversConfig) {
      const parsed = JSON.parse(row.approversConfig)
      if (Array.isArray(parsed) && parsed.length > 0) {
        configList = parsed.map(item => ({
          level: item.level || 1,
          type: item.type || 'DEPT_LEADER',
          value: item.value || ''
        }))
      }
    }
  } catch { /* 解析失败使用默认配置 */ }

  Object.assign(templateForm, {
    id: row.id,
    templateName: row.templateName || '',
    templateCode: row.templateCode || '',
    approvalLevels: row.approvalLevels ?? 2,
    status: row.status ?? 1,
    description: row.description || '',
    approversConfigList: configList
  })
  templateDialogVisible.value = true
}

function handleTemplateDialogClose() {
  templateFormRef.value?.resetFields()
}

async function handleTemplateSubmit() {
  const valid = await templateFormRef.value.validate().catch(() => false)
  if (!valid) return
  templateSubmitLoading.value = true
  try {
    // 将审批人配置列表序列化为 JSON 字符串
    const payload = {
      ...templateForm,
      approversConfig: JSON.stringify(templateForm.approversConfigList)
    }
    delete payload.approversConfigList

    if (templateIsEdit.value) {
      await updateTemplate(payload)
      ElMessage.success('更新模板成功')
    } else {
      await addTemplate(payload)
      ElMessage.success('新增模板成功')
    }
    templateDialogVisible.value = false
    loadTemplates()
  } catch {
    // 错误由拦截器处理
  } finally {
    templateSubmitLoading.value = false
  }
}

function handleTemplateDelete(row) {
  ElMessageBox.confirm(`确认删除模板「${row.templateName}」？`, '删除确认', {
    type: 'warning'
  }).then(async () => {
    await deleteTemplate(row.id)
    ElMessage.success('删除成功')
    loadTemplates()
  }).catch(() => {})
}

onMounted(() => {
  loadTodo()
  loadTemplates()
})
</script>

<style scoped>
.page-container { min-height: 100%; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
</style>
