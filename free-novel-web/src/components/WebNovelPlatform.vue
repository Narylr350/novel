<template>
  <div class="container">
<!--    <div class="header" @click="goTo('SyosetuNovel')" >-->
<!--      <p class="subtitle">成为小说家（syosetu）</p>-->
<!--      <p class="subtitle">日本小说</p>-->
<!--      <p class="subtitle">点击进入汉化页面</p>-->
<!--    </div>-->
    <div class="header" @click="goTo('NovelPiaNovel')" >
      <p class="subtitle">novelPia</p>
      <p class="subtitle">韩国小说</p>
      <p class="subtitle">点击进入汉化页面</p>
    </div>
    <div class="header" @click="gotoBooktoki" >
      <p class="subtitle">booktoki468</p>
      <p class="subtitle">韩国小说-booktoki468</p>
      <p class="subtitle">点击跳转到网站</p>
      <p class="subtitle">拼好书书兔插件（油猴）：https://jpg.freenovel.sbs/pinhaoshu.js  插件使用方式:进群查看</p>
    </div>
    <p class="subtitle">拼好书书兔插件（油猴）：https://jpg.freenovel.sbs/pinhaoshu.js  插件使用方式:进群查看</p>
<!--    <div class="header" @click="goTo('UploadAndShare')" >-->
<!--      <p class="subtitle">上传分享</p>-->
<!--      <p class="subtitle">可以上传汉化好的小说，也可以上传原本（系统负责汉化）</p>-->
<!--      <p class="subtitle">点击进入上传分享页面</p>-->
<!--    </div>-->
  </div>
</template>

<script>
export default {
  data() {
    return {
      searchQuery: "",
      rawHtml: "",
    };
  },
  computed: {},
  methods: {
    goTo(pageName) {
      this.$router.push({ name: pageName });
    },
    gotoBooktoki(){
      window.open('https://booktoki468.com/', '_blank');
    }
  },
  mounted() {

  },
};
</script>

<style scoped>
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding-top: 20px;
  min-height: 100vh;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.header {
  text-align: center;
  margin-bottom: 30px;
  padding: 20px 0;
  background: #ff6b6b;
  color: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.header h1 {
  font-size: 2.5rem;
  margin-bottom: 10px;
  font-weight: 700;
}

.subtitle {
  font-size: 1.2rem;
  opacity: 0.9;
}

.novels-container :deep(.searchkekka_box) {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
  padding: 25px;
  margin-bottom: 20px;
  transition: transform 0.3s, box-shadow 0.3s;
  border: 1px solid #eaeaea;
}

.novels-container :deep(.searchkekka_box:hover) {
  transform: translateY(-5px);
  box-shadow: 0 12px 20px rgba(0, 0, 0, 0.15);
}

.novels-container :deep(.novel_h) {
  margin-bottom: 15px;
}

.novels-container :deep(.novel_h a) {
  color: #2c3e50;
  text-decoration: none;
  font-size: 1.3rem;
  font-weight: 600;
  transition: color 0.3s;
}

.novels-container :deep(.novel_h a:hover) {
  color: #3498db;
}

.novels-container :deep(.author-info) {
  color: #7f8c8d;
  margin-bottom: 15px;
  font-size: 0.9rem;
}

.novels-container :deep(.author-info a) {
  color: #7f8c8d;
  text-decoration: none;
}

.novels-container :deep(.author-info a:hover) {
  text-decoration: underline;
  color: #3498db;
}

.novels-container :deep(.left) {
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
  font-size: 0.9rem;
  color: #2c3e50;
}

.novels-container :deep(.ex) {
  margin: 15px 0;
  line-height: 1.6;
  color: #34495e;
  font-size: 0.95rem;
}

.novels-container :deep(.genre-keywords) {
  margin: 15px 0;
  font-size: 0.9rem;
  color: #7f8c8d;
}

.novels-container :deep(.genre-keywords a) {
  color: #3498db;
  text-decoration: none;
  margin-right: 10px;
}

.novels-container :deep(.genre-keywords a:hover) {
  text-decoration: underline;
}

.novels-container :deep(.stats) {
  margin-top: 20px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 10px;
  font-size: 0.85rem;
  color: #7f8c8d;
}

.novels-container :deep(.stats span) {
  margin-right: 10px;
}

.novels-container :deep(.marginleft) {
  margin-left: 10px;
}

.novels-container :deep(table) {
  width: 100%;
  border-collapse: collapse;
}

.novels-container :deep(td) {
  padding: 10px;
  border: 1px solid #eaeaea;
}

:deep(a) {
  text-decoration: none;
  color: #3498db;
  font-weight: 500;
  transition: color 0.3s ease;
}

:deep(a:hover) {
  color: #2980b9;
}

:deep(a:visited) {
  color: #3498db;
}

:deep(a:active) {
  color: #1a5276;
}
</style>
