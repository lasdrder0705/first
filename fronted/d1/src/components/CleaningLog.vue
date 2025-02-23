<template>
  <div class="cleaning-log">
    <nav-bar />
    <main class="main-content">
      <div class="container">
        <div class="content-wrapper">
          <h2>清洁日志</h2>

          <!-- 加载状态 -->
          <div v-if="loading" class="loading">
            加载中...
          </div>
          <div v-else-if="error" class="error-message">
            {{ error }}
          </div>

          <!-- 今日任务 -->
          <div class="task-section">
            <div class="section-header">
              <h3>今日清洁任务</h3>
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
                   @click="handleTaskClick(task)">
                <div class="task-header">
                  <span class="task-time">{{ task.timeSlot }}</span>
                  <span :class="['status-badge', task.status]">
                    {{ getStatusText(task.status) }}
                  </span>
                </div>
                <div class="task-content">
                  <p><strong>清洁区域：</strong>{{ task.area }}</p>
                  <p><strong>重点事项：</strong>{{ task.focus }}</p>
                </div>
                <div v-if="task.assignedTo" class="assigned-info">
                  指派给: {{ getCleanerName(task.assignedTo) }}
                </div>
                <button v-if="task.status === 'pending' && canStartTask(task)"
                        @click.stop="startCleaning(task)"
                        class="start-btn">
                  开始清洁
                </button>
                <div v-if="task.status === 'ongoing'" class="ongoing-hint">
                  点击卡片继续填写清洁记录
                </div>
              </div>
            </div>
          </div>

          <!-- 清洁记录表单 -->
          <div v-if="activeTask" class="cleaning-form">
            <h3>清洁记录填写</h3>
            <form @submit.prevent="handleSubmit">
              <div class="form-row">
                <div class="form-group">
                  <label>清洁时间段</label>
                  <input type="text" :value="activeTask.timeSlot" disabled />
                </div>
                <div class="form-group">
                  <label>清洁区域</label>
                  <input type="text" :value="activeTask.area" disabled />
                </div>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label>开始时间</label>
                  <input type="text" :value="formatTime(cleaningRecord.startTime)" disabled />
                </div>
                <div class="form-group">
                  <label>结束时间 <span class="required">*</span></label>
                  <input type="datetime-local" 
                         v-model="cleaningRecord.endTime"
                         required />
                </div>
              </div>

              <div class="form-group">
                <label>清洁内容 <span class="required">*</span></label>
                <textarea 
                  v-model="cleaningRecord.content"
                  placeholder="请详细描述清洁工作内容..."
                  required
                ></textarea>
              </div>

              <div class="form-group">
                <label>卫生情况</label>
                <div class="condition-section">
                  <div class="radio-group">
                    <label>
                      <input type="radio" 
                             v-model="cleaningRecord.condition" 
                             value="good" /> 良好
                    </label>
                    <label>
                      <input type="radio" 
                             v-model="cleaningRecord.condition" 
                             value="normal" /> 一般
                    </label>
                    <label>
                      <input type="radio" 
                             v-model="cleaningRecord.condition" 
                             value="poor" /> 较差
                    </label>
                  </div>
                  <textarea 
                    v-if="cleaningRecord.condition === 'poor'"
                    v-model="cleaningRecord.conditionDesc"
                    placeholder="请说明卫生情况较差的原因..."
                    required
                  ></textarea>
                </div>
              </div>

              <div class="form-group">
                <label>设施损坏情况</label>
                <div class="damage-section">
                  <div class="radio-group">
                    <label>
                      <input type="radio" 
                             v-model="cleaningRecord.hasDamage" 
                             :value="false" /> 无损坏
                    </label>
                    <label>
                      <input type="radio" 
                             v-model="cleaningRecord.hasDamage" 
                             :value="true" /> 发现损坏
                    </label>
                  </div>
                  <textarea 
                    v-if="cleaningRecord.hasDamage"
                    v-model="cleaningRecord.damageDesc"
                    placeholder="请详细描述设施损坏情况..."
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
                        @click="cancelCleaning">
                  取消
                </button>
              </div>
            </form>
          </div>

          <!-- 历史记录 -->
          <div class="history-section">
            <h3>清洁记录</h3>
            <div class="records-table-wrapper">
              <table class="records-table">
                <thead>
                  <tr>
                    <th>日期</th>
                    <th>时间段</th>
                    <th>区域</th>
                    <th>保洁人员</th>
                    <th>卫生情况</th>
                    <th>设施损坏</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="record in cleaningRecords" :key="record.id">
                    <td>{{ formatDate(record.startTime) }}</td>
                    <td>{{ record.timeSlot }}</td>
                    <td>{{ record.area }}</td>
                    <td>{{ record.cleanerName }}</td>
                    <td>
                      <span :class="['condition-badge', record.condition]">
                        {{ getConditionText(record.condition) }}
                      </span>
                    </td>
                    <td>
                      <span :class="['damage-badge', record.hasDamage ? 'yes' : 'no']">
                        {{ record.hasDamage ? '有损坏' : '无损坏' }}
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
          <h3>清洁记录详情</h3>
          <div class="detail-content">
            <p><strong>清洁时间：</strong>{{ formatDateTime(selectedRecord.startTime) }} - {{ formatDateTime(selectedRecord.endTime) }}</p>
            <p><strong>清洁区域：</strong>{{ selectedRecord.area }}</p>
            <p><strong>清洁内容：</strong>{{ selectedRecord.content }}</p>
            <p><strong>卫生情况：</strong>{{ getConditionText(selectedRecord.condition) }}</p>
            <p v-if="selectedRecord.condition === 'poor'"><strong>情况说明：</strong>{{ selectedRecord.conditionDesc }}</p>
            <p><strong>设施损坏：</strong>{{ selectedRecord.hasDamage ? '有' : '无' }}</p>
            <p v-if="selectedRecord.hasDamage"><strong>损坏说明：</strong>{{ selectedRecord.damageDesc }}</p>
            <div v-if="selectedRecord.images && selectedRecord.images.length" class="detail-images">
              <h4>现场照片：</h4>
              <div class="image-grid">
                <img v-for="(url, index) in selectedRecord.images" 
                     :key="index"
                     :src="url"
                     alt="清洁照片" />
              </div>
            </div>
          </div>
          <button class="close-btn" @click="closeDetail">关闭</button>
        </div>
      </div>

      <!-- 添加发布任务的弹窗 -->
      <div v-if="showPublishTaskModal" class="modal">
        <div class="modal-content">
          <h3>发布清洁任务</h3>
          <form @submit.prevent="handlePublishTask">
            <div class="form-group">
              <label>时间段 <span class="required">*</span></label>
              <input type="text" 
                     v-model="newTask.timeSlot" 
                     placeholder="例如：早班 (06:00-14:00)"
                     required />
            </div>
            <div class="form-group">
              <label>清洁区域 <span class="required">*</span></label>
              <input type="text" 
                     v-model="newTask.area" 
                     placeholder="请输入清洁区域"
                     required />
            </div>
            <div class="form-group">
              <label>重点事项</label>
              <textarea v-model="newTask.focus" 
                        placeholder="请输入重点关注事项"></textarea>
            </div>
            <div class="form-group">
              <label>指派给</label>
              <select v-model="newTask.assignedTo">
                <option value="">不指定</option>
                <option v-for="cleaner in cleaners" 
                        :key="cleaner.id" 
                        :value="cleaner.id">
                  {{ cleaner.nickname || cleaner.username }} ({{ maskIdCard(cleaner.username) }})
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
  name: 'CleaningLog',
  components: {
    NavBar
  },
  data() {
    return {
      loading: true,
      error: null,
      todayTasks: [],
      activeTask: null,
      cleaningRecord: null,
      imagePreviewUrls: [],
      cleaningRecords: [],
      showDetailModal: false,
      selectedRecord: null,
      showPublishTaskModal: false,
      newTask: {
        timeSlot: '',
        area: '',
        focus: '',
        assignedTo: ''
      },
      cleaners: [],
      isAdmin: false
    }
  },
  methods: {
    // 获取今日任务
    async fetchTodayTasks() {
      this.loading = true;
      this.error = null;
      try {
        const response = await fetch(`${API_BASE_URL}/api/cleaning-tasks/today`, {
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

    // 开始清洁
    async startCleaning(task) {
      try {
        const response = await fetch(`${API_BASE_URL}/api/cleaning-tasks/${task.id}/start`, {
          method: 'PUT',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
            'Content-Type': 'application/json'
          }
        });

        if (!response.ok) {
          const error = await response.json();
          throw new Error(error.message || '开始清洁失败');
        }

        const result = await response.json();
        if (result.success) {
          this.activeTask = task;
          this.cleaningRecord = {
            taskId: task.id,
            startTime: new Date(),
            endTime: null,
            content: '',
            condition: 'good',
            conditionDesc: '',
            hasDamage: false,
            damageDesc: '',
            images: []
          };
          this.imagePreviewUrls = [];
          task.status = 'ongoing';
        } else {
          throw new Error(result.message);
        }
      } catch (error) {
        console.error('开始清洁失败:', error);
        alert('开始清洁失败: ' + (error.message || '请稍后重试'));
      }
    },

    // 取消清洁
    cancelCleaning() {
      if (!confirm('确定要取消本次清洁记录吗？')) return;
      this.activeTask.status = 'pending';
      this.activeTask = null;
      this.cleaningRecord = null;
      this.imagePreviewUrls = [];
    },

    // 处理图片上传
    handleFileChange(event) {
      const files = Array.from(event.target.files);
      if (files.length > 5) {
        alert('最多只能上传5张图片');
        return;
      }

      const validFiles = files.filter(file => {
        const isValidType = ['image/jpeg', 'image/png'].includes(file.type);
        const isValidSize = file.size <= 5 * 1024 * 1024; // 5MB
        return isValidType && isValidSize;
      });

      if (validFiles.length !== files.length) {
        alert('请上传jpg或png格式的图片，且大小不超过5MB');
        return;
      }

      this.cleaningRecord.images = validFiles;
      this.imagePreviewUrls = validFiles.map(file => URL.createObjectURL(file));
    },

    // 移除图片
    removeImage(index) {
      this.cleaningRecord.images.splice(index, 1);
      URL.revokeObjectURL(this.imagePreviewUrls[index]);
      this.imagePreviewUrls.splice(index, 1);
    },

    // 提交清洁记录
    async handleSubmit() {
      try {
        if (!this.cleaningRecord.endTime) {
          alert('请选择结束时间');
          return;
        }

        const endTime = new Date(this.cleaningRecord.endTime);
        if (endTime <= this.cleaningRecord.startTime) {
          alert('结束时间必须晚于开始时间');
          return;
        }

        const formData = new FormData();
        formData.append('taskId', this.cleaningRecord.taskId.toString());
        formData.append('startTime', this.cleaningRecord.startTime.getTime().toString());
        formData.append('endTime', endTime.getTime().toString());
        formData.append('content', this.cleaningRecord.content.trim());
        formData.append('condition', this.cleaningRecord.condition);
        formData.append('hasDamage', this.cleaningRecord.hasDamage.toString());

        if (this.cleaningRecord.condition === 'poor') {
          formData.append('conditionDesc', this.cleaningRecord.conditionDesc.trim());
        }

        if (this.cleaningRecord.hasDamage) {
          formData.append('damageDesc', this.cleaningRecord.damageDesc.trim());
        }

        if (this.cleaningRecord.images.length > 0) {
          this.cleaningRecord.images.forEach(file => {
            formData.append('images', file);
          });
        }

        const response = await fetch('/api/cleaning-logs', {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          body: formData
        });

        const result = await response.json();
        if (!response.ok) {
          throw new Error(result.message || result.error || '提交失败');
        }

        if (result.success) {
          alert('清洁记录提交成功');
          this.activeTask = null;
          this.cleaningRecord = null;
          this.imagePreviewUrls = [];
          await this.fetchCleaningRecords();
          await this.fetchTodayTasks();
        } else {
          throw new Error(result.message || '提交失败');
        }
      } catch (error) {
        console.error('提交清洁记录失败:', error);
        alert('提交失败: ' + (error.message || '请稍后重试'));
      }
    },

    // 获取清洁记录
    async fetchCleaningRecords() {
      try {
        const response = await fetch('/api/cleaning-logs', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });
        const result = await response.json();
        if (result.success) {
          this.cleaningRecords = result.data;
        } else {
          throw new Error(result.message);
        }
      } catch (error) {
        console.error('获取清洁记录失败:', error);
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
        'pending': '待清洁',
        'ongoing': '清洁中',
        'completed': '已完成'
      };
      return statusMap[status] || status;
    },

    // 获取卫生情况文本
    getConditionText(condition) {
      const conditionMap = {
        'good': '良好',
        'normal': '一般',
        'poor': '较差'
      };
      return conditionMap[condition] || condition;
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

    // 添加新方法
    handleTaskClick(task) {
      if (task.status === 'ongoing') {
        this.activeTask = task;
        // 如果还没有清洁记录，则初始化一个
        if (!this.cleaningRecord) {
          this.cleaningRecord = {
            taskId: task.id,
            startTime: new Date(),
            endTime: null,
            content: '',
            condition: 'good',
            conditionDesc: '',
            hasDamage: false,
            damageDesc: '',
            images: []
          };
        }
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

    // 获取有清洁权限的员工列表
    async fetchCleaners() {
      try {
        const response = await fetch('/api/employees', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });
        const result = await response.json();
        if (result.success) {
          // 过滤出有清洁权限的员工
          this.cleaners = result.data.filter(emp => {
            try {
              const permissions = JSON.parse(emp.permissions || '[]');
              return permissions.includes('公共区域清洁日志');
            } catch (e) {
              console.error('解析权限失败:', e);
              return false;
            }
          });
          console.log('获取到的保洁员列表:', this.cleaners); // 调试用
        }
      } catch (error) {
        console.error('获取保洁员列表失败:', error);
        throw error;
      }
    },

    // 判断当前用户是否可以开始任务
    canStartTask(task) {
      if (!task.assignedTo) return true; // 未指派任务任何人可以开始
      
      const token = localStorage.getItem('token');
      const tokenData = this.parseJwt(token);
      const currentUserId = this.cleaners.find(c => c.username === tokenData.sub)?.id;
      return task.assignedTo === currentUserId;
    },

    // 获取保洁员姓名
    getCleanerName(cleanerId) {
      const cleaner = this.cleaners.find(c => c.id === cleanerId);
      return cleaner ? (cleaner.nickname || cleaner.username) : '未知';
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
          alert('时间段和清洁区域为必填项');
          return;
        }

        const response = await fetch(`${API_BASE_URL}/api/cleaning-tasks`, {
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
      
      // 不管是否是管理员，都需要获取保洁员列表
      await this.fetchCleaners();
      
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
.cleaning-log {
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

h2 {
  color: #2d3748;
  margin-bottom: 2rem;
}

.placeholder-content {
  text-align: center;
  padding: 3rem;
  color: #718096;
}

.placeholder-content p {
  margin: 0.5rem 0;
}

.loading {
  text-align: center;
  padding: 2rem;
  color: #718096;
}

.error-message {
  text-align: center;
  padding: 2rem;
  color: #e53e3e;
}

.task-section {
  margin-bottom: 3rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.task-section h3 {
  font-size: 1.25rem;
  color: #2d3748;
  margin-bottom: 1.5rem;
  padding-bottom: 0.75rem;
  border-bottom: 2px solid #e2e8f0;
}

.task-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

.task-card {
  background: white;
  border-radius: 16px;
  padding: 1.5rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  border: 1px solid #e2e8f0;
  transition: all 0.3s ease;
}

.task-card.pending {
  background: linear-gradient(to bottom right, #ffffff, #f7fafc);
}

.task-card.ongoing {
  background: linear-gradient(to bottom right, #ffffff, #f0fff4);
  border: 2px solid #48bb78;
}

.task-card.completed {
  background: #f7fafc;
  opacity: 0.85;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.task-time {
  font-size: 1.2rem;
  font-weight: 600;
  color: #2d3748;
}

.status-badge {
  padding: 0.5rem 1.25rem;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.status-badge.pending {
  background-color: #fff5f5;
  color: #c53030;
  border: 1px solid #fed7d7;
}

.status-badge.ongoing {
  background-color: #f0fff4;
  color: #2f855a;
  border: 1px solid #c6f6d5;
}

.status-badge.completed {
  background-color: #f7fafc;
  color: #4a5568;
  border: 1px solid #e2e8f0;
}

.task-content {
  margin: 1.25rem 0;
  line-height: 1.7;
}

.task-content p {
  margin: 0.75rem 0;
  color: #4a5568;
}

.task-content p strong {
  color: #2d3748;
  font-weight: 600;
  margin-right: 0.5rem;
}

.start-btn {
  background-color: #48bb78;
  color: white;
  padding: 0.875rem;
  border-radius: 12px;
  border: none;
  cursor: pointer;
  font-weight: 600;
  font-size: 1rem;
  transition: all 0.3s ease;
  width: 100%;
  margin-top: 1.25rem;
  text-align: center;
}

.start-btn:hover {
  background-color: #38a169;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(72, 187, 120, 0.2);
}

.ongoing-hint {
  color: #48bb78;
  font-size: 0.95rem;
  text-align: center;
  margin-top: 1.25rem;
  font-style: italic;
  padding: 0.75rem;
  background-color: #f0fff4;
  border-radius: 8px;
}

.cleaning-form {
  background-color: #fff;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  margin-top: 2rem;
}

.form-row {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
}

.form-group {
  flex: 1;
}

.form-group label {
  display: block;
  margin-bottom: 0.75rem;
  color: #2d3748;
  font-weight: 500;
  font-size: 0.95rem;
}

.form-group .required {
  color: #e53e3e;
  margin-left: 0.25rem;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  transition: border-color 0.3s ease;
}

.form-group input:focus,
.form-group textarea:focus {
  border-color: #48bb78;
  outline: none;
  box-shadow: 0 0 0 3px rgba(72, 187, 120, 0.2);
}

.condition-section {
  margin-bottom: 2rem;
  padding: 1.5rem;
  background-color: #f8fafc;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.radio-group {
  display: flex;
  gap: 2rem;
  padding: 0.5rem 0;
}

.radio-group label {
  display: flex;
  align-items: center;
  cursor: pointer;
  font-weight: normal;
  color: #4a5568;
  transition: color 0.2s ease;
}

.radio-group label:hover {
  color: #2d3748;
}

.radio-group input[type="radio"] {
  appearance: none;
  -webkit-appearance: none;
  width: 1.2rem;
  height: 1.2rem;
  border: 2px solid #cbd5e0;
  border-radius: 50%;
  margin-right: 0.75rem;
  position: relative;
  transition: all 0.2s ease;
}

.radio-group input[type="radio"]:checked {
  border-color: #48bb78;
  background-color: #48bb78;
}

.radio-group input[type="radio"]:checked::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 0.5rem;
  height: 0.5rem;
  background-color: white;
  border-radius: 50%;
}

.damage-section {
  margin-bottom: 2rem;
  padding: 1.5rem;
  background-color: #f8fafc;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.upload-area {
  margin-top: 0.5rem;
  padding: 1.5rem;
  background-color: #f8fafc;
  border-radius: 12px;
  border: 2px dashed #e2e8f0;
  text-align: center;
  transition: all 0.3s ease;
}

.upload-area:hover {
  border-color: #48bb78;
  background-color: #f0fff4;
}

.upload-tip {
  margin-top: 1rem;
  color: #718096;
  font-size: 0.9rem;
}

.preview-images {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  margin-top: 1rem;
}

.preview-item {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-btn {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.remove-btn:hover {
  background-color: rgba(0, 0, 0, 0.7);
  transform: scale(1.1);
}

.form-actions {
  text-align: right;
}

.submit-btn,
.cancel-btn {
  padding: 0.5rem 1rem;
  border-radius: 4px;
  border: none;
  cursor: pointer;
}

.submit-btn {
  background-color: #48bb78;
  color: white;
}

.cancel-btn {
  background-color: #e53e3e;
  color: white;
}

.history-section {
  margin: 2rem 0;
  padding: 2rem;
  background: #f8fafc;
  border-radius: 0 0 8px 8px;
}

.history-section h3 {
  font-size: 1.25rem;
  color: #2d3748;
  margin-bottom: 1.5rem;
  padding: 0 0 0.75rem;
  border-bottom: 2px solid #e2e8f0;
}

.records-table-wrapper {
  width: 100%;
  overflow-x: auto;
  border-radius: 8px;
  background: white;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.records-table {
  width: 100%;
  min-width: 1000px;
  border-collapse: separate;
  border-spacing: 0;
  background: white;
  border-radius: 8px;
  overflow-x: auto;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.records-table th,
.records-table td {
  padding: 1rem 1.5rem;
}

.records-table th {
  background-color: #f1f5f9;
  color: #475569;
  font-weight: 600;
  text-align: left;
  border-bottom: 2px solid #e2e8f0;
  white-space: nowrap;
}

.records-table td {
  color: #4a5568;
  border-bottom: 1px solid #e2e8f0;
}

.records-table tr:last-child td {
  border-bottom: none;
}

.records-table tbody tr {
  transition: background-color 0.2s ease;
}

.records-table tbody tr:hover {
  background-color: #f8fafc;
}

/* 状态标签样式 */
.condition-badge,
.damage-badge {
  padding: 0.375rem 0.75rem;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 500;
}

.condition-badge.good {
  background-color: #f0fff4;
  color: #2f855a;
  border: 1px solid #c6f6d5;
}

.condition-badge.normal {
  background-color: #fffaf0;
  color: #c05621;
  border: 1px solid #feebc8;
}

.condition-badge.poor {
  background-color: #fff5f5;
  color: #c53030;
  border: 1px solid #fed7d7;
}

.damage-badge {
  font-size: 0.875rem;
}

.damage-badge.yes {
  background-color: #fff5f5;
  color: #c53030;
  border: 1px solid #fed7d7;
}

.damage-badge.no {
  background-color: #f0fff4;
  color: #2f855a;
  border: 1px solid #c6f6d5;
}

/* 查看按钮样式 */
.view-btn {
  background-color: #4299e1;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all 0.2s ease;
}

.view-btn:hover {
  background-color: #3182ce;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(66, 153, 225, 0.2);
}

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
}

.modal-content {
  background-color: white;
  padding: 2rem;
  border-radius: 8px;
  max-width: 600px;
  width: 90%;
  max-height: 90vh;
  overflow: auto;
}

.detail-content {
  margin-bottom: 1rem;
}

.detail-images {
  margin-top: 1rem;
}

.image-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.image-grid img {
  width: calc(33.33% - 0.33rem);
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
}

.close-btn {
  background-color: #e53e3e;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  border: none;
  cursor: pointer;
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

.modal-content .form-group {
  margin-bottom: 1.5rem;
}

.modal-content input,
.modal-content textarea,
.modal-content select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #e2e8f0;
  border-radius: 0.375rem;
  font-size: 1rem;
}

.modal-content textarea {
  min-height: 100px;
  resize: vertical;
}

.modal-content .form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 2rem;
}

.modal-content .submit-btn,
.modal-content .cancel-btn {
  padding: 0.75rem 2rem;
  border-radius: 0.375rem;
  font-weight: 500;
  cursor: pointer;
}

.modal-content .submit-btn {
  background: #48bb78;
  color: white;
  border: none;
}

.modal-content .cancel-btn {
  background: #e2e8f0;
  color: #4a5568;
  border: none;
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