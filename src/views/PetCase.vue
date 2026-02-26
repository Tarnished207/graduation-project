<template>
  <div class="health-page-wrapper">
    <div class="main-content-area">
      <div class="main-layout">
        <div class="pet-sidebar">
          <div class="sidebar-header">
            <span class="sh-title">我的毛孩子</span>
            <el-button type="text" icon="el-icon-plus" @click="$router.push('/front/pet')">管理</el-button>
          </div>
          <div v-for="pet in myPets" :key="pet.id" class="pet-menu-item" :class="{active: currentPetId===pet.id}"
               @click="switchPet(pet.id)">
            <div class="avatar-box">
              <img :src="getImageUrl(pet.avatar)" class="avatar-img-fit" alt="avatar">
            </div>
            <div class="menu-info">
              <div class="menu-name">{{ pet.nickname }}</div>
              <div class="menu-breed">{{ pet.breed || '未知品种' }}</div>
            </div>
            <div class="active-dot" v-if="currentPetId===pet.id"></div>
          </div>
          <div v-if="myPets.length===0" class="empty-pet-tip">
            <el-empty description="暂无宠物" :image-size="60"></el-empty>
            <el-button type="primary" size="small" plain @click="$router.push('/front/pet')">去添加</el-button>
          </div>
        </div>

        <div class="record-content-wrapper">
          <div class="health-banner fade-in delay-1">
            <div class="hb-text">
              <h2>{{ currentPetName }} 的病例记录 🩺</h2>
              <p>集中记录诊断、处置与随访，便于医生快速了解情况。</p>
            </div>
            <el-button type="primary" icon="el-icon-edit-outline" class="add-btn" @click="openAdd">新增病例</el-button>
          </div>

          <div class="timeline-area fade-in delay-3">
            <div v-if="cases.length>0">
              <el-timeline>
                <el-timeline-item v-for="c in cases" :key="c.id" :timestamp="c.createTime | fmtDate" size="large"
                                  color="#409EFF">
                  <el-card class="record-card">
                    <div class="rc-header">
                      <div class="rc-title-group">
                        <el-tag type="primary" effect="dark" size="small" class="rc-tag">病例</el-tag>
                        <span class="rc-title-text">{{ c.diagnosis || '未填写诊断' }}</span>
                      </div>
                      <div class="rc-cost" v-if="c.cost && c.cost>0"><i class="el-icon-wallet"></i> ¥{{ c.cost }}</div>
                    </div>
                    <div class="rc-metrics">
                      <div class="metric-item" v-if="c.hospital"><i
                          class="el-icon-location-outline"></i><span>{{ c.hospital }}</span></div>
                      <div class="metric-item next" v-if="c.nextTime"><i
                          class="el-icon-bell"></i><span>随访: {{ c.nextTime | fmtDateSimple }}</span></div>
                      <div class="metric-item weight" v-if="c.weight"><i class="fas fa-weight"></i><span>{{ c.weight }} kg</span>
                      </div>
                    </div>
                    <div class="rc-desc">
                      <div v-if="c.symptoms"><b>主诉/症状：</b>{{ c.symptoms }}</div>
                      <div v-if="c.treatment"><b>处置方案：</b>{{ c.treatment }}</div>
                      <div v-if="c.prescription"><b>处方药物：</b>{{ c.prescription }}</div>
                    </div>
                    <div class="rc-footer">
                      <div class="rc-spacer"></div>
                      <div class="rc-actions">
                        <el-button type="text" icon="el-icon-delete" class="del-btn" @click="delCase(c.id)">删除
                        </el-button>
                      </div>
                    </div>
                  </el-card>
                </el-timeline-item>
              </el-timeline>
            </div>
            <div v-else class="empty-state-styled">
              <div class="empty-inner">
                <i class="el-icon-notebook-2 empty-icon"></i>
                <div class="empty-title">暂无病例</div>
                <el-button plain round size="medium" icon="el-icon-edit" @click="openAdd" class="empty-btn">新增病例
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog title="新增病例" :visible.sync="dialogVisible" width="600px" custom-class="record-dialog">
      <el-form :model="form" label-width="100px" size="medium">
        <el-form-item label="诊断">
          <el-input v-model="form.diagnosis" placeholder="如：胃肠炎"></el-input>
        </el-form-item>
        <el-form-item label="主诉/症状">
          <el-input type="textarea" v-model="form.symptoms" rows="2"></el-input>
        </el-form-item>
        <el-form-item label="处置方案">
          <el-input type="textarea" v-model="form.treatment" rows="2"></el-input>
        </el-form-item>
        <el-form-item label="处方药物">
          <el-input v-model="form.prescription" placeholder="可填写药品与用法"></el-input>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="记录日期">
              <el-date-picker type="date" v-model="form.createTime" :clearable="false"
                              style="width:100%"></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="下次随访">
              <el-date-picker type="date" v-model="form.nextTime" style="width:100%"></el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="医院/地点">
              <el-input v-model="form.hospital"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="体重(kg)">
              <el-input v-model="form.weight" type="number" min="0"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="花费(元)">
          <el-input v-model="form.cost" type="number" min="0"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submitCase">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'PetCase',
  data() {
    return {
      user: {},
      myPets: [],
      currentPetId: null,
      cases: [],
      dialogVisible: false,
      form: {
        diagnosis: '', symptoms: '', treatment: '', prescription: '',
        createTime: new Date(), nextTime: null, hospital: '', weight: null, cost: null
      }
    }
  },
  filters: {
    fmtDate(val) {
      return val ? val.substring(0, 10) : '';
    },
    fmtDateSimple(val) {
      return val ? val.substring(0, 10) : '';
    }
  },
  created() {
    const u = sessionStorage.getItem("user");
    if (u) {
      this.user = JSON.parse(u);
      this.loadPets();
    } else {
      this.$router.push("/login");
    }
  },
  computed: {
    currentPetName() {
      if (!this.currentPetId) return '';
      const pet = this.myPets.find(p => p.id === this.currentPetId);
      return pet ? pet.nickname : '';
    }
  },
  methods: {
    getImageUrl(url) {
      if (!url) return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';
      if (url.startsWith('http')) return url;
      if (url.startsWith('/')) return 'http://localhost:9090' + url;
      return 'http://localhost:9090/images/' + url;
    },
    loadPets() {
      this.$http.get('/petInfo/list/' + this.user.id).then(res => {
        const list = res.data.data || res.data;
        this.myPets = list || [];
        if (this.myPets.length > 0) {
          this.currentPetId = this.myPets[0].id;
          this.loadCases();
        }
      });
    },
    switchPet(id) {
      this.currentPetId = id;
      this.loadCases();
    },
    loadCases() {
      if (!this.currentPetId) return;
      this.$http.get('/petCase/list/' + this.currentPetId).then(res => {
        this.cases = res.data && (res.data.data || res.data) || [];
      });
    },
    openAdd() {
      if (!this.currentPetId) {
        this.$message.warning("请先添加宠物");
        return;
      }
      this.form = {
        diagnosis: '', symptoms: '', treatment: '', prescription: '',
        createTime: new Date(), nextTime: null, hospital: '', weight: null, cost: null
      };
      this.dialogVisible = true;
    },
    submitCase() {
      if (!this.currentPetId) {
        this.$message.warning("请先选择宠物");
        return;
      }
      const data = JSON.parse(JSON.stringify(this.form));
      data.petId = this.currentPetId;
      // 格式化日期
      const fmt = d => {
        const y = d.getFullYear();
        const m = (d.getMonth() + 1).toString().padStart(2, '0');
        const dd = d.getDate().toString().padStart(2, '0');
        return `${y}-${m}-${dd}`;
      };
      if (data.createTime) data.createTime = fmt(new Date(data.createTime));
      if (data.nextTime) data.nextTime = fmt(new Date(data.nextTime));
      this.$http.post('/petCase/add', data).then(res => {
        if (res.data) {
          this.$message.success("病例已保存");
          this.dialogVisible = false;
          this.loadCases();
        } else {
          this.$message.error("保存失败");
        }
      }).catch(() => this.$message.error("提交失败，请检查输入"));
    },
    delCase(id) {
      this.$confirm('确定删除这条病例吗？').then(() => {
        this.$http.get('/petCase/delete/' + id).then(res => {
          if (res.data) {
            this.$message.success("已删除");
            this.loadCases();
          }
        });
      });
    }
  }
}
</script>

<style scoped>
@import '../assets/css/user-home.css';
</style>
