import Vue from 'vue'
import App from './App.vue'
import router from './router'
import ElementUI from 'element-ui';             // 1. 引入 ElementUI
import 'element-ui/lib/theme-chalk/index.css';  // 2. 引入 ElementUI 样式
import axios from 'axios';                      // 3. 引入 Axios

// 4. 配置 Axios 默认后端地址
const baseUrl = process.env.VUE_APP_BASE_URL || 'http://localhost:9090';
axios.defaults.baseURL = baseUrl;
Vue.prototype.$baseUrl = baseUrl;

// 5. 将 Axios 挂载到 Vue 原型上，这样在页面里就能用 this.$http 访问了
Vue.prototype.$http = axios;

// --- 新增：Axios 拦截器配置 ---
// 请求拦截器：每次请求带上 Token
axios.interceptors.request.use(config => {
    // 优先从 sessionStorage 获取 JWT Token
    let token = sessionStorage.getItem("token");

    // 如果没有 Token，但有 User 信息 (兼容旧逻辑)，尝试获取 userId
    if (!token) {
        let userJson = sessionStorage.getItem("user");
        if (userJson) {
            let user = JSON.parse(userJson);
            // 这里仅仅是兼容，实际上后端 AuthInterceptor 已经强制校验 JWT
            // 如果后端开启了严格模式，这里传 userId 会导致 401，从而触发重新登录
            // 建议：如果只有 user 没有 token，直接视作未登录
        }
    }

    if (token) {
        config.headers['token'] = token;
    }
    
    return config;
}, error => {
    return Promise.reject(error);
});

// 响应拦截器：统一处理 401 禁用状态
let isLockingOut = false; // 全局锁，防止重复弹窗

axios.interceptors.response.use(response => {
    return response;
}, error => {
    // 1. 处理 401 禁用/未授权
    if (error.response && error.response.status === 401) {
        // 如果已经在处理下线逻辑，直接挂起，不再重复弹窗
        if (isLockingOut) {
            return new Promise(() => {
            });
        }

        isLockingOut = true; // 加锁

        // 获取后端返回的具体消息，如果没有则使用默认消息
        const msg = (error.response.data && error.response.data.msg)
            ? error.response.data.msg
            : "您的账号已被禁用，系统将强制下线";

        // 使用 MessageBox 强提示，确保用户看到
        ElementUI.MessageBox.alert(msg, '下线通知', {
            confirmButtonText: '确定',
            type: 'error',
            showClose: false, // 禁止关闭，强制确认
            callback: action => {
                sessionStorage.removeItem("user");
                router.push('/login');
                isLockingOut = false; // 重置锁
            }
        });

        // 返回一个永远 pending 的 Promise，中断后续请求链
        return new Promise(() => {
        });
    }

    // 2. 屏蔽超时与网络错误提示
    if (error.code === 'ECONNABORTED' || error.message.includes('timeout') || error.message.includes('Network Error')) {
        console.warn('请求超时或网络异常，已自动忽略提示:', error.message);
        return new Promise(() => {
        }); // 挂起，不显示错误
    }

    return Promise.reject(error);
});
// ----------------------------

// 6. 使用 ElementUI
Vue.use(ElementUI);

Vue.config.productionTip = false

new Vue({
    router,
    render: h => h(App)
}).$mount('#app')
