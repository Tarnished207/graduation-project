<template>
  <div class="planet-login-container">

    <!-- 动态 Canvas 背景 -->
    <canvas id="particle-canvas" class="particle-bg"></canvas>

    <!-- 装饰光晕 -->
    <div class="planet p1"></div>
    <div class="planet p2"></div>
    <div class="planet p3"></div>

    <div class="login-card glass-effect">

      <div class="card-left">
        <div class="logo-box float-anim">
          <img src="../assets/logo.png" alt="logo">
        </div>
        <h2 class="planet-title">Paw Planet</h2>
        <div class="planet-tagline">
          <span>探索</span> · <span>陪伴</span> · <span>治愈</span>
        </div>
        <div class="planet-desc">
          欢迎来到爪爪星球<br>
          这里是毛孩子的快乐宇宙 🪐
        </div>

        <!-- 每日一句 -->
        <div class="daily-quote">
          <i class="el-icon-chat-dot-round"></i>
          <span>{{ currentQuote }}</span>
        </div>
      </div>

      <div class="card-right">
        <div class="form-header">
          <h3>居民登陆</h3>
          <p>Verify Your Identity</p>
        </div>

        <el-form :model="form" @submit.native.prevent class="planet-form">
          <el-form-item>
            <el-input
                v-model="form.username"
                prefix-icon="el-icon-user"
                placeholder="星球居民 ID / 账号"
                class="planet-input">
            </el-input>
          </el-form-item>

          <el-form-item>
            <el-input
                v-model="form.password"
                prefix-icon="el-icon-key"
                type="password"
                placeholder="通行密钥 (密码)"
                show-password
                class="planet-input"
                @keyup.enter.native="login">
            </el-input>
          </el-form-item>

          <el-button type="primary" class="landing-btn" :loading="loading" @click="login">
            🚀 立即登陆星球
          </el-button>

          <div class="form-footer">
            <span class="link-btn" @click="forgotPassword">忘记密钥?</span>
            <span class="link-btn register-link" @click="$router.push('/register')">申请居住证 (注册)</span>
          </div>
        </el-form>
      </div>
    </div>

    <!-- 弹窗保持不变 -->
    <el-dialog title="🚀 重置通行密钥" :visible.sync="forgotVisible" width="420px" custom-class="planet-dialog" append-to-body>
      <div style="padding: 0 10px;">
        <p style="color:#666; font-size:13px; margin-bottom:20px; text-align:center;">
          我们将向您的星球专属邮箱发送验证码
        </p>

        <el-form :model="resetForm" :rules="resetRules" ref="resetForm">

          <el-form-item prop="email">
            <el-input v-model="resetForm.email" prefix-icon="el-icon-message" placeholder="请输入绑定的邮箱" class="planet-input"></el-input>
          </el-form-item>

          <el-form-item prop="code">
            <el-row :gutter="10">
              <el-col :span="15">
                <el-input v-model="resetForm.code" prefix-icon="el-icon-chat-dot-square" placeholder="6位验证码" class="planet-input"></el-input>
              </el-col>
              <el-col :span="9">
                <el-button
                    type="primary"
                    plain
                    style="width:100%; border-radius:10px; height:45px; padding: 0;"
                    :disabled="timer > 0"
                    @click="sendEmailCode">
                  {{ timer > 0 ? `${timer}s后重发` : '获取验证码' }}
                </el-button>
              </el-col>
            </el-row>
          </el-form-item>

          <el-form-item prop="newPassword">
            <el-input v-model="resetForm.newPassword" prefix-icon="el-icon-lock" type="password" placeholder="设置新密钥" show-password class="planet-input"></el-input>
          </el-form-item>

        </el-form>
      </div>

      <span slot="footer">
        <el-button @click="forgotVisible = false" size="medium" style="border-radius:10px;">取 消</el-button>
        <el-button type="primary" :loading="resetLoading" @click="handleResetSubmit" size="medium" style="background: linear-gradient(90deg, #ff758c, #ff7eb3); border:none; border-radius:10px; padding: 10px 30px;">
          确认重置
        </el-button>
      </span>
    </el-dialog>

  </div>
</template>

<script>
export default {
  data() {
    return {
      // 登录相关
      form: { username: '', password: '' },
      loading: false,

      // --- 找回密码相关 ---
      forgotVisible: false,
      resetLoading: false,
      timer: 0, // 倒计时计数器
      resetForm: { email: '', code: '', newPassword: '' },
      resetRules: {
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          // 简单的正则验证邮箱格式
          { pattern: /^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+(.[a-zA-Z0-9_-])+/, message: '邮箱格式不正确', trigger: 'blur' }
        ],
        code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
        newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }]
      },

      // 每日一句
      quotes: [
        "猫咪是掉落凡间的小天使 🐱",
        "狗狗是你生命的一部分，但你是它的一生 🐶",
        "吸猫一口，活到九十九",
        "最好的心理医生，往往有四条腿和毛茸茸的尾巴",
        "每一个毛孩子，都值得被温柔以待",
        "回家有它的迎接，是世界上最治愈的事"
      ],
      currentQuote: ""
    }
  },
  mounted() {
    this.initCanvas();
    this.currentQuote = this.quotes[Math.floor(Math.random() * this.quotes.length)];
  },
  methods: {
    // 初始化粒子背景
    initCanvas() {
      const canvas = document.getElementById('particle-canvas');
      const ctx = canvas.getContext('2d');
      let width, height, particles;

      const init = () => {
        width = window.innerWidth;
        height = window.innerHeight;
        canvas.width = width;
        canvas.height = height;
        particles = [];
        for (let i = 0; i < 50; i++) {
          particles.push({
            x: Math.random() * width,
            y: Math.random() * height,
            r: Math.random() * 3 + 1,
            dx: (Math.random() - 0.5) * 0.5,
            dy: (Math.random() - 0.5) * 0.5,
            color: `rgba(255, 255, 255, ${Math.random() * 0.5 + 0.1})`
          });
        }
      };

      const draw = () => {
        ctx.clearRect(0, 0, width, height);
        particles.forEach((p, i) => {
          p.x += p.dx;
          p.y += p.dy;

          if (p.x < 0 || p.x > width) p.dx *= -1;
          if (p.y < 0 || p.y > height) p.dy *= -1;

          ctx.beginPath();
          ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2);
          ctx.fillStyle = p.color;
          ctx.fill();

          // 连线
          for (let j = i + 1; j < particles.length; j++) {
            const p2 = particles[j];
            const dist = Math.sqrt((p.x - p2.x) ** 2 + (p.y - p2.y) ** 2);
            if (dist < 150) {
              ctx.beginPath();
              ctx.strokeStyle = `rgba(255, 255, 255, ${0.1 - dist / 1500})`;
              ctx.lineWidth = 0.5;
              ctx.moveTo(p.x, p.y);
              ctx.lineTo(p2.x, p2.y);
              ctx.stroke();
            }
          }
        });
        requestAnimationFrame(draw);
      };

      init();
      draw();
      window.addEventListener('resize', init);
    },

    // 登录逻辑
    login() {
      // 1. 校验输入
      if(!this.form.username || !this.form.password) {
        this.$message.warning("请填写完整的居民ID和密钥");
        return;
      }

      this.loading = true;

      // 2. 发送请求
      this.$http.post('/sysUser/login', this.form).then(res => {
        this.loading = false;
        const r = res.data;
        if (r && r.code === '200' && r.data) {
          this.$message.success("欢迎回到爪爪星球！");

          // --- 核心修改：同时存储 User 信息和 Token ---
          const role = r.data.role;

          // 拦截管理员
          if (role === 'ADMIN') {
            this.$message.closeAll(); // 关闭之前可能存在的提示
            this.$message.warning("管理员请从管理端登录");
            return; // 终止后续逻辑，不存储 Session 也不跳转
          }

          sessionStorage.setItem("user", JSON.stringify(r.data));
          if (r.token) {
            sessionStorage.setItem("token", r.token);
          }

          if (role === 'SERVICE') {
            this.$router.push('/service/workbench');
          } else if (role === 'DOCTOR') {
            this.$router.push('/doctor/workbench');
          } else {
            this.$router.push('/front/home');
          }
        } else if (r && r.code === '403') {
          this.$message.error("账号已禁用");
        } else {
          this.$message.error((r && r.msg) ? r.msg : "ID或密钥错误");
        }
      }).catch(() => {
        this.loading = false;
        this.$message.error("星球信号连接中断");
      });
    },

    // 打开忘记密码弹窗
    forgotPassword() {
      this.forgotVisible = true;
      this.resetForm = { email: '', code: '', newPassword: '' }; // 清空表单
      this.timer = 0; // 重置倒计时
    },

    // 1. 发送验证码 (对接后端)
    sendEmailCode() {
      // 只校验邮箱这一项
      if (!this.resetForm.email) {
        this.$message.warning("请先输入您的邮箱地址 📧");
        return;
      }
      // 简单的格式检查
      if (!/^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+(.[a-zA-Z0-9_-])+/.test(this.resetForm.email)) {
        this.$message.warning("邮箱格式好像不对哦");
        return;
      }

      // 开始发送请求
      this.$http.post('/sysUser/sendEmail', { email: this.resetForm.email }).then(res => {
        if (res.data === true) {
          this.$message.success("验证码已发送，请去邮箱查收 (有效期5分钟)");

          // 启动倒计时
          this.timer = 60;
          let interval = setInterval(() => {
            this.timer--;
            if (this.timer <= 0) {
              clearInterval(interval);
            }
          }, 1000);

        } else {
          this.$message.error("发送失败，请检查邮箱是否输入正确");
        }
      }).catch(err => {
        console.error(err);
        this.$message.error("发送失败，可能是网络问题");
      });
    },

    // 2. 提交重置密码 (对接后端)
    handleResetSubmit() {
      this.$refs.resetForm.validate(valid => {
        if (valid) {
          this.resetLoading = true;
          this.$http.post('/sysUser/resetPass', this.resetForm).then(res => {
            this.resetLoading = false;
            if (res.data === true) {
              this.forgotVisible = false;
              this.$message.success("密钥重置成功！请使用新密钥登陆 ✨");
            } else {
              this.$message.error("验证码错误或已过期");
            }
          }).catch(() => {
            this.resetLoading = false;
            this.$message.error("系统繁忙，请稍后再试");
          });
        }
      });
    }
  }
}
</script>

<style scoped>
/* 保持以前的漂亮样式 */
.planet-login-container {
  height: 100vh;
  width: 100%;
  background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
}

/* Canvas 背景 */
.particle-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.planet {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(5px);
  animation: floatPlanet 20s infinite linear;
  z-index: 2;
}
.p1 { width: 300px; height: 300px; top: -50px; left: -50px; background: radial-gradient(circle, rgba(255,255,255,0.4) 0%, rgba(255,255,255,0) 70%); }
.p2 { width: 150px; height: 150px; bottom: 50px; right: 100px; background: radial-gradient(circle, rgba(255,255,255,0.3) 0%, rgba(255,255,255,0) 70%); animation-duration: 15s; }
.p3 { width: 80px; height: 80px; top: 20%; right: 20%; background: #fff; opacity: 0.2; animation-duration: 25s; }
@keyframes floatPlanet { 0% { transform: translate(0, 0) rotate(0deg); } 50% { transform: translate(20px, 40px) rotate(180deg); } 100% { transform: translate(0, 0) rotate(360deg); } }

/* 毛玻璃卡片 */
.login-card {
  width: 900px;
  height: 520px;
  background: rgba(255, 255, 255, 0.7); /* 半透明背景 */
  border-radius: 24px;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.15);
  display: flex;
  overflow: hidden;
  z-index: 10;
  backdrop-filter: blur(10px); /* 毛玻璃模糊 */
  border: 1px solid rgba(255, 255, 255, 0.5); /* 边框高光 */
}

.card-left {
  flex: 1;
  background: linear-gradient(160deg, rgba(255, 154, 158, 0.9) 0%, rgba(250, 208, 196, 0.9) 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  padding: 40px;
  text-align: center;
  position: relative;
}
.logo-box img { width: 140px; filter: drop-shadow(0 10px 20px rgba(214, 69, 65, 0.3)); }
.float-anim { animation: floatLogo 4s ease-in-out infinite; } @keyframes floatLogo { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-15px); } }
.planet-title { font-family: 'Comic Sans MS', '幼圆', sans-serif; font-size: 36px; font-weight: 800; margin: 20px 0 10px; letter-spacing: 2px; text-shadow: 2px 2px 4px rgba(0,0,0,0.1); }
.planet-tagline { font-size: 14px; margin-bottom: 30px; opacity: 0.9; letter-spacing: 4px; }
.planet-desc { font-size: 15px; line-height: 1.8; opacity: 0.85; font-weight: 500; }

.daily-quote {
  margin-top: 40px;
  background: rgba(255, 255, 255, 0.2);
  padding: 10px 20px;
  border-radius: 20px;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-right {
  flex: 1.1;
  background: rgba(255, 255, 255, 0.8);
  padding: 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.form-header h3 { font-size: 28px; color: #333; margin-bottom: 5px; }
.form-header p { font-size: 14px; color: #bbb; margin-bottom: 40px; letter-spacing: 1px; }

.planet-input >>> .el-input__inner {
  height: 50px;
  border-radius: 12px;
  background: rgba(244, 246, 248, 0.8);
  border: 1px solid #edf2f7;
  padding-left: 45px;
  font-size: 15px;
  transition: all 0.3s;
}
.planet-input >>> .el-input__inner:focus { background: #fff; border-color: #ff9a9e; box-shadow: 0 0 0 3px rgba(255, 154, 158, 0.1); }
.planet-input >>> .el-input__prefix { left: 15px; font-size: 18px; line-height: 50px; color: #a0aec0; }
.el-form-item { margin-bottom: 25px; }

.landing-btn { width: 100%; height: 50px; border-radius: 12px; background: linear-gradient(90deg, #ff758c 0%, #ff7eb3 100%); border: none; font-size: 16px; font-weight: bold; letter-spacing: 4px; margin-top: 10px; box-shadow: 0 10px 20px rgba(255, 117, 140, 0.3); transition: transform 0.2s; }
.landing-btn:hover { transform: translateY(-3px); box-shadow: 0 15px 25px rgba(255, 117, 140, 0.4); }

.form-footer { margin-top: 20px; display: flex; justify-content: space-between; font-size: 13px; color: #999; }
.link-btn { cursor: pointer; transition: color 0.3s; }
.link-btn:hover { color: #ff758c; }
.register-link { color: #ff758c; font-weight: 600; }

/* 弹窗样式 */
.planet-dialog >>> .el-dialog { border-radius: 16px; overflow: hidden; }
.planet-dialog >>> .el-dialog__header { background: #f9f9f9; padding: 20px; font-weight: bold; }
.planet-dialog >>> .el-dialog__body { padding: 30px 20px 10px; }
</style>
