<template>
  <div class="planet-home-layout">

    <div class="hero-section">
      <div class="hero-overlay"></div>
      <div class="hero-content">
        <h1 class="hero-title">为爱宠，定制科学生活</h1>
        <p class="hero-subtitle">Scientific Breeding · Smart Care · Endless Love</p>
      </div>
    </div>

    <div class="main-wrapper">

      <div class="dashboard-grid">

        <!-- 左侧主控区 -->
        <div class="dash-main">

          <!-- 1. 宠物焦点仪表盘 -->
          <div class="focus-dashboard glass-panel">
            <div class="panel-head">
              <div class="ph-left">
                <span class="ph-title">我的毛孩子</span>
                <span class="ph-sub">COMPANIONS</span>
              </div>
              <div class="ph-right">
                <el-button type="text" class="manage-link" @click="$router.push('/front/pet')">管理全部 <i
                    class="el-icon-arrow-right"></i></el-button>
              </div>
            </div>

            <div v-if="myPets.length > 0" class="pet-control-area">
              <!-- 顶部宠物切换条 -->
              <div class="pet-tabs">
                <div
                    class="pet-tab-item"
                    v-for="p in myPets"
                    :key="p.id"
                    :class="{active: String(currentPetId) === String(p.id)}"
                    @click="currentPetId = p.id"
                >
                  <div class="tab-avatar-wrapper">
                    <img :src="getImageUrl(p.avatar)" class="tab-avatar">
                    <span class="overdue-dot" v-if="hasOverdue(p.id)" title="有时段计划未完成"></span>
                  </div>
                  <span class="tab-name">{{ p.nickname }}</span>
                </div>
                <div class="pet-tab-add" @click="$router.push('/front/pet')">
                  <i class="el-icon-plus"></i>
                </div>
              </div>

              <!-- 选中的宠物详情卡 -->
              <div class="focus-hero-card" v-if="currentPet">
                <div class="fhc-visual">
                  <div class="avatar-halo">
                    <img :src="getImageUrl(currentPet.avatar)" class="fhc-avatar">
                    <div class="status-badge" :class="getHealthClass(currentPet.healthStatus)">
                      <i :class="getHealthIcon(currentPet.healthStatus)"></i>
                      {{ getHealthStatusText(currentPet.healthStatus) }}
                    </div>
                  </div>
                </div>
                <div class="fhc-info">
                  <div class="info-header">
                    <div class="name-box">
                      <span class="pet-name">{{ currentPet.nickname }}</span>
                      <i :class="currentPet.sex === 0 ? 'el-icon-male male-icon' : 'el-icon-female female-icon'"></i>
                    </div>
                    <div class="breed-tag">{{ currentPet.breed || '未知品种' }}</div>
                  </div>

                  <div class="data-row">
                    <div class="data-col">
                      <span class="lbl">年龄</span>
                      <span class="val">{{ currentPet.age || '-' }} <small>岁</small></span>
                    </div>
                    <div class="data-col">
                      <span class="lbl">体重</span>
                      <span class="val">{{ currentPet.weight || '-' }} <small>kg</small></span>
                    </div>
                    <div class="data-col">
                      <span class="lbl">下次疫苗</span>
                      <span class="val" :class="{warn: isVaccineSoon(currentPet.nextVaccine)}">{{
                          currentPet.nextVaccine || '无计划'
                        }}</span>
                    </div>
                  </div>

                  <div class="action-row">
                    <div class="quick-stat-pill" :class="{overdue: overdueFeedCount > 0}"
                         @click="$router.push('/front/feed')">
                      <i class="fas" :class="overdueFeedCount > 0 ? 'fa-clock' : 'fa-utensils'"></i>
                      <span>今日喂养: {{ todayFeedDone }}/{{ todayFeedTotal }}</span>
                      <span class="overdue-tag" v-if="overdueFeedCount > 0">超时提醒</span>
                    </div>
                    <div class="quick-stat-pill warning" v-if="healthAlertCount > 0"
                         @click="$router.push('/front/health')">
                      <i class="fas fa-heartbeat"></i>
                      <span>健康提醒: {{ healthAlertCount }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div v-else class="empty-state">
              <img src="https://cdn-icons-png.flaticon.com/512/7486/7486744.png" class="empty-img">
              <p>还没有添加宠物哦</p>
              <el-button type="primary" round @click="$router.push('/front/pet')">立即添加</el-button>
            </div>
          </div>

          <!-- 2. 商城推荐 (横向流) -->
          <div class="recommend-section">
            <div class="section-title">
              <h3><i class="fas fa-gift" style="color:#FF9F43"></i> 猜你喜欢</h3>
            </div>
            <div class="rec-grid" v-loading="mallLoading">
              <div class="rec-card" v-for="prod in recommendProducts" :key="prod.id"
                   @click="$router.push('/front/mall')">
                <div class="rec-img-box">
                  <img :src="getImageUrl(prod.img || prod.image)" class="rec-img">
                  <!-- 新增：推荐理由标签 -->
                  <div class="reason-tag" v-if="prod.recommendReason">
                    <i class="fas fa-thumbs-up"></i> {{ prod.recommendReason }}
                  </div>
                </div>
                <div class="rec-info">
                  <div class="rec-name" :title="prod.name">{{ prod.name }}</div>
                  <div class="rec-price">¥{{ prod.price }}</div>
                </div>
              </div>
            </div>
          </div>

        </div>

        <!-- 右侧侧边栏 -->
        <div class="dash-side">

          <!-- 用户卡片 -->
          <div class="side-card user-card">
            <div class="uc-bg"></div>
            <div class="uc-content">
              <el-avatar :size="70" :src="getImageUrl(user.avatar)" class="uc-avatar"></el-avatar>
              <div class="uc-name">{{ user.nickname || '铲屎官' }}</div>
              <div class="uc-level"><i class="fas fa-medal"></i> 星球居民 Lv.1</div>

              <div class="uc-stats">
                <div class="us-item">
                  <span class="us-num">{{ myPets.length }}</span>
                  <span class="us-lbl">爱宠</span>
                </div>
                <div class="us-item">
                  <span class="us-num">{{ orderCount }}</span>
                  <span class="us-lbl">订单</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 快捷入口 -->
          <div class="side-card quick-menu">
            <div class="menu-title">快捷服务</div>
            <div class="menu-grid">
              <div class="menu-item" @click="$router.push('/front/feed')">
                <div class="mi-icon c-blue"><i class="fas fa-utensils"></i></div>
                <span class="mi-txt">喂养计划</span>
              </div>
              <div class="menu-item" @click="$router.push('/front/health')">
                <div class="mi-icon c-red"><i class="fas fa-notes-medical"></i></div>
                <span class="mi-txt">健康档案</span>
              </div>
              <div class="menu-item" @click="$router.push('/front/orders')">
                <div class="mi-icon c-green"><i class="fas fa-shopping-bag"></i></div>
                <span class="mi-txt">我的订单</span>
              </div>
              <div class="menu-item consult-entry" @click="openConsult">
                <div class="mi-icon c-orange">
                  <i class="fas fa-comments"></i>
                  <span v-if="consultUnread > 0" class="consult-dot"></span>
                </div>
                <span class="mi-txt">在线咨询</span>
              </div>
            </div>
          </div>

          <!-- 快捷入口 -->
          <div class="side-card tips-card">
            <div class="tip-icon"><i class="fas fa-lightbulb"></i></div>
            <div class="tip-content">
              <div class="tip-title">{{ dailyTip.title }}</div>
              <div class="tip-text">{{ dailyTip.text }}</div>
            </div>
          </div>

        </div>

      </div>

    </div>

  </div>
</template>

<script>
import UserPill from '@/components/UserPill.vue';
export default {
  data() {
    return {
      user: {},
      myPets: [],
      currentPetId: null,
      feedPlans: [],
      recommendProducts: [],
      mallLoading: false,
      orderCount: 0,
      realTotalCost: '0.00',
      consultUnread: 0,
      currentTime: '',
      timeTimer: null,
      tipPool: [
        {title: '每日小贴士', text: '定期给宠物梳毛可以减少家中掉毛，还能增进感情哦！'},
        {title: '每日小贴士', text: '外出遛宠记得佩戴牵引绳，并检查项圈松紧是否合适。'},
        {title: '每日小贴士', text: '换粮建议循序渐进：7 天逐步过渡，减少肠胃不适。'},
        {title: '每日小贴士', text: '猫砂盆建议每天清理，能有效降低异味并预防泌尿问题。'},
        {title: '每日小贴士', text: '饭后 30 分钟再运动，避免剧烈活动引发呕吐或胀气。'},
        {title: '每日小贴士', text: '定期驱虫与疫苗很重要，记得按计划在健康档案里记录。'}
      ]
    };
  },
  computed: {
    dailyTip() {
      const pool = Array.isArray(this.tipPool) ? this.tipPool : [];
      if (pool.length === 0) return {title: '每日小贴士', text: ''};
      const todayIndex = this.getTipIndexByDay();
      return pool[todayIndex] || pool[0];
    },
    currentPet() {
      if (!this.myPets || this.myPets.length === 0) return null;
      const hit = this.myPets.find(p => String(p.id) === String(this.currentPetId));
      return hit || this.myPets[0];
    },
    todayWeekDay() {
      const d = new Date();
      return (d.getDay() + 6) % 7 + 1;
    },
    todayFeedList() {
      const wd = this.todayWeekDay;
      let list = Array.isArray(this.feedPlans) ? this.feedPlans : [];
      list = list.filter(p => {
        const planWd = p.weekDay;
        return planWd === null || planWd === undefined || planWd === 0 || Number(planWd) === wd;
      });
      if (this.currentPetId !== null && this.currentPetId !== undefined && this.currentPetId !== '') {
        list = list.filter(p => p.petId === null || p.petId === undefined || String(p.petId) === String(this.currentPetId));
      }
      return list;
    },
    todayFeedTotal() {
      return this.todayFeedList.length;
    },
    todayFeedDone() {
      return this.todayFeedList.filter(p => Number(p.status) === 1).length;
    },
    healthAlertCount() {
      const pets = Array.isArray(this.myPets) ? this.myPets : [];
      return pets.filter(p => {
        const hs = p.healthStatus;
        const hasHealthIssue = hs !== null && hs !== undefined && Number(hs) !== 0;
        return hasHealthIssue || this.isVaccineSoon(p.nextVaccine);
      }).length;
    },
    overdueFeedCount() {
      return this.todayFeedList.filter(p => {
        if (Number(p.status) === 1) return false;
        if (!p.planTime) return false;
        // 字符串比较 "08:00" < "09:00"
        return p.planTime < this.currentTime;
      }).length;
    }
  },
  watch: {
    currentPetId(val) {
      if (val === null || val === undefined) return;
      sessionStorage.setItem('currentPetId', String(val));
    }
  },
  created() {
    const userStr = sessionStorage.getItem("user");
    if (userStr) {
      this.user = JSON.parse(userStr);
      this.initData();
      this.loadConsultUnread();
      if (this.$root) {
        this.$root.$on('consult-unread-changed', this.handleConsultUnreadChanged);
      }
    } else {
      this.$router.push("/login");
    }
  },
  mounted() {
    this.updateCurrentTime();
    this.timeTimer = setInterval(this.updateCurrentTime, 30000);
  },
  destroyed() {
    if (this.timeTimer) clearInterval(this.timeTimer);
    if (this.$root) {
      this.$root.$off('consult-unread-changed', this.handleConsultUnreadChanged);
    }
  },
  methods: {
    updateCurrentTime() {
      const now = new Date();
      const h = String(now.getHours()).padStart(2, '0');
      const m = String(now.getMinutes()).padStart(2, '0');
      this.currentTime = `${h}:${m}`;
    },
    hasOverdue(petId) {
      const wd = this.todayWeekDay;
      let list = Array.isArray(this.feedPlans) ? this.feedPlans : [];
      const petPlans = list.filter(p => {
        const planWd = p.weekDay;
        const isToday = planWd === null || planWd === undefined || planWd === 0 || Number(planWd) === wd;
        const isThisPet = String(p.petId) === String(petId);
        return isToday && isThisPet;
      });
      return petPlans.some(p => {
        if (Number(p.status) === 1) return false;
        if (!p.planTime) return false;
        return p.planTime < this.currentTime;
      });
    },
    scrollToTop() { window.scrollTo({ top: 0, behavior: 'smooth' }); },
    openConsult() {
      if (this.$root) this.$root.$emit('open-consult');
    },
    handleConsultUnreadChanged(val) {
      this.consultUnread = Number(val) || 0;
    },
    getTipIndexByDay() {
      const now = new Date();
      const start = new Date(now.getFullYear(), 0, 0);
      const dayOfYear = Math.floor((now.getTime() - start.getTime()) / 86400000);
      const uid = Number(this.user && this.user.id) || 0;
      const poolLen = (Array.isArray(this.tipPool) && this.tipPool.length) ? this.tipPool.length : 1;
      const idx = (dayOfYear + uid) % poolLen;
      return idx < 0 ? 0 : idx;
    },
    getImageUrl(url) {
      if (!url) return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';
      if (url.startsWith('http')) return url;
      if (url.startsWith('/')) return this.$baseUrl + url;
      return this.$baseUrl + '/images/' + url;
    },
    getPetTypeText(type) {
      if (Number(type) === 0) return '狗狗';
      if (Number(type) === 1) return '猫猫';
      return '宠物';
    },
    getPetSexText(sex) {
      if (Number(sex) === 0) return '公';
      if (Number(sex) === 1) return '母';
      return '-';
    },
    getHealthClass(status) {
      const s = Number(status);
      if (s === 1) return 'bad';
      if (s === 2) return 'treating';
      return 'good';
    },
    getHealthIcon(status) {
      const s = Number(status);
      if (s === 1) return 'fas fa-heart-broken';
      if (s === 2) return 'fas fa-band-aid';
      return 'fas fa-heart';
    },
    getHealthStatusText(status) {
      const s = Number(status);
      if (s === 1) return '生病中';
      if (s === 2) return '治疗中';
      return '健康状态';
    },
    formatWeight(weight) {
      if (weight === null || weight === undefined || weight === '') return '-';
      const n = Number(weight);
      if (Number.isNaN(n)) return String(weight);
      return n + ' kg';
    },
    isVaccineSoon(dateStr) {
      if (!dateStr) return false;
      const parts = String(dateStr).split('-').map(v => Number(v));
      if (parts.length !== 3 || parts.some(n => Number.isNaN(n))) return false;
      const target = new Date(parts[0], parts[1] - 1, parts[2]);
      const now = new Date();
      const startToday = new Date(now.getFullYear(), now.getMonth(), now.getDate());
      const diffDays = Math.floor((target.getTime() - startToday.getTime()) / (24 * 60 * 60 * 1000));
      return diffDays >= 0 && diffDays <= 7;
    },

    initData() {
      this.fetchMyPets();
      this.fetchFeedPlans();
      this.fetchRecommendProducts();
      this.fetchRealOrderData();
    },
    fetchMyPets() {
      this.$http.get('/petInfo/list/' + this.user.id).then(res => {
        this.myPets = Array.isArray(res.data) ? res.data : (res.data.data || []);
        const saved = sessionStorage.getItem('currentPetId');
        if (saved) {
          this.currentPetId = saved;
          return;
        }
        if (this.myPets.length > 0) {
          this.currentPetId = this.myPets[0].id;
        }
      });
    },
    fetchFeedPlans() {
      this.$http.get('/feedPlan/list/user/' + this.user.id).then(res => {
        this.feedPlans = res.data.data || res.data || [];
      });
    },
    fetchRecommendProducts() {
      this.mallLoading = true;
      this.$http.get('/product/recommend', {
        params: {userId: this.user.id}
      }).then(res => {
        this.mallLoading = false;
        const list = res.data.data || res.data || [];
        this.recommendProducts = list.slice(0, 5);
      }).catch(err => {
        console.error("推荐接口调用失败", err);
        this.mallLoading = false;
      });
    },
    fetchRealOrderData() {
      this.$http.get('/orders/list').then(res => {
        let list = res.data.data || res.data;
        const myOrders = list.filter(o => o.userId === this.user.id && Number(o.status) !== -1);
        this.orderCount = myOrders.length;
        let sum = 0;
        myOrders.forEach(o => {
          if (o.status > 0) sum += Number(o.totalAmount);
        });
        this.realTotalCost = sum.toFixed(2);
      });
    },

    loadConsultUnread() {
      if (!this.user || !this.user.id) return;
      this.$http.get('/chat/allMessages', {
        params: {userId: this.user.id}
      }).then(res => {
        const msgs = res.data || [];
        const tempUnread = {};
        msgs.forEach(m => {
          if (m.receiverId === this.user.id && (m.isRead === 0 || m.isRead === null)) {
            if (!tempUnread[m.senderId]) tempUnread[m.senderId] = 0;
            tempUnread[m.senderId]++;
          }
        });
        this.consultUnread = Object.values(tempUnread).reduce((sum, v) => sum + v, 0);
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
