<template>
  <div class="parking-management">
    <nav-bar />
    <main class="main-content">
      <div class="container">
        <div class="management-content">
          <div class="actions">
            <div class="action-group">
              <button @click="toggleNewForm" class="action-btn">
                {{ showNewForm ? '取消' : '新增车位' }}
              </button>
              <div class="search-box">
                <input 
                  type="text" 
                  v-model="searchQuery" 
                  placeholder="搜索车位号..."
                  @input="handleSearch"
                />
              </div>
            </div>
          </div>

          <!-- 新增车位表单 -->
          <div v-if="showNewForm" class="new-parking-form">
            <h3>新增车位信息</h3>
            <form @submit.prevent="createParkingSpace">
              <!-- 楼房和车型分组 -->
              <div class="form-row">
                <div class="form-group">
                  <label>所属楼房: <span class="required">*</span></label>
                  <select v-model="newParking.buildingId" required>
                    <option value="">请选择楼房</option>
                    <option v-for="building in buildings" :key="building.id" :value="building.id">
                      {{ building.name }}
                    </option>
                  </select>
                </div>
                <div class="form-group">
                  <label>可停车型:</label>
                  <select v-model="newParking.allowedVehicleType" 
                          @change="handleVehicleTypeChange" 
                          required>
                    <option value="">请选择</option>
                    <option v-for="type in getAllowedVehicleTypes()" 
                            :key="type.value" 
                            :value="type.value">
                      {{ type.label }}
                    </option>
                  </select>
                </div>
                <div class="form-group" v-if="showChargingPileOption">
                  <label>充电桩:</label>
                  <select v-model="newParking.chargingPile">
                    <option value="无">无</option>
                    <option value="有">有</option>
                  </select>
                </div>
              </div>

              <!-- 位置信息分组 -->
              <div class="form-row">
                <div class="form-group">
                  <label>位置类型:</label>
                  <select v-model="newParking.locationType" 
                          @change="handleLocationTypeChange" 
                          required>
                    <option value="">请选择位置类型</option>
                    <option value="地上">地上</option>
                    <option value="地下">地下</option>
                  </select>
                </div>

                <!-- 地上车位选项 -->
                <div v-if="newParking.locationType === '地上'" class="form-group">
                  <label>方向:</label>
                  <select v-model="newParking.groundDirection" 
                          @change="handleLocationChange" 
                          required>
                    <option value="">请选择方向</option>
                    <option value="北面">北面</option>
                    <option value="南面">南面</option>
                    <option value="东面">东面</option>
                    <option value="西面">西面</option>
                  </select>
                </div>

                <!-- 地下车位选项 -->
                <div v-if="newParking.locationType === '地下'" class="form-group">
                  <label>地下层数:</label>
                  <select v-model="newParking.undergroundLevel" 
                          @change="handleLocationChange" 
                          required>
                    <option value="">请选择层数</option>
                    <option value="1">地下一层</option>
                    <option value="2">地下二层</option>
                    <option value="3">地下三层</option>
                  </select>
                </div>
              </div>

              <!-- 车位编号和类型分组 -->
              <div class="form-row">
                <div class="form-group">
                  <label>车位编号:</label>
                  <input type="text" 
                         v-model="newParking.spaceNumber" 
                         readonly 
                         required />
                  <small class="form-text text-muted">
                    车位编号将根据位置自动生成
                  </small>
                </div>
                <div class="form-group">
                  <label>车位类型: <span class="required">*</span></label>
                  <select v-model="newParking.type" required>
                    <option value="">请选择</option>
                    <option value="出售">出售</option>
                    <option value="出租">出租</option>
                    <option value="免费">免费</option>
                  </select>
                </div>
              </div>

              <!-- 价格和面积分组 -->
              <div class="form-row">
                <div class="form-group">
                  <label>价格:</label>
                  <div class="price-input-group">
                    <input 
                      type="number" 
                      v-model="newParking.price" 
                      step="0.01"
                      :disabled="newParking.type === '免费'"
                      :required="newParking.type !== '免费'"
                    />
                    <span class="price-unit">{{ priceUnit }}</span>
                  </div>
                </div>
                <div class="form-group">
                  <label>面积(平方米):</label>
                  <input type="number" v-model="newParking.area" step="0.01" required />
                </div>
              </div>

              <!-- 描述单独一行，但宽度受控 -->
              <div class="form-row">
                <div class="form-group full-width">
                  <label>描述:</label>
                  <textarea v-model="newParking.description"></textarea>
                </div>
              </div>

              <button type="submit" class="submit-btn">提交</button>
            </form>
          </div>

          <!-- 筛选区域 -->
          <div class="filters">
            <!-- 车位类型筛选 -->
            <div class="filter-group">
              <label>车位类型:</label>
              <select v-model="currentCategory">
                <option v-for="category in categories" 
                        :key="category.value" 
                        :value="category.value">
                  {{ category.label }}
                </option>
              </select>
            </div>

            <!-- 可停车辆类型筛选 -->
            <div class="filter-group">
              <label>可停车辆类型:</label>
              <select v-model="currentVehicleType">
                <option v-for="type in vehicleTypes" 
                        :key="type.value" 
                        :value="type.value">
                  {{ type.label }}
                </option>
              </select>
            </div>

            <!-- 状态筛选 -->
            <div class="filter-group">
              <label>状态:</label>
              <select v-model="currentStatus">
                <option v-for="status in statusTypes" 
                        :key="status.value" 
                        :value="status.value">
                  {{ status.label }}
                </option>
              </select>
            </div>
          </div>

          <!-- 车位列表 -->
          <div class="parking-list">
            <h3>车位列表</h3>
            <div class="parking-cards">
              <div v-for="parking in filteredParkingSpaces" :key="parking.id" class="parking-card">
                <div class="card-header">
                  <h3>车位编号: {{ parking.spaceNumber }}</h3>
                </div>
                
                <div class="card-content">
                  <div class="location-info">
                    <span class="label">所属楼房:</span>
                    <span class="value">{{ parking.buildingName || getBuildingName(parking.buildingId) }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">类型:</span>
                    <span class="value">{{ parking.type }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">可停车型:</span>
                    <span class="value">{{ parking.allowedVehicleType }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">价格:</span>
                    <span class="value">{{ parking.price ? `¥${parking.price}${parking.type === '出租' ? '/月' : ''}` : '免费' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">充电桩:</span>
                    <span class="value">{{ parking.chargingPile || '无' }}</span>
                  </div>
                </div>

                <div class="card-actions">
                  <button 
                    v-if="canRent(parking)"
                    @click="showRentDialog(parking)"
                    class="action-button rent"
                  >
                    {{ parking.type === '免费' ? '停车' : '租赁' }}
                  </button>
                  <button 
                    v-if="parking.status === '空闲' && parking.type === '出售'"
                    @click="showSellDialog(parking)"
                    class="action-button sell"
                  >
                    出售
                  </button>
                  <button 
                    v-if="parking.status === '已占用' && (parking.type === '出租' || parking.type === '免费')"
                    @click="handleEndRent(parking)"
                    :class="['action-button', 'end-rent', { 'free': parking.type === '免费' }]"
                  >
                    {{ parking.type === '免费' ? '结束停车' : '结束出租' }}
                  </button>
                  <button 
                    @click="openEditDialog(parking)"
                    class="action-button edit"
                    :disabled="!canEdit(parking)"
                  >
                    编辑
                  </button>
                  <button 
                    @click="confirmDelete(parking)"
                    class="action-button delete"
                    :disabled="!canDelete(parking)"
                    :title="!canDelete(parking) ? '只能删除空闲状态的车位' : ''"
                  >
                    <i class="fas fa-trash"></i> 删除
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 租赁对话框 -->
    <div v-if="showRentForm" class="modal">
      <div class="modal-content">
        <h3>车位租赁</h3>
        <form @submit.prevent="handleRent">
          <div class="form-group">
            <label>选择车辆:</label>
            <select v-model="rentForm.vehicleId" required>
              <option value="">请选择车辆</option>
              <option v-for="vehicle in filteredAvailableVehicles" 
                      :key="vehicle.id" 
                      :value="vehicle.id">
                {{ vehicle.plateNumber }} - {{ vehicle.vehicleType }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>开始日期:</label>
            <input type="date" v-model="rentForm.startDate" required>
          </div>
          <div class="form-group">
            <label>结束日期:</label>
            <input type="date" v-model="rentForm.endDate" 
                   :min="rentForm.startDate" required>
          </div>
          <div class="modal-actions">
            <button type="submit" class="submit-btn">确认租赁</button>
            <button type="button" @click="showRentForm = false" class="cancel-btn">取消</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 编辑车位模态框 -->
    <div v-if="showEditModal" class="modal">
      <div class="modal-content">
        <h3>编辑车位信息</h3>
        <form @submit.prevent="handleEditSubmit">
          <!-- 所属楼房选择 -->
          <div class="form-group">
            <label>所属楼房:</label>
            <select v-model="editingParking.buildingId" required>
              <option value="">请选择楼房</option>
              <option v-for="building in buildings" :key="building.id" :value="building.id">
                {{ building.name }}
              </option>
            </select>
          </div>

          <!-- 车位类型 -->
          <div class="form-group">
            <label>车位类型:</label>
            <select v-model="editingParking.type" required>
              <option value="出售">出售</option>
              <option value="出租">出租</option>
              <option value="免费">免费</option>
            </select>
          </div>

          <!-- 价格设置 -->
          <div class="form-group" v-if="editingParking.type !== '免费'">
            <label>价格:</label>
            <div class="price-input-group">
              <input 
                type="number" 
                v-model="editingParking.price" 
                step="0.01"
                required
              />
              <span class="price-unit">{{ editingParking.type === '出租' ? '元/月' : '元' }}</span>
            </div>
          </div>

          <!-- 可停车型 -->
          <div class="form-group">
            <label>可停车型:</label>
            <select v-model="editingParking.allowedVehicleType" required>
              <option v-for="type in getAllowedVehicleTypes()" 
                      :key="type.value" 
                      :value="type.value">
                {{ type.label }}
              </option>
            </select>
          </div>

          <!-- 充电桩选项 -->
          <div class="form-group" v-if="showChargingPileOptionInEdit">
            <label>充电桩:</label>
            <select v-model="editingParking.chargingPile">
              <option value="无">无</option>
              <option value="有">有</option>
            </select>
          </div>

          <!-- 面积 -->
          <div class="form-group">
            <label>面积(平方米):</label>
            <input type="number" v-model="editingParking.area" step="0.01" required />
          </div>

          <!-- 描述 -->
          <div class="form-group">
            <label>描述:</label>
            <textarea v-model="editingParking.description"></textarea>
          </div>

          <!-- 状态字段设置为只读 -->
          <div class="form-group">
            <label>状态:</label>
            <input type="text" v-model="editingParking.status" readonly />
          </div>

          <div class="modal-actions">
            <button type="submit">保存</button>
            <button type="button" @click="closeEditDialog">取消</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 出售表单对话框 -->
    <div v-if="showSellForm" class="modal">
      <div class="modal-content">
        <h3>车位出售</h3>
        <form @submit.prevent="handleSell">
          <div class="form-group">
            <label>选择车辆:</label>
            <select v-model="sellForm.vehicleId" required>
              <option value="">请选择车辆</option>
              <option v-for="vehicle in filteredAvailableVehicles" 
                      :key="vehicle.id" 
                      :value="vehicle.id">
                {{ vehicle.plateNumber }} - {{ vehicle.vehicleType }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>出售价格:</label>
            <input type="number" v-model="sellForm.price" required>
          </div>
          <div class="form-group">
            <label>交易日期:</label>
            <input type="date" v-model="sellForm.date" required>
          </div>
          <div class="modal-actions">
            <button type="submit" class="submit-btn">确认出售</button>
            <button type="button" @click="showSellForm = false" class="cancel-btn">取消</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import NavBar from './NavBar.vue'

export default {
  name: 'ParkingManagement',
  components: {
    NavBar
  },
  data() {
    return {
      parkingSpaces: [],
      vehicles: [],
      showNewForm: false,
      showRentForm: false,
      showEditModal: false,
      showSellForm: false,
      searchQuery: '',
      selectedParking: null,
      currentCategory: 'all',
      currentVehicleType: 'all',
      currentStatus: 'all',
      buildings: [],
      newParking: {
        spaceNumber: '',
        buildingId: '',
        allowedVehicleType: '',
        chargingPile: '无',
        locationType: '',
        groundDirection: '',
        undergroundLevel: '',
        type: '',
        price: null,
        area: null,
        description: '',
        status: '空闲'
      },
      rentForm: {
        parkingId: null,
        vehicleId: '',
        startDate: '',
        endDate: ''
      },
      editingParking: null,
      isSubmitting: false,
      availableVehicles: [],
      error: '',
      categories: [
        { label: '全部', value: 'all' },
        { label: '出售', value: '出售' },
        { label: '出租', value: '出租' },
        { label: '免费', value: '免费' }
      ],
      vehicleTypes: [
        { label: '全部', value: 'all' },
        { label: '小型汽车', value: '小型汽车' },
        { label: '电动汽车', value: '电动汽车' },
        { label: '电瓶车', value: '电瓶车' },
        { label: '摩托车', value: '摩托车' },
        { label: '自行车', value: '自行车' }
      ],
      statusTypes: [
        { label: '全部', value: 'all' },
        { label: '空闲', value: '空闲' },
        { label: '已占用', value: '已占用' },
        { label: '已售出', value: '已售出' },
        { label: '维护中', value: '维护中' }
      ],
      sellForm: {
        parkingId: null,
        vehicleId: '',
        price: 0,
        date: ''
      },
      today: new Date().toISOString().split('T')[0],
      vehicleCategories: {
        small: ['电动车', '摩托车'],  // 小型车辆类型
        large: ['小型汽车']           // 大型车辆类型
      },
      parkingVehicleTypes: {
        small: [  // 小型车位可停车型
          { value: '摩托车', label: '摩托车' },
          { value: '电瓶车', label: '电瓶车' },
          { value: '自行车', label: '自行车' }
        ],
        large: [  // 大型车位可停车型
          { value: '油车', label: '油车' },
          { value: '电动汽车', label: '电动汽车' }
        ]
      },
      loading: false
    }
  },
  computed: {
    showChargingPileOption() {
      return ['电动汽车', '电瓶车'].includes(this.newParking.allowedVehicleType)
    },
    filteredParkingSpaces() {
      let filtered = [...this.parkingSpaces];
      
      // 搜索过滤
      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase();
        filtered = filtered.filter(parking => 
          parking.spaceNumber.toLowerCase().includes(query) ||
          parking.location.toLowerCase().includes(query)
        );
      }
      
      // 车位类型过滤
      if (this.currentCategory !== 'all') {
        filtered = filtered.filter(parking => parking.type === this.currentCategory);
      }
      
      // 可停车辆类型过滤
      if (this.currentVehicleType !== 'all') {
        filtered = filtered.filter(parking => {
          // 检查允许的车辆类型是否匹配
          return parking.allowedVehicleType === this.currentVehicleType;
        });
      }
      
      // 状态过滤
      if (this.currentStatus !== 'all') {
        filtered = filtered.filter(parking => parking.status === this.currentStatus);
      }
      
      return filtered.map(parking => {
        // 从位置信息中提取楼房ID
        let bid = null;
        if (parking.location) {
          // 提取位置信息中的数字部分作为楼房ID
          const match = parking.location.match(/^(\d+)/);
          if (match) {
            bid = parseInt(match[1]);
          }
        }
        
        console.log('车位信息:', parking);
        console.log('从位置信息提取的楼房ID:', bid);
        
        return {
          ...parking,
          buildingName: this.getBuildingName(bid)
        };
      });
    },
    priceUnit() {
      return this.newParking.type === '出售' ? '元' : '元/月';
    },
    filteredAvailableVehicles() {
      if (!this.selectedParking || !this.availableVehicles) return [];
      
      const filtered = this.availableVehicles.filter(vehicle => 
        this.canParkVehicle(vehicle, this.selectedParking)
      );
      
      console.log('过滤后的车辆列表:', filtered);
      return filtered;
    },
    isVehicleTypeMatched(vehicle, parking) {
      if (!vehicle || !parking) return false;
      
      // 判断车位类型
      const isSmallParking = ['摩托车', '电瓶车', '自行车'].includes(parking.allowedVehicleType);
      const isLargeParking = ['油车', '电动汽车'].includes(parking.allowedVehicleType);
      
      // 判断车辆类型匹配
      if (isSmallParking) {
        return this.vehicleCategories.small.includes(vehicle.vehicle_type);
      }
      if (isLargeParking) {
        return this.vehicleCategories.large.includes(vehicle.vehicle_type);
      }
      return false;
    },
    showChargingPileOptionInEdit() {
      return this.editingParking && 
             (this.editingParking.allowedVehicleType === '电动汽车' || 
              this.editingParking.allowedVehicleType === '电瓶车');
    }
  },
  methods: {
    // 获取所有车位
    async fetchParkingSpaces() {
      try {
        const response = await fetch('/api/parking-spaces', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });

        if (!response.ok) {
          throw new Error('获取车位列表失败');
        }

        const result = await response.json();
        if (!result.success) {
          throw new Error(result.message);
        }

        this.parkingSpaces = result.data;
        console.log('API返回的原始车位数据:', this.parkingSpaces);

        // 处理每个车位的楼房信息
        for (const parking of this.parkingSpaces) {
          const buildingId = this.extractBuildingId(parking.location);
          console.log('车位信息:', parking);
          console.log('从位置信息提取的楼房ID:', buildingId);
          if (buildingId) {
            const building = this.buildings.find(b => b.id === buildingId);
            if (building) {
              parking.buildingName = building.name;
            }
          }
        }
      } catch (err) {
        console.error('获取车位列表错误:', err);
        this.error = err.message;
      }
    },

    // 创建新车位
    async createParkingSpace() {
      try {
        // 验证必填字段
        if (!this.newParking.buildingId || !this.newParking.spaceNumber) {
          alert('请填写所有必填项');
          return;
        }

        // 检查车位编号在同一楼房内是否已存在
        const isDuplicate = this.parkingSpaces.some(space => 
          space.buildingId === this.newParking.buildingId && 
          space.spaceNumber === this.newParking.spaceNumber
        );

        if (isDuplicate) {
          alert('该楼房内已存在相同车位编号');
          return;
        }

        // 构造请求数据
        const parkingData = {
          ...this.newParking,
          building: this.newParking.buildingId, // 添加building字段
          location: this.generateLocation(), // 生成位置信息
          status: '空闲', // 设置初始状态
          type: this.newParking.type || '出售' // 设置默认类型
        };

        const response = await fetch('/api/parking-spaces', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          body: JSON.stringify(parkingData)
        });

        const result = await response.json();
        if (!result.success) {
          throw new Error(result.message || '创建车位失败');
        }

        await this.fetchParkingSpaces();
        this.showNewForm = false;
        this.resetNewParkingForm();
        alert('创建车位成功');
      } catch (err) {
        console.error('创建车位错误:', err);
        alert(err.message);
      }
    },

    // 更新车位信息
    async updateParkingSpace() {
      try {
        if (!this.validateForm(this.editingParking)) {
          return;
        }

        const response = await fetch(`/api/parking-spaces/${this.editingParking.id}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          body: JSON.stringify(this.editingParking)
        });

        const result = await response.json();
        if (!result.success) {
          throw new Error(result.message);
        }

        // 更新本地数据
        const index = this.parkingSpaces.findIndex(s => s.id === this.editingParking.id);
        if (index !== -1) {
          this.parkingSpaces[index] = result.data;
        }

        this.closeEditModal();
        alert('更新成功');
      } catch (err) {
        console.error('更新车位错误:', err);
        alert(err.message);
      }
    },

    // 删除车位
    async confirmDelete(parking) {
      try {
        // 检查车位状态
        if (parking.status !== '空闲') {
          alert('只能删除空闲状态的车位');
          return;
        }

        // 确认删除
        if (!confirm(`确定要删除车位 ${parking.spaceNumber} 吗？`)) {
          return;
        }

        const response = await fetch(`/api/parking-spaces/${parking.id}`, {
          method: 'DELETE',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });

        const result = await response.json();
        if (!result.success) {
          throw new Error(result.message);
        }

        // 删除成功后刷新列表
        await this.fetchParkingSpaces();
        alert('删除成功');
      } catch (err) {
        console.error('删除车位错误:', err);
        alert(err.message || '删除失败');
      }
    },

    // 判断车位是否可以删除
    canDelete(parking) {
      return parking.status === '空闲';
    },

    // 搜索车位
    async handleSearch() {
      try {
        let url = '/api/parking-spaces/search?';
        const params = new URLSearchParams();

        if (this.searchQuery) {
          params.append('query', this.searchQuery);
        }
        if (this.filterStatus) {
          params.append('status', this.filterStatus);
        }

        const response = await fetch(`${url}${params}`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });

        const result = await response.json();
        if (!result.success) {
          throw new Error(result.message);
        }

        this.parkingSpaces = result.data;
      } catch (err) {
        console.error('搜索车位错误:', err);
        alert(err.message);
      }
    },

    // 表单验证
    validateForm(parking) {
      // 检查所属楼房
      if (!parking.buildingId) {
        alert('请选择所属楼房');
        return false;
      }

      // 检查位置类型和相关信息
      if (!parking.locationType) {
        alert('请选择位置类型');
        return false;
      }

      if (parking.locationType === '地上' && !parking.groundDirection) {
        alert('请选择地上方向');
        return false;
      }

      if (parking.locationType === '地下' && !parking.undergroundLevel) {
        alert('请选择地下层数');
        return false;
      }

      // 检查车位编号
      if (!parking.spaceNumber) {
        alert('请等待系统生成车位编号');
        return false;
      }

      // 检查车位类型
      if (!parking.type) {
        alert('请选择车位类型');
        return false;
      }

      // 检查可停车辆类型
      if (!parking.allowedVehicleType) {
        alert('请选择可停车辆类型');
        return false;
      }

      // 检查价格（如果是出售或出租类型）
      if ((parking.type === '出售' || parking.type === '出租') && 
          (parking.price === null || parking.price === undefined || parking.price <= 0)) {
        alert('请输入有效的价格');
        return false;
      }

      // 检查面积
      if (parking.area !== null && parking.area !== undefined && parking.area <= 0) {
        alert('请输入有效的面积');
        return false;
      }

      return true;
    },

    // 重置表单
    resetNewParkingForm() {
      this.newParking = {
        spaceNumber: '',
        buildingId: '',
        allowedVehicleType: '',
        chargingPile: '无',
        locationType: '',
        groundDirection: '',
        undergroundLevel: '',
        type: '出售',
        price: null,
        area: null,
        description: '',
        status: '空闲'
      };
    },

    // 打开编辑模态框
    openEditDialog(parking) {
      this.editingParking = {
        id: parking.id,
        spaceNumber: parking.spaceNumber,
        buildingId: this.extractBuildingId(parking.location),
        allowedVehicleType: parking.allowedVehicleType,
        chargingPile: parking.chargingPile || '无',
        locationType: this.getLocationType(parking.location),
        type: parking.type,
        price: parking.price,
        area: parking.area,
        description: parking.description,
        status: parking.status,
        location: parking.location
      };
      this.showEditModal = true;
    },

    // 关闭编辑模态框
    closeEditDialog() {
      this.showEditModal = false;
      this.editingParking = null;
    },

    // 获取可用车辆列表
    async fetchAvailableVehicles() {
      try {
        const response = await fetch('/api/vehicles', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });

        const result = await response.json();
        if (!result.success) {
          throw new Error(result.message);
        }

        // 不再过滤parkingSpace，因为后端可能没有这个字段
        this.availableVehicles = result.data;
        console.log('获取到的所有车辆:', result.data);
      } catch (err) {
        console.error('获取可用车辆列表失败:', err);
        alert(err.message);
      }
    },

    // 获取楼房列表
    async fetchBuildings() {
      this.loading = true;
      try {
        const response = await fetch('/api/buildings', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });
        const result = await response.json();
        if (result.success) {
          this.buildings = result.data;
          console.log('获取到的楼房列表:', this.buildings);
        } else {
          console.error('获取楼房列表失败:', result.message);
        }
      } catch (error) {
        console.error('获取楼房列表错误:', error);
      } finally {
        this.loading = false;
      }
    },

    // 获取楼房名称的方法
    getBuildingName(buildingId) {
      console.log('查找楼房ID:', buildingId);
      console.log('可用楼房列表:', this.buildings);
      
      if (buildingId) {
        const building = this.buildings.find(b => {
          const match = Number(b.id) === Number(buildingId);
          console.log(`比较: ${b.id} vs ${buildingId}, 结果: ${match}`);
          return match;
        });
        return building ? building.name : '未分配';
      }
      return '未分配';
    },

    // 获取方向名称
    getDirectionName(direction) {
      const directions = {
        'N': '北面',
        'S': '南面',
        'E': '东面',
        'W': '西面'
      };
      return directions[direction] || '';
    },

    // 获取地下层数
    getUndergroundLevel(level) {
      return level ? `地下${level}层` : '';
    },

    // 显示租赁对话框
    showRentDialog(parking) {
      console.log('选中的车位信息:', parking);
      console.log('车位允许的车辆类型:', parking.allowedVehicleType);
      
      this.selectedParking = parking;
      this.rentForm = {
        parkingId: parking.id,
        vehicleId: '',
        startDate: new Date().toISOString().split('T')[0],
        endDate: parking.type === '免费' ? '' : new Date().toISOString().split('T')[0]
      };
      this.showRentForm = true;
      
      // 先获取车辆列表
      this.fetchAvailableVehicles().then(() => {
        console.log('可用车辆列表更新后:', this.availableVehicles);
        this.availableVehicles.forEach(vehicle => {
          const canPark = this.canParkVehicle(vehicle, parking);
          console.log(`车辆 ${vehicle.plateNumber} (${vehicle.vehicleType}) 是否可停放: ${canPark}`);
        });
        console.log('过滤后的车辆列表:', this.filteredAvailableVehicles);
      });
    },

    // 关闭租赁对话框
    closeRentDialog() {
      this.showRentForm = false;
      this.rentForm = {
        parkingId: null,
        vehicleId: '',
        startDate: '',
        endDate: ''
      };
    },

    // 处理租赁
    async handleRent() {
      try {
        // 调用租赁API
        const response = await fetch('/api/parking-transactions/rent', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          body: JSON.stringify(this.rentForm)
        });

        const result = await response.json();
        if (result.success) {
          // 更新车辆信息中的停车位
          await this.updateVehicleParkingSpace(
            this.rentForm.vehicleId, 
            this.selectedParking.spaceNumber
          );
          
          this.showRentForm = false;
          this.fetchParkingSpaces();
        }
      } catch (error) {
        console.error('租赁失败:', error);
      }
    },

    // 更新车辆的停车位信息
    async updateVehicleParkingSpace(vehicleId, spaceNumber) {
      try {
        const response = await fetch(`/api/vehicles/${vehicleId}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          body: JSON.stringify({
            parkingSpace: spaceNumber
          })
        });

        if (!response.ok) {
          throw new Error('更新车辆停车位失败');
        }
      } catch (error) {
        console.error('更新车辆停车位失败:', error);
      }
    },

    // 判断是否可以编辑
    canEdit(parking) {
      return parking.status !== '已售出';
    },

    // 结束出租/停车
    async handleEndRent(parking) {
      const actionType = parking.type === '免费' ? '停车' : '出租';
      if (!confirm(`确定要结束车位 ${parking.spaceNumber} 的${actionType}吗？`)) {
        return;
      }
      
      try {
        const response = await fetch(`/api/parking-transactions/${parking.id}/end-rent`, {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });
        const result = await response.json();
        if (!response.ok) {
          throw new Error(result.error || '操作失败');
        }
        await this.fetchParkingSpaces();
        alert(`${actionType}已结束！`);
      } catch (err) {
        console.error('结束出租错误:', err);
        alert(err.message);
      }
    },

    // 显示出售对话框
    showSellDialog(parking) {
      this.selectedParking = parking;
      this.sellForm = {
        parkingId: parking.id,
        vehicleId: '',
        price: parking.price,
        date: new Date().toISOString().split('T')[0]
      };
      this.showSellForm = true;
      this.fetchAvailableVehicles();
    },

    // 关闭出售对话框
    closeSellDialog() {
      this.showSellForm = false;
      this.sellForm = {
        parkingId: null,
        vehicleId: '',
        price: 0,
        date: ''
      };
    },

    // 处理出售
    async handleSell() {
      try {
        const response = await fetch('/api/parking-transactions/sell', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          body: JSON.stringify(this.sellForm)
        });

        if (!response.ok) {
          throw new Error('出售失败');
        }

        await this.fetchParkingSpaces();
        this.showSellForm = false;
        alert('出售成功');
      } catch (err) {
        console.error('出售失败:', err);
        alert(err.message);
      }
    },

    // 判断车辆是否可以停在该车位
    canParkVehicle(vehicle, parking) {
      if (!vehicle || !parking) return false;
      
      console.log('检查车辆匹配:', vehicle);
      console.log('车位要求:', parking);

      // 获取车辆类型（兼容不同的字段名）
      const vehicleType = vehicle.vehicleType || vehicle.vehicle_type;
      const allowedType = parking.allowedVehicleType;
      
      console.log('车辆类型:', vehicleType);
      console.log('车位允许类型:', allowedType);

      // 直接类型匹配
      if (vehicleType === allowedType) {
        console.log('类型完全匹配');
        return true;
      }

      // 小型车位（摩托车、电瓶车、自行车）对应电动车和摩托车
      const smallParkingTypes = ['摩托车', '电瓶车', '自行车'];
      const smallVehicleTypes = ['电动车', '摩托车'];
      
      // 大型车位（油车、电动汽车）对应小型汽车
      const largeParkingTypes = ['油车', '电动汽车'];
      const largeVehicleTypes = ['小型汽车'];

      // 判断匹配关系
      if (smallParkingTypes.includes(allowedType)) {
        const isMatch = smallVehicleTypes.includes(vehicleType);
        console.log('小型车位匹配结果:', isMatch);
        return isMatch;
      }
      
      if (largeParkingTypes.includes(allowedType)) {
        const isMatch = largeVehicleTypes.includes(vehicleType);
        console.log('大型车位匹配结果:', isMatch);
        return isMatch;
      }

      console.log('无匹配结果');
      return false;
    },

    // 切换新增表单显示
    toggleNewForm() {
      this.showNewForm = !this.showNewForm;
      if (!this.showNewForm) {
        this.resetNewParkingForm();
      }
    },

    // 重置表单
    resetNewParkingForm() {
      this.newParking = {
        spaceNumber: '',
        buildingId: '',
        allowedVehicleType: '',
        chargingPile: '无',
        locationType: '',
        groundDirection: '',
        undergroundLevel: '',
        type: '出售',
        price: null,
        area: null,
        description: '',
        status: '空闲'
      };
    },

    // 处理位置类型变化
    handleLocationTypeChange() {
      this.newParking.groundDirection = '';
      this.newParking.undergroundLevel = '';
      this.handleLocationChange();
    },

    // 处理位置变化，生成车位编号
    handleLocationChange() {
      let prefix = '';
      let number = '';

      // 获取楼号
      const buildingId = this.newParking.buildingId;
      
      if (this.newParking.locationType === '地上') {
        switch (this.newParking.groundDirection) {
          case '北面':
            prefix = 'N';
            break;
          case '南面':
            prefix = 'S';
            break;
          case '东面':
            prefix = 'E';
            break;
          case '西面':
            prefix = 'W';
            break;
        }
        if (prefix) {
          number = this.getNextNumber(buildingId, prefix);
        }
      } else if (this.newParking.locationType === '地下') {
        if (this.newParking.undergroundLevel) {
          prefix = 'B' + this.newParking.undergroundLevel;
          number = this.getNextNumber(buildingId, prefix);
        }
      }

      this.newParking.spaceNumber = number ? `${prefix}-${number}` : '';
    },

    // 获取下一个编号
    getNextNumber(buildingId, prefix) {
      // 过滤出同一楼号、同一区域的车位
      const sameAreaSpaces = this.parkingSpaces.filter(space => {
        const spacePrefix = space.spaceNumber.split('-')[0];
        const spaceBuildingId = space.buildingId; // 直接使用buildingId属性
        return spaceBuildingId === buildingId && spacePrefix === prefix;
      });

      if (sameAreaSpaces.length === 0) {
        return '101'; // 该区域第一个车位
      }

      // 获取当前最大编号
      const maxNumber = Math.max(...sameAreaSpaces.map(space => {
        const number = parseInt(space.spaceNumber.split('-')[1]);
        return isNaN(number) ? 0 : number;
      }));

      // 返回下一个编号
      return String(maxNumber + 1).padStart(3, '0');
    },

    // 从位置信息中提取楼号
    extractBuildingId(location) {
      if (!location) return null;
      const match = location.match(/^(\d+)/);
      return match ? parseInt(match[1]) : null;
    },

    // 生成位置信息
    generateLocation() {
      const buildingId = this.newParking.buildingId;
      if (!buildingId) return '';

      let location = `${buildingId}号楼`;
      
      if (this.newParking.locationType === '地上') {
        location += `${this.newParking.groundDirection}`;
      } else if (this.newParking.locationType === '地下') {
        location += `地下${this.newParking.undergroundLevel}层`;
      }
      
      return location;
    },

    handleVehicleTypeChange() {
      if (!this.showChargingPileOption) {
        this.newParking.chargingPile = '无'
      }
    },

    // 添加回 canRent 方法
    canRent(parking) {
      return parking.status === '空闲' && (parking.type === '出租' || parking.type === '免费');
    },

    // 从位置信息中提取位置类型
    getLocationType(location) {
      if (location.includes('地下')) {
        return '地下';
      }
      return '地上';
    },

    // 处理编辑表单提交
    async handleEditSubmit() {
      try {
        if (!this.editingParking) return;
        
        // 如果是免费车位，价格设为0
        if (this.editingParking.type === '免费') {
          this.editingParking.price = 0;
        }
        
        // 验证价格
        if (this.editingParking.type !== '免费' && 
            (!this.editingParking.price || this.editingParking.price <= 0)) {
          alert('请输入有效的价格');
          return;
        }

        const response = await fetch(`/api/parking-spaces/${this.editingParking.id}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          body: JSON.stringify(this.editingParking)
        });

        const result = await response.json();
        if (!result.success) {
          throw new Error(result.message);
        }

        await this.fetchParkingSpaces();
        this.closeEditDialog();
        alert('更新成功');
      } catch (err) {
        console.error('更新车位信息失败:', err);
        alert(err.message || '更新失败');
      }
    },

    // 添加车辆类型筛选方法
    handleVehicleTypeFilter(type) {
      this.currentVehicleType = type;
    },

    // 修改新增车位表单中的车辆类型选项
    getAllowedVehicleTypes() {
      return [
        { value: '小型汽车', label: '小型汽车' },
        { value: '电动汽车', label: '电动汽车' },
        { value: '电瓶车', label: '电瓶车' },
        { value: '摩托车', label: '摩托车' },
        { value: '自行车', label: '自行车' }
      ];
    }
  },
  async created() {
    // 确保先加载楼房列表，再加载车位列表
    this.fetchBuildings().then(() => {
      this.fetchParkingSpaces();
    });
  },
  watch: {
    // 监听车位类型变化
    'newParking.type': {
      handler(newType) {
        // 当类型改变为"免费"时，自动设置价格为0
        if (newType === '免费') {
          this.newParking.price = 0;
        }
      },
      immediate: true
    }
  }
}
</script>

<style scoped>
.parking-management {
  min-height: 100vh;
  background-image: linear-gradient(rgba(255, 255, 255, 0.9), rgba(255, 255, 255, 0.9)),
                    url('/parking-bg.jpg');
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

/* 操作区域样式 */
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
  border-color: #3182ce;
  box-shadow: 0 0 0 3px rgba(49, 130, 206, 0.1);
}

/* 表单样式 */
.new-parking-form {
  background: white;
  padding: 2rem;
  border-radius: 1rem;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  margin-bottom: 2rem;
}

.form-row {
  display: flex;
  gap: 1.5rem;
  margin-bottom: 1.5rem;
  width: 100%; /* 确保form-row占满容器宽度 */
}

.form-group {
  flex: 1;
  min-width: 0; /* 防止flex子项溢出 */
}

/* 添加全宽样式 */
.form-group.full-width {
  max-width: 100%; /* 限制最大宽度 */
  flex: none; /* 不参与flex布局的伸缩 */
  width: 100%; /* 占满容器宽度 */
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #2d3748;
  font-weight: 500;
}

.form-group input,
.form-group select,
.form-group textarea {
  box-sizing: border-box;
  width: 100%;
  padding: 0.8rem 1rem;
  border: 2px solid #e2e8f0;
  border-radius: 0.5rem;
  font-size: 1rem;
  background-color: white;
  transition: all 0.3s ease;
}

.form-group textarea {
  min-height: 120px;
  resize: vertical;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #3182ce;
  box-shadow: 0 0 0 3px rgba(49, 130, 206, 0.1);
}

/* 价格输入组样式优化 */
.price-input-group {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  width: 100%;
}

.price-input-group input {
  flex: 1;
}

.price-unit {
  color: #4a5568;
  font-size: 0.9rem;
  white-space: nowrap;
  padding-right: 0.5rem;
}

/* 车位卡片样式 */
.parking-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 2rem;
  padding: 1rem 0;
}

.parking-card {
  background: white;
  border-radius: 1rem;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition: all 0.3s ease;
}

.parking-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.card-header {
  padding: 1rem;
  border-bottom: 1px solid #e2e8f0;
  background-color: #f8fafc;
}

.card-header h3 {
  margin: 0;
  font-size: 1.1rem;
  color: #2d3748;
  font-weight: 600;
}

.location-info {
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e2e8f0;
}

.location-info .label {
  font-weight: 500;
  color: #4a5568;
  margin-right: 0.5rem;
}

.location-info .value {
  color: #2d3748;
}

.card-content {
  padding: 1rem;
}

.info-item {
  margin-bottom: 0.75rem;
  display: flex;
  align-items: center;
}

.info-item .label {
  min-width: 80px;
  font-weight: 500;
  color: #4a5568;
}

.info-item .value {
  color: #2d3748;
}

/* 按钮样式 */
.card-actions {
  padding: 1.5rem;
  border-top: 1px solid #e2e8f0;
  display: flex;
  gap: 1rem;
}

.action-button {
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

.rent {
  background-color: #48bb78;
  color: white;
}

.edit {
  background-color: #3182ce;
  color: white;
}

.delete {
  background-color: #e53e3e;
  color: white;
}

.rent:hover,
.edit:hover,
.delete:hover {
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
  align-items: center;
  justify-content: center;
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

.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 2rem;
}

.form-actions button {
  flex: 1;
  padding: 0.8rem;
  border: none;
  border-radius: 0.5rem;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.submit-btn {
  background-color: #3182ce;
  color: white;
}

.cancel-btn {
  background-color: #e2e8f0;
  color: #4a5568;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .container {
    padding: 0 1rem;
  }

  .action-group {
    flex-direction: column;
    width: 100%;
  }

  .search-box {
    width: 100%;
  }

  .parking-cards {
    grid-template-columns: 1fr;
  }

  .card-actions {
    flex-direction: column;
  }

  .modal-content {
    padding: 1.5rem;
  }

  .form-actions {
    flex-direction: column;
  }

  .form-row {
    flex-direction: column;
    gap: 1rem;
  }

  .form-group,
  .form-group.full-width {
    width: 100%;
  }
}

/* 错误提示样式 */
.error-message {
  background-color: #fff5f5;
  border: 1px solid #feb2b2;
  color: #c53030;
  padding: 1rem;
  border-radius: 0.5rem;
  margin-bottom: 1rem;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.error-message i {
  color: #c53030;
}

/* 筛选区域样式 */
.filters {
  display: flex;
  gap: 1.5rem;
  margin: 2rem 0;
  flex-wrap: wrap;
  padding: 1.5rem;
  background: #f8fafc;
  border-radius: 0.8rem;
  border: 1px solid #e2e8f0;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 1rem;
  min-width: 200px;
}

.filter-group label {
  font-weight: 500;
  color: #4a5568;
  white-space: nowrap;
  font-size: 0.95rem;
}

.filter-group select {
  padding: 0.75rem 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 0.375rem;
  background-color: white;
  min-width: 140px;
  font-size: 0.95rem;
  color: #2d3748;
  cursor: pointer;
  transition: all 0.2s ease;
}

.filter-group select:hover {
  border-color: #cbd5e0;
  background-color: #f9fafb;
}

.filter-group select:focus {
  outline: none;
  border-color: #4299e1;
  box-shadow: 0 0 0 3px rgba(66, 153, 225, 0.1);
}

.filter-group select option {
  padding: 0.5rem;
  font-size: 0.95rem;
}

@media (max-width: 768px) {
  .filters {
    flex-direction: column;
    gap: 1rem;
    padding: 1rem;
  }
  
  .filter-group {
    width: 100%;
    justify-content: space-between;
  }
  
  .filter-group select {
    flex: 1;
    margin-left: 1rem;
  }
}

.generated-number {
  background-color: #f7fafc !important;
  cursor: not-allowed;
}

.hint-text {
  font-size: 0.8rem;
  color: #718096;
  margin-top: 0.25rem;
  display: block;
}

.form-text {
  font-size: 0.875rem;
  color: #666;
  margin-top: 0.25rem;
}

.form-group input[readonly] {
  background-color: #f8f9fa;
  cursor: not-allowed;
}

textarea {
  width: 100%;
  min-height: 100px;
  padding: 0.8rem;
  border: 1px solid #e2e8f0;
  border-radius: 0.5rem;
  resize: vertical;
  box-sizing: border-box; /* 确保padding不会增加总宽度 */
}

textarea:focus {
  outline: none;
  border-color: #3182ce;
  box-shadow: 0 0 0 3px rgba(49, 130, 206, 0.1);
}

.filters {
  display: flex;
  gap: 1.5rem;
  margin: 2rem 0;
  flex-wrap: wrap;
  padding: 1.5rem;
  background: #f8fafc;
  border-radius: 0.8rem;
  border: 1px solid #e2e8f0;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 1rem;
  min-width: 200px;
}

.filter-group label {
  font-weight: 500;
  color: #4a5568;
  white-space: nowrap;
  font-size: 0.95rem;
}

.filter-group select {
  padding: 0.75rem 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 0.375rem;
  background-color: white;
  min-width: 140px;
  font-size: 0.95rem;
  color: #2d3748;
  cursor: pointer;
  transition: all 0.2s ease;
}

.filter-group select:hover {
  border-color: #cbd5e0;
  background-color: #f9fafb;
}

.filter-group select:focus {
  outline: none;
  border-color: #4299e1;
  box-shadow: 0 0 0 3px rgba(66, 153, 225, 0.1);
}

.filter-group select option {
  padding: 0.5rem;
  font-size: 0.95rem;
}

@media (max-width: 768px) {
  .filters {
    flex-direction: column;
    gap: 1rem;
    padding: 1rem;
  }
  
  .filter-group {
    width: 100%;
    justify-content: space-between;
  }
  
  .filter-group select {
    flex: 1;
    margin-left: 1rem;
  }
}

.action-button.delete:disabled {
  background-color: #cbd5e0;
  cursor: not-allowed;
  opacity: 0.6;
}

.action-button.delete:not(:disabled):hover {
  background-color: #e53e3e;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(229, 62, 62, 0.2);
}

/* 禁用状态的输入框样式 */
.price-input-group input:disabled {
  background-color: #f8f9fa;
  cursor: not-allowed;
  color: #666;
  border-color: #e2e8f0;
}

/* 提示文本样式 */
.price-input-group small {
  display: block;
  margin-top: 0.25rem;
  color: #718096;
  font-size: 0.875rem;
}
</style> 