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

    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <template #header>欢迎使用 OA 办公自动化系统</template>
          <p>请通过左侧菜单访问各功能模块。系统包含以下核心功能：</p>
          <el-divider />
          <el-row :gutter="16">
            <el-col :span="6" v-for="m in modules" :key="m.name">
              <el-card shadow="hover" class="module-card">
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
import { ref, onMounted } from 'vue'
import { getOverview } from '@/api/dashboard'

const overviewCards = ref([
  { label: '用户总数', value: '--', icon: 'User', color: '#409EFF' },
  { label: '部门总数', value: '--', icon: 'OfficeBuilding', color: '#67C23A' },
  { label: '角色总数', value: '--', icon: 'Avatar', color: '#E6A23C' },
  { label: '公告总数', value: '--', icon: 'Bell', color: '#F56C6C' }
])

const modules = [
  { name: '系统管理', icon: 'Setting', desc: '用户/角色/菜单/部门管理' },
  { name: '考勤管理', icon: 'Timer', desc: '签到签退/请假申请/月度统计' },
  { name: '审批中心', icon: 'DocumentChecked', desc: '多级审批流程/撤回' },
  { name: '公告通知', icon: 'Bell', desc: '发布/编辑/删除公告' },
  { name: '日程管理', icon: 'Calendar', desc: '个人日程/日期范围查询' },
  { name: '报销管理', icon: 'Money', desc: '报销申请/多级审批/统计' }
]

onMounted(async () => {
  try {
    const res = await getOverview()
    if (res.data) {
      overviewCards.value[0].value = res.data.userCount ?? '--'
      overviewCards.value[1].value = res.data.deptCount ?? '--'
      overviewCards.value[2].value = res.data.roleCount ?? '--'
      overviewCards.value[3].value = res.data.noticeCount ?? '--'
    }
  } catch {
    // 使用默认值
  }
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
}
.module-desc {
  color: #909399;
  font-size: 12px;
  margin-top: 8px;
}
</style>
