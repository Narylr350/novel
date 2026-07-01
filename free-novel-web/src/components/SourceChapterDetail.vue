<template>
  <div :style="{ fontSize: `${currentFontSize}px`, color: textFontColor, backgroundColor: textColor }" class="source-reader">
    <div class="settings-panel" v-if="showSettings">
      <div class="settings-header">
        <h3>设置</h3>
        <button @click="showSettings = false">×</button>
      </div>
      <div class="setting-group">
        <label>阅读背景</label>
        <div class="theme-options">
          <button class="theme-light" @click="changeTheme('#f5f5f5')"></button>
          <button class="theme-yellow" @click="changeTheme('#f9f6e6')"></button>
          <button class="theme-black" @click="changeTheme('#111111')"></button>
        </div>
      </div>
      <div class="setting-group">
        <label>字体大小</label>
        <div class="font-size-controls">
          <button @click="decreaseFontSize">A-</button>
          <span>{{ currentFontSize }}</span>
          <button @click="increaseFontSize">A+</button>
        </div>
      </div>
    </div>

    <div class="sidebar" :class="{ show: showSidebar }">
      <div class="sidebar-actions">
        <button @click="showSettings = true">设置</button>
        <button @click="goBackBook">目录</button>
        <button @click="goBack">返回</button>
      </div>
    </div>

    <div v-if="loading" class="loading-modal" :style="{ backgroundColor: textColor, color: textFontColor }">
      <div class="loading-content" :style="{ backgroundColor: textColor, color: textFontColor }">
        <div class="loading-spinner"></div>
        <p>正在加载内容，请稍候...</p>
      </div>
    </div>

    <div class="reader-main" @click="toggleSidebar">
      <div class="reader-header">
        <h2 class="chapter-title">{{ chapter.title || '书源章节' }}</h2>
        <div class="reader-meta">
          <span>书源：{{ sourceId }}</span>
          <span v-if="chapter.rendererMode">调试渲染：{{ chapter.rendererMode }}</span>
          <span v-if="chapter.finalUrl">最终地址：{{ chapter.finalUrl }}</span>
        </div>
      </div>

      <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
      <div v-else-if="!loading && contentLines.length === 0" class="empty-message">
        正文为空，请检查书源正文规则或该章节是否需要桌面浏览器渲染。
      </div>
      <div v-else class="novel-content">
        <p v-for="(line, index) in contentLines" :key="index">{{ line }}</p>
      </div>
    </div>

    <div class="novel-footer" :style="{ backgroundColor: textColor, color: textFontColor }">
      <button class="next-chapter-btn" @click="goBackBook" :style="{ backgroundColor: textColor, color: textFontColor }">返回目录</button>
      <button class="next-chapter-btn" @click="goHome" :style="{ backgroundColor: textColor, color: textFontColor }">回到主页</button>
    </div>
  </div>
</template>

<script>
import { getSourceChapterContent } from '@/api/bookSources.mjs';

export default {
  name: 'SourceChapterDetail',
  data() {
    return {
      loading: false,
      errorMessage: '',
      chapter: {},
      currentFontSize: Number(localStorage.getItem('sourceReaderFontSize')) || 18,
      textColor: localStorage.getItem('sourceReaderBg') || '#f5f5f5',
      textFontColor: localStorage.getItem('sourceReaderColor') || '#333333',
      showSettings: false,
      showSidebar: false,
    };
  },
  computed: {
    sourceId() {
      return this.$route.params.sourceId || '';
    },
    contentLines() {
      const content = this.chapter.content || '';
      return content.split(/\r?\n/).map((line) => line.trim()).filter(Boolean);
    },
  },
  mounted() {
    this.fetchContent();
  },
  methods: {
    fetchContent() {
      if (!this.sourceId || !this.$route.query.chapterUrl) {
        this.errorMessage = '缺少书源章节地址';
        return;
      }
      this.loading = true;
      this.errorMessage = '';
      getSourceChapterContent({
        sourceId: this.sourceId,
        bookKey: this.$route.query.bookKey || '',
        chapterKey: this.$route.query.chapterKey || '',
        bookUrl: this.$route.query.bookUrl || '',
        tocUrl: this.$route.query.tocUrl || '',
        chapterUrl: this.$route.query.chapterUrl || '',
      })
        .then((chapter) => {
          this.chapter = chapter || {};
        })
        .catch((error) => {
          console.error('Source content failed:', error);
          this.errorMessage = '正文加载失败';
        })
        .finally(() => {
          this.loading = false;
        });
    },
    toggleSidebar() {
      this.showSidebar = !this.showSidebar;
    },
    changeTheme(color) {
      this.textColor = color;
      this.textFontColor = color === '#111111' ? '#eeeeee' : '#333333';
      localStorage.setItem('sourceReaderBg', this.textColor);
      localStorage.setItem('sourceReaderColor', this.textFontColor);
    },
    decreaseFontSize() {
      this.currentFontSize = Math.max(14, this.currentFontSize - 1);
      localStorage.setItem('sourceReaderFontSize', this.currentFontSize);
    },
    increaseFontSize() {
      this.currentFontSize = Math.min(30, this.currentFontSize + 1);
      localStorage.setItem('sourceReaderFontSize', this.currentFontSize);
    },
    goBackBook() {
      this.$router.push({
        name: 'SourceBookDetail',
        params: { sourceId: this.sourceId },
        query: {
          bookKey: this.$route.query.bookKey,
          bookUrl: this.$route.query.bookUrl,
          tocUrl: this.$route.query.tocUrl,
          keyword: this.$route.query.keyword,
        },
      });
    },
    goBack() {
      this.$router.back();
    },
    goHome() {
      this.$router.push({ name: 'RecommendationList' });
    },
  },
};
</script>

<style scoped>
.source-reader {
  position: relative;
  min-height: 100vh;
}

.reader-main {
  max-width: 800px;
  margin: 0 auto;
  padding: 30px 10px;
}

.reader-header {
  margin-bottom: 20px;
}

.chapter-title {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 10px;
}

.reader-meta {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
  color: #999;
  font-size: 14px;
  word-break: break-all;
}

.novel-content {
  line-height: 1.8;
  text-align: justify;
  max-width: 100%;
  overflow-wrap: break-word;
  word-break: break-word;
}

.novel-content p {
  text-indent: 2em;
  margin-bottom: 30px;
  white-space: pre-wrap;
}

.novel-footer {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
}

.next-chapter-btn {
  padding: 10px 20px;
  border: 1px solid #ddd;
  border-radius: 30px;
  cursor: pointer;
  font-size: 14px;
  width: 45%;
  height: 40px;
}

.sidebar {
  width: 60px;
  height: 100vh;
  position: fixed;
  right: -60px;
  top: 0;
  background-color: #f0f0f0;
  box-shadow: -2px 0 5px rgba(0, 0, 0, 0.1);
  z-index: 800;
  padding-top: 20px;
  transition: right 0.3s ease;
}

.sidebar.show {
  right: 0;
}

.sidebar-actions button {
  width: 48px;
  min-height: 54px;
  margin: 0 6px 8px 6px;
  background: none;
  border: none;
  cursor: pointer;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
}

.sidebar-actions button:hover {
  background-color: #e0e0e0;
}

.settings-panel {
  position: fixed;
  top: 0;
  right: 0;
  width: 300px;
  max-width: 90vw;
  background-color: #fff;
  box-shadow: -2px 0 10px rgba(0, 0, 0, 0.1);
  z-index: 900;
  padding: 20px;
}

.settings-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.settings-header button {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
}

.setting-group {
  margin-bottom: 20px;
}

.setting-group label {
  display: block;
  margin-bottom: 10px;
  font-weight: bold;
}

.theme-options {
  display: flex;
  gap: 10px;
}

.theme-options button {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 1px solid #ddd;
  cursor: pointer;
}

.theme-light {
  background-color: #f5f5f5;
}

.theme-yellow {
  background-color: #f9f6e6;
}

.theme-black {
  background-color: #111111;
}

.font-size-controls {
  display: flex;
  align-items: center;
}

.font-size-controls button {
  padding: 5px 10px;
  margin: 0 5px;
  background-color: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
}

.loading-modal {
  position: fixed;
  inset: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.loading-content {
  padding: 20px;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  border-top-color: #333;
  animation: spin 1s ease-in-out infinite;
  margin-bottom: 10px;
}

.error-message {
  color: #c23a3a;
  margin: 20px 0;
  text-align: center;
}

.empty-message {
  color: #777;
  margin: 40px 0;
  text-align: center;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .reader-main {
    padding: 20px 14px;
  }

  .novel-footer {
    gap: 10px;
  }
}
</style>
