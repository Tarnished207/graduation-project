<template>
  <div class="product-page">

    <div class="stats-container">
      <div class="stat-card main-card">
        <div class="main-card-content">
          <div class="icon-circle">
            <i class="el-icon-trophy"></i>
          </div>
          <div class="text-group">
            <div class="label">商品总数 (SKU)</div>
            <div class="number">{{ stats.total || 0 }}</div>
            <div class="tag">
              <i class="el-icon-top"></i> 本周上新 {{ stats.newThisWeek || 0 }}
            </div>
          </div>
        </div>
        <i class="el-icon-shopping-bag-1 bg-watermark"></i>
      </div>

      <div class="small-cards-group">
        <div class="stat-card small-card">
          <div class="card-header">
            <span>覆盖品类</span>
            <i class="el-icon-menu icon-grey"></i>
          </div>
          <div class="card-body">
            <div class="card-value">{{ stats.categoryCount || 0 }}</div>
          </div>
          <div class="card-footer"></div>
        </div>

        <div class="stat-card small-card">
          <div class="card-header">
            <span>热卖中</span>
            <i class="el-icon-sell icon-grey"></i>
          </div>
          <div class="card-body">
            <div class="card-value orange-text">{{ stats.active || 0 }}</div>
          </div>
          <div class="card-footer"></div>
        </div>

        <div class="stat-card small-card">
          <div class="card-header">
            <span>库存预警</span>
            <i class="el-icon-warning-outline icon-grey"></i>
          </div>
          <div class="card-body">
            <div class="card-value red-text">{{ stats.lowStock || 0 }}</div>
          </div>
          <div class="card-footer">
            <span class="sub-tag red" v-if="stats.lowStock > 0">需要补货</span>
          </div>
        </div>
      </div>
    </div>

    <div class="content-panel">

      <div class="toolbar">
        <div class="left-tools">
          <el-input
              v-model="searchForm.name"
              placeholder="🔍 搜索商品..."
              class="search-input"
              @keyup.enter.native="load">
          </el-input>

          <el-select v-model="searchForm.category" placeholder="全部分类" class="filter-select" @change="load">
            <el-option label="🥘 主粮" value="food"></el-option>
            <el-option label="🍖 零食" value="snack"></el-option>
            <el-option label="🎾 玩具" value="toy"></el-option>
            <el-option label="💊 医疗" value="medical"></el-option>
            <el-option label="🧴 日用" value="daily"></el-option>
            <el-option label="🔧 器械" value="device"></el-option>
          </el-select>

          <el-button circle icon="el-icon-search" @click="load"></el-button>
          <el-button circle icon="el-icon-refresh" @click="resetSearch"></el-button>
        </div>

        <el-button type="primary" round icon="el-icon-plus" class="add-btn" @click="handleAdd">上架新品</el-button>
      </div>

      <el-table
          :data="tableData"
          style="width: 100%"
          class="custom-table"
          :header-cell-style="{background:'#fff', color:'#909399', borderBottom:'1px solid #f0f0f0', fontWeight:'500'}"
      >
        <el-table-column label="商品信息" min-width="220">
          <template slot-scope="scope">
            <div class="product-meta">
              <el-image :src="getImageUrl(scope.row.image)" class="product-thumb" fit="cover">
                <div slot="error" class="image-slot"><i class="el-icon-picture-outline"></i></div>
              </el-image>
              <div class="meta-text">
                <div class="p-name">{{ scope.row.name }}</div>
                <div class="p-cat"><el-tag size="mini" type="info">{{ formatCategory(scope.row.category) }}</el-tag></div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="价格" width="150">
          <template slot-scope="scope">
            <span class="price-font">¥{{ scope.row.price }}</span>
          </template>
        </el-table-column>

        <el-table-column label="标签 (Tags)" min-width="220">
          <template slot-scope="scope">
            <div class="tags-container">
              <span v-if="!scope.row.tags" style="color:#eee">—</span>
              <el-tag
                  v-else
                  v-for="(tag, index) in scope.row.tags.split(',')"
                  :key="index"
                  size="mini"
                  type="info"
                  class="plain-tag">
                #{{ tag }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="stock" label="库存" width="150">
          <template slot-scope="scope">
            <div class="stock-indicator">
              <div class="indicator-bg">
                <div class="indicator-fill"
                     :style="{width: Math.min(scope.row.stock, 100) + '%'}"
                     :class="scope.row.stock < 10 ? 'fill-red' : 'fill-green'">
                </div>
              </div>
              <span class="stock-text">{{ scope.row.stock }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" align="right">
          <template slot-scope="scope">
            <el-button type="text" style="color:#666" @click="openStockDialog(scope.row)">补货</el-button>
            <el-button type="text" style="color:#666" @click="handleEdit(scope.row)">编辑</el-button>
            <el-popconfirm title="确认下架？" @confirm="handleDelete(scope.row.id)">
              <el-button slot="reference" type="text" style="color:#ff6b6b; margin-left:10px">下架</el-button>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog :title="form.id ? '编辑商品' : '上架新商品'" :visible.sync="dialogVisible" width="800px"
               custom-class="rounded-dialog" top="5vh">
      <el-form :model="form" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="名称"><el-input v-model="form.name"></el-input></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="分类">
              <el-select v-model="form.category" style="width:100%">
                <el-option label="主粮" value="food"></el-option>
                <el-option label="零食" value="snack"></el-option>
                <el-option label="玩具" value="toy"></el-option>
                <el-option label="医疗" value="medical"></el-option>
                <el-option label="日用" value="daily"></el-option>
                <el-option label="器械" value="device"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="售价">
              <el-input-number v-model="form.price" :precision="2" :step="1" style="width:100%"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="原价">
              <el-input-number v-model="form.originalPrice" :precision="2" :step="1" style="width:100%"
                               placeholder="市场价"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="库存">
              <el-input-number v-model="form.stock" style="width:100%"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="品牌">
              <el-input v-model="form.brand" placeholder="如：皇家、渴望"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标签">
              <el-select v-model="dynamicTags" multiple filterable allow-create default-first-option
                         placeholder="回车生成标签" style="width:100%">
                <el-option label="幼犬" value="幼犬"></el-option>
                <el-option label="成犬" value="成犬"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="主图封面">
          <el-upload class="avatar-uploader"
                     :action="$baseUrl + '/file/upload'"
                     name="file"
                     :show-file-list="false"
                     :on-success="handleUploadSuccess">
            <img v-if="form.image" :src="getImageUrl(form.image)" class="avatar">
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
        </el-form-item>

        <el-form-item label="轮播相册">
          <el-upload
              :action="$baseUrl + '/file/upload'"
              name="file"
              list-type="picture-card"
              :file-list="swiperFileList"
              :on-success="handleSwiperSuccess"
              :on-remove="handleSwiperRemove">
            <i class="el-icon-plus"></i>
          </el-upload>
          <div style="font-size:12px;color:#999;line-height:1.5">建议上传 3-5 张高清大图，展示商品细节</div>
        </el-form-item>

        <el-form-item label="规格参数">
          <div v-for="(spec, index) in specList" :key="index" style="display:flex; gap:10px; margin-bottom:10px">
            <el-input v-model="spec.key" placeholder="参数名 (如:重量)" style="width:120px" size="small"></el-input>
            <el-input v-model="spec.value" placeholder="参数值 (如:2kg)" style="flex:1" size="small"></el-input>
            <el-button type="danger" icon="el-icon-delete" circle size="small" @click="removeSpec(index)"></el-button>
          </div>
          <el-button type="primary" plain size="small" icon="el-icon-plus" @click="addSpec">添加参数</el-button>
        </el-form-item>

        <el-form-item label="列表简介">
          <el-input
              type="textarea"
              v-model="form.description"
              placeholder="展示在商品列表卡片上的简短介绍（建议30字以内）"
              :rows="2">
          </el-input>
        </el-form-item>

        <el-form-item label="商品详情">
          <el-input
              type="textarea"
              v-model="form.content"
              placeholder="支持HTML代码。建议使用 <p>段落</p> <img>图片 布局"
              :rows="6">
          </el-input>
          <div style="margin-top:5px">
            <el-popover placement="top" width="400" trigger="click">
              <div v-html="form.content"
                   style="max-height:300px;overflow:auto;border:1px solid #eee;padding:10px"></div>
              <el-button slot="reference" size="mini" type="text">预览效果</el-button>
            </el-popover>
          </div>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false" round>取消</el-button>
        <el-button type="primary" @click="save" round class="confirm-btn">保存</el-button>
      </span>
    </el-dialog>

    <el-dialog title="📦 快速补货" :visible.sync="stockDialogVisible" width="400px" custom-class="rounded-dialog">
      <div style="text-align:center; padding: 20px;">
        <h3>{{ currentProduct.name }}</h3>
        <p style="color:#888">当前库存: <span style="color:#ff6b6b;font-weight:bold">{{ currentProduct.stock }}</span></p>
        <el-input-number v-model="addStockNum" :min="1" :step="10"></el-input-number>
      </div>
      <span slot="footer"><el-button type="primary" @click="confirmAddStock" round class="confirm-btn">确认入库</el-button></span>
    </el-dialog>

  </div>
</template>

<script>
export default {
  data() {
    return {
      stats: { total: 0, lowStock: 0, active: 0, categoryCount: 0, newThisWeek: 0 },
      tableData: [],
      searchForm: { name: '', category: '' },
      dialogVisible: false,
      stockDialogVisible: false,
      form: {
        id: null,
        name: '',
        price: 0,
        originalPrice: 0,
        stock: 0,
        category: '',
        image: '',
        description: '',
        content: '',
        tags: '',
        brand: '',
        swiperImages: '',
        specs: ''
      },
      currentProduct: {},
      addStockNum: 10,
      dynamicTags: [],
      swiperFileList: [],
      specList: []
    }
  },
  created() { this.load(); this.loadStats(); },
  methods: {
    loadStats() { this.$http.get('/product/stats').then(res => { this.stats = res.data; }); },
    load() { this.$http.get('/product/list', { params: { name: this.searchForm.name, category: this.searchForm.category } }).then(res => { this.tableData = res.data; }); },
    resetSearch() { this.searchForm = { name: '', category: '' }; this.load(); },
    handleAdd() {
      this.form = {price: 0, stock: 100, category: 'food'};
      this.dynamicTags = [];
      this.swiperFileList = [];
      this.specList = [];
      this.dialogVisible = true;
    },
    handleEdit(row) {
      this.form = JSON.parse(JSON.stringify(row));
      this.dynamicTags = this.form.tags ? this.form.tags.split(',') : [];

      // 处理轮播图回显
      this.swiperFileList = [];
      if (this.form.swiperImages) {
        try {
          // 尝试解析JSON，如果失败可能是逗号分隔字符串
          let images = [];
          if (this.form.swiperImages.startsWith('[')) {
            images = JSON.parse(this.form.swiperImages);
          } else {
            images = this.form.swiperImages.split(',');
          }
          this.swiperFileList = images.map(url => ({name: url, url: this.getImageUrl(url), rawUrl: url}));
        } catch (e) {
          console.error(e);
        }
      }

      // 处理规格回显
      this.specList = [];
      if (this.form.specs) {
        try {
          this.specList = JSON.parse(this.form.specs);
        } catch (e) {
          console.error(e);
        }
      }

      this.dialogVisible = true;
    },
    // --- 轮播图与规格逻辑 ---
    handleSwiperSuccess(res, file, fileList) {
      this.swiperFileList = fileList;
    },
    handleSwiperRemove(file, fileList) {
      this.swiperFileList = fileList;
    },
    addSpec() {
      this.specList.push({key: '', value: ''});
    },
    removeSpec(index) {
      this.specList.splice(index, 1);
    },
    // -----------------------
    save() {
      // --- [新增] 表单必填项校验 ---
      if (!this.form.name) return this.$message.warning("请输入商品名称");
      if (!this.form.category) return this.$message.warning("请选择商品分类");
      if (this.form.price === undefined || this.form.price === null) return this.$message.warning("请输入商品价格");
      if (this.form.stock === undefined || this.form.stock === null) return this.$message.warning("请输入初始库存");
      if (!this.form.image) return this.$message.warning("请上传商品封面图片");
      // ---------------------------
      this.form.tags = this.dynamicTags.join(',');

      // 序列化新字段
      this.form.swiperImages = JSON.stringify(this.swiperFileList.map(f => f.rawUrl || f.response)); // 兼容刚上传的和回显的
      this.form.specs = JSON.stringify(this.specList.filter(s => s.key && s.value));

      let url = this.form.id ? '/product/update' : '/product/add';
      if(!this.form.status) this.form.status = 1;

      this.$http.post(url, this.form).then(res => {
        if (res.data) {
          this.$message.success("保存成功");
          this.dialogVisible = false;
          this.load();
          this.loadStats();
        }
      })
    },
    handleUploadSuccess(res) {
      this.form.image = res;
    },
    handleDelete(id) {
      this.$http.get('/product/delete/' + id).then(res => {
        if (res.data) {
          this.$message.success("下架成功");
          this.load();
          this.loadStats();
        } else {
          this.$message.error("操作失败");
        }
      });
    },
    openStockDialog(row) {
      this.currentProduct = JSON.parse(JSON.stringify(row));
      this.addStockNum = 10;
      this.stockDialogVisible = true;
    },
    confirmAddStock() {
      if (!this.currentProduct.id) return;
      this.$http.post('/product/addStock', {
        id: this.currentProduct.id,
        num: this.addStockNum
      }).then(res => {
        if (res.data) {
          this.$message.success("补货成功");
          this.stockDialogVisible = false;
          this.load();
          this.loadStats();
        } else {
          this.$message.error("补货失败");
        }
      });
    },
    formatCategory(val) {
      const map = {
        food: '主粮',
        snack: '零食',
        toy: '玩具',
        medical: '医疗',
        daily: '日用',
        device: '器械'
      };
      return map[val] || val;
    },
    getImageUrl(url) {
      if (!url) return '';
      if (url.startsWith('http')) return url;
      let baseUrl = this.$baseUrl || 'http://localhost:9090';
      if (url.startsWith('/')) return baseUrl + url;
      return baseUrl + '/images/' + url;
    },
  }
}
</script>

<style scoped>
/* 全局样式 */
.product-page {
  padding: 25px;
  background-color: #f7f8fa;
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

/* --- 顶部统计 Bento Grid --- */
.stats-container {
  display: flex;
  gap: 20px;
  margin-bottom: 25px;
  height: 140px;
}

/* 主卡片 (橙色) - 占比 35% */
.main-card {
  flex: 0 0 35%;
  background: linear-gradient(135deg, #ffbc6e 0%, #ff9f43 100%);
  border-radius: 16px;
  color: #fff;
  position: relative;
  overflow: hidden;
  box-shadow: 0 8px 20px rgba(255, 159, 67, 0.25);
  display: flex;
  align-items: center;
  padding: 0 30px;
}

.main-card-content { z-index: 2; display: flex; align-items: center; gap: 20px; width: 100%; }
.icon-circle {
  width: 64px; height: 64px;
  background: rgba(255, 255, 255, 0.25);
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.icon-circle i { font-size: 32px; color: #fff; }
.text-group .label { font-size: 15px; opacity: 0.9; margin-bottom: 5px; }
.text-group .number { font-size: 42px; font-weight: 800; line-height: 1; margin-bottom: 8px; }
.text-group .tag { display: inline-block; background: rgba(255, 255, 255, 0.2); padding: 4px 12px; border-radius: 20px; font-size: 13px; font-weight: 500; }
.bg-watermark { position: absolute; right: -20px; bottom: -30px; font-size: 180px; color: #fff; opacity: 0.15; transform: rotate(-15deg); z-index: 1; }

/* 右侧小卡片组 - 均匀分布 */
.small-cards-group {
  flex: 1;
  display: flex;
  gap: 20px;
}

.small-card {
  flex: 1;
  background: #fff;
  border-radius: 16px;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
  transition: transform 0.2s;
  border: 1px solid #f0f0f0;
  min-width: 0;
}
.small-card:hover { transform: translateY(-3px); }

/* 头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  color: #909399;
  font-size: 14px;
  align-items: center;
}
.icon-grey { font-size: 18px; color: #ccc; }

/* 中间 */
.card-body {
  margin-top: auto;
  margin-bottom: 10px;
}
.card-value { font-size: 36px; font-weight: 700; color: #303133; line-height: 1; }
.orange-text { color: #ff9f43; }
.red-text { color: #ff6b6b; }

/* 底部占位槽 */
.card-footer {
  height: 24px;
  display: flex;
  align-items: center;
}
.sub-tag {
  font-size: 12px; color: #fff;
  padding: 3px 8px; border-radius: 4px;
  display: inline-block;
}
.sub-tag.red { background: #ff6b6b; }


/* --- 主内容区 --- */
.content-panel {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
}
.toolbar { display: flex; justify-content: space-between; margin-bottom: 20px; }
.left-tools { display: flex; gap: 12px; }
.search-input { width: 220px; }
::v-deep .el-input__inner { border-radius: 20px; background: #f9f9f9; border: 1px solid #eee; }
::v-deep .el-input__inner:focus { background: #fff; border-color: #ff9f43; }
.add-btn { background: #ff9f43; border-color: #ff9f43; box-shadow: 0 4px 10px rgba(255, 159, 67, 0.4); }
.add-btn:hover { background: #ffaf5e; border-color: #ffaf5e; }

/* 表格 */
.custom-table { border-radius: 8px; }
.product-meta { display: flex; align-items: center; gap: 12px; }
.product-thumb { width: 48px; height: 48px; border-radius: 8px; background: #f5f5f5; border: 1px solid #eee; }
.p-name { font-weight: 600; color: #333; font-size: 14px; margin-bottom: 4px; }
.price-font { font-family: 'DIN', sans-serif; font-weight: 700; color: #ff6b6b; font-size: 16px; }
.tags-container { display: flex; flex-wrap: wrap; gap: 5px; }
.plain-tag { background: #f4f4f5; border: none; color: #909399; }

/* 库存条 */
.stock-indicator { display: flex; align-items: center; gap: 10px; font-size: 13px; color: #999; }
.indicator-bg { flex: 1; height: 6px; background: #EEE; border-radius: 3px; overflow: hidden; min-width: 60px; }
.indicator-fill { height: 100%; border-radius: 3px; transition: width 0.5s ease; }
.fill-green { background: #55efc4; }
.fill-red { background: #ff6b6b; }
.stock-text { width: 30px; text-align: right; }

/* 弹窗按钮 */
.confirm-btn { background: #ff9f43; border-color: #ff9f43; }
.avatar-uploader .el-upload { border: 2px dashed #eee; border-radius: 12px; cursor: pointer; position: relative; overflow: hidden; }
.avatar { width: 100px; height: 100px; display: block; }
.avatar-uploader-icon { font-size: 28px; color: #ccc; width: 100px; height: 100px; line-height: 100px; text-align: center; }
</style>