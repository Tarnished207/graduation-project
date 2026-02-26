<template>
  <div class="page-container">

    <div class="top-cards">
      <div class="card blue-card clickable" @click="$router.push('/user')">
        <div class="card-content"><div class="num">{{ stats.userCount || 0 }}</div><div class="txt">总用户数</div></div>
        <i class="el-icon-user bg-icon"></i>
      </div>
      <div class="card green-card clickable" @click="$router.push('/orders')">
        <div class="card-content"><div class="num">{{ stats.orderCount || 0 }}</div><div class="txt">订单总量</div></div>
        <i class="el-icon-tickets bg-icon"></i>
      </div>
      <div class="card orange-card clickable" @click="$router.push('/orders')">
        <div class="card-content"><div class="num">¥{{ stats.revenue || 0 }}</div><div class="txt">平台总交易额</div></div>
        <i class="el-icon-money bg-icon"></i>
      </div>
      <div class="card purple-card clickable" @click="$router.push('/pet')">
        <div class="card-content"><div class="num">{{ stats.petCount || 0 }}</div><div class="txt">宠物档案数</div></div>
        <i class="el-icon-sugar bg-icon"></i>
      </div>
    </div>

    <div class="section-header">
      <i class="el-icon-s-data" style="color:#0984e3"></i> 运营数据监控
      <span class="sub-title">Business Data Monitoring</span>
    </div>

    <el-row :gutter="20">
      <el-col :span="12">
        <div class="chart-box">
          <div class="chart-header">
            <div class="chart-title">📈 营收走势</div>
            <el-date-picker
                v-model="revenueRange"
                type="daterange"
                size="mini"
                value-format="yyyy-MM-dd"
                @change="initRevenueChart"
                style="width: 220px">
            </el-date-picker>
          </div>
          <div id="revenueChart" style="width: 100%; height: 300px;"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="chart-box">
          <div class="chart-header">
            <div class="chart-title">🏆 热门商品销量排行</div>
            <el-date-picker
                v-model="salesRange"
                type="daterange"
                size="mini"
                value-format="yyyy-MM-dd"
                @change="initSalesChart"
                style="width: 220px">
            </el-date-picker>
          </div>
          <div id="salesChart" style="width: 100%; height: 300px;"></div>
        </div>
      </el-col>
    </el-row>

    <div class="section-header" style="margin-top: 30px;">
      <i class="el-icon-monitor" style="color:#6c5ce7"></i> 平台运营深度分析
      <span class="sub-title">Platform Operations Analysis</span>
    </div>

    <el-row :gutter="20">
      <el-col :span="12">
        <div class="chart-box">
          <div class="chart-header">
            <div class="chart-title">🚀 平台增长与活跃趋势</div>
            <el-date-picker
                v-model="activityRange"
                type="daterange"
                size="mini"
                value-format="yyyy-MM-dd"
                @change="initActivityChart"
                style="width: 220px">
            </el-date-picker>
          </div>
          <div id="activityChart" style="width: 100%; height: 300px;"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="chart-box">
          <div class="chart-header">
            <div class="chart-title">🏥 宠物年龄结构分布 (全量快照)</div>
          </div>
          <div id="ageChart" style="width: 100%; height: 300px;"></div>
        </div>
      </el-col>
    </el-row>

  </div>
</template>

<script>
import * as echarts from 'echarts';

export default {
  data() {
    return {
      stats: {},
      charts: {},
      revenueRange: [],
      salesRange: [],
      activityRange: []
    }
  },
  created() {
    const end = new Date();
    // 默认展示最近7天 (营收和活跃度)
    const start7 = new Date();
    start7.setTime(start7.getTime() - 3600 * 1000 * 24 * 6);
    const defaultRange = [this.formatDate(start7), this.formatDate(end)];

    // 热门商品销量默认展示最近一个季度 (90天)
    const start90 = new Date();
    start90.setTime(start90.getTime() - 3600 * 1000 * 24 * 89);
    const quarterRange = [this.formatDate(start90), this.formatDate(end)];

    this.revenueRange = defaultRange;
    this.salesRange = quarterRange;
    this.activityRange = defaultRange;
  },
  mounted() {
    this.loadStats();
    this.initAllCharts();
    window.addEventListener('resize', this.handleResize);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    formatDate(date) {
      let y = date.getFullYear();
      let m = (date.getMonth() + 1).toString().padStart(2, '0');
      let d = date.getDate().toString().padStart(2, '0');
      return `${y}-${m}-${d}`;
    },
    loadStats() {
      this.$http.get('/sysUser/stats').then(res => { if(res.data) this.$set(this.stats, 'userCount', res.data.userCount); }).catch(()=>{});
      this.$http.get('/orders/stats').then(res => {
        if(res.data) {
          this.$set(this.stats, 'orderCount', res.data.total);
          this.$set(this.stats, 'revenue', res.data.revenue);
        }
      }).catch(()=>{});
      this.$http.get('/petInfo/stats').then(res => { if(res.data) this.$set(this.stats, 'petCount', res.data.total); }).catch(()=>{});
    },

    refreshData() {
      this.loadStats();
      this.initAllCharts();
      this.$message.success('数据已同步');
    },

    initAllCharts() {
      this.initRevenueChart();
      this.initSalesChart();
      this.initActivityChart();
      this.initAgeChart();
    },

    initRevenueChart() {
      const start = this.revenueRange ? this.revenueRange[0] : '';
      const end = this.revenueRange ? this.revenueRange[1] : '';
      this.$http.get('/echarts/revenueTrend', {params: {startTime: start, endTime: end}}).then(res => {
        if (res.data) {
          const chart = echarts.init(document.getElementById('revenueChart'));
          this.charts.revenue = chart;
          chart.setOption({
            tooltip: { trigger: 'axis' },
            grid: {left: '3%', right: '4%', top: '15%', bottom: '10%', containLabel: true},
            xAxis: { type: 'category', data: res.data.dates, boundaryGap: false, axisLine: { lineStyle: { color: '#ccc' } } },
            yAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed' } } },
            series: [{
              name: '营收(¥)',
              data: res.data.values, type: 'line', smooth: true, showSymbol: false, lineStyle: { width: 3 }, itemStyle: { color: '#0984e3' },
              areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{offset: 0, color: 'rgba(9,132,227,0.4)'}, {offset: 1, color: 'rgba(9,132,227,0.01)'}]) }
            }]
          });
        }
      });
    },

    initSalesChart() {
      const start = this.salesRange ? this.salesRange[0] : '';
      const end = this.salesRange ? this.salesRange[1] : '';
      this.$http.get('/echarts/salesRank', {params: {startTime: start, endTime: end}}).then(res => {
        if (res.data) {
          const chart = echarts.init(document.getElementById('salesChart'));
          this.charts.sales = chart;
          chart.setOption({
            tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
            grid: {left: '3%', right: '4%', top: '15%', bottom: '10%', containLabel: true},
            xAxis: { type: 'category', data: res.data.names, axisLabel: { interval: 0, rotate: 15, color: '#666' } },
            yAxis: { type: 'value' },
            series: [{
              name: '销量',
              data: res.data.values, type: 'bar', barWidth: '40%',
              itemStyle: { borderRadius: [5, 5, 0, 0], color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{offset: 0, color: '#74b9ff'}, {offset: 1, color: '#0984e3'}]) },
              label: { show: true, position: 'top', color: '#666' }
            }]
          });
        }
      });
    },

    initActivityChart() {
      const start = this.activityRange ? this.activityRange[0] : '';
      const end = this.activityRange ? this.activityRange[1] : '';
      this.$http.get('/echarts/platformActivity', {params: {startTime: start, endTime: end}}).then(res => {
        if (res.data) {
          const chart = echarts.init(document.getElementById('activityChart'));
          this.charts.activity = chart;
          chart.setOption({
            tooltip: {trigger: 'axis'},
            legend: {bottom: '0%', left: 'center'},
            grid: {left: '3%', right: '4%', top: '15%', bottom: '20%', containLabel: true},
            xAxis: {type: 'category', data: res.data.dates, axisLine: {lineStyle: {color: '#ccc'}}},
            yAxis: {type: 'value', splitLine: {lineStyle: {type: 'dashed'}}},
            series: [
              {
                name: '新增用户',
                type: 'bar',
                data: res.data.userValues,
                itemStyle: {color: '#6c5ce7'}
              },
              {
                name: '咨询消息',
                type: 'line',
                smooth: true,
                data: res.data.chatValues,
                itemStyle: {color: '#e17055'},
                lineStyle: {width: 3}
              }
            ]
          });
        }
      });
    },

    initAgeChart() {
      this.$http.get('/echarts/petAgeDist').then(res => {
        if (res.data) {
          const chart = echarts.init(document.getElementById('ageChart'));
          this.charts.age = chart;
          chart.setOption({
            tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
            legend: { bottom: '0%', left: 'center' },
            series: [{
              type: 'pie', radius: ['40%', '70%'], avoidLabelOverlap: false,
              itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
              label: { show: true, formatter: '{b}: {d}%', fontWeight: 'bold' },
              color: ['#55efc4', '#0984e3', '#e17055'],
              data: res.data
            }]
          });
        }
      });
    },

    handleResize() {
      if(this.charts.revenue) this.charts.revenue.resize();
      if(this.charts.sales) this.charts.sales.resize();
      if(this.charts.age) this.charts.age.resize();
      if (this.charts.activity) this.charts.activity.resize();
    }
  }
}
</script>

<style scoped>
.page-container { padding: 20px; background-color: #f0f2f5; min-height: 100vh; }

/* 顶部卡片 */
.top-cards { display: flex; gap: 20px; margin-bottom: 25px; }
.card { flex: 1; padding: 25px; border-radius: 16px; position: relative; box-shadow: 0 8px 20px rgba(0,0,0,0.05); transition: all 0.3s; overflow: hidden; color: #fff; display: flex; align-items: center; }
.card.clickable { cursor: pointer; }
.card.clickable:hover { transform: translateY(-5px); box-shadow: 0 12px 25px rgba(0,0,0,0.15); }
.blue-card { background: linear-gradient(135deg, #0984e3 0%, #74b9ff 100%); }
.green-card { background: linear-gradient(135deg, #00b894 0%, #55efc4 100%); }
.orange-card { background: linear-gradient(135deg, #e17055 0%, #fab1a0 100%); }
.purple-card { background: linear-gradient(135deg, #6c5ce7 0%, #a29bfe 100%); }
.card-content { z-index: 2; position: relative; }
.card .num { font-size: 36px; font-weight: 800; margin-bottom: 5px; line-height: 1; font-family: 'DIN', sans-serif; text-shadow: 1px 1px 2px rgba(0,0,0,0.1); }
.card .txt { font-size: 14px; opacity: 0.9; font-weight: 500; }
.bg-icon { position: absolute; right: -10px; bottom: -20px; font-size: 100px; opacity: 0.2; transform: rotate(-15deg); z-index: 1; color: #fff; }

/* 区域标题 */
.section-header { font-size: 18px; font-weight: bold; color: #333; margin-bottom: 15px; border-left: 5px solid #0984e3; padding-left: 10px; display: flex; align-items: center; gap: 8px; }
.sub-title { font-size: 12px; color: #999; font-weight: normal; margin-left: 5px; }

/* 图表盒子 */
.chart-box { background: #fff; padding: 20px; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.03); margin-bottom: 5px; }

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.chart-title {
  font-weight: 600;
  font-size: 15px;
  color: #555;
}
</style>