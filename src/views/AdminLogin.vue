<template>
  <div class="admin-login-wrapper">

    <div class="login-container">

      <div class="brand-section">
        <div class="brand-content">
          <img src="../assets/logo.png" alt="logo" class="brand-logo" />
          <h2 class="brand-title">Paw Planet</h2>
          <div class="brand-subtitle">管理控制台</div>
          <div class="brand-desc">
            专业 · 极简 · 智能<br>
            一站式宠物服务管理平台
          </div>
        </div>
        <div class="circle c1"></div>
        <div class="circle c2"></div>
      </div>

      <div class="form-section">
        <div class="form-header">
          <h3>欢迎回来</h3>
          <p>请使用管理员账号登录系统</p>
        </div>

        <el-form :model="form" class="login-form" @submit.native.prevent>
          <el-form-item>
            <el-input
                v-model="form.username"
                prefix-icon="el-icon-user"
                placeholder="输入管理员账号"
                class="custom-input">
            </el-input>
          </el-form-item>

          <el-form-item>
            <el-input
                v-model="form.password"
                prefix-icon="el-icon-lock"
                type="password"
                placeholder="输入密码"
                show-password
                class="custom-input"
                @keyup.enter.native="login">
            </el-input>
          </el-form-item>

          <el-button type="primary" class="submit-btn" :loading="loading" @click="login">
            立即登录
          </el-button>
        </el-form>

      </div>

    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      form: { username: '', password: '' },
      loading: false
    }
  },
  methods: {
    login() {
      if(!this.form.username || !this.form.password) {
        this.$message.warning("请输入完整的账号和密码");
        return;
      }

      this.loading = true;
      this.$http.post('/sysUser/login', this.form).then(res => {
        this.loading = false;
        const r = res.data;
        if (r && r.code === '200' && r.data) {
          if (r.data.role !== 'ADMIN') {
            this.$message.error("您不是管理员，请前往用户入口登录");
            return;
          }
          this.$message.success("登录成功，欢迎指挥官！");
          sessionStorage.setItem("user", JSON.stringify(r.data));
          this.$router.push('/manage');
        } else if (r && r.code === '403') {
          this.$message.error("账号已禁用");
        } else {
          this.$message.error((r && r.msg) ? r.msg : "账号或密码错误");
        }
      }).catch(() => {
        this.loading = false;
        this.$message.error("服务器连接失败");
      });
    }
  }
}
</script>

<style scoped>
/* --- 整体背景优化 --- */
.admin-login-wrapper {
  height: 100vh;
  width: 100%;
  /* 改为干净的浅灰色背景，突出中间的卡片 */
  background-color: #f0f2f5;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

/* --- 登录卡片 --- */
.login-container {
  width: 800px;
  height: 480px;
  background: #fff;
  border-radius: 16px; /* 圆角稍微小一点，更干练 */
  box-shadow: 0 10px 30px rgba(0,0,0,0.08); /* 阴影更柔和 */
  display: flex;
  overflow: hidden;
  border: 1px solid #ebeef5;
}

/* --- 左侧品牌区 (核心改动) --- */
.brand-section {
  flex: 0.8; /* 左侧稍微窄一点 */
  /* 【重点】背景色使用后台侧边栏同款浅蓝灰 */
  background-color: #E3F2F3;
  /* 【重点】文字颜色使用后台同款深蓝灰 */
  color: #1E3648;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.brand-content { z-index: 2; }

/* 【修复 Logo 变形】 */
.brand-logo {
  height: 80px; /* 只固定高度 */
  width: auto;  /* 宽度自动适应 */
  margin-bottom: 20px;
  /* 加一点投影让它浮起来 */
  filter: drop-shadow(0 4px 6px rgba(0,0,0,0.1));
}

.brand-title { font-size: 26px; font-weight: 700; margin: 0; letter-spacing: 1px; color: #1E3648; }
.brand-subtitle { font-size: 14px; opacity: 0.8; margin-top: 8px; text-transform: uppercase; letter-spacing: 2px; font-weight: 500;}
.brand-desc { margin-top: 30px; font-size: 13px; opacity: 0.7; line-height: 1.8; }

/* 装饰圆圈 (改为深色微透明，适应浅背景) */
.circle { position: absolute; border-radius: 50%; background: rgba(30, 54, 72, 0.06); }
.c1 { width: 200px; height: 200px; top: -50px; left: -50px; }
.c2 { width: 300px; height: 300px; bottom: -80px; right: -80px; }

/* --- 右侧表单区 --- */
.form-section {
  flex: 1.2;
  padding: 40px 60px; /* 增加内边距 */
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: #fff;
}

.form-header h3 { font-size: 22px; color: #1E3648; margin-bottom: 10px; font-weight: 600; }
.form-header p { font-size: 14px; color: #909399; margin-bottom: 35px; }

/* 输入框美化 */
.custom-input >>> .el-input__inner {
  height: 48px; /* 更高一点 */
  border-radius: 8px;
  background: #f8fafa; /* 非常淡的灰背景 */
  border: 1px solid #e4e7ed;
  padding-left: 42px;
  font-size: 15px;
  color: #1E3648;
  transition: all 0.3s;
}
.custom-input >>> .el-input__inner:focus {
  background: #fff;
  /* 聚焦时使用森系绿边框 */
  border-color: #42b883;
  box-shadow: 0 0 0 2px rgba(66, 184, 131, 0.1);
}
.custom-input >>> .el-input__prefix {
  left: 12px; font-size: 18px; line-height: 48px; color: #c0c4cc;
}
.el-form-item { margin-bottom: 25px; }

/* --- 登录按钮 (核心改动) --- */
.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 8px;
  /* 【重点】使用后台选中菜单的森系绿渐变 */
  background: linear-gradient(135deg, #42b883 0%, #35495e 100%);
  border: none;
  font-weight: 600;
  letter-spacing: 2px;
  margin-top: 15px;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(66, 184, 131, 0.3);
}
.submit-btn:hover {
  opacity: 0.95;
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(66, 184, 131, 0.4);
}
</style>
