<template>
  <div class="translation-config">
    <h2>翻译API配置管理</h2>
    
    <!-- 平台列表 -->
    <div class="section">
      <div class="section-header">
        <h3>翻译平台</h3>
        <el-button type="primary" size="small" @click="showAddPlatform = true">添加平台</el-button>
      </div>
      
      <el-table :data="platforms" style="width: 100%" v-loading="loading">
        <el-table-column prop="platformName" label="平台名称" width="150" />
        <el-table-column prop="apiUrl" label="API URL" min-width="300">
          <template #default="scope">
            <el-input 
              v-model="scope.row.apiUrl" 
              size="small"
              @blur="updateApiUrl(scope.row)"
              placeholder="请输入API URL"
            />
          </template>
        </el-table-column>
        <el-table-column prop="apiKeyCount" label="密钥数量" width="100" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button type="primary" size="small" @click="manageKeys(scope.row)">
              管理密钥
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 添加平台对话框 -->
    <el-dialog v-model="showAddPlatform" title="添加翻译平台" width="500px">
      <el-form :model="newPlatform" label-width="100px">
        <el-form-item label="平台名称">
          <el-input v-model="newPlatform.platformName" placeholder="例如: xianyu, siliconflow" />
        </el-form-item>
        <el-form-item label="API URL">
          <el-input v-model="newPlatform.apiUrl" placeholder="例如: https://api.example.com/v1/chat/completions" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddPlatform = false">取消</el-button>
        <el-button type="primary" @click="addPlatform">添加</el-button>
      </template>
    </el-dialog>

    <!-- API密钥管理对话框 -->
    <el-dialog v-model="showKeyManager" :title="'管理密钥 - ' + (currentPlatform?.platformName || '')" width="700px">
      <div class="key-manager">
        <div class="add-key-form">
          <el-input v-model="newApiKey" placeholder="输入新的API密钥" style="width: 70%">
            <template #prepend>API Key</template>
          </el-input>
          <el-button type="primary" @click="addApiKey" style="margin-left: 10px">添加</el-button>
        </div>
        
        <el-table :data="currentKeys" style="width: 100%; margin-top: 20px" v-loading="keyLoading">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="API密钥" min-width="250">
            <template #default="scope">
              <div class="key-display">
                <span v-if="!scope.row.showFull">{{ scope.row.apiKey }}</span>
                <span v-else>{{ scope.row.fullApiKey }}</span>
                <el-button 
                  type="text" 
                  size="small" 
                  @click="scope.row.showFull = !scope.row.showFull"
                >
                  {{ scope.row.showFull ? '隐藏' : '显示' }}
                </el-button>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button type="warning" size="small" @click="editKey(scope.row)">编辑</el-button>
              <el-button type="danger" size="small" @click="deleteKey(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <!-- 编辑密钥对话框 -->
    <el-dialog v-model="showEditKey" title="编辑API密钥" width="500px">
      <el-input v-model="editingKey.apiKey" placeholder="输入新的API密钥" />
      <template #footer>
        <el-button @click="showEditKey = false">取消</el-button>
        <el-button type="primary" @click="saveEditedKey">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import service from "@/api/axios";

export default {
  name: 'TranslationConfig',
  data() {
    return {
      platforms: [],
      loading: false,
      showAddPlatform: false,
      newPlatform: {
        platformName: '',
        apiUrl: ''
      },
      showKeyManager: false,
      currentPlatform: null,
      currentKeys: [],
      keyLoading: false,
      newApiKey: '',
      showEditKey: false,
      editingKey: {
        id: null,
        apiKey: ''
      }
    };
  },
  created() {
    this.loadPlatforms();
  },
  methods: {
    async loadPlatforms() {
      this.loading = true;
      try {
        const response = await service.get('/api/translation-config/platforms');
        this.platforms = response.data;
      } catch (error) {
        this.$message.error('加载平台列表失败');
      } finally {
        this.loading = false;
      }
    },
    
    async addPlatform() {
      if (!this.newPlatform.platformName) {
        this.$message.warning('请输入平台名称');
        return;
      }
      try {
        await service.post('/api/translation-config/platforms', this.newPlatform);
        this.$message.success('平台添加成功');
        this.showAddPlatform = false;
        this.newPlatform = { platformName: '', apiUrl: '' };
        this.loadPlatforms();
      } catch (error) {
        this.$message.error(error.response?.data?.error || '添加失败');
      }
    },
    
    async updateApiUrl(platform) {
      try {
        await service.put(`/api/translation-config/platforms/${platform.id}/url`, {
          apiUrl: platform.apiUrl
        });
        this.$message.success('API URL更新成功');
      } catch (error) {
        this.$message.error('更新失败');
      }
    },
    
    async manageKeys(platform) {
      this.currentPlatform = platform;
      this.showKeyManager = true;
      this.loadKeys(platform.id);
    },
    
    async loadKeys(platformId) {
      this.keyLoading = true;
      try {
        const response = await service.get(`/api/translation-config/platforms/${platformId}/keys`);
        this.currentKeys = response.data.map(k => ({ ...k, showFull: false }));
      } catch (error) {
        this.$message.error('加载密钥列表失败');
      } finally {
        this.keyLoading = false;
      }
    },
    
    async addApiKey() {
      if (!this.newApiKey) {
        this.$message.warning('请输入API密钥');
        return;
      }
      try {
        await service.post(`/api/translation-config/platforms/${this.currentPlatform.id}/keys`, {
          apiKey: this.newApiKey
        });
        this.$message.success('API密钥添加成功');
        this.newApiKey = '';
        this.loadKeys(this.currentPlatform.id);
        this.loadPlatforms();
      } catch (error) {
        this.$message.error(error.response?.data?.error || '添加失败');
      }
    },
    
    editKey(key) {
      this.editingKey = {
        id: key.id,
        apiKey: key.fullApiKey
      };
      this.showEditKey = true;
    },
    
    async saveEditedKey() {
      try {
        await service.put(`/api/translation-config/keys/${this.editingKey.id}`, {
          apiKey: this.editingKey.apiKey
        });
        this.$message.success('API密钥更新成功');
        this.showEditKey = false;
        this.loadKeys(this.currentPlatform.id);
      } catch (error) {
        this.$message.error('更新失败');
      }
    },
    
    async deleteKey(key) {
      try {
        await this.$confirm('确定要删除这个API密钥吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        await service.delete(`/api/translation-config/keys/${key.id}`);
        this.$message.success('API密钥已删除');
        this.loadKeys(this.currentPlatform.id);
        this.loadPlatforms();
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败');
        }
      }
    }
  }
};
</script>

<style scoped>
.translation-config {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.translation-config h2 {
  margin-bottom: 20px;
  color: #333;
}

.section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.section-header h3 {
  margin: 0;
  color: #333;
}

.add-key-form {
  display: flex;
  align-items: center;
}

.key-display {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.key-display span {
  font-family: monospace;
  word-break: break-all;
}
</style>
