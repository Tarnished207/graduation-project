// 管理端路由：ADMIN
export default [
    {
        path: '/manage',
        name: 'Manage',
        component: () => import('../../views/Manage.vue'),
        redirect: '/home',
        meta: {requireAuth: true, role: 'ADMIN'},
        children: [
            {
                path: '/home',
                name: 'Home',
                component: () => import('../../views/Home.vue'),
                meta: {requireAuth: true, role: 'ADMIN'}
            },
            {
                path: '/user',
                name: 'User',
                component: () => import('../../views/User.vue'),
                meta: {requireAuth: true, role: 'ADMIN'}
            },
            {
                path: '/product',
                name: 'Product',
                component: () => import('../../views/Product.vue'),
                meta: {requireAuth: true, role: 'ADMIN'}
            },
            {
                path: '/orders',
                name: 'Orders',
                component: () => import('../../views/Orders.vue'),
                meta: {requireAuth: true, role: 'ADMIN'}
            },
            {
                path: '/pet',
                name: 'Pet',
                component: () => import('../../views/Pet.vue'),
                meta: {requireAuth: true, role: 'ADMIN'}
            },
            {
                path: '/monitor',
                name: 'Monitor',
                component: () => import('../../views/Monitor.vue'),
                meta: {requireAuth: true, role: 'ADMIN'}
            }
        ]
    }
];

