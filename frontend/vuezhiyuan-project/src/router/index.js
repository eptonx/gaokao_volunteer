import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // ========== 考生端路由 ==========
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { guest: true },
    },
    {
      path: '/score',
      name: 'score',
      component: () => import('@/views/ScoreView.vue'),
      meta: { auth: true },
    },
    {
      path: '/ai',
      name: 'ai',
      component: () => import('@/views/AiChatView.vue'),
      meta: { auth: true },
    },
    {
      path: '/plan',
      name: 'plan',
      component: () => import('@/views/PlanView.vue'),
      meta: { auth: true },
    },

    // ========== 院校端路由（/institution 前缀） ==========
    {
      path: '/institution/login',
      name: 'instLogin',
      component: () => import('@/views/institution/Login.vue'),
    },
    {
      path: '/institution/register',
      name: 'instRegister',
      component: () => import('@/views/institution/Register.vue'),
    },
    // 院校管理端（侧边栏布局）
    {
      path: '/institution',
      component: () => import('@/layouts/InstitutionLayout.vue'),
      children: [
        { path: 'workbench', name: 'instWorkbench', component: () => import('@/views/institution/Workbench.vue') },
        { path: 'enrollment-plan', name: 'instEnrollmentPlan', component: () => import('@/views/institution/EnrollmentPlan.vue') },
        { path: 'admission-guide', name: 'instAdmissionGuide', component: () => import('@/views/institution/AdmissionGuide.vue') },
        { path: 'admission-score', name: 'instAdmissionScore', component: () => import('@/views/institution/AdmissionScore.vue') },
        { path: 'settings', name: 'instSettings', component: () => import('@/views/institution/AccountSettings.vue') },
        { path: '', redirect: '/institution/workbench' },
      ],
    },

    
    // 考生端：院校查询展示
    {
      path: '/search',
      name: 'search',
      component: () => import('@/views/InstitutionSearch.vue'),
      meta: { auth: true },
    },
    {
      path: '/institution/:id',
      name: 'institution-detail',
      component: () => import('@/views/InstitutionDetail.vue'),
      meta: { auth: true },
    },
    {
      path: '/favorites',
      name: 'favorites',
      component: () => import('@/views/MyFavorites.vue'),
      meta: { auth: true },
    },
    {
      path: '/center',
      name: 'center',
      component: () => import('@/views/PersonalCenter.vue'),
      meta: { auth: true },
    },

    
    // ========== 运营端路由 ==========
    {
      path: '/admin/dashboard',
      name: 'dashboard',
      component: () => import('@/views/admin/Dashboard.vue'),
      meta: { needLogin: true, title: '仪表盘' },
    },
    {
      path: '/admin/login',
      name: 'adminLogin',
      component: () => import('@/views/Login.vue'),
    },
    {
      path: '/admin',
      redirect: '/admin/dashboard',
    },
    {
      path: '/admin/users',
      name: 'adminUsers',
      component: () => import('@/views/AdminManage.vue'),
      meta: { needLogin: true },
    },
    {
      path: '/admin/qualificationAudit',
      name: 'qualificationAudit',
      component: () => import('@/views/QualAudit.vue'),
      meta: { needLogin: true },
    },
    {
      path: '/admin/guide-review',
      name: 'guideReview',
      component: () => import('@/views/admin/GuideReview.vue'),
      meta: { needLogin: true, title: '招生简章审核' },
    },
    {
      path: '/admin/institution',
      name: 'institution',
      component: () => import('@/views/admin/InstitutionManage.vue'),
    },
    {
      path: '/admin/majorDict',
      name: 'majorDict',
      component: () => import('@/views/admin/MajorDictManage.vue'),
    },
    {
      path: '/admin/admissionHistory',
      name: 'admissionHistory',
      component: () => import('@/views/admin/AdmissionHistoryManage.vue'),
    },
    {
      path: '/admin/login-record',
      name: 'loginRecord',
      component: () => import('@/views/admin/LoginRecord.vue'),
      meta: { needLogin: true, title: '登录日志' },
    },

    // 默认重定向
    {
      path: '/',
      redirect: '/login',
    },
    { path: '/:pathMatch(.*)*', redirect: '/login' },
  ],
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  const auth = useAuthStore()
  // JWT 认证守卫
  if (to.meta.auth && !auth.isLoggedIn) return next('/login')
  if (to.meta.guest && auth.isLoggedIn) return next('/ai')
  // 运营端登录守卫（标记 needLogin 的页面，后台 session 处理）
  if (!to.meta.needLogin) return next()
  next()
})

export default router
