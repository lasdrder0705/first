<template>
  <div class="owner-management">
    <nav-bar></nav-bar>
    <main class="main-content">
      <div class="container">
        <h2>业主信息管理</h2>
        <!-- 业主新增/编辑表单 -->
        <div class="owner-form">
          <h3>{{ editingOwner ? '编辑业主信息' : '新增业主信息' }}</h3>
          <form @submit.prevent="handleOwnerSubmit">
            <!-- 姓名和联系电话一行 -->
            <div class="form-row">
              <div class="form-group">
                <label>姓名:</label>
                <input type="text" v-model="ownerForm.name" required />
              </div>
              <div class="form-group">
                <label>联系电话:</label>
                <input type="text" v-model="ownerForm.contact_number" />
              </div>
            </div>
            
            <!-- 所属楼房和门牌号一行 -->
            <div class="form-row">
              <div class="form-group">
                <label>所属楼房:</label>
                <select v-model="ownerForm.building_id" required>
                  <option value="">请选择楼房</option>
                  <option v-for="building in (buildings || [])" :key="building?.id" :value="building?.id">
                    {{ building?.name || '未命名楼房' }}
                  </option>
                </select>
              </div>
              <div class="form-group">
                <label>门牌号:</label>
                <input type="text" v-model="ownerForm.door_number" placeholder="例如：101室" />
              </div>
            </div>
            
            <!-- 车辆信息和车位归属一行 -->
            <div class="form-row">
              <div class="form-group">
                <label>车辆信息:</label>
                <div class="vehicles-section">
                  <select v-model="selectedVehicles" multiple placeholder="请选择车辆">
                    <option v-for="vehicle in (vehicles || [])" 
                            :key="vehicle.id" 
                            :value="vehicle.id">
                      {{ formatVehicleOption(vehicle) }}
                    </option>
                  </select>
                  <button type="button" class="add-vehicle-btn" @click="showAddVehicleDialog">
                    <i class="fas fa-plus"></i>
                    添加新车辆
                  </button>
                </div>
              </div>
              <div class="form-group">
                <label>车位归属:</label>
                <select v-model="ownerForm.parking_space" 
                        :disabled="hasAssignedParking">
                  <option value="">请选择车位</option>
                  <option v-for="space in parkingSpaces" 
                          :key="space.id" 
                          :value="space.spaceNumber">
                    {{ formatParkingSpace(space) }}
                  </option>
                </select>
                <span v-if="hasAssignedParking" class="hint">
                  已选车辆已有指定车位
                </span>
              </div>
            </div>
            
            <div class="form-actions">
              <button type="submit">{{ editingOwner ? '保存修改' : '新增业主' }}</button>
              <button type="button" @click="resetOwnerForm">取消</button>
            </div>
          </form>
        </div>
        <!-- 业主列表 -->
        <div class="owners-list">
          <div class="list-header">
            <h3>业主列表</h3>
            <div class="search-box">
              <input 
                type="text" 
                v-model="searchQuery" 
                placeholder="搜索业主姓名/电话/门牌号..."
                @input="handleSearch"
              />
              <i class="fas fa-search"></i>
            </div>
          </div>
          <div class="owner-cards">
            <div v-for="owner in filteredOwners" :key="owner.id" class="owner-card">
              <div class="owner-header">
                <h4>{{ owner.name }}</h4>
                <span class="owner-id">#{{ owner.id }}</span>
              </div>
              
              <div class="owner-info">
                <div class="info-item">
                  <i class="fas fa-phone"></i>
                  <span class="label">联系电话:</span>
                  <span class="value">{{ owner.contact_number || '未设置' }}</span>
                </div>
                
                <div class="info-item">
                  <i class="fas fa-building"></i>
                  <span class="label">所属楼房:</span>
                  <span class="value">{{ owner.building_id ? getBuildingName(owner.building_id) : '未知楼房' }}</span>
                </div>
                
                <div class="info-item">
                  <i class="fas fa-door-open"></i>
                  <span class="label">门牌号:</span>
                  <span class="value">{{ owner.door_number || '未设置' }}</span>
                </div>
                
                <div class="info-item">
                  <i class="fas fa-car"></i>
                  <span class="label">车辆信息:</span>
                  <span class="value">
                    <template v-if="owner.vehicles && owner.vehicles.length">
                      {{ formatVehicleList(owner.vehicles) }}
                    </template>
                    <template v-else>无车辆</template>
                  </span>
                </div>
                
                <div class="info-item">
                  <i class="fas fa-parking"></i>
                  <span class="label">车位归属:</span>
                  <span class="value">{{ owner.parking_space || '未设置' }}</span>
                </div>
              </div>

              <div class="card-actions">
                <button class="edit-btn" @click="editOwner(owner)">
                  <i class="fas fa-edit"></i>
                  编辑
                </button>
                <button class="delete-btn" @click="confirmDelete(owner)">
                  <i class="fas fa-trash"></i>
                  删除
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 新增车辆的弹窗 -->
    <div v-if="vehicleDialogVisible" class="modal">
      <div class="modal-content">
        <h3>新增车辆信息</h3>
        <form @submit.prevent="submitVehicle">
          <div class="form-group">
            <label>车牌号:</label>
            <input type="text" v-model="vehicleForm.plate_number" required />
          </div>
          <div class="form-group">
            <label>车辆类型:</label>
            <select v-model="vehicleForm.vehicle_type" required>
              <option value="小型车">小型车</option>
              <option value="大型车">大型车</option>
              <option value="电动车">电动车</option>
              <option value="摩托车">摩托车</option>
            </select>
          </div>
          <div class="form-actions">
            <button type="submit">保存车辆</button>
            <button type="button" @click="vehicleDialogVisible = false">取消</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 添加删除确认对话框 -->
    <div v-if="showDeleteConfirm" class="modal">
      <div class="modal-content">
        <h3>确认删除</h3>
        <p>确定要删除业主 "{{ ownerToDelete?.name }}" 吗？</p>
        <p class="warning-text">此操作将同时：</p>
        <ul class="warning-list">
          <li>删除业主的所有信息</li>
          <li>清除关联的车位信息</li>
          <li>此操作不可恢复</li>
        </ul>
        <div class="modal-actions">
          <button class="confirm-btn" @click="deleteOwner">确认删除</button>
          <button class="cancel-btn" @click="showDeleteConfirm = false">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import NavBar from "./NavBar.vue";

export default {
  name: "OwnerManagement",
  components: { NavBar },
  data() {
    return {
      owners: [], // 业主列表
      buildings: [], // 楼房列表
      vehicles: [], // 车辆列表
      parkingSpaces: [], // 车位列表
      ownerForm: {
        id: null,
        name: "",
        contact_number: "",
        building_id: "",
        door_number: "",
        parking_space: "",
      },
      editingOwner: false,
      selectedVehicles: [], // 选中的车辆ID数组
      vehicleDialogVisible: false,
      vehicleForm: {
        plate_number: "",
        vehicle_type: "小型车",
        owner_type: "业主",
        owner_name: "",
        status: "正常"
      },
      showDeleteConfirm: false,
      ownerToDelete: null,
      searchQuery: '', // 添加搜索查询
    };
  },
  methods: {
    async fetchOwners() {
      try {
        const res = await fetch("/api/owners", {
          headers: { 
            Authorization: "Bearer " + localStorage.getItem("token") 
          },
        });
        const data = await res.json();
        if (data.success) {
          this.owners = (data.data || []).map(owner => {
            let vehicleIds = [];
            try {
              if (owner.vehicles) {
                vehicleIds = typeof owner.vehicles === 'string' 
                  ? JSON.parse(owner.vehicles) 
                  : owner.vehicles;
              }
            } catch (e) {
              console.warn('Failed to parse vehicles for owner:', owner.id, e);
            }

            const formattedOwner = {
              id: owner.id,
              name: owner.name || '未知业主',
              contact_number: owner.contact_number || owner.contactNumber || '',
              building_id: owner.building_id || owner.buildingId || null,
              door_number: owner.door_number || owner.doorNumber || '',
              parking_space: owner.parking_space || owner.parkingSpace || '',
              vehicles: vehicleIds
            };
            console.log('Formatted owner:', formattedOwner);
            return formattedOwner;
          });
        } else {
          console.error("获取业主信息失败：", data.message);
          this.owners = [];
        }
      } catch (err) {
        console.error("获取业主信息失败：", err);
        this.owners = [];
      }
    },
    async fetchBuildings() {
      try {
        const res = await fetch('/api/buildings', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });
        const data = await res.json();
        if(data.success) {
          this.buildings = data.data;
        } else {
          console.error("获取楼房信息失败:", data.message);
        }
      } catch(err) {
        console.error("获取楼房信息异常:", err);
      }
    },
    async fetchVehicles() {
      try {
        const res = await fetch('/api/vehicles', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });
        const data = await res.json();
        if(data.success) {
          this.vehicles = data.data.map(vehicle => {
            // 确保所有必要字段都有默认值
            return {
              ...vehicle,
              plate_number: vehicle.plate_number || vehicle.plateNumber || '未知车牌',
              vehicle_type: vehicle.vehicle_type || vehicle.vehicleType || '未知类型',
              parking_space: vehicle.parking_space || vehicle.parkingSpace || '',
              owner_name: vehicle.owner_name || vehicle.ownerName || '',
              owner_type: vehicle.owner_type || vehicle.ownerType || '业主',
              status: vehicle.status || '正常'
            };
          });
          console.log('Fetched vehicles:', this.vehicles);
        } else {
          console.error("获取车辆信息失败:", data.message);
          this.vehicles = [];
        }
      } catch(err) {
        console.error("获取车辆信息异常:", err);
        this.vehicles = [];
      }
    },
    async fetchParkingSpaces() {
      try {
        const res = await fetch("/api/parking-spaces/available", {
          headers: { 
            Authorization: "Bearer " + localStorage.getItem("token") 
          },
        });
        const data = await res.json();
        if (data.success) {
          this.parkingSpaces = (data.data || []).map(space => ({
            ...space,
            spaceNumber: space.spaceNumber || space.space_number || '未编号',
            location: space.location || '',
            type: space.type || '',
            status: space.status || '空闲'
          }));
          console.log('Available parking spaces:', this.parkingSpaces);
        } else {
          console.error("获取车位信息失败：", data.message);
          this.parkingSpaces = [];
        }
      } catch (err) {
        console.error("获取车位信息失败：", err);
        this.parkingSpaces = [];
      }
    },
    getBuildingName(buildingId) {
      if (!buildingId) return '未知楼房';
      if (!this.buildings || !this.buildings.length) return '加载中...';
      const building = this.buildings.find(b => b?.id === buildingId);
      return building ? building.name : '未知楼房';
    },
    formatVehicles(vehiclesJson) {
      try {
        // 如果是空值或空字符串，直接返回空
        if (!vehiclesJson || vehiclesJson === '' || vehiclesJson === '[]') {
          return '';
        }

        // 如果已经是数组，直接使用
        const vehicleIds = Array.isArray(vehiclesJson) 
          ? vehiclesJson 
          : JSON.parse(vehiclesJson);

        if (!Array.isArray(vehicleIds) || !vehicleIds.length) {
          return '';
        }

        if (!this.vehicles || !this.vehicles.length) {
          return '加载中...';
        }

        return vehicleIds
          .map(id => {
            const vehicle = this.vehicles.find(v => v.id === id);
            if (!vehicle) return '';
            // 统一使用下划线格式的字段名
            const plateNumber = vehicle.plate_number || '未知车牌';
            const vehicleType = vehicle.vehicle_type || '未知类型';
            return `${plateNumber} (${vehicleType})`;
          })
          .filter(text => text)
          .join('、');
      } catch (err) {
        console.error('解析车辆信息失败:', err, 'vehiclesJson:', vehiclesJson);
        return '';
      }
    },
    async handleOwnerSubmit() {
      try {
        if (!this.ownerForm.name.trim()) {
          alert("请输入业主姓名");
          return;
        }

        const ownerData = {
          name: this.ownerForm.name.trim(),
          contact_number: this.ownerForm.contact_number?.trim() || '',
          building_id: this.ownerForm.building_id ? Number(this.ownerForm.building_id) : null,
          door_number: this.ownerForm.door_number?.trim() || '',
          parking_space: this.ownerForm.parking_space || '',
          vehicles: Array.isArray(this.selectedVehicles) ? this.selectedVehicles : []
        };

        if (this.editingOwner) {
          ownerData.id = this.ownerForm.id;
        }

        console.log('Submitting owner data:', ownerData);

        const res = await fetch(
          this.editingOwner ? `/api/owners/${ownerData.id}` : "/api/owners",
          {
            method: this.editingOwner ? "PUT" : "POST",
            headers: {
              "Content-Type": "application/json",
              Authorization: "Bearer " + localStorage.getItem("token"),
            },
            body: JSON.stringify(ownerData)
          }
        );

        const data = await res.json();
        console.log('Server response:', data);

        if (data.success) {
          // 如果选择了车位，更新车位状态
          if (ownerData.parking_space) {
            await this.updateParkingSpaceStatus(ownerData.parking_space, ownerData.name);
          }
          alert(this.editingOwner ? "更新成功" : "添加成功");
          await this.fetchOwners();
          this.resetOwnerForm();
        } else {
          alert(data.message || "操作失败");
        }
      } catch (err) {
        console.error("保存业主信息失败：", err);
        alert("操作失败：" + err.message);
      }
    },
    editOwner(owner) {
      try {
        this.ownerForm = {
          id: owner.id,
          name: owner.name,
          contact_number: owner.contact_number,
          building_id: owner.building_id,
          door_number: owner.door_number,
          parking_space: owner.parking_space,
        };
        // 处理车辆信息
        this.selectedVehicles = Array.isArray(owner.vehicles) 
          ? owner.vehicles 
          : (owner.vehicles ? JSON.parse(owner.vehicles) : []);
        this.editingOwner = true;
      } catch (err) {
        console.error('Error in editOwner:', err);
        this.selectedVehicles = [];
        this.editingOwner = true;
      }
    },
    resetOwnerForm() {
      this.ownerForm = {
        id: null,
        name: "",
        contact_number: "",
        building_id: "",
        door_number: "",
        parking_space: "",
      };
      this.editingOwner = false;
    },
    showAddVehicleDialog() {
      if (!this.ownerForm.name) {
        alert("请先填写业主姓名");
        return;
      }
      this.vehicleDialogVisible = true;
      this.vehicleForm = {
        plate_number: "",
        vehicle_type: "小型车",
        owner_type: "业主",
        owner_name: this.ownerForm.name,
        status: "正常"
      };
    },
    async submitVehicle() {
      try {
        // 验证车牌号格式
        const validation = this.validatePlateNumber(
          this.vehicleForm.plate_number, 
          this.vehicleForm.vehicle_type
        );
        
        if (!validation.isValid) {
          alert(validation.message);
          return;
        }

        // 检查车牌号是否已存在
        const response = await fetch(`/api/vehicles/search?query=${this.vehicleForm.plate_number}`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });

        const result = await response.json();
        if (result.success && result.data.length > 0) {
          alert('该车牌号已被登记');
          return;
        }

        // 转换数据格式以匹配后端要求
        const vehicleData = {
          plateNumber: this.vehicleForm.plate_number.trim(),
          vehicleType: this.vehicleForm.vehicle_type,
          ownerType: this.vehicleForm.owner_type,
          ownerName: this.vehicleForm.owner_name,
          status: this.vehicleForm.status,
          createdAt: new Date().toISOString()
        };

        // 提交数据
        const submitResponse = await fetch('/api/vehicles', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          body: JSON.stringify(vehicleData)
        });

        const submitResult = await submitResponse.json();
        if (submitResult.success) {
          // 更新车辆列表
          await this.fetchVehicles();
          // 将新车辆添加到选中列表
          if (submitResult.data && submitResult.data.id) {
            this.selectedVehicles.push(submitResult.data.id);
          }
          // 关闭弹窗
          this.vehicleDialogVisible = false;
          // 重置表单
          this.vehicleForm = {
            plate_number: '',
            vehicle_type: '小型车',
            owner_type: '业主',
            owner_name: this.ownerForm.name, // 保持业主姓名
            status: '正常'
          };
          alert('添加车辆成功');
        } else {
          throw new Error(submitResult.message || '添加车辆失败');
        }
      } catch (error) {
        console.error('添加车辆失败:', error);
        alert('添加车辆失败: ' + error.message);
      }
    },
    formatVehicleOption(vehicle) {
      if (!vehicle) return '未知车辆';
      const plateNumber = vehicle.plate_number || vehicle.plateNumber || '未知车牌';
      const vehicleType = vehicle.vehicle_type || vehicle.vehicleType || '未知类型';
      let text = `${plateNumber} (${vehicleType}`;
      if (vehicle.parking_space) {
        text += ` - 车位: ${vehicle.parking_space}`;
      }
      text += ')';
      return text;
    },
    formatParkingSpace(space) {
      if (!space) return '未知车位';
      const number = space.spaceNumber || space.space_number || '未编号';
      const location = space.location || '';
      const type = space.type || '';
      const price = space.price ? `￥${space.price}` : '';
      return `${number} (${location} ${type} ${price})`.trim();
    },
    formatVehicleList(vehicleIds) {
      if (!vehicleIds || !vehicleIds.length || !this.vehicles) return '无车辆';
      
      return vehicleIds
        .map(id => {
          const vehicle = this.vehicles.find(v => v.id === id);
          if (!vehicle) return null;
          return `${vehicle.plate_number} (${vehicle.vehicle_type})`;
        })
        .filter(Boolean)
        .join('、') || '无车辆';
    },
    // 添加更新车位状态的方法
    async updateParkingSpaceStatus(spaceNumber, ownerName) {
      try {
        const res = await fetch(`/api/parking-spaces/${spaceNumber}/status`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            Authorization: "Bearer " + localStorage.getItem("token"),
          },
          body: JSON.stringify({
            status: '已分配',
            ownerName: ownerName
          })
        });
        const data = await res.json();
        if (!data.success) {
          console.error('更新车位状态失败:', data.message);
        }
      } catch (err) {
        console.error('更新车位状态异常:', err);
      }
    },
    confirmDelete(owner) {
      this.ownerToDelete = owner;
      this.showDeleteConfirm = true;
    },
    async deleteOwner() {
      try {
        if (!this.ownerToDelete || !this.ownerToDelete.id) {
          alert("无效的业主信息");
          return;
        }

        const res = await fetch(`/api/owners/${this.ownerToDelete.id}`, {
          method: 'DELETE',
          headers: {
            'Content-Type': 'application/json',
            Authorization: "Bearer " + localStorage.getItem("token"),
          }
        });

        const data = await res.json();
        if (data.success) {
          alert("业主删除成功");
          // 重新加载数据
          await Promise.all([
            this.fetchVehicles(),
            this.fetchParkingSpaces()
          ]);
          await this.fetchOwners();
        } else {
          alert(data.message || "删除失败");
        }
      } catch (err) {
        console.error("删除业主失败：", err);
        alert("删除失败：" + err.message);
      } finally {
        this.showDeleteConfirm = false;
        this.ownerToDelete = null;
      }
    },
    // 添加车牌号验证方法
    validatePlateNumber(plateNumber, vehicleType) {
      // 移除所有空格
      plateNumber = plateNumber.replace(/\s/g, '');
      
      // 不同类型车辆的车牌号正则表达式
      const patterns = {
        '小型车': {
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
    // 添加搜索处理方法
    handleSearch() {
      // 这里可以添加防抖逻辑如果需要
      // 目前使用计算属性，所以这个方法可以为空
    }
  },
  watch: {
    selectedVehicles: {
      handler(newVehicles) {
        if (!newVehicles || !newVehicles.length) return;
        
        // 查找选中车辆中是否有已分配车位的
        const selectedVehicle = this.vehicles.find(v => 
          newVehicles.includes(v.id) && v.parking_space
        );
        
        if (selectedVehicle) {
          this.ownerForm.parking_space = selectedVehicle.parking_space;
        }
      },
      immediate: true
    }
  },
  async created() {
    try {
      // 先加载车辆和楼房信息，再加载业主信息
      await Promise.all([
        this.fetchVehicles(),
        this.fetchBuildings(),
        this.fetchParkingSpaces()
      ]);
      await this.fetchOwners();
    } catch (err) {
      console.error('初始化数据失败:', err);
      alert('加载数据失败，请刷新页面重试');
    }
  },
  computed: {
    hasAssignedParking() {
      if (!this.selectedVehicles || !this.selectedVehicles.length) return false;
      return this.vehicles.some(v => 
        this.selectedVehicles.includes(v.id) && v.parking_space
      );
    },
    // 添加过滤业主的计算属性
    filteredOwners() {
      if (!this.searchQuery) return this.owners;
      
      const query = this.searchQuery.toLowerCase().trim();
      return this.owners.filter(owner => {
        return (
          (owner.name && owner.name.toLowerCase().includes(query)) ||
          (owner.contact_number && owner.contact_number.includes(query)) ||
          (owner.door_number && owner.door_number.toLowerCase().includes(query)) ||
          (this.getBuildingName(owner.building_id).toLowerCase().includes(query))
        );
      });
    }
  },
};
</script>

<style scoped>
.owner-management {
  min-height: 100vh;
  background-image: url('/assets/owner-bg.jpg'); /* 请自行替换为实际背景图片路径 */
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  position: relative;
}

.owner-management::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.7);  /* 半透明遮罩 */
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
.owner-form,
.owners-list {
  background: rgba(255, 255, 255, 0.9);
  padding: 1.5rem;
  border-radius: 10px;
  margin-bottom: 2rem;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}
.form-group {
  margin-bottom: 1rem;
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
  background-color: rgba(255, 255, 255, 0.9);
}
.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #3182ce;
  box-shadow: 0 0 0 3px rgba(49, 130, 206, 0.2);
}
.form-actions {
  margin-top: 2rem;
  display: flex;
  gap: 1rem;
  justify-content: flex-end; /* 按钮靠右对齐 */
  padding-top: 1rem;
  border-top: 1px solid #e2e8f0;
}
.form-actions button {
  min-width: 120px;
  padding: 0.75rem 1.5rem;
  border-radius: 6px;
  font-weight: 600;
  font-size: 1rem;
  transition: all 0.3s ease;
  cursor: pointer;
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
/* 取消按钮样式 */
.form-actions button[type="button"] {
  background-color: #fff;
  color: #2d3748;
  border: 1px solid #e2e8f0;
}
.form-actions button[type="button"]:hover {
  background-color: #f7fafc;
  border-color: #cbd5e0;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}
/* 按钮点击效果 */
.form-actions button:active {
  transform: translateY(0);
  box-shadow: none;
}
.owner-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
  padding: 1rem 0;
}
.owner-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition: all 0.3s ease;
}
.owner-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}
.owner-header {
  padding: 1.25rem;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.owner-header h4 {
  margin: 0;
  color: #2d3748;
  font-size: 1.25rem;
  font-weight: 600;
}
.owner-id {
  color: #718096;
  font-size: 0.875rem;
  font-weight: 500;
}
.owner-info {
  padding: 1.25rem;
}
.info-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
  color: #4a5568;
}
.info-item:last-child {
  margin-bottom: 0;
}
.info-item i {
  width: 1.25rem;
  color: #4299e1;
  text-align: center;
}
.info-item .label {
  min-width: 80px;
  color: #718096;
  font-weight: 500;
}
.info-item .value {
  color: #2d3748;
  flex: 1;
}
.card-actions {
  padding: 1.25rem;
  border-top: 1px solid #e2e8f0;
  display: flex;
  gap: 1rem;
}
.card-actions button {
  flex: 1;
  padding: 0.75rem;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
}
.edit-btn {
  background-color: #4299e1;
  color: white;
  border: none;
}
.edit-btn:hover {
  background-color: #3182ce;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(66, 153, 225, 0.2);
}
.delete-btn {
  background-color: #f56565;
  color: white;
  border: none;
}
.delete-btn:hover {
  background-color: #e53e3e;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(245, 101, 101, 0.2);
}
/* 响应式调整 */
@media (max-width: 768px) {
  .owner-cards {
    grid-template-columns: 1fr;
  }
  .info-item {
    flex-wrap: wrap;
  }
  .info-item .label {
    min-width: auto;
    width: 100%;
  }
  .card-actions {
    flex-direction: column;
  }
  .card-actions button {
    width: 100%;
  }
}
/* 新增车辆按钮样式 */
.add-vehicle-btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 0.75rem 1.25rem;
  border-radius: 6px;
  font-weight: 500;
  font-size: 0.95rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(76, 175, 80, 0.2);
}
.add-vehicle-btn:hover {
  background-color: #43A047;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(76, 175, 80, 0.3);
}
.add-vehicle-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(76, 175, 80, 0.2);
}
.add-vehicle-btn i {
  font-size: 0.9em;
}
/* 调整vehicles-section的样式 */
.vehicles-section {
  display: flex;
  gap: 1rem;
  align-items: flex-start;
}
.vehicles-section select {
  flex: 1;
  min-width: 200px;
  padding: 0.75rem;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background-color: white;
}
/* 响应式调整 */
@media (max-width: 768px) {
  .vehicles-section {
    flex-direction: column;
    gap: 0.75rem;
  }
  .add-vehicle-btn {
    width: 100%;
    justify-content: center;
  }
}
/* 添加错误提示样式 */
.error-message {
  color: #e53e3e;
  font-size: 0.875rem;
  margin-top: 0.5rem;
}
/* 添加标题样式 */
h2, h3 {
  color: #1a365d;
  margin-bottom: 1.5rem;
  font-weight: 600;
}
h2 {
  font-size: 2rem;
  text-align: center;
  margin-bottom: 2rem;
}
h3 {
  font-size: 1.5rem;
  border-bottom: 2px solid #e2e8f0;
  padding-bottom: 0.5rem;
}
/* 添加行布局样式 */
.form-row {
  display: flex;
  gap: 1.5rem;
  margin-bottom: 1rem;
}
.form-row .form-group {
  flex: 1;
  margin-bottom: 0;
}
/* 警告文本样式 */
.warning-text {
  color: #e53e3e;
  font-weight: 600;
  margin-top: 1rem;
  margin-bottom: 0.5rem;
}
.warning-list {
  color: #4a5568;
  margin-left: 1.5rem;
  margin-bottom: 1.5rem;
}
.warning-list li {
  margin-bottom: 0.5rem;
}
/* 确认删除对话框样式 */
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
  z-index: 2000;
}
.modal-content {
  background: white;
  padding: 2.5rem;
  border-radius: 16px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}
.modal-content h3 {
  color: #2d3748;
  margin-bottom: 1rem;
}
.modal-content p {
  color: #4a5568;
  margin-bottom: 1.5rem;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 2rem;
}
.confirm-btn {
  background-color: #e53e3e;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 4px;
  cursor: pointer;
}
.cancel-btn {
  background-color: #718096;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 4px;
  cursor: pointer;
}
.confirm-btn:hover {
  background-color: #c53030;
}
.cancel-btn:hover {
  background-color: #4a5568;
}
/* 列表头部样式 */
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
  box-shadow: 0 0 0 3px rgba(66, 153, 225, 0.15);
  outline: none;
}

.search-box input::placeholder {
  color: #a0aec0;
}

.search-box i {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: #4299e1;
  font-size: 1.1rem;
  pointer-events: none;
}

/* 添加搜索框悬停效果 */
.search-box:hover input {
  border-color: #e2e8f0;
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
  
  .search-box input {
    font-size: 16px;
  }
}

/* 修改表单组样式 */
.form-group input[type="text"] {
  width: 100%;
  max-width: 300px; /* 限制最大宽度为300px */
}

/* 特别处理姓名输入框 */
.form-group input[name="name"],
.form-group input[v-model="ownerForm.name"] {
  max-width: 200px; /* 进一步限制姓名输入框的宽度 */
}

/* 保持联系电话输入框宽度稍大一些 */
.form-group input[v-model="ownerForm.contact_number"] {
  max-width: 250px;
}
</style>
