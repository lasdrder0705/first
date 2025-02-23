<template>
  <div class="visitor-log">
    <nav-bar />
    <main class="main-content">
      <div class="container">
        <div class="content-wrapper">
          <h2>来访人员登记</h2>
          
          <!-- 搜索已有访客 -->
          <div class="search-section">
            <div class="search-box">
              <input 
                type="text" 
                v-model="searchQuery" 
                placeholder="输入身份证号/手机号搜索访客..."
                @input="searchVisitor"
              />
              <button class="search-btn">
                <i class="fas fa-search"></i>
              </button>
            </div>
          </div>

          <!-- 访客登记表单 -->
          <div class="visitor-form">
            <h3>{{ isEdit ? '编辑访客信息' : '新访客登记' }}</h3>
            <form @submit.prevent="handleSubmit">
              <div class="form-row">
                <div class="form-group">
                  <label>访客姓名 <span class="required">*</span></label>
                  <input 
                    type="text" 
                    v-model="visitorForm.name"
                    required
                  />
                </div>
                <div class="form-group">
                  <label>身份证号 <span class="required">*</span></label>
                  <input 
                    type="text" 
                    v-model="visitorForm.idCard"
                    required
                  />
                </div>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label>联系电话 <span class="required">*</span></label>
                  <input 
                    type="tel" 
                    v-model="visitorForm.phone"
                    required
                  />
                </div>
                <div class="form-group">
                  <label>访问类型 <span class="required">*</span></label>
                  <select v-model="visitorForm.visitType" required>
                    <option value="">请选择访问类型</option>
                    <option value="业主亲属">业主亲属</option>
                    <option value="业主朋友">业主朋友</option>
                    <option value="快递配送">快递配送</option>
                    <option value="维修服务">维修服务</option>
                    <option value="其他">其他</option>
                  </select>
                </div>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label>访问目的 <span class="required">*</span></label>
                  <input 
                    type="text" 
                    v-model="visitorForm.purpose"
                    required
                  />
                </div>
                <div class="form-group">
                  <label>预计离开时间</label>
                  <input 
                    type="datetime-local" 
                    v-model="visitorForm.expectedLeaveTime"
                  />
                </div>
              </div>

              <!-- 业主关联信息 -->
              <div class="form-row" v-if="['业主亲属', '业主朋友'].includes(visitorForm.visitType)">
                <div class="form-group">
                  <label>关联业主 <span class="required">*</span></label>
                  <select v-model="visitorForm.ownerId" required>
                    <option value="">请选择业主</option>
                    <option v-for="owner in owners" :key="owner.id" :value="owner.id">
                      {{ owner.name }} - {{ owner.building }}{{ owner.doorNumber }}
                    </option>
                  </select>
                </div>
                <div class="form-group">
                  <label>与业主关系</label>
                  <input 
                    type="text" 
                    v-model="visitorForm.relationWithOwner"
                    placeholder="如：父母、朋友等"
                  />
                </div>
              </div>

              <div class="form-group">
                <label>备注</label>
                <textarea 
                  v-model="visitorForm.remarks"
                  placeholder="其他需要记录的信息..."
                ></textarea>
              </div>

              <div class="form-actions">
                <button type="submit" class="submit-btn">
                  {{ isEdit ? '保存修改' : '登记访客' }}
                </button>
                <button type="button" class="cancel-btn" @click="resetForm">
                  取消
                </button>
              </div>
            </form>
          </div>

          <!-- 访客记录列表 -->
          <div class="visitor-records">
            <h3>今日访客记录</h3>
            <div class="records-table">
              <table>
                <thead>
                  <tr>
                    <th>访客姓名</th>
                    <th>身份证号</th>
                    <th>联系电话</th>
                    <th>访问类型</th>
                    <th>访问目的</th>
                    <th>到访时间</th>
                    <th>预计离开时间</th>
                    <th>状态</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="record in visitorRecords" :key="record.id">
                    <td>{{ record.name }}</td>
                    <td>{{ maskIdCard(record.idCard) }}</td>
                    <td>{{ maskPhone(record.phone) }}</td>
                    <td>{{ record.visitType }}</td>
                    <td>{{ record.purpose }}</td>
                    <td>{{ formatDateTime(record.visitTime) }}</td>
                    <td>{{ formatDateTime(record.expectedLeaveTime) }}</td>
                    <td>
                      <span :class="['status-badge', record.status]">
                        {{ record.status }}
                      </span>
                    </td>
                    <td>
                      <div class="action-buttons">
                        <button @click="editRecord(record)" class="edit-btn">
                          <i class="fas fa-edit"></i>
                        </button>
                        <button @click="recordLeave(record)" 
                                class="leave-btn"
                                v-if="record.status === '在访'">
                          <i class="fas fa-sign-out-alt"></i>
                        </button>
                      </div>
                    </td>
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
  name: 'VisitorLog',
  components: {
    NavBar
  },
  data() {
    return {
      searchQuery: '',
      isEdit: false,
      owners: [], // 业主列表
      visitorForm: {
        name: '',
        idCard: '',
        phone: '',
        visitType: '',
        purpose: '',
        expectedLeaveTime: '',
        ownerId: '',
        relationWithOwner: '',
        remarks: ''
      },
      visitorRecords: [
        {
          id: 1,
          name: '张三',
          idCard: '330102199001011234',
          phone: '13800138000',
          visitType: '业主亲属',
          purpose: '探亲',
          visitTime: '2024-03-20 09:00:00',
          expectedLeaveTime: '2024-03-20 18:00:00',
          status: '在访'
        },
        // 更多测试数据...
      ]
    }
  },
  methods: {
    // 搜索访客
    async searchVisitor() {
        if (!this.searchQuery) return;
        
        try {
            const response = await fetch(`/api/visitors/search?query=${this.searchQuery}`, {
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`
                }
            });
            
            const result = await response.json();
            if (result.success) {
                // 如果找到访客，自动填充表单
                if (result.data && result.data.length > 0) {
                    const visitor = result.data[0];
                    this.visitorForm = {
                        name: visitor.name,
                        idCard: visitor.idCard,
                        phone: visitor.phone,
                        visitType: '',
                        purpose: '',
                        expectedLeaveTime: '',
                        ownerId: '',
                        relationWithOwner: '',
                        remarks: ''
                    };
                }
            } else {
                throw new Error(result.message);
            }
        } catch (error) {
            console.error('搜索访客失败:', error);
        }
    },

    // 提交表单
    async handleSubmit() {
        try {
            // 表单验证
            if (!this.visitorForm.name?.trim() ||
                !this.visitorForm.idCard?.trim() ||
                !this.visitorForm.phone?.trim() ||
                !this.visitorForm.visitType?.trim() ||
                !this.visitorForm.purpose?.trim()) {
                alert('请填写所有必填项');
                return;
            }

            const url = this.isEdit ? 
                `/api/visitors/${this.visitorForm.id}` : 
                '/api/visitors';
            
            const method = this.isEdit ? 'PUT' : 'POST';
            
            // 验证身份证号格式
            const idCardRegex = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/;
            if (!idCardRegex.test(this.visitorForm.idCard)) {
                alert('请输入正确的身份证号码');
                return;
            }

            // 验证手机号格式
            const phoneRegex = /^1[3-9]\d{9}$/;
            if (!phoneRegex.test(this.visitorForm.phone)) {
                alert('请输入正确的手机号码');
                return;
            }

            // 如果是业主亲属或朋友，必须选择关联业主
            if (['业主亲属', '业主朋友'].includes(this.visitorForm.visitType) && !this.visitorForm.ownerId) {
                alert('请选择关联业主');
                return;
            }

            // 构造请求数据
            const requestData = {
                name: this.visitorForm.name.trim(),
                id_card: this.visitorForm.idCard.trim(),
                phone: this.visitorForm.phone.trim(),
                visit_type: this.visitorForm.visitType.trim(),
                purpose: this.visitorForm.purpose.trim(),
                expected_leave_time: this.visitorForm.expectedLeaveTime,
                owner_id: this.visitorForm.ownerId || null,
                relation_with_owner: this.visitorForm.relationWithOwner?.trim() || null,
                remarks: this.visitorForm.remarks?.trim() || null
            };

            const response = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('token')}`
                },
                body: JSON.stringify(requestData)
            });

            const result = await response.json();
            if (result.success) {
                alert(this.isEdit ? '访客信息更新成功' : '访客登记成功');
                this.resetForm();
                await this.fetchTodayVisitors();
            } else {
                throw new Error(result.message);
            }
        } catch (error) {
            console.error('提交访客信息失败:', error);
            alert('操作失败: ' + error.message);
        }
    },

    // 获取今日访客记录
    async fetchTodayVisitors() {
        try {
            const response = await fetch('/api/visitors/today', {
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`
                }
            });
            
            const result = await response.json();
            if (result.success) {
                this.visitorRecords = result.data;
            } else {
                throw new Error(result.message);
            }
        } catch (error) {
            console.error('获取今日访客记录失败:', error);
        }
    },

    // 记录访客离开
    async recordLeave(record) {
        try {
            if (!confirm('确认记录该访客离开吗？')) return;

            const response = await fetch(`/api/visitors/${record.id}/leave`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`
                }
            });

            const result = await response.json();
            if (result.success) {
                alert('已记录访客离开');
                await this.fetchTodayVisitors();
            } else {
                throw new Error(result.message);
            }
        } catch (error) {
            console.error('记录访客离开失败:', error);
            alert('操作失败: ' + error.message);
        }
    },

    // 编辑记录
    editRecord(record) {
        this.isEdit = true;
        this.visitorForm = {
            id: record.id,
            name: record.name,
            idCard: record.idCard,
            phone: record.phone,
            visitType: record.visitType,
            purpose: record.purpose,
            expectedLeaveTime: record.expectedLeaveTime,
            ownerId: record.ownerId,
            relationWithOwner: record.relationWithOwner,
            remarks: record.remarks
        };
    },

    // 重置表单
    resetForm() {
      this.isEdit = false;
      this.visitorForm = {
        name: '',
        idCard: '',
        phone: '',
        visitType: '',
        purpose: '',
        expectedLeaveTime: '',
        ownerId: '',
        relationWithOwner: '',
        remarks: ''
      };
    },

    // 格式化日期时间
    formatDateTime(datetime) {
      return new Date(datetime).toLocaleString();
    },

    // 掩码身份证号
    maskIdCard(idCard) {
      return idCard.replace(/^(.{6})(?:\d+)(.{4})$/, '$1****$2');
    },

    // 掩码手机号
    maskPhone(phone) {
      return phone.replace(/^(\d{3})\d{4}(\d{4})$/, '$1****$2');
    }
  },
  async created() {
    // 获取业主列表
    try {
      const response = await fetch('/api/owners', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      const result = await response.json();
      if (result.success) {
        this.owners = result.data;
      }
    } catch (error) {
      console.error('获取业主列表失败:', error);
    }

    // 获取今日访客记录
    await this.fetchTodayVisitors();
  }
}
</script>

<style scoped>
.visitor-log {
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

/* 搜索框样式 */
.search-section {
  margin-bottom: 2rem;
}

.search-box {
  display: flex;
  gap: 1rem;
  max-width: 500px;
}

.search-box input {
  flex: 1;
  padding: 0.75rem;
  border: 2px solid #e2e8f0;
  border-radius: 0.5rem;
  font-size: 1rem;
}

.search-btn {
  padding: 0 1.5rem;
  background: #4299e1;
  color: white;
  border: none;
  border-radius: 0.5rem;
  cursor: pointer;
}

/* 表单样式 */
.visitor-form {
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
  min-height: 100px;
  resize: vertical;
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

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 1rem;
  font-size: 0.875rem;
  font-weight: 500;
}

.在访 {
  background: #9ae6b4;
  color: #22543d;
}

.已离开 {
  background: #cbd5e0;
  color: #2d3748;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
}

.edit-btn,
.leave-btn {
  padding: 0.5rem;
  border: none;
  border-radius: 0.25rem;
  cursor: pointer;
  color: white;
}

.edit-btn {
  background: #4299e1;
}

.leave-btn {
  background: #ed8936;
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