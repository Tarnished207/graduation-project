<template>
  <div class="user-center-layout">

    <div class="main-container">

      <div class="bg-decoration"></div>

      <div class="grid-layout">

        <div class="profile-card fade-in-up">
          <div class="card-header-bg">
            <svg class="waves" viewBox="0 24 150 28" preserveAspectRatio="none" shape-rendering="auto">
              <defs>
                <path id="gentle-wave" d="M-160 44c30 0 58-18 88-18s 58 18 88 18 58-18 88-18 58 18 88 18 v44h-352z"/>
              </defs>
              <g class="parallax">
                <use xlink:href="#gentle-wave" x="48" y="0" fill="rgba(255,255,255,0.7"/>
                <use xlink:href="#gentle-wave" x="48" y="3" fill="rgba(255,255,255,0.5)"/>
                <use xlink:href="#gentle-wave" x="48" y="5" fill="rgba(255,255,255,0.3)"/>
                <use xlink:href="#gentle-wave" x="48" y="7" fill="#fff"/>
              </g>
            </svg>
          </div>

          <div class="avatar-section">
            <el-upload
                class="avatar-uploader"
                action="http://localhost:9090/file/upload"
                :show-file-list="false"
                :on-success="handleAvatarSuccess">
              <div class="avatar-wrapper">
                <el-avatar :size="90" :src="getImageUrl(user.avatar)" class="real-avatar"></el-avatar>
                <div class="upload-mask">
                  <i class="el-icon-camera-solid"></i>
                  <span>更换</span>
                </div>
              </div>
            </el-upload>
          </div>

          <div class="info-section">
            <div class="user-name">{{ user.nickname || user.username }}</div>
            <div class="user-id">ID: {{ user.id }}</div>
            <div class="user-tags">
              <span class="tag-badge"><i class="el-icon-user"></i> 星球居民</span>
            </div>
          </div>

          <div class="action-section">
            <el-button round size="small" @click="$router.push('/front/pet')">我的宠物</el-button>
            <el-button round size="small" type="primary" plain @click="$router.push('/front/orders')">查看订单
            </el-button>
          </div>
        </div>

        <div class="settings-card fade-in-up delay-100">
          <el-tabs v-model="activeTab" class="custom-tabs">

            <el-tab-pane name="info">
              <span slot="label"><i class="el-icon-postcard"></i> 基础资料</span>

              <div class="panel-content">
                <div class="panel-header">
                  <h3>编辑资料</h3>
                  <p>完善您的个人信息，让我们更了解您</p>
                </div>

                <div class="form-grid">
                  <el-form :model="form" label-position="top" size="medium">
                    <div class="input-row">
                      <el-form-item label="用户名">
                        <el-input v-model="form.username" disabled prefix-icon="el-icon-user-solid"
                                  class="custom-input"></el-input>
                      </el-form-item>
                      <el-form-item label="昵称">
                        <el-input v-model="form.nickname" placeholder="请输入昵称" prefix-icon="el-icon-edit"
                                  class="custom-input"></el-input>
                      </el-form-item>
                    </div>

                    <div class="input-row">
                      <el-form-item label="手机号">
                        <el-input v-model="form.phone" placeholder="绑定手机号" prefix-icon="el-icon-mobile-phone"
                                  class="custom-input"></el-input>
                      </el-form-item>
                      <el-form-item label="邮箱">
                        <el-input v-model="form.email" placeholder="绑定邮箱" prefix-icon="el-icon-message"
                                  class="custom-input"></el-input>
                      </el-form-item>
                    </div>

                    <el-form-item label="收货地址">
                      <el-input type="textarea" :rows="3" v-model="form.address" placeholder="设置默认收货地址"
                                class="custom-input"></el-input>
                    </el-form-item>

                    <el-form-item>
                      <el-button type="primary" class="save-btn" @click="save" :loading="loading">保存更改</el-button>
                    </el-form-item>
                  </el-form>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane name="security">
              <span slot="label"><i class="el-icon-lock"></i> 安全设置</span>

              <div class="panel-content">
                <div class="panel-header">
                  <h3>修改密码</h3>
                  <p>定期修改密码可以保护您的账号安全</p>
                </div>

                <div class="form-grid narrow">
                  <el-form :model="pwdForm" :rules="pwdRules" ref="pwdForm" label-position="top" size="medium">
                    <el-form-item label="当前密码" prop="password">
                      <el-input type="password" v-model="pwdForm.password" show-password prefix-icon="el-icon-key"
                                class="custom-input"></el-input>
                    </el-form-item>
                    <el-form-item label="新密码" prop="newPassword">
                      <el-input type="password" v-model="pwdForm.newPassword" show-password prefix-icon="el-icon-lock"
                                class="custom-input"></el-input>
                    </el-form-item>
                    <el-form-item label="确认新密码" prop="confirmPassword">
                      <el-input type="password" v-model="pwdForm.confirmPassword" show-password
                                prefix-icon="el-icon-check" class="custom-input"></el-input>
                    </el-form-item>
                    <el-form-item>
                      <el-button type="danger" class="save-btn" @click="updatePassword">确认修改</el-button>
                    </el-form-item>
                  </el-form>
                </div>
              </div>
            </el-tab-pane>

          </el-tabs>
        </div>

      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "UserCenter",
  data() {
    const validatePass = (rule, value, callback) => {
      if (value === '') callback(new Error('请输入新密码'));
      else {
        if (this.pwdForm.confirmPassword !== '') this.$refs.pwdForm.validateField('confirmPassword');
        callback();
      }
    };
    const validatePass2 = (rule, value, callback) => {
      if (value === '') callback(new Error('请再次输入密码'));
      else if (value !== this.pwdForm.newPassword) callback(new Error('两次输入密码不一致!'));
      else callback();
    };

    return {
      activeTab: 'info',
      loading: false,
      form: {...JSON.parse(sessionStorage.getItem("user") || '{}')},
      user: JSON.parse(sessionStorage.getItem("user") || '{}'),
      pwdForm: {password: '', newPassword: '', confirmPassword: ''},
      pwdRules: {
        password: [{required: true, message: '请输入旧密码', trigger: 'blur'}],
        newPassword: [{validator: validatePass, trigger: 'blur'}],
        confirmPassword: [{validator: validatePass2, trigger: 'blur'}]
      }
    }
  },
  created() {
    if (!this.user.username) {
      this.$router.push("/login");
      return;
    }
    this.loadUserInfo();
  },
  methods: {
    loadUserInfo() {
      this.$http.get("/sysUser/username/" + this.user.username).then(res => {
        const data = res.data;
        if (data && (data.code === '200' || data.username)) {
          const userData = data.data || data;
          this.user = userData;
          this.form = JSON.parse(JSON.stringify(userData));
          sessionStorage.setItem("user", JSON.stringify(this.user));
        }
      });
    },
    getImageUrl(url) {
      if (!url) return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';
      if (url.startsWith('http')) return url;
      if (url.startsWith('/')) return 'http://localhost:9090' + url;
      return 'http://localhost:9090/images/' + url;
    },
    handleAvatarSuccess(res) {
      this.form.avatar = res;
      this.$message.success("头像上传成功，请点击保存");
    },
    save() {
      this.loading = true;
      this.$http.post("/sysUser/update", this.form).then(res => {
        const isSuccess = (res.data === true) || (res.data && res.data.code === '200');
        if (isSuccess) {
          this.$message.success("保存成功");
          this.user = JSON.parse(JSON.stringify(this.form));
          this.form = JSON.parse(JSON.stringify(this.user));
          sessionStorage.setItem("user", JSON.stringify(this.user));
          localStorage.setItem("user", JSON.stringify(this.user));
        } else {
          this.$message.error("保存失败，请稍后重试");
        }
      }).catch(err => {
        console.error(err);
        this.$message.error("请求出错，请检查网络或后端服务");
      }).finally(() => {
        this.loading = false;
      });
    },
    updatePassword() {
      this.$refs.pwdForm.validate((valid) => {
        if (valid) {
          this.loading = true;
          this.pwdForm.username = this.user.username;

          this.$http.post("/sysUser/password", this.pwdForm).then(res => {
            const data = res.data;
            if (data === true || (data && data.code === '200')) {
              this.$message.success("密码修改成功，请重新登录");
              sessionStorage.removeItem("user");
              localStorage.removeItem("user");
              this.$router.push("/login");
            } else {
              this.$message.error((data ? data.msg : "") || "旧密码错误");
            }
          }).catch(err => {
            console.error(err);
            this.$message.error("请求失败");
          }).finally(() => {
            this.loading = false;
          });
        }
      });
    }
  }
}
</script>

<style scoped>
/* 1. 引用首页样式 */
@import '../assets/css/user-home.css';

.user-center-layout {
  min-height: 100vh;
  background-color: #f7f9fc;
  background-image: radial-gradient(#e3e8ee 1px, transparent 1px);
  background-size: 20px 20px;
  padding-top: 80px;
  position: relative;
  overflow-x: hidden;
}

.bg-decoration {
  position: absolute;
  top: -100px;
  right: -100px;
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, #fff5e6 0%, #ffe4e4 100%);
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.6;
  z-index: 0;
}

.main-container {
  max-width: 1100px;
  margin: 40px auto;
  padding: 0 20px;
  position: relative;
  z-index: 1;
}

.grid-layout {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 30px;
}

/* 个人中心卡片样式 */
.profile-card {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.06);
  text-align: center;
  position: sticky;
  top: 100px;
  transition: 0.3s;
}

.profile-card:hover {
  transform: translateY(-5px);
}

.card-header-bg {
  height: 120px;
  background: linear-gradient(120deg, #ff9f43 0%, #ffc048 100%);
  position: relative;
}

.waves {
  width: 100%;
  height: 50px;
  position: absolute;
  bottom: 0;
  left: 0;
}

.parallax > use {
  animation: move-forever 25s cubic-bezier(.55, .5, .45, .5) infinite;
}

.parallax > use:nth-child(1) {
  animation-delay: -2s;
  animation-duration: 7s;
}

.parallax > use:nth-child(2) {
  animation-delay: -3s;
  animation-duration: 10s;
}

.parallax > use:nth-child(3) {
  animation-delay: -4s;
  animation-duration: 13s;
}

.parallax > use:nth-child(4) {
  animation-delay: -5s;
  animation-duration: 20s;
}

@keyframes move-forever {
  0% {
    transform: translate3d(-90px, 0, 0);
  }
  100% {
    transform: translate3d(85px, 0, 0);
  }
}

.avatar-section {
  margin-top: -60px;
  position: relative;
  z-index: 2;
  margin-bottom: 15px;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
  cursor: pointer;
}

.real-avatar {
  border: 5px solid white;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  background: white;
  transition: 0.3s;
}

.upload-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  border: 5px solid transparent;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
  opacity: 0;
  transition: 0.3s;
}

.avatar-wrapper:hover .upload-mask {
  opacity: 1;
}

.user-name {
  font-size: 22px;
  font-weight: 800;
  color: #333;
  margin-bottom: 5px;
}

.user-id {
  font-size: 12px;
  color: #999;
  margin-bottom: 15px;
  font-family: monospace;
}

.user-tags {
  margin-bottom: 25px;
}

.tag-badge {
  background: #fff5e6;
  color: #ff9f43;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: bold;
}

.action-section {
  padding: 0 20px 30px;
  display: flex;
  justify-content: center;
  gap: 10px;
}

.settings-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.04);
  padding: 10px 30px 30px;
  min-height: 550px;
}

.custom-tabs >>> .el-tabs__nav-wrap::after {
  height: 1px;
  background-color: #f0f0f0;
}

.custom-tabs >>> .el-tabs__item {
  height: 60px;
  line-height: 60px;
  font-size: 16px;
  color: #666;
}

.custom-tabs >>> .el-tabs__item.is-active {
  color: #ff9f43;
  font-weight: bold;
}

.custom-tabs >>> .el-tabs__active-bar {
  background-color: #ff9f43;
  height: 3px;
  border-radius: 3px;
}

.panel-content {
  padding-top: 20px;
  animation: fadeIn 0.5s ease;
}

.panel-header h3 {
  margin: 0 0 5px 0;
  color: #333;
  font-size: 20px;
}

.panel-header p {
  margin: 0 0 25px 0;
  color: #999;
  font-size: 13px;
}

.form-grid {
  max-width: 600px;
}

.form-grid.narrow {
  max-width: 400px;
}

.input-row {
  display: flex;
  gap: 20px;
}

.input-row .el-form-item {
  flex: 1;
}

.custom-input >>> .el-input__inner {
  border-radius: 8px;
  border: 1px solid #eee;
  transition: 0.3s;
  padding-left: 35px;
}

.custom-input >>> .el-input__inner:focus {
  border-color: #ff9f43;
  box-shadow: 0 0 0 3px rgba(255, 159, 67, 0.1);
}

.custom-input >>> .el-input__prefix {
  left: 8px;
  color: #aaa;
}

.save-btn {
  width: 100%;
  border-radius: 8px;
  font-weight: bold;
  letter-spacing: 1px;
  margin-top: 10px;
}

.el-button--primary {
  background: linear-gradient(90deg, #ff9f43, #ffc048);
  border: none;
  box-shadow: 0 4px 10px rgba(255, 159, 67, 0.3);
}

.el-button--primary:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

.fade-in-up {
  animation: fadeInUp 0.6s ease backwards;
}

.delay-100 {
  animation-delay: 0.1s;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .grid-layout {
    grid-template-columns: 1fr;
  }

  .profile-card {
    position: static;
  }

  .nav-center {
    display: none;
  }

  .user-center-layout {
    padding-top: 70px;
  }
}
</style>
