import { createApp } from 'vue'
import App from './App.vue'
import './styles/tailwind.css';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import router from './router';
import { getAppMode, loadAppMode } from './config/appMode.mjs';

async function bootstrap() {
  await loadAppMode();

  const app = createApp(App);
  const appMode = getAppMode();

  app.provide('appMode', appMode);
  app.config.globalProperties.$appMode = appMode;
  app.use(ElementPlus);
  app.use(router);
  app.mount('#app');
}

bootstrap();
