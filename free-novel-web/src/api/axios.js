import axios from 'axios';
import { buildAuthorizationHeader } from './requestAuth.mjs';

// 安全的消息显示函数，延迟加载 ElMessage 避免初始化问题
const showError = (msg) => {
    setTimeout(() => {
        import('element-plus').then(({ ElMessage }) => {
            ElMessage.error(msg);
        }).catch(() => {
            console.error('Message error:', msg);
        });
    }, 0);
};

// 创建 axios 实例
const service = axios.create({
    baseURL: process.env.VUE_APP_API_BASE_URL || '',
    timeout: 300000, // 请求超时时间
});

// npm run build --no-source-map --modern

export function containsChinese(str) {
    return /[\u4e00-\u9fa5]/.test(str); // 匹配所有中文Unicode字符
}

// 使用XPath获取标题文本
export function getTitleText() {
    try {
        const result = document.evaluate(
            '//*[@id="app"]/div/header/div/div[1]/span',
            document,
            null,
            XPathResult.FIRST_ORDERED_NODE_TYPE,
            null
        );
        return result.singleNodeValue?.textContent || '';
    } catch (error) {
        return '';
    }
}
export function getTitleText1() {
    try {
        const result = document.evaluate(
            '//*[@id="app"]/div/main/div/div[4]/div[1]/div/span[1]',
            document,
            null,
            XPathResult.FIRST_ORDERED_NODE_TYPE,
            null
        );
        return result.singleNodeValue?.textContent || '';
    } catch (error) {
        return '';
    }
}
export function getTitleText2() {
    try {
        const result = document.evaluate(
            '//*[@id="app"]/div/main/div/nav/div/a[1]',
            document,
            null,
            XPathResult.FIRST_ORDERED_NODE_TYPE,
            null
        );
        return result.singleNodeValue?.textContent || '';
    } catch (error) {
        return '';
    }
}


service.interceptors.request.use((config) => {
    const titleText = getTitleText();
    const titleText1 = getTitleText1();
    const titleText2 = getTitleText2();
    let newVar = titleText1 + titleText + titleText2;
    if (newVar.length > 0 && !containsChinese(newVar)) {
        showError("system error");
        return Promise.reject(new Error('system error'));
    }
    if (config.url.includes('/api/auth/') && !config.url.includes('/api/auth/isLogin')) {
        localStorage.removeItem('Authorization');
    }
    const token = localStorage.getItem('Authorization');
    const authorizationHeader = buildAuthorizationHeader(config, token);
    if (authorizationHeader) {
        config.headers.Authorization = authorizationHeader;
    }
    return config;
});

// 响应拦截器
service.interceptors.response.use(
    (response) => {
        // 如果响应码为 401，清空 Authorization 并提示用户
        if (response.status === 401) {
            localStorage.removeItem('Authorization');
        } else if (response.status === 502) {
            showError('请勿频繁刷新页面');
        }
        return response;
    },
    (error) => {
        // 处理响应错误
        if (error.response && error.response.status === 401) {
            localStorage.removeItem('Authorization');
        }
        else if (error.response) {
            const errorMsg = error.response.data || error.response.statusText || '请求失败';
            if (errorMsg && typeof errorMsg === 'string') {
                showError(errorMsg);
            } else if (errorMsg && typeof errorMsg === 'object') {
                showError(JSON.stringify(errorMsg));
            } else {
                showError('请求失败');
            }
        } else {
            showError('请求出错，请稍后重试');
        }
        return Promise.reject(error);
    }
);

export default service;
