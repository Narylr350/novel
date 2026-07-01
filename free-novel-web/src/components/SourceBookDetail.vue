<template>
  <div class="novel-container">
    <div class="content-wrapper">
      <div class="main-content">
        <div class="novel-header">
          <div class="novel-info-container">
            <div class="cover">
              <img v-if="book.coverUrl" :src="book.coverUrl" alt="小说封面">
              <div v-else class="placeholder-image">无封面</div>
            </div>
            <div class="novel-info">
              <h1 class="novel-title">{{ book.name || '书源小说详情' }}</h1>
              <div class="tags">
                <span class="tag">书源：{{ sourceId }}</span>
                <span class="tag" v-if="book.kind">{{ book.kind }}</span>
              </div>
              <div class="novel-stats">
                <span class="stat">
                  作者：{{ book.author || '未知' }}
                  <template v-if="book.wordCount"> | 字数：{{ book.wordCount }}</template>
                  <template v-if="book.updateTime"> | 更新：{{ book.updateTime }}</template>
                </span>
              </div>
              <div class="novel-intro" v-if="book.intro">{{ book.intro }}</div>
            </div>
          </div>
          <div class="action-buttons">
            <button class="read-button" @click="readFirstChapter" :disabled="chapters.length === 0">阅读</button>
            <button class="shelf-button" @click="goBackSearch">返回搜索</button>
          </div>
        </div>

        <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
        <div v-if="loading" class="section">
          <h2 class="section-title">目录</h2>
          <div class="empty-message">正在加载...</div>
        </div>
        <div v-else class="section">
          <div class="section-header">
            <h2 class="section-title">目录 · {{ chapters.length }}章</h2>
            <div class="sort-controls">
              <span>排序: </span>
              <button @click="toggleSortOrder" class="sort-button" :class="{ active: sortOrder === 'asc' }">正序</button>
              <button @click="toggleSortOrder" class="sort-button" :class="{ active: sortOrder === 'desc' }">倒序</button>
            </div>
          </div>
          <div v-if="chapters.length === 0" class="empty-message">暂无章节</div>
          <div v-else class="chapter-list1">
            <div class="chapter-grid">
              <div
                class="chapter-item"
                v-for="chapter in sortedChapters"
                :key="chapter.chapterKey || chapter.chapterUrl"
                @click="gotoChapter(chapter)"
              >
                <div class="chapter-number">第{{ chapter.index + 1 }}章</div>
                <div class="chapter-title">{{ chapter.title || '未命名章节' }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getSourceBookDetail, getSourceBookToc } from '@/api/bookSources.mjs';

export default {
  name: 'SourceBookDetail',
  data() {
    return {
      loading: false,
      errorMessage: '',
      book: {},
      chapters: [],
      sortOrder: 'asc',
    };
  },
  computed: {
    sourceId() {
      return this.$route.params.sourceId || '';
    },
    bookKey() {
      return this.$route.query.bookKey || '';
    },
    bookUrl() {
      return this.$route.query.bookUrl || '';
    },
    tocUrl() {
      return this.book.tocUrl || this.$route.query.tocUrl || '';
    },
    sortedChapters() {
      const chapters = [...this.chapters];
      return chapters.sort((a, b) => {
        return this.sortOrder === 'asc' ? a.index - b.index : b.index - a.index;
      });
    },
  },
  mounted() {
    this.fetchDetail();
  },
  methods: {
    fetchDetail() {
      if (!this.sourceId || !this.bookUrl) {
        this.errorMessage = '缺少书源或书籍地址';
        return;
      }
      this.loading = true;
      this.errorMessage = '';
      getSourceBookDetail({
        sourceId: this.sourceId,
        bookKey: this.bookKey,
        bookUrl: this.bookUrl,
      })
        .then((book) => {
          this.book = book || {};
          return getSourceBookToc({
            sourceId: this.sourceId,
            bookKey: this.book.bookKey || this.bookKey,
            bookUrl: this.book.bookUrl || this.bookUrl,
            tocUrl: this.book.tocUrl,
          });
        })
        .then((toc) => {
          this.chapters = toc.chapters || [];
        })
        .catch((error) => {
          console.error('Source detail failed:', error);
          this.errorMessage = '详情或目录加载失败';
        })
        .finally(() => {
          this.loading = false;
        });
    },
    toggleSortOrder() {
      this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc';
    },
    readFirstChapter() {
      if (this.sortedChapters.length > 0) {
        this.gotoChapter(this.sortedChapters[0]);
      }
    },
    gotoChapter(chapter) {
      this.$router.push({
        name: 'SourceChapterDetail',
        params: { sourceId: this.sourceId },
        query: {
          bookKey: this.book.bookKey || this.bookKey,
          bookUrl: this.book.bookUrl || this.bookUrl,
          tocUrl: this.tocUrl,
          chapterKey: chapter.chapterKey,
          chapterUrl: chapter.chapterUrl,
        },
      });
    },
    goBackSearch() {
      this.$router.push({ name: 'SourceSearch', params: { sourceId: this.sourceId } });
    },
  },
};
</script>

<style scoped>
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.sort-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sort-button {
  padding: 4px 10px;
  background-color: #f0f0f0;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.sort-button.active {
  background-color: #ff6b00;
  color: white;
  border-color: #ff6b00;
}

.novel-container {
  width: 100%;
  background-color: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 20px;
}

.content-wrapper {
  max-width: 60%;
  margin: 0 auto;
  padding: 0 15px;
}

.main-content {
  padding-top: 15px;
}

.novel-header,
.section {
  background-color: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
}

.section {
  padding: 15px;
}

.novel-info-container {
  display: flex;
  padding: 15px;
}

.cover {
  width: 120px;
  height: 160px;
  overflow: hidden;
  display: inline-block;
}

.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.placeholder-image {
  width: 100%;
  height: 100%;
  background-color: #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 12px;
}

.novel-info {
  display: flex;
  flex-direction: column;
  width: calc(100% - 140px);
  margin-left: 20px;
}

.novel-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 10px;
  color: #333;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 10px;
}

.tag {
  background-color: #f0f0f0;
  color: #666;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  margin-right: 8px;
  margin-bottom: 5px;
}

.novel-stats,
.novel-intro {
  font-size: 12px;
  color: #666;
  line-height: 1.7;
  margin-bottom: 10px;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  padding: 15px;
  border-top: 1px solid #f0f0f0;
}

.read-button,
.shelf-button {
  border: none;
  border-radius: 20px;
  padding: 8px 15px;
  font-size: 14px;
  margin-right: 10px;
  margin-bottom: 10px;
  cursor: pointer;
}

.read-button {
  background-color: #ff6b00;
  color: white;
}

.read-button:disabled {
  background-color: #c9c9c9;
  cursor: not-allowed;
}

.shelf-button {
  background-color: #f5f5f5;
  color: #666;
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  border-left: 4px solid #ff6b00;
  padding-left: 10px;
}

.chapter-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.chapter-item {
  display: flex;
  flex-direction: column;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 5px;
  font-size: 12px;
  color: #333;
  cursor: pointer;
}

.chapter-number {
  color: #ff6b00;
  font-weight: bold;
  margin-bottom: 5px;
}

.chapter-title {
  font-size: 12px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.error-message {
  color: #c23a3a;
  margin: 0 0 16px 0;
}

.empty-message {
  color: #777;
  padding: 30px 0;
  text-align: center;
}

@media (max-width: 768px) {
  .content-wrapper {
    max-width: 100%;
  }

  .cover {
    width: 80px;
    height: 120px;
  }

  .novel-info {
    margin-left: 20px;
    width: calc(100% - 100px);
  }

  .chapter-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
