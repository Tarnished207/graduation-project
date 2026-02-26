<template>
  <div class="user-feed-layout">

    <div class="main-container">
      <div class="feed-dashboard fade-in">

        <div class="side-panel">
          <div class="panel-header">
            <span>宠物列表</span>
          </div>

          <div class="pet-selector-list" v-loading="petLoading">
            <div
                class="pet-select-item"
                v-for="pet in myPets"
                :key="pet.id"
                :class="{active: currentPetId === pet.id}"
                @click="selectPet(pet.id)">

              <div class="avatar-wrapper">
                <img
                    :src="getImageUrl(pet.avatar)"
                    class="avatar-box"
                    :class="(
                      ((allPlans.filter(p => p.petId === pet.id && (((p.weekDay || 0) === 0) || (p.weekDay === currentWeekDay))).length === 0)
                       || (allPlans.filter(p => p.petId === pet.id && (((p.weekDay || 0) === 0) || (p.weekDay === currentWeekDay))).every(p => p.status === 1)))
                        ? 'ring-green'
                        : 'ring-orange'
                    )">
              </div>

              <div class="info">
                <div class="name-row">
                  <span class="name">{{ pet.nickname }}</span>
                  <el-tag size="mini" effect="dark" v-if="pet.healthStatus!==0" type="danger"
                          style="transform: scale(0.8);">
                    {{ getStatusText(pet.healthStatus) }}
                  </el-tag>
                </div>
                <span class="breed">{{ pet.breed || '中华田园宠' }}</span>
              </div>

              <i v-if="currentPetId === pet.id" class="el-icon-caret-right active-arrow"></i>
            </div>

            <div v-if="myPets.length === 0 && !petLoading" class="empty-tip">
              暂无宠物，快去添加吧
            </div>
          </div>

          <div class="pet-profile-card" v-if="currentPet">
            <div class="card-title"><i class="el-icon-collection-tag"></i> 档案卡</div>
            <div class="profile-grid">
              <div class="p-item">
                <span class="val">{{ currentPet.weight || '-' }} <small>kg</small></span>
                <span class="lbl">体重</span>
              </div>
              <div class="p-item">
                <span class="val">{{ currentPet.age || '-' }} <small>岁</small></span>
                <span class="lbl">年龄</span>
              </div>
              <div class="p-item">
                <span class="val">{{ currentPet.sex === 1 ? '妹妹' : '弟弟' }}</span>
                <span class="lbl">性别</span>
              </div>
            </div>
            <div class="energy-box">
              <div class="eb-label">💡 每日热量建议</div>
              <div class="eb-val">{{ calculateCalories(currentPet.weight) }} kcal</div>
            </div>
          </div>
        </div>

        <div class="main-content">

          <div class="health-alert" v-if="currentPet && currentPet.healthStatus !== 0">
            <div class="ha-left">
              <i class="el-icon-first-aid-kit"></i>
              <span><b>{{ currentPet.nickname }}</b> 身体抱恙 ({{ getStatusText(currentPet.healthStatus) }})，请遵医嘱调整饮食。</span>
            </div>
            <el-button size="mini" type="danger" round plain @click="$router.push('/front/health')">查看处方</el-button>
          </div>

          <div class="stats-board">
            <div class="stats-left">
              <h3>{{ getWeekDayLabelSafe(currentWeekDay) }}食谱</h3>
              <div class="progress-wrap">
                <el-progress
                    type="circle"
                    :percentage="completionRate"
                    :width="50"
                    :stroke-width="4"
                    :show-text="false"
                    :color="customColors"></el-progress>
                <div class="progress-text">
                  <span>完成度</span>
                  <b>{{ completedCount }}/{{ plansForCurrentView.length }}</b>
                </div>
              </div>
            </div>
            <div class="stats-right">
              <el-button type="primary" icon="el-icon-plus" round @click="handleAdd">新增计划</el-button>
            </div>
          </div>

          <div class="week-nav-rich">
            <div
                class="day-block"
                v-for="day in weekDays"
                :key="day.val"
                :class="{ active: currentWeekDay === day.val, today: isToday(day.val) }"
                @click="currentWeekDay = day.val">
              <div class="d-label">{{ day.label }}</div>
              <div class="d-status">
                <i class="el-icon-success" v-if="hasPlanInDay(day.val) && isDayCompleted(day.val)"
                   style="color:#67c23a"></i>
                <span class="dot-warn" v-else-if="hasPlanInDay(day.val)"></span>
                <span class="dot-empty" v-else></span>
              </div>
            </div>
          </div>

          <div class="rich-list-wrapper" v-loading="loading">
            <div class="scroll-container">
              <div v-if="sortedPlans.length > 0" class="plans-container">
                <div
                    class="plan-item"
                    v-for="plan in sortedPlans"
                    :key="plan.id"
                    :class="{ 'is-done': plan.status === 1 }">

                  <div class="pi-check" @click="toggleStatus(plan)">
                    <i class="el-icon-success" v-if="plan.status === 1"></i>
                    <div class="circle" v-else></div>
                  </div>

                  <div class="pi-time">
                    <span class="big-time">{{ plan.planTime }}</span>
                    <span class="tag-once">{{ getWeekDayLabelSafe(plan.weekDay) }}</span>
                  </div>

                  <div class="pi-content">
                    <div class="pi-header">
                      <span class="pi-title">{{ plan.planName }}</span>
                      <el-tag size="mini" type="info" effect="plain" class="pet-tag" v-if="!currentPetId">
                        {{ getPetName(plan.petId) }}
                      </el-tag>
                    </div>
                    <div class="pi-detail">
                      <span v-if="plan.foodName" class="food-info">
                         <i class="fas fa-utensils"></i> {{ plan.foodName }}
                      </span>
                      <span v-if="plan.amount" class="amount-info">
                         x {{ plan.amount }}
                      </span>
                    </div>
                  </div>

                  <div class="pi-action">
                    <div class="mall-btn" v-if="isProductInMall(plan.foodName)"
                         @click.stop="$router.push('/front/mall')">
                      <i class="el-icon-shopping-bag-1"></i> 补货
                    </div>

                    <el-dropdown trigger="click" @command="(cmd) => handleItemCommand(cmd, plan)">
                      <span class="el-dropdown-link action-more">
                        <i class="el-icon-more"></i>
                      </span>
                      <el-dropdown-menu slot="dropdown">
                        <el-dropdown-item command="edit" icon="el-icon-edit">编辑</el-dropdown-item>
                        <el-dropdown-item command="delete" icon="el-icon-delete" style="color:#F56C6C">删除
                        </el-dropdown-item>
                      </el-dropdown-menu>
                    </el-dropdown>
                  </div>
                </div>
              </div>

              <div v-else class="empty-rich">
                <img src="https://cdn-icons-png.flaticon.com/512/7486/7486754.png">
                <p>本日暂无任务，点击右上角添加</p>
              </div>
            </div>
          </div>

          <div class="recommend-section" v-if="recommendProducts.length > 0">
            <div class="rec-header">
              <span class="rec-title"><i class="el-icon-goods"></i> 猜你喜欢</span>
              <span class="rec-more" @click="$router.push('/front/mall')">去商城逛逛 ></span>
            </div>
            <div class="rec-list">
              <div class="rec-card" v-for="prod in recommendProducts" :key="prod.id"
                   @click="$router.push('/front/mall')">
                <img :src="getImageUrl(prod.img || prod.image)" class="rec-img">
                <div class="rec-info">
                  <div class="r-name">{{ prod.name }}</div>
                  <div class="r-price">¥ {{ prod.price }}</div>
                </div>
                <div class="rec-add"><i class="el-icon-shopping-cart-2"></i></div>
              </div>
            </div>
          </div>

        </div>
      </div>
    </div>

    <el-dialog :visible.sync="dialogVisible" :title="form.id?'编辑计划':'添加计划'" width="450px" center
               custom-class="clean-dialog">
      <el-form :model="form" label-width="80px" size="small">
        <el-form-item label="喂食对象">
          <el-select v-model="form.petId" style="width:100%" @change="handlePetChangeInForm">
            <el-option v-for="p in myPets" :key="p.id" :label="p.nickname" :value="p.id"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="执行周期">
          <el-select v-model="form.weekDay" placeholder="请选择" style="width:100%">
            <el-option label="🔁 每天 (自动生成7天计划)" :value="0"></el-option>
            <el-option v-for="day in weekDays" :key="day.val" :label="`📅 仅每${day.label}`"
                       :value="day.val"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="时间">
          <el-time-select v-model="form.planTime" :picker-options="{start:'06:00', step:'00:15', end:'23:45'}"
                          style="width:100%"></el-time-select>
        </el-form-item>
        <el-form-item label="内容">
          <el-autocomplete v-model="form.planName" :fetch-suggestions="queryTags" placeholder="如: 早餐"
                           style="width:100%"></el-autocomplete>
        </el-form-item>
        <el-form-item label="食物">
          <el-autocomplete v-model="form.foodName" :fetch-suggestions="querySearchProducts" placeholder="关联商城商品"
                           style="width:100%" @select="handleSelectProduct">
            <template slot-scope="{ item }">
              <div class="name">{{ item.value }}</div>
            </template>
          </el-autocomplete>
        </el-form-item>
        <el-form-item label="份量">
          <el-input v-model="form.amount" placeholder="如: 50g">
            <template slot="append" v-if="recAmount">建议: {{ recAmount }}g</template>
          </el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import UserPill from '@/components/UserPill.vue';
export default {
  components: {UserPill},
  name: "UserFeed",
  data() {
    return {
      user: JSON.parse(sessionStorage.getItem("user") || '{}'),
      myPets: [],
      allPlans: [],
      allProducts: [],
      recommendProducts: [],
      currentPetId: null,
      loading: false,
      petLoading: false,
      dialogVisible: false,
      form: {},
      recAmount: '',

      weekDays: [
        {label: '周一', val: 1}, {label: '周二', val: 2}, {label: '周三', val: 3},
        {label: '周四', val: 4}, {label: '周五', val: 5}, {label: '周六', val: 6},
        {label: '周日', val: 7}
      ],
      currentWeekDay: new Date().getDay() || 7,
      todayVal: new Date().getDay() || 7,

      customColors: [
        {color: '#f56c6c', percentage: 40},
        {color: '#e6a23c', percentage: 80},
        {color: '#67c23a', percentage: 100}
      ]
    };
  },
  computed: {
    currentPet() {
      if (!this.currentPetId) return null;
      return this.myPets.find(p => p.id === this.currentPetId);
    },
    plansForCurrentView() {
      if (!this.currentPetId && this.myPets.length > 0) return [];
      const pid = this.currentPetId || (this.myPets[0] ? this.myPets[0].id : null);
      if (!pid) return [];

      const petPlans = this.allPlans.filter(p => p.petId === pid);
      return petPlans.filter(p => {
        return (p.weekDay || 0) === 0 || p.weekDay === this.currentWeekDay;
      });
    },
    sortedPlans() {
      return [...this.plansForCurrentView].sort((a, b) => {
        if ((a.status || 0) !== (b.status || 0)) {
          return (a.status || 0) - (b.status || 0);
        }
        return a.planTime > b.planTime ? 1 : -1;
      });
    },
    completedCount() {
      return this.plansForCurrentView.filter(p => p.status === 1).length;
    },
    completionRate() {
      if (!this.plansForCurrentView.length) return 0;
      return Math.round((this.completedCount / this.plansForCurrentView.length) * 100);
    }
  },
  created() {
    if (!this.user.id) return this.$router.push("/login");
    this.initData();
  },
  methods: {
    getImageUrl(url) {
      if (!url) return 'https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png';
      if (url.startsWith('http')) return url;
      if (url.startsWith('/')) return 'http://localhost:9090' + url;
      return 'http://localhost:9090/images/' + url;
    },
    handleUserCommand(cmd) {
      if (cmd === 'logout') {
        sessionStorage.removeItem("user");
        this.$router.push("/login");
      } else if (cmd === 'home') this.$router.push("/front/home");
    },

    async initData() {
      this.loading = true;
      this.petLoading = true;
      try {
        const res = await this.$http.get('/petInfo/list/' + this.user.id);
        this.myPets = res.data || [];

        if (this.myPets.length > 0 && !this.currentPetId) {
          this.currentPetId = this.myPets[0].id;
        }

        const res2 = await this.$http.get('/feedPlan/list/user/' + this.user.id);
        this.allPlans = res2.data || [];
      } catch (e) {
        console.error(e);
      } finally {
        this.loading = false;
        this.petLoading = false;
      }

      this.$http.get('/product/list', {params: {pageNum: 1, pageSize: 50}}).then(res => {
        const list = res.data.data || res.data || [];
        this.allProducts = list.map(item => ({value: item.name}));
        this.recommendProducts = list.sort(() => Math.random() - 0.5).slice(0, 4);
      });
    },

    selectPet(id) {
      this.currentPetId = id;
    },

    toggleStatus(plan) {
      const newStatus = plan.status === 1 ? 0 : 1;
      plan.status = newStatus;
      this.$http.post('/feedPlan/update', plan).then(res => {
        if (!res.data) plan.status = plan.status === 1 ? 0 : 1;
      });
    },

    hasPlanInDay(dayVal) {
      if (!this.currentPetId) return false;
      return this.allPlans.some(p => p.petId === this.currentPetId && ((p.weekDay || 0) === 0 || p.weekDay === dayVal));
    },
    isDayCompleted(dayVal) {
      if (!this.currentPetId) return false;
      const dayPlans = this.allPlans.filter(p => p.petId === this.currentPetId && ((p.weekDay || 0) === 0 || p.weekDay === dayVal));
      if (dayPlans.length === 0) return false;
      return dayPlans.every(p => p.status === 1);
    },

    isToday(val) {
      return val === this.todayVal;
    },
    getWeekDayLabelSafe(val) {
      if (!val) return '今日';
      const day = this.weekDays.find(w => w.val === val);
      return day ? day.label : '未知';
    },

    getHealthClass(status) {
      return {0: 'h-green', 1: 'h-red', 2: 'h-yellow'}[status] || 'h-gray';
    },
    getStatusText(status) {
      return {0: '健康', 1: '生病护理', 2: '治疗恢复'}[status] || '未知';
    },
    calculateCalories(w) {
      return w ? Math.round(70 * Math.pow(w, 0.75)) : 0;
    },
    getPetName(id) {
      const p = this.myPets.find(i => i.id === id);
      return p ? p.nickname : '';
    },

    isProductInMall(foodName) {
      if (!foodName || !this.allProducts.length) return false;
      return this.allProducts.some(p => foodName.includes(p.value) || p.value.includes(foodName));
    },

    queryTags(qs, cb) {
      cb([{value: '☀️ 早餐'}, {value: '🌙 晚餐'}, {value: '🍛 午餐'}, {value: '🍖 加餐'}, {value: '🍭 零食'}]);
    },
    querySearchProducts(qs, cb) {
      const res = qs ? this.allProducts.filter(i => i.value.toLowerCase().includes(qs.toLowerCase())) : this.allProducts;
      cb(res);
    },
    handleSelectProduct(item) {
      this.form.foodName = item.value;
    },

    handleAdd() {
      if (this.myPets.length === 0) return this.$message.warning("请先添加宠物");
      this.form = {
        userId: this.user.id,
        petId: this.currentPetId,
        planTime: '08:00',
        planName: '☀️ 早餐',
        weekDay: this.currentWeekDay,
        status: 0
      };
      this.handlePetChangeInForm(this.currentPetId);
      this.dialogVisible = true;
    },
    handleItemCommand(cmd, plan) {
      if (cmd === 'edit') this.handleEdit(plan);
      if (cmd === 'delete') this.handleDelete(plan.id);
    },
    handleEdit(row) {
      this.form = JSON.parse(JSON.stringify(row));
      if (this.form.weekDay === undefined || this.form.weekDay === null) this.$set(this.form, 'weekDay', 0);
      this.handlePetChangeInForm(this.form.petId);
      this.dialogVisible = true;
    },
    handlePetChangeInForm(petId) {
      const p = this.myPets.find(i => i.id === petId);
      if (p && p.weight) this.recAmount = Math.round((70 * Math.pow(p.weight, 0.75) / 3.5) / 2);
      else this.recAmount = '';
    },
    save() {
      if (!this.form.planName || !this.form.planTime) return this.$message.warning("信息不完整");
      let url = this.form.id ? '/feedPlan/update' : '/feedPlan/add';
      this.$http.post(url, this.form).then(res => {
        if (res.data) {
          this.$message.success("保存成功");
          this.dialogVisible = false;
          this.initData();
        }
      });
    },
    handleDelete(id) {
      this.$http.get('/feedPlan/delete/' + id).then(res => {
        if (res.data) {
          this.$message.success("删除成功");
          this.initData();
        }
      });
    }
  }
};
</script>

<style scoped>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css');
@import '../assets/css/user-home.css';

.user-feed-layout {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding-top: 80px;
  font-family: "Varela Round", sans-serif;
}

.main-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.feed-dashboard {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 25px;
}

/* ================= 左侧宠物列表 ================= */
.side-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.panel-header {
  margin-bottom: 12px;
  padding: 10px 0 5px 8px;
}

.panel-header span {
  font-size: 26px;
  font-weight: 900;
  color: #2c3e50;
  position: relative;
  padding-left: 18px;
  letter-spacing: 1.5px;
  line-height: 1;
}

.panel-header span::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 7px;
  height: 26px;
  background: linear-gradient(to bottom, #ff9f43, #ff6b6b);
  border-radius: 4px;
  box-shadow: 0 2px 6px rgba(255, 107, 107, 0.4);
}

.pet-selector-list {
  background: white;
  border-radius: 20px;
  padding: 18px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
}

.pet-select-item {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 18px;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  margin-bottom: 12px;
  border: 1px solid transparent;
}

.pet-select-item:hover {
  background: #fffcf5;
  border-color: #ffecd1;
  transform: translateX(3px);
}

.pet-select-item.active {
  background: #fff8e6;
  border-color: #ffe0b2;
  box-shadow: 0 4px 12px rgba(255, 165, 2, 0.1);
}

.pet-select-item.active .name {
  color: #e67e22;
}

.avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.avatar-box {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #fff;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.avatar-box.ring-green {
  box-shadow: 0 0 0 3px #67c23a, 0 4px 10px rgba(0, 0, 0, 0.1);
}

.avatar-box.ring-orange {
  box-shadow: 0 0 0 3px #e6a23c, 0 4px 10px rgba(0, 0, 0, 0.1);
}

.status-badge {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  position: absolute;
  bottom: 3px;
  right: 3px;
  border: 2px solid white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.h-green {
  background: #67c23a;
}

.h-red {
  background: #f56c6c;
  animation: pulse 1s infinite;
}

.h-yellow {
  background: #e6a23c;
}

.h-gray {
  background: #ccc;
}

.info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 6px;
  overflow: hidden;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.name {
  font-size: 19px;
  font-weight: 800;
  color: #333;
  display: block;
  line-height: 1.2;
}

.breed {
  font-size: 14px;
  color: #888;
  font-weight: 500;
}

.active-arrow {
  font-size: 20px;
  color: #ff9f43;
  font-weight: bold;
}

/* 宠物名片 */
.pet-profile-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 22px;
  color: white;
  box-shadow: 0 8px 20px rgba(118, 75, 162, 0.4);
  margin-top: 5px;
}

.card-title {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  padding-bottom: 8px;
  font-weight: bold;
}

.profile-grid {
  display: flex;
  justify-content: space-between;
  margin-bottom: 25px;
}

.p-item {
  text-align: center;
}

.p-item .val {
  font-size: 20px;
  font-weight: bold;
  display: block;
  margin-bottom: 4px;
}

.p-item .val small {
  font-size: 13px;
  font-weight: normal;
  opacity: 0.8;
}

.p-item .lbl {
  font-size: 12px;
  opacity: 0.7;
}

.energy-box {
  background: rgba(0, 0, 0, 0.25);
  padding: 12px;
  border-radius: 10px;
  text-align: center;
}

.eb-label {
  font-size: 12px;
  opacity: 0.9;
  margin-bottom: 6px;
}

.eb-val {
  font-size: 22px;
  font-weight: bold;
  color: #ffda79;
  letter-spacing: 1px;
}

/* ================= 右侧内容 ================= */
.main-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 健康警告 */
.health-alert {
  background: #fef0f0;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 1px solid #fab6b6;
  box-shadow: 0 4px 15px rgba(245, 108, 108, 0.05);
}

.ha-left {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #f56c6c;
  font-size: 15px;
  font-weight: bold;
}

.ha-left i {
  font-size: 20px;
}

/* 1. 统计栏 */
.stats-board {
  background: white;
  border-radius: 16px;
  padding: 20px; /* 统一内边距 */
  display: flex;
  align-items: center;
  gap: 30px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.03);
}

.stats-left {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 20px;
}

.stats-left h3 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.progress-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}

.progress-text {
  display: flex;
  flex-direction: column;
  font-size: 12px;
  color: #999;
}

.progress-text b {
  font-size: 16px;
  color: #333;
}

/* 2. 星期导航 (修复对齐问题) */
.week-nav-rich {
  display: flex;
  background: white;
  padding: 20px; /* 关键修改：统一为20px，与其他模块对齐 */
  border-radius: 16px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.03);
  gap: 10px;
  justify-content: space-between;
}

.day-block {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  border-radius: 10px;
  cursor: pointer;
  transition: 0.2s;
  background: #f8f9fa;
}

.day-block:hover {
  background: #f0f0f0;
}

.day-block.active {
  background: #333;
  color: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  transform: translateY(-2px);
}

.day-block.today {
  color: #ff9f43;
  font-weight: bold;
  border: 1px solid #ffecd1;
}

.day-block.today.active {
  color: #ff9f43;
  border: none;
}

.d-label {
  font-size: 14px;
  margin-bottom: 4px;
}

.d-status i {
  font-size: 12px;
}

.dot-warn {
  display: inline-block;
  width: 6px;
  height: 6px;
  background: #e6a23c;
  border-radius: 50%;
}

.dot-empty {
  display: inline-block;
  width: 4px;
  height: 4px;
  background: #eee;
  border-radius: 50%;
}

/* 3. 计划列表容器 */
.rich-list-wrapper {
  background: white;
  border-radius: 16px;
  padding: 20px; /* 统一内边距 */
  height: 550px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.03);
}

/* 滚动区域 */
.scroll-container {
  flex: 1;
  overflow-y: auto;
  padding-right: 8px;
}

.scroll-container::-webkit-scrollbar {
  width: 6px;
}

.scroll-container::-webkit-scrollbar-thumb {
  background: #e0e0e0;
  border-radius: 3px;
}

.scroll-container::-webkit-scrollbar-thumb:hover {
  background: #ccc;
}

.plans-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.plan-item {
  display: flex;
  align-items: center;
  padding: 18px;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  transition: 0.2s;
  background: white;
}

.plan-item:hover {
  border-color: #d9ecff;
  box-shadow: 0 6px 15px rgba(64, 158, 255, 0.08);
  transform: translateY(-2px);
}

.plan-item.is-done {
  opacity: 0.5;
  background: #f9f9f9;
  border-color: #eee;
  box-shadow: none;
  transform: none;
}

.plan-item.is-done .pi-title {
  text-decoration: line-through;
  color: #aaa;
}

.pi-check {
  margin-right: 20px;
  cursor: pointer;
  font-size: 26px;
  color: #67c23a;
  display: flex;
  align-items: center;
}

.pi-check .circle {
  width: 22px;
  height: 22px;
  border: 2px solid #ddd;
  border-radius: 50%;
  transition: 0.2s;
}

.pi-check:hover .circle {
  border-color: #67c23a;
}

.pi-time {
  width: 70px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  border-right: 1px solid #f0f0f0;
  margin-right: 20px;
}

.big-time {
  font-size: 20px;
  font-weight: 800;
  color: #333;
  font-family: 'Arial';
}

.tag-daily {
  font-size: 11px;
  color: #67c23a;
  background: #f0f9eb;
  padding: 2px 6px;
  border-radius: 4px;
}

.tag-once {
  font-size: 11px;
  color: #e6a23c;
  background: #fdf6ec;
  padding: 2px 6px;
  border-radius: 4px;
}

.pi-content {
  flex: 1;
}

.pi-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.pi-title {
  font-size: 17px;
  font-weight: bold;
  color: #333;
}

.pi-detail {
  font-size: 14px;
  color: #666;
  display: flex;
  gap: 20px;
}

.food-info i {
  color: #ff9f43;
  margin-right: 5px;
}

.pi-action {
  display: flex;
  align-items: center;
  gap: 15px;
  opacity: 0;
  transition: 0.2s;
}

.plan-item:hover .pi-action {
  opacity: 1;
}

.mall-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 5px 12px;
  background: #fff8e1;
  color: #ff9f43;
  border-radius: 20px;
  font-size: 12px;
  cursor: pointer;
  font-weight: bold;
  transition: 0.2s;
}

.mall-btn:hover {
  background: #ff9f43;
  color: white;
}

.action-more {
  cursor: pointer;
  color: #ccc;
  font-size: 20px;
  padding: 5px;
}

.action-more:hover {
  color: #409eff;
}

.empty-rich {
  text-align: center;
  padding: 60px 0;
}

.empty-rich img {
  width: 80px;
  opacity: 0.5;
  margin-bottom: 15px;
}

.empty-rich p {
  color: #ccc;
}

/* 4. 底部推荐 */
.recommend-section {
  margin-top: 5px; /* 间距微调 */
  background: white;
  padding: 20px; /* 统一内边距 */
  border-radius: 16px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.03);
}

.rec-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.rec-title {
  font-weight: bold;
  font-size: 16px;
  color: #333;
}

.rec-more {
  font-size: 12px;
  color: #999;
  cursor: pointer;
}

.rec-list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15px;
}

.rec-card {
  background: #fcfcfc;
  border-radius: 10px;
  padding: 10px;
  cursor: pointer;
  transition: 0.2s;
  position: relative;
  border: 1px solid #f5f5f5;
}

.rec-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
}

.rec-img {
  width: 100%;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 8px;
}

.r-name {
  font-size: 13px;
  color: #333;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}

.r-price {
  font-size: 14px;
  font-weight: bold;
  color: #ff9f43;
}

.rec-add {
  position: absolute;
  bottom: 10px;
  right: 10px;
  background: #ff9f43;
  color: white;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

/* 动画 */
@keyframes breath {
  0% {
    box-shadow: 0 0 0 0 rgba(103, 194, 58, 0.4);
  }
  70% {
    box-shadow: 0 0 0 6px rgba(103, 194, 58, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(103, 194, 58, 0);
  }
}

@keyframes pulse {
  0% {
    transform: scale(0.95);
    opacity: 0.8;
  }
  50% {
    transform: scale(1.05);
    opacity: 1;
  }
  100% {
    transform: scale(0.95);
    opacity: 0.8;
  }
}

.clean-dialog {
  border-radius: 12px !important;
}
</style>
