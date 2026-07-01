<template>
  <div class="source-search-page">
    <div class="search-bar">
      <input type="text" v-model="keyword" @keyup.enter="handleSearch" placeholder="请输入书名" />
      <button @click="handleSearch" class="search-button" :disabled="loading">搜索</button>
    </div>

    <div class="source-meta">
      <span>书源：{{ sourceTitle }}</span>
      <span v-if="resultCount !== null">结果：{{ resultCount }}</span>
      <button class="link-button" @click="goAdmin">返回书源管理</button>
    </div>

    <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
    <div v-if="loading" class="empty-message">正在搜索...</div>
    <div v-else-if="searched && searchResults.length === 0" class="empty-message">暂无结果</div>

    <div class="novel-list">
      <div v-for="book in searchResults" :key="book.bookKey || book.bookUrl" class="novel-card">
        <div class="novel-info">
          <h3 class="novel-title">{{ book.name || '未命名小说' }}</h3>
          <div class="novel-meta" v-if="book.author">作者：{{ book.author }}</div>
          <div class="novel-meta" v-if="book.kind">分类：{{ book.kind }}</div>
          <div class="novel-meta" v-if="book.wordCount">字数：{{ book.wordCount }}</div>
          <div class="novel-meta" v-if="book.lastChapter">最新：{{ book.lastChapter }}</div>
          <div class="novel-intro" v-if="book.intro">{{ book.intro }}</div>
        </div>
        <button class="read-button" @click="gotoBook(book)">查看详情</button>
      </div>
    </div>
  </div>
</template>

<script>
import { listBookSources, searchSourceBooks } from '@/api/bookSources.mjs';

export default {
  name: 'SourceSearch',
  data() {
    return {
      keyword: '',
      loading: false,
      searched: false,
      errorMessage: '',
      searchResults: [],
      resultCount: null,
      source: null,
    };
  },
  computed: {
    sourceId() {
      return this.$route.params.sourceId || '';
    },
    sourceTitle() {
      return this.source?.bookSourceName || this.sourceId;
    },
  },
  mounted() {
    this.keyword = this.$route.query.keyword || '';
    this.loadSourceMeta();
    if (this.keyword) {
      this.searchByKeyword();
    }
  },
  methods: {
    loadSourceMeta() {
      listBookSources()
        .then((data) => {
          this.source = (data.sources || []).find((item) => item.sourceId === this.sourceId) || null;
        })
        .catch((error) => {
          console.error('Load source meta failed:', error);
        });
    },
    handleSearch() {
      const nextKeyword = this.keyword.trim();
      if (!nextKeyword || this.loading) {
        return;
      }
      this.$router.replace({
        name: 'SourceSearch',
        params: { sourceId: this.sourceId },
        query: { keyword: nextKeyword },
      });
      this.searchByKeyword();
    },
    searchByKeyword() {
      const keyword = this.keyword.trim();
      if (!keyword) {
        this.searchResults = [];
        this.resultCount = null;
        return;
      }
      this.loading = true;
      this.searched = true;
      this.errorMessage = '';
      searchSourceBooks({ sourceId: this.sourceId, keyword, page: 1 })
        .then((data) => {
          this.searchResults = data.books || [];
          this.resultCount = this.searchResults.length;
        })
        .catch((error) => {
          console.error('Source search failed:', error);
          this.searchResults = [];
          this.resultCount = 0;
          this.errorMessage = '搜索失败，请检查书源和登录状态';
        })
        .finally(() => {
          this.loading = false;
        });
    },
    gotoBook(book) {
      this.$router.push({
        name: 'SourceBookDetail',
        params: { sourceId: this.sourceId },
        query: {
          bookKey: book.bookKey,
          bookUrl: book.bookUrl,
          keyword: this.keyword.trim(),
        },
      });
    },
    goAdmin() {
      this.$router.push({ name: 'SourceAdmin' });
    },
  },
};
</script>

<style scoped>
.source-search-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.search-bar {
  display: flex;
  margin-bottom: 12px;
}

.search-bar input {
  flex: 1;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px 0 0 4px;
  outline: none;
}

.search-button {
  padding: 10px 20px;
  background-color: #ff6600;
  color: white;
  border: none;
  border-radius: 0 4px 4px 0;
  cursor: pointer;
}

.search-button:disabled {
  background-color: #c9c9c9;
  cursor: not-allowed;
}

.source-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: #777;
  margin-bottom: 18px;
}

.link-button {
  border: none;
  background: none;
  color: #ff6600;
  cursor: pointer;
  padding: 0;
}

.novel-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.novel-card {
  display: flex;
  border-bottom: 1px solid #eee;
  padding-bottom: 20px;
  position: relative;
}

.novel-info {
  flex: 1;
  padding-right: 110px;
}

.novel-title {
  font-size: 18px;
  margin-bottom: 8px;
}

.novel-meta {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.novel-intro {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-top: 10px;
}

.read-button {
  position: absolute;
  right: 0;
  top: 0;
  padding: 5px 10px;
  background-color: #ff6600;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.error-message {
  color: #c23a3a;
  margin-bottom: 16px;
}

.empty-message {
  color: #777;
  margin: 30px 0;
  text-align: center;
}

@media (max-width: 768px) {
  .novel-card {
    flex-direction: column;
  }

  .novel-info {
    padding-right: 0;
  }

  .read-button {
    position: static;
    margin-top: 10px;
    align-self: flex-end;
  }
}
</style>
