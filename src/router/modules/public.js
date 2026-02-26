// 公开路由：无需登录即可访问
export default [
    {
        path: '/admin',
        name: 'AdminLogin',
        component: () => import('../../views/AdminLogin.vue')
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('../../views/Login.vue')
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('../../views/Register.vue')
    },
    {
        path: '/',
        redirect: '/login'
    }
];

