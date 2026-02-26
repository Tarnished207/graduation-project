<template>
  <div style="height: 100%">
    <el-container style="height: 100%;">

      <el-aside width="220px" style="background-color: #E3F2F3; height: 100%; z-index: 10; overflow-x: hidden;">

        <div class="sidebar-logo">
          <img src="../assets/logo.png" alt="logo" />
        </div>

        <el-menu
            :default-openeds="['1', '2']"
            background-color="transparent"
            text-color="#1E3648"
            style="border-right: none; width: 220px;"
            router
            :default-active="$route.path">

          <el-menu-item index="/home">
            <i class="el-icon-s-home"></i>
            <span slot="title">后台首页</span>
          </el-menu-item>

          <el-menu-item index="/monitor">
            <i class="el-icon-pie-chart"></i>
            <span slot="title">数据监控中心</span>
          </el-menu-item>

          <el-submenu index="1">
            <template slot="title">
              <i class="el-icon-user-solid"></i>
              <span>档案管理</span>
            </template>
            <el-menu-item index="/user" class="nest-menu-item">
              <i class="el-icon-user"></i> 用户列表
            </el-menu-item>
            <el-menu-item index="/pet" class="nest-menu-item">
              <i class="el-icon-sugar"></i> 宠物列表
            </el-menu-item>
          </el-submenu>

          <el-submenu index="2">
            <template slot="title">
              <i class="el-icon-s-shop"></i>
              <span>商城运营</span>
            </template>
            <el-menu-item index="/product" class="nest-menu-item">
              <i class="el-icon-goods"></i> 商品库管理
            </el-menu-item>
            <el-menu-item index="/orders" class="nest-menu-item">
              <i class="el-icon-s-order"></i> 订单处理
            </el-menu-item>
          </el-submenu>

        </el-menu>
      </el-aside>

      <el-container>
        <el-header style="text-align: right; font-size: 12px; border-bottom: none; line-height: 60px; background-color: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.06); z-index: 9;">
          <div style="display: flex; justify-content: space-between; align-items: center; height: 100%;">

            <div style="font-size: 14px; color: #606266; margin-left: 10px; display:flex; align-items:center;">
              <i class="el-icon-s-fold" style="font-size: 20px; margin-right: 12px; cursor: pointer; color: #333;"></i>
              <span style="letter-spacing: 0.5px; font-weight: 500; color: #1E3648;">欢迎回来，指挥官！</span>
            </div>

            <div style="display: flex; align-items: center;">
              <el-avatar size="small" :src="getImageUrl(user.avatar)"
                         style="margin-right: 10px; border: 2px solid #f0f2f5;">
                <img src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png"/>
              </el-avatar>

              <span style="font-size: 14px; margin-right: 10px; font-weight: 600; color: #333;">{{ user.nickname || '管理员' }}</span>

              <el-dropdown @command="handleCommand">
                <i class="el-icon-caret-bottom" style="font-size: 14px; cursor: pointer; color: #909399;"></i>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="logout" style="color: #ff6b6b;">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </div>
          </div>
        </el-header>

        <el-main style="background-color: #f5f7fa; padding: 20px;">
          <transition name="fade-transform" mode="out-in">
            <router-view />
          </transition>
        </el-main>
      </el-container>

    </el-container>
  </div>
</template>

<script>
export default {
  data() {
    return {
      user: {}
    }
  },
  created() {
    // 修复点：改为 sessionStorage，确保能读到登录信息
    let userStr = sessionStorage.getItem("user") || "{}";
    this.user = JSON.parse(userStr);
  },
  methods: {
    // 修复点：添加图片路径处理函数
    getImageUrl(url) {
      if (!url) return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';
      if (url.startsWith('http')) return url;
      return 'http://localhost:9090/images/' + url;
    },
    handleCommand(command) {
      if (command === 'logout') {
        this.logout();
      }
    },
    logout() {
      // 修复点：退出时清理 sessionStorage
      sessionStorage.removeItem("user");
      this.$router.push('/');
      this.$message.success("已安全退出");
    }
  }
}
</script>

<style scoped>
.sidebar-logo {
  height: 100px;
  line-height: 100px;
  text-align: center;
  background-color: #E3F2F3;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: none;
  padding: 10px 0;
}

.sidebar-logo img {
  height: 90px;
  width: auto;
  filter: drop-shadow(0 4px 6px rgba(0, 0, 0, 0.1));
}

.el-menu-item, .el-submenu__title {
  height: 50px;
  line-height: 50px;
  font-size: 14px;
  margin: 4px 8px;
  border-radius: 8px;
  color: #1E3648 !important;
  font-weight: 500;
}

.el-menu-item i, .el-submenu__title i {
  color: #4F7F9F;
  margin-right: 10px;
  font-size: 18px;
  transition: all 0.3s;
}

.el-menu-item.is-active {
  background: linear-gradient(135deg, #42b883 0%, #35495e 100%) !important;
  color: #fff !important;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(66, 184, 131, 0.3);
}

.el-menu-item.is-active i {
  color: #fff !important;
  transform: scale(1.1);
}

.el-menu-item:hover, .el-submenu__title:hover {
  background-color: rgba(66, 184, 131, 0.1) !important;
  color: #1E3648 !important;
}
.el-menu-item:hover i, .el-submenu__title:hover i {
  color: #42b883 !important;
}

.nest-menu-item i {
  margin-left: 6px;
}

.fade-transform-enter-active, .fade-transform-leave-active {
  transition: all .3s;
}
.fade-transform-enter {
  opacity: 0;
  transform: translateX(-10px);
}
.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(10px);
}
.el-container {
  background-color: #E3F2F3;
}

.el-header {
  border-top-left-radius: 20px;
  background-color: #fff;
}
.el-main {
  border-bottom-left-radius: 20px;
  background-color: #f5f7fa;
}
</style>