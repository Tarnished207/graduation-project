<template>
  <div class="product-detail-layout">
    <div class="detail-nav">
      <div class="nav-container">
        <div class="back-btn-wrapper" @click="goBack">
          <i class="el-icon-back"></i>
          <span>返回</span>
        </div>
        <el-divider direction="vertical"></el-divider>
        <el-breadcrumb separator-class="el-icon-arrow-right">
          <el-breadcrumb-item :to="{ path: backPath }">{{ backText }}</el-breadcrumb-item>
          <el-breadcrumb-item>{{ getCategoryName(product.category) }}</el-breadcrumb-item>
          <el-breadcrumb-item>{{ product.name }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </div>

    <div class="detail-main-container fade-in" v-loading="loading">
      <div class="product-core-info" v-if="product.id">
        <div class="core-left">
          <div class="gallery-container">
            <div class="main-image-wrapper">
              <el-image
                  class="main-img"
                  v-if="swiperList.length > 0"
                  :src="getImageUrl(swiperList[currentImgIndex])"
                  :preview-src-list="previewList"
                  fit="contain">
                <div slot="error" class="image-slot">
                  <i class="el-icon-picture-outline"></i>
                </div>
              </el-image>
            </div>

            <div class="thumbnail-list" v-if="swiperList.length > 1">
              <div
                  class="thumb-item"
                  v-for="(item, index) in swiperList"
                  :key="index"
                  :class="{ active: currentImgIndex === index }"
                  @mouseenter="switchImage(index)"
                  @click="switchImage(index)">
                <img :src="getImageUrl(item)">
              </div>
            </div>
          </div>
        </div>

        <div class="core-right">
          <div class="brand-tag" v-if="product.brand">{{ product.brand }}</div>
          <h1 class="product-title">{{ product.name }}</h1>
          <p class="product-subtitle">{{ product.description || '优质宠物用品，给它最好的呵护' }}</p>

          <div class="price-card">
            <div class="price-item">
              <span class="label">售价</span>
              <span class="currency">¥</span>
              <span class="price">{{ product.price }}</span>
              <span class="original-price" v-if="product.originalPrice && product.originalPrice > product.price">
                ¥{{ product.originalPrice }}
              </span>
            </div>
            <div class="promotion-item" v-if="product.recommendReason">
              <span class="label">推荐理由</span>
              <span class="reason">{{ product.recommendReason }}</span>
            </div>
          </div>

          <div class="spec-info">
            <div class="spec-row">
              <span class="label">所属分类</span>
              <span class="val">{{ getCategoryName(product.category) }}</span>
            </div>

            <!-- 动态规格展示 -->
            <div class="spec-row" v-for="(spec, idx) in specList" :key="'s'+idx">
              <span class="label">{{ spec.key }}</span>
              <span class="val">{{ spec.value }}</span>
            </div>

            <div class="spec-row" v-if="product.tags">
              <span class="label">商品标签</span>
              <div class="tags-group">
                <el-tag v-for="tag in product.tags.split(',')" :key="tag" size="small" type="info" effect="plain">
                  {{ tag }}
                </el-tag>
              </div>
            </div>
            <div class="spec-row">
              <span class="label">剩余库存</span>
              <span class="val" :class="{ 'low-stock': product.stock < 10 }">{{ product.stock }} 件</span>
            </div>
          </div>

          <div class="action-area">
            <div class="qty-selector">
              <span class="label">购买数量</span>
              <el-input-number v-model="buyNum" :min="1" :max="product.stock" size="medium"></el-input-number>
            </div>

            <div class="btn-group">
              <el-button type="warning" plain icon="el-icon-shopping-cart-2" class="action-btn cart-btn"
                         @click="addToCart">
                加入购物车
              </el-button>
              <el-button type="danger" class="action-btn buy-btn" @click="quickBuy">
                立即购买
              </el-button>
            </div>

            <div class="extra-actions">
              <div class="action-item" @click="consultService">
                <i class="el-icon-chat-dot-round"></i>
                <span>咨询客服</span>
              </div>
              <div class="action-item" @click="shareProduct">
                <i class="el-icon-share"></i>
                <span>分享商品</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="product-detail-bottom" v-if="product.id">
        <div class="tab-header">
          <div class="tab-item active">商品详细描述</div>
        </div>
        <div class="detail-content">
          <div class="rich-text ql-editor"
               v-html="product.content || '<div class=\'empty-content\'>商家太懒了，还没有上传详细图文介绍哦~</div>'"></div>
        </div>
      </div>

      <div class="loading-placeholder" v-else-if="!loading">
        <el-empty description="商品可能已下架或不存在"></el-empty>
      </div>
    </div>

    <!-- 确认订单弹窗 (复用商城逻辑) -->
    <el-dialog title="确认订单" :visible.sync="orderVisible" width="500px" center custom-class="clean-dialog">
      <div class="order-confirm-list">
        <el-input v-model="address" placeholder="请填写收货地址 (必填)" prefix-icon="el-icon-location-outline"
                  style="margin-bottom:15px"></el-input>
        <div class="oc-item">
          <span>{{ product.name }}</span>
          <span class="oc-right">x{{ buyNum }} <small>¥{{ (product.price * buyNum).toFixed(2) }}</small></span>
        </div>
        <div class="oc-total">总计：<span>¥ {{ (product.price * buyNum).toFixed(2) }}</span></div>
      </div>
      <span slot="footer">
        <el-button @click="orderVisible = false">取消</el-button>
        <el-button type="primary" @click="submitOrder" :loading="orderLoading">立即支付</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "ProductDetail",
  data() {
    return {
      user: JSON.parse(sessionStorage.getItem("user") || '{}'),
      id: this.$route.params.id,
      product: {},
      loading: false,
      buyNum: 1,
      orderVisible: false,
      orderLoading: false,
      address: '',
      swiperList: [],
      specList: [],
      currentImgIndex: 0
    };
  },
  computed: {
    previewList() {
      return this.swiperList.map(url => this.getImageUrl(url));
    },
    backPath() {
      const from = this.$route.query.from;
      if (from === 'cart') return '/front/cart';
      if (from === 'order') return '/front/orders';
      return '/front/mall';
    },
    backText() {
      const from = this.$route.query.from;
      if (from === 'cart') return '我的购物车';
      if (from === 'order') return '我的订单';
      return '宠物商城';
    }
  },
  created() {
    if (!this.user.id) return this.$router.push("/login");
    this.address = this.user.address || '';
    this.loadProduct();
  },
  methods: {
    switchImage(index) {
      this.currentImgIndex = index;
    },
    goBack() {
      if (this.$route.query.from) {
        this.$router.push(this.backPath);
      } else {
        // 如果没有来源信息，尝试返回上一页，如果不行则去商城
        if (window.history.length > 1) {
          this.$router.back();
        } else {
          this.$router.push('/front/mall');
        }
      }
    },
    loadProduct() {
      this.loading = true;
      this.$http.get('/product/' + this.id).then(res => {
        this.product = res.data.data || res.data || {};

        // 解析轮播图
        this.swiperList = [];
        if (this.product.swiperImages) {
          try {
            let raw = this.product.swiperImages;
            // 兼容单引号 JSON 格式
            if (raw.startsWith('[') && raw.includes("'")) {
              raw = raw.replace(/'/g, '"');
            }

            if (raw.startsWith('[')) {
              this.swiperList = JSON.parse(raw);
            } else {
              this.swiperList = raw.split(',').filter(s => s && s.trim());
            }
          } catch (e) {
            console.error("Failed to parse swiperImages", e);
            // 降级处理：尝试移除所有括号引号后按逗号分割
            this.swiperList = this.product.swiperImages.replace(/[\[\]"']/g, '').split(',').filter(s => s && s.trim());
          }
        }

        // 确保至少有一张图（主图兜底）
        if (this.swiperList.length === 0 && this.product.image) {
          this.swiperList = [this.product.image];
        }

        this.currentImgIndex = 0;

        // 解析规格
        this.specList = [];
        if (this.product.specs) {
          try {
            this.specList = JSON.parse(this.product.specs);
          } catch (e) {
          }
        }

        this.loading = false;
      }).catch(() => {
        this.loading = false;
        this.$message.error("获取商品详情失败");
      });
    },
    getImageUrl(url) {
      let baseUrl = this.$baseUrl || 'http://localhost:9090';
      if (!url) return baseUrl + '/images/default.png';

      let finalUrl = '';
      if (url.startsWith('http')) {
        finalUrl = url;
      } else if (url.startsWith('/')) {
        finalUrl = baseUrl + url;
      } else {
        finalUrl = baseUrl + '/images/' + url;
      }

      // 关键修复：对 URL 进行编码，防止文件名中包含空格或中文导致无法访问
      return encodeURI(finalUrl);
    },
    getCategoryName(cat) {
      const map = {
        'food': '营养主粮', 'snack': '美味零食', 'toy': '趣味玩具',
        'daily': '日常用品', 'medical': '医疗保健', 'device': '宠物器械'
      };
      return map[cat] || cat;
    },
    addToCart() {
      const cartItem = {userId: this.user.id, productId: this.product.id, quantity: this.buyNum};
      this.$http.post('/cart/add', cartItem).then(res => {
        if (res.data) {
          this.$message.success("已加入购物车");
        }
      });
    },
    quickBuy() {
      this.orderVisible = true;
    },
    submitOrder() {
      if (!this.address) return this.$message.warning("请填写收货地址");
      this.orderLoading = true;

      // 直接下单逻辑（不经过购物车列表，但后端checkout接口通常需要cartIds）
      // 这里为了兼容性，先将商品加入购物车，然后获取其ID进行结算
      const cartItem = {userId: this.user.id, productId: this.product.id, quantity: this.buyNum};
      this.$http.post('/cart/add', cartItem).then(res => {
        // 假设返回的是新加入的购物车项，或者我们需要重新获取最新的购物车项
        this.$http.get('/cart/list/' + this.user.id).then(cartRes => {
          const list = cartRes.data || [];
          // 找到刚加入的这一项（通常是最后一项或者根据productId匹配）
          const item = list.find(i => i.productId === this.product.id);
          if (item) {
            const request = {
              userId: this.user.id,
              cartIds: [item.id],
              address: this.address
            };
            this.$http.post('/cart/checkout', request).then(checkRes => {
              if (checkRes.data) {
                this.$message.success("下单成功！");
                this.orderVisible = false;
                setTimeout(() => this.$router.push('/front/orders'), 1000);
              }
            });
          }
        });
      }).finally(() => {
        this.orderLoading = false;
      });
    },
    consultService() {
      this.$root.$emit('open-consult', this.product);
    },
    shareProduct() {
      this.$message.info("链接已复制到剪贴板 (模拟功能)");
    }
  }
};
</script>

<style scoped>
.product-detail-layout {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding-top: 80px;
  padding-bottom: 50px;
}

.detail-nav {
  background: white;
  border-bottom: 1px solid #eee;
  padding: 15px 0;
  margin-bottom: 20px;
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
}

.back-btn-wrapper {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: #666;
  font-size: 14px;
  line-height: 1;
  transition: all 0.3s;
  padding: 2px 8px;
  border-radius: 4px;
}

.back-btn-wrapper:hover {
  color: #e67e22;
  background-color: #fff5eb;
}

.back-btn-wrapper i {
  font-weight: bold;
  font-size: 16px;
  margin-top: 1px;
}

.el-divider--vertical {
  margin: 0 15px;
  height: 14px;
  background-color: #ddd;
}

::v-deep .el-breadcrumb {
  line-height: 1;
  display: flex;
  align-items: center;
}

::v-deep .el-breadcrumb__item {
  display: flex;
  align-items: center;
}

.detail-main-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.product-core-info {
  background: white;
  border-radius: 12px;
  padding: 40px;
  display: flex;
  gap: 50px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03);
  margin-bottom: 30px;
}

.core-left {
  width: 450px;
  flex-shrink: 0;
}

.gallery-container {
  width: 450px;
}

.main-image-wrapper {
  width: 450px;
  height: 450px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  margin-bottom: 15px;
}

.main-img {
  width: 100%;
  height: 100%;
}

.thumbnail-list {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding: 2px;
}

.thumbnail-list::-webkit-scrollbar {
  height: 6px;
}

.thumbnail-list::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 3px;
}

.thumb-item {
  width: 60px;
  height: 60px;
  border: 2px solid transparent;
  border-radius: 4px;
  cursor: pointer;
  overflow: hidden;
  flex-shrink: 0;
  transition: all 0.2s;
}

.thumb-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb-item:hover, .thumb-item.active {
  border-color: #ff0036;
  opacity: 1;
}

.thumb-item:not(.active):hover {
  opacity: 0.8;
}

.core-right {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.brand-tag {
  display: inline-block;
  background: #333;
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 2px;
  margin-bottom: 8px;
  align-self: flex-start;
}

.product-title {
  font-size: 26px;
  font-weight: bold;
  color: #333;
  margin: 0 0 10px 0;
  line-height: 1.3;
}

.product-subtitle {
  font-size: 15px;
  color: #ff6b6b;
  margin-bottom: 25px;
}

.price-card {
  background: #fff5f5;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
}

.price-item {
  display: flex;
  align-items: baseline;
  margin-bottom: 10px;
}

.price-item .label {
  font-size: 13px;
  color: #999;
  margin-right: 15px;
}

.price-item .currency {
  font-size: 20px;
  color: #ff0036;
  font-weight: bold;
  margin-right: 2px;
}

.price-item .price {
  font-size: 36px;
  color: #ff0036;
  font-weight: bold;
  font-family: Arial;
}

.original-price {
  margin-left: 10px;
  color: #999;
  text-decoration: line-through;
  font-size: 14px;
}

.promotion-item {
  display: flex;
  align-items: center;
  font-size: 13px;
}

.promotion-item .label {
  color: #999;
  margin-right: 15px;
}

.promotion-item .reason {
  color: #666;
  background: #ffeaa7;
  padding: 2px 8px;
  border-radius: 4px;
}

.spec-info {
  margin-bottom: 35px;
}

.spec-row {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  font-size: 14px;
}

.spec-row .label {
  color: #999;
  width: 80px;
  flex-shrink: 0;
}

.spec-row .val {
  color: #333;
}

.low-stock {
  color: #ff6b6b;
  font-weight: bold;
}

.tags-group {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-area {
  margin-top: auto;
  padding-top: 30px;
  border-top: 1px dashed #eee;
}

.qty-selector {
  display: flex;
  align-items: center;
  margin-bottom: 25px;
}

.qty-selector .label {
  font-size: 14px;
  color: #999;
  margin-right: 15px;
}

.btn-group {
  display: flex;
  gap: 15px;
  margin-bottom: 25px;
}

.action-btn {
  height: 50px;
  padding: 0 40px;
  font-size: 18px;
  font-weight: bold;
  border-radius: 6px;
}

.cart-btn {
  border: 1px solid #ff0036;
  color: #ff0036;
}

.buy-btn {
  background: #ff0036;
  border-color: #ff0036;
}

.extra-actions {
  display: flex;
  gap: 30px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: color 0.2s;
}

.action-item:hover {
  color: #ff9f43;
}

.action-item i {
  font-size: 18px;
}

.product-detail-bottom {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03);
}

.tab-header {
  border-bottom: 1px solid #eee;
  padding: 0 30px;
}

.tab-item {
  display: inline-block;
  padding: 20px 0;
  font-size: 16px;
  font-weight: bold;
  color: #333;
  border-bottom: 3px solid transparent;
  margin-right: 40px;
}

.tab-item.active {
  color: #ff0036;
  border-bottom-color: #ff0036;
}

.detail-content {
  padding: 40px;
  min-height: 400px;
}

.rich-text {
  line-height: 1.8;
  color: #333;
}

.rich-text >>> img {
  max-width: 100%;
  display: block;
  margin: 20px auto;
  border-radius: 8px;
}

.empty-content {
  text-align: center;
  color: #999;
  padding: 100px 0;
}

/* 订单弹窗样式复用 */
.order-confirm-list {
  padding: 10px 0;
}

.oc-item {
  display: flex;
  justify-content: space-between;
  padding: 15px 0;
  border-bottom: 1px dashed #eee;
  font-size: 15px;
  color: #555;
}

.oc-right small {
  margin-left: 10px;
  color: #ff9f43;
  font-weight: bold;
}

.oc-total {
  text-align: right;
  margin-top: 25px;
  font-size: 18px;
  font-weight: bold;
}

.oc-total span {
  color: #ff6b6b;
  font-size: 24px;
}

.clean-dialog {
  border-radius: 12px !important;
}

.fade-in {
  animation: fadeIn 0.5s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
