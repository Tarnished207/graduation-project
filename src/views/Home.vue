<template>
  <div class="page-container">

    <div class="welcome-card">
      <div class="welcome-left">
        <el-avatar :size="64" :src="getImageUrl(user.avatar)" class="avatar">
          <img src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"/>
        </el-avatar>
        <div class="welcome-text">
          <div class="title">早安，{{ user.nickname || '管理员' }}！</div>
          <div class="subtitle">今日系统运行平稳，暂无异常报警。</div>
        </div>
      </div>
      <div class="welcome-right">
        <div class="stat-box">
          <div class="label">待发货</div>
          <div class="val" style="color:#0984e3">{{ orderStats.toShip || 0 }}</div>
        </div>
        <div class="stat-box">
          <div class="label">库存预警</div>
          <div class="val warning">{{ productStats.lowStock || 0 }}</div>
        </div>
        <div class="stat-box">
          <div class="label">待支付</div>
          <div class="val" style="color:#b2bec3">{{ orderStats.pending || 0 }}</div>
        </div>
      </div>
    </div>

    <div class="dashboard-grid">
      <div class="grid-item revenue-card" @click="$router.push('/orders')">
        <div class="card-icon"><i class="el-icon-wallet"></i></div>
        <div class="card-info">
          <div class="card-label">总营收</div>
          <div class="card-num">¥{{ orderStats.revenue || 0 }}</div>
        </div>
        <i class="el-icon-money bg-icon"></i>
      </div>
      <div class="grid-item blue-card" @click="$router.push('/user')">
        <div class="item-top"><span>总用户</span><i class="el-icon-user"></i></div>
        <div class="item-num">{{ userStats.userCount || 0 }}</div>
      </div>
      <div class="grid-item purple-card" @click="$router.push('/pet')">
        <div class="item-top"><span>萌宠档案</span><i class="el-icon-sugar"></i></div>
        <div class="item-num">{{ petStats.total || 0 }}</div>
      </div>
      <div class="grid-item orange-card" @click="$router.push('/product')">
        <div class="item-top"><span>商品SKU</span><i class="el-icon-goods"></i></div>
        <div class="item-num">{{ productStats.total || 0 }}</div>
      </div>
    </div>

    <el-row :gutter="20" style="margin-top: 25px;">

      <el-col :span="15">
        <div class="panel">
          <div class="panel-header">
            <span>📦 最新交易记录</span>
            <el-button type="text" @click="$router.push('/orders')">全部订单</el-button>
          </div>
          <el-table :data="latestOrders" style="width: 100%" :header-cell-style="{background:'#fff', color:'#999'}">
            <el-table-column label="商品" min-width="180">
              <template slot-scope="scope">
                <div style="display:flex; align-items:center; gap:10px">
                  <el-image :src="getImageUrl(scope.row.productImage)"
                            style="width:36px;height:36px;border-radius:4px"></el-image>
                  <span style="font-size:13px">{{ scope.row.productName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="买家" prop="buyerName" width="100"></el-table-column>
            <el-table-column label="金额" width="100">
              <template slot-scope="scope"><span style="font-weight:bold; color:#ff6b6b">¥{{ scope.row.totalAmount }}</span></template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template slot-scope="scope">
                <el-tag v-if="scope.row.status===0" size="mini" type="warning">待支付</el-tag>
                <el-tag v-else-if="scope.row.status===1" size="mini" type="primary">待发货</el-tag>
                <el-tag v-else-if="scope.row.status===2" size="mini" type="success">已发货</el-tag>
                <el-tag v-else-if="scope.row.status===3" size="mini" type="success">已完成</el-tag>
                <el-tag v-else size="mini" type="info">已取消</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>

      <el-col :span="9">
        <div class="panel">
          <div class="panel-header">
            <span>🤖 智能办公助手</span>
            <el-tag size="mini" type="info" effect="plain">{{ currentDate }}</el-tag>
          </div>

          <div class="todo-list">

            <div class="todo-item urgent" v-if="orderStats.toShip > 0">
              <div class="todo-icon blue"><i class="el-icon-box"></i></div>
              <div class="todo-content">
                <div class="t-title">待发货订单</div>
                <div class="t-desc">有 {{ orderStats.toShip }} 位买家正在等待发货</div>
              </div>
              <el-button size="mini" type="primary" plain round @click="$router.push('/orders')">去发货</el-button>
            </div>

            <div class="todo-item warning" v-if="productStats.lowStock > 0">
              <div class="todo-icon red"><i class="el-icon-warning-outline"></i></div>
              <div class="todo-content">
                <div class="t-title">库存告急</div>
                <div class="t-desc">{{ productStats.lowStock }} 个商品库存不足 10 件</div>
              </div>
              <el-button size="mini" type="danger" plain round @click="$router.push('/product')">去补货</el-button>
            </div>

            <div class="todo-item normal" v-if="orderStats.pending > 0">
              <div class="todo-icon orange"><i class="el-icon-time"></i></div>
              <div class="todo-content">
                <div class="t-title">待支付订单</div>
                <div class="t-desc">{{ orderStats.pending }} 笔订单尚未付款，请留意</div>
              </div>
              <el-button size="mini" round @click="$router.push('/orders')">查看</el-button>
            </div>

            <div class="todo-item normal">
              <div class="todo-icon green"><i class="el-icon-plus"></i></div>
              <div class="todo-content">
                <div class="t-title">快捷上新</div>
                <div class="t-desc">快速发布新的宠物商品</div>
              </div>
              <el-button size="mini" round @click="$router.push('/product')">去发布</el-button>
            </div>

            <div class="todo-item normal">
              <div class="todo-icon purple"><i class="el-icon-user-solid"></i></div>
              <div class="todo-content">
                <div class="t-title">用户管理</div>
                <div class="t-desc">当前共有 {{ userStats.userCount || 0 }} 位注册用户</div>
              </div>
              <el-button size="mini" round @click="$router.push('/user')">管理</el-button>
            </div>

          </div>
        </div>
      </el-col>
    </el-row>

  </div>
</template>

<script>
export default {
  data() {
    return {
      // 修复点：从 sessionStorage 读取数据
      user: sessionStorage.getItem("user") ? JSON.parse(sessionStorage.getItem("user")) : {},
      productStats: {},
      userStats: {},
      petStats: {},
      orderStats: {},
      latestOrders: [],
      currentDate: new Date().toLocaleDateString()
    }
  },
  created() {
    this.loadAllStats();
  },
  methods: {
    // 修复点：新增图片路径处理方法
    getImageUrl(url) {
      if (!url) return 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png';
      if (url.startsWith('http')) return url;
      if (url.startsWith('/')) return 'http://localhost:9090' + url;
      return 'http://localhost:9090/images/' + url;
    },
    loadAllStats() {
      // 1. 获取各模块统计
      this.$http.get('/product/stats').then(res => { if(res.data) this.productStats = res.data; }).catch(()=>{});
      this.$http.get('/sysUser/stats').then(res => { if(res.data) this.userStats = res.data; }).catch(()=>{});
      this.$http.get('/petInfo/stats').then(res => { if(res.data) this.petStats = res.data; }).catch(()=>{});
      this.$http.get('/orders/stats').then(res => { if(res.data) this.orderStats = res.data; }).catch(()=>{});

      // 2. 获取最新订单
      this.$http.get('/orders/list').then(res => {
        if (res.data && res.data.length > 0) {
          this.latestOrders = res.data.slice(0, 5);
        }
      }).catch(()=>{});
    }
  }
}
</script>

<style scoped>
.page-container { padding: 25px; background-color: #f7f8fa; min-height: 100vh; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif; }

/* 欢迎卡片 */
.welcome-card { background: #fff; border-radius: 16px; padding: 30px; display: flex; justify-content: space-between; align-items: center; margin-bottom: 25px; box-shadow: 0 4px 12px rgba(0,0,0,0.03); }
.welcome-left { display: flex; align-items: center; gap: 20px; }
.welcome-text .title { font-size: 20px; font-weight: bold; color: #333; margin-bottom: 8px; }
.welcome-text .subtitle { color: #999; font-size: 14px; }
.welcome-right { display: flex; gap: 40px; text-align: right; }
.stat-box .label { font-size: 12px; color: #999; margin-bottom: 5px; }
.stat-box .val { font-size: 24px; font-weight: bold; color: #333; }
.stat-box .val.warning { color: #ff6b6b; }

/* Dashboard Grid */
.dashboard-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; height: 160px; }
.grid-item { border-radius: 20px; padding: 25px; color: #fff; position: relative; overflow: hidden; cursor: pointer; transition: transform 0.2s; box-shadow: 0 8px 20px rgba(0,0,0,0.05); display: flex; flex-direction: column; justify-content: space-between; }
.grid-item:hover { transform: translateY(-3px); }

/* 配色 */
.revenue-card { background: linear-gradient(135deg, #00b894 0%, #00cec9 100%); box-shadow: 0 8px 20px rgba(0, 184, 148, 0.3); }
.revenue-card .card-icon { width: 40px; height: 40px; background: rgba(255,255,255,0.2); border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 20px; margin-bottom: 5px; }
.revenue-card .card-label { font-size: 14px; opacity: 0.9; }
.revenue-card .card-num { font-size: 32px; font-weight: 800; margin-top: 5px; }
.bg-icon { position: absolute; right: -20px; bottom: -30px; font-size: 120px; opacity: 0.15; transform: rotate(-15deg); }

.blue-card { background: #fff; color: #333; border: 1px solid #f0f0f0; } .blue-card .item-num { color: #0984e3; } .blue-card i { color: #0984e3; background: #e2f0ff; padding: 8px; border-radius: 10px; }
.purple-card { background: #fff; color: #333; border: 1px solid #f0f0f0; } .purple-card .item-num { color: #6c5ce7; } .purple-card i { color: #6c5ce7; background: #eeeaf9; padding: 8px; border-radius: 10px; }
.orange-card { background: #fff; color: #333; border: 1px solid #f0f0f0; } .orange-card .item-num { color: #ff9f43; } .orange-card i { color: #ff9f43; background: #fff4e6; padding: 8px; border-radius: 10px; }
.item-top { display: flex; justify-content: space-between; align-items: center; font-size: 14px; color: #888; }
.item-num { font-size: 32px; font-weight: bold; margin-top: 20px; }

/* 下方两栏 */
.panel { background: #fff; border-radius: 16px; padding: 20px; min-height: 400px; box-shadow: 0 4px 12px rgba(0,0,0,0.03); }
.panel-header { display: flex; justify-content: space-between; align-items: center; font-weight: bold; color: #333; margin-bottom: 20px; border-bottom: 1px solid #f5f5f5; padding-bottom: 15px; }

/* 待办列表 */
.todo-list { display: flex; flex-direction: column; gap: 15px; }
.todo-item { display: flex; align-items: center; justify-content: space-between; padding: 15px; background: #fff; border: 1px solid #f0f0f0; border-radius: 12px; transition: 0.2s; }
.todo-item:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.05); transform: translateX(5px); }

/* 图标样式 */
.todo-icon { width: 40px; height: 40px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 20px; flex-shrink: 0; }
.todo-icon.blue { background: #e2f0ff; color: #0984e3; }
.todo-icon.red { background: #ffe2e2; color: #ff6b6b; }
.todo-icon.orange { background: #fff7e6; color: #ffa940; }
.todo-icon.green { background: #e6fffa; color: #00b894; }
.todo-icon.purple { background: #f3e8ff; color: #7c3aed; }

.todo-content { flex: 1; margin-left: 15px; }
.t-title { font-weight: 600; color: #333; font-size: 14px; margin-bottom: 4px; }
.t-desc { color: #999; font-size: 12px; }
</style>
