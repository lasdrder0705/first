<template>
  <div class="patrol-log">
    <nav-bar />
    <main class="main-content">
      <div class="container">
        <div class="content-wrapper">
          <h2>安全巡逻日志</h2>

          <!-- 在任务列表上方添加加载状态 -->
          <div v-if="loading" class="loading">
            加载中...
          </div>
          <div v-else-if="error" class="error-message">
            {{ error }}
          </div>

          <!-- 今日任务 -->
          <div class="task-section">
            <div class="section-header">
              <h3>今日巡逻任务</h3>
              <button v-if="isAdmin" 
                      @click="showPublishTaskModal = true" 
                      class="publish-btn">
                发布任务
              </button>
            </div>
            <div class="task-cards">
              <div v-for="task in todayTasks" 
                   :key="task.id" 
                   :class="['task-card', task.status]"
                   @click="task.status === 'ongoing' ? continuePatrol(task) : null">
                <div class="task-header">
                  <span class="task-time">{{ task.timeSlot }}</span>
                  <span :class="['status-badge', task.status]">
                    {{ getStatusText(task.status) }}
                  </span>
                </div>
                <div class="task-content">
                  <p><strong>巡逻区域：</strong>{{ task.area }}</p>
                  <p><strong>重点关注：</strong>{{ task.focus }}</p>
                </div>
                <div v-if="task.assignedTo" class="assigned-info">
                  指派给: {{ getGuardName(task.assignedTo) }}
                </div>
                <button v-if="task.status === 'pending' && canStartTask(task)"
                        @click.stop="startPatrol(task)"
                        class="start-btn">
                  开始巡逻
                </button>
                <div v-else-if="task.status === 'ongoing'" class="continue-hint">
                  点击继续填写巡逻记录
                </div>
              </div>
            </div>
          </div>

          <!-- 巡逻记录表单 -->
          <div v-if="activeTask" class="patrol-form">
            <h3>巡逻记录填写</h3>
            <form @submit.prevent="handleSubmit">
              <div class="form-row">
                <div class="form-group">
                  <label>巡逻时间段</label>
                  <input type="text" :value="activeTask.timeSlot" disabled />
                </div>
                <div class="form-group">
                  <label>巡逻区域</label>
                  <input type="text" :value="activeTask.area" disabled />
                </div>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label>开始时间</label>
                  <input type="text" :value="formatTime(patrolRecord.startTime)" disabled />
                </div>
                <div class="form-group">
                  <label>结束时间 <span class="required">*</span></label>
                  <input type="datetime-local" 
                         v-model="patrolRecord.endTime"
                         required />
                </div>
              </div>

              <div class="form-group">
                <label>巡逻路线 <span class="required">*</span></label>
                <textarea 
                  v-model="patrolRecord.route"
                  placeholder="请详细描述巡逻路线..."
                  required
                ></textarea>
              </div>

              <div class="form-group">
                <label>异常情况</label>
                <div class="abnormal-section">
                  <div class="radio-group">
                    <label>
                      <input type="radio" 
                             v-model="patrolRecord.hasAbnormal" 
                             :value="false" /> 无异常
                    </label>
                    <label>
                      <input type="radio" 
                             v-model="patrolRecord.hasAbnormal" 
                             :value="true" /> 发现异常
                    </label>
                  </div>
                  <textarea 
                    v-if="patrolRecord.hasAbnormal"
                    v-model="patrolRecord.abnormalDesc"
                    placeholder="请详细描述发现的异常情况..."
                    required
                  ></textarea>
                </div>
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
                  提交记录
                </button>
                <button type="button" 
                        class="cancel-btn"
                        @click="cancelPatrol">
                  取消
                </button>
              </div>
            </form>
          </div>

          <!-- 历史记录 -->
          <div class="history-section">
            <h3>巡逻记录</h3>
            <div class="records-table">
              <table>
                <thead>
                  <tr>
                    <th>日期</th>
                    <th>时间段</th>
                    <th>区域</th>
                    <th>巡逻人员</th>
                    <th>开始时间</th>
                    <th>结束时间</th>
                    <th>是否异常</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="record in patrolRecords" :key="record.id">
                    <td>{{ formatDate(record.startTime) }}</td>
                    <td>{{ record.timeSlot }}</td>
                    <td>{{ record.area }}</td>
                    <td>{{ record.patrollerName }}</td>
                    <td>{{ formatTime(record.startTime) }}</td>
                    <td>{{ formatTime(record.endTime) }}</td>
                    <td>
                      <span :class="['abnormal-badge', record.hasAbnormal ? 'yes' : 'no']">
                        {{ record.hasAbnormal ? '有异常' : '正常' }}
                      </span>
                    </td>
                    <td>
                      <button class="view-btn" @click="viewRecord(record)">
                        查看详情
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      <!-- 记录详情弹窗 -->
      <div v-if="showDetailModal" class="modal">
        <div class="modal-content">
          <h3>巡逻记录详情</h3>
          <div class="detail-content">
            <p><strong>巡逻时间：</strong>{{ formatDateTime(selectedRecord.startTime) }} - {{ formatDateTime(selectedRecord.endTime) }}</p>
            <p><strong>巡逻区域：</strong>{{ selectedRecord.area }}</p>
            <p><strong>巡逻路线：</strong>{{ selectedRecord.route }}</p>
            <p><strong>异常情况：</strong>{{ selectedRecord.hasAbnormal ? selectedRecord.abnormalDesc : '无' }}</p>
            <div v-if="selectedRecord.images && selectedRecord.images.length" class="detail-images">
              <h4>现场照片：</h4>
              <div class="image-grid">
                <img v-for="(url, index) in selectedRecord.images" 
                     :key="index"
                     :src="url"
                     alt="巡逻照片" />
              </div>
            </div>
          </div>
          <button class="close-btn" @click="closeDetail">关闭</button>
        </div>
      </div>

      <!-- 添加发布任务的弹窗 -->
      <div v-if="showPublishTaskModal" class="modal">
        <div class="modal-content">
          <h3>发布巡逻任务</h3>
          <form @submit.prevent="handlePublishTask">
            <div class="form-group">
              <label>时间段 <span class="required">*</span></label>
              <input type="text" 
                     v-model="newTask.timeSlot" 
                     placeholder="例如：早班 (06:00-14:00)"
                     required />
            </div>
            <div class="form-group">
              <label>巡逻区域 <span class="required">*</span></label>
              <input type="text" 
                     v-model="newTask.area" 
                     placeholder="请输入巡逻区域"
                     required />
            </div>
            <div class="form-group">
              <label>重点关注</label>
              <textarea v-model="newTask.focus" 
                        placeholder="请输入重点关注事项"></textarea>
            </div>
            <div class="form-group">
              <label>指派给</label>
              <select v-model="newTask.assignedTo">
                <option value="">不指定</option>
                <option v-for="guard in securityGuards" 
                        :key="guard.id" 
                        :value="guard.id">
                  {{ guard.nickname || guard.username }} ({{ maskIdCard(guard.username) }})
                </option>
              </select>
            </div>
            <div class="form-actions">
              <button type="submit" class="submit-btn">发布</button>
              <button type="button" 
                      @click="showPublishTaskModal = false" 
                      class="cancel-btn">
                取消
              </button>
            </div>
          </form>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import NavBar from './NavBar.vue'
import { API_BASE_URL } from '../config'

export default {
  name: 'PatrolLog',
  components: {
    NavBar
  },
  data() {
    return {
      loading: true,
      error: null,
      todayTasks: [],
      activeTask: null,
      patrolRecord: null,
      imagePreviewUrls: [],
      patrolRecords: [],
      showDetailModal: false,
      selectedRecord: null,
      isAdmin: false,
      showPublishTaskModal: false,
      newTask: {
        timeSlot: '',
        area: '',
        focus: '',
        assignedTo: ''
      },
      securityGuards: []
    }
  },
  methods: {
    // 获取今日任务
    async fetchTodayTasks() {
      this.loading = true;
      this.error = null;
      try {
        const response = await fetch(`${API_BASE_URL}/api/patrol-tasks/today`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });
        const result = await response.json();
        if (result.success) {
          this.todayTasks = result.data;
        } else {
          throw new Error(result.message);
        }
      } catch (error) {
        console.error('获取今日任务失败:', error);
        this.error = '获取任务失败，请稍后重试';
      } finally {
        this.loading = false;
      }
    },

    // 开始巡逻
    async startPatrol(task) {
      try {
        // 更新任务状态
        const response = await fetch(`${API_BASE_URL}/api/patrol-tasks/${task.id}/start`, {
          method: 'PUT',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
            'Content-Type': 'application/json'  // 添加Content-Type头
          }
        });

        if (!response.ok) {
          const error = await response.json();
          throw new Error(error.message || '开始巡逻失败');
        }

        const result = await response.json();
        if (result.success) {
          this.activeTask = task;
          this.patrolRecord = {
            taskId: task.id,
            startTime: new Date(),
            endTime: null,
            route: '',
            hasAbnormal: false,
            abnormalDesc: '',
            images: []
          };
          this.imagePreviewUrls = [];
          task.status = 'ongoing';
        } else {
          throw new Error(result.message);
        }
      } catch (error) {
        console.error('开始巡逻失败:', error);
        alert('开始巡逻失败: ' + (error.message || '请稍后重试'));
      }
    },

    // 取消巡逻
    async cancelPatrol() {
      try {
        if (!confirm('确定要取消本次巡逻吗？')) return;
        
        // 调用后端接口取消巡逻
        const response = await fetch(`/api/patrol-tasks/${this.activeTask.id}/cancel`, {
          method: 'PUT',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
            'Content-Type': 'application/json'
          }
        });

        if (!response.ok) {
          const error = await response.json();
          throw new Error(error.message || '取消巡逻失败');
        }

        // 重置本地状态
        this.activeTask.status = 'pending';
        this.activeTask = null;
        this.patrolRecord = null;
        this.imagePreviewUrls = [];
        
        // 刷新任务列表
        await this.fetchTodayTasks();
      } catch (error) {
        console.error('取消巡逻失败:', error);
        alert('取消巡逻失败: ' + (error.message || '请稍后重试'));
      }
    },

    // 处理图片上传
    handleFileChange(event) {
      const files = Array.from(event.target.files);
      if (files.length > 5) {
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
        alert('请上传jpg或png格式的图片，且大小不超过5MB');
        return;
      }

      this.patrolRecord.images = validFiles;
      
      // 生成预览URL
      this.imagePreviewUrls = validFiles.map(file => URL.createObjectURL(file));
    },

    // 移除图片
    removeImage(index) {
      this.patrolRecord.images.splice(index, 1);
      URL.revokeObjectURL(this.imagePreviewUrls[index]);
      this.imagePreviewUrls.splice(index, 1);
    },

    // 提交巡逻记录
    async handleSubmit() {
      try {
        if (!this.patrolRecord.endTime) {
          alert('请选择结束时间');
          return;
        }

        const endTime = new Date(this.patrolRecord.endTime);
        if (endTime <= this.patrolRecord.startTime) {
          alert('结束时间必须晚于开始时间');
          return;
        }

        // 构造FormData
        const formData = new FormData();
        formData.append('taskId', String(this.patrolRecord.taskId)); // 转换为字符串
        formData.append('startTime', this.patrolRecord.startTime.toISOString());
        formData.append('endTime', endTime.toISOString());
        formData.append('route', this.patrolRecord.route.trim());
        formData.append('hasAbnormal', String(this.patrolRecord.hasAbnormal)); // 转换为字符串
        
        if (this.patrolRecord.hasAbnormal && this.patrolRecord.abnormalDesc) {
          formData.append('abnormalDesc', this.patrolRecord.abnormalDesc.trim());
        }

        // 处理图片上传
        if (this.patrolRecord.images && this.patrolRecord.images.length > 0) {
          this.patrolRecord.images.forEach(file => {
            formData.append('images', file);
          });
        }

        // 打印FormData内容以便调试
        for (let pair of formData.entries()) {
          console.log('FormData:', pair[0], pair[1]);
        }

        const response = await fetch('http://localhost:8080/api/patrol-logs', {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          body: formData
        });

        const result = await response.json();
        console.log('Response:', result);

        if (!response.ok) {
          throw new Error(result.message || result.error || '提交失败');
        }

        if (result.success) {
          alert('巡逻记录提交成功');
          this.activeTask = null;
          this.patrolRecord = null;
          this.imagePreviewUrls = [];
          await this.fetchPatrolRecords();
          await this.fetchTodayTasks();
        } else {
          throw new Error(result.message || '提交失败');
        }
      } catch (error) {
        console.error('提交巡逻记录失败:', error);
        alert('提交失败: ' + (error.message || '请稍后重试'));
      }
    },

    // 获取巡逻记录
    async fetchPatrolRecords() {
      try {
        const response = await fetch('/api/patrol-logs', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });
        const result = await response.json();
        if (result.success) {
          this.patrolRecords = result.data;
        } else {
          throw new Error(result.message);
        }
      } catch (error) {
        console.error('获取巡逻记录失败:', error);
      }
    },

    // 查看记录详情
    viewRecord(record) {
      // 如果后台返回的记录中有 imageUrls 字段（保存为 JSON 字符串），则转换成数组赋值给 images 属性
      if (record.imageUrls && typeof record.imageUrls === 'string') {
        try {
          record.images = JSON.parse(record.imageUrls);
        } catch (e) {
          console.error('解析 imageUrls 失败:', e);
          record.images = [];
        }
      } else {
        // 如果 record.imageUrls 不是字符串，则直接赋值（或默认为空数组）
        record.images = record.imageUrls || [];
      }
      this.selectedRecord = record;
      this.showDetailModal = true;
    },

    // 关闭详情弹窗
    closeDetail() {
      this.showDetailModal = false;
      this.selectedRecord = null;
    },

    // 格式化日期
    formatDate(date) {
      return new Date(date).toLocaleDateString();
    },

    // 格式化时间
    formatTime(date) {
      return new Date(date).toLocaleTimeString();
    },

    // 格式化日期时间
    formatDateTime(date) {
      return new Date(date).toLocaleString();
    },

    // 获取状态文本
    getStatusText(status) {
      const statusMap = {
        'pending': '待巡逻',
        'ongoing': '巡逻中',
        'completed': '已完成'
      };
      return statusMap[status] || status;
    },

    // 定时刷新任务列表
    startTaskRefresh() {
      this.taskRefreshInterval = setInterval(() => {
        this.fetchTodayTasks();
      }, 60000); // 每分钟刷新一次
    },

    // 组件销毁时清除定时器
    clearTaskRefresh() {
      if (this.taskRefreshInterval) {
        clearInterval(this.taskRefreshInterval);
      }
    },

    // 继续巡逻
    async continuePatrol(task) {
      try {
        // 获取进行中的巡逻记录
        const response = await fetch(`/api/patrol-tasks/${task.id}/current-record`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });

        if (!response.ok) {
          const error = await response.json();
          throw new Error(error.message || '获取巡逻记录失败');
        }

        const result = await response.json();
        
        this.activeTask = task;
        if (result.success && result.data) {
          // 如果有进行中的记录，加载已有数据
          this.patrolRecord = {
            taskId: task.id,
            startTime: new Date(result.data.startTime),
            endTime: result.data.endTime ? new Date(result.data.endTime) : null,
            route: result.data.route || '',
            hasAbnormal: result.data.hasAbnormal || false,
            abnormalDesc: result.data.abnormalDesc || '',
            images: []
          };
          // 如果有图片，加载图片预览
          if (result.data.imageUrls) {
            this.imagePreviewUrls = result.data.imageUrls;
          }
        } else {
          // 如果没有记录，创建新的记录
          this.patrolRecord = {
            taskId: task.id,
            startTime: new Date(),
            endTime: null,
            route: '',
            hasAbnormal: false,
            abnormalDesc: '',
            images: []
          };
          this.imagePreviewUrls = [];
        }
      } catch (error) {
        console.error('继续巡逻失败:', error);
        alert('继续巡逻失败: ' + (error.message || '请稍后重试'));
      }
    },

    // 检查是否是管理员
    async checkAdminStatus() {
      try {
        const token = localStorage.getItem('token');
        if (!token) return;
        
        const response = await fetch(`${API_BASE_URL}/api/user/profile`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        const result = await response.json();
        if (result.success) {
          this.isAdmin = result.data.role === 'admin';
        }
      } catch (error) {
        console.error('检查管理员状态失败:', error);
      }
    },

    // 获取有巡逻权限的员工列表
    async fetchSecurityGuards() {
      try {
        const response = await fetch('/api/employees', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });
        const result = await response.json();
        if (result.success) {
          // 过滤出有巡逻权限的员工
          this.securityGuards = result.data.filter(emp => {
            try {
              const permissions = JSON.parse(emp.permissions || '[]');
              return permissions.includes('安全巡逻日志');
            } catch (e) {
              console.error('解析权限失败:', e);
              return false;
            }
          });
          console.log('获取到的保安列表:', this.securityGuards); // 调试用
        }
      } catch (error) {
        console.error('获取保安列表失败:', error);
        throw error;
      }
    },

    // 判断当前用户是否可以开始任务
    canStartTask(task) {
      if (!task.assignedTo) return true; // 未指派任务任何人可以开始
      
      const token = localStorage.getItem('token');
      const tokenData = this.parseJwt(token);
      // 修改这里，使用 username 而不是 sub
      const currentUserId = this.securityGuards.find(g => g.username === tokenData.sub)?.id;
      return task.assignedTo === currentUserId;
    },

    // 获取保安姓名
    getGuardName(guardId) {
      const guard = this.securityGuards.find(g => g.id === guardId);
      return guard ? (guard.nickname || guard.username) : '未知';
    },

    // 掩码身份证号
    maskIdCard(idCard) {
      return idCard.replace(/^(.{6})(?:\d+)(.{4})$/, '$1****$2');
    },

    // 解析JWT token
    parseJwt(token) {
      try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        return JSON.parse(window.atob(base64));
      } catch (e) {
        return null;
      }
    },

    // 发布任务
    async handlePublishTask() {
      try {
        // 验证表单数据
        if (!this.newTask.timeSlot || !this.newTask.area) {
          alert('时间段和巡逻区域为必填项');
          return;
        }

        const response = await fetch(`${API_BASE_URL}/api/patrol-tasks`, {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            time_slot: this.newTask.timeSlot,
            area: this.newTask.area,
            focus: this.newTask.focus || '',
            assigned_to: this.newTask.assignedTo || null
          })
        });

        const result = await response.json();
        if (result.success) {
          alert('任务发布成功');
          this.showPublishTaskModal = false;
          this.newTask = {
            timeSlot: '',
            area: '',
            focus: '',
            assignedTo: ''
          };
          await this.fetchTodayTasks();
        } else {
          throw new Error(result.message);
        }
      } catch (error) {
        alert('发布任务失败: ' + error.message);
      }
    }
  },
  async created() {
    try {
      // 检查是否是管理员
      await this.checkAdminStatus();
      
      // 不管是否是管理员，都需要获取保安列表
      await this.fetchSecurityGuards();
      
      // 获取今日任务
      await this.fetchTodayTasks();
      
      this.loading = false;
    } catch (error) {
      console.error('初始化失败:', error);
      this.error = '加载数据失败';
      this.loading = false;
    }
  },
  beforeDestroy() {
    // 清除定时器
    this.clearTaskRefresh();
  }
}
</script>

<style scoped>
.patrol-log {
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

/* 任务卡片样式 */
.task-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.task-card {
  background: #fff;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
  cursor: pointer;
  transition: all 0.3s ease;
}

.task-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.task-card.ongoing {
  border: 2px solid #fbd38d;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.task-time {
  font-weight: 600;
  color: #2d3748;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 1rem;
  font-size: 0.875rem;
  font-weight: 500;
}

.task-card.pending .status-badge {
  background: #bee3f8;
  color: #2c5282;
}

.task-card.ongoing .status-badge {
  background: #fefcbf;
  color: #975a16;
}

.task-card.completed .status-badge {
  background: #c6f6d5;
  color: #2f855a;
}

.task-content {
  margin-bottom: 1rem;
}

.task-content p {
  margin: 0.5rem 0;
  color: #4a5568;
}

.start-btn {
  width: 100%;
  padding: 0.75rem;
  background: #4299e1;
  color: white;
  border: none;
  border-radius: 0.375rem;
  cursor: pointer;
  font-weight: 500;
}

/* 表单样式 */
.patrol-form {
  background: #f8fafc;
  padding: 2rem;
  border-radius: 0.5rem;
  margin: 2rem 0;
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

/* 异常情况样式 */
.abnormal-section {
  margin-top: 0.5rem;
}

.radio-group {
  display: flex;
  gap: 2rem;
  margin-bottom: 1rem;
}

.radio-group label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
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

.abnormal-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 1rem;
  font-size: 0.875rem;
  font-weight: 500;
}

.abnormal-badge.yes {
  background: #fed7d7;
  color: #c53030;
}

.abnormal-badge.no {
  background: #c6f6d5;
  color: #2f855a;
}

.view-btn {
  padding: 0.5rem 1rem;
  background: #4299e1;
  color: white;
  border: none;
  border-radius: 0.375rem;
  cursor: pointer;
  font-size: 0.875rem;
}

/* 弹窗样式 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 2rem;
  border-radius: 0.5rem;
  max-width: 600px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.detail-content {
  margin: 1.5rem 0;
}

.detail-content p {
  margin: 0.75rem 0;
}

.detail-images {
  margin-top: 1.5rem;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 1rem;
  margin-top: 1rem;
}

.image-grid img {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 0.375rem;
}

.close-btn {
  width: 100%;
  padding: 0.75rem;
  background: #e2e8f0;
  color: #4a5568;
  border: none;
  border-radius: 0.375rem;
  cursor: pointer;
  margin-top: 1.5rem;
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

.loading {
  text-align: center;
  padding: 2rem;
  color: #4a5568;
}

.error-message {
  text-align: center;
  padding: 2rem;
  color: #e53e3e;
  background: #fed7d7;
  border-radius: 0.5rem;
  margin-bottom: 1rem;
}

.continue-hint {
  text-align: center;
  color: #b7791f;
  font-size: 0.9rem;
  margin-top: 1rem;
  padding: 0.5rem;
  background: #fef3c7;
  border-radius: 0.375rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.publish-btn {
  background-color: #4299e1;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 0.375rem;
  border: none;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s ease;
}

.publish-btn:hover {
  background-color: #3182ce;
  transform: translateY(-1px);
}

.assigned-info {
  margin: 0.5rem 0;
  color: #666;
  font-size: 0.9rem;
}

.task-card.assigned {
  border: 2px solid #fbd38d;
}
</style> 