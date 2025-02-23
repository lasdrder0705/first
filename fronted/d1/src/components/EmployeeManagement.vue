<template>
  <div class="employee-management">
    <nav-bar></nav-bar>
    <main class="main-content">
      <div class="container">
        <h2>员工账号管理</h2>
        <!-- 新增／编辑员工账号表单 -->
        <div class="employee-form">
          <h3>{{ editingEmployee ? '编辑员工账号' : '新增员工账号' }}</h3>
          <form @submit.prevent="handleEmployeeSubmit">
            <div class="form-row">
              <div class="form-group">
                <label>姓名:</label>
                <input type="text" v-model="employeeForm.name" required />
              </div>
              <div class="form-group">
                <label>身份证号码:</label>
                <div class="id-card-input">
                  <input 
                    type="text" 
                    v-model="employeeForm.id_card" 
                    required 
                    :disabled="editingEmployee && !isEditingIdCard"
                  />
                  <button 
                    v-if="editingEmployee" 
                    type="button" 
                    class="edit-id-card-btn"
                    @click="toggleIdCardEdit"
                  >
                    {{ isEditingIdCard ? '取消修改' : '修改' }}
                  </button>
                </div>
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>职位:</label>
                <select v-model="employeeForm.role" required>
                  <option value="">请选择</option>
                  <option value="保安">保安</option>
                  <option value="保洁">保洁</option>
                  <option value="普通员工">普通员工</option>
                </select>
              </div>
            </div>
            <!-- 功能权限区域 -->
            <div class="form-group permissions-section">
              <label class="permissions-label">功能权限:</label>
              <div class="permissions-container">
                <div v-for="(perm, index) in availablePermissions" 
                     :key="index" 
                     class="permission-item"
                     :class="{ 'selected': employeeForm.permissions.includes(perm) }">
                  <input 
                    type="checkbox" 
                    :id="'perm-' + index" 
                    v-model="employeeForm.permissions" 
                    :value="perm"
                    class="permission-checkbox" 
                  />
                  <label :for="'perm-' + index" class="permission-label">
                    <span class="permission-icon">
                      <i :class="getPermissionIcon(perm)"></i>
                    </span>
                    <span class="permission-text">{{ perm }}</span>
                  </label>
                </div>
              </div>
              <div class="permissions-hint">
                * 如未选择权限，将根据职位自动分配默认权限
              </div>
            </div>
            <div class="form-actions">
              <button type="submit" :class="{ 'edit-mode': editingEmployee }">
                {{ editingEmployee ? '保存修改' : '新增员工' }}
              </button>
              <button type="button">取消</button>
            </div>
          </form>
        </div>
        <!-- 员工列表 -->
        <div class="employee-list">
          <div class="list-header">
            <h3>员工列表</h3>
            <div class="search-box">
              <input 
                type="text" 
                v-model="searchQuery" 
                placeholder="搜索员工姓名/身份证号..."
                @input="handleSearch"
              />
              <i class="fas fa-search"></i>
            </div>
          </div>
          <div v-for="employee in filteredEmployees" :key="employee.id" class="employee-card">
            <div class="employee-info">
              <h3>{{ employee.name }}</h3>
              <p>身份证号：{{ employee.id_card }}</p>
              <p>职位：{{ employee.role }}</p>
              <p>权限：{{ Array.isArray(employee.permissions) ? employee.permissions.join(', ') : employee.permissions }}</p>
            </div>
            <div class="card-actions">
              <button @click="editEmployee(employee)">编辑</button>
              <button @click="deleteEmployee(employee.id)">删除</button>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import NavBar from "./NavBar.vue";
export default {
  name: "EmployeeManagement",
  components: { NavBar },
  data() {
    return {
      employees: [],
      employeeForm: {
        id: null,
        name: "",
        id_card: "",
        role: "",
        permissions: []
      },
      editingEmployee: false,
      isEditingIdCard: false,
      originalIdCard: '',
      // 修改默认权限配置
      defaultPermissions: {
        "保安": ["来访人员登记", "保修上传", "安全巡逻日志"],
        "保洁": ["保修上传", "公共区域清洁日志"],
        "普通员工": ["报修清单管理", "楼房信息管理", "车辆登记管理", "车位租售管理"] // 新增普通员工角色及默认权限
      },
      // 更新可用权限列表
      availablePermissions: [
        "来访人员登记", 
        "保修上传", 
        "安全巡逻日志", 
        "公共区域清洁日志", 
        "报修清单管理",
        "楼房信息管理",  // 新增
        "车辆登记管理",  // 新增
        "车位租售管理"   // 新增
      ],
      searchQuery: '', // 添加搜索查询
    };
  },
  computed: {
    // 添加过滤员工的计算属性
    filteredEmployees() {
      if (!this.searchQuery) return this.employees;
      
      const query = this.searchQuery.toLowerCase().trim();
      return this.employees.filter(employee => {
        return (
          (employee.name && employee.name.toLowerCase().includes(query)) ||
          (employee.id_card && employee.id_card.includes(query)) ||
          (employee.role && employee.role.toLowerCase().includes(query))
        );
      });
    }
  },
  methods: {
    async fetchEmployees() {
      try {
        const token = localStorage.getItem('token');
        const response = await fetch('/api/employees', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        
        if (!response.ok) {
          throw new Error('获取员工数据失败');
        }
        
        const data = await response.json();
        if (data.success) {
          this.employees = data.data.map(employee => {
            let permissions = [];
            try {
              permissions = JSON.parse(employee.permissions || '[]');
            } catch (e) {
              console.warn('解析权限失败:', e);
              permissions = employee.permissions ? employee.permissions.split(',').map(p => p.trim()) : [];
            }
            
            return {
              id: employee.id,
              name: employee.nickname,
              id_card: employee.username, // 使用 username 作为身份证号
              role: employee.role,
              permissions: permissions
            };
          });
        }
      } catch (error) {
        console.error('获取员工列表失败：', error);
      }
    },
    async handleEmployeeSubmit() {
      try {
        // 表单验证
        if (!this.employeeForm.name || !this.employeeForm.id_card || !this.employeeForm.role) {
          alert('请填写完整的员工信息');
          return;
        }

        // 验证身份证号格式
        const idCardRegex = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/;
        if (!idCardRegex.test(this.employeeForm.id_card)) {
          alert('请输入正确的身份证号码');
          return;
        }

        // 如果未选择权限，则根据角色赋予默认权限
        if (!this.employeeForm.permissions || this.employeeForm.permissions.length === 0) {
          this.employeeForm.permissions = this.defaultPermissions[this.employeeForm.role] || [];
        }

        const token = localStorage.getItem('token');
        const requestData = {
          name: this.employeeForm.name,
          id_card: this.employeeForm.id_card,
          role: this.employeeForm.role,
          permissions: this.employeeForm.permissions
        };

        // 根据是否有 id 判断是新增还是编辑
        const url = this.employeeForm.id ? 
          `/api/employees/${this.employeeForm.id}` : 
          '/api/employees';
        
        const response = await fetch(url, {
          method: this.employeeForm.id ? 'PUT' : 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          body: JSON.stringify(requestData)
        });

        const result = await response.json();
        
        if (!response.ok) {
          throw new Error(result.message || '保存员工信息失败');
        }

        if (result.success) {
          alert(this.employeeForm.id ? '员工信息更新成功' : '员工账号创建成功！\n登录账号为身份证号，初始密码为身份证后8位');
          this.resetEmployeeForm();
          await this.fetchEmployees();
        } else {
          throw new Error(result.message || '保存失败');
        }
      } catch (error) {
        console.error('保存员工信息失败：', error);
        alert(error.message || '保存员工信息失败');
      }
    },
    editEmployee(employee) {
      try {
        let permissions = [];
        if (employee.permissions) {
          if (Array.isArray(employee.permissions)) {
            permissions = employee.permissions;
          } else {
            try {
              permissions = JSON.parse(employee.permissions);
            } catch (e) {
              permissions = employee.permissions.split(',').map(p => p.trim());
            }
          }
        }

        // 设置表单数据
        this.employeeForm = {
          id: employee.id,
          name: employee.nickname || employee.name,
          id_card: employee.id_card || employee.username, // 使用 id_card 或 username 作为身份证号
          role: employee.role,
          permissions: permissions
        };

        // 保存原始身份证号
        this.originalIdCard = employee.id_card || employee.username;
        
        // 设置编辑状态
        this.editingEmployee = true;
        this.isEditingIdCard = false;
      } catch (error) {
        console.error('编辑员工信息失败:', error);
        alert('编辑员工信息失败');
      }
    },
    resetEmployeeForm() {
      this.employeeForm = {
        id: null,
        name: "",
        id_card: "",
        role: "",
        permissions: []
      };
      this.editingEmployee = false;
      this.isEditingIdCard = false;
      this.originalIdCard = '';
    },
    formatPermissions(permissions) {
      if (!permissions) return '';
      try {
        // 如果已经是数组，直接使用
        if (Array.isArray(permissions)) {
          return permissions.join(", ");
        }
        // 尝试解析 JSON 字符串
        const perms = JSON.parse(permissions);
        return Array.isArray(perms) ? perms.join(", ") : permissions;
      } catch (e) {
        // 如果解析失败，可能是普通字符串，直接返回
        console.warn('权限格式化失败:', e);
        return permissions;
      }
    },
    formatEmployee(employee) {
      return {
        id: employee.id,
        name: employee.nickname, // 使用 nickname 作为员工姓名
        id_card: employee.username, // 使用 username 作为身份证号
        role: employee.role,
        permissions: JSON.parse(employee.permissions || '[]')
      };
    },
    async deleteEmployee(id) {
      try {
        // 添加确认对话框
        if (!confirm('确定要删除该员工吗？此操作不可恢复。')) {
          return;
        }

        const token = localStorage.getItem('token');
        const response = await fetch(`/api/employees/${id}`, {
          method: 'DELETE',
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        const result = await response.json();
        
        if (!response.ok) {
          throw new Error(result.message || '删除员工失败');
        }

        if (result.success) {
          alert('删除员工成功');
          // 重新获取员工列表
          await this.fetchEmployees();
        } else {
          throw new Error(result.message || '删除失败');
        }
      } catch (error) {
        console.error('删除员工失败：', error);
        alert(error.message || '删除员工失败');
      }
    },
    toggleIdCardEdit() {
      if (this.isEditingIdCard) {
        this.employeeForm.id_card = this.originalIdCard;
      } else {
        this.originalIdCard = this.employeeForm.id_card;
      }
      this.isEditingIdCard = !this.isEditingIdCard;
    },
    getPermissionIcon(permission) {
      const icons = {
        '来访人员登记': 'fas fa-user-check',
        '保修上传': 'fas fa-tools',
        '安全巡逻日志': 'fas fa-shield-alt',
        '公共区域清洁日志': 'fas fa-broom',
        '报修清单管理': 'fas fa-clipboard-list',
        '楼房信息管理': 'fas fa-building',      // 新增
        '车辆登记管理': 'fas fa-car',           // 新增
        '车位租售管理': 'fas fa-parking'        // 新增
      };
      return icons[permission] || 'fas fa-check';
    },
    // 添加搜索处理方法
    handleSearch() {
      // 这里可以添加防抖逻辑如果需要
      // 目前使用计算属性，所以这个方法可以为空
    }
  },
  async created() {
    await this.fetchEmployees();
  }
};
</script>

<style scoped>
.employee-management {
  min-height: 100vh;
  background-image: url('/assets/employee-bg.jpg'); /* 背景图片路径，请自行替换 */
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  position: relative;
}

.employee-management::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(10px);
  z-index: -1;
}

.main-content {
  padding: 2rem 0;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 2rem;
}

.employee-form,
.employee-list {
  background: rgba(255, 255, 255, 0.93);
  padding: 1.5rem;
  border-radius: 10px;
  margin-bottom: 2rem;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.form-row {
  display: flex;
  gap: 1.5rem;
  margin-bottom: 1rem;
}

.form-row .form-group {
  flex: 1;
}

.form-group {
  margin-bottom: 1rem;
}

/* 姓名输入框样式 */
.form-group input[type="text"] {
  width: 100%;
  max-width: 300px; /* 限制最大宽度 */
}

/* 身份证号输入框容器样式 */
.id-card-input {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  max-width: 500px; /* 身份证号输入框组合的最大宽度 */
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #1a365d;
  font-weight: 500;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 1rem;
  transition: all 0.3s ease;
  background-color: rgba(255, 255, 255, 0.93);
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #3182ce;
  box-shadow: 0 0 0 3px rgba(49, 130, 206, 0.2);
}

.permissions-section {
  background-color: #f8fafc;
  padding: 1.5rem;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.permissions-label {
  font-size: 1.1rem;
  color: #2d3748;
  margin-bottom: 1rem;
  display: block;
}

.permissions-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 1rem;
}

.permission-item {
  position: relative;
  background: white;
  padding: 1rem;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  cursor: pointer;
}

.permission-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  border-color: #cbd5e0;
}

.permission-item.selected {
  background-color: #ebf8ff;
  border-color: #4299e1;
}

.permission-checkbox {
  position: absolute;
  opacity: 0;
  cursor: pointer;
}

.permission-label {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  width: 100%;
  cursor: pointer;
}

.permission-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2rem;
  height: 2rem;
  background-color: #edf2f7;
  border-radius: 6px;
  color: #4a5568;
}

.selected .permission-icon {
  background-color: #4299e1;
  color: white;
}

.permission-text {
  font-size: 0.95rem;
  color: #4a5568;
  font-weight: 500;
}

.selected .permission-text {
  color: #2b6cb0;
}

.permissions-hint {
  font-size: 0.875rem;
  color: #718096;
  margin-top: 0.5rem;
  font-style: italic;
}

/* 添加动画效果 */
.permission-item {
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式布局调整 */
@media (max-width: 768px) {
  .container {
    padding: 0.5rem;
  }
  .employee-form,
  .employee-list {
    padding: 1rem;
  }
  .form-group input,
  .form-group select {
    font-size: 16px;
  }
  .form-actions {
    flex-direction: column;
    align-items: stretch;
  }
  .form-actions button {
    width: 100%;
    margin-bottom: 0.5rem;
  }
  .form-row {
    flex-direction: column;
    gap: 1rem;
  }
  .form-row .form-group {
    margin-bottom: 0;
  }
  .permissions-container {
    grid-template-columns: 1fr;
  }
  
  .permission-item {
    padding: 0.75rem;
  }
  
  .permission-icon {
    width: 1.75rem;
    height: 1.75rem;
  }
  
  .permission-text {
    font-size: 0.9rem;
  }
}

.id-card-input {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.edit-id-card-btn {
  padding: 0.75rem 1rem;
  background-color: #4a5568;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.875rem;
  transition: all 0.3s ease;
}

.edit-id-card-btn:hover {
  background-color: #2d3748;
}

input:disabled {
  background-color: #edf2f7;
  cursor: not-allowed;
}

/* 表单操作按钮样式 */
.form-actions {
  margin-top: 2rem;
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  padding-top: 1.5rem;
  border-top: 1px solid #e2e8f0;
}

.form-actions button {
  min-width: 120px;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  font-size: 0.95rem;
  transition: all 0.3s ease;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

/* 提交按钮样式 */
.form-actions button[type="submit"] {
  background-color: #3182ce;
  color: white;
  border: none;
  box-shadow: 0 2px 4px rgba(49, 130, 206, 0.1);
}

.form-actions button[type="submit"]:hover {
  background-color: #2c5282;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(49, 130, 206, 0.2);
}

.form-actions button[type="submit"]::before {
  content: "\f067";  /* 加号图标 */
  font-family: "Font Awesome 5 Free";
  font-weight: 900;
}

.form-actions button[type="submit"].edit-mode::before {
  content: "\f0c7";  /* 保存图标 */
}

/* 取消按钮样式 */
.form-actions button[type="button"] {
  background-color: #fff;
  color: #4a5568;
  border: 1px solid #e2e8f0;
}

.form-actions button[type="button"]:hover {
  background-color: #f7fafc;
  border-color: #cbd5e0;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.form-actions button[type="button"]::before {
  content: "\f00d";  /* 取消图标 */
  font-family: "Font Awesome 5 Free";
  font-weight: 900;
}

/* 员工卡片样式 */
.employee-card {
  border: 1px solid #e2e8f0;
  padding: 1.5rem;
  border-radius: 12px;
  margin-bottom: 1.5rem;
  background: rgba(255, 255, 255, 0.95);
  transition: all 0.3s ease;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.employee-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.employee-info {
  flex: 1;
}

.employee-info h3 {
  color: #1a365d;
  font-size: 1.25rem;
  margin-bottom: 0.75rem;
  font-weight: 600;
}

.employee-info p {
  color: #4a5568;
  margin: 0.5rem 0;
  line-height: 1.5;
}

/* 卡片操作按钮样式 */
.card-actions {
  display: flex;
  gap: 0.75rem;
}

.card-actions button {
  padding: 0.625rem 1.25rem;
  border-radius: 6px;
  font-weight: 500;
  font-size: 0.875rem;
  transition: all 0.3s ease;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

/* 编辑按钮 */
.card-actions button:first-child {
  background-color: #4299e1;
  color: white;
  border: none;
}

.card-actions button:first-child:hover {
  background-color: #3182ce;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(66, 153, 225, 0.2);
}

.card-actions button:first-child::before {
  content: "\f044";  /* 编辑图标 */
  font-family: "Font Awesome 5 Free";
  font-weight: 900;
}

/* 删除按钮 */
.card-actions button:last-child {
  background-color: #fc8181;
  color: white;
  border: none;
}

.card-actions button:last-child:hover {
  background-color: #f56565;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(252, 129, 129, 0.2);
}

.card-actions button:last-child::before {
  content: "\f2ed";  /* 删除图标 */
  font-family: "Font Awesome 5 Free";
  font-weight: 900;
}

/* 身份证号修改按钮样式 */
.edit-id-card-btn {
  padding: 0.625rem 1rem;
  background-color: #718096;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.875rem;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.edit-id-card-btn::before {
  content: "\f044";  /* 编辑图标 */
  font-family: "Font Awesome 5 Free";
  font-weight: 900;
}

.edit-id-card-btn:hover {
  background-color: #4a5568;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(74, 85, 104, 0.2);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .form-actions {
    flex-direction: column;
  }

  .form-actions button {
    width: 100%;
  }

  .card-actions {
    flex-direction: column;
    gap: 0.5rem;
  }

  .card-actions button {
    width: 100%;
    justify-content: center;
  }

  .employee-card {
    flex-direction: column;
    gap: 1rem;
  }

  .employee-info {
    width: 100%;
  }
}

/* 修改列表头部样式 */
.list-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.5rem;
  margin-bottom: 2rem;
  text-align: center;
}

.list-header h3 {
  margin: 0;
  color: #2d3748;
  font-size: 1.5rem;
}

.search-box {
  position: relative;
  width: 100%;
  max-width: 500px;
  margin: 0 auto;
}

.search-box input {
  width: 100%;
  padding: 0.875rem 1rem 0.875rem 3rem;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  font-size: 1rem;
  transition: all 0.3s ease;
  background-color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.search-box input:focus {
  border-color: #4299e1;
  box-shadow: 0 0 0 3px rgba(66, 153, 225, 0.1);
  outline: none;
}

.search-box i {
  position: absolute;
  left: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  color: #a0aec0;
  font-size: 1rem;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .list-header {
    flex-direction: column;
    gap: 1rem;
  }
  
  .search-box {
    width: 100%;
  }
}
</style> 