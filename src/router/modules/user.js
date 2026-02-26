// 用户端路由：普通用户角色 USER
export default [
    {
        path: '/front',
        component: () => import('../../layout/UserLayout.vue'),
        children: [
            {
                path: 'home',
                name: 'UserHome',
                component: () => import('../../views/UserHome.vue'),
                meta: {requireAuth: true, role: 'USER'}
            },
            {
                path: 'chat',
                name: 'Chat',
                component: () => import('../../views/Chat.vue'),
                meta: {requireAuth: true, role: 'USER'}
            },
            {
                path: 'user-center',
                name: 'UserCenter',
                component: () => import('../../views/UserCenter.vue'),
                meta: {requireAuth: true, role: 'USER'}
            },
            {
                path: 'cart',
                name: 'Cart',
                component: () => import('../../views/Cart.vue'),
                meta: {requireAuth: true, role: 'USER'}
            },
            {
                path: 'mall',
                name: 'Mall',
                component: () => import('../../views/UserMall.vue'),
                meta: {requireAuth: true, role: 'USER'}
            },
            {
                path: 'product/:id',
                name: 'ProductDetail',
                component: () => import('../../views/ProductDetail.vue'),
                meta: {requireAuth: true, role: 'USER'}
            },
            {
                path: 'feed',
                name: 'UserFeed',
                component: () => import('../../views/UserFeed.vue'),
                meta: {requireAuth: true, role: 'USER'}
            },
            {
                path: 'pet',
                name: 'UserPet',
                component: () => import('../../views/UserPet.vue'),
                meta: {requireAuth: true, role: 'USER'}
            },
            {
                path: 'health',
                name: 'MyHealth',
                component: () => import('../../views/HealthRecord.vue'),
                meta: {requireAuth: true, role: 'USER'}
            },
            {
                path: 'case',
                name: 'PetCase',
                component: () => import('../../views/PetCase.vue'),
                meta: {requireAuth: true, role: 'USER'}
            },
            {
                path: 'orders',
                name: 'MyOrders',
                component: () => import('../../views/UserOrders.vue'),
                meta: {requireAuth: true, role: 'USER'}
            }
        ]
    }
];
