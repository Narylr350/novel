<template>
  <div :style="readerStyle" class="source-reader">
    <div class="guide-modal" v-if="showGuide">
      <div class="modal-content">
        <div class="modal-header">
          <h3>指南</h3>
          <button @click="showGuide = false">×</button>
        </div>
        <div class="guide-body">
          <p>1、点击正文中间区域可唤出左右侧工具栏。</p>
          <p>2、开启“点击翻页”后，点击屏幕上方/下方可上滑/下滑。</p>
          <p>3、目录、上一章、下一章基于当前书源目录跳转，不写入本地小说库。</p>
          <p>4、书源章节暂不支持本地小说的段评、润色版本、编辑保存等本地库功能。</p>
        </div>
      </div>
    </div>

    <div class="chapter-list" :class="{ show: showChapterList }" :style="{ backgroundColor: textColor, color: textFontColor }">
      <div class="drawer-header">
        <h3>目录</h3>
        <button @click="showChapterList = false">×</button>
      </div>
      <div class="chapter-search">
        <input type="text" placeholder="搜索章节" v-model="searchQuery">
      </div>
      <div class="chapter-body">
        <ul>
          <li
            v-for="chapterItem in filteredChapters"
            :key="chapterItem.chapterKey || chapterItem.chapterUrl"
            :class="{ active: isCurrentChapter(chapterItem) }"
          >
            <span>{{ chapterItem.title || '未命名章节' }}</span>
            <button @click="goToChapter(chapterItem)">阅读本章</button>
          </li>
        </ul>
      </div>
    </div>

    <div class="settings-panel" :class="{ show: showSettings }" :style="{ backgroundColor: textColor, color: textFontColor }">
      <div class="drawer-header">
        <h3>设置</h3>
        <button @click="showSettings = false">×</button>
      </div>
      <div class="settings-content">
        <div class="setting-group">
          <label>阅读背景</label>
          <div class="theme-options">
            <button v-for="theme in themes" :key="theme.color" :class="theme.class" @click="changeTheme(theme.color)"></button>
            <el-color-picker v-model="textColor" show-alpha :predefine="predefineColors" @change="changeTheme" />
          </div>
        </div>
        <div class="setting-group">
          <label>字体颜色</label>
          <div class="theme-options">
            <button v-for="theme in themes" :key="`font-${theme.color}`" :class="theme.class" @click="changeFontTheme(theme.color)"></button>
            <el-color-picker v-model="textFontColor" show-alpha :predefine="predefineColors" @change="changeFontTheme" />
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
        <div class="setting-group">
          <label>段距大小</label>
          <div class="font-size-controls">
            <button @click="decreaseLineSpacing">A-</button>
            <span>{{ lineSpacing }}</span>
            <button @click="increaseLineSpacing">A+</button>
          </div>
        </div>
        <div class="setting-group">
          <label>字体加粗</label>
          <div class="font-size-controls">
            <button @click="decreaseLineWeight">A-</button>
            <span>{{ lineWeight }}</span>
            <button @click="increaseLineWeight">A+</button>
          </div>
        </div>
        <div class="setting-group">
          <label>阅读方式</label>
          <div class="reading-mode">
            <button @click="toggleReadingMode(false)" :class="{ active: !isClickablePagination }">手动换页</button>
            <button @click="toggleReadingMode(true)" :class="{ active: isClickablePagination }">点击换页</button>
          </div>
        </div>
      </div>
    </div>

    <div class="sidebar right" :class="{ show: showSidebar }">
      <div class="sidebar-actions">
        <button @click="showGuide = true"><i>🆘</i><span>帮助</span></button>
        <button @click="openChapterList"><i>📝</i><span>目录</span></button>
        <button @click="prevChapter" :disabled="!prevChapterItem"><i>⬅️</i><span>上章</span></button>
        <button @click="nextChapter" :disabled="!nextChapterItem"><i>➡️</i><span>下章</span></button>
        <button @click="goBack"><i>🔙</i><span>返回</span></button>
      </div>
    </div>

    <div class="sidebar left" :class="{ show: showSidebar }">
      <div class="sidebar-actions">
        <button v-if="isSunMoon" @click="sunMoon(false)"><i>🌜</i><span>夜间模式</span></button>
        <button v-else @click="sunMoon(true)"><i>🌞</i><span>白天模式</span></button>
        <button @click="showSettings = true"><i>⚙️</i><span>设置</span></button>
      </div>
    </div>

    <div v-if="loading" class="loading-modal" :style="{ backgroundColor: textColor, color: textFontColor }">
      <div class="loading-content" :style="{ backgroundColor: textColor, color: textFontColor }">
        <div class="loading-spinner"></div>
        <p>正在加载内容，请稍候...</p>
      </div>
    </div>

    <div class="reader-main" @click="handleReaderClick">
      <div class="reader-header">
        <h2 class="chapter-title">{{ chapter.title || queryChapterTitle || '书源章节' }}</h2>
        <div class="reader-meta">
          <span>字数：{{ contentCharCount }}</span>
          <span>书源：{{ sourceId }}</span>
          <span v-if="chapter.rendererMode">调试渲染：{{ chapter.rendererMode }}</span>
          <span v-if="chapter.finalUrl">最终地址：{{ chapter.finalUrl }}</span>
        </div>
      </div>

      <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
      <div v-else-if="!loading && contentLines.length === 0" class="empty-message">
        正文为空，请检查书源正文规则或该章节是否需要桌面浏览器渲染。
      </div>
      <div v-else class="novel-content" :style="contentStyle">
        <p v-for="(line, index) in contentLines" :key="index" :style="{ marginBottom: `${lineSpacing}px` }">{{ line }}</p>
      </div>
    </div>

    <div class="novel-footer" :style="{ backgroundColor: textColor, color: textFontColor }">
      <button :disabled="!prevChapterItem" class="chapter-btn" @click="prevChapter" :style="footerButtonStyle">上一章</button>
      <button :disabled="!nextChapterItem" class="chapter-btn" @click="nextChapter" :style="footerButtonStyle">下一章</button>
    </div>
    <div class="novel-footer" :style="{ backgroundColor: textColor, color: textFontColor }">
      <button class="chapter-btn" @click="goBackBook" :style="footerButtonStyle">返回目录</button>
      <button class="chapter-btn" @click="goHome" :style="footerButtonStyle">回到主页</button>
    </div>
  </div>
</template>

<script>
import { getSourceBookToc, getSourceChapterContent } from '@/api/bookSources.mjs';
import { splitSourceContentLines } from '@/utils/sourceContentLines.mjs';

export default {
  name: 'SourceChapterDetail',
  data() {
    return {
      loading: false,
      tocLoading: false,
      errorMessage: '',
      chapter: {},
      chapters: [],
      searchQuery: '',
      currentFontSize: Number(localStorage.getItem('currentFontSize')) || Number(localStorage.getItem('sourceReaderFontSize')) || 18,
      lineSpacing: Number(localStorage.getItem('lineSpacing')) || 50,
      lineWeight: Number(localStorage.getItem('lineWeight')) || 600,
      textColor: localStorage.getItem('textColor') || localStorage.getItem('sourceReaderBg') || 'rgba(255, 255, 255, 0.68)',
      textFontColor: localStorage.getItem('textFontColor') || localStorage.getItem('sourceReaderColor') || '#333',
      isClickablePagination: localStorage.getItem('isClickablePagination') === 'true',
      isSunMoon: localStorage.getItem('isSunMoon') === 'true',
      showGuide: false,
      showSettings: false,
      showSidebar: false,
      showChapterList: false,
      predefineColors: [
        '#ff4500',
        '#ff8c00',
        '#ffd700',
        '#90ee90',
        '#00ced1',
        '#1e90ff',
        '#c7158577',
        '#f5f5f5',
        '#f9f6e6',
        '#111111',
      ],
      themes: [
        { color: '#f5f5f5', class: 'theme-light' },
        { color: '#f9f6e6', class: 'theme-yellow' },
        { color: 'rgba(0, 0, 0, 1)', class: 'theme-black' },
      ],
    };
  },
  computed: {
    sourceId() {
      return this.$route.params.sourceId || '';
    },
    queryChapterTitle() {
      return this.$route.query.chapterTitle || '';
    },
    contentLines() {
      return splitSourceContentLines(this.chapter.content || '');
    },
    contentCharCount() {
      return (this.chapter.content || '').length;
    },
    readerStyle() {
      return {
        fontSize: `${this.currentFontSize}px`,
        color: this.textFontColor,
        backgroundColor: this.textColor,
      };
    },
    contentStyle() {
      return {
        fontWeight: `${this.lineWeight}`,
        fontSize: `${this.currentFontSize}px`,
        color: this.textFontColor,
        backgroundColor: this.textColor,
      };
    },
    footerButtonStyle() {
      return {
        backgroundColor: this.textColor,
        color: this.textFontColor,
      };
    },
    filteredChapters() {
      if (!this.searchQuery) {
        return this.chapters;
      }
      const query = this.searchQuery.toLowerCase();
      return this.chapters.filter((chapter) => {
        return String(chapter.index + 1).includes(query) || (chapter.title || '').toLowerCase().includes(query);
      });
    },
    currentChapterIndex() {
      return this.chapters.findIndex((chapter) => this.isCurrentChapter(chapter));
    },
    prevChapterItem() {
      return this.currentChapterIndex > 0 ? this.chapters[this.currentChapterIndex - 1] : null;
    },
    nextChapterItem() {
      return this.currentChapterIndex >= 0 && this.currentChapterIndex < this.chapters.length - 1
        ? this.chapters[this.currentChapterIndex + 1]
        : null;
    },
  },
  watch: {
    '$route.query.chapterUrl'() {
      this.fetchContent();
      this.loadToc();
    },
  },
  mounted() {
    this.fetchContent();
    this.loadToc();
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
          if (!this.chapter.title && this.queryChapterTitle) {
            this.chapter.title = this.queryChapterTitle;
          }
        })
        .catch((error) => {
          console.error('Source content failed:', error);
          this.errorMessage = '正文加载失败';
        })
        .finally(() => {
          this.loading = false;
        });
    },
    loadToc() {
      if (!this.sourceId || !this.$route.query.bookUrl) {
        return;
      }
      this.tocLoading = true;
      getSourceBookToc({
        sourceId: this.sourceId,
        bookKey: this.$route.query.bookKey || '',
        bookUrl: this.$route.query.bookUrl || '',
        tocUrl: this.$route.query.tocUrl || '',
      })
        .then((toc) => {
          this.chapters = toc.chapters || [];
        })
        .catch((error) => {
          console.error('Source toc failed:', error);
        })
        .finally(() => {
          this.tocLoading = false;
        });
    },
    isCurrentChapter(chapter) {
      const currentKey = this.$route.query.chapterKey || '';
      const currentUrl = this.$route.query.chapterUrl || '';
      return Boolean(
        (currentKey && chapter.chapterKey === currentKey) ||
        (currentUrl && chapter.chapterUrl === currentUrl)
      );
    },
    openChapterList() {
      this.showChapterList = true;
      if (this.chapters.length === 0 && !this.tocLoading) {
        this.loadToc();
      }
    },
    prevChapter() {
      if (this.prevChapterItem) {
        this.goToChapter(this.prevChapterItem);
      }
    },
    nextChapter() {
      if (this.nextChapterItem) {
        this.goToChapter(this.nextChapterItem);
      }
    },
    goToChapter(chapter) {
      this.showChapterList = false;
      this.showSidebar = false;
      this.$router.push({
        name: 'SourceChapterDetail',
        params: { sourceId: this.sourceId },
        query: {
          bookKey: this.$route.query.bookKey,
          bookUrl: this.$route.query.bookUrl,
          tocUrl: this.$route.query.tocUrl,
          chapterKey: chapter.chapterKey,
          chapterUrl: chapter.chapterUrl,
          chapterTitle: chapter.title,
          keyword: this.$route.query.keyword,
        },
      });
      window.scrollTo({ top: 0, behavior: 'smooth' });
    },
    handleReaderClick(event) {
      if (!this.isClickablePagination) {
        this.showSidebar = !this.showSidebar;
        return;
      }
      const screenHeight = window.innerHeight;
      const clickY = event.clientY;
      if (clickY < screenHeight / 3) {
        window.scrollBy({ top: -screenHeight / 1.2, behavior: 'smooth' });
      } else if (clickY > (screenHeight / 3) * 2) {
        window.scrollBy({ top: screenHeight / 1.2, behavior: 'smooth' });
      } else {
        this.showSidebar = !this.showSidebar;
      }
    },
    toggleReadingMode(mode) {
      this.isClickablePagination = mode;
      localStorage.setItem('isClickablePagination', mode);
    },
    changeTheme(color) {
      this.textColor = color;
      localStorage.setItem('textColor', color);
      localStorage.setItem('sourceReaderBg', color);
    },
    changeFontTheme(color) {
      this.textFontColor = color;
      localStorage.setItem('textFontColor', color);
      localStorage.setItem('sourceReaderColor', color);
    },
    sunMoon(isSM) {
      this.isSunMoon = isSM;
      localStorage.setItem('isSunMoon', this.isSunMoon);
      if (this.isSunMoon) {
        this.changeFontTheme('hsl(0deg 0% 5.49%)');
        this.changeTheme('hsl(0deg 0% 97.25%)');
      } else {
        this.changeFontTheme('hsl(0deg 0% 56.47%)');
        this.changeTheme('hsl(0deg 0% 17.25%)');
      }
    },
    decreaseFontSize() {
      this.currentFontSize = Math.max(12, this.currentFontSize - 1);
      localStorage.setItem('currentFontSize', this.currentFontSize);
      localStorage.setItem('sourceReaderFontSize', this.currentFontSize);
    },
    increaseFontSize() {
      this.currentFontSize = Math.min(224, this.currentFontSize + 1);
      localStorage.setItem('currentFontSize', this.currentFontSize);
      localStorage.setItem('sourceReaderFontSize', this.currentFontSize);
    },
    decreaseLineSpacing() {
      this.lineSpacing = Math.max(0, this.lineSpacing - 1);
      localStorage.setItem('lineSpacing', this.lineSpacing);
    },
    increaseLineSpacing() {
      this.lineSpacing = Math.min(1000, this.lineSpacing + 1);
      localStorage.setItem('lineSpacing', this.lineSpacing);
    },
    decreaseLineWeight() {
      this.lineWeight = Math.max(100, this.lineWeight - 50);
      localStorage.setItem('lineWeight', this.lineWeight);
    },
    increaseLineWeight() {
      this.lineWeight = Math.min(1000, this.lineWeight + 50);
      localStorage.setItem('lineWeight', this.lineWeight);
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
  transition: background-color 0.3s ease, color 0.3s ease;
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
  white-space: pre-wrap;
  word-break: break-word;
  overflow-wrap: anywhere;
  font-feature-settings: "kern" 0, "liga" 0, "palt" 1;
}

.novel-footer {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
}

.chapter-btn {
  padding: 10px 20px;
  border: 1px solid #ddd;
  border-radius: 30px;
  cursor: pointer;
  font-size: 14px;
  width: 45%;
  height: 40px;
}

.chapter-btn:disabled,
.sidebar-actions button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.sidebar {
  width: 60px;
  height: 100vh;
  position: fixed;
  top: 0;
  background-color: #f0f0f0;
  box-shadow: -2px 0 5px rgba(0, 0, 0, 0.1);
  z-index: 800;
  padding-top: 20px;
  transition: right 0.3s ease, left 0.3s ease;
}

.sidebar.right {
  right: -60px;
}

.sidebar.right.show {
  right: 0;
}

.sidebar.left {
  left: -60px;
}

.sidebar.left.show {
  left: 0;
}

.sidebar-actions button {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 48px;
  min-height: 68px;
  margin: 0 6px 8px;
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

.sidebar-actions i {
  font-style: normal;
  font-size: 22px;
  margin-bottom: 4px;
}

.settings-panel,
.chapter-list {
  position: fixed;
  top: 0;
  width: 350px;
  max-width: 92vw;
  height: 100vh;
  background-color: #fff;
  box-shadow: -2px 0 10px rgba(0, 0, 0, 0.1);
  z-index: 900;
  overflow-y: auto;
  transition: right 0.3s ease;
}

.settings-panel,
.chapter-list {
  right: -350px;
}

.settings-panel.show,
.chapter-list.show {
  right: 0;
}

.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
}

.drawer-header h3 {
  font-size: 18px;
}

.drawer-header button,
.modal-header button {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: inherit;
}

.settings-content,
.chapter-search {
  padding: 20px;
}

.setting-group {
  margin-bottom: 20px;
}

.setting-group label {
  display: block;
  margin-bottom: 10px;
  font-weight: bold;
}

.theme-options,
.font-size-controls,
.reading-mode {
  display: flex;
  align-items: center;
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

.font-size-controls button,
.reading-mode button {
  padding: 5px 10px;
  background-color: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
}

.reading-mode button.active {
  background-color: #1890ff;
  color: white;
  border-color: #1890ff;
}

.chapter-search input {
  width: 100%;
  padding: 8px 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
}

.chapter-body {
  height: calc(100vh - 80px);
  overflow-y: auto;
}

.chapter-body ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.chapter-body li {
  padding: 10px 15px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.chapter-body li.active {
  background-color: #f0f8ff;
  font-weight: bold;
}

.chapter-body li span {
  flex: 1;
  font-size: 13px;
}

.chapter-body li button {
  background-color: #f5f5f5;
  border: none;
  padding: 3px 8px;
  border-radius: 3px;
  font-size: 12px;
  cursor: pointer;
}

.guide-modal,
.loading-modal {
  position: fixed;
  inset: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.guide-modal {
  background-color: rgba(0, 0, 0, 0.5);
}

.modal-content,
.loading-content {
  background-color: #fff;
  color: #333;
  padding: 20px;
  border-radius: 8px;
  max-width: 520px;
  width: 90vw;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.guide-body p {
  line-height: 1.7;
  margin-bottom: 8px;
}

.loading-content {
  width: auto;
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
