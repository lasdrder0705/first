<template>
  <div class="login-container">
    <div class="login-overlay">
      <form @submit.prevent="handleSubmit" class="login-form">
        <h2>{{ isLogin ? '用户登录' : '用户注册' }}</h2>
        
        <div class="form-group">
          <label for="username">用户名</label>
          <input 
            type="text" 
            id="username"
            v-model="username"
            :placeholder="isLogin ? '请输入用户名' : '请设置用户名'"
            required
          />
        </div>

        <div class="form-group">
          <label for="password">密码</label>
          <input 
            type="password" 
            id="password"
            v-model="password"
            :placeholder="isLogin ? '请输入密码' : '请设置密码'"
            required
          />
        </div>

        <div v-if="!isLogin" class="form-group">
          <label for="confirmPassword">确认密码</label>
          <input 
            type="password" 
            id="confirmPassword"
            v-model="confirmPassword"
            placeholder="请再次输入密码"
            required
          />
        </div>

        <button type="submit" :disabled="isLoading">
          {{ buttonText }}
        </button>
        
        <p v-if="error" class="error">
          <span class="error-icon">!</span>
          {{ error }}
        </p>

        <div class="switch-form">
          <a href="#" @click.prevent="toggleForm">
            {{ isLogin ? '没有账号？立即注册' : '已有账号？立即登录' }}
          </a>
        </div>
      </form>
    </div>
    <div class="login-tips" v-if="!isLogin">
      <p>员工请使用身份证号作为账号，初始密码为身份证后8位</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Login',
  data() {
    return {
      isLogin: true,
      username: '',
      password: '',
      confirmPassword: '',
      error: '',
      isLoading: false
    }
  },
  computed: {
    buttonText() {
      if (this.isLoading) {
        return this.isLogin ? '登录中...' : '注册中...';
      }
      return this.isLogin ? '登录' : '注册';
    }
  },
  methods: {
    async handleSubmit() {
      this.error = '';
      
      if (!this.isLogin && this.password !== this.confirmPassword) {
        this.error = '两次输入的密码不一致';
        return;
      }

      this.isLoading = true;
      
      try {
        if (this.isLogin) {
          await this.handleLogin();
        } else {
          await this.handleRegister();
        }
      } finally {
        this.isLoading = false;
      }
    },
    
    async handleLogin() {
      try {
        const response = await fetch('/api/auth/login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            username: this.username.trim(),
            password: this.password
          })
        });

        const result = await response.json();
        
        if (!result.success) {
          throw new Error(result.message || '登录失败');
        }

        localStorage.setItem('token', result.data.token);
        this.$router.push('/dashboard');
      } catch (err) {
        console.error('登录错误:', err);
        alert(err.message || '登录失败，请检查用户名和密码');
      }
    },

    async handleRegister() {
      try {
        const response = await fetch('/api/register', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            username: this.username,
            password: this.password
          })
        });

        if (!response.ok) {
          const data = await response.json();
          throw new Error(data.error || '注册失败');
        }

        // 注册成功后自动登录
        await this.handleLogin();
        
      } catch (err) {
        console.error('注册错误:', err);
        this.error = err.message;
      }
    },

    toggleForm() {
      this.isLogin = !this.isLogin;
      this.error = '';
      this.username = '';
      this.password = '';
      this.confirmPassword = '';
    }
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-image: url('/background.jpg');
  background-size: cover;
  background-position: center;
}

.login-overlay {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.login-form {
  background: rgba(255, 255, 255, 0.95);
  padding: 2.5rem;
  border-radius: 1rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1), 0 1px 3px rgba(0, 0, 0, 0.08);
  width: 100%;
  max-width: 400px;
  transition: transform 0.2s ease;
  animation: slideIn 0.3s ease-out;
}

.login-form:hover {
  transform: translateY(-2px);
}

h2 {
  color: #1a365d;
  text-align: center;
  margin-bottom: 2rem;
  font-size: 1.8rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  color: #2d3748;
  font-weight: 500;
}

input {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e2e8f0;
  border-radius: 0.5rem;
  font-size: 1rem;
  transition: all 0.2s;
}

input:focus {
  outline: none;
  border-color: #3182ce;
  box-shadow: 0 0 0 3px rgba(49, 130, 206, 0.2);
}

button {
  width: 100%;
  padding: 0.875rem;
  background-color: #3182ce;
  color: white;
  border: none;
  border-radius: 0.5rem;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

button:hover:not(:disabled) {
  background-color: #2c5282;
  transform: translateY(-1px);
}

button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.error {
  margin-top: 1rem;
  padding: 0.75rem;
  background-color: #fff5f5;
  border: 1px solid #feb2b2;
  border-radius: 0.5rem;
  color: #c53030;
  font-size: 0.875rem;
  display: flex;
  align-items: center;
}

.error-icon {
  background-color: #c53030;
  color: white;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-right: 0.5rem;
  font-weight: bold;
}

@media (max-width: 640px) {
  .login-form {
    margin: 1rem;
    padding: 1.5rem;
  }

  h2 {
    font-size: 1.5rem;
  }

  input, button {
    font-size: 0.875rem;
  }
}

.switch-form {
  margin-top: 1rem;
  text-align: center;
}

.switch-form a {
  color: #3182ce;
  text-decoration: none;
  font-size: 0.875rem;
  transition: color 0.2s;
}

.switch-form a:hover {
  color: #2c5282;
  text-decoration: underline;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.password-strength {
  font-size: 0.75rem;
  margin-top: 0.25rem;
}

.password-strength.weak {
  color: #e53e3e;
}

.password-strength.medium {
  color: #d69e2e;
}

.password-strength.strong {
  color: #38a169;
}

.login-tips {
  text-align: center;
  margin-top: 1rem;
  color: #666;
  font-size: 0.9rem;
}
</style> 