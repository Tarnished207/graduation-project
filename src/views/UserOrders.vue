<template>
  <div class="user-orders-layout">

    <div class="main-container">

      <div class="stats-overview fade-in-up">

        <div class="stat-card primary-card">
          <div class="card-content">
            <div class="icon-box">
              <i class="el-icon-trophy"></i>
            </div>
            <div class="info">
              <div class="label">累计消费 (元)</div>
              <div class="number">¥{{ myStats.totalSpent || 0 }}</div>
            </div>
          </div>
          <i class="el-icon-money bg-icon"></i>
        </div>

        <div class="stat-card">
          <div class="label">剁手记录</div>
          <div class="value">{{ myStats.orderCount || 0 }} <span class="unit">单</span></div>
          <div class="icon-corner blue"><i class="el-icon-tickets"></i></div>
        </div>

        <div class="stat-card clickable" @click="quickFilter('0')">
          <div class="label">待支付</div>
          <div class="value danger-text">{{ myStats.pendingPay || 0 }}</div>
          <div class="icon-corner red"><i class="el-icon-wallet"></i></div>
        </div>

        <div class="stat-card clickable" @click="quickFilter('2')">
          <div class="label">待收货</div>
          <div class="value warning-text">{{ myStats.processing || 0 }}</div>
          <div class="icon-corner orange"><i class="el-icon-box"></i></div>
        </div>
      </div>

      <div class="orders-container fade-in-up delay-100">

        <el-tabs v-model="activeStatus" @tab-click="handleTabClick" class="custom-tabs">
          <el-tab-pane label="全部订单" name="all"></el-tab-pane>
          <el-tab-pane label="待支付" name="0"></el-tab-pane>
          <el-tab-pane label="待发货" name="1"></el-tab-pane>
          <el-tab-pane label="已发货" name="2"></el-tab-pane>
          <el-tab-pane label="已完成" name="3"></el-tab-pane>
          <el-tab-pane label="售后/取消" name="-1"></el-tab-pane>
        </el-tabs>

        <div class="order-list" v-loading="loading">
          <el-empty v-if="orders.length === 0" description="暂无相关订单"></el-empty>

          <div class="order-item" v-for="item in orders" :key="item.id">
            <div class="order-header">
              <div class="header-left">
                <span class="order-no">订单号：{{ item.orderNo }}</span>
                <span class="order-time">{{ item.createTime | fmtDate }}</span>
              </div>
              <div class="order-status" :class="'status-' + item.status">
                {{ getStatusText(item.status) }}
              </div>
            </div>

            <div class="order-body">
              <div class="product-info" @click="viewProduct(item)">
                <el-image :src="getImageUrl(item.productImage)" class="p-img" fit="cover">
                  <div slot="error" class="image-slot"><i class="el-icon-picture-outline"></i></div>
                </el-image>
                <div class="p-detail">
                  <div class="p-name">{{ item.productName }}</div>
                  <div class="p-desc">{{ item.productDescription || '暂无详细描述' }}</div>
                </div>
              </div>
              <div class="price-info">
                <div class="p-price">单价: ¥{{ (item.totalAmount / item.quantity).toFixed(2) }}</div>
                <div class="p-qty">x {{ item.quantity }}</div>
              </div>
              <div class="total-info">
                <span class="label">实付金额</span>
                <span class="amount">¥{{ item.totalAmount }}</span>
              </div>
            </div>

            <div class="order-footer">
              <div class="address-text">
                <i class="el-icon-location-outline"></i> {{ item.address || '无收货地址' }}
              </div>
              <div class="action-btns">
                <template v-if="item.status === 0">
                  <el-button size="small" type="text" @click="cancelOrder(item)">取消订单</el-button>
                  <el-button size="small" type="danger" round @click="payOrder(item)">立即支付</el-button>
                </template>

                <template v-if="item.status === 1">
                  <el-button size="small" plain disabled>等待商家发货</el-button>
                </template>

                <template v-if="item.status === 2">
                  <el-button size="small" type="success" plain @click="confirmReceive(item)">确认收货</el-button>
                </template>

                <template v-if="item.status === 3 || item.status === -1">
                  <el-button size="small" type="info" icon="el-icon-delete" circle
                             @click="deleteOrder(item.id)"></el-button>
                </template>
              </div>
            </div>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "UserOrders",
  data() {
    return {
      user: JSON.parse(sessionStorage.getItem("user") || '{}'),
      orders: [],
      // 默认从路由参数读取状态，没有则默认 'all'
      activeStatus: this.$route.query.status ? String(this.$route.query.status) : 'all',
      loading: false,
      // 统计数据
      myStats: {totalSpent: 0, orderCount: 0, pendingPay: 0, processing: 0}
    }
  },
  filters: {
    fmtDate(val) {
      if (!val) return ''
      return val.replace('T', ' ')
    }
  },
  watch: {
    // 监听路由参数变化，实现浏览器后退/前进时 Tab 同步切换
    '$route.query.status': function (newVal) {
      this.activeStatus = newVal ? String(newVal) : 'all';
      this.loadOrders();
    }
  },
  created() {
    if (!this.user.id) {
      this.$message.warning("请先登录");
      this.$router.push("/login");
      return;
    }
    this.loadOrders();
    this.loadMyStats();
  },
  methods: {
    // 1. 加载订单列表
    loadOrders() {
      this.loading = true;
      let statusParam = null;
      if (this.activeStatus !== 'all') {
        statusParam = parseInt(this.activeStatus);
      }

      this.$http.get("/orders/list", {
        params: {
          userId: this.user.id,
          status: statusParam
        }
      }).then(res => {
        this.orders = res.data;
      }).catch(() => {
        this.$message.error("加载订单失败");
      }).finally(() => {
        this.loading = false;
      });
    },

    // 2. 加载统计数据
    loadMyStats() {
      this.$http.get("/orders/my-stats/" + this.user.id).then(res => {
        if (res.data) {
          this.myStats = res.data;
        }
      });
    },

    // Tab 点击事件：同步修改 URL
    handleTabClick(tab) {
      // 修改路由参数，但不刷新页面 (replace)
      if (this.activeStatus !== this.$route.query.status) {
        this.$router.replace({
          path: '/front/orders',
          query: {status: this.activeStatus === 'all' ? undefined : this.activeStatus}
        }).catch(err => {
        }); // 捕获重复导航错误
      }
      this.loadOrders();
    },

    // 点击统计卡片快速筛选
    quickFilter(status) {
      this.activeStatus = status;
      this.handleTabClick();
    },

    // 支付逻辑
    payOrder(item) {
      this.$confirm(`需支付 ¥${item.totalAmount}，确认支付吗？`, '支付确认', {
        confirmButtonText: '确定支付',
        cancelButtonText: '取消',
        type: 'success'
      }).then(() => {
        item.status = 1; // 变为待发货
        this.$http.post("/orders/update", item).then(res => {
          if (res.data) {
            this.$message.success("支付成功！等待发货");
            this.loadOrders();
            this.loadMyStats(); // 刷新统计
          }
        })
      }).catch(() => {
      });
    },

    // 取消订单
    cancelOrder(item) {
      this.$confirm('确定要取消该订单吗？', '提示', {type: 'warning'}).then(() => {
        item.status = -1; // 已取消
        this.$http.post("/orders/update", item).then(res => {
          if (res.data) {
            this.$message.info("订单已取消");
            this.loadOrders();
            this.loadMyStats();
          }
        })
      }).catch(() => {
      });
    },

    // 确认收货
    confirmReceive(item) {
      this.$confirm('确认已收到商品且无误？', '收货确认', {type: 'success'}).then(() => {
        item.status = 3; // 已完成
        this.$http.post("/orders/update", item).then(res => {
          if (res.data) {
            this.$message.success("交易完成！");
            this.loadOrders();
            this.loadMyStats();
          }
        })
      }).catch(() => {
      });
    },

    // 删除订单
    deleteOrder(id) {
      this.$confirm('删除后无法恢复，确定吗？', '删除确认', {type: 'warning'}).then(() => {
        this.$http.get("/orders/delete/" + id).then(res => {
          if (res.data) {
            this.$message.success("删除成功");
            this.loadOrders();
            this.loadMyStats();
          }
        })
      }).catch(() => {
      });
    },

    // 辅助方法
    viewProduct(item) {
      if (item.productId) {
        this.$router.push({
          path: '/front/product/' + item.productId,
          query: {from: 'order'}
        });
      }
    },
    getImageUrl(url) {
      if (!url) return 'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png';
      if (url.startsWith('http')) return url;
      return 'http://localhost:9090/images/' + url;
    },
    getStatusText(status) {
      const map = {0: '待支付', 1: '待发货', 2: '已发货', 3: '已完成', '-1': '已取消'}
      return map[status] || '未知';
    }
  }
}
</script>

<style scoped>
/* 引入公共样式 */
@import '../assets/css/user-home.css';

.user-orders-layout {
  background-color: #f7f9fc;
  min-height: 100vh;
  padding-top: 80px;
}

.main-container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px;
}

/* --- 统计看板样式 --- */
.stats-overview {
  display: grid;
  grid-template-columns: 1.5fr 1fr 1fr 1fr;
  gap: 20px;
  margin-bottom: 25px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
  display: flex;
  flex-direction: column;
  justify-content: center;
  height: 130px; /* 稍微增高一点，容纳大字体 */
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card.clickable {
  cursor: pointer;
}

.stat-card.clickable:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.06);
}

/* 主卡片（累计消费） */
.primary-card {
  background: linear-gradient(135deg, #ff9f43 0%, #ffc048 100%);
  color: white;
  border: none;
}

.primary-card .card-content {
  display: flex;
  align-items: center;
  gap: 20px;
  position: relative;
  z-index: 2;
}

.primary-card .icon-box {
  width: 60px;
  height: 60px; /* 图标圈也变大 */
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.primary-card .info .label {
  font-size: 15px;
  opacity: 0.95;
  margin-bottom: 6px;
  font-weight: 500;
}

/* [修改] 累计消费数字加大 */
.primary-card .info .number {
  font-size: 40px;
  font-weight: 800;
  font-family: 'DIN', sans-serif;
  line-height: 1;
}

.bg-icon {
  position: absolute;
  right: -10px;
  bottom: -20px;
  font-size: 110px;
  color: #fff;
  opacity: 0.15;
  transform: rotate(-15deg);
  z-index: 1;
}

/* 小卡片通用样式 */
/* [修改] 标题文字加大 */
.stat-card .label {
  font-size: 15px;
  color: #888;
  margin-bottom: 10px;
  font-weight: 500;
}

/* [修改] 普通数字加大 */
.stat-card .value {
  font-size: 34px;
  font-weight: 800;
  color: #333;
  font-family: 'DIN', sans-serif;
  line-height: 1;
}

.stat-card .unit {
  font-size: 14px;
  color: #999;
  font-weight: normal;
  margin-left: 4px;
}

.danger-text {
  color: #ff6b6b !important;
}

.warning-text {
  color: #ff9f43 !important;
}

/* 右上角图标装饰 */
.icon-corner {
  position: absolute;
  top: 15px;
  right: 15px;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.icon-corner.blue {
  background: #e3f2fd;
  color: #2196f3;
}

.icon-corner.red {
  background: #ffebee;
  color: #f44336;
}

.icon-corner.orange {
  background: #fff3e0;
  color: #ff9800;
}

/* --- 订单列表样式 (保持不变) --- */
.orders-container {
  background: white;
  border-radius: 16px;
  padding: 20px;
  min-height: 500px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
}

::v-deep .el-tabs__item {
  font-size: 16px;
  height: 55px;
  line-height: 55px;
}

/* Tab文字也稍微调大 */
::v-deep .el-tabs__item.is-active {
  color: #ff9f43;
  font-weight: bold;
}

::v-deep .el-tabs__active-bar {
  background-color: #ff9f43;
}

.order-list {
  margin-top: 20px;
}

.order-item {
  border: 1px solid #eee;
  border-radius: 12px;
  margin-bottom: 20px;
  transition: all 0.2s;
  background: #fff;
}

.order-item:hover {
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  border-color: #ffcc80;
}

.order-header {
  background: #fafafa;
  padding: 15px 25px;
  border-bottom: 1px solid #eee;
  border-radius: 12px 12px 0 0;
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.order-no {
  font-weight: bold;
  color: #333;
  margin-right: 15px;
}

.order-time {
  color: #999;
}

.order-status {
  font-weight: bold;
  font-size: 15px;
}

/* 状态文字加大 */
.status-0 {
  color: #e67e22;
}

.status-1 {
  color: #3498db;
}

.status-2 {
  color: #2ecc71;
}

.status-3 {
  color: #27ae60;
}

.status--1 {
  color: #95a5a6;
}

.order-body {
  padding: 25px;
  display: flex;
  align-items: center;
}

.product-info {
  flex: 1;
  display: flex;
  gap: 20px;
  cursor: pointer;
}

.p-img {
  width: 90px;
  height: 90px;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}

.p-name {
  font-weight: 600;
  font-size: 16px;
  color: #333;
  margin-bottom: 8px;
}

.p-desc {
  font-size: 13px;
  color: #999;
}

.price-info {
  width: 150px;
  text-align: right;
  color: #666;
  font-size: 15px;
}

.p-qty {
  color: #999;
  margin-top: 4px;
  font-size: 13px;
}

.total-info {
  width: 150px;
  text-align: right;
  padding-left: 20px;
  border-left: 1px solid #f5f5f5;
}

.total-info .label {
  display: block;
  font-size: 13px;
  color: #999;
  margin-bottom: 4px;
}

.total-info .amount {
  font-size: 22px;
  color: #e74c3c;
  font-weight: bold;
  font-family: 'DIN', sans-serif;
}

.order-footer {
  padding: 15px 25px;
  border-top: 1px dashed #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.address-text {
  font-size: 13px;
  color: #777;
  max-width: 50%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.action-btns {
  display: flex;
  gap: 10px;
}

/* 动画 */
.fade-in-up {
  animation: fadeInUp 0.6s ease backwards;
}

.delay-100 {
  animation-delay: 0.1s;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .stats-overview {
    grid-template-columns: 1fr 1fr;
  }

  .primary-card {
    grid-column: span 2;
  }

  .order-body {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .price-info, .total-info {
    width: 100%;
    text-align: left;
    padding: 0;
    border: none;
    display: flex;
    justify-content: space-between;
  }

  .order-footer {
    flex-direction: column;
    gap: 10px;
    align-items: flex-end;
  }

  .address-text {
    width: 100%;
    max-width: 100%;
  }
}
</style>
