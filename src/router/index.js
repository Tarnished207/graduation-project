import Vue from 'vue'
import VueRouter from 'vue-router'
// 路由模块化：按角色与访问级别拆分
import publicRoutes from './modules/public'
import userRoutes from './modules/user'
import serviceRoutes from './modules/service'
import adminRoutes from './modules/admin'

Vue.use(VueRouter)

// 汇总各模块路由
const routes = [
    ...publicRoutes,
    ...userRoutes,
    ...serviceRoutes,
    ...adminRoutes
]

const router = new VueRouter({
    routes,
    scrollBehavior(to, from, savedPosition) {
        if (savedPosition) return savedPosition;
        return {x: 0, y: 0};
    }
})

// 路由守卫 (保持逻辑不变，只做安全检查)
router.beforeEach((to, from, next) => {
    const userJson = sessionStorage.getItem("user");
    const user = userJson ? JSON.parse(userJson) : null;

    // 白名单
    const publicPaths = ['/login', '/register', '/admin'];
    if (publicPaths.includes(to.path)) {
        next();
        return;
    }

    // 没登录？踢回
    if (!user) {
        next('/login');
        return;
    }

    // 角色大本营
    const homeMap = {
        'ADMIN': '/manage/home',
        'USER': '/front/home',
        'SERVICE': '/service/workbench',
        'DOCTOR': '/doctor/workbench'
    };

    // 权限检查
    const requireRole = to.meta.role;
    if (requireRole && user.role !== requireRole) {
        const correctHome = homeMap[user.role] || '/login';
        if (to.path === correctHome) {
            next();
        } else {
            next(correctHome);
        }
        return;
    }

    next();
})

export default router
