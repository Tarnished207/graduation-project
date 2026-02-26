<template>
  <div class="page-container">

    <div class="stats-container">
      <div class="stat-card main-card">
        <div class="main-card-content">
          <div class="icon-circle"><i class="el-icon-s-order"></i></div>
          <div class="text-group">
            <div class="label">全平台订单</div>
            <div class="number">{{ stats.total || 0 }}</div>
            <div class="tag"><i class="el-icon-refresh"></i> 实时监控中</div>
          </div>
        </div>
        <i class="el-icon-tickets bg-watermark"></i>
      </div>

      <div class="small-cards-group">
        <div class="stat-card small-card">
          <div class="card-header"><span>待发货</span><i class="el-icon-box icon-grey"></i></div>
          <div class="card-body"><div class="card-value blue-text">{{ stats.toShip || 0 }}</div></div>
          <div class="card-footer"><span class="sub-tag blue" v-if="stats.toShip > 0">尽快处理</span></div>
        </div>
        <div class="stat-card small-card">
          <div class="card-header"><span>总营收</span><i class="el-icon-coin icon-grey"></i></div>
          <div class="card-body"><div class="card-value green-text"><span style="font-size:18px">¥</span>{{ stats.revenue || 0 }}</div></div>
          <div class="card-footer"></div>
        </div>
        <div class="stat-card small-card">
          <div class="card-header"><span>待支付</span><i class="el-icon-time icon-grey"></i></div>
          <div class="card-body"><div class="card-value grey-text">{{ stats.pending || 0 }}</div></div>
          <div class="card-footer"></div>
        </div>
      </div>
    </div>

    <div class="content-panel">
      <div class="toolbar">
        <div class="left-tools">
          <el-input v-model="searchOrderNo" placeholder="🔍 搜订单号..." class="search-input" @keyup.enter.native="load"></el-input>
          <el-select v-model="searchStatus" placeholder="订单状态" class="filter-select" @change="load" clearable>
            <el-option label="⏳ 待支付" :value="0"></el-option>
            <el-option label="📦 待发货" :value="1"></el-option>
            <el-option label="🚚 已发货" :value="2"></el-option>
            <el-option label="✅ 已完成" :value="3"></el-option>
            <el-option label="❌ 已取消" :value="-1"></el-option>
          </el-select>
          <el-button circle icon="el-icon-search" @click="load"></el-button>
          <el-button circle icon="el-icon-refresh" @click="resetSearch"></el-button>
        </div>
      </div>

      <el-table
          :data="tableData"
          style="width: 100%"
          class="custom-table"
          :header-cell-style="{background:'#fff', color:'#909399', borderBottom:'1px solid #f0f0f0', fontWeight:'500'}"
      >
        <el-table-column label="商品信息" min-width="260">
          <template slot-scope="scope">
            <div class="order-meta">
              <el-image :src="scope.row.productImage" class="product-thumb" fit="cover">
                <div slot="error" class="image-slot"><i class="el-icon-picture-outline"></i></div>
              </el-image>
              <div class="meta-text">
                <div class="p-name">{{ scope.row.productName }}</div>
                <div class="order-no">NO. {{ scope.row.orderNo }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="买家" min-width="160">
          <template slot-scope="scope">
            <div class="buyer-info">
              <span class="b-name">{{ scope.row.buyerName }}</span>
              <span class="b-phone">{{ scope.row.buyerPhone }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="totalAmount" label="总价" min-width="120">
          <template slot-scope="scope">
            <span class="price-font">¥{{ scope.row.totalAmount }}</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.status === 0" type="warning" size="mini" effect="dark">待支付</el-tag>
            <el-tag v-else-if="scope.row.status === 1" type="primary" size="mini" effect="dark">待发货</el-tag>
            <el-tag v-else-if="scope.row.status === 2" type="success" size="mini" effect="plain">已发货</el-tag>
            <el-tag v-else-if="scope.row.status === 3" type="success" size="mini" effect="dark">已完成</el-tag>
            <el-tag v-else type="info" size="mini" effect="plain">已取消</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" align="right">
          <template slot-scope="scope">
            <el-button type="text" @click="openDetail(scope.row)">详情/设置</el-button>
            <el-popconfirm v-if="user.role === 'ADMIN'" title="确定删除？" @confirm="handleDelete(scope.row.id)"
                           style="margin-left: 10px">
              <el-button slot="reference" type="text" style="color:#ff6b6b">删除</el-button>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog title="订单详情" :visible.sync="detailVisible" width="650px" custom-class="detail-dialog">
      <div class="detail-content">

        <el-steps :active="currentStep" finish-status="success" simple style="margin-bottom: 20px;">
          <el-step title="下单" icon="el-icon-shopping-cart-full"></el-step>
          <el-step title="支付" icon="el-icon-wallet"></el-step>
          <el-step title="发货" icon="el-icon-truck"></el-step>
          <el-step title="完成" icon="el-icon-circle-check"></el-step>
        </el-steps>

        <el-descriptions title="基础信息" :column="2" border size="small">
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ currentOrder.createTime | fmtDate }}</el-descriptions-item>
          <el-descriptions-item label="买家昵称">{{ currentOrder.buyerName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentOrder.buyerPhone || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="商品名称">{{ currentOrder.productName }}</el-descriptions-item>
          <el-descriptions-item label="购买数量">{{ currentOrder.quantity }} 件</el-descriptions-item>
          <el-descriptions-item label="实付金额">
            <span style="color:#f56c6c; font-weight:bold; font-size:16px">¥{{ currentOrder.totalAmount }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <div style="margin-top: 20px;">
          <div style="font-weight:bold; margin-bottom:10px; font-size:14px">📍 收货地址</div>
          <el-input type="textarea" v-model="currentOrder.address" rows="2"></el-input>
          <div style="font-size:12px; color:#999; margin-top:5px">
            * {{ user.role === 'ADMIN' ? '管理员' : '客服' }}可修改地址并保存
          </div>
        </div>

        <div class="admin-panel" v-if="user.role === 'ADMIN'"
             style="margin-top: 20px; background:#f9f9f9; padding:15px; border-radius:8px;">
          <div style="font-weight:bold; margin-bottom:10px; color:#333">⚙️ 状态干预 (管理员权限)</div>
          <el-radio-group v-model="currentOrder.status" size="small">
            <el-radio-button :label="0">待支付</el-radio-button>
            <el-radio-button :label="1">待发货</el-radio-button>
            <el-radio-button :label="2">已发货</el-radio-button>
            <el-radio-button :label="3">已完成</el-radio-button>
            <el-radio-button :label="-1">已取消</el-radio-button>
          </el-radio-group>
          <div style="font-size:12px; color:#e6a23c; margin-top:5px">
            <i class="el-icon-warning"></i> 警告：强制修改状态可能导致业务流程错乱，请谨慎操作。
          </div>
        </div>

      </div>
      <span slot="footer">
        <el-button @click="detailVisible = false">关 闭</el-button>
        <el-button type="primary" @click="saveOrder">保存修改</el-button>
      </span>
    </el-dialog>

  </div>
</template>

<script>
export default {
  data() {
    return {
      tableData: [],
      stats: { total: 0, toShip: 0, revenue: 0, pending: 0 },
      searchOrderNo: '',
      searchStatus: '',

      // 详情弹窗相关
      detailVisible: false,
      currentOrder: {},
      currentStep: 0,
      user: {}
    }
  },
  filters: {
    fmtDate(val) {
      if(!val) return ''
      return val.replace('T', ' ')
    }
  },
  created() {
    this.user = JSON.parse(sessionStorage.getItem("user") || "{}");
    this.load();
    this.loadStats();
  },
  methods: {
    loadStats() { this.$http.get('/orders/stats').then(res => { this.stats = res.data; }); },
    load() {
      this.$http.get('/orders/list', {
        params: { orderNo: this.searchOrderNo, status: this.searchStatus === '' ? null : this.searchStatus }
      }).then(res => { this.tableData = res.data; });
    },
    resetSearch() { this.searchOrderNo = ''; this.searchStatus = ''; this.load(); },

    // 打开详情
    openDetail(row) {
      this.currentOrder = JSON.parse(JSON.stringify(row)); // 深拷贝

      // 计算步骤条位置
      if (this.currentOrder.status === 0) this.currentStep = 1;
      else if (this.currentOrder.status === 1) this.currentStep = 2;
      else if (this.currentOrder.status === 2) this.currentStep = 3;
      else if (this.currentOrder.status === 3) this.currentStep = 4;
      else this.currentStep = 0; // 取消

      this.detailVisible = true;
    },

    // 保存修改 (地址 or 状态)
    saveOrder() {
      this.$http.post('/orders/update', this.currentOrder).then(res => {
        if(res.data) {
          this.$message.success("订单信息已更新");
          this.detailVisible = false;
          this.load();
          this.loadStats();
        } else {
          this.$message.error("更新失败");
        }
      })
    },

    handleDelete(id) {
      this.$http.get('/orders/delete/' + id).then(res => {
        if(res.data) { this.$message.success("删除成功"); this.load(); this.loadStats(); }
      })
    }
  }
}
</script>

<style scoped>
/* 保持统一的 Bento 风格 */
.page-container { padding: 25px; background-color: #f7f8fa; min-height: 100vh; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif; }
.stats-container { display: flex; gap: 20px; margin-bottom: 25px; height: 140px; }
.main-card { flex: 0 0 35%; background: linear-gradient(135deg, #00b894 0%, #00cec9 100%); border-radius: 16px; color: #fff; position: relative; overflow: hidden; box-shadow: 0 8px 20px rgba(0, 184, 148, 0.25); display: flex; align-items: center; padding: 0 30px; }
.main-card-content { z-index: 2; display: flex; align-items: center; gap: 20px; width: 100%; }
.icon-circle { width: 64px; height: 64px; background: rgba(255, 255, 255, 0.25); border-radius: 50%; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.icon-circle i { font-size: 32px; color: #fff; }
.text-group .label { font-size: 15px; opacity: 0.9; margin-bottom: 5px; }
.text-group .number { font-size: 42px; font-weight: 800; line-height: 1; margin-bottom: 8px; }
.text-group .tag { display: inline-block; background: rgba(255, 255, 255, 0.2); padding: 4px 12px; border-radius: 20px; font-size: 13px; font-weight: 500; }
.bg-watermark { position: absolute; right: -20px; bottom: -30px; font-size: 180px; color: #fff; opacity: 0.15; transform: rotate(-15deg); z-index: 1; }
.small-cards-group { flex: 1; display: flex; gap: 20px; }
.small-card { flex: 1; background: #fff; border-radius: 16px; padding: 20px 24px; display: flex; flex-direction: column; justify-content: space-between; box-shadow: 0 4px 12px rgba(0,0,0,0.03); border: 1px solid #f0f0f0; min-width: 0; transition: transform 0.2s; }
.small-card:hover { transform: translateY(-3px); }
.card-header { display: flex; justify-content: space-between; color: #909399; font-size: 14px; align-items: center; }
.icon-grey { font-size: 18px; color: #ccc; }
.card-body { margin-top: auto; margin-bottom: 10px; }
.card-value { font-size: 32px; font-weight: 700; color: #303133; line-height: 1; }
.card-footer { height: 24px; display: flex; align-items: center; }
.blue-text { color: #0984e3; } .green-text { color: #00b894; } .grey-text { color: #b2bec3; }
.sub-tag.blue { background: #0984e3; color: #fff; padding: 3px 8px; border-radius: 4px; font-size: 12px; }

/* 内容区 */
.content-panel { background: #fff; border-radius: 16px; padding: 20px; box-shadow: 0 4px 12px rgba(0,0,0,0.03); }
.toolbar { display: flex; justify-content: space-between; margin-bottom: 20px; }
.left-tools { display: flex; gap: 12px; }
.search-input { width: 220px; }
::v-deep .el-input__inner { border-radius: 20px; background: #f9f9f9; border: 1px solid #eee; }
::v-deep .el-input__inner:focus { background: #fff; border-color: #00b894; }
.custom-table { border-radius: 8px; }
.order-meta { display: flex; align-items: center; gap: 12px; }
.product-thumb { width: 48px; height: 48px; border-radius: 8px; background: #f5f5f5; border: 1px solid #eee; flex-shrink: 0; }
.p-name { font-weight: 600; color: #333; font-size: 14px; margin-bottom: 4px; }
.order-no { font-size: 12px; color: #999; font-family: monospace; }
.price-font { font-family: 'DIN', sans-serif; font-weight: 700; color: #2d3436; font-size: 16px; }
.buyer-info { display: flex; flex-direction: column; font-size: 13px; }
.b-name { color: #333; font-weight: 500; }
.b-phone { color: #999; font-size: 12px; }

/* 弹窗样式 */
::v-deep .detail-dialog { border-radius: 12px; }
</style>