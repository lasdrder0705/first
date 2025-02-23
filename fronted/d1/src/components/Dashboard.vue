<template>
  <div class="dashboard">
    <nav-bar />
    <main class="main-content">
      <div class="container">
        <div class="profile-card">
          <div class="profile-header">
            <div class="avatar-section">
              <div class="avatar-container">
                <img :src="avatarUrl" 
                     alt="用户头像" 
                     class="avatar"
                     @click="triggerAvatarUpload"
                />
                <div class="avatar-overlay" @click="triggerAvatarUpload">
                  <span>
                    <i class="upload-icon">📷</i>
                    <br>
                    点击更换头像
                  </span>
                </div>
                <input 
                  type="file" 
                  ref="fileInput" 
                  @change="handleAvatarChange" 
                  accept="image/*" 
                  style="display: none"
                />
              </div>
              <p class="upload-hint">支持 jpg、png、gif 格式，最大 5MB</p>
            </div>
            
            <div class="profile-info">
              <h2>{{ userInfo.nickname || userInfo.username }}</h2>
              <p class="bio">{{ userInfo.bio !== null && userInfo.bio !== undefined && userInfo.bio !== '' ? userInfo.bio : '这个人很懒，什么都没写~' }}</p>
              <p class="join-date">注册时间：{{ formatDate(userInfo.created_at) }}</p>
            </div>
          </div>

          <div class="profile-content">
            <div class="profile-form" v-if="isEditing">
              <div class="form-group">
                <label>昵称</label>
                <input type="text" v-model="editForm.nickname" placeholder="设置昵称">
              </div>

              <div class="form-group">
                <label>邮箱</label>
                <input type="email" v-model="editForm.email" placeholder="设置邮箱">
              </div>

              <div class="form-group">
                <label>个人简介</label>
                <textarea v-model="editForm.bio" placeholder="写点什么介绍自己..."></textarea>
              </div>

              <div class="form-actions">
                <button @click="saveProfile" :disabled="isSaving" class="save-btn">
                  {{ isSaving ? '保存中...' : '保存' }}
                </button>
                <button @click="cancelEdit" class="cancel-btn">取消</button>
              </div>
            </div>

            <div class="profile-details" v-else>
              <div class="info-section">
                <h3>基本信息</h3>
                <div class="info-grid">
                  <div class="info-item">
                    <span class="label">用户名</span>
                    <span class="value">{{ userInfo.username }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">邮箱</span>
                    <span class="value">{{ userInfo.email || '未设置' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">昵称</span>
                    <span class="value">{{ userInfo.nickname || '未设置' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">注册时间</span>
                    <span class="value">{{ formatDate(userInfo.created_at) }}</span>
                  </div>
                </div>
                <button @click="startEdit" class="edit-btn">编辑资料</button>
              </div>
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
  name: 'Dashboard',
  components: {
    NavBar
  },
  data() {
    return {
      userInfo: {
        username: '',
        email: '',
        nickname: '',
        avatar: '',
        bio: '',
        created_at: ''
      },
      isEditing: false,
      isSaving: false,
      editForm: {
        nickname: '',
        email: '',
        bio: ''
      },
      error: ''
    }
  },
  computed: {
    avatarUrl() {
      if (!this.userInfo.avatar) {
        return '/default-avatar.png';
      }
      // 确保头像URL是完整的
      if (this.userInfo.avatar.startsWith('http') || this.userInfo.avatar.startsWith('/api/')) {
        return this.userInfo.avatar;
      }
      // 否则添加API前缀
      return `/api${this.userInfo.avatar}`;
    },
    isEmployee() {
      return this.userInfo?.role === 'employee';
    }
  },
  methods: {
    async fetchUserInfo() {
      try {
        const token = localStorage.getItem('token');
        if (!token) {
          throw new Error('未登录');
        }

        const response = await fetch('/api/user/profile', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (!response.ok) {
          const data = await response.json();
          throw new Error(data.message || '获取用户信息失败');
        }

        const result = await response.json();
        if (!result.success) {
          throw new Error(result.message);
        }

        // 修改这里，不要给 bio 设置默认值
        this.userInfo = {
          ...result.data,
          avatar: result.data.avatar || '/default-avatar.png',
          bio: result.data.bio,  // 不设置默认值
          created_at: result.data.createdAt
        };

        console.log('User info loaded:', this.userInfo); // 用于调试
      } catch (err) {
        console.error('获取用户信息错误:', err);
        this.error = err.message;
        
        if (err.message.includes('未登录')) {
          localStorage.removeItem('token');
          this.$router.push('/login');
        }
      }
    },

    initEditForm() {
      this.editForm = {
        nickname: this.userInfo.nickname ?? '',
        email: this.userInfo.email ?? '',
        bio: this.userInfo.bio ?? ''
      };
      console.log('Initialized edit form:', this.editForm); // 添加调试日志
    },

    startEdit() {
      this.isEditing = true;
      this.initEditForm();
    },

    cancelEdit() {
      this.isEditing = false;
      this.initEditForm();
    },

    async saveProfile() {
      try {
        this.isSaving = true;
        this.error = '';

        const requestData = {
          nickname: this.editForm.nickname?.trim(),
          email: this.editForm.email?.trim(),
          bio: this.editForm.bio?.trim()
        };

        console.log('Saving profile with data:', requestData);

        const response = await fetch('/api/user/profile', {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          body: JSON.stringify(requestData)
        });

        const result = await response.json();
        if (!response.ok) {
          throw new Error(result.message || '保存失败');
        }

        console.log('Save profile response:', result);

        // 直接使用返回的数据更新 userInfo
        this.userInfo = {
          ...this.userInfo,
          ...result.data,
          bio: result.data.bio,  // 确保明确设置 bio
          created_at: result.data.createdAt
        };

        this.isEditing = false;
        this.isSaving = false;
      } catch (err) {
        console.error('保存个人信息错误:', err);
        this.error = err.message;
        this.isSaving = false;
      }
    },

    triggerAvatarUpload() {
      console.log('触发头像上传');
      // 确保input元素存在
      if (this.$refs.fileInput) {
        this.$refs.fileInput.click();
      } else {
        console.error('找不到文件上传输入框');
      }
    },

    async handleAvatarChange(event) {
      try {
        const file = event.target.files[0];
        if (!file) return;

        // 检查文件类型
        if (!file.type.startsWith('image/')) {
          alert('请选择图片文件');
          return;
        }

        // 检查文件大小（限制为5MB）
        if (file.size > 5 * 1024 * 1024) {
          alert('图片大小不能超过5MB');
          return;
        }

        const formData = new FormData();
        formData.append('file', file);

        const response = await fetch('/api/user/profile/avatar', {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          body: formData
        });

        const result = await response.json();
        if (!result.success) {
          throw new Error(result.message);
        }

        // 更新头像URL并立即重新获取用户信息
        this.userInfo.avatar = result.data;
        await this.fetchUserInfo();
        
        // 清空文件输入框
        this.$refs.fileInput.value = '';
        
        alert('头像上传成功');
      } catch (err) {
        console.error('上传头像失败:', err);
        alert(err.message || '上传头像失败');
      }
    },

    handleLogout() {
      localStorage.removeItem('token');
      this.$router.push('/login');
    },

    formatDate(dateString) {
      if (!dateString) return '';
      try {
        const date = new Date(dateString);
        if (isNaN(date.getTime())) {
          console.error('Invalid date:', dateString);
          return '';
        }
        
        return date.toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        });
      } catch (e) {
        console.error('日期格式化错误:', e);
        return '';
      }
    }
  },
  created() {
    this.fetchUserInfo();
  }
}
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  background-color: #f0f2f5;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  padding: 2rem 0;
}

.container {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 2rem;
}

.profile-card {
  background: white;
  border-radius: 1rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  padding: 3rem;
  width: 100%;
  margin: 0 auto;
  transition: all 0.3s ease;
}

.profile-header {
  display: flex;
  gap: 3rem;
  margin-bottom: 3rem;
  padding-bottom: 2rem;
  border-bottom: 1px solid #eee;
}

.avatar-section {
  text-align: center;
}

.avatar-container {
  position: relative;
  width: 180px;
  height: 180px;
  margin-bottom: 1rem;
  cursor: pointer;
}

.avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
  cursor: pointer;
}

.avatar-container:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay span {
  text-align: center;
  font-size: 0.9rem;
  padding: 0.5rem;
}

.upload-icon {
  font-size: 1.5rem;
  margin-bottom: 0.5rem;
}

/* 添加点击反馈效果 */
.avatar-container:active {
  transform: scale(0.98);
}

.upload-hint {
  color: #666;
  font-size: 0.9rem;
  margin-top: 0.5rem;
}

.profile-info {
  flex: 1;
}

.profile-info h2 {
  font-size: 2rem;
  color: #1a1a1a;
  margin: 0 0 1rem 0;
}

.bio {
  font-size: 1.1rem;
  color: #666;
  margin: 1rem 0;
  line-height: 1.6;
  word-break: break-word;
}

.join-date {
  color: #999;
  font-size: 0.9rem;
  margin-top: 0.5rem;
}

.info-section {
  background: #f8f9fa;
  border-radius: 0.8rem;
  padding: 2rem;
  margin-top: 2rem;
}

.info-section h3 {
  margin: 0 0 1.5rem 0;
  color: #1a1a1a;
  font-size: 1.3rem;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 2rem;
  margin-bottom: 2rem;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.info-item .label {
  color: #666;
  font-size: 0.9rem;
}

.info-item .value {
  color: #1a1a1a;
  font-size: 1.1rem;
  font-weight: 500;
}

/* 按钮样式 */
button {
  padding: 0.8rem 2rem;
  border-radius: 0.5rem;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.edit-btn {
  background-color: #1890ff;
  color: white;
  border: none;
  padding: 1rem 3rem;
}

.save-btn {
  background-color: #52c41a;
  color: white;
  border: none;
}

.cancel-btn {
  background-color: #f0f0f0;
  color: #666;
  border: none;
}

.logout-btn {
  background-color: #ff4d4f;
  color: white;
  border: none;
}

button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

/* 表单样式 */
.form-group {
  margin-bottom: 2rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.8rem;
  color: #1a1a1a;
  font-weight: 500;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 1rem;
  border: 1px solid #d9d9d9;
  border-radius: 0.5rem;
  font-size: 1rem;
  transition: all 0.3s;
}

.form-group textarea {
  min-height: 120px;
  resize: vertical;
}

.form-group input:focus,
.form-group textarea:focus {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
  outline: none;
}

.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 2rem;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .container {
    max-width: 1000px;
  }
}

@media (max-width: 992px) {
  .profile-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .container {
    padding: 0 1rem;
  }
  
  .profile-card {
    padding: 1.5rem;
  }
}
</style> 