<template>
  <div class="cart-page">
    <div class="main-container">
      <div class="cart-header">
        <div class="header-nav">
          <div class="back-btn-wrapper" @click="goBack">
            <i class="el-icon-back"></i>
            <span>返回</span>
          </div>
          <el-divider direction="vertical"></el-divider>
          <el-breadcrumb separator-class="el-icon-arrow-right">
            <el-breadcrumb-item :to="{ path: '/front/mall' }">宠物商城</el-breadcrumb-item>
            <el-breadcrumb-item>我的购物车</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-title">
          <i class="el-icon-shopping-cart-full"></i> 购物车
          <span class="count" v-if="cartList.length">({{ cartList.length }})</span>
        </div>
      </div>

      <div class="cart-content fade-in" v-loading="loading">
        <div class="cart-list" v-if="cartList.length > 0">
          <el-table :data="cartList" style="width: 100%" class="cart-table" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55"></el-table-column>

            <el-table-column label="商品信息" min-width="300">
              <template slot-scope="scope">
                <div class="product-info" @click="openDetail(scope.row)">
                  <img :src="getCartItemImage(scope.row)" class="p-img">
                  <div class="p-text">
                    <div class="p-name">{{ scope.row.productName }}</div>
                    <div class="p-price">单价: ¥{{ scope.row.price }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="数量" width="180" align="center">
              <template slot-scope="scope">
                <div class="qty-control">
                  <el-button size="mini" circle icon="el-icon-minus" @click="updateCartQty(scope.row, -1)"
                             :disabled="scope.row.quantity <= 1"></el-button>
                  <span class="qty-num">{{ scope.row.quantity }}</span>
                  <el-button size="mini" circle icon="el-icon-plus" @click="updateCartQty(scope.row, 1)"></el-button>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="小计" width="150" align="center">
              <template slot-scope="scope">
                <span class="subtotal">¥{{ (scope.row.price * scope.row.quantity).toFixed(2) }}</span>
              </template>
            </el-table-column>

            <el-table-column label="操作" width="100" align="center">
              <template slot-scope="scope">
                <el-button type="text" icon="el-icon-delete" class="del-btn" @click="removeFromCart(scope.row.id)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="cart-footer-bar">
            <div class="left">
              <el-button type="text" @click="clearCart">清空购物车</el-button>
            </div>
            <div class="right">
              <div class="total-info">
                <span>已选 <span class="num">{{ multipleSelection.length }}</span> 件商品</span>
                <span class="total-text">合计: <span class="total-price">¥ {{ totalPrice }}</span></span>
              </div>
              <el-button type="primary" round class="checkout-btn" :disabled="multipleSelection.length === 0"
                         @click="handleCheckout">
                去结算
              </el-button>
            </div>
          </div>
        </div>

        <div class="empty-cart" v-else>
          <img src="https://cdn-icons-png.flaticon.com/512/2038/2038854.png" alt="Empty Cart">
          <h2>购物车空空如也</h2>
          <p>快去商城挑选心仪的商品吧~</p>
          <el-button type="primary" round @click="$router.push('/front/mall')">去逛逛</el-button>
        </div>
      </div>
    </div>

    <!-- 确认订单弹窗 -->
    <el-dialog title="确认订单" :visible.sync="orderVisible" width="500px" center custom-class="clean-dialog">
      <div class="order-confirm-list">
        <el-input v-model="address" placeholder="请填写收货地址 (必填)" prefix-icon="el-icon-location-outline"
                  style="margin-bottom:15px"></el-input>
        <div class="oc-item" v-for="item in multipleSelection" :key="item.id">
          <span>{{ item.productName }}</span>
          <span class="oc-right">x{{ item.quantity }} <small>¥{{
              (item.price * item.quantity).toFixed(2)
            }}</small></span>
        </div>
        <div class="oc-total">总计：<span>¥ {{ totalPrice }}</span></div>
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
  name: "Cart",
  data() {
    return {
      user: JSON.parse(sessionStorage.getItem("user") || '{}'),
      cartList: [],
      loading: false,
      multipleSelection: [], // 选中的商品
      allProducts: [], // 用于获取图片等信息

      orderVisible: false,
      orderLoading: false,
      address: ''
    };
  },
  created() {
    if (!this.user.id) return this.$router.push("/login");
    this.address = this.user.address || '';
    this.loadCart();
    this.loadProducts(); // 预加载商品信息以获取图片
  },
  computed: {
    totalPrice() {
      return this.multipleSelection.reduce((sum, item) => sum + (item.price || 0) * (item.quantity || 1), 0).toFixed(2);
    }
  },
  methods: {
    goBack() {
      if (window.history.length > 1) {
        this.$router.back();
      } else {
        this.$router.push('/front/mall');
      }
    },
    getImageUrl(url) {
      if (!url) return 'http://localhost:9090/images/default.png';
      if (url.startsWith('http')) return url;
      if (url.startsWith('/')) return 'http://localhost:9090' + url;
      return 'http://localhost:9090/images/' + url;
    },
    getCartItemImage(item) {
      if (item.image) return this.getImageUrl(item.image);
      // 如果购物车项没有图片，尝试从allProducts查找
      const prod = this.allProducts.find(p => p.id === item.productId);
      if (prod && prod.image) return this.getImageUrl(prod.image);
      return 'http://localhost:9090/images/default.png';
    },
    loadProducts() {
      // 简单获取一下所有商品用于图片匹配，也可以后端优化Cart接口直接返回图片
      this.$http.get('/product/list').then(res => {
        this.allProducts = res.data.data || res.data || [];
      });
    },
    loadCart() {
      this.loading = true;
      this.$http.get('/cart/list/' + this.user.id).then(res => {
        this.cartList = res.data || [];
        this.loading = false;
      }).catch(() => this.loading = false);
    },
    updateCartQty(item, delta) {
      const newQty = item.quantity + delta;
      if (newQty < 1) return;
      this.$http.post('/cart/update', {id: item.id, quantity: newQty}).then(res => {
        if (res.data) item.quantity = newQty;
      });
    },
    removeFromCart(id) {
      this.$confirm('确定要将该商品移出购物车吗?', '提示', {
        type: 'warning'
      }).then(() => {
        this.$http.get('/cart/delete/' + id).then(res => {
          if (res.data) {
            this.$message.success('删除成功');
            this.loadCart();
          }
        });
      }).catch(() => {
      });
    },
    clearCart() {
      // 这里的清空逻辑需要后端支持，暂时用循环删除模拟或者留空
      this.$message.info('请逐个删除商品');
    },
    handleSelectionChange(val) {
      this.multipleSelection = val;
    },
    openDetail(item) {
      this.$router.push({path: '/front/product/' + item.productId, query: {from: 'cart'}});
    },
    handleCheckout() {
      if (this.multipleSelection.length === 0) return this.$message.warning("请至少选择一件商品");
      if (!this.address && this.user.address) this.address = this.user.address;
      this.orderVisible = true;
    },
    submitOrder() {
      if (!this.address) return this.$message.warning("请填写收货地址");
      this.orderLoading = true;
      const request = {
        userId: this.user.id,
        cartIds: this.multipleSelection.map(item => item.id),
        address: this.address
      };
      this.$http.post('/cart/checkout', request).then(res => {
        if (res.data) {
          this.$message.success("下单成功！");
          this.orderVisible = false;
          this.loadCart();
          setTimeout(() => this.$router.push('/front/orders'), 1000);
        }
      }).finally(() => {
        this.orderLoading = false;
      });
    }
  }
};
</script>

<style scoped>
.cart-page {
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

.cart-header {
  margin-bottom: 25px;
}

.header-nav {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
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

.header-title {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-top: 15px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-title .count {
  font-size: 16px;
  color: #999;
  font-weight: normal;
}

.cart-content {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03);
  padding: 30px;
  min-height: 500px;
}

.cart-table {
  margin-bottom: 30px;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 15px;
  cursor: pointer;
}

.p-img {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid #eee;
}

.p-name {
  font-weight: bold;
  color: #333;
  font-size: 15px;
  margin-bottom: 5px;
}

.p-price {
  color: #999;
  font-size: 13px;
}

.qty-control {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.qty-num {
  font-weight: bold;
  width: 30px;
  text-align: center;
}

.subtotal {
  color: #ff6b6b;
  font-weight: bold;
  font-size: 16px;
}

.del-btn {
  color: #999;
}

.del-btn:hover {
  color: #ff6b6b;
}

.cart-footer-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.right {
  display: flex;
  align-items: center;
  gap: 30px;
}

.total-info {
  text-align: right;
}

.total-text {
  font-size: 16px;
  margin-left: 20px;
}

.total-price {
  font-size: 24px;
  color: #ff6b6b;
  font-weight: bold;
}

.checkout-btn {
  padding: 12px 40px;
  font-size: 16px;
  background: linear-gradient(90deg, #ff9f43, #ff6b6b);
  border: none;
}

.checkout-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.empty-cart {
  text-align: center;
  padding: 80px 0;
}

.empty-cart img {
  width: 150px;
  opacity: 0.5;
  margin-bottom: 20px;
}

.empty-cart h2 {
  color: #333;
  margin-bottom: 10px;
}

.empty-cart p {
  color: #999;
  margin-bottom: 30px;
}

/* 订单确认弹窗 */
.order-confirm-list {
  padding: 10px 0;
}

.oc-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px dashed #eee;
}

.oc-right small {
  margin-left: 10px;
  color: #ff9f43;
  font-weight: bold;
}

.oc-total {
  text-align: right;
  margin-top: 20px;
  font-size: 18px;
  font-weight: bold;
}

.oc-total span {
  color: #ff6b6b;
  font-size: 22px;
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
