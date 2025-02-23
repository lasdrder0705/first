<template>
  <div class="maintenance">
    <nav-bar />
    <main class="main-content">
      <div class="container">
        <div class="content-wrapper">
          <h2>报修信息上传</h2>
          
          <!-- 报修表单 -->
          <div class="maintenance-form">
            <form @submit.prevent="handleSubmit">
              <div class="form-row">
                <div class="form-group">
                  <label>报修类型 <span class="required">*</span></label>
                  <select v-model="maintenanceForm.type" required>
                    <option value="">请选择报修类型</option>
                    <option value="公共设施">公共设施</option>
                    <option value="电子设备">电子设备</option>
                    <option value="绿化">绿化</option>
                    <option value="道路">道路</option>
                    <option value="其他">其他</option>
                  </select>
                </div>
                <div class="form-group">
                  <label>紧急程度 <span class="required">*</span></label>
                  <select v-model="maintenanceForm.urgency" required>
                    <option value="">请选择紧急程度</option>
                    <option value="紧急">紧急</option>
                    <option value="一般">一般</option>
                    <option value="低">低</option>
                  </select>
                </div>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label>所属楼栋 <span class="required">*</span></label>
                  <select v-model="maintenanceForm.buildingId" required>
                    <option value="">请选择楼栋</option>
                    <option v-for="building in buildings" 
                            :key="building.id" 
                            :value="building.id">
                      {{ building.name }}
                    </option>
                  </select>
                </div>
                <div class="form-group">
                  <label>具体位置 <span class="required">*</span></label>
                  <input 
                    type="text" 
                    v-model="maintenanceForm.location"
                    placeholder="请详细描述故障位置"
                    required
                  />
                </div>
              </div>

              <div class="form-group">
                <label>故障描述 <span class="required">*</span></label>
                <textarea 
                  v-model="maintenanceForm.description"
                  placeholder="请详细描述故障情况..."
                  required
                ></textarea>
              </div>

              <div class="form-group">
                <label>图片上传</label>
                <div class="upload-area">
                  <input 
                    type="file" 
                    ref="fileInput"
                    @change="handleFileChange"
                    accept="image/*"
                    multiple
                  />
                  <div class="preview-images" v-if="imagePreviewUrls.length">
                    <div v-for="(url, index) in imagePreviewUrls" 
                         :key="index" 
                         class="preview-item">
                      <img :src="url" alt="预览图" />
                      <button type="button" 
                              class="remove-btn"
                              @click="removeImage(index)">
                        <i class="fas fa-times"></i>
                      </button>
                    </div>
                  </div>
                  <p class="upload-tip">支持jpg、png格式，最多上传5张</p>
                </div>
              </div>

              <div class="form-actions">
                <button type="submit" class="submit-btn">
                  提交报修
                </button>
                <button type="button" 
                        class="cancel-btn"
                        @click="resetForm">
                  重置
                </button>
              </div>
            </form>
          </div>

          <!-- 我的报修记录 -->
          <div class="maintenance-records">
            <h3>我的报修记录</h3>
            <div class="records-table">
              <table>
                <thead>
                  <tr>
                    <th>报修类型</th>
                    <th>紧急程度</th>
                    <th>位置</th>
                    <th>提交时间</th>
                    <th>状态</th>
                    <th>处理结果</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="record in maintenanceRecords" :key="record.id">
                    <td>{{ record.type }}</td>
                    <td>
                      <span :class="['urgency-badge', record.urgency]">
                        {{ record.urgency }}
                      </span>
                    </td>
                    <td>{{ record.buildingName }} - {{ record.location }}</td>
                    <td>{{ formatDateTime(record.createdAt) }}</td>
                    <td>
                      <span :class="['status-badge', record.status]">
                        {{ record.status }}
                      </span>
                    </td>
                    <td>{{ record.result || '-' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import NavBar from './NavBar.vue'

export default {
  name: 'Maintenance',
  components: {
    NavBar
  },
  data() {
    return {
      buildings: [], // 楼栋列表
      maintenanceForm: {
        type: '',
        urgency: '',
        buildingId: '',
        location: '',
        description: '',
        images: []
      },
      imagePreviewUrls: [],
      maintenanceRecords: [
        {
          id: 1,
          type: '电子设备',
          urgency: '紧急',
          buildingName: 'A栋',
          location: '一楼大厅',
          description: '监控设备故障',
          createdAt: '2024-03-20 10:00:00',
          status: '待处理',
          result: null
        }
        // 更多测试数据...
      ]
    }
  },
  methods: {
    // 提交报修
    async handleSubmit() {
      try {
        // 表单验证
        if (!this.maintenanceForm.type ||
            !this.maintenanceForm.urgency ||
            !this.maintenanceForm.buildingId ||
            !this.maintenanceForm.location ||
            !this.maintenanceForm.description) {
          alert('请填写所有必填项');
          return;
        }

        // 构造FormData对象，用于上传文件
        const formData = new FormData();
        formData.append('type', this.maintenanceForm.type);
        formData.append('urgency', this.maintenanceForm.urgency);
        formData.append('buildingId', this.maintenanceForm.buildingId);
        formData.append('location', this.maintenanceForm.location);
        formData.append('description', this.maintenanceForm.description);

        // 添加图片文件
        this.maintenanceForm.images.forEach((file, index) => {
          formData.append(`images[${index}]`, file);
        });

        const response = await fetch('/api/maintenance', {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          body: formData
        });

        const result = await response.json();
        if (result.success) {
          alert('报修信息提交成功');
          this.resetForm();
          await this.fetchMaintenanceRecords();
        } else {
          throw new Error(result.message);
        }
      } catch (error) {
        console.error('提交报修失败:', error);
        alert('提交失败: ' + error.message);
      }
    },

    // 处理文件选择
    handleFileChange(event) {
      const files = Array.from(event.target.files);
      
      // 限制上传数量
      if (this.maintenanceForm.images.length + files.length > 5) {
        alert('最多只能上传5张图片');
        return;
      }

      // 验证文件类型和大小
      const validFiles = files.filter(file => {
        const isValidType = ['image/jpeg', 'image/png'].includes(file.type);
        const isValidSize = file.size <= 5 * 1024 * 1024; // 5MB
        return isValidType && isValidSize;
      });

      if (validFiles.length !== files.length) {
        alert('只支持jpg、png格式，且单个文件不超过5MB');
      }

      // 添加到表单数据
      this.maintenanceForm.images.push(...validFiles);

      // 生成预览URL
      validFiles.forEach(file => {
        const reader = new FileReader();
        reader.onload = (e) => {
          this.imagePreviewUrls.push(e.target.result);
        };
        reader.readAsDataURL(file);
      });
    },

    // 移除图片
    removeImage(index) {
      this.maintenanceForm.images.splice(index, 1);
      this.imagePreviewUrls.splice(index, 1);
    },

    // 重置表单
    resetForm() {
      this.maintenanceForm = {
        type: '',
        urgency: '',
        buildingId: '',
        location: '',
        description: '',
        images: []
      };
      this.imagePreviewUrls = [];
      if (this.$refs.fileInput) {
        this.$refs.fileInput.value = '';
      }
    },

    // 格式化日期时间
    formatDateTime(datetime) {
      return new Date(datetime).toLocaleString();
    },

    // 获取楼栋列表
    async fetchBuildings() {
      try {
        const response = await fetch('/api/buildings', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });
        const result = await response.json();
        if (result.success) {
          this.buildings = result.data;
        }
      } catch (error) {
        console.error('获取楼栋列表失败:', error);
      }
    },

    // 获取报修记录
    async fetchMaintenanceRecords() {
      try {
        const response = await fetch('/api/maintenance/my-records', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });
        const result = await response.json();
        if (result.success) {
          this.maintenanceRecords = result.data;
        }
      } catch (error) {
        console.error('获取报修记录失败:', error);
      }
    }
  },
  async created() {
    await this.fetchBuildings();
    await this.fetchMaintenanceRecords();
  }
}
</script>

<style scoped>
.maintenance {
  min-height: 100vh;
  background-color: #f7fafc;
}

.main-content {
  padding: 2rem 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

.content-wrapper {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

h2, h3 {
  color: #2d3748;
  margin-bottom: 2rem;
}

/* 表单样式 */
.maintenance-form {
  background: #f8fafc;
  padding: 2rem;
  border-radius: 0.5rem;
  margin-bottom: 2rem;
}

.form-row {
  display: flex;
  gap: 2rem;
  margin-bottom: 1.5rem;
}

.form-group {
  flex: 1;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #4a5568;
  font-weight: 500;
}

.required {
  color: #e53e3e;
  margin-left: 0.25rem;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #e2e8f0;
  border-radius: 0.375rem;
  font-size: 1rem;
}

.form-group textarea {
  min-height: 120px;
  resize: vertical;
}

/* 图片上传样式 */
.upload-area {
  border: 2px dashed #e2e8f0;
  padding: 1.5rem;
  border-radius: 0.5rem;
  text-align: center;
}

.preview-images {
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
  flex-wrap: wrap;
}

.preview-item {
  position: relative;
  width: 100px;
  height: 100px;
}

.preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 0.25rem;
}

.remove-btn {
  position: absolute;
  top: -0.5rem;
  right: -0.5rem;
  background: #e53e3e;
  color: white;
  border: none;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-tip {
  margin-top: 0.5rem;
  color: #718096;
  font-size: 0.875rem;
}

/* 按钮样式 */
.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 2rem;
}

.submit-btn,
.cancel-btn {
  padding: 0.75rem 2rem;
  border-radius: 0.375rem;
  font-weight: 500;
  cursor: pointer;
}

.submit-btn {
  background: #48bb78;
  color: white;
  border: none;
}

.cancel-btn {
  background: #e2e8f0;
  color: #4a5568;
  border: none;
}

/* 记录表格样式 */
.records-table {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #e2e8f0;
}

th {
  background: #f7fafc;
  font-weight: 600;
  color: #4a5568;
}

/* 状态标签样式 */
.urgency-badge,
.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 1rem;
  font-size: 0.875rem;
  font-weight: 500;
}

.urgency-badge.紧急 {
  background: #fed7d7;
  color: #c53030;
}

.urgency-badge.一般 {
  background: #feebc8;
  color: #c05621;
}

.urgency-badge.低 {
  background: #c6f6d5;
  color: #2f855a;
}

.status-badge.待处理 {
  background: #bee3f8;
  color: #2c5282;
}

.status-badge.处理中 {
  background: #fefcbf;
  color: #975a16;
}

.status-badge.已完成 {
  background: #c6f6d5;
  color: #2f855a;
}

@media (max-width: 768px) {
  .form-row {
    flex-direction: column;
    gap: 1rem;
  }
  
  .records-table {
    font-size: 0.875rem;
  }
  
  th, td {
    padding: 0.75rem;
  }
}
</style> 