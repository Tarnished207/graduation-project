<template>
  <div class="planet-register-container">
    <div class="planet p1"></div>
    <div class="planet p2"></div>

    <div class="register-card">
      <div class="card-header">
        <h2>申请居住证</h2>
        <p>Join Paw Planet</p>
      </div>

      <el-form :model="form" :rules="rules" ref="registerForm" label-width="80px" class="planet-form">

        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" placeholder="设置您的居民ID (登录账号)"></el-input>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input type="password" v-model="form.password" show-password placeholder="设置通行密钥"></el-input>
        </el-form-item>

        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="想怎么称呼您？"></el-input>
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="用于找回密码 (必填)"></el-input>
        </el-form-item>

        <el-button type="primary" class="submit-btn" :loading="loading" @click="register">
          立即加入星球 🚀
        </el-button>

        <div class="footer-link">
          <span @click="$router.push('/login')">已有账号？去登陆 &gt;</span>
        </div>

      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      loading: false,
      form: {
        username: '',
        password: '',
        nickname: '',
        email: '',
        // phone: '',   <-- 已删除
        // address: '', <-- 已删除
        role: 'USER'
      },
      rules: {
        username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { pattern: /^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+(.[a-zA-Z0-9_-])+/, message: '邮箱格式不对', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    register() {
      this.$refs.registerForm.validate(valid => {
        if (valid) {
          this.loading = true;
          this.$http.post('/sysUser/register', this.form).then(res => {
            this.loading = false;
            // 兼容后端返回格式
            if (res.data && (res.data === true || res.data.includes("成功"))) {
              this.$message.success("注册成功！欢迎加入 🪐");
              this.$router.push('/login');
            } else {
              this.$message.error(res.data || "注册失败");
            }
          }).catch(err => {
            this.loading = false;
            this.$message.error("连接星球失败，请检查网络");
          });
        }
      });
    }
  }
}
</script>

<style scoped>
/* 样式保持不变 */
.planet-register-container {
  height: 100vh;
  width: 100%;
  background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
}
.planet { position: absolute; border-radius: 50%; background: rgba(255, 255, 255, 0.2); }
.p1 { width: 200px; height: 200px; top: -50px; right: -50px; }
.p2 { width: 100px; height: 100px; bottom: 50px; left: 50px; }

.register-card {
  width: 500px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.1);
  backdrop-filter: blur(10px);
}
.card-header { text-align: center; margin-bottom: 30px; }
.card-header h2 { color: #333; margin-bottom: 5px; }
.card-header p { color: #999; font-size: 14px; letter-spacing: 2px; }

.submit-btn {
  width: 100%;
  border-radius: 10px;
  background: linear-gradient(90deg, #ff758c, #ff7eb3);
  border: none;
  font-weight: bold;
  letter-spacing: 2px;
  margin-top: 20px;
}
.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(255, 117, 140, 0.3);
}

.footer-link {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #ff758c;
  cursor: pointer;
}
</style>