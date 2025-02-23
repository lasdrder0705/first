<template>
  <div class="vehicle-management">
    <nav-bar />
    <main class="main-content">
      <div class="container">
        <div class="management-content">
          <div class="actions">
            <button @click="toggleNewForm" class="action-btn">
              {{ showNewForm ? '取消' : '新增车辆' }}
            </button>
            <div class="search-box">
              <input 
                type="text" 
                v-model="searchQuery" 
                placeholder="搜索车牌号..."
                @input="handleSearch"
              />
            </div>
          </div>

          <!-- 新增车辆表单 -->
          <div v-if="showNewForm" class="new-vehicle-form">
            <h3>新增车辆信息</h3>
            <form @submit.prevent="createVehicle">
              <div class="form-group">
                <label>车牌号:</label>
                <input type="text" v-model="newVehicle.plateNumber" required />
              </div>
              <div class="form-group">
                <label>车主姓名:</label>
                <input type="text" v-model="newVehicle.ownerName" required />
              </div>
              <div class="form-group">
                <label>车主类型:</label>
                <select v-model="newVehicle.ownerType" required>
                  <option value="">请选择</option>
                  <option value="业主">业主</option>
                  <option value="员工">员工</option>
                </select>
              </div>
              <div class="form-group">
                <label>车辆类型:</label>
                <select v-model="newVehicle.vehicleType" required>
                  <option value="">请选择</option>
                  <option value="小型汽车">小型汽车</option>
                  <option value="电动车">电动车</option>
                  <option value="摩托车">摩托车</option>
                </select>
              </div>
              <div class="form-group">
                <label>联系电话:</label>
                <input type="tel" v-model="newVehicle.contactNumber" required />
              </div>
              <div class="form-group">
                <label>登记日期:</label>
                <input type="text" :value="formatDate(new Date())" disabled />
                <small class="form-text text-muted">系统自动生成当前日期作为登记日期</small>
              </div>
              <button type="submit" class="submit-btn">提交</button>
            </form>
          </div>

          <!-- 车辆列表 -->
          <div class="vehicles-list">
            <h3>车辆列表</h3>
            <div class="vehicle-cards">
              <div class="vehicle-card" v-for="vehicle in filteredVehicles" :key="vehicle.id">
                <div class="vehicle-header">
                  <h4>{{ vehicle.plateNumber }}</h4>
                  <span :class="['status-tag', `status-${vehicle.status}`]">
                    {{ vehicle.status }}
                  </span>
                </div>
                <div class="vehicle-info">
                  <p>车牌号: {{ vehicle.plateNumber }}</p>
                  <p>车主: {{ vehicle.ownerName }}</p>
                  <p>类型: {{ vehicle.vehicleType }}</p>
                  <p>联系电话: {{ vehicle.contactNumber }}</p>
                  <p>停车位: {{ vehicle.parkingSpace || '未分配' }}</p>
                  <p>登记日期: {{ formatDate(vehicle.createdAt) }}</p>
                </div>
                <div class="card-actions">
                  <button @click="openEditModal(vehicle)" class="edit-btn">
                    <i class="fas fa-edit"></i> 编辑
                  </button>
                  <button @click="confirmDelete(vehicle)" class="delete-btn">
                    <i class="fas fa-trash"></i> 删除
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 编辑车辆弹窗 -->
    <div v-if="showEditModal" class="modal">
      <div class="modal-content">
        <h3>编辑车辆信息</h3>
        <form @submit.prevent="handleEdit">
          <div class="form-group">
            <label>车牌号:</label>
            <input type="text" v-model="editingVehicle.plateNumber" required />
          </div>
          <div class="form-group">
            <label>车主姓名:</label>
            <input type="text" v-model="editingVehicle.ownerName" required />
          </div>
          <div class="form-group">
            <label>车主类型:</label>
            <select v-model="editingVehicle.ownerType" required>
              <option value="">请选择</option>
              <option value="业主">业主</option>
              <option value="员工">员工</option>
              <option value="访客">访客</option>
            </select>
          </div>
          <div class="form-group">
            <label>车辆类型:</label>
            <select v-model="editingVehicle.vehicleType" required>
              <option value="">请选择</option>
              <option value="小型汽车">小型汽车</option>
              <option value="电动车">电动车</option>
              <option value="摩托车">摩托车</option>
            </select>
          </div>
          <div class="form-group">
            <label>联系电话:</label>
            <input type="tel" v-model="editingVehicle.contactNumber" required />
          </div>
          <div class="form-group">
            <label>停车位:</label>
            <input type="text" v-model="editingVehicle.parkingSpace" />
          </div>
          <div class="form-group">
            <label>到期日期:</label>
            <input type="date" v-model="editingVehicle.expiryDate" required />
          </div>
          <div class="form-group">
            <label>状态:</label>
            <select v-model="editingVehicle.status">
              <option value="正常">正常</option>
              <option value="即将到期">即将到期</option>
              <option value="已过期">已过期</option>
              <option value="已注销">已注销</option>
            </select>
          </div>
          <div class="modal-actions">
            <button type="submit" class="submit-btn" :disabled="isSubmitting">
              {{ isSubmitting ? '保存中...' : '保存' }}
            </button>
            <button type="button" @click="closeEditModal" class="cancel-btn">取消</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import NavBar from './NavBar.vue'

export default {
  name: 'VehicleManagement',
  components: {
    NavBar
  },
  data() {
    return {
      vehicles: [],
      showNewForm: false,
      showEditModal: false,
      searchQuery: '',
      newVehicle: {
        plateNumber: '',
        ownerName: '',
        ownerType: '',
        vehicleType: '',
        contactNumber: '',
        parkingSpace: '',
        status: ''
      },
      editingVehicle: null,
      isSubmitting: false,
      error: ''
    }
  },
  computed: {
    filteredVehicles() {
      if (!this.searchQuery) return this.vehicles;
      const query = this.searchQuery.toLowerCase();
      return this.vehicles.filter(vehicle => 
        vehicle.plateNumber.toLowerCase().includes(query) ||
        vehicle.ownerName.toLowerCase().includes(query)
      );
    }
  },
  methods: {
    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return d.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      });
    },

    handleLogout() {
      localStorage.removeItem('token');
      this.$router.push('/login');
    },

    toggleNewForm() {
      this.showNewForm = !this.showNewForm;
      if (!this.showNewForm) {
        this.resetNewVehicleForm();
      }
    },

    resetNewVehicleForm() {
      this.newVehicle = {
        plateNumber: '',
        ownerName: '',
        ownerType: '',
        vehicleType: '',
        contactNumber: '',
        parkingSpace: '',
        status: ''
      };
    },

    async fetchVehicles() {
      try {
        const response = await fetch('/api/vehicles', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });

        if (!response.ok) {
          throw new Error('获取车辆信息失败');
        }

        const result = await response.json();
        if (result.success) {
          this.vehicles = result.data;
        } else {
          throw new Error(result.message);
        }
      } catch (err) {
        console.error('获取车辆列表错误:', err);
      }
    },

    // 验证车牌号格式
    validatePlateNumber(plateNumber, vehicleType) {
      // 移除所有空格
      plateNumber = plateNumber.replace(/\s/g, '');
      
      // 不同类型车辆的车牌号正则表达式
      const patterns = {
        '小型汽车': {
          // 普通汽车和新能源汽车的组合正则
          pattern: /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-HJ-NP-Z0-9]{4}[A-HJ-NP-Z0-9DF]$/,
          example: '浙A9J987（普通）、浙AD12345（新能源）'
        },
        '电动车': {
          pattern: /^[A-Z]{2}[0-9]{6}$/,
          example: 'DD123456'
        },
        '摩托车': {
          pattern: /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼][A-Z][0-9]{5}$/,
          example: '浙A12345'
        }
      };

      // 验证对应车型的车牌格式
      const vehiclePattern = patterns[vehicleType];
      if (!vehiclePattern) {
        return {
          isValid: false,
          message: '请先选择车辆类型'
        };
      }

      if (!vehiclePattern.pattern.test(plateNumber)) {
        return {
          isValid: false,
          message: `车牌格式不正确，示例：${vehiclePattern.example}`
        };
      }

      return { isValid: true };
    },

    async createVehicle() {
      try {
        // 验证车牌号
        const validation = this.validatePlateNumber(this.newVehicle.plateNumber, this.newVehicle.vehicleType);
        if (!validation.isValid) {
          alert(validation.message);
          return;
        }

        // 添加创建时间
        const vehicleData = {
          ...this.newVehicle,
          createdAt: new Date().toISOString() // 添加创建时间
        };

        const response = await fetch('/api/vehicles', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          body: JSON.stringify(vehicleData)
        });

        const result = await response.json();
        if (!result.success) {
          throw new Error(result.message);
        }

        this.vehicles.push(result.data);
        this.showNewForm = false;
        this.resetNewVehicleForm();
      } catch (err) {
        console.error('创建车辆错误:', err);
        alert(err.message);
      }
    },

    // 打开编辑模态框
    openEditModal(vehicle) {
      // 创建一个深拷贝，避免直接修改原数据
      this.editingVehicle = JSON.parse(JSON.stringify(vehicle));
      this.showEditModal = true;
    },

    // 关闭编辑模态框
    closeEditModal() {
      this.showEditModal = false;
      this.editingVehicle = null;
      this.error = '';
    },

    // 处理编辑提交
    async handleEdit() {
      try {
        // 验证车牌号
        const validation = this.validatePlateNumber(
          this.editingVehicle.plateNumber, 
          this.editingVehicle.vehicleType
        );
        if (!validation.isValid) {
          alert(validation.message);
          return;
        }

        // 验证必填字段
        if (!this.editingVehicle.ownerName || !this.editingVehicle.contactNumber) {
          alert('请填写必要信息');
          return;
        }

        this.isSubmitting = true;
        const response = await fetch(`/api/vehicles/${this.editingVehicle.id}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          body: JSON.stringify(this.editingVehicle)
        });

        const result = await response.json();
        if (!result.success) {
          throw new Error(result.message);
        }

        // 更新本地数据
        const index = this.vehicles.findIndex(v => v.id === this.editingVehicle.id);
        if (index !== -1) {
          this.vehicles[index] = { ...this.vehicles[index], ...result.data };
        }

        this.closeEditModal();
        alert('更新成功');
      } catch (err) {
        console.error('更新错误:', err);
        alert(err.message);
      } finally {
        this.isSubmitting = false;
      }
    },

    async confirmDelete(vehicle) {
      if (confirm(`确定要删除车牌号为 "${vehicle.plateNumber}" 的车辆吗？`)) {
        try {
          const response = await fetch(`/api/vehicles/${vehicle.id}`, {
            method: 'DELETE',
            headers: {
              'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
          });

          const result = await response.json();
          if (!result.success) {
            throw new Error(result.message);
          }

          this.vehicles = this.vehicles.filter(v => v.id !== vehicle.id);
        } catch (err) {
          console.error('删除车辆错误:', err);
          alert(err.message);
        }
      }
    },

    async handleSearch() {
      try {
        if (!this.searchQuery.trim()) {
          await this.fetchVehicles();
          return;
        }

        const response = await fetch(`/api/vehicles/search?query=${encodeURIComponent(this.searchQuery)}`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });

        if (!response.ok) {
          throw new Error('搜索车辆失败');
        }

        const data = await response.json();
        this.vehicles = data;
      } catch (err) {
        console.error('搜索车辆错误:', err);
      }
    }
  },
  created() {
    this.fetchVehicles();
  }
}
</script>

<style scoped>
.vehicle-management {
  min-height: 100vh;
  background-image: linear-gradient(rgba(255, 255, 255, 0.9), rgba(255, 255, 255, 0.9)),
                    url('/vehicle-bg.jpg');
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
}

.main-content {
  padding: 2rem 0;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 2rem;
}

.management-content {
  padding: 2rem 0;
}

/* 操作区域样式 */
.actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  gap: 1rem;
}

.action-btn {
  background-color: #3182ce;
  color: white;
  padding: 0.8rem 2rem;
  border: none;
  border-radius: 0.5rem;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-btn:hover {
  background-color: #2c5282;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(49, 130, 206, 0.2);
}

/* 搜索框样式 */
.search-box {
  flex: 0 0 300px;
}

.search-box input {
  width: 100%;
  padding: 0.8rem 1.2rem;
  border: 2px solid #e2e8f0;
  border-radius: 0.5rem;
  font-size: 1rem;
  transition: all 0.3s ease;
  background-color: white;
}

.search-box input:focus {
  outline: none;
  border-color: #3182ce;
  box-shadow: 0 0 0 3px rgba(49, 130, 206, 0.1);
}

/* 车辆卡片列表样式 */
.vehicle-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 2rem;
  padding: 1rem 0;
}

.vehicle-card {
  background: white;
  border-radius: 1rem;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition: all 0.3s ease;
}

.vehicle-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.vehicle-header {
  padding: 1.5rem;
  background-color: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.vehicle-header h4 {
  margin: 0;
  color: #1a365d;
  font-size: 1.2rem;
  font-weight: 600;
}

/* 状态标签样式 */
.status-tag {
  padding: 0.4rem 1rem;
  border-radius: 2rem;
  font-size: 0.9rem;
  font-weight: 500;
  color: white;
}

.status-正常 {
  background-color: #48bb78;
}

.status-已过期 {
  background-color: #e53e3e;
}

.status-已注销 {
  background-color: #718096;
}

/* 车辆信息样式 */
.vehicle-info {
  padding: 1.5rem;
}

.vehicle-info p {
  margin: 0.8rem 0;
  color: #2d3748;
  display: flex;
  align-items: center;
  gap: 0.8rem;
}

.vehicle-info i {
  color: #3182ce;
  width: 1.2rem;
  text-align: center;
}

/* 表单样式 */
.new-vehicle-form {
  background: white;
  padding: 2rem;
  border-radius: 1rem;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  margin-bottom: 2rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #2d3748;
  font-weight: 500;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 0.8rem 1rem;
  border: 2px solid #e2e8f0;
  border-radius: 0.5rem;
  font-size: 1rem;
  background-color: white;
  transition: all 0.3s ease;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #3182ce;
  box-shadow: 0 0 0 3px rgba(49, 130, 206, 0.1);
}

/* 按钮样式 */
.card-actions {
  padding: 1.5rem;
  border-top: 1px solid #e2e8f0;
  display: flex;
  gap: 1rem;
}

.edit-btn,
.delete-btn {
  flex: 1;
  padding: 0.8rem;
  border: none;
  border-radius: 0.5rem;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.edit-btn {
  background-color: #3182ce;
  color: white;
}

.delete-btn {
  background-color: #e53e3e;
  color: white;
}

.edit-btn:hover,
.delete-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 模态框样式 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 1rem;
}

.submit-btn {
  background-color: #4CAF50;
  color: white;
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.submit-btn:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.cancel-btn {
  background-color: #f44336;
  color: white;
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: bold;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .container {
    padding: 0 1rem;
  }

  .actions {
    flex-direction: column;
  }

  .search-box {
    width: 100%;
  }

  .vehicle-cards {
    grid-template-columns: 1fr;
  }

  .vehicle-header {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }

  .card-actions {
    flex-direction: column;
  }
}

/* 添加禁用输入框的样式 */
input:disabled {
  background-color: #f8f9fa;
  cursor: not-allowed;
}

.form-text {
  font-size: 0.875rem;
  color: #6c757d;
  margin-top: 0.25rem;
}
</style> 