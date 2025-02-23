<template>
  <header class="header">
    <div class="container">
      <h1>{{ pageTitle }}</h1>
      <nav class="nav-bar">
        <!-- 管理员可以看到所有菜单 -->
        <template v-if="isAdmin">
          <router-link to="/dashboard" class="nav-link" :class="{ active: currentPath === '/dashboard' }">
            个人资料
          </router-link>
          <router-link to="/buildings" class="nav-link" :class="{ active: currentPath === '/buildings' }">
            楼房信息管理
          </router-link>
          <router-link to="/vehicles" class="nav-link" :class="{ active: currentPath === '/vehicles' }">
            车辆登记管理
          </router-link>
          <router-link to="/parking" class="nav-link" :class="{ active: currentPath === '/parking' }">
            车位租售管理
          </router-link>
          <router-link to="/owners" class="nav-link" :class="{ active: currentPath === '/owners' }">
            业主信息管理
          </router-link>
          <router-link to="/staff" class="nav-link" :class="{ active: currentPath === '/staff' }">
            员工账号管理
          </router-link>
          <!-- 新增业务管理下拉菜单 -->
          <div class="dropdown">
            <button class="nav-link dropdown-toggle" 
                    @click="toggleDropdown" 
                    :class="{ active: isBusinessActive }">
              业务管理
              <i class="fas fa-chevron-down"></i>
            </button>
            <div class="dropdown-menu" v-show="showDropdown">
              <router-link to="/visitor-log" class="dropdown-item">
                来访登记
              </router-link>
              <router-link to="/maintenance" class="dropdown-item">
                报修管理
              </router-link>
              <router-link to="/patrol-log" class="dropdown-item">
                巡逻日志
              </router-link>
              <router-link to="/cleaning-log" class="dropdown-item">
                清洁日志
              </router-link>
              <router-link to="/repair-list" class="dropdown-item">
                维修管理
              </router-link>
            </div>
          </div>
        </template>
        
        <!-- 员工只能看到有权限的菜单 -->
        <template v-else>
          <router-link to="/dashboard" class="nav-link" :class="{ active: currentPath === '/dashboard' }">
            个人资料
          </router-link>
          
          <!-- 根据权限显示楼房管理 -->
          <router-link v-if="canAccessBuildings" 
                      to="/buildings" 
                      class="nav-link" 
                      :class="{ active: currentPath === '/buildings' }">
            楼房信息管理
          </router-link>
          
          <!-- 根据权限显示车辆管理 -->
          <router-link v-if="canAccessVehicles" 
                      to="/vehicles" 
                      class="nav-link" 
                      :class="{ active: currentPath === '/vehicles' }">
            车辆登记管理
          </router-link>
          
          <!-- 根据权限显示车位管理 -->
          <router-link v-if="canAccessParking" 
                      to="/parking" 
                      class="nav-link" 
                      :class="{ active: currentPath === '/parking' }">
            车位租售管理
          </router-link>
          
          <!-- 员工的业务管理下拉菜单 -->
          <div class="dropdown" v-if="hasAnyBusinessPermission">
            <button class="nav-link dropdown-toggle" 
                    @click="toggleDropdown"
                    :class="{ active: isBusinessActive }">
              业务管理
              <i class="fas fa-chevron-down"></i>
            </button>
            <div class="dropdown-menu" v-show="showDropdown">
              <router-link v-if="hasPermission('来访人员登记')" 
                          to="/visitor-log" 
                          class="dropdown-item">
                来访登记
              </router-link>
              <router-link v-if="hasPermission('保修上传')" 
                          to="/maintenance" 
                          class="dropdown-item">
                保修管理
              </router-link>
              <router-link v-if="hasPermission('安全巡逻日志')" 
                          to="/patrol-log" 
                          class="dropdown-item">
                巡逻日志
              </router-link>
              <router-link v-if="hasPermission('公共区域清洁日志')" 
                          to="/cleaning-log" 
                          class="dropdown-item">
                清洁日志
              </router-link>
              <router-link v-if="hasPermission('报修清单管理')" 
                          to="/repair-list" 
                          class="dropdown-item">
                维修管理
              </router-link>
            </div>
          </div>
        </template>
      </nav>
      <button @click="handleLogout" class="logout-btn">退出登录</button>
    </div>
  </header>
</template>

<script>
export default {
  name: 'NavBar',
  data() {
    return {
      userInfo: {
        role: '',
        permissions: []
      },
      showDropdown: false,
      businessRoutes: ['/visitor-log', '/maintenance', '/patrol-log', '/cleaning-log', '/repair-list']
    }
  },
  computed: {
    currentPath() {
      return this.$route.path;
    },
    pageTitle() {
      return this.getPageTitle(this.currentPath);
    },
    isAdmin() {
      return this.userInfo.role === 'admin';
    },
    userRole() {
      return this.isAdmin ? '管理员' : this.userInfo.role;
    },
    isBusinessActive() {
      return this.businessRoutes.includes(this.currentPath);
    },
    hasAnyBusinessPermission() {
      const businessPermissions = [
        '来访人员登记',
        '保修上传',
        '安全巡逻日志',
        '公共区域清洁日志',
        '报修清单管理'
      ];
      return businessPermissions.some(permission => this.hasPermission(permission));
    },
    // 判断是否有权限访问特定页面
    canAccessBuildings() {
      return this.isAdmin || this.hasPermission('楼房信息管理');
    },
    canAccessVehicles() {
      return this.isAdmin || this.hasPermission('车辆登记管理');
    },
    canAccessParking() {
      return this.isAdmin || this.hasPermission('车位租售管理');
    }
  },
  methods: {
    getPageTitle(path) {
      const titles = {
        '/dashboard': '个人中心',
        '/buildings': '楼房信息管理系统',
        '/vehicles': '车辆登记管理系统',
        '/parking': '车位租售管理系统',
        '/visitor-log': '来访登记系统',
        '/maintenance': '保修管理系统',
        '/patrol-log': '巡逻日志系统',
        '/cleaning-log': '清洁日志系统',
        '/repair-list': '报修清单系统'
      };
      return titles[path] || '管理系统';
    },
    parseToken() {
      const token = localStorage.getItem('token');
      if (token) {
        try {
          // 解析 JWT token（这里假设 token 是 base64 编码的）
          const base64Url = token.split('.')[1];
          const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
          const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
          }).join(''));

          const payload = JSON.parse(jsonPayload);
          console.log('Token解析结果:', payload);
          
          this.userInfo = {
            role: payload.role,
            permissions: payload.permissions || []
          };
        } catch (e) {
          console.error('Token解析失败:', e);
        }
      }
    },
    hasPermission(permission) {
      if (this.isAdmin) return true;
      return this.userInfo.permissions && this.userInfo.permissions.includes(permission);
    },
    handleLogout() {
      localStorage.removeItem('token');
      this.$router.push('/login');
    },
    toggleDropdown() {
      this.showDropdown = !this.showDropdown;
    }
  },
  created() {
    this.parseToken();
  },
  mounted() {
    // 点击页面其他地方关闭下拉菜单
    document.addEventListener('click', (e) => {
      if (!e.target.closest('.dropdown')) {
        this.showDropdown = false;
      }
    });
  },
  beforeUnmount() {
    document.removeEventListener('click', this.closeDropdown);
  }
}
</script>

<style scoped>
.header {
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 1.5rem 0;
  position: sticky;
  top: 0;
  z-index: 10;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

h1 {
  font-size: 1.5rem;
  color: #1a365d;
  margin: 0;
}

.nav-bar {
  margin: 0;
  display: flex;
  gap: 1.5rem;
}

.nav-link {
  text-decoration: none;
  color: #666;
  font-size: 1rem;
  font-weight: 500;
  padding: 0.5rem 1rem;
  border-radius: 0.5rem;
  transition: all 0.3s ease;
}

.nav-link:hover {
  color: #3182ce;
  background-color: rgba(49, 130, 206, 0.1);
  text-decoration: none;
}

.nav-link.active {
  color: #3182ce;
  background-color: rgba(49, 130, 206, 0.1);
  font-weight: 600;
}

.logout-btn {
  background-color: #e53e3e;
  color: white;
  border: none;
  padding: 0.5rem 1.5rem;
  border-radius: 0.5rem;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  background-color: #c53030;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(229, 62, 62, 0.2);
}

.dropdown {
  position: relative;
  display: inline-block;
}

.dropdown-toggle {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  background: none;
  border: none;
  font-size: 1rem;
  padding: 0.5rem 1rem;
}

.dropdown-toggle i {
  font-size: 0.8rem;
  transition: transform 0.3s ease;
}

.dropdown-toggle.active i {
  transform: rotate(180deg);
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  left: 0;
  min-width: 200px;
  background: white;
  border-radius: 0.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 0.5rem 0;
  z-index: 1000;
  animation: slideDown 0.3s ease;
}

.dropdown-item {
  display: block;
  padding: 0.75rem 1.5rem;
  color: #4a5568;
  text-decoration: none;
  transition: all 0.3s ease;
}

.dropdown-item:hover {
  background-color: #f7fafc;
  color: #3182ce;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式调整 */
@media (max-width: 768px) {
  .container {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }

  .nav-bar {
    flex-direction: column;
    width: 100%;
  }

  .nav-link {
    padding: 0.75rem;
  }

  .logout-btn {
    width: 100%;
    padding: 0.75rem;
  }

  .dropdown-menu {
    position: static;
    width: 100%;
    box-shadow: none;
    border: 1px solid #e2e8f0;
    margin-top: 0.5rem;
  }
  
  .dropdown-toggle {
    width: 100%;
    justify-content: space-between;
  }
}
</style> 