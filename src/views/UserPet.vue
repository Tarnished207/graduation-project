<template>
  <div class="user-pet-layout">

    <div class="main-container">

      <div class="family-dashboard fade-in">
        <div class="dash-item title-card">
          <div class="texts">
            <h2>萌宠家族 🐾</h2>
            <p>陪伴是最长情的告白</p>
          </div>
          <div class="stats-mini">
            <span><b>{{ myStats.total }}</b> 成员</span>
            <i class="dot"></i>
            <span><b>{{ myStats.dogCount }}</b> 汪</span>
            <i class="dot"></i>
            <span><b>{{ myStats.catCount }}</b> 喵</span>
          </div>
        </div>

        <div class="dash-item add-action-card" @click="handleAdd">
          <span class="plus-icon">+</span>
          <span class="add-text">登记新成员</span>
        </div>
      </div>

      <div class="filter-strip fade-in delay-1">
        <div class="filter-left">

          <div class="filter-group">
            <span class="f-label">物种</span>
            <div class="tab-options">
              <span class="tab-item" :class="{active: filters.type===''}" @click="setFilter('type', '')">全部</span>
              <span class="tab-item" :class="{active: filters.type===0}" @click="setFilter('type', 0)">🐶 汪星人</span>
              <span class="tab-item" :class="{active: filters.type===1}" @click="setFilter('type', 1)">🐱 喵星人</span>
            </div>
          </div>

          <div class="v-divider"></div>

          <div class="filter-group">
            <span class="f-label">状态</span>
            <div class="tab-options">
              <span class="tab-item" :class="{active: filters.health===''}" @click="setFilter('health', '')">全部</span>
              <span class="tab-item health-safe" :class="{active: filters.health===0}" @click="setFilter('health', 0)">
                <i class="el-icon-sunny"></i> 活蹦乱跳
              </span>
              <span class="tab-item health-treat" :class="{active: filters.health===2}" @click="setFilter('health', 2)">
                <i class="el-icon-first-aid-kit"></i> 治疗恢复
              </span>
              <span class="tab-item health-sick" :class="{active: filters.health===1}" @click="setFilter('health', 1)">
                <i class="el-icon-warning-outline"></i> 生病护理
              </span>
            </div>
          </div>

        </div>

        <div class="search-box">
          <i class="el-icon-search"></i>
          <input v-model="filters.name" placeholder="搜索昵称..." @input="handleFilter">
        </div>
      </div>

      <div class="pet-cards-grid fade-in delay-2" v-if="filteredData.length > 0">
        <div class="fun-card" v-for="pet in filteredData" :key="pet.id">
          <div class="card-cover" @click="handleEdit(pet)">
            <img :src="getImageUrl(pet.avatar)" class="cover-img">
            <div class="overlay"></div>
            <div class="status-tag" :class="getHealthClass(pet.healthStatus)">
              {{ getHealthText(pet.healthStatus) }}
            </div>
          </div>

          <div class="card-info">
            <div class="main-row">
              <div class="name-wrap">
                <span class="name">{{ pet.nickname }}</span>
                <i v-if="pet.sex===0" class="el-icon-male gender-icon male"></i>
                <i v-else class="el-icon-female gender-icon female"></i>
              </div>
              <span class="breed-badge">{{ pet.breed || '神秘物种' }}</span>
            </div>

            <div class="data-row">
              <div class="d-item">
                <span class="val">{{ pet.age }}<small>岁</small></span>
              </div>
              <div class="d-line"></div>
              <div class="d-item">
                <span class="val">{{ latestWeightMap[pet.id] || pet.weight }}<small>kg</small></span>
              </div>
              <div class="d-line"></div>
              <div class="d-item">
                <span class="val">{{ pet.sterilization===1?'已绝育':'未绝育' }}</span>
              </div>
            </div>

            <div class="action-bar">
              <el-tooltip content="查看详细健康记录" placement="top">
                <div class="health-link" @click="$router.push('/front/health')">
                  <i class="el-icon-data-line"></i> 健康档案
                </div>
              </el-tooltip>
              <el-tooltip content="查看病例记录" placement="top">
                <div class="health-link" @click="$router.push({ path: '/front/health', query: { tab: 'case' } })">
                  <i class="el-icon-first-aid-kit"></i> 宠物病例
                </div>
              </el-tooltip>
              <div class="right-btns">
                <button class="icon-btn" @click="handleEdit(pet)"><i class="el-icon-edit"></i></button>
                <el-popconfirm title="要把这个小可爱移除吗？" @confirm="handleDelete(pet.id)">
                  <button slot="reference" class="icon-btn del"><i class="el-icon-delete"></i></button>
                </el-popconfirm>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="empty-layout fade-in">

        <div v-if="originData.length === 0">
          <img src="https://cdn-icons-png.flaticon.com/512/7486/7486747.png" class="empty-img">
          <h3>还没登记任何毛孩子</h3>
          <p>点击右上角的按钮，开始记录美好的陪伴时光吧 ~</p>
          <button class="big-add-btn" @click="handleAdd">去登记一个</button>
        </div>

        <div v-else>
          <img src="https://cdn-icons-png.flaticon.com/512/6134/6134065.png" class="empty-img" style="width: 120px; opacity: 0.6;">
          <h3>没有找到符合条件的毛孩子</h3>
          <p>您目前没有处于该状态的宠物，这是好事！</p>
          <button class="big-add-btn plain" @click="resetFilter">清除筛选条件</button>
        </div>

      </div>

    </div>

    <el-dialog
        :visible.sync="dialogVisible"
        width="550px"
        custom-class="fun-dialog"
        :close-on-click-modal="false"
        center>

      <span slot="title" class="dialog-title">
         {{ form.id ? '编辑档案' : '欢迎新成员 🎉' }}
      </span>

      <div class="dialog-body">
        <div class="avatar-upload-center">
          <el-upload
              class="avatar-uploader"
              action="http://localhost:9090/file/upload"
              :show-file-list="false"
              :on-success="handleUploadSuccess">
            <div class="avatar-preview" :class="{hasImg: form.avatar}">
              <img v-if="form.avatar" :src="getImageUrl(form.avatar)">
              <div v-else class="placeholder">
                <i class="el-icon-camera-solid"></i>
                <span>上传萌照</span>
              </div>
            </div>
          </el-upload>
        </div>

        <el-form :model="form" label-position="top" size="small">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="昵称">
                <el-input v-model="form.nickname" placeholder="叫什么名字呢?" class="fun-input"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="品种">
                <el-input v-model="form.breed" placeholder="例如: 柯基" class="fun-input"></el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <div class="radio-row">
            <el-form-item label="身份">
              <el-radio-group v-model="form.type" size="small" fill="#ff9f43">
                <el-radio-button :label="0">🐶 汪星人</el-radio-button>
                <el-radio-button :label="1">🐱 喵星人</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="性别">
              <el-radio-group v-model="form.sex" size="small" fill="#ff9f43">
                <el-radio-button :label="0">♂ 男孩</el-radio-button>
                <el-radio-button :label="1">♀ 女孩</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </div>

          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="年龄 (岁)">
                <el-input-number v-model="form.age" :min="0" :controls="false" style="width:100%" class="fun-input"></el-input-number>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="体重 (kg)">
                <el-input-number v-model="form.weight" :min="0" :precision="1" :step="0.1" :controls="false" style="width:100%" class="fun-input"></el-input-number>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="是否绝育">
                <el-select v-model="form.sterilization" placeholder="请选择">
                  <el-option label="未绝育" :value="0"></el-option>
                  <el-option label="已绝育" :value="1"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <div class="health-section">
            <p class="sec-title"><i class="el-icon-first-aid-kit"></i> 健康信息</p>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="生日">
                  <el-date-picker v-model="form.birthday" type="date" value-format="yyyy-MM-dd" placeholder="选择生日" style="width:100%"></el-date-picker>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="下次疫苗时间">
                  <el-date-picker v-model="form.nextVaccine" type="date" value-format="yyyy-MM-dd" placeholder="设置提醒" style="width:100%"></el-date-picker>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="当前状态">
              <el-radio-group v-model="form.healthStatus" size="small">
                <el-radio-button :label="0">☀️ 活蹦乱跳</el-radio-button>
                <el-radio-button :label="2">💊 治疗恢复</el-radio-button>
                <el-radio-button :label="1">🏥 生病护理</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </div>

        </el-form>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false" round>取消</el-button>
        <el-button type="primary" @click="save" round class="save-btn-fun">保存档案</el-button>
      </span>
    </el-dialog>

  </div>
</template>

<script>
export default {
  name: "UserPet",
  data() {
    return {
      user: JSON.parse(sessionStorage.getItem("user") || '{}'),
      originData: [], // 原始全量数据
      filteredData: [],
      myStats: { total: 0, dogCount: 0, catCount: 0, avgAge: 0 },

      filters: { name: '', type: '', health: '' },
      dialogVisible: false,
      form: {},
      latestWeightMap: {}
    }
  },
  created() {
    if (!this.user.id) return this.$router.push("/login");
    this.load();
  },
  methods: {
    getImageUrl(url) {
      if (!url) return 'https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png';
      if (url.startsWith('http')) return url;
      if (url.startsWith('/')) return 'http://localhost:9090' + url;
      return 'http://localhost:9090/images/' + url;
    },
    load() {
      this.$http.get('/petInfo/list/' + this.user.id).then(res => {
        this.originData = res.data || [];
        this.originData.forEach(p => {
          if (p && p.birthday) {
            const age = this.computeAgeYears(p.birthday);
            this.$set(p, 'age', age);
          }
        });
        this.handleFilter();
        this.calculateStats();
        this.computeLatestWeights();
      });
    },

    computeAgeYears(birthdayStr) {
      const b = new Date(birthdayStr);
      if (isNaN(b.getTime())) return 0;
      const t = new Date();
      let age = t.getFullYear() - b.getFullYear();
      const m = t.getMonth() - b.getMonth();
      if (m < 0 || (m === 0 && t.getDate() < b.getDate())) age--;
      return age < 0 ? 0 : age;
    },

    calculateStats() {
      const list = this.originData;
      let dog = 0, cat = 0, totalAge = 0;
      list.forEach(p => {
        if (p.type === 0) dog++; else cat++;
        if (p.age) totalAge += p.age;
      });
      this.myStats = {
        total: list.length,
        dogCount: dog,
        catCount: cat,
        avgAge: list.length ? (totalAge / list.length).toFixed(1) : 0
      };
    },

    setFilter(key, val) {
      this.filters[key] = val;
      this.handleFilter();
    },
    resetFilter() {
      this.filters = { name: '', type: '', health: '' };
      this.handleFilter();
    },
    handleFilter() {
      const { name, type, health } = this.filters;
      this.filteredData = this.originData.filter(item => {
        const matchName = !name || (item.nickname && item.nickname.includes(name));
        const matchType = type === '' || item.type === type;
        const matchHealth = health === '' || item.healthStatus === health;
        return matchName && matchType && matchHealth;
      });
    },
    async computeLatestWeights() {
      const pets = this.originData || [];
      const promises = pets.map(p => Promise.all([
        this.$http.get('/healthRecord/list/' + p.id),
        this.$http.get('/petCase/list/' + p.id)
      ]).then(([recRes, caseRes]) => {
        const recs = recRes.data || [];
        const cases = caseRes.data && (caseRes.data.data || caseRes.data) || [];
        let latest = null;
        let latestTime = 0;
        recs.forEach(r => {
          if (r.weight) {
            const t = new Date(r.createTime).getTime() || 0;
            if (t >= latestTime) {
              latestTime = t;
              latest = parseFloat(r.weight);
            }
          }
        });
        cases.forEach(c => {
          if (c.weight) {
            const t = new Date(c.createTime).getTime() || 0;
            if (t >= latestTime) {
              latestTime = t;
              latest = parseFloat(c.weight);
            }
          }
        });
        if (!latest && p.weight) latest = parseFloat(p.weight);
        this.$set(this.latestWeightMap, p.id, latest || null);
      }).catch(() => {
        this.$set(this.latestWeightMap, p.id, p.weight || null);
      }));
      await Promise.all(promises);
    },

    getHealthText(status) { return {0:'活蹦乱跳', 1:'生病护理', 2:'治疗恢复'}[status] || '状态未知'; },
    getHealthClass(status) { return {0:'tag-green', 1:'tag-red', 2:'tag-yellow'}[status] || 'tag-gray'; },

    handleAdd() {
      this.form = { type: 0, sex: 0, weight: 4.0, healthStatus: 0, sterilization: 0 };
      this.dialogVisible = true;
    },
    handleEdit(row) {
      this.form = JSON.parse(JSON.stringify(row));
      this.dialogVisible = true;
    },
    save() {
      this.form.userId = this.user.id;
      if (this.form.birthday) {
        const by = new Date(this.form.birthday).getFullYear();
        const cy = new Date().getFullYear();
        this.form.age = cy - by;
      }

      let url = this.form.id ? '/petInfo/update' : '/petInfo/add';
      this.$http.post(url, this.form).then(res => {
        if(res.data) {
          this.$message.success("保存成功");
          this.dialogVisible = false;
          this.load();
        } else this.$message.error("保存失败");
      })
    },
    handleDelete(id) {
      this.$http.get('/petInfo/delete/' + id).then(res => {
        if(res.data) {
          this.$message.success("已删除");
          this.load();
        }
      })
    },
    handleUploadSuccess(res) {
      this.$set(this.form, 'avatar', res);
    }
  }
}
</script>

<style scoped>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css');
@import '../assets/css/user-home.css';

.user-pet-layout {
  min-height: 100vh;
  background-color: #f7f9fb;
  padding-top: 80px;
  font-family: "Varela Round", "Noto Sans SC", sans-serif;
  color: #333;
}
.main-container { max-width: 1200px; margin: 0 auto; padding: 40px 20px; }

/* === 1. 家庭看板 === */
.family-dashboard {
  display: grid;
  grid-template-columns: 1fr 240px;
  gap: 25px;
  margin-bottom: 25px;
}

.dash-item {
  background: white; border-radius: 20px; padding: 30px;
  box-shadow: 0 8px 25px rgba(0,0,0,0.03); transition: 0.3s;
  display: flex; align-items: center;
}
.dash-item:hover { transform: translateY(-3px); box-shadow: 0 15px 30px rgba(0,0,0,0.06); }

.title-card {
  justify-content: space-between;
  background: linear-gradient(135deg, #fffcf5 0%, #fff 100%);
  border: 1px solid #fffbf0;
}
.texts h2 { margin: 0 0 5px 0; font-size: 26px; color: #333; font-weight: 800; }
.texts p { margin: 0; color: #999; font-size: 14px; }

.stats-mini { display: flex; gap: 15px; align-items: center; font-size: 13px; color: #666; background: #fdfdfd; padding: 8px 15px; border-radius: 20px; border: 1px solid #f0f0f0; }
.stats-mini b { font-size: 16px; color: #ff9f43; margin-right: 2px; }
.dot { width: 3px; height: 3px; background: #ddd; border-radius: 50%; }

.add-action-card {
  background: linear-gradient(135deg, #ff9f43, #ffc048);
  color: white; cursor: pointer; justify-content: center; gap: 10px;
}
.add-action-card:hover { filter: brightness(1.05); }
.plus-icon { font-size: 24px; font-weight: bold; }
.add-text { font-size: 16px; font-weight: bold; }

/* === 2. 平铺筛选栏 (修复对齐问题) === */
.filter-strip {
  background: white; border-radius: 16px; padding: 12px 25px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.02); display: flex;
  align-items: center; justify-content: space-between; margin-bottom: 30px;
}
.filter-left { display: flex; gap: 40px; align-items: center; }
.v-divider { width: 1px; height: 20px; background: #eee; }

/* 关键修复：高度对齐 */
.filter-group { display: flex; align-items: center; gap: 12px; height: 32px; }
.f-label { font-size: 13px; font-weight: bold; color: #999; line-height: 32px; /* 强制垂直居中 */ }

.tab-options { display: flex; gap: 5px; height: 32px; align-items: center; }
.tab-item {
  font-size: 13px; cursor: pointer; color: #666; transition: 0.2s;
  padding: 0 10px; height: 28px; line-height: 28px; /* 显式高度 */
  border-radius: 4px; display: flex; align-items: center; gap: 4px;
}
.tab-item:hover { color: #333; background: #f5f5f5; }
.tab-item.active { color: #333; font-weight: bold; background: #f0f0f0; }

/* 健康状态特别色 */
.health-safe.active { color: #67c23a; background: #f0f9eb; }
.health-treat.active { color: #e6a23c; background: #fdf6ec; }
.health-sick.active { color: #ff6b6b; background: #fef0f0; }

.search-box {
  display: flex; align-items: center; gap: 10px;
  background: #f9f9f9; padding: 0 15px; height: 36px; /* 显式高度 */
  border-radius: 18px; border: 1px solid #eee; width: 220px; transition: 0.3s;
}
.search-box:focus-within { background: white; border-color: #ff9f43; box-shadow: 0 0 0 3px rgba(255,159,67,0.1); }
.search-box input { border: none; background: transparent; outline: none; width: 100%; color: #333; height: 100%; }
.search-box i { color: #ccc; }

/* === 3. 趣味卡片 === */
.pet-cards-grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 30px; padding-bottom: 50px;
}

.fun-card {
  background: white; border-radius: 20px; overflow: hidden;
  box-shadow: 0 8px 25px rgba(0,0,0,0.04); transition: 0.3s;
  display: flex; flex-direction: column;
}
.fun-card:hover { transform: translateY(-8px); box-shadow: 0 20px 40px rgba(0,0,0,0.08); }

.card-cover { height: 180px; position: relative; overflow: hidden; cursor: pointer; }
.cover-img { width: 100%; height: 100%; object-fit: cover; transition: 0.5s; }
.fun-card:hover .cover-img { transform: scale(1.05); }
.overlay {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%;
  background: linear-gradient(to top, rgba(0,0,0,0.4), transparent 60%);
}

.status-tag {
  position: absolute; top: 12px; right: 12px;
  padding: 4px 10px; border-radius: 6px; font-size: 11px; font-weight: bold;
  backdrop-filter: blur(5px); color: white;
}
.tag-green { background: rgba(103, 194, 58, 0.9); }
.tag-red { background: rgba(245, 108, 108, 0.9); }
.tag-yellow { background: rgba(230, 162, 60, 0.9); }
.tag-gray { background: rgba(144, 147, 153, 0.9); }

.card-info { padding: 20px; flex: 1; display: flex; flex-direction: column; }
.main-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.name-wrap { display: flex; align-items: center; gap: 6px; }
.name { font-size: 18px; font-weight: 800; color: #333; }
.gender-icon { font-size: 12px; padding: 2px; border-radius: 50%; color: white; width: 16px; height: 16px; display: flex; align-items: center; justify-content: center; }
.male { background: #409eff; } .female { background: #ff6b6b; }
.breed-badge { font-size: 11px; background: #f0f2f5; color: #909399; padding: 2px 8px; border-radius: 6px; }

.data-row {
  display: flex; justify-content: space-between; margin-bottom: 20px;
  align-items: center;
  flex-wrap: nowrap;
  background: #fcfcfc; padding: 10px 15px; border-radius: 10px;
}

.d-item {
  display: flex;
  align-items: baseline;
  gap: 4px;
  flex: 0 0 auto;
  white-space: nowrap;
}
.d-item .val { font-size: 14px; font-weight: bold; color: #333; }
.d-item small { font-size: 11px; font-weight: normal; color: #999; }
.d-line { width: 1px; height: 16px; background: #eee; }

.action-bar {
  display: flex; justify-content: space-between; align-items: center;
  border-top: 1px dashed #eee; padding-top: 15px; margin-top: auto;
}
.health-link { font-size: 12px; color: #409eff; cursor: pointer; display: flex; align-items: center; gap: 4px; transition: 0.2s; }
.health-link:hover { color: #66b1ff; text-decoration: underline; }

.icon-btn { border: none; background: #f5f7fa; color: #666; width: 32px; height: 32px; border-radius: 8px; cursor: pointer; transition: 0.2s; display: flex; align-items: center; justify-content: center; }
.icon-btn:hover { background: #eef1f6; color: #409eff; }
.icon-btn.del:hover { background: #fef0f0; color: #ff6b6b; }
.right-btns { display: flex; gap: 8px; }

/* 空状态 */
.empty-layout { text-align: center; padding: 80px 0; }
.empty-img { width: 150px; opacity: 0.8; margin-bottom: 20px; }
.empty-layout p { color: #999; margin-bottom: 20px; }
.big-add-btn {
  background: linear-gradient(90deg, #ff9f43, #ffc048); color: white; border: none;
  padding: 12px 40px; border-radius: 30px; font-size: 16px; font-weight: bold;
  cursor: pointer; box-shadow: 0 5px 15px rgba(255, 159, 67, 0.4); transition: 0.2s;
}
.big-add-btn.plain {
  background: white; border: 1px solid #ff9f43; color: #ff9f43; box-shadow: none;
}
.big-add-btn.plain:hover { background: #fff5e6; }
.big-add-btn:hover { transform: translateY(-3px); }

/* 弹窗 */
.fun-dialog { border-radius: 16px !important; overflow: hidden; }
.avatar-upload-center { display: flex; justify-content: center; margin-bottom: 25px; }
.avatar-preview {
  width: 110px; height: 110px; border-radius: 50%; border: 3px solid #fff;
  box-shadow: 0 5px 15px rgba(0,0,0,0.1); background: #f9f9f9;
  display: flex; align-items: center; justify-content: center; overflow: hidden; cursor: pointer;
}
.avatar-preview:hover { border-color: #ff9f43; }
.avatar-preview img { width: 100%; height: 100%; object-fit: cover; }
.placeholder { text-align: center; color: #ccc; }
.placeholder i { font-size: 24px; display: block; margin-bottom: 5px; }

.fun-input >>> .el-input__inner { border-radius: 10px; background: #f5f7fa; border: 1px solid #eee; }
.fun-input >>> .el-input__inner:focus { background: white; border-color: #ff9f43; }
.radio-row { display: flex; gap: 20px; margin-bottom: 5px; }
.health-section { background: #fffcf5; padding: 15px; border-radius: 12px; border: 1px dashed #ffe0b2; margin-top: 10px; }
.sec-title { font-size: 12px; font-weight: bold; color: #ff9f43; margin-bottom: 10px; }

.save-btn-fun { background: #ff9f43; border-color: #ff9f43; box-shadow: 0 4px 10px rgba(255,159,67,0.3); }

/* 动画 */
.fade-in { animation: fadeIn 0.6s ease; }
.delay-1 { animation-delay: 0.1s; }
.delay-2 { animation-delay: 0.2s; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
</style>
