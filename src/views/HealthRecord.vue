<template>
  <div class="health-page-wrapper">

    <div class="main-content-area">

      <div class="global-dashboard fade-in">
        <div class="g-card monthly-cost">
          <div class="g-content">
            <div class="g-label">本月全家医疗支出</div>
            <div class="g-val">¥ {{ globalStats.monthCost }}</div>
          </div>
          <i class="bg-icon el-icon-money"></i>
        </div>
        <div class="g-card upcoming-tasks">
          <div class="g-content">
            <div class="g-label">全家近期临近待办</div>
            <div class="g-val">{{ globalStats.upcomingCount }} <span class="unit">项</span></div>
          </div>
          <i class="bg-icon el-icon-alarm-clock"></i>
        </div>
        <div class="g-card sick-count" :class="{ 'is-safe': globalStats.sickCount === 0 }">
          <div class="g-content">
            <div class="g-label">生病/治疗中</div>
            <div class="g-val">{{ globalStats.sickCount }} <span class="unit">只</span></div>
          </div>
          <i class="bg-icon el-icon-first-aid-kit"></i>
        </div>
      </div>

      <div class="main-layout">

        <div class="pet-sidebar">
          <div class="sidebar-header">
            <span class="sh-title">我的毛孩子</span>
            <el-button type="text" icon="el-icon-plus" @click="$router.push('/front/pet')">管理</el-button>
          </div>

          <div
              v-for="pet in myPets"
              :key="pet.id"
              class="pet-menu-item"
              :class="{active: currentPetId === pet.id}"
              @click="switchPet(pet.id)"
          >
            <div class="avatar-box">
              <img :src="getImageUrl(pet.avatar)" class="avatar-img-fit" alt="avatar">
            </div>

            <div class="menu-info">
              <div class="menu-name">{{ pet.nickname }}</div>
              <div class="menu-breed">{{ pet.breed || '未知品种' }}</div>
            </div>

            <div class="active-dot" v-if="currentPetId === pet.id"></div>
          </div>

          <div v-if="myPets.length === 0" class="empty-pet-tip">
            <el-empty description="暂无宠物" :image-size="60"></el-empty>
            <el-button type="primary" size="small" plain @click="$router.push('/front/pet')">去添加</el-button>
          </div>
        </div>

        <div class="record-content-wrapper">

          <div class="health-banner fade-in delay-1">
            <div class="hb-text">
              <h2>{{
                  activeTab === 'health' ? (currentPetName + ' 的健康档案 🩺') : (currentPetName + ' 的病例记录 🩺')
                }}</h2>
              <p>{{
                  activeTab === 'health' ? '科学记录每一次就诊、疫苗与驱虫，守护爱宠健康成长。' : '集中记录诊断、处置与随访，便于医生快速了解情况。'
                }}</p>
            </div>
            <div class="hb-actions">
              <el-button :type="activeTab==='health' ? 'primary' : 'default'" size="mini" @click="activeTab='health'">
                健康档案
              </el-button>
              <el-button :type="activeTab==='case' ? 'primary' : 'default'" size="mini" @click="activeTab='case'">
                宠物病例
              </el-button>
              <el-button type="primary" icon="el-icon-edit-outline" class="add-btn" @click="openAdd">
                {{ activeTab === 'health' ? '新增记录' : '新增病例' }}
              </el-button>
            </div>
          </div>

          <div class="pet-summary-bar fade-in delay-2" v-if="currentPetId">
            <div class="ps-item">
              <span class="ps-label">历史总花费</span>
              <span class="ps-val cost">¥{{ summaryStats.totalCost }}</span>
            </div>
            <div class="v-line"></div>
            <div class="ps-item">
              <span class="ps-label">最新体重</span>
              <span class="ps-val">{{ summaryStats.latestWeight }} kg</span>
            </div>
            <div class="v-line"></div>
            <div class="ps-item">
              <span class="ps-label">{{ nextLabel }}</span>
              <el-tooltip :content="nextDetail" placement="top" effect="dark" :disabled="!nextDetail">
                <span class="ps-val date" :class="{urgent: summaryStats.isUrgent}">{{
                    summaryStats.nextAppoint || '无日程'
                  }}</span>
              </el-tooltip>
            </div>
            <div class="v-line"></div>
            <div class="ps-item">
              <span class="ps-label">记录数</span>
              <span class="ps-val">{{ summaryStats.count }}</span>
            </div>
          </div>

          <div class="timeline-area fade-in delay-3" v-if="activeTab==='health'">
            <div v-if="records.length > 0">
              <el-timeline>
                <el-timeline-item
                    v-for="rec in records"
                    :key="rec.id"
                    :timestamp="rec.createTime | fmtDate"
                    placement="top"
                    :color="getTypeColor(rec.type)"
                    size="large"
                >
                  <el-card class="record-card">
                    <div class="rc-header">
                      <div class="rc-title-group">
                        <el-tag :type="getTypeTag(rec.type)" effect="dark" size="small" class="rc-tag">{{
                            rec.type
                          }}
                        </el-tag>
                        <span class="rc-title-text">{{ rec.title || '未填写项目' }}</span>
                      </div>
                      <div class="rc-cost" v-if="rec.cost && rec.cost > 0">
                        <i class="el-icon-wallet"></i> ¥{{ rec.cost }}
                      </div>
                    </div>

                    <div class="rc-metrics">
                      <div class="metric-item weight" v-if="rec.weight">
                        <i class="fas fa-weight"></i>
                        <span>{{ rec.weight }} kg</span>
                      </div>
                      <div class="metric-item next" v-if="rec.nextTime">
                        <i class="el-icon-bell"></i>
                        <span>下次: {{ rec.nextTime | fmtDateSimple }}</span>
                      </div>
                      <div class="metric-item" v-if="rec.hospital">
                        <i class="el-icon-location-outline"></i>
                        <span>{{ rec.hospital }}</span>
                      </div>
                    </div>

                    <div class="rc-desc" v-if="rec.description">
                      {{ rec.description }}
                    </div>

                    <div class="rc-imgs" v-if="rec.img">
                      <el-image
                          v-for="(url,i) in rec.img.split(',')"
                          :key="i"
                          :src="getImageUrl(url)"
                          :preview-src-list="getPreviewList(rec.img)"
                          class="rc-thumb"
                      ></el-image>
                    </div>

                    <div class="rc-footer">
                      <div class="rc-doctor" v-if="rec.doctor" style="color:#909399; font-size:13px;">
                        <span>执行人: {{ rec.doctor }}</span>
                      </div>
                      <div class="rc-spacer" v-else></div>
                      <div class="rc-actions">
                        <el-button type="text" icon="el-icon-delete" class="del-btn" @click="delRecord(rec.id)">删除
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
                <div class="empty-title">暂无健康记录</div>
                <div class="empty-desc">
                  <!--                  还没有数据哦，快去给 <span class="pet-name">{{ currentPetName }}</span> 记一笔吧 ~-->
                </div>
                <el-button plain round size="medium" icon="el-icon-edit" @click="openAdd" class="empty-btn">
                  立即记录
                </el-button>
              </div>
            </div>
          </div>

          <div class="timeline-area fade-in delay-3" v-if="activeTab==='case'">
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

    <el-dialog title="新增记录" :visible.sync="dialogVisible" width="550px" custom-class="record-dialog">
      <el-form :model="form" label-width="90px" size="medium">
        <el-form-item label="记录类型">
          <el-radio-group v-model="form.type" size="small">
            <el-radio-button label="疫苗"></el-radio-button>
            <el-radio-button label="驱虫"></el-radio-button>
            <el-radio-button label="生病"></el-radio-button>
            <el-radio-button label="洗护"></el-radio-button>
            <el-radio-button label="体检"></el-radio-button>
            <el-radio-button label="日常"></el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="项目标题">
          <el-input v-model="form.title" placeholder="简要描述，如：妙三多第二针、洗澡..."></el-input>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="体重(kg)">
              <el-input v-model="form.weight" type="number" min="0" placeholder="选填"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="花费(元)">
              <el-input v-model="form.cost" type="number" min="0" placeholder="选填"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="记录日期">
              <el-date-picker
                  type="date"
                  placeholder="选择日期"
                  v-model="form.createTime"
                  :clearable="false"
                  style="width: 100%;">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="下次提醒">
              <el-date-picker
                  type="date"
                  placeholder="选填"
                  v-model="form.nextTime"
                  :picker-options="nextTimePickerOptions"
                  style="width: 100%;">
              </el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="详细备注">
          <el-input type="textarea" v-model="form.description" rows="2"
                    placeholder="记录医嘱、症状、药物用法..."></el-input>
        </el-form-item>

        <div class="optional-section-title">以下信息选填</div>

        <el-form-item label="医院/地点">
          <el-input v-model="form.hospital" prefix-icon="el-icon-location-outline" placeholder="选填"></el-input>
        </el-form-item>

        <el-form-item label="执行人员">
          <el-input v-model="form.doctorName" placeholder="请输入姓名">
            <el-select v-model="form.doctorRole" slot="prepend" placeholder="请选择" style="width: 90px;">
              <el-option label="医师" value="医师"></el-option>
              <el-option label="美容师" value="美容师"></el-option>
              <el-option label="其他" value="其他"></el-option>
            </el-select>
          </el-input>
        </el-form-item>

      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRecord">保存</el-button>
      </span>
    </el-dialog>

    <el-dialog title="新增病例" :visible.sync="caseDialogVisible" width="600px" custom-class="record-dialog">
      <el-form :model="caseForm" label-width="100px" size="medium">
        <el-form-item label="诊断">
          <el-input v-model="caseForm.diagnosis"></el-input>
        </el-form-item>
        <el-form-item label="主诉/症状">
          <el-input type="textarea" v-model="caseForm.symptoms" rows="2"></el-input>
        </el-form-item>
        <el-form-item label="处置方案">
          <el-input type="textarea" v-model="caseForm.treatment" rows="2"></el-input>
        </el-form-item>
        <el-form-item label="处方药物">
          <el-input v-model="caseForm.prescription"></el-input>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="记录日期">
              <el-date-picker type="date" v-model="caseForm.createTime" :clearable="false"
                              style="width:100%"></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="下次随访">
              <el-date-picker type="date" v-model="caseForm.nextTime" style="width:100%"></el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="医院/地点">
              <el-input v-model="caseForm.hospital"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="体重(kg)">
              <el-input v-model="caseForm.weight" type="number" min="0"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="花费(元)">
          <el-input v-model="caseForm.cost" type="number" min="0"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="caseDialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submitCase">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "HealthRecord",
  data() {
    return {
      user: {},
      myPets: [],
      currentPetId: null,
      records: [],
      cases: [],
      allFamilyRecords: [],
      allFamilyCases: [],
      dialogVisible: false,
      caseDialogVisible: false,
      form: {
        type: '疫苗', title: '', weight: '', cost: '',
        createTime: new Date(), nextTime: '',
        description: '', hospital: '', doctor: '', img: '',
        doctorRole: '医师', doctorName: ''
      },
      caseForm: {
        diagnosis: '', symptoms: '', treatment: '', prescription: '',
        createTime: new Date(), nextTime: null, hospital: '', weight: null, cost: null
      },
      activeTab: 'health'
    }
  },
  computed: {
    currentPetName() {
      if (!this.currentPetId) return '';
      const pet = this.myPets.find(p => p.id === this.currentPetId);
      // 【关键修复】使用 nickname
      return pet ? pet.nickname : '';
    },
    // 【关键修复】确保依赖 createTime
    nextTimePickerOptions() {
      // 显式声明依赖，强迫 Vue 在 createTime 变化时重新计算这个对象
      const dependency = this.form.createTime;
      const self = this;

      return {
        disabledDate(time) {
          const startDate = self.form.createTime ? new Date(self.form.createTime) : new Date();
          startDate.setHours(0, 0, 0, 0);
          return time.getTime() < startDate.getTime();
        }
      };
    },
    stats() {
      if (!this.records || this.records.length === 0) {
        return {count: 0, totalCost: 0, latestWeight: '-', nextAppoint: '', isUrgent: false};
      }
      let cost = 0;
      this.records.forEach(r => {
        if (r.cost) cost += Number(r.cost);
      });
      let weight = this.latestWeightForCurrentPet;
      let next = '无日程';
      let isUrgent = false;
      const now = new Date().getTime();
      const futureRecs = this.records
          .filter(r => r.nextTime && new Date(r.nextTime).getTime() > now)
          .sort((a, b) => new Date(a.nextTime).getTime() - new Date(b.nextTime).getTime());

      if (futureRecs.length > 0) {
        next = this.$options.filters.fmtDateSimple(futureRecs[0].nextTime);
        if (new Date(futureRecs[0].nextTime).getTime() - now < 7 * 24 * 3600 * 1000) {
          isUrgent = true;
        }
      }
      return {
        count: this.records.length,
        totalCost: cost.toFixed(0),
        latestWeight: weight,
        nextAppoint: next,
        isUrgent
      };
    },
    summaryStats() {
      if (this.activeTab === 'case') {
        if (!this.cases || this.cases.length === 0) {
          return {count: 0, totalCost: 0, latestWeight: '-', nextAppoint: '', isUrgent: false};
        }
        let cost = 0;
        this.cases.forEach(c => {
          if (c.cost) cost += Number(c.cost);
        });
        let weight = this.latestWeightForCurrentPet;
        let next = '无日程';
        let isUrgent = false;
        const now = new Date().getTime();
        const futureCases = this.cases
            .filter(c => c.nextTime && new Date(c.nextTime).getTime() > now)
            .sort((a, b) => new Date(a.nextTime).getTime() - new Date(b.nextTime).getTime());
        if (futureCases.length > 0) {
          next = this.$options.filters.fmtDateSimple(futureCases[0].nextTime);
          if (new Date(futureCases[0].nextTime).getTime() - now < 7 * 24 * 3600 * 1000) {
            isUrgent = true;
          }
        }
        return {
          count: this.cases.length,
          totalCost: cost.toFixed(0),
          latestWeight: weight,
          nextAppoint: next,
          isUrgent
        };
      }
      return this.stats;
    },
    latestWeightForCurrentPet() {
      const pet = this.myPets.find(p => p.id === this.currentPetId) || {};
      let latest = pet.weight ? parseFloat(pet.weight) : null;
      let latestTime = 0;
      (this.records || []).forEach(r => {
        if (r.weight) {
          const t = new Date(r.createTime).getTime() || 0;
          if (t >= latestTime) {
            latestTime = t;
            latest = parseFloat(r.weight);
          }
        }
      });
      (this.cases || []).forEach(c => {
        if (c.weight) {
          const t = new Date(c.createTime).getTime() || 0;
          if (t >= latestTime) {
            latestTime = t;
            latest = parseFloat(c.weight);
          }
        }
      });
      return latest === null || latest === undefined ? '-' : latest;
    },
    nextLabel() {
      return this.activeTab === 'case' ? '下一次随访' : '下一次提醒';
    },
    nextDetail() {
      const now = new Date().getTime();
      if (this.activeTab === 'case') {
        const futureCases = (this.cases || [])
            .filter(c => c.nextTime && new Date(c.nextTime).getTime() > now)
            .sort((a, b) => new Date(a.nextTime).getTime() - new Date(b.nextTime).getTime());
        if (futureCases.length === 0) return '';
        const c = futureCases[0];
        const title = c.diagnosis || '';
        return title ? `随访于该病例: ${title}` : '随访';
      } else {
        const futureRecs = (this.records || [])
            .filter(r => r.nextTime && new Date(r.nextTime).getTime() > now)
            .sort((a, b) => new Date(a.nextTime).getTime() - new Date(b.nextTime).getTime());
        if (futureRecs.length === 0) return '';
        const r = futureRecs[0];
        const t = r.type || '';
        const title = r.title || '';
        const joined = [t, title].filter(Boolean).join(' · ');
        return joined ? `来自: ${joined}` : '';
      }
    },
    globalStats() {
      let totalMonthCost = 0;
      let upcomingCount = 0;
      let sickCount = 0;
      const now = new Date();
      const currentMonth = now.getMonth();
      const currentYear = now.getFullYear();

      this.myPets.forEach(p => {
        if (p.status === 1 || p.status === 2 || p.healthStatus === 1 || p.healthStatus === 2) sickCount++;
      });

      this.allFamilyRecords.forEach(rec => {
        const d = new Date(rec.createTime);
        if (rec.cost && d.getMonth() === currentMonth && d.getFullYear() === currentYear) {
          totalMonthCost += Number(rec.cost);
        }
        if (rec.nextTime) {
          const t = new Date(rec.nextTime).getTime();
          const diff = t - now.getTime();
          if (diff > 0 && diff < 7 * 24 * 3600 * 1000) {
            upcomingCount++;
          }
        }
      });
      this.allFamilyCases.forEach(c => {
        const d = new Date(c.createTime);
        if (c.cost && d.getMonth() === currentMonth && d.getFullYear() === currentYear) {
          totalMonthCost += Number(c.cost);
        }
        if (c.nextTime) {
          const t = new Date(c.nextTime).getTime();
          const diff = t - now.getTime();
          if (diff > 0 && diff < 7 * 24 * 3600 * 1000) {
            upcomingCount++;
          }
        }
      });
      return {monthCost: totalMonthCost.toFixed(0), upcomingCount, sickCount};
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
      const t = this.$route.query.tab;
      if (t === 'case') this.activeTab = 'case';
    } else {
      this.$router.push("/login");
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
          this.loadRecords();
          this.loadCases();
          this.fetchAllRecords();
          this.fetchAllCases();
        }
      }).catch(() => {
        this.myPets = [];
        this.$message.error('加载宠物列表失败');
      });
    },
    async fetchAllRecords() {
      if (!this.myPets.length) return;
      const promises = this.myPets.map(pet => this.$http.get('/healthRecord/list/' + pet.id));
      try {
        const results = await Promise.all(promises);
        let all = [];
        results.forEach(res => {
          if (res.data) all = all.concat(res.data);
        });
        this.allFamilyRecords = all;
      } catch (e) {
        console.error(e);
      }
    },
    async fetchAllCases() {
      if (!this.myPets.length) return;
      const promises = this.myPets.map(pet => this.$http.get('/petCase/list/' + pet.id));
      try {
        const results = await Promise.all(promises);
        let all = [];
        results.forEach(res => {
          const list = res.data && (res.data.data || res.data) || [];
          all = all.concat(list);
        });
        this.allFamilyCases = all;
      } catch (e) {
        console.error(e);
      }
    },
    switchPet(id) {
      this.currentPetId = id;
      this.loadRecords();
      this.loadCases();
    },
    loadRecords() {
      if (!this.currentPetId) return;
      this.$http.get('/healthRecord/list/' + this.currentPetId).then(res => {
        this.records = res.data;
      }).catch(() => {
        this.records = [];
        this.$message.error('加载健康记录失败');
      });
    },
    loadCases() {
      if (!this.currentPetId) return;
      this.$http.get('/petCase/list/' + this.currentPetId).then(res => {
        this.cases = res.data && (res.data.data || res.data) || [];
      }).catch(() => {
        this.cases = [];
        this.$message.error('加载病例失败');
      });
    },
    openAdd() {
      if (!this.currentPetId) {
        this.$message.warning("请先添加宠物");
        return;
      }
      if (this.activeTab === 'health') {
        this.form = {
          type: '疫苗', title: '', weight: '', cost: '',
          createTime: new Date(), nextTime: '',
          description: '', hospital: '', doctor: '', img: '',
          doctorRole: '医师', doctorName: ''
        };
        this.dialogVisible = true;
      } else {
        this.caseForm = {
          diagnosis: '', symptoms: '', treatment: '', prescription: '',
          createTime: new Date(), nextTime: null, hospital: '', weight: null, cost: null
        };
        this.caseDialogVisible = true;
      }
    },

    submitRecord() {
      if (!this.currentPetId) {
        this.$message.warning("请先选择宠物");
        return;
      }
      let formData = JSON.parse(JSON.stringify(this.form));
      formData.petId = this.currentPetId;

      if (formData.weight !== null && formData.weight < 0) {
        this.$message.warning("体重不能为负数");
        return;
      }
      if (formData.cost !== null && formData.cost < 0) {
        this.$message.warning("花费不能为负数");
        return;
      }

      if (formData.createTime) {
        formData.createTime = this.formatTimeFull(new Date(formData.createTime));
      } else {
        formData.createTime = this.formatTimeFull(new Date());
      }

      if (formData.nextTime) {
        const nextDate = new Date(formData.nextTime);
        const recordDate = this.form.createTime ? new Date(this.form.createTime) : new Date();
        recordDate.setHours(0, 0, 0, 0);
        nextDate.setHours(0, 0, 0, 0);

        if (nextDate < recordDate) {
          this.$message.warning("下次提醒日期必须晚于本次记录日期");
          return;
        }
        formData.nextTime = this.formatDateSimple(nextDate);
      } else {
        formData.nextTime = null;
      }

      if (this.form.doctorName) {
        formData.doctor = (this.form.doctorRole || '') + ' ' + this.form.doctorName;
      } else {
        formData.doctor = '';
      }

      formData.weight = formData.weight ? parseFloat(formData.weight) : null;
      formData.cost = formData.cost ? parseFloat(formData.cost) : null;

      this.$http.post('/healthRecord/add', formData).then(res => {
        if (res.data) {
          this.$message.success("记录成功！");
          this.dialogVisible = false;
          this.loadRecords();
          this.fetchAllRecords();
        } else {
          this.$message.error("保存失败");
        }
      }).catch(err => {
        this.$message.error("提交失败，请检查输入");
      });
    },

    delRecord(id) {
      this.$confirm('确定删除这条记录吗？').then(() => {
        this.$http.get('/healthRecord/delete/' + id).then(res => {
          if (res.data) {
            this.$message.success("已删除");
            this.loadRecords();
            this.fetchAllRecords();
          }
        }).catch(() => {
          this.$message.error('删除失败');
        });
      }).catch(() => {
      });
    },
    submitCase() {
      if (!this.currentPetId) {
        this.$message.warning("请先选择宠物");
        return;
      }
      let data = JSON.parse(JSON.stringify(this.caseForm));
      data.petId = this.currentPetId;
      const fmt = d => {
        const y = d.getFullYear();
        const m = (d.getMonth() + 1).toString().padStart(2, '0');
        const dd = d.getDate().toString().padStart(2, '0');
        return `${y}-${m}-${dd}`;
      };
      // createTime 按后端要求发送到 “yyyy-MM-dd HH:mm:ss”
      if (data.createTime) {
        data.createTime = this.formatTimeFull(new Date(data.createTime));
      } else {
        data.createTime = this.formatTimeFull(new Date());
      }
      if (data.nextTime) data.nextTime = fmt(new Date(data.nextTime));
      // 数值字段转为 number，避免字符串导致后端类型绑定失败
      data.weight = data.weight ? parseFloat(data.weight) : null;
      data.cost = data.cost ? parseFloat(data.cost) : null;
      this.$http.post('/petCase/add', data).then(res => {
        if (res.data) {
          this.$message.success("病例已保存");
          this.caseDialogVisible = false;
          this.loadCases();
          this.fetchAllCases();
        } else {
          this.$message.error("保存失败");
        }
      }).catch(() => {
        this.$message.error("提交失败，请检查输入");
      });
    },
    delCase(id) {
      this.$confirm('确定删除这条病例吗？').then(() => {
        this.$http.get('/petCase/delete/' + id).then(res => {
          if (res.data) {
            this.$message.success("已删除");
            this.loadCases();
            this.fetchAllCases();
          }
        }).catch(() => {
          this.$message.error('删除失败');
        });
      }).catch(() => {
      });
    },
    getTypeColor(type) {
      const map = {
        '疫苗': '#67C23A',
        '生病': '#F56C6C',
        '驱虫': '#E6A23C',
        '洗护': '#409EFF',
        '体检': '#909399',
        '日常': '#333'
      };
      return map[type] || '#909399';
    },
    getTypeTag(type) {
      const map = {
        '疫苗': 'success',
        '生病': 'danger',
        '驱虫': 'warning',
        '洗护': 'primary',
        '体检': 'info',
        '日常': ''
      };
      return map[type] || 'info';
    },
    getPreviewList(imgStr) {
      return imgStr ? imgStr.split(',').map(url => this.getImageUrl(url)) : [];
    },

    formatTimeFull(date) {
      const y = date.getFullYear();
      const m = (date.getMonth() + 1).toString().padStart(2, '0');
      const d = date.getDate().toString().padStart(2, '0');
      return `${y}-${m}-${d} 00:00:00`;
    },
    formatDateSimple(date) {
      const y = date.getFullYear();
      const m = (date.getMonth() + 1).toString().padStart(2, '0');
      const d = date.getDate().toString().padStart(2, '0');
      return `${y}-${m}-${d}`;
    }
  }
}
</script>

<style scoped>
@import '../assets/css/user-home.css';

/* 页面背景 */
.health-page-wrapper {
  min-height: 100vh;
  background-color: #f7f9fc;
  background-image: radial-gradient(#e3e8ee 1px, transparent 1px);
  background-size: 20px 20px;
  padding-top: 80px;
}

/* 主容器 */
.main-content-area {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;
}

/* === 全局健康看板 === */
.global-dashboard {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 25px;
  margin-bottom: 30px;
}
.g-card {
  background: white;
  border-radius: 16px;
  padding: 20px 30px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.04);
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  transition: 0.3s;
}

.g-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.08);
}
.g-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 20px;
  flex-shrink: 0;
}

.g-info {
  z-index: 2;
}

.g-label {
  font-size: 13px;
  color: #888;
  margin-bottom: 5px;
}

.g-val {
  font-size: 24px;
  font-weight: 800;
  color: #333;
}

.g-val .unit {
  font-size: 14px;
  font-weight: normal;
  color: #999;
}

.bg-icon {
  position: absolute;
  right: -10px;
  bottom: -10px;
  font-size: 100px;
  opacity: 0.05;
  transform: rotate(-15deg);
}

.monthly-cost .g-icon {
  background: #fff5e6;
  color: #ff9f43;
}

.monthly-cost {
  border-bottom: 4px solid #ff9f43;
}

.upcoming-tasks .g-icon {
  background: #e6f7ff;
  color: #409eff;
}

.upcoming-tasks {
  border-bottom: 4px solid #409eff;
}

.sick-count .g-icon {
  background: #fef0f0;
  color: #f56c6c;
}

.sick-count {
  border-bottom: 4px solid #f56c6c;
}

/* === 主布局 === */
.main-layout {
  display: flex;
  gap: 30px;
  align-items: flex-start;
}

/* 左侧：宠物列表 */
.pet-sidebar {
  width: 280px;
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.04);
  flex-shrink: 0;
  position: sticky;
  top: 100px;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.sh-title {
  font-weight: 800;
  font-size: 18px;
  color: #333;
}

.pet-menu-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  margin-bottom: 12px;
  background: #f9f9f9;
  transition: 0.2s;
  position: relative;
  border: 1px solid transparent;
}

.pet-menu-item:hover {
  background: #f0f0f0;
}

.pet-menu-item.active {
  background: #fff;
  border-color: #ff9f43;
  box-shadow: 0 4px 15px rgba(255, 159, 67, 0.15);
}

/* 头像容器 */
.avatar-box {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  margin-right: 12px;
  border: 1px solid #ddd;
}
.avatar-img-fit {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.menu-info {
  flex: 1;
  overflow: hidden;
}

.menu-name {
  font-weight: bold;
  font-size: 15px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.menu-breed {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.active-dot {
  width: 8px;
  height: 8px;
  background: #ff9f43;
  border-radius: 50%;
  position: absolute;
  right: 15px;
}

.empty-pet-tip {
  padding: 20px 0;
}

/* 右侧内容 */
.record-content-wrapper {
  flex: 1;
  min-width: 0;
}

.health-banner {
  background: linear-gradient(120deg, #a1c4fd 0%, #c2e9fb 100%);
  border-radius: 16px;
  padding: 25px 30px;
  color: #1e3648;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  box-shadow: 0 8px 20px rgba(161, 196, 253, 0.3);
}

.hb-text h2 {
  margin: 0 0 5px 0;
  font-size: 24px;
  font-weight: 800;
}

.hb-text p {
  margin: 0;
  opacity: 0.8;
  font-size: 14px;
}

.add-btn {
  background: white;
  color: #409eff;
  border: none;
  font-weight: bold;
  box-shadow: 0 4px 10px rgba(255, 255, 255, 0.5);
}

.add-btn:hover {
  background: #f0f8ff;
  color: #409eff;
}

.pet-summary-bar {
  background: white;
  border-radius: 12px;
  padding: 15px 25px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.03);
  margin-bottom: 25px;
  display: flex;
  align-items: center;
  justify-content: space-around;
}

.ps-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.ps-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.ps-val {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.ps-val.cost {
  color: #ff9f43;
}

.ps-val.date {
  color: #666;
  font-size: 16px;
}

.ps-val.date.urgent {
  color: #f56c6c;
  font-weight: bold;
}

.v-line {
  width: 1px;
  height: 30px;
  background: #eee;
}

.record-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.rc-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  border-bottom: 1px dashed #eee;
  padding-bottom: 12px;
}

.rc-title-text {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-left: 8px;
}

.rc-cost {
  color: #F56C6C;
  font-weight: bold;
}

.rc-metrics {
  display: flex;
  gap: 15px;
  margin-bottom: 12px;
}

.metric-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: #666;
  background: #f5f7fa;
  padding: 4px 10px;
  border-radius: 4px;
}

.metric-item.weight {
  color: #409EFF;
  background: #ecf5ff;
}

.metric-item.next {
  color: #E6A23C;
  background: #fdf6ec;
}

.rc-desc {
  background: #fafafa;
  padding: 10px;
  border-radius: 6px;
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
  line-height: 1.6;
}

.rc-imgs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 10px;
}

.rc-thumb {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  border: 1px solid #eee;
}

.rc-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.rc-doctor {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: #666;
}

.del-btn {
  color: #ccc;
}

.del-btn:hover {
  color: #f56c6c;
}

.fade-in {
  animation: fadeIn 0.6s ease backwards;
}

.delay-1 {
  animation-delay: 0.1s;
}

.delay-2 {
  animation-delay: 0.2s;
}

.delay-3 {
  animation-delay: 0.3s;
}

/* --- 新增空状态样式 --- */
.empty-state-styled {
  background: #fff;
  border-radius: 12px;
  border: 2px dashed #f0f0f0; /* 虚线边框增加设计感 */
  padding: 60px 20px;
  text-align: center;
  margin-top: 20px;
  transition: all 0.3s;
}

.empty-state-styled:hover {
  border-color: #ff9f43;
  background: #fffcf5; /* 鼠标悬停微微泛黄 */
}

.empty-icon {
  font-size: 56px;
  color: #ffc048; /* 使用主题橙色 */
  margin-bottom: 15px;
  background: #fff5e6;
  padding: 20px;
  border-radius: 50%;
}

.empty-title {
  font-size: 20px;
  font-weight: 800; /* 加粗字体 */
  color: #333;
  margin-bottom: 10px;
  letter-spacing: 1px;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.optional-section-title {
  margin: 15px 0 10px;
  font-size: 12px;
  color: #999;
  border-top: 1px dashed #eee;
  padding-top: 15px;
  text-align: center;
}
</style>
