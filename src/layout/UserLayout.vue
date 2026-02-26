<template>
  <div class="user-layout-container">
    <div class="nav-header" :class="{ 'scrolled': isScrolled || isNotHome }">
      <div class="nav-left">
        <div class="brand" @click="$router.push('/front/home')">
          <img src="@/assets/logo.png" class="logo-img" alt="logo">
          <div class="brand-group">
            <span class="brand-text">Paw Planet</span>
            <i class="fas fa-paw logo-icon"></i>
          </div>
        </div>
      </div>
      <div class="nav-center">
        <div class="nav-item" :class="{ active: $route.path === '/front/home' }" @click="$router.push('/front/home')">
          首页
        </div>
        <div class="nav-item" :class="{ active: $route.path === '/front/pet' }" @click="$router.push('/front/pet')">
          我的宠物
        </div>
        <div class="nav-item" :class="{ active: $route.path === '/front/feed' }" @click="$router.push('/front/feed')">
          喂养计划
        </div>
        <div class="nav-item" :class="{ active: $route.path === '/front/health' }"
             @click="$router.push('/front/health')">健康档案
        </div>
        <div class="nav-item" :class="{ active: $route.path === '/front/mall' }" @click="$router.push('/front/mall')">
          严选商城
        </div>
        <div class="nav-item" :class="{ active: $route.path === '/front/orders' }"
             @click="$router.push('/front/orders')">我的订单
        </div>
      </div>
      <div class="nav-right">
        <!-- 通用：通知中心入口 -->
        <el-popover placement="bottom-end" width="320" trigger="hover" popper-class="notice-popover">
          <div class="notice-popover-header">
            <span class="nph-title">健康关怀通知</span>
            <el-link type="primary" :underline="false" style="font-size:12px" v-if="unreadCount > 0"
                     @click="markAllRead">全部已读
            </el-link>
          </div>
          <div class="notice-popover-list" v-if="notices.length > 0">
            <div class="npl-item" v-for="n in notices" :key="n.id">
              <div class="npl-icon"><i class="fas fa-heartbeat"></i></div>
              <div class="npl-content">
                <div class="npl-top">
                  <span class="npl-title">{{ n.title }}</span>
                  <span class="npl-time">{{ n.createTime | formatTime }}</span>
                </div>
                <div class="npl-body">{{ n.content }}</div>
              </div>
            </div>
          </div>
          <div class="notice-empty" v-else>
            <i class="fas fa-bell-slash"></i>
            <p>暂无新通知</p>
          </div>
          <div class="nav-notice-btn" slot="reference">
            <i class="fas fa-bell"></i>
            <span class="notice-badge" v-if="unreadCount > 0">{{ unreadCount }}</span>
          </div>
        </el-popover>

        <el-dropdown trigger="click" @command="handleUserCommand">
          <UserPill :user="user"/>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="profile" icon="el-icon-user">个人中心</el-dropdown-item>
            <el-dropdown-item command="logout" divided icon="el-icon-switch-button" style="color:#f56c6c;">退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </div>

    <!-- 页面主体内容 -->
    <div class="layout-main">
      <router-view/>
    </div>
  </div>
</template>

<script>
import UserPill from '@/components/UserPill.vue';

export default {
  name: 'UserLayout',
  components: {UserPill},
  filters: {
    formatTime(time) {
      if (!time) return '';
      const date = new Date(time);
      const m = String(date.getMonth() + 1).padStart(2, '0');
      const d = String(date.getDate()).padStart(2, '0');
      const h = String(date.getHours()).padStart(2, '0');
      const min = String(date.getMinutes()).padStart(2, '0');
      return `${m}-${d} ${h}:${min}`;
    }
  },
  data() {
    return {
      user: {},
      isScrolled: false,
      notices: [],
      unreadCount: 0,
      socket: null,
      reconnectTimer: null
    };
  },
  computed: {
    isNotHome() {
      return this.$route.path !== '/front/home';
    }
  },
  created() {
    const userStr = sessionStorage.getItem("user");
    if (userStr) {
      this.user = JSON.parse(userStr);
      this.fetchNotices();
      this.initWebSocket();
    } else {
      this.$router.push("/login");
    }
  },
  mounted() {
    window.addEventListener('scroll', this.handleScroll);
  },
  destroyed() {
    window.removeEventListener('scroll', this.handleScroll);
    if (this.socket) this.socket.close();
    if (this.reconnectTimer) clearTimeout(this.reconnectTimer);
  },
  methods: {
    initWebSocket() {
      if (typeof (WebSocket) === "undefined") return;
      if (!this.user.id) return;

      const wsUrl = `ws://localhost:9090/ws/chat/${this.user.id}`;
      this.socket = new WebSocket(wsUrl);
      this.socket.onmessage = (msg) => {
        if (msg.data === "NEW_NOTICE") {
          this.$notify({
            title: '新消息提示',
            message: '您收到一条新的健康关怀通知',
            type: 'info',
            position: 'bottom-right'
          });
          this.fetchNotices();
        }
      };
      this.socket.onclose = () => {
        this.reconnectTimer = setTimeout(() => this.initWebSocket(), 5000);
      };
    },
    handleScroll() {
      this.isScrolled = window.scrollY > 50;
    },
    fetchNotices() {
      if (!this.user || !this.user.id) return;
      // 1. 获取未读数量
      this.$http.get('/sysNotice/unreadCount', {params: {userId: this.user.id}}).then(res => {
        this.unreadCount = res.data.data || 0;
      });
      // 2. 获取通知列表
      this.$http.get('/sysNotice/list', {params: {userId: this.user.id, limit: 5}}).then(res => {
        this.notices = res.data.data || [];
      });
    },
    markAllRead() {
      if (!this.user || !this.user.id) return;
      this.$http.post('/sysNotice/readAll', {userId: this.user.id}).then(res => {
        if (res.data.code === '200') {
          this.notices = [];
          this.unreadCount = 0;
          this.$message.success('已全部标记为已读');
          this.fetchNotices();
        }
      });
    },
    handleUserCommand(cmd) {
      if (cmd === 'logout') {
        sessionStorage.removeItem("user");
        localStorage.removeItem("user");
        this.$router.push("/login");
      } else if (cmd === 'profile') {
        this.$router.push("/front/user-center");
      }
    }
  }
};
</script>

<style scoped src="@/assets/css/user-home.css"></style>
<style scoped>
.layout-main {
}

.user-layout-container:has(.nav-header.scrolled) .layout-main {
}
</style>
