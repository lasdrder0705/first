import { createRouter, createWebHistory } from 'vue-router';

// 解析 JWT token 获取权限信息
function getPermissionsFromToken() {
  const token = localStorage.getItem('token');
  if (!token) return { role: '', permissions: [] };

  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));

    const payload = JSON.parse(jsonPayload);
    return {
      role: payload.role,
      permissions: payload.permissions || []
    };
  } catch (e) {
    console.error('Token解析失败:', e);
    return { role: '', permissions: [] };
  }
}

const routes = [
  {
    path: '/visitor-log',
    component: () => import('../components/VisitorLog.vue'),
    meta: { 
      requiresAuth: true,
      requiredPermission: '来访人员登记'
    }
  },
  {
    path: '/patrol-log',
    component: () => import('../components/PatrolLog.vue'),
    meta: { 
      requiresAuth: true,
      requiredPermission: '安全巡逻日志'
    }
  },
  {
    path: '/cleaning-log',
    component: () => import('../components/CleaningLog.vue'),
    meta: { 
      requiresAuth: true,
      requiredPermission: '公共区域清洁日志'
    }
  },
  {
    path: '/repair-list',
    component: () => import('../components/RepairList.vue'),
    meta: { 
      requiresAuth: true,
      requiredPermission: '报修清单管理'
    }
  },
  {
    path: '/maintenance',
    component: () => import('../components/Maintenance.vue'),
    meta: { 
      requiresAuth: true,
      requiredPermission: '保修上传'
    }
  },
  // ... 其他路由配置
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');
  
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!token) {
      next('/login');
      return;
    }

    const { role, permissions } = getPermissionsFromToken();
    
    // 检查是否需要特定权限
    if (to.meta.requiredPermission) {
      // 管理员可以访问所有页面
      if (role === 'admin' || permissions.includes(to.meta.requiredPermission)) {
        next();
      } else {
        alert('您没有权限访问该页面');
        next(false);
      }
    } else {
      next();
    }
  } else {
    next();
  }
});

export default router; 