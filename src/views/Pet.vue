<template>
  <div class="page-container">

    <div class="stats-container">
      <div class="stat-card main-card">
        <div class="main-card-content">
          <div class="icon-circle"><i class="el-icon-collection-tag"></i></div>
          <div class="text-group">
            <div class="label">萌宠总数</div>
            <div class="number">{{ stats.total || 0 }}</div>
            <div class="tag">
              <i class="el-icon-male"></i> 公 {{ stats.boyCount || 0 }} / <i class="el-icon-female"></i> 母 {{ (stats.total - stats.boyCount) || 0 }}
            </div>
          </div>
        </div>
        <i class="el-icon-collection-tag bg-watermark"></i>
      </div>

      <div class="small-cards-group">
        <div class="stat-card small-card">
          <div class="card-header"><span>汪星人</span><i class="el-icon-medal-1 icon-grey"></i></div>
          <div class="card-body"><div class="card-value blue-text">{{ stats.dogCount || 0 }}</div></div>
          <div class="card-footer"></div>
        </div>
        <div class="stat-card small-card">
          <div class="card-header"><span>喵星人</span><i class="el-icon-moon icon-grey"></i></div>
          <div class="card-body"><div class="card-value pink-text">{{ stats.catCount || 0 }}</div></div>
          <div class="card-footer"></div>
        </div>
        <div class="stat-card small-card">
          <div class="card-header"><span>平均年龄</span><i class="el-icon-time icon-grey"></i></div>
          <div class="card-body"><div class="card-value">{{ stats.avgAge || 0 }} <span style="font-size:14px;color:#999">岁</span></div></div>
          <div class="card-footer"></div>
        </div>
      </div>
    </div>

    <div class="content-panel">
      <div class="toolbar-v2">
        <el-form :inline="true" size="small" class="search-form">
          <div class="search-row">
            <el-form-item label="🔍 搜昵称">
              <el-input v-model="searchName" placeholder="输入宠物名" @keyup.enter.native="load" clearable></el-input>
            </el-form-item>
            <el-form-item label="🐾 物种">
              <el-select v-model="searchType" placeholder="全部" clearable @change="load" style="width: 100px;">
                <el-option label="🐶 狗狗" :value="0"></el-option>
                <el-option label="🐱 猫猫" :value="1"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="🧬 品种">
              <el-input v-model="searchBreed" placeholder="搜品种..." @keyup.enter.native="load" clearable
                        style="width: 130px;"></el-input>
            </el-form-item>
            <el-form-item label="🏥 健康">
              <el-select v-model="searchHealth" placeholder="全部" clearable @change="load" style="width: 110px;">
                <el-option label="🟢 健康" :value="0"></el-option>
                <el-option label="🔴 生病" :value="1"></el-option>
                <el-option label="🟡 治疗" :value="2"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="👤 主人">
              <el-input v-model="searchOwner" placeholder="搜主人姓名..." @keyup.enter.native="load" clearable
                        style="width: 130px;"></el-input>
            </el-form-item>
          </div>
          <div class="search-row second-row">
            <el-form-item label="🎂 年龄区间">
              <div class="age-range">
                <el-input-number v-model="searchAgeMin" :min="0" :max="99" placeholder="最小" :controls="false"
                                 style="width: 60px;"></el-input-number>
                <span class="range-sep">-</span>
                <el-input-number v-model="searchAgeMax" :min="0" :max="99" placeholder="最大" :controls="false"
                                 style="width: 60px;"></el-input-number>
                <span class="unit">岁</span>
              </div>
            </el-form-item>
            <el-form-item class="btn-group">
              <el-button type="primary" icon="el-icon-search" round @click="load">筛选</el-button>
              <el-button icon="el-icon-refresh" round @click="resetSearch">重置</el-button>
              <el-divider direction="vertical"></el-divider>
              <el-button type="warning" round icon="el-icon-message" @click="handleBatchNotice"
                         :disabled="multipleSelection.length === 0">发送关怀
              </el-button>
            </el-form-item>
          </div>
        </el-form>
      </div>

      <el-table
          :data="tableData"
          style="width: 100%"
          class="custom-table"
          @selection-change="handleSelectionChange"
          :header-cell-style="{background:'#f8f9fa', color:'#606266', borderBottom:'1px solid #ebeef5', fontWeight:'600'}"
      >
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column label="宠物信息" min-width="180">
          <template slot-scope="scope">
            <div class="pet-meta">
              <el-image :src="getImageUrl(scope.row.avatar)" class="pet-avatar" fit="cover">
                <div slot="error" class="image-slot"><i class="el-icon-picture-outline"></i></div>
              </el-image>
              <div class="meta-text">
                <div class="p-name">
                  {{ scope.row.nickname }}
                  <i v-if="scope.row.sex === 0" class="el-icon-male sex-icon male"></i>
                  <i v-else class="el-icon-female sex-icon female"></i>
                </div>
                <div class="p-id">ID: {{ scope.row.id }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="物种/品种" min-width="140">
          <template slot-scope="scope">
            <div class="species-box">
              <el-tag v-if="scope.row.type === 0" size="mini" type="primary" effect="light" class="mini-tag">狗</el-tag>
              <el-tag v-else size="mini" type="danger" effect="light" color="#fff0f6" style="border-color:#ffadd2;color:#eb2f96" class="mini-tag">猫</el-tag>
              <span class="breed-text">{{ scope.row.breed || '未知' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="age" label="年龄" min-width="80" align="center">
          <template slot-scope="scope">{{ scope.row.age }} 岁</template>
        </el-table-column>

        <el-table-column label="绝育" min-width="90" align="center">
          <template slot-scope="scope">
            <span v-if="scope.row.sterilization === 1" class="status-text success">已绝育</span>
            <span v-else class="status-text gray">未绝育</span>
          </template>
        </el-table-column>

        <el-table-column label="健康" min-width="100" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.healthStatus === 0" type="success" size="mini" effect="dark" style="border-radius:10px">健康</el-tag>
            <el-tag v-else-if="scope.row.healthStatus === 1" type="danger" size="mini" effect="dark" style="border-radius:10px">生病</el-tag>
            <el-tag v-else-if="scope.row.healthStatus === 2" type="warning" size="mini" effect="dark" style="border-radius:10px">治疗中</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="下次疫苗" min-width="130" align="center">
          <template slot-scope="scope">
            <span :style="isOverdue(scope.row.nextVaccine) ? 'color:#ff4d4f;font-weight:bold' : 'color:#909399;font-size:13px'">
              {{ scope.row.nextVaccine || '-' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="主人信息" min-width="140" show-overflow-tooltip>
          <template slot-scope="scope">
            <div class="owner-info">
              <span class="owner-name">{{ scope.row.ownerName || '未知' }}</span>
              <span class="owner-id">UID: {{ scope.row.userId }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" style="color:#666" @click="handleEdit(scope.row)">编辑</el-button>
            <el-popconfirm title="确定删除这只宠物吗？" @confirm="handleDelete(scope.row.id)">
              <el-button slot="reference" type="text" style="color:#ff6b6b; margin-left:10px">删除</el-button>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
        :title="form.id ? '编辑宠物档案' : '新增宠物'"
        :visible.sync="dialogVisible"
        width="600px"
        custom-class="clean-dialog"
        :close-on-click-modal="false">

      <div class="dialog-body">

        <div class="avatar-section">
          <el-upload
              class="avatar-uploader"
              :action="$baseUrl + '/file/upload'"
              name="file"
              :show-file-list="false"
              :on-success="handleUploadSuccess">
            <div class="avatar-wrapper">
              <img v-if="form.avatar" :src="getImageUrl(form.avatar)" class="avatar">
              <div v-else class="avatar-placeholder">
                <i class="el-icon-camera-solid"></i>
                <span>点击上传</span>
              </div>
            </div>
          </el-upload>
          <div class="avatar-tip">请上传正方形宠物头像</div>
        </div>

        <el-form :model="form" label-position="top" size="medium" class="tidy-form">

          <div class="form-group-label">📋 基本信息</div>
          <div class="form-card">
            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="昵称">
                  <el-input v-model="form.nickname" placeholder="宠物名字"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="主人ID (UID)">
                  <el-input v-model="form.userId" placeholder="关联的用户ID"></el-input>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="品种">
                  <el-input v-model="form.breed" placeholder="如：柯基"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <div class="flex-row">
                  <el-form-item label="物种" style="flex:1">
                    <el-select v-model="form.type" placeholder="选择">
                      <el-option label="🐶 狗" :value="0"></el-option>
                      <el-option label="🐱 猫" :value="1"></el-option>
                    </el-select>
                  </el-form-item>
                  <el-form-item label="性别" style="flex:1; margin-left: 15px;">
                    <el-select v-model="form.sex" placeholder="选择">
                      <el-option label="♂ 公" :value="0"></el-option>
                      <el-option label="♀ 母" :value="1"></el-option>
                    </el-select>
                  </el-form-item>
                </div>
              </el-col>
            </el-row>
          </div>

          <div class="form-group-label" style="margin-top: 20px;">🏥 健康体征</div>
          <div class="form-card health-bg">
            <el-row :gutter="24">
              <el-col :span="8">
                <el-form-item label="年龄 (岁)">
                  <el-input-number v-model="form.age" :min="0" :controls="false" style="width:100%"></el-input-number>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="体重 (kg)">
                  <el-input-number v-model="form.weight" :precision="1" :step="0.1" :controls="false" style="width:100%"></el-input-number>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="健康状态">
                  <el-select v-model="form.healthStatus" placeholder="选择">
                    <el-option label="🟢 健康" :value="0"></el-option>
                    <el-option label="🔴 生病" :value="1"></el-option>
                    <el-option label="🟡 治疗" :value="2"></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="下次疫苗日期">
                  <el-date-picker v-model="form.nextVaccine" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" style="width:100%"></el-date-picker>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="绝育情况">
                  <div style="margin-top: 5px;">
                    <el-radio-group v-model="form.sterilization" size="small">
                      <el-radio-button :label="0">未绝育</el-radio-button>
                      <el-radio-button :label="1">已绝育</el-radio-button>
                    </el-radio-group>
                  </div>
                </el-form-item>
              </el-col>
            </el-row>
          </div>

        </el-form>
      </div>

      <span slot="footer">
        <el-button @click="dialogVisible = false" size="medium">取消</el-button>
        <el-button type="primary" @click="save" class="confirm-btn" size="medium">保存档案</el-button>
      </span>
    </el-dialog>

    <!-- 发送通知对话框 -->
    <el-dialog
        :visible.sync="noticeDialogVisible"
        width="500px"
        append-to-body
        custom-class="notice-dialog-v2">
      <div slot="title" class="dialog-title">
        <i class="el-icon-message-solid"></i>
        <span>健康关怀推送</span>
      </div>

      <div class="notice-container">
        <!-- 收件人预览 -->
        <div class="recipient-preview">
          <div class="rp-header">
            <span class="rp-label">发送对象 ({{ multipleSelection.length }})</span>
            <el-link type="info" :underline="false" @click="multipleSelection=[]" style="font-size:12px">清空已选
            </el-link>
          </div>
          <div class="rp-list">
            <div v-for="p in multipleSelection" :key="p.id" class="rp-item">
              <el-avatar :size="24" :src="getImageUrl(p.avatar)"></el-avatar>
              <span class="rp-name">{{ p.nickname }}</span>
            </div>
          </div>
        </div>

        <el-form :model="noticeForm" label-position="top" class="notice-form">
          <el-form-item label="关怀主题">
            <el-input v-model="noticeForm.title" placeholder="输入推送标题，如：春季防疫小贴士" maxlength="30"
                      show-word-limit></el-input>
          </el-form-item>
          <el-form-item label="详细内容">
            <el-input
                type="textarea"
                :rows="5"
                v-model="noticeForm.content"
                placeholder="在此输入具体的防疫建议、日常护理或体检提醒..."
                maxlength="500"
                show-word-limit>
            </el-input>
          </el-form-item>
        </el-form>

        <div class="notice-tip">
          <i class="el-icon-info"></i>
          消息将通过系统通知实时推送给对应宠物的主人。
        </div>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="noticeDialogVisible = false" round>再想想</el-button>
        <el-button type="warning" @click="sendBatchNotice" :loading="noticeLoading" round icon="el-icon-s-promotion">
          立即推送
        </el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
export default {
  data() {
    return {
      tableData: [],
      stats: { total: 0, dogCount: 0, catCount: 0, boyCount: 0, avgAge: 0 },
      searchName: '',
      searchType: '',
      searchBreed: '',
      searchHealth: '',
      searchOwner: '',
      searchAgeMin: undefined,
      searchAgeMax: undefined,
      dialogVisible: false,
      multipleSelection: [],
      noticeDialogVisible: false,
      noticeLoading: false,
      noticeForm: {title: '', content: ''},
      form: {}
    }
  },
  created() {
    this.load();
    this.loadStats();
  },
  methods: {
    getImageUrl(url) {
      if (!url) return '';
      if (url.startsWith('http')) return url;
      let baseUrl = this.$baseUrl || 'http://localhost:9090';
      if (url.startsWith('/')) return baseUrl + url;
      return baseUrl + '/images/' + url;
    },
    loadStats() {
      this.$http.get('/petInfo/stats').then(res => { this.stats = res.data; });
    },
    load() {
      let params = {
        name: this.searchName,
        type: this.searchType,
        breed: this.searchBreed,
        healthStatus: this.searchHealth,
        ageMin: this.searchAgeMin,
        ageMax: this.searchAgeMax,
        ownerName: this.searchOwner
      };
      this.$http.get('/petInfo/listAll', { params: params }).then(res => {
        this.tableData = res.data;
      });
    },
    resetSearch() {
      this.searchName = '';
      this.searchType = '';
      this.searchBreed = '';
      this.searchHealth = '';
      this.searchOwner = '';
      this.searchAgeMin = undefined;
      this.searchAgeMax = undefined;
      this.load();
    },
    isOverdue(dateStr) {
      if(!dateStr) return false;
      return new Date(dateStr) < new Date();
    },
    handleEdit(row) {
      this.form = JSON.parse(JSON.stringify(row));
      this.dialogVisible = true;
    },
    save() {
      let url = this.form.id ? '/petInfo/update' : '/petInfo/add';
      this.$http.post(url, this.form).then(res => {
        if(res.data) {
          this.$message.success("保存成功");
          this.dialogVisible = false;
          this.load();
          this.loadStats();
        } else {
          this.$message.error("保存失败");
        }
      })
    },
    handleDelete(id) {
      this.$http.get('/petInfo/delete/' + id).then(res => {
        if(res.data) {
          this.$message.success("删除成功");
          this.load();
          this.loadStats();
        }
      })
    },
    handleUploadSuccess(res) {
      this.$set(this.form, 'avatar', res);
    },
    handleSelectionChange(val) {
      this.multipleSelection = val;
    },
    handleBatchNotice() {
      this.noticeForm = {title: '健康关怀提醒', content: ''};
      this.noticeDialogVisible = true;
    },
    sendBatchNotice() {
      if (!this.noticeForm.title || !this.noticeForm.content) {
        this.$message.warning("请填写标题和内容");
        return;
      }
      this.noticeLoading = true;

      // 按用户分组宠物，以便在通知中指明具体宠物名称
      const userPetMap = {};
      this.multipleSelection.forEach(p => {
        if (!userPetMap[p.userId]) userPetMap[p.userId] = [];
        userPetMap[p.userId].push(p.nickname);
      });

      const promises = Object.keys(userPetMap).map(userId => {
        const petNames = userPetMap[userId].join('、');
        // 在内容开头自动加上宠物名称，让用户知道是哪只宠物受到了关怀
        const customizedContent = `【关怀宠物：${petNames}】${this.noticeForm.content}`;
        return this.$http.post('/sysNotice/batchSend', {
          userIds: [userId],
          title: this.noticeForm.title,
          content: customizedContent
        });
      });

      Promise.all(promises).then(results => {
        const allSuccess = results.every(res => res.data.code === '200');
        if (allSuccess) {
          this.$message.success("关怀消息已按主人分组发送成功");
          this.noticeDialogVisible = false;
        } else {
          this.$message.warning("部分消息发送可能存在问题");
        }
        this.noticeLoading = false;
      }).catch(() => {
        this.$message.error("部分消息发送失败");
        this.noticeLoading = false;
      });
    }
  }
}
</script>

<style scoped>
/* 全局基础 */
.page-container {
  padding: 25px;
  background-color: #f7f8fa;
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

/* --- 1. 统计卡片 --- */
.stats-container { display: flex; gap: 20px; margin-bottom: 25px; height: 140px; }
.main-card { flex: 0 0 35%; background: linear-gradient(135deg, #a29bfe 0%, #6c5ce7 100%); border-radius: 16px; color: #fff; position: relative; overflow: hidden; box-shadow: 0 8px 20px rgba(108, 92, 231, 0.25); display: flex; align-items: center; padding: 0 30px; }
.main-card-content { z-index: 2; display: flex; align-items: center; gap: 20px; width: 100%; }
.icon-circle { width: 64px; height: 64px; background: rgba(255, 255, 255, 0.25); border-radius: 50%; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.icon-circle i { font-size: 32px; color: #fff; }
.text-group .label { font-size: 15px; opacity: 0.9; margin-bottom: 5px; }
.text-group .number { font-size: 42px; font-weight: 800; line-height: 1; margin-bottom: 8px; }
.text-group .tag { display: inline-block; background: rgba(255, 255, 255, 0.2); padding: 4px 12px; border-radius: 20px; font-size: 13px; font-weight: 500; }
.bg-watermark { position: absolute; right: -20px; bottom: -30px; font-size: 180px; color: #fff; opacity: 0.15; transform: rotate(-15deg); z-index: 1; }

.small-cards-group { flex: 1; display: flex; gap: 20px; }
.small-card { flex: 1; background: #fff; border-radius: 16px; padding: 20px 24px; display: flex; flex-direction: column; justify-content: space-between; box-shadow: 0 4px 12px rgba(0,0,0,0.03); transition: transform 0.2s; border: 1px solid #f0f0f0; min-width: 0; }
.small-card:hover { transform: translateY(-3px); }
.card-header { display: flex; justify-content: space-between; color: #909399; font-size: 14px; align-items: center; }
.icon-grey { font-size: 18px; color: #ccc; }
.card-body { margin-top: auto; margin-bottom: 10px; }
.card-value { font-size: 36px; font-weight: 700; color: #303133; line-height: 1; }
.card-footer { height: 24px; }
.blue-text { color: #0984e3; } .pink-text { color: #fd79a8; }

/* --- 2. 内容区 & 表格优化 --- */
.content-panel { background: #fff; border-radius: 16px; padding: 20px; box-shadow: 0 4px 12px rgba(0,0,0,0.03); }

.toolbar-v2 {
  margin-bottom: 30px;
  background: #fcfcfc;
  padding: 25px;
  border-radius: 12px;
  border: 1px solid #f0f0f0;
}

.search-form .search-row {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.search-form .second-row {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px dashed #eee;
  align-items: center;
}

.age-range {
  display: flex;
  align-items: center;
  gap: 5px;
  background: #f9f9f9;
  padding: 0 10px;
  border-radius: 4px;
  border: 1px solid #eee;
}

.range-sep {
  color: #ccc;
}

.unit {
  color: #999;
  font-size: 12px;
  margin-left: 5px;
}

.btn-group {
  margin-left: auto !important;
  margin-right: 0 !important;
}

.search-form ::v-deep .el-form-item {
  margin-bottom: 0;
  margin-right: 20px;
}

::v-deep .el-form-item__label {
  font-weight: 600;
  color: #606266;
}

::v-deep .el-input__inner {
  border-radius: 8px;
  background: #fff;
  border: 1px solid #dcdfe6;
}

/* 弹窗样式优化 */
::v-deep .notice-dialog-v2 {
  border-radius: 16px;
  overflow: hidden;
}

.dialog-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 700;
  color: #303133;
}

.dialog-title i {
  color: #e6a23c;
}

.notice-container {
  padding: 10px 20px 20px 20px;
}

.recipient-preview {
  background: #fdf6ec;
  border: 1px solid #faecd8;
  border-radius: 10px;
  padding: 15px;
  margin-bottom: 25px;
}

.rp-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.rp-label {
  font-size: 13px;
  font-weight: 600;
  color: #843534;
}

.rp-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  max-height: 80px;
  overflow-y: auto;
}

.rp-item {
  display: flex;
  align-items: center;
  gap: 5px;
  background: #fff;
  padding: 4px 8px;
  border-radius: 20px;
  border: 1px solid #faecd8;
}

.rp-name {
  font-size: 12px;
  color: #666;
}

.notice-tip {
  margin-top: 15px;
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 5px;
}

.notice-form ::v-deep .el-form-item {
  margin-right: 0;
  margin-bottom: 20px;
}

.notice-form ::v-deep .el-input__inner,
.notice-form ::v-deep .el-textarea__inner {
  width: 100%;
}

.dialog-footer {
  padding-top: 10px;
}

/* 表格样式 */
.custom-table { border-radius: 8px; }
.pet-meta { display: flex; align-items: center; gap: 15px; }
.pet-avatar { width: 44px; height: 44px; border-radius: 12px; border: 1px solid #f0f0f0; flex-shrink: 0; background: #f5f7fa; }
.image-slot { display: flex; justify-content: center; align-items: center; width: 100%; height: 100%; color: #ccc; font-size: 16px; }
.p-name { font-weight: 600; color: #333; font-size: 14px; display: flex; align-items: center; gap: 6px; }
.p-id { font-size: 12px; color: #999; font-family: monospace; }
.sex-icon { font-size: 12px; padding: 2px; border-radius: 50%; color: #fff; width: 16px; height: 16px; text-align: center; line-height: 16px; display: inline-block; }
.sex-icon.male { background: #74b9ff; }
.sex-icon.female { background: #ff7675; }

.species-box { display: flex; align-items: center; gap: 8px; }
.mini-tag { border-radius: 6px; }
.breed-text { font-size: 13px; color: #606266; }

.status-text.success { color: #67c23a; font-weight: 500; font-size: 13px; }
.status-text.gray { color: #909399; font-size: 13px; }

.owner-info { display: flex; flex-direction: column; line-height: 1.4; }
.owner-name { font-weight: 600; color: #333; font-size: 13px; }
.owner-id { font-size: 12px; color: #ccc; }

/* --- 3. 弹窗优化 --- */
::v-deep .clean-dialog { border-radius: 16px; overflow: hidden; }
::v-deep .el-dialog__header { padding: 20px 25px; background: #fafafa; border-bottom: 1px solid #eee; font-weight: bold; font-size: 16px; }
::v-deep .el-dialog__body { padding: 0; }
::v-deep .el-dialog__footer { padding: 15px 25px; border-top: 1px solid #eee; background: #fff; }

.dialog-body { padding: 25px 30px; max-height: 65vh; overflow-y: auto; }

/* 头像 */
.avatar-section { display: flex; flex-direction: column; align-items: center; margin-bottom: 25px; }
.avatar-wrapper {
  width: 100px; height: 100px; border-radius: 50%; border: 3px solid #f0f0f0;
  display: flex; align-items: center; justify-content: center;
  overflow: hidden; cursor: pointer; position: relative; background: #f9f9f9; transition: 0.3s;
}
.avatar-wrapper:hover { border-color: #6c5ce7; box-shadow: 0 0 10px rgba(108, 92, 231, 0.2); }
.avatar { width: 100%; height: 100%; object-fit: cover; }
.avatar-placeholder { display: flex; flex-direction: column; align-items: center; color: #ccc; }
.avatar-placeholder i { font-size: 24px; margin-bottom: 5px; }
.avatar-placeholder span { font-size: 12px; }
.avatar-tip { font-size: 12px; color: #999; margin-top: 8px; }

/* 表单分组 */
.form-group-label { font-size: 14px; font-weight: bold; color: #333; margin-bottom: 15px; padding-left: 10px; border-left: 4px solid #6c5ce7; }
.form-card { background: #fff; border: 1px solid #eee; border-radius: 12px; padding: 20px 15px 5px 15px; margin-bottom: 10px; }
.health-bg { background: #fdfdfd; border: 1px dashed #e0e0e0; }

.flex-row { display: flex; gap: 15px; }
.confirm-btn { background: #6c5ce7; border-color: #6c5ce7; }
.confirm-btn:hover { background: #a29bfe; border-color: #a29bfe; }
</style>