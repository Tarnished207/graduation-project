// 客服与医生端路由
export default [
    {
        path: '/service/workbench',
        name: 'ServiceHome',
        component: () => import('../../views/ServiceHome.vue'),
        meta: {requireAuth: true, role: 'SERVICE'}
    },
    {
        path: '/doctor/workbench',
        name: 'DoctorHome',
        component: () => import('../../views/DoctorHome.vue'),
        meta: {requireAuth: true, role: 'DOCTOR'}
    }
];
