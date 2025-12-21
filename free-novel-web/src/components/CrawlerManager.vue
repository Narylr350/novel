<template>
  <div class="crawler-manager">
    <h2>🕷️ 爬虫管理</h2>
    
    <!-- 状态概览 -->
    <el-card class="status-card">
      <template #header>
        <div class="card-header">
          <span>📊 系统状态</span>
          <el-button type="primary" size="small" @click="refreshStatus" :loading="loading">
            刷新
          </el-button>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="6">
          <el-statistic title="小说总数" :value="status.totalNovels" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="待下载章节" :value="status.pendingChapters" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="翻译中章节" :value="status.translatingChapters" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="已完成章节" :value="status.completedChapters" />
        </el-col>
      </el-row>
      
      <el-divider />
      
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="switch-item">
            <span>下载功能：</span>
            <el-tag :type="status.dbDownloadEnabled ? 'success' : 'danger'">
              {{ status.dbDownloadEnabled ? '已启用' : '已关闭' }}
            </el-tag>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="switch-item">
            <span>AI翻译：</span>
            <el-tag :type="status.dbTranslationEnabled ? 'success' : 'danger'">
              {{ status.dbTranslationEnabled ? '已启用' : '已关闭' }}
            </el-tag>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="switch-item">
            <span>定时任务：</span>
            <el-tag :type="status.schedulerEnabled ? 'success' : 'info'">
              {{ status.schedulerEnabled ? '运行中' : '已停止' }}
            </el-tag>
          </div>
        </el-col>
      </el-row>
    </el-card>
    
    <!-- 快捷操作 -->
    <el-card class="action-card">
      <template #header>
        <span>⚡ 快捷操作</span>
      </template>
      
      <el-space wrap>
        <el-button type="success" @click="enableDownload" :loading="actionLoading">
          ✅ 启用下载
        </el-button>
        <el-button type="warning" @click="disableDownload" :loading="actionLoading">
          ⏸️ 关闭下载
        </el-button>
        <el-divider direction="vertical" />
        <el-button type="danger" @click="disableTranslation" :loading="actionLoading">
          🚫 关闭AI翻译
        </el-button>
        <el-button type="primary" @click="enableTranslation" :loading="actionLoading">
          🤖 启用AI翻译
        </el-button>
        <el-divider direction="vertical" />
        <el-button type="info" @click="triggerDownload" :loading="actionLoading">
          🔄 手动触发下载
        </el-button>
      </el-space>
    </el-card>
    
    <!-- 配置管理 -->
    <el-card class="config-card">
      <template #header>
        <div class="card-header">
          <span>⏰ 定时任务开关</span>
          <el-button type="primary" size="small" @click="loadTaskSwitches" :loading="taskSwitchLoading">
            刷新
          </el-button>
        </div>
      </template>
      
      <el-table :data="taskSwitches" stripe style="width: 100%">
        <el-table-column prop="name" label="任务名称" width="150" />
        <el-table-column prop="description" label="说明" />
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-switch
              v-model="scope.row.enabled"
              @change="(val) => toggleTaskSwitch(scope.row.key, val)"
              :loading="switchingKey === scope.row.key"
              active-text="开"
              inactive-text="关"
            />
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 爬虫配置 -->
    <el-card class="config-card">
      <template #header>
        <div class="card-header">
          <span>⚙️ 爬虫配置</span>
          <el-button type="primary" size="small" @click="loadConfig" :loading="configLoading">
            刷新配置
          </el-button>
        </div>
      </template>
      
      <el-table :data="configs" stripe style="width: 100%">
        <el-table-column prop="key" label="配置项" width="250" />
        <el-table-column prop="description" label="说明" width="200" />
        <el-table-column label="值">
          <template #default="scope">
            <el-input
              v-if="editingKey === scope.row.key"
              v-model="editValue"
              size="small"
              @keyup.enter="saveConfig(scope.row.key)"
            />
            <span v-else class="config-value">{{ formatValue(scope.row.value) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button
              v-if="editingKey !== scope.row.key"
              type="primary"
              size="small"
              @click="startEdit(scope.row)"
            >
              编辑
            </el-button>
            <template v-else>
              <el-button type="success" size="small" @click="saveConfig(scope.row.key)">
                保存
              </el-button>
              <el-button size="small" @click="cancelEdit">取消</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 待处理小说 -->
    <el-card class="pending-card">
      <template #header>
        <div class="card-header">
          <span>📚 待处理小说</span>
          <el-button type="primary" size="small" @click="loadPendingNovels" :loading="pendingLoading">
            刷新
          </el-button>
        </div>
      </template>
      
      <el-table :data="pendingNovels" stripe style="width: 100%">
        <el-table-column prop="novelId" label="ID" width="100" />
        <el-table-column prop="title" label="小说名称" />
        <el-table-column prop="platform" label="平台" width="120" />
        <el-table-column prop="pendingCount" label="待处理章节" width="120" />
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="downloadNovel(scope.row.novelId)"
              :loading="downloadingId === scope.row.novelId"
            >
              下载
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/axios'

export default {
  name: 'CrawlerManager',
  setup() {
    const loading = ref(false)
    const actionLoading = ref(false)
    const configLoading = ref(false)
    const pendingLoading = ref(false)
    const taskSwitchLoading = ref(false)
    const switchingKey = ref('')
    
    const status = ref({
      totalNovels: 0,
      pendingChapters: 0,
      translatingChapters: 0,
      errorChapters: 0,
      completedChapters: 0,
      schedulerEnabled: false,
      dbDownloadEnabled: false,
      dbTranslationEnabled: false
    })
    
    const configs = ref([])
    const pendingNovels = ref([])
    const taskSwitches = ref([])
    
    const editingKey = ref('')
    const editValue = ref('')
    const downloadingId = ref(null)
    
    // 加载状态
    const refreshStatus = async () => {
      loading.value = true
      try {
        const res = await request.get('/api/crawler/status')
        status.value = res.data
      } catch (e) {
        ElMessage.error('获取状态失败')
      } finally {
        loading.value = false
      }
    }
    
    // 加载配置
    const loadConfig = async () => {
      configLoading.value = true
      try {
        const res = await request.get('/api/crawler/config')
        configs.value = res.data
      } catch (e) {
        ElMessage.error('获取配置失败')
      } finally {
        configLoading.value = false
      }
    }
    
    // 加载待处理小说
    const loadPendingNovels = async () => {
      pendingLoading.value = true
      try {
        const res = await request.get('/api/crawler/pending-novels')
        pendingNovels.value = res.data
      } catch (e) {
        ElMessage.error('获取待处理小说失败')
      } finally {
        pendingLoading.value = false
      }
    }
    
    // 加载定时任务开关
    const loadTaskSwitches = async () => {
      taskSwitchLoading.value = true
      try {
        const res = await request.get('/api/crawler/task-switches')
        taskSwitches.value = res.data
      } catch (e) {
        ElMessage.error('获取任务开关失败')
      } finally {
        taskSwitchLoading.value = false
      }
    }
    
    // 切换任务开关
    const toggleTaskSwitch = async (key, enabled) => {
      switchingKey.value = key
      try {
        await request.post(`/api/crawler/task-switch/${key}`, { enabled })
        ElMessage.success(enabled ? '任务已启用' : '任务已关闭')
      } catch (e) {
        ElMessage.error('操作失败')
        // 回滚开关状态
        loadTaskSwitches()
      } finally {
        switchingKey.value = ''
      }
    }
    
    // 快捷操作
    const enableDownload = async () => {
      actionLoading.value = true
      try {
        await request.post('/api/crawler/enable-download')
        ElMessage.success('下载功能已启用')
        refreshStatus()
      } catch (e) {
        ElMessage.error('操作失败')
      } finally {
        actionLoading.value = false
      }
    }
    
    const disableDownload = async () => {
      actionLoading.value = true
      try {
        await request.post('/api/crawler/disable-download')
        ElMessage.success('下载功能已关闭')
        refreshStatus()
      } catch (e) {
        ElMessage.error('操作失败')
      } finally {
        actionLoading.value = false
      }
    }
    
    const enableTranslation = async () => {
      actionLoading.value = true
      try {
        await request.post('/api/crawler/enable-translation')
        ElMessage.success('AI翻译功能已启用')
        refreshStatus()
      } catch (e) {
        ElMessage.error('操作失败')
      } finally {
        actionLoading.value = false
      }
    }
    
    const disableTranslation = async () => {
      actionLoading.value = true
      try {
        await request.post('/api/crawler/disable-translation')
        ElMessage.success('AI翻译功能已关闭')
        refreshStatus()
      } catch (e) {
        ElMessage.error('操作失败')
      } finally {
        actionLoading.value = false
      }
    }
    
    const triggerDownload = async () => {
      actionLoading.value = true
      try {
        await request.post('/api/crawler/trigger-download')
        ElMessage.success('下载任务已触发')
      } catch (e) {
        ElMessage.error('操作失败')
      } finally {
        actionLoading.value = false
      }
    }
    
    // 配置编辑
    const startEdit = (row) => {
      editingKey.value = row.key
      editValue.value = row.value
    }
    
    const cancelEdit = () => {
      editingKey.value = ''
      editValue.value = ''
    }
    
    const saveConfig = async (key) => {
      try {
        await request.put(`/api/crawler/config/${key}`, { value: editValue.value })
        ElMessage.success('配置已保存')
        cancelEdit()
        loadConfig()
        refreshStatus()
      } catch (e) {
        ElMessage.error('保存失败')
      }
    }
    
    // 下载单个小说
    const downloadNovel = async (novelId) => {
      downloadingId.value = novelId
      try {
        await request.post(`/api/crawler/download-novel/${novelId}`)
        ElMessage.success('下载任务已开始')
      } catch (e) {
        ElMessage.error('下载失败')
      } finally {
        downloadingId.value = null
      }
    }
    
    // 格式化配置值显示
    const formatValue = (value) => {
      if (value && value.length > 50) {
        return value.substring(0, 50) + '...'
      }
      return value
    }
    
    onMounted(() => {
      refreshStatus()
      loadConfig()
      loadPendingNovels()
      loadTaskSwitches()
    })
    
    return {
      loading,
      actionLoading,
      configLoading,
      pendingLoading,
      taskSwitchLoading,
      switchingKey,
      status,
      configs,
      pendingNovels,
      taskSwitches,
      editingKey,
      editValue,
      downloadingId,
      refreshStatus,
      loadConfig,
      loadPendingNovels,
      loadTaskSwitches,
      toggleTaskSwitch,
      enableDownload,
      disableDownload,
      enableTranslation,
      disableTranslation,
      triggerDownload,
      startEdit,
      cancelEdit,
      saveConfig,
      downloadNovel,
      formatValue
    }
  }
}
</script>

<style scoped>
.crawler-manager {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.crawler-manager h2 {
  margin-bottom: 20px;
  color: #303133;
}

.status-card,
.action-card,
.config-card,
.pending-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.switch-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.config-value {
  font-family: monospace;
  color: #606266;
}

:deep(.el-statistic__content) {
  font-size: 24px;
}
</style>
