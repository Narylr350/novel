<template>
  <div :style="{ fontSize: `${currentFontSize}px`, color: `${textFontColor}`, backgroundColor: `${textColor}` }" class="app11">
    <!-- 指南弹窗 -->
    <div class="guide-modal-abcde" v-if="showGuide">
      <div class="modal-content-abcde">
        <div class="modal-header-abcde">
          <h3>指南</h3>
          <button @click="closeGuide">×</button>
        </div>
        <div class="modal-body-abcde">
          <div class="guide-section-abcde">
            <h4>1、(设置——点击翻页)点击屏幕中间，可以唤出设置，调整背景色、字体大小等</h4>
            <h4>2、(设置——点击翻页)点击屏幕下方，可以自动下滑</h4>
            <h4>3、(设置——点击翻页)点击屏幕上方，可以自动上滑</h4>
            <h4>4、如果你希望可以汉化小说的最新章节，请确保这本小说的收藏数大于0，系统将会定时汉化最新章节</h4>
            <h4>5、可以发布段评了</h4>
          </div>
        </div>
      </div>
    </div>

    <!-- 目录侧边栏 -->
    <div class="chapter-list-abcde" :class="{ show: showChapterList }" :style="{backgroundColor:textColor}">
      <div class="chapter-header-abcde">
        <h3>目录</h3>
        <button @click="closeChapterList">×</button>
      </div>

      <div class="chapter-search-abcde">
        <input type="text" placeholder="搜索章节" v-model="searchQuery">
      </div>

      <div class="chapter-body-abcde">
        <ul class="chapter-list-content-abcde">
          <li v-for="chapter in filteredChapters" :key="chapter.id"
              :class="{'active': chapter.isCurrent}">
            <span>{{ chapter.title }}</span>
            <button @click="goToChapter(chapter.id)" class="btn-read-abcde">阅读本章</button>
          </li>
        </ul>
      </div>

      <div class="chapter-footer-abcde">
      </div>
    </div>

    <!-- 加载提示 -->
    <div class="loading-modal" v-if="isLoading" :style="{ backgroundColor: textColor, color: textFontColor }">
      <div class="loading-content" :style="{ backgroundColor: textColor, color: textFontColor }">
        <div class="loading-spinner" :style="{ backgroundColor: textColor, color: textFontColor }"></div>
        <p :style="{ backgroundColor: textColor, color: textFontColor }">正在加载内容，请稍候...</p>
      </div>
    </div>

    <!-- 设置面板 -->
    <div class="settings-panel-abcde" :class="{ show: showSettings }">
      <div class="settings-header-abcde">
        <h3>设置</h3>
        <button @click="closeSettings">×</button>
      </div>

      <div class="settings-content-abcde">
        <div class="setting-group-abcde">
          <label>阅读背景</label>
          <div class="theme-options-abcde">
            <button
                v-for="(theme, index) in themes"
                :key="index"
                :class="theme.class"
                @click="changeTheme(theme.color)"></button>
            <button>
              <el-color-picker v-model="textColor" show-alpha :predefine="predefineColors" @change="changeTheme"/>
            </button>
          </div>
        </div>

        <div class="setting-group-abcde">
          <label>字体颜色</label>
          <div class="theme-options-abcde">
            <button
                v-for="(theme, index) in themes"
                :key="index"
                :class="theme.class"
                @click="changeFontTheme(theme.color)"></button>
            <button>
              <el-color-picker v-model="textFontColor" show-alpha :predefine="predefineColors" @change="changeFontTheme"/>
            </button>
          </div>
        </div>

        <div class="setting-group-abcde">
          <label>字体大小</label>
          <div class="font-size-controls-abcde">
            <button @click="decreaseFontSize">A-</button>
            <span>{{ currentFontSize }}</span>
            <button @click="increaseFontSize">A+</button>
          </div>
        </div>

        <div class="setting-group-abcde">
          <label>段距大小</label>
          <div class="font-size-controls-abcde">
            <button @click="setLineSpacingIncrease">A-</button>
            <span>{{ lineSpacing }}</span>
            <button @click="setLineSpacingAdd">A+</button>
          </div>
        </div>
        <div class="setting-group-abcde">
          <label>字体加粗</label>
          <div class="font-size-controls-abcde">
            <button @click="setLineWeightIncrease">A-</button>
            <span>{{ lineWeight }}</span>
            <button @click="setLineWeightAdd">A+</button>
          </div>
        </div>

        <div class="setting-group-abcde">
          <label>阅读方式</label>
          <div class="reading-mode-abcde">
            <button @click="toggleReadingMode(false)" :class="{ active: !isClickablePagination }">手动换页</button>
            <button @click="toggleReadingMode(true)" :class="{ active: isClickablePagination }">点击换页</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 侧边栏 -->
    <div class="sidebar-abcde" :class="{ show: showSidebar }">
      <div class="sidebar-actions-abcde">
        <button @click="openGuide">
          <i class="icon-menu-abcde">🆘</i>
          <span>帮助</span>
        </button>
        <button @click="faceMenu">
          <i class="icon-bookmark-abcde">📝</i>
          <span>目录</span>
        </button>
        <button @click="prevChapter" :disabled="!prevChapterId">
          <i class="icon-menu-abcde">⬅️</i>
          <span>上章</span>
        </button>
        <button @click="nextChapter" :disabled="!nextChapterId">
          <i class="icon-menu-abcde">➡️</i>
          <span>下章</span>
        </button>
        <button @click="faceBack">
          <i class="icon-bookmark-abcde">👎🏽</i>
          <span>翻译问题</span>
        </button>
        <button @click="goBack">
          <i class="icon-bookmark-abcde">🔙</i>
          <span>返回</span>
        </button>
      </div>
    </div>
    <div class="sidebar-abcde1" :class="{ show: showSidebar }">
      <div class="sidebar-actions-abcde">
        <button v-if="isSunMoon" @click="sunMoon(false)">
          <i class="icon-bookmark-abcde">🌜</i>
          <span>夜间模式</span>
        </button>
        <button v-else @click="sunMoon(true)">
          <i class="icon-bookmark-abcde">🌞</i>
          <span>白天模式</span>
        </button>
        <button @click="openSettings">
          <i class="icon-settings-abcde">⚙️</i>
          <span>设置</span>
        </button>
        <button v-if="!modify" @click="modifyContent">
          <i class="icon-settings-abcde">✍</i>
          <span>修改</span>
        </button>
        <button v-if="modify" @click="cancelModifyContent">
          <i class="icon-settings-abcde">✍</i>
          <span>取消修改</span>
        </button>
        <button v-if="modify" @click="saveModifyContent">
          <i class="icon-settings-abcde">✔</i>
          <span>保存</span>
        </button>
        <!-- 在侧边栏（sidebar-abcde 或 sidebar-abcde1）中任选一处，加一个按钮 -->
        <!-- <button @click="openFontDialog">
          <i class="icon-font-abcde">📝</i>
          <span>字体</span>
        </button>
        <button @click="clearAllCustomFonts">
          <i class="icon-font-abcde">📝</i>
          <span>删除字体</span>
        </button> -->
        <!-- 侧边栏只剩一个「字体」按钮 -->
        <button @click="showFontManageDialog = true">
          <i class="icon-font-abcde">📝</i>
          <span>字体</span>
        </button>
        <button @click="executeShowComments">
          <i class="icon-font-abcde">📝</i>
          <span>{{showComments?'关闭段评':'显示段评'}}</span>
        </button>
        <button  @click="openPic">
          <i class="icon-settings-abcde">🖼</i>
          <span>{{openPicTag ? '关闭' : '开启'}}插图</span>
        </button>
      </div>
    </div>

    <!-- 笔记按钮 -->
    <div class="note-button-abcde" @mousedown.prevent v-if="showNoteButton" >
      <button class="note-btn primary" @click.stop="addNote">发段评</button>
      <button class="note-btn secondary" @click.stop="openKeyReplace">修改</button>
    </div>

    <!-- 小说阅读器主体 -->
    <div class="novel-reader-abcde"
         v-if="isLoad && isFontLoaded"
         :style="{fontWeight: `${lineWeight}`,fontSize: `${currentFontSize}px`, color: `${textFontColor}`, backgroundColor: `${textColor}`}"
         @click="handleNovelReaderClick"
    >
      <div class="novel-header-abcde">
        <h2 class="chapter-title-abcde">{{ chapter.title }}</h2>
        <div class="novel-meta-abcde" style="align-items: center">
          <span style="margin: 0 0 0 10px;" class="meta-item word-count-abcde">字数：{{ chapter.content.length }}</span>
          <span style="margin: 0 0 0 10px;" class="meta-item update-time-abcde">时间：{{ formatDate(chapter.updatedAt) }}</span>

          <!-- 版本下拉 -->
          <span style="margin: 0 0 0 10px;" class="version-select-abcde" @click.stop>
            <el-select
              v-model="selectedVersionUserId"
              style="width: 100px"
              filterable
              @change="loadVersionUserId"
              popper-class="version-scroll"
            >
              <el-option
                v-for="v in allContentVersion"
                :key="v.userId"
                :label="v.username"
                :value="v.userId"
              />
            </el-select>
          </span>
        </div>
      </div>
      <div class="novel-header-abcde">
          <div class="novel-meta-abcde" style="align-items: center">
          <span style="margin: 0 0 0 10px;" class="meta-item word-count-abcde">※※选中片段，点击【发段评】可以发布段评了※※</span>
          <span style="margin: 0 0 0 10px;" class="meta-item word-count-abcde">当前章节存在{{ processedChapterData.imgLength }}张插图，如果没显示，则为网络问题</span>
        </div>
        <div class="novel-meta-abcde" style="align-items: center">
          <span v-if="allContentVersion.length > 1" style="margin: 0 0 0 10px;" class="meta-item word-count-abcde">注意：当前章节存在：{{ allContentVersion.length -1 }}条润色版本</span>
        </div>
      </div>
      <div :key="divKey" class="novel-content-abcde" id="content-container" style="text-align: left;">
        <template v-for="(item) in processedChapterData.processedLines" :key="'row-' + item.originalIndex">
          <!-- 普通文字行 -->
          <template v-if="item.type === 'line' && item.textIndex !== null">
            <!-- 原文，永不改变 -->
            <p
              :data-key="item.textIndex"
              class="novel-line"
              :style="{marginBottom: lineSpacing + 'px', fontFamily: novelFontFamily}"
            >{{ item.content }}<span @click.stop="openPost(item.textIndex)" v-if="showComments && textNumCounts.get(item.textIndex) > 0" :style="{fontWeight: `${lineWeight/1.5}`,fontSize: `${currentFontSize/1.5}px`, color: `${textFontColor}`, border: `1px solid ${textFontColor}`}" class="comment-tag">{{ textNumCounts.get(item.textIndex) }}</span></p>
            

            <!-- 全局开关为 true 时出现输入框 -->
            <textarea
              v-if="modify"
              v-model="item._edit"
              :placeholder="item.content"
              @click.stop
              rows="3"
              :style="{fontFamily: novelFontFamily}"
              style="
                display:block;
                width:100%;
                margin:-10px 0 20px 0;
                resize:vertical;
                border: 2px solid #007acc;   /* 明显边框：2 像素蓝色 */
                border-radius: 4px;          /* 圆角，看着更舒服 */
                padding: 6px 8px;            /* 内容与边框留点间距 */
                field-sizing:content;
              "
            ></textarea>
          </template>

          <!-- 以下分支保持你原来的逻辑，不动 -->
          <p
            v-else-if="item.type === 'line' && item.textIndex == null && openPicTag"
            :style="{marginBottom: lineSpacing}"
            class="image-center"
            v-html="processImageTag(item.content)"
          ></p>

          <p
            v-else-if="item.type === 'separator'"
            :class="{ 'image-center': isImageTag(item.content) }"
            style="display:none;"
          >此内容搬运自拼好书</p>
      </template>
      </div>
    </div>

    <div class="novel-footer" :style="{ backgroundColor: textColor, color: textFontColor }">
      <button :disabled="!prevChapterId" class="prev-chapter-btn-abcde" @click="prevChapter" :style="{ backgroundColor: textColor, color: textFontColor }">上一章</button>
      <button :disabled="!nextChapterId" class="next-chapter-btn-abcde" @click="nextChapter" :style="{ backgroundColor: textColor, color: textFontColor }">下一章</button>
    </div>
    <div class="novel-footer" :style="{ backgroundColor: textColor, color: textFontColor }">
      <button class="next-chapter-btn-abcde" @click="goHome" :style="{ backgroundColor: textColor, color: textFontColor }">回到主页</button>
      <button @click="showFontManageDialog = true" :style="{ backgroundColor: textColor, color: textFontColor }">
        <span>字体管理</span>
      </button>
    </div>

    <div class="novel-footer" :style="{ backgroundColor: textColor, color: textFontColor, height: this.windowHeight + 'px' }">
    </div>
    <!-- 笔记抽屉 -->
    <el-drawer
        title="我的笔记"
        v-model:modelValue="showNotesPanel"
        direction="rtl"
        size="80%"
    >
      <div class="notes-panel-abcde">
        <ul>
          <li v-for="(note, index) in currentChapterNotes" :key="index">
            <div class="note-content-abcde">
              <pre>{{ note.content }}</pre>
              <button class="delete-note-abcde" @click="deleteNote(note.id, index)">删除</button>
            </div>
          </li>
        </ul>
      </div>
    </el-drawer>

    <!-- 反馈弹窗 -->
    <el-dialog
        title="反馈问题类型"
        v-model="showFeedbackDialog"
        width="30%"
        :before-close="handleClose"
    >
      <div class="feedback-options">
        <el-button class="feedback-btn" :class="{ active: feedbackForm.type === '未翻译完' }" @click="selectFeedback('未翻译完')">未翻译完！</el-button>
        <div class="other-option">
          <el-button class="feedback-btn" :class="{ active: feedbackForm.type === '不宜阅读' }" @click="selectFeedback('不宜阅读')">章节不宜阅读</el-button>
        </div>
        <div class="other-option">
          <el-button class="feedback-btn" :class="{ active: showOtherInput }" @click="toggleOtherInput">
            {{ showOtherInput ? '其它' : '其它' }}
          </el-button>
          <el-input
              v-if="showOtherInput"
              v-model="feedbackForm.other"
              type="textarea"
              placeholder="请填写具体信息"
              :rows="3"
          ></el-input>
        </div>
      </div>
      <div class="feedback-success-message">
        在提交之后，该章节可能被删除，请保证该小说收藏量大于0，预计在一分钟内重新翻译。
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button color="#626aef" type="primary" @click="submitFeedback">提交</el-button>
        </span>
      </template>
    </el-dialog>
    <!-- 字体管理弹窗 -->
    <el-dialog
      title="字体管理"
      v-model="showFontManageDialog"
      width="420px"
      :show-close="true"
      @open="refreshFontList"
      @close="showFontManageDialog = false"
    >
      <!-- 上传 & 操作按钮区 -->
      <el-row justify="space-around" align="middle" style="margin-bottom:16px;">
        <el-col :span="7">
          <input
            ref="fontFileInput"
            type="file"
            accept=".otf,.ttf,.woff,.woff2"
            style="display:none"
            @change="handleFontFile"
          />
          <el-button
            type="primary"
            size="small"
            style="width:100%"
            @click="$refs.fontFileInput.click()"
          >
            上传字体
          </el-button>
        </el-col>

        <el-col :span="7">
          <el-button
            type="danger"
            size="small"
            plain
            style="width:100%"
            @click="deleteAllCustomFonts"
          >
            清空全部
          </el-button>
        </el-col>

        <el-col :span="7">
          <el-button
            type="info"
            size="small"
            plain
            style="width:100%"
            @click="downloadCustomFonts"
          >
            下载网站字体
          </el-button>
        </el-col>
      </el-row>

      <!-- 字体列表（保持原样） -->
      <el-scrollbar max-height="200px">
        <div v-if="fontList.length">
          <el-tag
            v-for="f in fontList"
            :key="f.key"
            :type="f.key === activeFontKey ? 'success' : ''"
            closable
            @close="deleteFontByKey(f.key)"
            style="margin:4px 4px 0 0;cursor:pointer"
            @click="applyFontByKey(f.key)"
          >
            {{ f.name }}
          </el-tag>
        </div>
        <el-empty v-else description="暂无自定义字体" :image-size="60"/>
      </el-scrollbar>
    </el-dialog>
  </div>
  <!-- 关键词替换弹框（复用 note-button-abcde 样式，位置一样） -->
  <!-- 关键词替换弹窗（竖排版） -->
  <div v-if="showKeyReplace" class="key-mask" :style="{fontFamily: novelFontFamily}">
    <div class="key-box" >
      <span>添加用户术语，可以对选中关键词进行修改，注意不能跨行修改</span>
      <div class="key-row">
        <label>将文本</label>
        <span class="key-edit" contenteditable ref="oldKeyRef">{{ oldKey }}</span>
      </div>
      <div class="key-row">
        <label>修改成</label>
        <span class="key-edit" contenteditable ref="newKeyRef">{{ newKey }}</span>
      </div>
      <div class="key-actions">
        <button class="key-btn save" @click="doReplace">保存</button>
        <button class="key-btn cancel" @click="closeKeyReplace">取消</button>
      </div>
    </div>
  </div>
  <CommentDialog v-model:visible="showDlg" :post="postInfo" @closed="onCommentClosed" />
</template>

<script>
import service from "@/api/axios";
import {ref} from 'vue';
import {ElMessage} from "element-plus";
import {
  buildContentVersions,
  normalizeNotesPayload,
  resolveChapterRequest,
} from "@/utils/chapterDetailPayloads.mjs";
import { decryptChapterContent } from "@/utils/chapterCrypto.mjs";
import CommentDialog from '@/components/CommentDialog.vue'
export default {
  name: 'ChapterDetail',
  components:{
    CommentDialog 
  },
  data() {
    return {
      postInfo:{
        chapterId:null,
        textNum:null,
        fontSize:null,
        textFontColor:null,
        textColor:null
      },
      showDlg:false,
      selectedVersionUserId:Number(localStorage.getItem("selectedVersionUserId")) || 0,
      lineSpacing: Number(localStorage.getItem("lineSpacing")) || 50,
      lineWeight: Number(localStorage.getItem("lineWeight")) || 600,
      lineSpacingDPR: Number(localStorage.getItem("lineSpacingDPR")) || 50,
      isLoading: false,
      chapter: null,
      textNumCounts:null,
      // `fontMapVersion` 不再需要，因为 IndexedDB 会处理版本控制和缓存
      prevChapterId: null,
      showFontDialog: false,      // 控制“上传字体”弹窗
      novelFontFamily: '',
      nextChapterId: null,
      showKeyReplace: false,     // 是否显示关键词替换弹框
      oldKey: '',            // 被替换词
      newKey: '',
      divKey: 0,
      windowHeight: 0,
      token: localStorage.getItem('Authorization'),
      novelId: null,
      showFontManageDialog: false,
      fontList: [],          // 存放 IndexedDB 中已上传的字体 [{key:'novelFont_123', name:'novelFont_123'}]
      activeFontKey: '',
      showGuide: false,
      showChapterList: false,
      showSettings: false,
      modify:false,
      showSidebar: false,
      showNotesPanel: ref(false),
      showNoteButton: false,
      commentsNum:null,
      showComments:localStorage.getItem('showComments') === 'true',
      showFeedbackDialog: false,
      showOtherInput: false,
      isSunMoon: localStorage.getItem('isSunMoon') === 'true',
      isLoad: false,
      isFontLoaded: false,
      fontMapVersion:Number(localStorage.getItem("fontMapVersion")) || 0,
      newfontMapVersion:null,
      chapters: [],
      searchQuery: '',
      openPicTag:localStorage.getItem('openPicTag') === 'true',
      notes: [],
      selectedText: '',
      textColor: localStorage.getItem("textColor") || 'rgba(255, 255, 255, 0.68)',
      textFontColor: localStorage.getItem("textFontColor") || '#333',
      allContentVersion:[{ userId: 0, username: '原版本' }],
      ContentVersionNum:0,
      predefineColors: [
        '#ff4500',
        '#ff8c00',
        '#ffd700',
        '#90ee90',
        '#00ced1',
        '#1e90ff',
        '#c71585',
        'rgba(255, 69, 0, 0.68)',
        'rgb(255, 120, 0)',
        'hsv(51, 100, 98)',
        'hsva(120, 40, 94, 0.5)',
        'hsl(181, 100%, 37%)',
        'hsla(209, 100%, 56%, 0.73)',
        '#c7158577',
      ],
      themes: [
        {color: '#f5f5f5', class: 'theme-light-abcde'},
        {color: '#f9f6e6', class: 'theme-yellow-abcde'},
        {color: 'rgba(0, 0, 0, 1)', class: 'theme-black--abcde'},
      ],
      fonts: [
        {name: '黑体'},
        {name: '宋体'},
        {name: '楷体'}
      ],
      currentFont: 0,
      dpr: Number(localStorage.getItem("dpr")) || window.devicePixelRatio,
      currentFontSize: Number(localStorage.getItem("currentFontSize")) || 23,
      feedbackForm: {
        type: '',
        other: ''
      },
      lastTouchTime: 0,
      isSelection: false,
      isClickablePagination: localStorage.getItem('isClickablePagination') === 'true',
      getContentData: {},
      canvases: {},
      // IndexedDB 相关变量
      db: null,
      dbName: 'NovelFontsDB',
      storeName: 'fonts',
      fontKey: 'novelFont',
      fontUrl: '/api/dic/getFontFile',
      fontName: 'novelFont',
    };
  },
  created() {
    this.fetchChapter(this.$route.params.id);
    this.getNotes();
    if (localStorage.getItem('hasSeenGuide') !== 'true') {
      this.showGuide = true;
      localStorage.setItem('hasSeenGuide', 'true');
    }
    document.addEventListener('selectionchange', this.getMinKeyInSelection);
    this.$nextTick(() => {
    const tn = Number(this.$route.query.tn)

    if (!tn) return
    // ③ 等渲染完再跳
    this.$nextTick(() => {
        const target = document.querySelector(`p[data-key="${tn}"]`)
        if (target) {
          target.scrollIntoView({ behavior: 'smooth', block: 'center' })
          // 如果想高亮 1 秒，可加下面两行
          target.style.backgroundColor = '#fffbcc'
          setTimeout(() => { target.style.backgroundColor = '' }, 1000)
        }
      })
    })
  },
  beforeUnmount() {
    document.removeEventListener('selectionchange', this.getMinKeyInSelection);
    this.canvases = {};
  },
mounted() {
  try {
    const request = indexedDB.open(this.dbName, 1);

    request.onupgradeneeded = (event) => {
      const db = event.target.result;
      if (!db.objectStoreNames.contains(this.storeName)) {
        db.createObjectStore(this.storeName);
      }
    };

    request.onsuccess = (event) => {
      this.db = event.target.result;
      const tx = this.db.transaction(this.storeName, "readwrite");
      const store = tx.objectStore(this.storeName);
      store.put("hello", "key1");
    };

    request.onerror = (event) => {
      console.error("onerror 触发:", event);
    };
  } catch (e) {
    console.error("open 调用异常:", e);
  }
  // fallback 定时器
  // setTimeout(() => {
  //   if (!this.db) {
  //     console.warn("IndexedDB 打开超时，可能被禁用，走 fallback");
  //     // this.loadAndApplyFont();
  //   }
  // }, 3000);
},
  watch: {
    '$route'(to, from) {
      if (to.params.id !== from.params.id) {
        this.fetchChapter(to.params.id);
      }
    },
    isClickablePagination(newValue) {
      localStorage.setItem('isClickablePagination', newValue);
    },
    'chapter.content': {
      handler() {
        this.clearContentContainer();
      },
      deep: true,
      immediate: false
    },
  },
  methods: {
    openPic(){
      this.openPicTag = !this.openPicTag
      localStorage.setItem("openPicTag", this.openPicTag)
    },
    executeShowComments() {
      this.showComments = !this.showComments
      localStorage.setItem("showComments", this.showComments)
    },
    async jumpToParagraph() {
      const tn = Number(this.$route.query.tn)
      if (!tn) return

      await this.$nextTick()
      let attempts = 0
      const tryScroll = () => {
        const p = document.querySelector(`p[data-key="${tn}"]`)
        if (p) {
          // ① 瞬间定位（无动画）
          p.scrollIntoView({ block: 'center', behavior: 'instant' })
          // ② 短闪高亮 0.3 秒
          p.style.backgroundColor = '#fffbcc'
          setTimeout(() => { p.style.backgroundColor = '' }, 3000)
          return
        }
        if (++attempts < 10) setTimeout(tryScroll, 50)
      }
      tryScroll()
    },
    onCommentClosed() {
      service.get("/api/chapterComment/comments/" + this.$route.params.id)
      .then(res => {
        this.textNumCounts = new Map()
        res.data.forEach(item => this.textNumCounts.set(item.textNum, item.cnt))
      })
    },
    openPost(textIndex){
      this.postInfo.chapterId = this.$route.params.id;
      this.postInfo.textNum =textIndex;
      this.postInfo.fontSize=this.currentFontSize,
      this.postInfo.textFontColor=this.textFontColor,
      this.postInfo.textColor=this.textColor
      this.showDlg = true
    },
    loadVersionUserId(){
      localStorage.setItem('selectedVersionUserId', this.selectedVersionUserId)
      const chapterRequest = resolveChapterRequest(this.selectedVersionUserId, this.allContentVersion);
      this.selectedVersionUserId = chapterRequest.versionUserId;

      if (chapterRequest.type === 'version') {
        this.getChapterByVersion(chapterRequest.versionUserId)
        return;
      }

      this.getOriginalChapter(this.$route.params.id);
    },
    async findAllContentVersion() {
      const response = await service.get(`/api/chapters/findAllContentVersion/${this.$route.params.id}`);
      const versions = Array.isArray(response.data) ? response.data : [];
      this.allContentVersion = buildContentVersions(versions);
      this.ContentVersionNum = versions.length
      // 不加 await 会返回 Promise，外面才能拿到
    },
    async deleteAllCustomFonts() {
      await this.clearAllCustomFonts();
      this.loadAndApplyFont();
      this.activeFontKey = '';
      ElMessage.success('已恢复默认字体');
      this.refreshFontList();
    },
    /* 上传完成后刷新列表 */
    async handleFontFile(e) {
      const file = e.target.files[0];
      if (!file) return;
      const buffer = await file.arrayBuffer();
      const key = this.fontName + '_' + this.newfontMapVersion;

      const open = indexedDB.open(this.dbName, 1);
      open.onsuccess = () => {
        const db = open.result;
        const tx = db.transaction(this.storeName, 'readwrite');
        const store = tx.objectStore(this.storeName);
        store.put(buffer, key);
        tx.oncomplete = () => {
          db.close();
          this.applyCustomFont(buffer);
          this.activeFontKey = key;
          ElMessage.success('字体已上传并应用');
          this.refreshFontList();   // 刷新列表
        };
      };
    },

    /* 读取 IndexedDB，生成 fontList */
    async refreshFontList() {
      const open = indexedDB.open(this.dbName, 1);
      open.onsuccess = () => {
        const db = open.result;
        const tx = db.transaction(this.storeName, 'readonly');
        const store = tx.objectStore(this.storeName);
        const list = [];
        const cursorReq = store.openCursor();
        cursorReq.onsuccess = (e) => {
          const cursor = e.target.result;
          if (cursor) {
            const key = cursor.key;
            if (typeof key === 'string' && key.startsWith(this.fontName)) {
              list.push({ key, name: key });
            }
            cursor.continue();
          } else {
            this.fontList = list;
            db.close();
          }
        };
      };
    },

    /* 点击标签切换字体 */
    async applyFontByKey(key) {
      const open = indexedDB.open(this.dbName, 1);
      open.onsuccess = () => {
        const db = open.result;
        const tx = db.transaction(this.storeName, 'readonly');
        const store = tx.objectStore(this.storeName);
        const req = store.get(key);
        req.onsuccess = () => {
          const buffer = req.result;
          if (buffer) {
            this.applyCustomFont(buffer);
            this.activeFontKey = key;
            ElMessage.success('已切换字体');
          }
          db.close();
        };
      };
    },

    /* 删除单个字体 */
    deleteFontByKey(key) {
      const open = indexedDB.open(this.dbName, 1);
      open.onsuccess = () => {
        const db = open.result;
        const tx = db.transaction(this.storeName, 'readwrite');
        const store = tx.objectStore(this.storeName);
        store.delete(key);
        tx.oncomplete = () => {
          ElMessage.success('已删除');
          if (key === this.activeFontKey) {
            this.loadAndApplyFont();          // 当前字体被删，回退默认
            this.activeFontKey = '';
          }
          this.refreshFontList();
          db.close();
        };
      };
    },
    // 1. 打开系统文件选择框
    openFontDialog() {
      const input = document.createElement('input');
      input.type = 'file';
      input.accept = '.otf,.ttf,.woff,.woff2';
      input.onchange = (e) => {
        const file = e.target.files[0];
        if (!file) return;
        this.storeFontToIndexedDB(file);   // 2. 存库
      };
      input.click();
    },

    // 2. 将文件 ArrayBuffer 存进 IndexedDB
    async storeFontToIndexedDB(file) {
      const buffer = await file.arrayBuffer();
      const key = this.fontName + '_' + this.newfontMapVersion;

      // 打开/创建数据库
      const open = indexedDB.open(this.dbName, 1);
      open.onupgradeneeded = () => {
        const db = open.result;
        if (!db.objectStoreNames.contains(this.storeName)) {
          db.createObjectStore(this.storeName);
        }
      };
      open.onsuccess = () => {
        const db = open.result;
        const tx = db.transaction(this.storeName, 'readwrite');
        const store = tx.objectStore(this.storeName);
        store.put(buffer, key);          // 存
        tx.oncomplete = () => {
          db.close();
          this.applyCustomFont(buffer);  // 3. 立即应用
        };
      };
    },

    // 3. 把 ArrayBuffer 变成 blob URL 并注入 @font-face
    applyCustomFont(buffer) {
      const key = this.fontName + '_' + this.newfontMapVersion;

      // 删除旧 <style>
      const oldStyle = document.getElementById(key);
      if (oldStyle) document.head.removeChild(oldStyle);

      // 新建 blob URL
      const blob = new Blob([buffer], { type: 'font/opentype' });
      const url = URL.createObjectURL(blob);

      // 新建 <style>
      const style = document.createElement('style');
      style.id = key;
      style.textContent = `
        @font-face {
          font-family: "${key}";
          src: url(${url}) format('opentype');
          font-weight: normal;
          font-style: normal;
        }
      `;
      document.head.appendChild(style);

      // 触发字体刷新
      this.novelFontFamily = `"${key}","PingFang SC","Microsoft YaHei","Helvetica Neue",Arial,sans-serif`;
      this.isFontLoaded = true;
    },

    // 4. 每次进入章节时，如果 IndexedDB 已有对应字体，直接应用
    async loadFontFromIndexedDB() {
      const key = this.fontName + '_' + this.newfontMapVersion;
      return new Promise((resolve) => {
        const open = indexedDB.open(this.dbName, 1);
        open.onsuccess = () => {
          const db = open.result;
          const tx = db.transaction(this.storeName, 'readonly');
          const store = tx.objectStore(this.storeName);
          const req = store.get(key);
          req.onsuccess = () => {
            
            const buffer = req.result;
            if (buffer) {
              // 用户自定义字体存在 → 直接用
              this.applyCustomFont(buffer);
            } else {
              // 用户没上传过 → 走默认字体
              this.loadAndApplyFont();
            }
            db.close();
            resolve();
          };
          req.onerror = () => {
            // 读取出错同样兜底
            this.loadAndApplyFont();
            resolve();
          };
        };
        open.onerror = () => {
          // 连库都打不开 → 兜底
          this.loadAndApplyFont();
          resolve();
        };
      });
    },
    /* 删除所有以 this.fontName 开头的字体记录 */
    async clearAllCustomFonts() {
      return new Promise((resolve) => {
        const open = indexedDB.open(this.dbName, 1);
        open.onsuccess = () => {
          const db = open.result;
          const tx = db.transaction(this.storeName, 'readwrite');
          const store = tx.objectStore(this.storeName);

          // 遍历全部 key
          const cursorReq = store.openCursor();
          cursorReq.onsuccess = (e) => {
            const cursor = e.target.result;
            if (cursor) {
              const k = cursor.key;
              if (typeof k === 'string' && k.startsWith(this.fontName)) {
                store.delete(k);   // 命中前缀就删
              }
              cursor.continue();
            } else {
              // 遍历结束
              tx.oncomplete = () => {
                db.close();
                resolve();
              };
            }
          };
          cursorReq.onerror = () => {
            db.close();
            resolve(); // 容错
          };
        };
        open.onerror = () => resolve();
      });
    },
    /* 下载默认网络字体到本地 */
    downloadCustomFonts() {
      const url = `https://jpg.freenovel.sbs/novelFont${this.newfontMapVersion}.otf`;
      const link = document.createElement('a');
      link.href = url;
      link.download = `novelFont${this.newfontMapVersion}.otf`; // 本地文件名
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    },
    // 原来的 loadAndApplyFont 把 service.get 那一段替换掉
    async loadAndApplyFont() {
      await this.clearAllCustomFonts();
      const style = document.createElement('style');
      style.id = this.fontName + "_" + this.newfontMapVersion;
      style.textContent = `
        @font-face {
          font-family: "${this.fontName + '_' + this.newfontMapVersion}";
          src: url('https://jpg.freenovel.sbs/novelFont${this.newfontMapVersion}.otf') format('opentype');
          font-weight: normal;
          font-style: normal;
        }
      `;
      // 如果已存在旧样式，先移除
      const oldStyle = document.getElementById(this.fontName + "_" + this.newfontMapVersion);
      if (oldStyle) {
        document.head.removeChild(oldStyle);
      }
      document.head.appendChild(style);
      this.novelFontFamily = this.fontName + "_" + this.newfontMapVersion + ',"PingFang SC","Microsoft YaHei","Helvetica Neue",Arial,sans-serif';
      this.isFontLoaded = true;
    },

    
    // 辅助函数：将 ArrayBuffer 转换为 Base64 字符串


    // --------------------------------------------------------------------------------
    // 其他方法（保留你原有的逻辑，只在需要时调用新方法）
    // --------------------------------------------------------------------------------
    setLineSpacingAdd() {
      if (this.lineSpacing < 1000) {
        this.lineSpacing++;
        localStorage.setItem('lineSpacing', this.lineSpacing);
      }
    },
    setLineWeightAdd() {
      if (this.lineWeight < 1000) {
        this.lineWeight= this.lineWeight + 50;
        localStorage.setItem('lineWeight', this.lineWeight);
      }
    },
    setLineSpacingIncrease() {
      if (this.lineSpacing > 0) {
        this.lineSpacing--;
        localStorage.setItem('lineSpacing', this.lineSpacing);
      }
    },
    setLineWeightIncrease() {
      if (this.lineWeight > 0) {
        this.lineWeight= this.lineWeight - 50;
        localStorage.setItem('lineWeight', this.lineWeight);
      }
    },
    showNote() {
      this.showNotesPanel = !this.showNotesPanel;
      this.getNotes();
    },
    getOriginalChapter(id) {
      this.isFontLoaded = false;
      this.isLoading = true;
      service.get(`/api/chapters/getChapterByIdApi/${id}`)
        .then(response => {
          this.chapter = response.data;
          if (this.chapter.content) {
            let item = localStorage.getItem("Authorization");
            const reversed = item.split('').reverse().join('');
            this.chapter.content = decryptChapterContent(this.chapter.content, reversed);
            this.newfontMapVersion = this.chapter.fontMapVersion
            this.loadFontFromIndexedDB();
            this.chapter.content = this.chapter.content
                .split('\n')
                .map(line => line.trim())
                .filter(line => (line !== '' && line !== '\n'))
                .join('\n');
            this.textNumCounts = new Map()
            this.chapter.textNumCounts.forEach(item => this.textNumCounts.set(item.textNum, item.cnt))
              // ② 强制让 Vue 先渲染完 DOM

              this.$nextTick().then(() => {
              this.jumpToParagraph()
            })
            
          } else {
            this.chapter.content = '';
          }
          this.prevChapterId = this.chapter.preId;
          this.nextChapterId = this.chapter.nextId;
          this.novelId = this.chapter.novelId;
          this.isLoad = true;
          this.isLoading = false;
        })
        .catch(error => {
          console.error('Error fetching chapter:', error);
          this.isLoad = true;
          this.isLoading = false;
        });
    },
    async fetchChapter(id) {
      await this.findAllContentVersion()
      const chapterRequest = resolveChapterRequest(this.selectedVersionUserId, this.allContentVersion);
      this.selectedVersionUserId = chapterRequest.versionUserId

      if (chapterRequest.type === 'version') {
        this.getChapterByVersion(chapterRequest.versionUserId)
        return;
      }

      this.getOriginalChapter(id);
    },
    openKeyReplace(){
      this.oldKey = window.getSelection().toString()
      this.showNoteButton = false;
      this.showKeyReplace = true;
      this.$nextTick(()=>this.$refs.oldKeyRef.focus());
    },
    closeKeyReplace(){
      this.showKeyReplace = false;
    },
    doReplace(){
      const oldK = this.$refs.oldKeyRef.textContent.trim();
      const newK = this.$refs.newKeyRef.textContent.trim();
      if(!oldK) return;
      const re = new RegExp(oldK.replace(/[.*+?^${}()|[\]\\]/g,'\\$&'),'g');
      this.chapter.content = this.chapter.content.replace(re,newK);
      this.divKey++;
      service.post("/api/glossary/batchAddFromChapter/" + this.chapter.novelId,{
        title: oldK,
        content: newK,
      }).then(()=>{
          this.closeKeyReplace();
      })

    },
    getChapterByVersion(versionId) {
      this.isFontLoaded = false;
      this.isLoading = true;
    
      
      service.get(`/api/chapters/getChapterByVersion/${this.$route.params.id}/${versionId}`)
          .then(response => {
            this.chapter = response.data;
            if (this.chapter.content) {
              let item = localStorage.getItem("Authorization");
              const reversed = item.split('').reverse().join('');
              this.chapter.content = decryptChapterContent(this.chapter.content, reversed);
              this.newfontMapVersion = this.chapter.fontMapVersion
              this.loadFontFromIndexedDB();
              this.chapter.content = this.chapter.content
                  .split('\n')
                  .map(line => line.trim())
                  .filter(line => (line !== '' && line !== '\n'))
                  .join('\n');
              this.textNumCounts = new Map()
              this.chapter.textNumCounts.forEach(item => this.textNumCounts.set(item.textNum, item.cnt))
              this.$nextTick().then(() => {
                this.jumpToParagraph()
              })
            } else {
              this.chapter.content = '';
            }
            this.prevChapterId = this.chapter.preId;
            this.nextChapterId = this.chapter.nextId;
            this.novelId = this.chapter.novelId;
            this.findAllContentVersion()
            this.isLoad = true;
            this.isLoading = false;
          })
          .catch(error => {
            console.error('Error fetching chapter:', error);
            this.isLoad = true;
            this.isLoading = false;
          });
    },
    reRenderCanvas() {
      this.clearContentContainer();
    },
    async renderChapterContent() {
    },
    formatDate(dateString) {
      const date = new Date(dateString);
      const year = date.getFullYear();
      const month = date.getMonth() + 1;
      const day = date.getDate();
      return `${year}-${month}-${day}`;
    },
    goBack() {
      this.$router.push({name: 'NovelDetail', params: {id: this.novelId}});
    },
    openGuide() {
      this.showGuide = true;
    },
    closeGuide() {
      this.showGuide = false;
    },
    closeChapterList() {
      this.showChapterList = false;
    },
    openSettings() {
      this.showSettings = true;
    },
    cancelModifyContent(){
      this.modify = false
    },
    modifyContent() {
      this.modify = true
      // 第一次打开时，给每行初始化“编辑副本”
      this.processedChapterData.processedLines.forEach(item => {
        if (item.textIndex !== null && item._edit === undefined) {
          item._edit = item.content
        }
      })
      this.showSidebar = false
    },
    saveModifyContent(){
      const text = this.processedChapterData.processedLines
      .filter(item => item.textIndex !== null && item._edit !== undefined)
      .map(item => item._edit)
      .join('\n');
      const newNote = {
        novelId: this.novelId,
        chapterId: this.$route.params.id,
        content: text
      };
      service.post('/api/chapters/saveModifyContent', newNote)
          .then(() => {
            ElMessage.success('已保存');
          })
          .catch(error => {
            console.error('Failed to add note:', error);
            this.$message.error('Failed to add note');
          })
          .finally(() => {
            this.modify = false;
          })
    },
    faceBack() {
      this.showFeedbackDialog = true;
    },
    faceMenu() {
      this.showChapterList = true;
      service.get(`/api/chapters/getChaptersByNovelId/${this.novelId}`)
          .then(response => {
            this.chapters = [...response.data];
          })
          .catch(error => console.error('Error fetching chapters:', error));
    },
    sunMoon(isSM) {
      this.isSunMoon = isSM;
      localStorage.setItem('isSunMoon', this.isSunMoon);
      if (this.isSunMoon) {
        const textC = 'hsl(0deg 0% 5.49%)';
        const bgC = 'hsl(0deg 0% 97.25%)';
        this.textFontColor = textC;
        localStorage.setItem('textFontColor', textC);
        this.textColor = bgC;
        localStorage.setItem('textColor', bgC);
      } else {
        const textC = 'hsl(0deg 0% 56.47%)';
        const bgC = 'hsl(0deg 0% 17.25%)';
        this.textFontColor = textC;
        localStorage.setItem('textFontColor', textC);
        this.textColor = bgC;
        localStorage.setItem('textColor', bgC);
      }
    },
    closeSettings() {
      this.showSettings = false;
    },
    prevChapter() {
      this.showSidebar = false
      this.selectedVersionUserId=Number(localStorage.getItem("selectedVersionUserId")) || 0
      if (this.prevChapterId) {
        this.clearContentContainer();
        this.$router.push({name: 'ChapterDetail', params: {id: this.prevChapterId}});
      }
    },
    nextChapter() {
      this.showSidebar = false
      this.selectedVersionUserId=Number(localStorage.getItem("selectedVersionUserId")) || 0
      if (this.nextChapterId) {
        this.clearContentContainer();
        this.$router.push({name: 'ChapterDetail', params: {id: this.nextChapterId}});
      }
    },
    goToChapter(chapterId) {
      this.selectedVersionUserId=Number(localStorage.getItem("selectedVersionUserId")) || 0
      this.showChapterList = false;
      this.showSidebar = false;
      this.clearContentContainer();
      this.$router.push({name: 'ChapterDetail', params: {id: chapterId}});
    },
    goHome() {
      this.$router.push({name: 'RecommendationList'});
    },
    changeTheme(color) {
      localStorage.setItem('textColor', color);
      this.textColor = color;
    },
    changeFontTheme(color) {
      this.textFontColor = color;
      localStorage.setItem('textFontColor', color);
    },
    increaseFontSize() {
      if (this.currentFontSize < 224) {
        this.currentFontSize++;
        localStorage.setItem('currentFontSize', this.currentFontSize);
      }
    },
    decreaseFontSize() {
      if (this.currentFontSize > 12) {
        this.currentFontSize--;
        localStorage.setItem('currentFontSize', this.currentFontSize);
      }
    },
    handleMouseClick1() {
      if (this.getMinKeyInSelection() > 0) {
        return
      }
      this.showSidebar = !this.showSidebar;
    },
    handleMouseClick(event) {
      const screenHeight = window.innerHeight;
      const upperThreshold = screenHeight / 3;
      const lowerThreshold = (screenHeight / 3) * 2;
      const novelReader = document.querySelector('.novel-reader-abcde');
      if (novelReader && novelReader.contains(event.target)) {
        const clickY = event.clientY;
        if (clickY < upperThreshold) {
          window.scrollBy({
            top: -screenHeight / 1.2,
            behavior: 'smooth'
          });
        } else if (clickY > lowerThreshold) {
          window.scrollBy({
            top: screenHeight / 1.2,
            behavior: 'smooth'
          });
        } else {
          this.showSidebar = !this.showSidebar;
        }
      }
    },
    getNotes() {
      if (!this.token || this.token === 'undefined') {
        ElMessage.warning("You are not logged in");
        return;
      }
      let newVar = this.$route.params.id;
      service.get(`/api/notes/chapter/${newVar}`)
          .then(response => {
            this.notes = normalizeNotesPayload(response.data).map(item => ({
              ...item,
              isSelected: false
            }));
          })
          .catch(error => console.error('Error getNotes:', error));
    },
    addNote() {
      this.openPost(this.commentsNum)
      this.showNoteButton = false;
    },
    saveNewTerm() {
      service.post("/api/glossary/batch/" + this.$route.params.id,{
        title: this.newSourceName,
        content: this.newTargetName,
      }).then(res => {
        const newTerm = res.data;
        if (newTerm.statue == 100) {
          ElMessage.warning("该术语已存在，请勿重新提交");
        } else{
          ElMessage.success("新术语已添加");
        }
        this.showNoteButton = false;
      })
    },
    deleteNote(id, index) {
      if (!this.token || this.token === 'undefined') {
        ElMessage.warning("You are not logged in");
        return;
      }
      this.notes.splice(index, 1);
      service.post('/api/notes/delete', {ids: [id]}, {
        headers: {'Content-Type': 'application/json'}
      })
          .then(() => {
            this.$message.success('Note deleted successfully');
          })
          .catch(error => {
            console.error('Deletion failed:', error);
            this.$message.error('Failed to delete note');
          });
    },
    isImageTag(line) {
      return line.includes('<img');
    },
    processImageTag(line) {
      const match = line.match(/<img[^>]*src="([^"]+)"[^>]*style="([^"]+)"[^>]*>/);
      if (match) {
        const src = match[1];
        const style = match[2];
        return `<img src="${src}" style="${style} max-width: 100%; height: auto; vertical-align: middle;">`;
      }
      return line;
    },
    closeFeedbackDialog() {
      this.showFeedbackDialog = false;
      this.feedbackForm = {
        type: '',
        other: ''
      };
      this.showOtherInput = false;
    },
    selectFeedback(type) {
      this.feedbackForm.type = type;
      this.feedbackForm.other = type;
      this.showOtherInput = false;
    },
    toggleOtherInput() {
      this.showOtherInput = !this.showOtherInput;
      if (this.showOtherInput) {
        this.feedbackForm.type = 'Other';
      } else {
        this.feedbackForm.type = '';
        this.feedbackForm.other = '';
      }
    },
    submitFeedback() {
      if (!this.token || this.token === 'undefined') {
        ElMessage.warning("You are not logged in");
        return;
      }
      if (!this.feedbackForm.type && !this.feedbackForm.other) {
        this.$message.error('Please select feedback type or fill in specific information');
        return;
      }
      if (this.feedbackForm.type === 'Other' && !this.feedbackForm.other) {
        this.$message.error('Please fill in specific information');
        return;
      }
      const feedbackData = {
        novelId: this.novelId,
        chapterId: this.$route.params.id,
        content: this.feedbackForm.other
      };
      service.post('/api/feedback/add', feedbackData)
          .then(response => {
            this.$message.success(response.data);
            this.closeFeedbackDialog();
          })
          .catch(error => {
            console.error('Failed to submit feedback:', error);
            this.$message.error('Failed to submit feedback');
          });
    },
    handleClose() {
      this.closeFeedbackDialog();
    },
    getMinKeyInSelection() {
      if (!window.getSelection().toString()) {
        this.showNoteButton = false
        return 0;
      }
      const sel = window.getSelection();
      if (!sel.rangeCount) {
        this.showNoteButton = false
        return 0;
      }
      

      const keySet = new Set();

      for (let i = 0; i < sel.rangeCount; i++) {
        const range = sel.getRangeAt(i);

        // 1. 先拿起点、终点所在节点
        let startEl = range.startContainer.nodeType === 1
                      ? range.startContainer
                      : range.startContainer.parentElement;
        let endEl   = range.endContainer.nodeType === 1
                      ? range.endContainer
                      : range.endContainer.parentElement;

        // 2. 向上找到 p.novel-line
        while (startEl && !startEl.classList?.contains('novel-line')) startEl = startEl.parentElement;
        while (endEl   && !endEl.classList?.contains('novel-line'))   endEl   = endEl.parentElement;

        // 3. 把这两行（可能同一行）收进来
        [startEl, endEl].forEach(el => {
          if (el?.dataset.key) keySet.add(el.dataset.key);
        });
      }

      if (!keySet.size) {
        this.showNoteButton = false
        return 0;
      }
      
      this.showNoteButton = true
      // 4. 取 key 数值最小者
      this.commentsNum =  [...keySet].reduce((min, k) =>
        k < min ? k : min
      );
      return this.commentsNum
    },
    checkSelection() {
      // console.log(window.getSelection());
      
      // const selectedText = window.getSelection().toString();
      // if (selectedText.trim() !== '') {
      //   this.selectedText = selectedText;
      //   this.showNoteButton = true;
      // } else {
      //   this.showNoteButton = false;
      // }
    },
    handleNovelReaderClick(event) {
      if (this.isClickablePagination) {
        this.handleMouseClick(event);
      } else {
        this.handleMouseClick1();
      }
    },
    toggleReadingMode(mode) {
      this.isClickablePagination = mode;
      localStorage.setItem("isClickablePagination", mode);
    },
    clearContentContainer() {
      this.divKey++;
      this.canvases = {};
    },
  },
  computed: {
processedChapterData() {
  const lines = this.chapter.content.split('\n');
  const processed = [];
  let textIndex = 1;
  let imgLength = 0;

  for (let i = 0; i < lines.length; i++) {
    const content = lines[i];
    const isImg = this.isImageTag(content);
    
    if (isImg) {
      imgLength++;
    }
    
    processed.push({
      type: 'line',
      content,
      originalIndex: i,
      isImg,
      textIndex: isImg ? null : textIndex++
    });
  }
  
  return {
    processedLines: processed,
    imgLength: imgLength
  };
},
    filteredChapters() {
      if (!this.searchQuery) {
        return this.chapters;
      }
      const query = this.searchQuery.toLowerCase();
      return this.chapters.filter(chapter => {
        return String(chapter.chapterNumber).includes(query);
      });
    },
    currentChapterNotes() {
      return this.notes.filter(note => note.chapterId === this.chapter.id);
    }
  }
};
</script>


<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  margin: 0;
  padding: 0;
  overflow-x: hidden;
  transition: background-color 0.3s ease, color 0.3s ease;
}

.app11 {
  position: relative;
  min-height: 100vh;
}
.novel-text p {
  margin-bottom: 15px;
  text-indent: 2em;
  line-height: 1.8;
}

.novel-footer button {
  padding: 8px 20px;
  margin: 0 10px;
  background-color: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  height: 100px;
}

.end-of-chapter button {
  padding: 8px 20px;
  background-color: #f0f0f0;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.end-of-chapter button:hover {
  background-color: #e0e0e0;
}

/* 侧边栏样式 */
.sidebar-abcde {
  width: 60px;
  height: 100vh;
  position: fixed;
  right: -60px;
  top: 0;
  background-color: #f0f0f0;
  box-shadow: -2px 0 5px rgba(0, 0, 0, 0.1);
  z-index: 800;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 20px;
  transition: right 0.3s ease;
}

.sidebar-abcde.show {
  right: 0;
}

.sidebar-abcde1 {
  width: 60px;
  height: 100vh;
  position: fixed;
  left: -60px;
  top: 0;
  background-color: #f0f0f0;
  box-shadow: -2px 0 5px rgba(0, 0, 0, 0.1);
  z-index: 800;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 20px;
  transition: left 0.3s ease;
}

.sidebar-abcde1.show {
  left: 0;
}

.sidebar-actions-abcde button {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 40px;
  height: 80px;
  margin-bottom: 5px;
  background: none;
  border: none;
  cursor: pointer;
  padding: 5px;
  border-radius: 4px;
}

.sidebar-actions-abcde button:hover {
  background-color: #e0e0e0;
}

.sidebar-actions-abcde i {
  font-size: 24px;
  margin-bottom: 5px;
}

.sidebar-actions-abcde span {
  font-size: 12px;
  color: #666;
}

/* 笔记按钮样式 */
.note-button-abcde{
  position: fixed;
  top: 20px; left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 12px;
  z-index: 1000;
}
.note-btn{
  padding: 8px 18px;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: all .25s ease;
  box-shadow: 0 2px 8px rgba(0,0,0,.12);
}

/* 主按钮 */
.note-btn.primary{
  background-color: rgb(55, 57, 68);
  border: 1px solid currentColor;
  color: #fff;
}
.note-btn.primary:hover{
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102,126,234,.35);
}

/* 次按钮 */
.note-btn.secondary{
  background-color: rgb(55, 57, 68);
  color: #fff;          /* 自动随父级字体颜色 */
  border: 1px solid currentColor;
}
.note-btn.secondary:hover{
  background: rgba(0,0,0,.05);
}

/* 笔记抽屉样式 */
.el-drawer {
  overflow: auto;
}

.notes-panel-abcde {
  padding: 20px;
}

.notes-panel-abcde h3 {
  margin-bottom: 15px;
  font-size: 18px;
}

.notes-panel-abcde ul {
  list-style: none;
  padding: 0;
}

.notes-panel-abcde li {
  padding: 10px 0;
  border-bottom: 5px solid #eee;
}

.notes-panel-abcde pre {
  white-space: pre-wrap;
  margin: 0;
}

.note-content-abcde {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.delete-note-abcde {
  background-color: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 4px 8px;
  cursor: pointer;
  margin-left: 10px;
}

.delete-note-abcde:hover {
  background-color: #e0e0e0;
}

/* 指南弹窗样式 */
.guide-modal-abcde {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content-abcde {
  background-color: #fff;
  border-radius: 8px;
  width: 500px;
  max-height: 80%;
  overflow-y: auto;
  padding: 20px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
}

.modal-header-abcde {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.modal-header-abcde h3 {
  font-size: 18px;
  font-weight: bold;
}

.modal-header-abcde button {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
}

.guide-section-abcde {
  margin-bottom: 20px;
}

.guide-section-abcde h4 {
  font-size: 14px;
  margin-bottom: 10px;
  color: #666;
}

.key-btn {
  background-color: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 8px 12px;
  margin: 0 5px 5px 0;
  cursor: pointer;
  font-size: 14px;
}

.arrow-buttons {
  display: flex;
}

.arrow-buttons.vertical {
  flex-direction: column;
}

.bookmark-tabs {
  display: flex;
  border-bottom: 1px solid #ddd;
  margin-bottom: 10px;
}

.bookmark-tabs span {
  padding: 5px 10px;
  cursor: pointer;
}

.bookmark-tabs span.active {
  color: #f44;
  border-bottom: 2px solid #f44;
}

.bookmark-content {
  padding: 10px 0;
  border: 1px solid #eee;
  border-radius: 4px;
}

.bookmark-item {
  margin-bottom: 10px;
}

.bookmark-progress {
  height: 10px;
  background-color: #eee;
  border-radius: 5px;
  margin-bottom: 5px;
}

.bookmark-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
}

.bookmark-note {
  font-size: 12px;
  color: #999;
  margin-top: 10px;
}

.comment-buttons {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-gap: 5px;
}

/* 目录组件样式 */
.chapter-list-abcde {
  position: fixed;
  top: 0;
  right: -350px;
  width: 350px;
  height: 100%;
  background-color: #fff;
  box-shadow: -2px 0 10px rgba(0, 0, 0, 0.1);
  z-index: 900;
  overflow-y: auto;
  transition: right 0.3s ease;
}

.chapter-list-abcde.show {
  right: 0;
}

.chapter-header-abcde {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
}

.chapter-header-abcde h3 {
  font-size: 18px;
}

.chapter-header-abcde button {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
}

.chapter-search-abcde {
  padding: 15px;
}

.chapter-search-abcde input {
  width: 100%;
  padding: 8px 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.chapter-body-abcde {
  padding: 10px 0;
  height: calc(100% - 150px);
  overflow-y: auto;
}

.chapter-list-content-abcde {
  list-style: none;
  padding: 0;
  margin: 0;
}

.chapter-list-content-abcde li {
  padding: 10px 15px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chapter-list-content-abcde li:hover {
  background-color: #f9f9f9;
}

.chapter-list-content-abcde li.active {
  background-color: #f0f8ff;
  font-weight: bold;
}

.chapter-list-content-abcde .btn-read-abcde {
  background-color: #f5f5f5;
  border: none;
  padding: 3px 8px;
  border-radius: 3px;
  font-size: 12px;
  cursor: pointer;
}

.chapter-footer-abcde {
  padding: 15px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: space-between;
}

.chapter-footer-abcde button {
  padding: 6px 12px;
  background-color: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
}

/* 设置面板样式 */
.settings-panel-abcde {
  position: fixed;
  top: 0;
  right: -350px;
  width: 350px;
  height: 80%;
  background-color: #fff;
  box-shadow: -2px 0 10px rgba(0, 0, 0, 0.1);
  z-index: 900;
  overflow-y: auto;
  transition: right 0.3s ease;
}

.settings-panel-abcde.show {
  right: 0;
}

.settings-header-abcde {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
}

.settings-header-abcde h3 {
  font-size: 18px;
}

.settings-header-abcde button {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
}

.settings-content-abcde {
  padding: 20px;
}

.setting-group-abcde {
  margin-bottom: 20px;
}

.setting-group-abcde label {
  display: block;
  margin-bottom: 10px;
  font-weight: bold;
}

.theme-options-abcde {
  display: flex;
  justify-content: space-between;
}

.theme-options-abcde button {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 1px solid #ddd;
  cursor: pointer;
  margin-right: 5px;
}

.font-options-abcde, .width-options, .mode-options {
  display: flex;
  flex-wrap: wrap;
}

.font-options-abcde button, .width-options button, .mode-options button {
  padding: 5px 10px;
  margin-right: 10px;
  margin-bottom: 5px;
  background-color: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
}
.theme-black--abcde {
  background-color: rgba(0, 0, 0, 1);
}
.theme-yellow-abcde {
  background-color: #f9f6e6;
}
.theme-light-abcde {
  background-color: #f5f5f5;
}
.font-options-abcde button.active, .width-options button.active, .mode-options button.active {
  background-color: #f0f8ff;
  border-color: #1890ff;
  color: #1890ff;
}

.font-size-controls-abcde {
  display: flex;
  align-items: center;
}

.font-size-controls-abcde button {
  padding: 5px 10px;
  margin: 0 5px;
  background-color: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
}

.font-size-controls-abcde span {
  margin: 0 10px;
}

.toggle-switch {
  display: flex;
  align-items: center;
}

.switch {
  position: relative;
  display: inline-block;
  width: 60px;
  height: 34px;
  margin-right: 10px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: .4s;
}

.slider:before {
  position: absolute;
  content: "";
  height: 26px;
  width: 26px;
  left: 4px;
  bottom: 4px;
  background-color: white;
  transition: .4s;
}

input:checked + .slider {
  background-color: #2196F3;
}

input:checked + .slider:before {
  transform: translateX(26px);
}

.slider.round {
  border-radius: 34px;
}

.slider.round:before {
  border-radius: 50%;
}

/* 小说阅读器样式 */
.novel-reader-abcde {
  max-width: 800px;
  margin: 0 auto;
}

.novel-header-abcde {
  margin-bottom: 20px;
}

.chapter-title-abcde {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 10px;
}

.novel-meta-abcde {
  display: flex;
  justify-content: center;
  color: #999;
  font-size: 14px;
}

.novel-content-abcde {
  line-height: 1.8;
  text-align: justify;
  max-width: 100%;
  overflow-wrap: break-word;
  word-break: break-word;
  margin: 0 10px 0 10px;
}
.image-center {
  text-indent: 0 !important;
  text-align: center !important;
  margin: 20px 0;
}

.image-center img {
  vertical-align: middle;
  max-width: 100%;
  height: auto;
}
.novel-content-abcde p {
  text-indent: 2em;
  max-width: 100%;
  overflow-wrap: break-word;
  word-break: break-word;
}

.novel-footer {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
}

.prev-chapter-btn-abcde, .next-chapter-btn-abcde {
  padding: 10px 20px;
  border: none;
  border-radius: 30px;
  cursor: pointer;
  font-size: 14px;
  width: 45%;
  height: 40px;
}

.prev-chapter-btn-abcde {
  background-color: #f5f5f5;
}

/* 反馈弹窗样式 */
.feedback-options {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.other-option {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.feedback-btn {
  width: 100%;
  padding: 10px 15px;
  text-align: left;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: #fff;
  color: #606266;
}

.feedback-btn.active {
  background-color: #409eff;
  color: white;
  border-color: #409eff;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .sidebar-abcde {
    width: 50px;
  }

  .sidebar-abcde1 {
    width: 50px;
  }

  .sidebar-actions-abcde i {
    font-size: 20px;
  }

  .el-dialog {
    width: 90% !important;
  }

  .dialog-footer {
    flex-direction: column;
    align-items: flex-end;
  }

  .dialog-footer .el-button {
    width: 100%;
    margin-bottom: 10px;
  }
}
.feedback-success-message {
  margin-top: 20px;
  padding: 15px;
  background-color: #f1de7c;
  color: #c23a3a;
  border-radius: 4px;
  text-align: center;
}
.reading-mode-abcde {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.reading-mode-abcde button {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background-color: #f5f5f5;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
}

.reading-mode-abcde button:hover {
  background-color: #e0e0e0;
}

.reading-mode-abcde button.active {
  background-color: #1890ff;
  color: white;
  border-color: #1890ff;
}

/* 为按钮添加一些额外的样式以提高可读性和视觉效果 */
.reading-mode-abcde button {
  font-weight: 500;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.reading-mode-abcde button.active {
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
  transform: translateY(-2px);
}

/* 为按钮添加焦点样式，以提高无障碍支持 */
.reading-mode-abcde button:focus {
  outline: none;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.5);
}

.loading-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000; /* 确保加载提示在最上层 */
}

.loading-content {
  background-color: white;
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

@keyframes spin {
  to { transform: rotate(360deg); }
}

.novel-line {
  word-break: keep-all;   /* 不要随意在单词里断开 */
  overflow-wrap: normal;  /* 禁止软换行 */
  white-space: pre-wrap;  /* 保留空格和换行符，但允许自动换行 */
  font-feature-settings: "kern" 0, "liga" 0, "palt" 1;

}
/* 仅对版本下拉生效 */
.version-scroll .el-select-dropdown__wrap {
  max-height: 200px;   /* 超出即出滑轮 */
  overflow-y: auto;
  white-space: normal;
  max-width: 260px;
}
.comment-tag {
  margin-left: 6px;
  padding: 0 4px;
  font-size: 12px;
  border-radius: 3px;
  vertical-align: super;
  cursor: pointer;
}
/* 关键词替换弹窗 */
/* 关键词替换弹窗 */
.key-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, .45);
  display: flex;
  align-items: flex-start;   /* 1. 从垂直居中改为顶部对齐 */
  justify-content: center;
  z-index: 2500;
}

.key-box {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 360px;
  max-width: 90vw;
  background: #fff;
  border-radius: 16px;
  padding: 20px 24px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, .18);
  margin-top: 60px;          /* 2. 距离顶部 60px（可自行调整） */
}
.key-row{display:flex;align-items:center;gap:8px}
.key-row label{width:52px;text-align:right;font-size:15px;color:#555}
.key-edit{flex:1;padding:6px 10px;border:1px solid #bbb;border-radius:6px;background:#fafafa;outline:none;font-size:15px;word-break:break-all}
.key-actions{display:flex;gap:12px;justify-content:center;margin-top:4px}
.key-btn{padding:6px 18px;border:none;border-radius:20px;font-size:14px;cursor:pointer}
.key-btn.save{background:#667eea;color:#fff}
.key-btn.cancel{background:#f5f5f5}
</style>
