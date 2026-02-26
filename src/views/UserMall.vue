<template>
  <div class="user-mall-layout">

    <div class="main-container">
      <div class="mall-dashboard fade-in">

        <div class="side-panel">

          <div class="category-widget">
            <div class="cat-title">商品分类</div>
            <div class="cat-list">
              <div class="cat-item" :class="{active: currentCategory === ''}" @click="filterCategory('')">
                <span>🛍️ 全部商品</span>
                <i class="el-icon-arrow-right" v-if="currentCategory === ''"></i>
              </div>
              <div class="cat-item"
                   v-for="cat in mallCategories"
                   :key="cat.key"
                   :class="{active: currentCategory === cat.key}"
                   @click="filterCategory(cat.key)">
                <span>{{ cat.icon }} {{ cat.name }}</span>
                <i class="el-icon-arrow-right" v-if="currentCategory === cat.key"></i>
              </div>
            </div>
          </div>

        </div>

        <div class="main-content">

          <div class="mall-toolbar">
            <div class="toolbar-left">
              <div class="sort-box">
                <span :class="{active: sortType==='default'}" @click="handleSort('default')">综合</span>
                <span :class="{active: sortType==='price'}" @click="handleSort('price')">价格 <i
                    class="el-icon-d-caret"></i></span>
              </div>
              <div class="search-box">
                <i class="el-icon-search"></i>
                <input type="text" v-model="searchKeyword" placeholder="搜索商品名称..." @keyup.enter="handleSearch">
              </div>
            </div>

            <div class="cart-entry">
              <el-button type="warning" plain round icon="el-icon-shopping-cart-full"
                         @click="$router.push('/front/cart')">
                购物车
                <span class="cart-badge" v-if="cartList.length">{{ cartList.length }}</span>
              </el-button>
            </div>
          </div>

          <div class="pet-toolbar" v-if="currentCategory === 'pet_rec'">
            <span>选择宠物：</span>
            <el-select v-model="selectedPetId" placeholder="选择宠物" @change="loadRecommendByPet" size="small"
                       style="width:220px;">
              <el-option v-for="p in pets" :key="p.id" :label="p.nickname || p.name || ('宠物#'+p.id)"
                         :value="p.id"></el-option>
            </el-select>
            <span class="pet-tip" v-if="pets.length===0"
                  style="margin-left:10px;color:#999;">暂无宠物档案，先去“我的宠物”创建吧</span>
          </div>

          <div class="product-grid" v-loading="loading">
            <div class="product-card" v-for="prod in pagedProducts" :key="prod.id" @click="openDetail(prod)">
              <div class="img-wrapper">
                <img :src="getImageUrl(prod.img || prod.image)" class="p-img">
                <div class="p-tag" v-if="prod.stock < 10">仅剩{{ prod.stock }}件</div>
                <div class="reason-tag" v-if="prod.recommendReason">
                  <i class="fas fa-thumbs-up"></i> {{ prod.recommendReason }}
                </div>
              </div>
              <div class="p-info">
                <div class="p-name" :title="prod.name">{{ prod.name }}</div>
                <div class="p-desc">{{ prod.description || '暂无描述' }}</div>
                <div class="p-bottom">
                  <div class="price-group">
                    <span class="currency">¥</span>
                    <span class="amount">{{ prod.price }}</span>
                  </div>
                  <button class="add-btn" @click.stop="addToCart(prod)" :disabled="prod.stock <= 0">
                    <i class="el-icon-shopping-cart-2"></i>
                  </button>
                </div>
              </div>
            </div>

            <div class="empty-state" v-if="filteredProducts.length === 0">
              <img src="https://cdn-icons-png.flaticon.com/512/7486/7486754.png" style="width: 80px; opacity: 0.5;">
              <p>暂无相关商品</p>
            </div>
          </div>

          <div class="pagination-box" v-if="filteredProducts.length > 0">
            <el-pagination
                background
                layout="prev, pager, next"
                :total="filteredProducts.length"
                :page-size="pageSize"
                :current-page.sync="pageNum"
                @current-change="handlePageChange">
            </el-pagination>
          </div>

        </div>
      </div>
    </div>

  </div>
</template>

<script>
export default {
  name: "UserMall",
  data() {
    return {
      user: JSON.parse(sessionStorage.getItem("user") || '{}'),
      allProducts: [],
      cartList: [],
      loading: false,
      cartLoading: false,
      searchKeyword: '',
      currentCategory: '',
      sortType: 'default',

      // 宠物推荐相关
      pets: [],
      selectedPetId: null,
      recommendProducts: [],

      pageNum: 1,
      mallCategories: [
        {name: '营养主粮', key: 'food', icon: '🐱'},
        {name: '美味零食', key: 'snack', icon: '🍖'},
        {name: '趣味玩具', key: 'toy', icon: '🎾'},
        {name: '日常用品', key: 'daily', icon: '🏠'},
        {name: '医疗保健', key: 'medical', icon: '💊'},
        {name: '宠物器械', key: 'device', icon: '🔧'},
        {name: '宠物专属推荐', key: 'pet_rec', icon: '🐾'}
      ],
      pageSize: 12
    };
  },
  computed: {
    filteredProducts() {
      // 宠物专属推荐模式：直接使用推荐结果
      if (this.currentCategory === 'pet_rec') {
        let list = this.recommendProducts || [];
        if (this.searchKeyword) {
          list = list.filter(p => p.name.includes(this.searchKeyword));
        }
        let sortedList = [...list];
        if (this.sortType === 'price') {
          sortedList.sort((a, b) => a.price - b.price);
        }
        return sortedList;
      }
      // 常规模式：从所有商品中筛选
      let list = this.allProducts;
      if (this.currentCategory) {
        const categoryMap = {
          'food': ['food', '主粮', '营养主粮'],
          'snack': ['snack', '零食', '美味零食'],
          'toy': ['toy', '玩具', '趣味玩具'],
          'daily': ['daily', '日用', '日常用品', '用品'],
          'medical': ['medical', '医疗', '医疗保健'],
          'device': ['device', '器械', '宠物器械']
        };
        const validValues = categoryMap[this.currentCategory] || [this.currentCategory];
        list = list.filter(p => validValues.includes(p.category) || p.category === this.currentCategory);
      }
      if (this.searchKeyword) {
        list = list.filter(p => p.name.includes(this.searchKeyword));
      }
      let sortedList = [...list];
      if (this.sortType === 'price') {
        sortedList.sort((a, b) => a.price - b.price);
      }
      return sortedList;
    },
    pagedProducts() {
      const start = (this.pageNum - 1) * this.pageSize;
      return this.filteredProducts.slice(start, start + this.pageSize);
    }
  },
  created() {
    if (!this.user.id) return this.$router.push("/login");
    this.address = this.user.address || '';
    this.loadProducts();
    this.loadCart();
  },
  methods: {
    getImageUrl(url) {
      if (!url) return 'http://localhost:9090/images/default.png';
      if (url.startsWith('http')) return url;
      if (url.startsWith('/')) return 'http://localhost:9090' + url;
      return 'http://localhost:9090/images/' + url;
    },
    loadProducts() {
      this.loading = true;
      this.$http.get('/product/list', {params: {name: this.searchKeyword}}).then(res => {
        this.allProducts = res.data.data || res.data || [];
        this.loading = false;
      }).catch(() => this.loading = false);
    },
    // 加载当前用户的宠物列表
    loadPets() {
      // 后端为路径参数风格：/petInfo/list/{userId}
      this.$http.get('/petInfo/list/' + this.user.id).then(res => {
        this.pets = res.data.data || res.data || [];
        if (this.pets.length > 0) {
          if (!this.selectedPetId) this.selectedPetId = this.pets[0].id;
          this.loadRecommendByPet();
        } else {
          this.recommendProducts = [];
        }
      });
    },
    // 根据选中宠物拉取专属推荐
    loadRecommendByPet() {
      if (!this.selectedPetId) {
        this.recommendProducts = [];
        return;
      }
      this.loading = true;
      this.$http.get('/product/recommend/pet', {
        params: {userId: this.user.id, petId: this.selectedPetId}
      }).then(res => {
        this.recommendProducts = res.data.data || res.data || [];
        this.pageNum = 1;
      }).finally(() => {
        this.loading = false;
      });
    },
    handleSearch() {
      this.currentCategory = '';
      this.loadProducts();
    },
    loadCart() {
      this.$http.get('/cart/list/' + this.user.id).then(res => {
        this.cartList = res.data || [];
      });
    },
    openDetail(prod) {
      this.$router.push({path: '/front/product/' + prod.id, query: {from: 'mall'}});
    },

    consultService(prod) {
      this.$root.$emit('open-consult', prod);
    },

    addToCart(product, num = 1) {
      const cartItem = {userId: this.user.id, productId: product.id, quantity: num};
      this.$http.post('/cart/add', cartItem).then(res => {
        if (res.data) {
          this.$message.success("已加入购物车");
          this.loadCart();
        }
      });
    },

    filterCategory(key) {
      this.currentCategory = key;
      this.pageNum = 1;
      if (key === 'pet_rec') {
        this.loadPets();
      }
    },
    handleSort(type) {
      this.sortType = type;
    },
    handlePageChange(page) {
      this.pageNum = page;
    },
    getCategoryName(cat) {
      const map = {
        'food': '营养主粮', 'snack': '美味零食', 'toy': '趣味玩具',
        'daily': '日常用品', 'medical': '医疗保健', 'device': '宠物器械'
      };
      return map[cat] || cat;
    }
  }
};
</script>

<style scoped>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css');
@import url('../assets/css/user-home.css');

.user-mall-layout {
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

.mall-dashboard {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 25px;
}

/* 左侧栏 */
.side-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.category-widget {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.03);
}

.cat-title {
  font-weight: bold;
  margin-bottom: 15px;
  color: #333;
  font-size: 16px;
  border-left: 4px solid #ff9f43;
  padding-left: 10px;
}

.cat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  color: #666;
  transition: 0.2s;
  font-size: 14px;
  margin-bottom: 5px;
}

.cat-item:hover {
  background: #fffcf5;
  color: #ff9f43;
}

.cat-item.active {
  background: #fff8e1;
  color: #ff9f43;
  font-weight: bold;
}

/* 右侧内容 */
.main-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.mall-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  padding: 15px 20px;
  border-radius: 16px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.03);
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.search-box {
  display: flex;
  align-items: center;
  background: #f5f7fa;
  padding: 10px 15px;
  border-radius: 20px;
  width: 260px;
}

.search-box i {
  color: #999;
  margin-right: 10px;
}

.search-box input {
  border: none;
  background: transparent;
  outline: none;
  width: 100%;
  font-size: 14px;
  color: #555;
}

.sort-box span {
  margin-right: 20px;
  font-size: 14px;
  color: #888;
  cursor: pointer;
  transition: 0.2s;
}

.sort-box span:last-child {
  margin-right: 0;
}

.sort-box span:hover, .sort-box span.active {
  color: #ff9f43;
  font-weight: bold;
}

.cart-entry {
  position: relative;
}

.cart-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background-color: #f56c6c;
  color: white;
  border-radius: 10px;
  padding: 0 6px;
  font-size: 12px;
  height: 18px;
  line-height: 18px;
  border: 1px solid white;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  min-height: 400px;
  align-content: start;
}

.product-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  transition: 0.3s;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.02);
  border: 1px solid transparent;
  display: flex;
  flex-direction: column;
  cursor: pointer;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.05);
  border-color: #ffecd1;
}

.img-wrapper {
  height: 180px;
  width: 100%;
  position: relative;
  overflow: hidden;
  background: #f9f9f9;
}

.p-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: 0.3s;
}

.product-card:hover .p-img {
  transform: scale(1.05);
}

.p-tag {
  position: absolute;
  top: 10px;
  left: 10px;
  background: #ff6b6b;
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 10px;
}

.p-info {
  padding: 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.p-name {
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  font-size: 15px;
}

.p-desc {
  font-size: 12px;
  color: #999;
  margin-bottom: 10px;
  height: 34px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
}

.p-bottom {
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price-group {
  color: #ff6b6b;
  font-weight: bold;
}

.currency {
  font-size: 12px;
}

.amount {
  font-size: 18px;
}

.add-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #f0f9eb;
  color: #67c23a;
  border: none;
  cursor: pointer;
  transition: 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.add-btn:hover {
  background: #67c23a;
  color: white;
}

.add-btn:disabled {
  background: #eee;
  color: #ccc;
  cursor: not-allowed;
}

.empty-state {
  grid-column: span 4;
  text-align: center;
  padding: 60px 0;
  color: #ccc;
}

.empty-state img {
  width: 100px;
  opacity: 0.5;
  margin-bottom: 15px;
}

.pagination-box {
  text-align: center;
  margin-top: 20px;
}

.order-confirm-list {
  padding: 10px 0;
}

.oc-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px dashed #eee;
  font-size: 14px;
  color: #555;
}

.oc-right small {
  margin-left: 10px;
  color: #ff9f43;
  font-weight: bold;
}

.oc-total {
  text-align: right;
  margin-top: 20px;
  font-size: 16px;
  font-weight: bold;
}

.oc-total span {
  color: #ff6b6b;
  font-size: 20px;
}

.clean-dialog {
  border-radius: 12px !important;
}

.cart-items-scroll::-webkit-scrollbar {
  width: 4px;
}

.cart-items-scroll::-webkit-scrollbar-thumb {
  background: #eee;
  border-radius: 2px;
}
</style>
