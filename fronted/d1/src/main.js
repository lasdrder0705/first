import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'
import Login from './components/Login.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// 定义路由配置
const routes = [
  { path: '/login', component: Login },
  { 
    path: '/dashboard',
    component: () => import('./components/Dashboard.vue'),
    meta: { requiresAuth: true }
  },
  // 管理员路由
  { 
    path: '/buildings',
    component: () => import('./components/BuildingManagement.vue'),
    meta: { requiresAuth: true }
  },
  { 
    path: '/vehicles',
    component: () => import('./components/VehicleManagement.vue'),
    meta: { requiresAuth: true }
  },
  { 
    path: '/parking',
    component: () => import('./components/ParkingManagement.vue'),
    meta: { requiresAuth: true }
  },
  { 
    path: '/owners',
    component: () => import('./components/OwnerManagement.vue'),
    meta: { requiresAuth: true }
  },
  { 
    path: '/staff',
    component: () => import('./components/EmployeeManagement.vue'),
    meta: { requiresAuth: true }
  },
  // 员工路由
  { 
    path: '/visitor-log',
    component: () => import('./components/VisitorLog.vue'),
    meta: { requiresAuth: true, permission: '来访人员登记' }
  },
  { 
    path: '/maintenance',
    component: () => import('./components/Maintenance.vue'),
    meta: { requiresAuth: true, permission: '保修上传' }
  },
  { 
    path: '/patrol-log',
    component: () => import('./components/PatrolLog.vue'),
    meta: { requiresAuth: true, permission: '安全巡逻日志' }
  },
  { 
    path: '/cleaning-log',
    component: () => import('./components/CleaningLog.vue'),
    meta: { requiresAuth: true, permission: '公共区域清洁日志' }
  },
  { 
    path: '/repair-list',
    component: () => import('./components/RepairList.vue'),
    meta: { requiresAuth: true, permission: '报修清单管理' }
  },
  { path: '/', redirect: '/login' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  console.log('路由跳转 - 目标:', to.path);
  console.log('路由跳转 - 来源:', from.path);
  console.log('路由跳转 - 元信息:', to.meta);
  
  const token = localStorage.getItem('token');
  if (to.meta.requiresAuth && !token) {
    console.log('未登录，重定向到登录页面');
    next('/login');
    return;
  }

  if (token && to.meta.permission) {
    try {
      // 修改 token 解析方法
      const parseJwt = (token) => {
        try {
          const base64Url = token.split('.')[1];
          const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
          // 添加填充
          const padding = '='.repeat((4 - base64.length % 4) % 4);
          const jsonPayload = decodeURIComponent(atob(base64 + padding).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
          }).join(''));
          return JSON.parse(jsonPayload);
        } catch (e) {
          console.error('Token解析详细错误:', e);
          return null;
        }
      };

      const tokenData = parseJwt(token);
      console.log('权限检查 - 解析后的token数据:', tokenData);

      if (!tokenData) {
        console.error('Token解析失败');
        next('/login');
        return;
      }

      console.log('权限检查 - 用户信息:', tokenData);
      console.log('权限检查 - 需要权限:', to.meta.permission);
      
      if (tokenData.role !== 'admin' && !tokenData.permissions?.includes(to.meta.permission)) {
        console.log('权限不足，重定向到仪表板');
        next('/dashboard');
        return;
      }
    } catch (e) {
      console.error('权限检查错误:', e);
      next('/login');
      return;
    }
  }
  
  console.log('允许路由跳转');
  next();
})

const app = createApp(App)
app.use(router)
app.use(ElementPlus)
app.mount('#app') 