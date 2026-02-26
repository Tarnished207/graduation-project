<template>
  <div class="page-container">

    <div class="stats-container">
      <div class="stat-card main-card">
        <div class="main-card-content">
          <div class="icon-circle">
            <i class="el-icon-user-solid"></i>
          </div>
          <div class="text-group">
            <div class="label">平台总用户数</div>
            <div class="number">{{ stats.userCount || 0 }}</div>
            <div class="tag">
              <i class="el-icon-s-custom"></i> 含管理员 {{ stats.adminCount || 0 }} 人
            </div>
          </div>
        </div>
        <i class="el-icon-user bg-watermark"></i>
      </div>

      <div class="small-cards-group">
        <div class="stat-card small-card">
          <div class="card-header">
            <span>活跃宠物</span>
            <i class="el-icon-sugar icon-grey"></i>
          </div>
          <div class="card-body">
            <div class="card-value blue-text">{{ stats.petCount || 0 }}</div>
          </div>
          <div class="card-footer"></div>
        </div>

        <div class="stat-card small-card">
          <div class="card-header">
            <span>消费订单</span>
            <i class="el-icon-tickets icon-grey"></i>
          </div>
          <div class="card-body">
            <div class="card-value">{{ stats.orderCount || 0 }}</div>
          </div>
          <div class="card-footer"></div>
        </div>
      </div>
    </div>

    <div class="content-panel">

      <div class="toolbar">
        <div class="left-tools">
          <el-input
              v-model="searchText"
              placeholder="🔍 搜账号/昵称/姓名..."
              class="search-input"
              @keyup.enter.native="load">
          </el-input>

          <el-input
              v-model="searchAddress"
              placeholder="📍 搜地址..."
              class="search-input"
              @keyup.enter.native="load">
          </el-input>

          <el-select v-model="searchRole" placeholder="🎭 选择角色" class="search-input" clearable @change="load">
            <el-option label="管理员" value="ADMIN"></el-option>
            <el-option label="商品客服" value="SERVICE"></el-option>
            <el-option label="宠物医生" value="DOCTOR"></el-option>
            <el-option label="普通用户" value="USER"></el-option>
          </el-select>

          <el-button circle icon="el-icon-search" @click="load"></el-button>
          <el-button circle icon="el-icon-refresh" @click="resetSearch"></el-button>
        </div>

        <div class="right-tools">
          <el-button type="warning" round icon="el-icon-message" @click="handleBatchNotice">发送通知</el-button>
          <el-button type="primary" round icon="el-icon-plus" class="add-btn" @click="handleAdd">新增用户</el-button>
        </div>
      </div>

      <el-table
          :data="tableData"
          style="width: 100%"
          class="custom-table"
          @selection-change="handleSelectionChange"
          :header-cell-style="{background:'#fff', color:'#909399', borderBottom:'1px solid #f0f0f0', fontWeight:'500'}"
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column label="用户" min-width="160">
          <template slot-scope="scope">
            <div class="user-meta">
              <div class="avatar-ring" :class="scope.row.status === 1 ? 'enabled' : 'disabled'">
                <el-avatar :size="44"
                           :src="scope.row.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
                           class="user-avatar"></el-avatar>
              </div>
              <div class="meta-text">
                <div class="u-name">{{ scope.row.nickname || '未设置昵称' }}</div>
                <div class="u-sub">@{{ scope.row.username }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="角色" width="100">
          <template slot-scope="scope">
            <span v-if="scope.row.role === 'ADMIN'" class="role-badge role-admin">管理员</span>
            <span v-else-if="scope.row.role === 'SERVICE'" class="role-badge" style="background:#fff7e6; color:#fa8c16;">商品客服</span>
            <span v-else-if="scope.row.role === 'DOCTOR'" class="role-badge" style="background:#e6fffb; color:#13c2c2;">宠物医生</span>
            <span v-else class="role-badge role-user">普通用户</span>
          </template>
        </el-table-column>

        <el-table-column prop="phone" label="联系电话" width="140"></el-table-column>

        <el-table-column prop="address" label="收货地址" min-width="180" show-overflow-tooltip></el-table-column>

        <el-table-column label="注册时间" width="160" sortableProp="createTime">
          <template slot-scope="scope">
            <span style="color: #999; font-size: 13px;">
              {{ scope.row.createTime ? scope.row.createTime.replace('T', ' ') : '-' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" align="right">
          <template slot-scope="scope">
            <el-button type="text" style="color:#666" @click="handleEdit(scope.row)">编辑</el-button>
            <el-popconfirm title="确定删除该用户吗？" @confirm="handleDelete(scope.row.id)">
              <el-button slot="reference" type="text" style="color:#ff6b6b; margin-left:10px">删除</el-button>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog :title="form.id ? '编辑用户' : '新增用户'" :visible.sync="dialogVisible" width="500px" custom-class="rounded-dialog">
      <el-form :model="form" label-width="80px">
        <el-form-item label="头像">
          <el-upload
              class="avatar-uploader"
              :action="$baseUrl + '/file/upload'"
              name="file"
              :show-file-list="false"
              :on-success="handleUploadSuccess">
            <img v-if="form.avatar" :src="form.avatar" class="avatar">
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
        </el-form-item>

        <el-form-item label="用户名">
          <el-input v-model="form.username" :disabled="!!form.id"></el-input>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname"></el-input>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="普通用户" value="USER">
              <span style="float: left">普通用户</span>
              <span style="float: right; color: #8492a6; font-size: 13px">用户</span>
            </el-option>
            <el-option label="商品客服" value="SERVICE">
              <span style="float: left">商品客服</span>
              <span style="float: right; color: #8492a6; font-size: 13px">处理售后</span>
            </el-option>
            <el-option label="宠物医生" value="DOCTOR">
              <span style="float: left">宠物医生</span>
              <span style="float: right; color: #8492a6; font-size: 13px">在线问诊</span>
            </el-option>
            <el-option label="管理员" value="ADMIN">
              <span style="float: left">管理员</span>
              <span style="float: right; color: #8492a6; font-size: 13px">系统权限</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="账号状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="电话">
          <el-input v-model="form.phone"></el-input>
        </el-form-item>
        <el-form-item label="地址">
          <el-input type="textarea" v-model="form.address"></el-input>
        </el-form-item>
        <el-form-item label="密码" v-if="!form.id">
          <el-input v-model="form.password" show-password placeholder="默认123456"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false" round>取消</el-button>
        <el-button type="primary" @click="save" round class="confirm-btn">保存</el-button>
      </span>
    </el-dialog>

    <!-- 发送通知对话框 -->
    <el-dialog title="发送系统通知" :visible.sync="noticeDialogVisible" width="500px">
      <el-form :model="noticeForm" label-position="top">
        <el-form-item label="通知标题">
          <el-input v-model="noticeForm.title" placeholder="请输入通知标题"></el-input>
        </el-form-item>
        <el-form-item label="通知内容">
          <el-input type="textarea" v-model="noticeForm.content" :rows="4" placeholder="请输入通知内容"></el-input>
        </el-form-item>
        <div style="color: #999; font-size: 12px; margin-top: -10px;">
          将发送给已选中的 {{ multipleSelection.length }} 位用户。
        </div>
      </el-form>
      <span slot="footer">
        <el-button @click="noticeDialogVisible = false" round>取消</el-button>
        <el-button type="primary" @click="sendBatchNotice" round :loading="noticeLoading">立即发送</el-button>
      </span>
    </el-dialog>

  </div>
</template>

<script>
export default {
  data() {
    return {
      tableData: [],
      searchText: '',
      searchAddress: '',
      searchRole: '',
      stats: {},
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
    loadStats() {
      this.$http.get('/sysUser/stats').then(res => {
        this.stats = res.data;
      })
    },
    load() {
      this.$http.get('/sysUser/list', {
        params: {
          name: this.searchText,
          address: this.searchAddress,
          role: this.searchRole
        }
      }).then(res => {
        this.tableData = res.data;
      })
    },
    resetSearch() {
      this.searchText = '';
      this.searchAddress = '';
      this.searchRole = '';
      this.load();
    },
    handleAdd() {
      this.form = {role: 'USER', status: 1};
      this.dialogVisible = true;
    },
    handleEdit(row) {
      this.form = JSON.parse(JSON.stringify(row));
      this.dialogVisible = true;
    },
    save() {
      let url = this.form.id ? '/sysUser/update' : '/sysUser/register';
      if(!this.form.password && !this.form.id) this.form.password = '123456';

      this.$http.post(url, this.form).then(res => {
        // 后端 register 返回字符串， update 返回 boolean，做个兼容处理
        if(res.data === true || res.data === '注册成功') {
          this.$message.success("操作成功");
          this.dialogVisible = false;
          this.load();
          this.loadStats();
        } else {
          this.$message.error("操作失败：" + res.data);
        }
      })
    },
    handleDelete(id) {
      this.$http.get('/sysUser/delete/' + id).then(res => {
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
      if (this.multipleSelection.length === 0) {
        this.$message.warning("请先在列表中勾选需要通知的用户");
        return;
      }
      this.noticeForm = {title: '系统通知', content: ''};
      this.noticeDialogVisible = true;
    },
    sendBatchNotice() {
      if (!this.noticeForm.title || !this.noticeForm.content) {
        this.$message.warning("请填写标题和内容");
        return;
      }
      this.noticeLoading = true;
      const userIds = this.multipleSelection.map(u => u.id);
      this.$http.post('/sysNotice/batchSend', {
        userIds: userIds,
        title: this.noticeForm.title,
        content: this.noticeForm.content
      }).then(res => {
        if (res.data.code === '200') {
          this.$message.success("通知已批量发送");
          this.noticeDialogVisible = false;
        }
        this.noticeLoading = false;
      }).catch(() => {
        this.noticeLoading = false;
      });
    }
  }
}
</script>

<style scoped>
/* 全局样式 复用 Product.vue 的风格 */
.page-container {
  padding: 25px;
  background-color: #f7f8fa;
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

/* 1. 顶部统计 */
.stats-container {
  display: flex;
  gap: 20px;
  margin-bottom: 25px;
  height: 140px;
}

/* 主卡片 (蓝色渐变) */
.main-card {
  flex: 0 0 35%;
  background: linear-gradient(135deg, #74b9ff 0%, #0984e3 100%); /* 蓝色系 */
  border-radius: 16px;
  color: #fff;
  position: relative;
  overflow: hidden;
  box-shadow: 0 8px 20px rgba(9, 132, 227, 0.25);
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

/* 小卡片 */
.small-cards-group { flex: 1; display: flex; gap: 20px; }
.small-card {
  flex: 1;
  background: #fff;
  border-radius: 16px;
  padding: 20px 24px;
  display: flex; flex-direction: column; justify-content: space-between;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
  border: 1px solid #f0f0f0;
  min-width: 0;
  transition: transform 0.2s;
}
.small-card:hover { transform: translateY(-3px); }
.card-header { display: flex; justify-content: space-between; color: #909399; font-size: 14px; align-items: center; }
.icon-grey { font-size: 18px; color: #ccc; }
.card-body { margin-top: auto; margin-bottom: 10px; }
.card-value { font-size: 36px; font-weight: 700; color: #303133; line-height: 1; }
.blue-text { color: #0984e3; }
.card-footer { height: 24px; } /* 占位防止跳动 */

/* 2. 主内容区 */
.content-panel {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
}
.toolbar { display: flex; justify-content: space-between; margin-bottom: 20px; }
.left-tools { display: flex; gap: 12px; }
.search-input { width: 200px; }
::v-deep .el-input__inner { border-radius: 20px; background: #f9f9f9; border: 1px solid #eee; }
::v-deep .el-input__inner:focus { background: #fff; border-color: #0984e3; }
.add-btn { background: #0984e3; border-color: #0984e3; box-shadow: 0 4px 10px rgba(9, 132, 227, 0.4); }
.add-btn:hover { background: #74b9ff; border-color: #74b9ff; }

/* 表格样式 */
.custom-table { border-radius: 8px; }

.user-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-ring {
  padding: 0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.avatar-ring.enabled {
  border: 3px solid #13c2c2;
}

.avatar-ring.disabled {
  border: 3px solid #f5222d;
}

.avatar-ring .el-avatar {
  display: block;
  border-radius: 0;
}

.user-avatar {
  border: none;
}
.u-name { font-weight: 600; color: #333; font-size: 14px; margin-bottom: 2px; }
.u-sub { color: #999; font-size: 12px; }

.meta-badges {
  margin-top: 6px;
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

/* 角色徽章 */
.role-badge {
  padding: 4px 10px; border-radius: 12px; font-size: 12px; font-weight: 500;
}
.role-admin { background: #ffe2e2; color: #ff6b6b; }
.role-user { background: #e2f0ff; color: #0984e3; }

/* 状态徽章（只读显示） */

/* 弹窗样式 */
.confirm-btn { background: #0984e3; border-color: #0984e3; }
.avatar-uploader .el-upload { border: 2px dashed #eee; border-radius: 50%; cursor: pointer; position: relative; overflow: hidden; }
.avatar-uploader .el-upload:hover { border-color: #0984e3; }
.avatar-uploader-icon { font-size: 28px; color: #ccc; width: 100px; height: 100px; line-height: 100px; text-align: center; }
.avatar { width: 100px; height: 100px; display: block; border-radius: 50%; }
</style>
