<template>
  <div class="source-admin-page">
    <div class="page-header">
      <div>
        <div class="eyebrow">书源维护</div>
        <h1>导入与验证书源</h1>
        <p>粘贴 Legado / 阅读书源 JSON，先校验，再导入到当前 FreeNovel 后端。</p>
      </div>
      <button class="ghost-button" @click="loadSources" :disabled="loadingSources">刷新列表</button>
    </div>

    <div class="admin-grid">
      <section class="panel">
        <div class="panel-header">
          <h2>书源 JSON</h2>
          <span>{{ sourceJson.length }} 字符</span>
        </div>
        <textarea
          v-model="sourceJson"
          class="source-input"
          placeholder="在这里粘贴单个书源对象，或书源数组 JSON"
        ></textarea>
        <div class="action-row">
          <button class="primary-button" @click="handleValidate" :disabled="actionLoading || !trimmedSourceJson">校验</button>
          <button class="primary-button import-button" @click="handleImport" :disabled="actionLoading || !trimmedSourceJson">导入</button>
        </div>
        <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>

        <div v-if="validateResult" class="result-box">
          <div class="result-title">校验结果</div>
          <div class="result-summary" :class="{ ok: issueCount === 0 }">
            {{ issueCount === 0 ? '未发现问题' : `发现 ${issueCount} 个问题` }}
          </div>
          <ul v-if="issueCount > 0" class="issue-list">
            <li v-for="(issue, index) in validateResult.issues" :key="index">
              <span class="issue-severity">{{ issue.severity || 'info' }}</span>
              {{ formatIssue(issue) }}
            </li>
          </ul>
        </div>

        <div v-if="importResult" class="result-box">
          <div class="result-title">导入结果</div>
          <div class="result-summary ok">已导入 {{ importResult.count || 0 }} 个书源</div>
          <ul v-if="importIssues.length > 0" class="issue-list">
            <li v-for="(issue, index) in importIssues" :key="index">
              <span class="issue-severity">{{ issue.severity || 'info' }}</span>
              {{ formatIssue(issue) }}
            </li>
          </ul>
        </div>
      </section>

      <section class="panel">
        <div class="panel-header">
          <h2>已导入书源</h2>
          <span>{{ sources.length }} 个</span>
        </div>
        <div v-if="loadingSources" class="empty-message">正在加载书源...</div>
        <div v-else-if="sources.length === 0" class="empty-message">还没有导入书源</div>
        <div v-else class="source-list">
          <article v-for="source in sources" :key="source.sourceId" class="source-card">
            <div class="source-card-header">
              <h3>{{ source.bookSourceName || '未命名书源' }}</h3>
              <span :class="['status-pill', source.enabled ? 'enabled' : 'disabled']">
                {{ source.enabled ? '启用' : '停用' }}
              </span>
            </div>
            <div class="source-url">{{ source.bookSourceUrl }}</div>
            <div class="source-id">ID：{{ source.sourceId }}</div>
            <div class="search-row">
              <input
                v-model="searchKeywords[source.sourceId]"
                type="text"
                placeholder="搜索书名，如：斗破"
                @keyup.enter="goSearch(source)"
              />
              <button class="primary-button small" @click="goSearch(source)">去搜索</button>
            </div>
          </article>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
import { importBookSources, listBookSources, validateBookSources } from '@/api/bookSources.mjs';

export default {
  name: 'SourceAdmin',
  data() {
    return {
      sourceJson: '',
      loadingSources: false,
      actionLoading: false,
      errorMessage: '',
      validateResult: null,
      importResult: null,
      sources: [],
      searchKeywords: {},
    };
  },
  computed: {
    trimmedSourceJson() {
      return this.sourceJson.trim();
    },
    issueCount() {
      return this.validateResult?.issues?.length || 0;
    },
    importIssues() {
      return this.importResult?.issues || [];
    },
  },
  mounted() {
    this.loadSources();
  },
  methods: {
    loadSources() {
      this.loadingSources = true;
      return listBookSources()
        .then((data) => {
          this.sources = data.sources || [];
          this.sources.forEach((source) => {
            if (!this.searchKeywords[source.sourceId]) {
              this.searchKeywords[source.sourceId] = '';
            }
          });
        })
        .catch((error) => {
          console.error('Load book sources failed:', error);
          this.errorMessage = '书源列表加载失败，请确认已登录且后端已启动';
        })
        .finally(() => {
          this.loadingSources = false;
        });
    },
    handleValidate() {
      if (!this.trimmedSourceJson || this.actionLoading) {
        return;
      }
      this.actionLoading = true;
      this.errorMessage = '';
      this.validateResult = null;
      validateBookSources(this.trimmedSourceJson)
        .then((data) => {
          this.validateResult = data || { issues: [] };
        })
        .catch((error) => {
          console.error('Validate book sources failed:', error);
          this.errorMessage = '校验失败，请检查 JSON 格式或登录状态';
        })
        .finally(() => {
          this.actionLoading = false;
        });
    },
    handleImport() {
      if (!this.trimmedSourceJson || this.actionLoading) {
        return;
      }
      this.actionLoading = true;
      this.errorMessage = '';
      this.importResult = null;
      importBookSources(this.trimmedSourceJson)
        .then((data) => {
          this.importResult = data || {};
          return this.loadSources();
        })
        .catch((error) => {
          console.error('Import book sources failed:', error);
          this.errorMessage = '导入失败，请先校验 JSON，并确认后端和登录状态正常';
        })
        .finally(() => {
          this.actionLoading = false;
        });
    },
    goSearch(source) {
      const keyword = (this.searchKeywords[source.sourceId] || '').trim();
      this.$router.push({
        name: 'SourceSearch',
        params: { sourceId: source.sourceId },
        query: keyword ? { keyword } : {},
      });
    },
    formatIssue(issue) {
      const path = issue.field ? `${issue.field}：` : '';
      return `${path}${issue.message || '未知问题'}`;
    },
  },
};
</script>

<style scoped>
.source-admin-page {
  max-width: 1180px;
  margin: 0 auto;
  padding: 24px 18px 40px;
  color: #333;
}

.page-header {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: flex-start;
  margin-bottom: 22px;
}

.eyebrow {
  color: #ff6600;
  font-size: 13px;
  font-weight: bold;
  margin-bottom: 6px;
}

.page-header h1 {
  margin: 0 0 8px;
  font-size: 28px;
}

.page-header p {
  margin: 0;
  color: #777;
}

.admin-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(320px, 420px);
  gap: 20px;
}

.panel {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  padding: 18px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
  color: #777;
  font-size: 13px;
}

.panel-header h2 {
  margin: 0;
  color: #333;
  font-size: 18px;
}

.source-input {
  width: 100%;
  min-height: 360px;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  resize: vertical;
  outline: none;
  font-family: Consolas, monospace;
  font-size: 13px;
  line-height: 1.5;
  box-sizing: border-box;
}

.source-input:focus,
.search-row input:focus {
  border-color: #ff6600;
}

.action-row {
  display: flex;
  gap: 10px;
  margin-top: 14px;
}

.primary-button,
.ghost-button {
  border: none;
  border-radius: 18px;
  padding: 8px 18px;
  cursor: pointer;
  font-size: 14px;
}

.primary-button {
  background: #ff6600;
  color: #fff;
}

.import-button {
  background: #333;
}

.ghost-button {
  background: #f5f5f5;
  color: #666;
}

.primary-button.small {
  padding: 7px 14px;
  white-space: nowrap;
}

.primary-button:disabled,
.ghost-button:disabled {
  background: #c9c9c9;
  color: #fff;
  cursor: not-allowed;
}

.result-box {
  margin-top: 16px;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 12px;
  background: #fafafa;
}

.result-title {
  font-weight: bold;
  margin-bottom: 8px;
}

.result-summary {
  color: #c23a3a;
}

.result-summary.ok {
  color: #238636;
}

.issue-list {
  margin: 10px 0 0;
  padding-left: 18px;
  color: #666;
  line-height: 1.6;
}

.issue-severity {
  color: #ff6600;
  margin-right: 6px;
}

.source-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.source-card {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 12px;
  background: #fff;
}

.source-card-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.source-card h3 {
  margin: 0 0 8px;
  font-size: 16px;
}

.status-pill {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  color: #fff;
}

.status-pill.enabled {
  background: #238636;
}

.status-pill.disabled {
  background: #999;
}

.source-url,
.source-id {
  color: #777;
  font-size: 12px;
  word-break: break-all;
  margin-bottom: 6px;
}

.search-row {
  display: flex;
  gap: 8px;
  margin-top: 10px;
}

.search-row input {
  flex: 1;
  min-width: 0;
  border: 1px solid #ddd;
  border-radius: 16px;
  padding: 7px 10px;
  outline: none;
}

.error-message {
  color: #c23a3a;
  margin-top: 12px;
}

.empty-message {
  color: #777;
  text-align: center;
  padding: 40px 0;
}

@media (max-width: 900px) {
  .page-header,
  .admin-grid {
    display: block;
  }

  .ghost-button {
    margin-top: 14px;
  }

  .panel {
    margin-bottom: 18px;
  }
}
</style>
