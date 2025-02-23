<template>
  <div class="building-management">
    <nav-bar />
    <main class="main-content">
      <div class="container">
        <div class="management-content">
          <div class="actions">
            <div class="action-group">
              <button @click="toggleNewForm" class="action-btn">
                {{ showNewForm ? '取消' : '新建楼房' }}
              </button>
              <div class="search-box">
                <input 
                  type="text" 
                  v-model="searchQuery" 
                  placeholder="搜索楼房名称..."
                  @input="handleSearch"
                />
              </div>
            </div>
          </div>
          <div v-if="showNewForm" class="new-building-form">
            <h3>新建楼房</h3>
            <form @submit.prevent="createBuilding">
              <div class="form-group">
                <label>楼房名称:</label>
                <input type="text" v-model="newBuilding.name" required />
              </div>
              <div class="form-group">
                <label>图片URL:</label>
                <input type="text" v-model="newBuilding.image" placeholder="选填" />
              </div>
              <div class="form-group">
                <label>楼层数:</label>
                <input type="number" v-model.number="newBuilding.floors" min="0" />
              </div>
              <div class="form-group">
                <label>入住人数:</label>
                <input type="number" v-model.number="newBuilding.occupancy" min="0" />
              </div>
              <div class="form-group">
                <label>总单元数:</label>
                <input type="number" v-model.number="newBuilding.total_units" min="0" />
              </div>
              <div class="form-group">
                <label>楼房状态:</label>
                <select v-model="newBuilding.status" required class="status-select">
                  <option value="">请选择状态</option>
                  <option v-for="status in buildingStatuses" 
                          :key="status.value" 
                          :value="status.value"
                          :class="status.class">
                    {{ status.label }}
                  </option>
                </select>
              </div>
              <button type="submit" class="submit-btn">提交</button>
            </form>
          </div>
          <div class="buildings-list">
            <h3>楼房列表</h3>
            <div v-if="buildings && buildings.length > 0" class="building-cards">
              <div class="building-card" v-for="building in filteredBuildings" :key="building.id">
                <div class="building-image">
                  <img v-if="building.image" :src="building.image" alt="楼房图片" />
                  <div class="building-status" :class="`status-${getStatusClass(building.status)}`">
                    {{ building.status }}
                  </div>
                </div>
                <div class="building-info">
                  <h4>{{ building.name }}</h4>
                  <div class="info-grid">
                    <div class="info-item">
                      <i class="fas fa-building"></i>
                      <span>{{ building.floors }}层</span>
                    </div>
                    <div class="info-item">
                      <i class="fas fa-home"></i>
                      <span>{{ building.total_units }}户</span>
                    </div>
                    <div class="info-item">
                      <i class="fas fa-users"></i>
                      <span>入住{{ building.occupancy }}户</span>
                    </div>
                    <div class="info-item">
                      <i class="fas fa-percentage"></i>
                      <span>入住率{{ building.occupancy_rate }}</span>
                    </div>
                  </div>
                  <div class="occupancy-bar">
                    <div class="occupancy-progress" 
                         :style="{ width: building.occupancy_rate }"
                         :class="{'high-occupancy': parseInt(building.occupancy_rate) > 80}">
                    </div>
                  </div>
                </div>
                <div class="card-actions">
                  <button @click="editBuilding(building)" class="edit-btn">
                    <i class="fas fa-edit"></i> 编辑
                  </button>
                  <button @click="confirmDelete(building)" class="delete-btn">
                    <i class="fas fa-trash"></i> 删除
                  </button>
                </div>
              </div>
            </div>
            <div v-else-if="isLoading" class="loading-state">
              加载中...
            </div>
            <div v-else class="empty-state">
              暂无楼房数据
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 编辑楼房的弹窗 -->
    <div v-if="showEditModal && editingBuilding" class="modal">
      <div class="modal-content">
        <h3>编辑楼房信息</h3>
        <form @submit.prevent="updateBuilding">
          <div class="form-group">
            <label>楼房名称:</label>
            <input type="text" v-model="editingBuilding.name" required />
          </div>
          <div class="form-group">
            <label>图片URL:</label>
            <input type="text" v-model="editingBuilding.image" placeholder="选填" />
          </div>
          <div class="form-group">
            <label>楼层数:</label>
            <input type="number" v-model.number="editingBuilding.floors" min="0" />
          </div>
          <div class="form-group">
            <label>入住人数:</label>
            <input type="number" v-model.number="editingBuilding.occupancy" min="0" />
          </div>
          <div class="form-group">
            <label>总单元数:</label>
            <input type="number" v-model.number="editingBuilding.total_units" min="0" />
          </div>
          <div class="form-group">
            <label>楼房状态:</label>
            <select v-model="editingBuilding.status" required class="status-select">
              <option value="">请选择状态</option>
              <option v-for="status in buildingStatuses" 
                      :key="status.value" 
                      :value="status.value"
                      :class="status.class">
                {{ status.label }}
              </option>
            </select>
          </div>
          <div class="modal-actions">
            <button type="submit" class="submit-btn">保存</button>
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
  name: 'BuildingManagement',
  components: {
    NavBar
  },
  data() {
    return {
      buildings: [],
      isLoading: true,
      showNewForm: false,
      showEditModal: false,
      searchQuery: '',
      buildingStatuses: [
        { value: '正常', label: '正常', class: 'status-normal' },
        { value: '危楼', label: '危楼', class: 'status-danger' },
        { value: '建筑中', label: '建筑中', class: 'status-construction' },
        { value: '装修中', label: '装修中', class: 'status-renovation' }
      ],
      newBuilding: {
        name: '',
        image: '',
        floors: 0,
        occupancy: 0,
        total_units: 0,
        status: ''
      },
      editingBuilding: null,
      error: ''
    }
  },
  computed: {
    filteredBuildings() {
      if (!this.buildings) return [];
      if (!this.searchQuery) return this.buildings;
      
      const query = this.searchQuery.toLowerCase().trim();
      return this.buildings.filter(building => 
        building.name.toLowerCase().includes(query)
      );
    }
  },
  methods: {
    async fetchBuildings() {
      this.isLoading = true;
      try {
        const response = await fetch('/api/buildings', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });

        const result = await response.json();
        if (!result.success) {
          throw new Error(result.message);
        }

        this.buildings = result.data;
      } catch (err) {
        console.error('获取楼房列表错误:', err);
        this.error = err.message;
      } finally {
        this.isLoading = false;
      }
    },
    toggleNewForm() {
      this.showNewForm = !this.showNewForm;
      if (!this.showNewForm) {
        this.resetNewBuildingForm();
      }
    },
    async createBuilding() {
      try {
        const response = await fetch('/api/buildings', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          body: JSON.stringify(this.newBuilding)
        });

        if (!response.ok) {
          const data = await response.json();
          throw new Error(data || '创建楼房失败');
        }

        const data = await response.json();
        if (data.success) {
          this.buildings.push(data.data);
        } else {
          throw new Error(data.message || '创建楼房失败');
        }
        
        this.showNewForm = false;
        this.resetNewBuildingForm();
      } catch (err) {
        console.error('创建楼房错误:', err);
        alert(err.message);
      }
    },
    handleLogout() {
      localStorage.removeItem('token');
      this.$router.push('/login');
    },
    async handleSearch() {
      try {
        if (!this.searchQuery.trim()) {
          await this.fetchBuildings();
          return;
        }

        const response = await fetch(`/api/buildings/search?query=${encodeURIComponent(this.searchQuery.trim())}`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });

        const result = await response.json();
        if (!result.success) {
          throw new Error(result.message || '搜索楼房失败');
        }

        this.buildings = result.data;
      } catch (err) {
        console.error('搜索楼房错误:', err);
      }
    },
    editBuilding(building) {
      this.editingBuilding = { ...building };
      this.showEditModal = true;
    },
    closeEditModal() {
      this.showEditModal = false;
      this.editingBuilding = null;
    },
    async updateBuilding() {
      try {
        const response = await fetch(`/api/buildings/${this.editingBuilding.id}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          body: JSON.stringify(this.editingBuilding)
        });

        if (!response.ok) {
          const data = await response.json();
          throw new Error(data || '更新楼房失败');
        }

        const updatedBuilding = await response.json();
        const index = this.buildings.findIndex(b => b.id === updatedBuilding.id);
        if (index !== -1) {
          this.buildings[index] = updatedBuilding;
        }
        this.closeEditModal();
      } catch (err) {
        console.error('更新楼房错误:', err);
        alert(err.message);
      }
    },
    async confirmDelete(building) {
      if (confirm(`确定要删除楼房 "${building.name}" 吗？`)) {
        try {
          const response = await fetch(`/api/buildings/${building.id}`, {
            method: 'DELETE',
            headers: {
              'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
          });

          if (!response.ok) {
            throw new Error('删除楼房失败');
          }

          this.buildings = this.buildings.filter(b => b.id !== building.id);
        } catch (err) {
          console.error('删除楼房错误:', err);
          alert(err.message);
        }
      }
    },
    getStatusClass(status) {
      switch(status) {
        case '正常':
          return 'normal';
        case '危楼':
          return 'danger';
        case '建筑中':
          return 'construction';
        case '装修中':
          return 'renovation';
        default:
          return 'normal';
      }
    },
    resetNewBuildingForm() {
      this.newBuilding = {
        name: '',
        image: '',
        floors: 0,
        occupancy: 0,
        total_units: 0,
        status: ''
      };
    }
  },
  created() {
    this.fetchBuildings();
  }
}
</script>

<style scoped>
.building-management {
  min-height: 100vh;
  background-image: linear-gradient(rgba(255, 255, 255, 0.9), rgba(255, 255, 255, 0.9)),
                    url('/building-bg.jpg');
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
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 1rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  padding: 2rem;
}

.actions {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  margin-bottom: 2rem;
}

.action-group {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.action-btn {
  background-color: #3182ce;
  color: #fff;
  padding: 0.8rem 1.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: transform 0.2s, background-color 0.2s;
}

.action-btn:hover {
  background-color: #2c5282;
  transform: translateY(-2px);
}

.search-box {
  width: 300px;
}

.search-box input {
  width: 100%;
  padding: 0.8rem 1.2rem;
  border: 2px solid #e2e8f0;
  border-radius: 0.5rem;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.search-box input:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}

.new-building-form {
  margin-bottom: 2rem;
  padding: 1.5rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background-color: #f7fafc;
}

.new-building-form h3 {
  margin-bottom: 1rem;
  color: #1a365d;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #2d3748;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  font-size: 1rem;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.form-group input:focus {
  outline: none;
  border-color: #3182ce;
  box-shadow: 0 0 0 2px rgba(49,130,206,0.2);
}

.submit-btn {
  background-color: #3182ce;
  color: #fff;
  padding: 0.8rem 2rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: transform 0.2s, background-color 0.2s;
}

.submit-btn:hover {
  background-color: #2c5282;
  transform: translateY(-2px);
}

.buildings-list h3 {
  margin-bottom: 1rem;
  color: #1a365d;
}

.building-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 2rem;
  padding: 1rem 0;
}

.building-card {
  background: white;
  border-radius: 1rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  transition: all 0.3s ease;
}

.building-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.building-image {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.building-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.building-card:hover .building-image img {
  transform: scale(1.05);
}

.building-status {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
  color: white;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.status-normal {
  background-color: #52c41a;
}

.status-danger {
  background-color: #ff4d4f;
}

.status-construction {
  background-color: #1890ff;
}

.status-renovation {
  background-color: #faad14;
}

.building-info {
  padding: 1.5rem;
}

.building-info h4 {
  font-size: 1.2rem;
  margin-bottom: 1rem;
  color: #1a1a1a;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
  margin-bottom: 1rem;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #666;
}

.info-item i {
  color: #1890ff;
  font-size: 1.1rem;
}

.occupancy-bar {
  height: 6px;
  background-color: #f0f0f0;
  border-radius: 3px;
  overflow: hidden;
  margin: 1rem 0;
}

.occupancy-progress {
  height: 100%;
  background-color: #1890ff;
  border-radius: 3px;
  transition: width 0.3s ease;
}

.high-occupancy {
  background-color: #52c41a;
}

.card-actions {
  display: flex;
  gap: 1rem;
  padding: 1.5rem;
  border-top: 1px solid #e2e8f0;
}

.card-actions button {
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

.edit-btn:hover {
  background-color: #2c5282;
}

.delete-btn:hover {
  background-color: #c53030;
}

.edit-btn i,
.delete-btn i {
  font-size: 1rem;
}

.edit-btn:active,
.delete-btn:active {
  transform: translateY(0);
  box-shadow: none;
}

@media (max-width: 768px) {
  .container {
    padding: 0 1rem;
  }

  .building-cards {
    grid-template-columns: 1fr;
  }

  .management-content {
    padding: 1.5rem;
  }

  .building-image {
    height: 180px;
  }

  .action-group {
    flex-direction: column;
    width: 100%;
  }

  .search-box {
    width: 100%;
  }

  .card-actions {
    flex-direction: column;
  }

  .card-actions button {
    width: 100%;
  }
}

/* 模态框样式 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 2rem;
  border-radius: 0.8rem;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  margin-top: 2rem;
}

/* 状态选择下拉框样式 */
.status-select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  font-size: 1rem;
  background-color: white;
  cursor: pointer;
  transition: all 0.3s ease;
}

.status-select:focus {
  outline: none;
  border-color: #3182ce;
  box-shadow: 0 0 0 2px rgba(49,130,206,0.2);
}

.status-select option {
  padding: 0.5rem;
  background-color: white;
}

/* 状态标签样式 */
.status-select option.status-normal {
  color: #52c41a;
  background-color: white;
}

.status-select option.status-danger {
  color: #ff4d4f;
  background-color: white;
}

.status-select option.status-construction {
  color: #1890ff;
  background-color: white;
}

.status-select option.status-renovation {
  color: #faad14;
  background-color: white;
}

/* 更新楼房卡片中的状态标签样式 */
.building-status {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
  color: white;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.building-status.status-normal {
  background-color: #52c41a;
}

.building-status.status-danger {
  background-color: #ff4d4f;
}

.building-status.status-construction {
  background-color: #1890ff;
}

.building-status.status-renovation {
  background-color: #faad14;
}

/* 移除之前的重复样式 */
.status-normal,
.status-danger,
.status-construction,
.status-renovation {
  background-color: transparent;
}

/* 添加加载和空状态样式 */
.loading-state,
.empty-state {
  text-align: center;
  padding: 2rem;
  color: #666;
  font-size: 1.1rem;
}

.loading-state {
  background: rgba(255, 255, 255, 0.8);
}

.empty-state {
  background: #f8f9fa;
  border-radius: 8px;
}
</style>
