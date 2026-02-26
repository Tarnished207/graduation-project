<template>
  <div class="service-workbench">
    <div class="sidebar">
      <div class="my-profile">
        <div class="avatar-box">
          <el-avatar :size="44" :src="getImageUrl(user.avatar)" @error="errorHandler">
            <img src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png"/>
          </el-avatar>
        </div>
        <div class="info">
          <div class="name">{{ user.nickname || user.username }}</div>
          <div class="role-badge"><i class="el-icon-first-aid-kit"></i> 医生端</div>
        </div>
        <el-tooltip content="退出登录" placement="right">
          <i class="el-icon-switch-button logout-icon" @click="logout"></i>
        </el-tooltip>
      </div>

      <div class="search-wrap">
        <el-input placeholder="搜索患者..." prefix-icon="el-icon-search" size="small" v-model="contactSearch"
                  class="seamless-input"></el-input>
      </div>

      <div class="session-list">
        <div
            v-for="item in filteredContacts"
            :key="item.userId"
            class="session-item"
            :class="{ active: currentChatUser && currentChatUser.id === item.userId }"
            @click="selectUser(item)"
        >
          <div class="avatar-box">
            <el-avatar :size="40" :src="getImageUrl(item.avatar)" shape="square" @error="errorHandler">
              <i class="el-icon-s-custom"></i>
            </el-avatar>
          </div>
          <div class="session-info">
            <div class="u-row">
              <span class="u-name">{{ item.nickname }}</span>
              <span class="u-time">{{ formatTime(item.lastTime) }}</span>
            </div>
            <div class="u-msg">{{ getMsgPreview(item.lastMsg) }}</div>
          </div>
          <el-badge :value="unreadMap[item.userId]" class="unread-badge" v-if="unreadMap[item.userId] > 0"/>
          <i class="el-icon-close close-session" @click.stop="removeSession(item)" title="结束会话"></i>
        </div>

        <div v-if="contactList.length === 0" class="empty-list">
          <i class="el-icon-chat-round" style="font-size:30px; margin-bottom:10px; opacity:0.3"></i>
          <span>暂无会话</span>
        </div>
      </div>
    </div>

    <div class="chat-container">
      <div class="chat-header">
        <div v-if="currentChatUser" class="chat-title">
          <span class="c-name">{{ currentChatUser.nickname }}</span>
        </div>
        <div v-else class="chat-title empty-title">
          <i class="el-icon-first-aid-kit"></i> 医生工作台
        </div>
      </div>

      <div class="chat-body" ref="chatBody">
        <div v-if="!currentChatUser" class="empty-state">
          <i class="el-icon-first-aid-kit" style="font-size: 60px; color:#e0e0e0; margin-bottom: 20px;"></i>
          <p>请从左侧选择患者开始问诊</p>
        </div>

        <div v-else class="msg-list">
          <div v-for="(msg, i) in messages" :key="i" class="msg-row" :class="msg.senderId === user.id ? 'me' : 'other'">
            <el-avatar :size="36" :src="getImageUrl(msg.senderId === user.id ? user.avatar : currentChatUser.avatar)"
                       class="msg-avatar" @error="errorHandler">
              <i class="el-icon-user-solid"></i>
            </el-avatar>

            <div class="bubble" v-if="msg.msgType === 'TEXT'">{{ msg.content }}</div>

            <div class="image-msg" v-else-if="msg.msgType === 'IMAGE'">
              <img :src="getImageUrl(msg.content)" class="img-msg">
            </div>

            <div class="product-card-msg" v-else-if="msg.msgType === 'PRODUCT'" @click="viewDetail(msg, 'PRODUCT')">
              <img :src="getImageUrl(parseMsgContent(msg.content).image)" class="pc-img">
              <div class="pc-info">
                <div class="pc-name">{{ parseMsgContent(msg.content).name }}</div>
                <div class="pc-price">¥{{ parseMsgContent(msg.content).price }}</div>
              </div>
              <div class="pc-foot">点击查看详情</div>
            </div>

            <div class="pet-card-msg" v-else-if="msg.msgType === 'PET'" @click="openPetFromMsg(msg)">
              <img :src="getImageUrl(parseMsgContent(msg.content).avatar)" class="petc-img">
              <div class="petc-info">
                <div class="petc-name">{{ parseMsgContent(msg.content).nickname }}</div>
                <div class="petc-meta">{{ parseMsgContent(msg.content).breed || '未知品种' }} ·
                  {{ parseMsgContent(msg.content).age }}岁
                </div>
              </div>
              <div class="petc-foot">点击查看病历时间轴</div>
            </div>

            <div class="order-card-msg" v-else-if="msg.msgType === 'ORDER'">
              <div class="oc-head">📋 咨询订单</div>
              <div class="oc-body">
                <div class="oc-title">{{ parseMsgContent(msg.content).name }}</div>
                <div class="oc-price-box">
                  <span class="oc-price">¥{{ parseMsgContent(msg.content).price }}</span>
                </div>
                <div class="oc-no-box">
                  <span class="oc-no">No.{{
                      parseMsgContent(msg.content).no || parseMsgContent(msg.content).orderNo
                    }}</span>
                </div>
              </div>
              <div class="oc-foot">
                <el-button type="text" size="mini" @click="viewDetail(msg, 'ORDER')">点击查看详情 &gt;</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="chat-footer" v-if="currentChatUser">
        <div class="tools-bar">
          <div class="icon-btn" title="发送图片">
            <i class="el-icon-picture-outline" @click="$refs.imgInput.click()"></i>
            <input type="file" ref="imgInput" accept="image/*" style="display:none" @change="uploadImage">
          </div>

          <el-popover placement="top" width="420" trigger="click" @show="loadRecommendProducts">
            <div class="prod-select-panel">
              <div class="psp-header">
                <el-select v-model="prodCategory" placeholder="全部分类" size="mini"
                           style="width: 110px; margin-right: 8px" @change="loadRecommendProducts">
                  <el-option label="全部" value=""></el-option>
                  <el-option label="主粮" value="food"></el-option>
                  <el-option label="零食" value="snack"></el-option>
                  <el-option label="玩具" value="toy"></el-option>
                  <el-option label="用品" value="daily"></el-option>
                  <el-option label="医疗" value="medical"></el-option>
                  <el-option label="器械" value="device"></el-option>
                </el-select>
                <el-input size="mini" v-model="prodSearch" placeholder="搜索商品名称..." prefix-icon="el-icon-search"
                          @input="loadRecommendProducts" clearable style="flex:1"></el-input>
              </div>

              <div class="psp-list" v-loading="prodLoading">
                <div v-for="p in recommendList" :key="p.id" class="psp-item" @click="sendProductCard(p)">
                  <img :src="getImageUrl(p.image)" class="psp-img">
                  <div class="psp-info">
                    <div class="psp-name">{{ p.name }}</div>
                    <div class="psp-row">
                      <span class="psp-price">¥{{ p.price }}</span>
                      <el-button type="text" size="mini" class="psp-send-btn">发送</el-button>
                    </div>
                  </div>
                </div>
                <div v-if="recommendList.length===0" class="psp-empty">暂无相关商品</div>
              </div>
            </div>
            <div class="icon-btn" slot="reference" title="推荐商品">
              <i class="el-icon-goods" style="color:#ff9f43"></i>
            </div>
          </el-popover>
        </div>
        <div class="input-area">
          <textarea class="input-box" v-model="inputMsg" placeholder="请输入回复内容... (Enter发送)"
                    @keyup.enter="send"></textarea>
          <div class="send-btn-wrap">
            <el-button type="primary" size="medium" @click="send" style="padding: 8px 25px;">发送</el-button>
          </div>
        </div>
      </div>
    </div>

    <div class="crm-panel">
      <div v-if="currentChatUser">
        <!-- 用户信息卡片 (与客服端一致) -->
        <div class="user-card-wrap">
          <div class="user-card-header">
            <el-avatar :size="60" :src="getImageUrl(currentChatUser.avatar)" class="uc-avatar" @error="errorHandler">
              <i class="el-icon-user-solid" style="font-size:24px; margin-top:10px;"></i>
            </el-avatar>
            <div class="uc-info">
              <div class="uc-name">{{ currentChatUser.nickname }}</div>
              <div class="uc-sub">ID: {{ currentChatUser.id }}</div>
            </div>
          </div>

          <div class="uc-details">
            <div class="uc-row">
              <span class="label">联系电话</span>
              <span class="val">{{ currentChatUser.phone || '暂无' }}</span>
            </div>
            <div class="uc-row">
              <span class="label">注册时间</span>
              <span class="val">{{
                  currentChatUser.createTime ? currentChatUser.createTime.substring(0, 10) : '-'
                }}</span>
            </div>
          </div>
        </div>

        <!-- 宠物列表 (手风琴折叠式) -->
        <div class="pet-accordion-list"
             style="margin-top:20px; flex:1; overflow:hidden; display:flex; flex-direction:column;">
          <div class="pl-header">Ta的宠物 ({{ userPets.length }})</div>

          <div class="pet-list-scroll-accordion">
            <div
                v-for="p in userPets"
                :key="p.id"
                class="pet-accordion-item"
                :class="{ active: currentPet && currentPet.id === p.id }"
            >
              <!-- 宠物头部（点击展开/收起） -->
              <div class="pai-header" @click="togglePet(p)">
                <img :src="getImageUrl(p.avatar)" class="pai-img">
                <div class="pai-info">
                  <div class="pai-name">{{ p.nickname }}</div>
                  <div class="pai-sub">
                    <span>{{ p.type === 0 ? '狗' : '猫' }}</span>
                    <span> · {{ p.breed || '未知' }}</span>
                    <span> · {{ p.sex === 0 ? '公' : '母' }}</span>
                    <span> · {{ p.sterilization === 1 ? '已绝育' : '未绝育' }}</span>
                    <span> · {{ p.age }}岁</span>
                    <span v-if="latestWeightMap[p.id]"> · {{ latestWeightMap[p.id] }} kg</span>
                  </div>
                </div>
                <i class="el-icon-arrow-down pai-arrow" :class="{ rotated: currentPet && currentPet.id === p.id }"></i>
              </div>

              <!-- 展开的病历区域 -->
              <div class="pai-content" v-if="currentPet && currentPet.id === p.id">
                <div class="pai-case-header">
                  <span class="pch-title">{{ p.nickname }}的病历</span>
                </div>

                <div class="pai-records">
                  <el-timeline v-if="records.length > 0" class="mini-timeline">
                    <el-timeline-item
                        v-for="(rec, idx) in records"
                        :key="idx"
                        :timestamp="rec.createTime | fmtDateSimple"
                        placement="top"
                        color="#409EFF"
                        hide-timestamp
                    >
                      <div class="mt-date">{{ rec.createTime | fmtDateSimple }}</div>
                      <div class="mt-card">
                        <div class="mt-diag">{{ rec.diagnosis }}</div>
                        <div class="mt-detail">
                          <span class="mt-label">主诉:</span> {{ rec.symptoms || '-' }}
                        </div>
                        <el-button type="text" size="mini" @click="openCaseDetail(rec)">查看详情</el-button>
                      </div>
                    </el-timeline-item>
                  </el-timeline>
                  <div v-else class="empty-tip-small">暂无就诊记录</div>
                </div>
              </div>
            </div>

            <div v-if="userPets.length===0" class="empty-tip">暂无宠物档案</div>
          </div>
        </div>
      </div>

      <div v-else class="crm-empty-placeholder">
        <i class="el-icon-s-custom"></i>
        <p>暂无患者档案</p>
      </div>
    </div>

    <el-dialog :visible.sync="caseDetailVisible" width="680px" custom-class="case-detail-dialog">
      <div slot="title" class="cd-title-bar">
        <el-tag type="primary" size="small" class="cd-chip">病例</el-tag>
        <div class="cd-title-text">{{ caseDetail.diagnosis || caseDetail.title }}</div>
      </div>
      <div class="case-detail-card">
        <div class="cd-pet-row" v-if="currentPet">
          <img :src="getImageUrl(currentPet.avatar)" class="cd-pet-avatar">
          <div class="cd-pet-info">
            <div class="cd-pet-name">
              {{ currentPet.nickname }}
              <i v-if="currentPet.sex===0" class="el-icon-male sex-icon male"></i>
              <i v-else class="el-icon-female sex-icon female"></i>
            </div>
            <div class="cd-pet-meta">
              <span>{{ currentPet.breed || '未知' }}</span>
              <span> · {{ currentPet.age }}岁</span>
              <span v-if="latestWeightMap[currentPet.id]"> · {{ latestWeightMap[currentPet.id] }} kg</span>
              <span> · {{ currentPet.sterilization === 1 ? '已绝育' : '未绝育' }}</span>
            </div>
          </div>
        </div>
        <div class="cd-meta">
          <div class="meta-item" v-if="caseDetail.hospital"><i
              class="el-icon-location-outline"></i>{{ caseDetail.hospital }}
          </div>
        </div>
        <div class="cd-content">
          <div class="cd-section" v-if="caseDetail.symptoms">
            <div class="cd-sec-title">主诉症状</div>
            <div class="cd-sec-body">{{ caseDetail.symptoms }}</div>
          </div>
          <div class="cd-section" v-if="caseDetail.treatment">
            <div class="cd-sec-title">处置方案</div>
            <div class="cd-sec-body">{{ caseDetail.treatment }}</div>
          </div>
          <div class="cd-section" v-if="caseDetail.prescription">
            <div class="cd-sec-title">处方药物</div>
            <div class="cd-sec-body">{{ caseDetail.prescription }}</div>
          </div>
          <div class="cd-section" v-if="!caseDetail.symptoms && !caseDetail.treatment && !caseDetail.prescription">
            <div class="cd-sec-body">{{ caseDetail.description || '暂无详细内容' }}</div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 详情弹窗 (商品/订单) -->
    <el-dialog :visible.sync="detailVisible" width="400px" custom-class="detail-dialog">
      <div slot="title" class="dd-title">
        <i :class="detailType==='PRODUCT' ? 'el-icon-goods' : 'el-icon-s-order'"></i>
        {{ detailType === 'PRODUCT' ? '商品详情' : '订单详情' }}
      </div>
      <div class="dd-content" v-if="detailVisible">
        <div class="dd-img-box">
          <img :src="getImageUrl(detailData.image || detailData.img)" class="dd-img">
        </div>
        <div class="dd-name">{{ detailData.name }}</div>
        <div class="dd-price">¥{{ detailData.price }}</div>

        <div v-if="detailType==='ORDER'" class="dd-order-info">
          <div class="dd-row">
            <span>订单编号</span>
            <span>{{ detailData.no }}</span>
          </div>
          <div class="dd-row">
            <span>当前状态</span>
            <span :class="'st-' + detailData.status">{{ getStatusText(detailData.status) }}</span>
          </div>
        </div>

        <div class="dd-desc" v-if="detailData.description">
          {{ detailData.description }}
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" size="small" @click="detailVisible = false">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      user: {},
      contactList: [],
      removedSessionMap: {},
      contactSearch: '',
      currentChatUser: null,
      messages: [],
      inputMsg: '',
      emojis: ['😀', '😁', '😂', '🤣', '😃', '😄', '😅', '😆', '😉', '😊'],
      prodLoading: false,
      recommendList: [],
      prodSearch: '',
      prodCategory: '',
      userPets: [],
      currentPet: null,
      records: [],
      caseDetailVisible: false,
      caseDetail: {},
      latestWeightMap: {},
      socket: null,
      reconnectTimer: null,

      // 未读消息提醒
      unreadMap: {},
      blinkTimer: null,
      originalTitle: document.title,

      // 详情弹窗相关
      detailVisible: false,
      detailType: 'PRODUCT', // PRODUCT / ORDER
      detailData: {},
    };
  },
  filters: {
    fmtDate(val) {
      return val ? val.substring(0, 10) : '';
    },
    fmtDateSimple(val) {
      return val ? val.substring(0, 10) : '';
    }
  },
  computed: {
    filteredContacts() {
      if (!this.contactSearch) return this.contactList;
      return this.contactList.filter(c => c.nickname.includes(this.contactSearch));
    }
  },
  created() {
    const userStr = sessionStorage.getItem("user");
    if (userStr) {
      this.user = JSON.parse(userStr);
      const savedMap = localStorage.getItem(`removed_sessions_${this.user.id}`);
      if (savedMap) {
        try {
          this.removedSessionMap = JSON.parse(savedMap);
        } catch (e) {
        }
      }
      this.loadRecentContacts();
      // 初始化WS
      this.initWebSocket();
    } else {
      this.$router.push("/login");
    }
  },
  mounted() {
    // 移除轮询
    // this.timer = setInterval(() => {
    //   this.loadRecentContacts();
    //   if (this.currentChatUser) this.loadHistory(this.currentChatUser.id, true);
    // }, 3000);
  },
  destroyed() {
    if (this.socket) this.socket.close();
    if (this.reconnectTimer) clearTimeout(this.reconnectTimer);
    // clearInterval(this.timer);
  },
  methods: {
    initWebSocket() {
      if (typeof (WebSocket) === "undefined") {
        console.log("不支持WS");
        return;
      }
      if (!this.user.id) return;

      const wsUrl = `ws://localhost:9090/ws/chat/${this.user.id}`;
      this.socket = new WebSocket(wsUrl);
      this.socket.onopen = () => console.log("医生端WS连接");
      this.socket.onmessage = (msg) => {
        try {
          const data = JSON.parse(msg.data);
          // 使用 == 兼容 String 和 Number 类型的 ID 比较
          if (this.currentChatUser && (data.senderId == this.currentChatUser.id || data.receiverId == this.currentChatUser.id)) {
            this.messages.push(data);
            this.scrollToBottom();
            if (data.senderId == this.currentChatUser.id) {
              this.$http.post('/chat/read', {
                senderId: data.senderId,
                receiverId: this.user.id
              }).then(() => {
                this.$set(this.unreadMap, data.senderId, 0);
                this.loadRecentContacts();
              });
            } else {
              this.loadRecentContacts();
            }
          } else {
            const senderId = data.senderId;
            if (senderId != this.user.id) {
              if (!this.unreadMap[senderId]) this.$set(this.unreadMap, senderId, 0);
              this.unreadMap[senderId]++;
            }
            this.loadRecentContacts();
          }
        } catch (e) {
        }
      };
      this.socket.onclose = () => {
        this.reconnectTimer = setTimeout(() => this.initWebSocket(), 3000);
      };
    },

    logout() {
      sessionStorage.removeItem("user");
      this.$message.success("已安全退出");
      this.$router.push("/login");
    },
    errorHandler() {
      return true;
    },
    getImageUrl(url) {
      if (!url) return '';
      if (url.startsWith('http')) return url;
      if (url.startsWith('/')) return 'http://localhost:9090' + url;
      return 'http://localhost:9090/images/' + url;
    },
    uploadImage(e) {
      const file = e.target.files[0];
      if (!file) return;
      const formData = new FormData();
      formData.append('file', file);
      this.$http.post('/file/upload', formData).then(res => {
        if (res.data) {
          const imgUrl = res.data;
          this.sendImage(imgUrl);
        } else {
          this.$message.error('上传失败');
        }
      });
      e.target.value = '';
    },
    sendImage(imgUrl) {
      if (!this.currentChatUser) return;
      const msg = {
        senderId: this.user.id,
        receiverId: this.currentChatUser.id,
        content: imgUrl,
        msgType: 'IMAGE',
        bizType: 'DOCTOR'
      };
      this.messages.push(msg);
      this.$http.post('/chat/send', msg).then(() => {
        this.scrollToBottom();
      });
    },
    loadRecommendProducts() {
      this.prodLoading = true;
      const params = {
        pageNum: 1,
        pageSize: 20,
        name: this.prodSearch
      };
      if (this.prodCategory) params.category = this.prodCategory;
      this.$http.get('/product/list', {params}).then(res => {
        this.recommendList = res.data.data || res.data || [];
        this.prodLoading = false;
      }).catch(() => this.prodLoading = false);
    },
    sendProductCard(product) {
      const content = JSON.stringify({
        id: product.id,
        name: product.name,
        price: product.price,
        image: product.img || product.image,
        description: product.description
      });
      const msg = {
        senderId: this.user.id,
        receiverId: this.currentChatUser.id,
        content: content,
        msgType: 'PRODUCT',
        bizType: 'DOCTOR'
      };
      this.messages.push(msg);
      this.$http.post('/chat/send', msg).then(() => {
        this.scrollToBottom();
      });
    },
    appendInput(text) {
      this.inputMsg += text;
    },
    parseMsgContent(content) {
      if (!content) return {};
      try {
        return JSON.parse(content);
      } catch (e) {
        return {};
      }
    },
    getMsgPreview(msgContent) {
      if (!msgContent) return '';
      if (msgContent.startsWith('{')) {
        if (msgContent.includes('"msgType":"PET"')) return '[宠物名片]';
        if (msgContent.includes('"msgType":"PRODUCT"')) return '[商品卡片]';
        return '[图文消息]';
      }
      return msgContent;
    },
    async loadRecentContacts() {
      const res = await this.$http.get('/chat/allMessages', {params: {userId: this.user.id}});
      const msgs = res.data;

      // --- 计算未读数 ---
      const tempUnread = {};
      msgs.forEach(m => {
        // 如果我是接收者，且消息未读 (0或null)
        if (m.receiverId == this.user.id && (m.isRead === 0 || m.isRead === null)) {
          if (!tempUnread[m.senderId]) tempUnread[m.senderId] = 0;
          tempUnread[m.senderId]++;
        }
      });
      this.unreadMap = tempUnread;
      // ----------------
      
      const usersRes = await this.$http.get('/sysUser/list');
      const userMap = {};
      usersRes.data.forEach(u => userMap[u.id] = u);
      const map = new Map();
      msgs.forEach(m => {
        const targetId = m.senderId == this.user.id ? m.receiverId : m.senderId;
        if (!map.has(targetId)) {
          const targetUser = userMap[targetId];
          if (targetUser) {
            map.set(targetId, {
              userId: targetId,
              nickname: targetUser.nickname || targetUser.username,
              avatar: targetUser.avatar,
              lastMsg: m.msgType === 'PET' ? '[宠物名片]' : (m.msgType === 'PRODUCT' ? '[商品]' : m.content),
              lastTime: m.createTime
            });
          }
        }
      });
      const newContacts = Array.from(map.values()).filter(nc => {
        // 过滤掉已结束且没有新消息的会话
        if (this.removedSessionMap.hasOwnProperty(nc.userId)) {
          const removedTime = this.removedSessionMap[nc.userId];
          if (nc.lastTime > removedTime) {
            delete this.removedSessionMap[nc.userId];
            this.saveRemovedSessions();
            this.$message.info(`收到 ${nc.nickname} 的新消息`);
            return true;
          }
          return false;
        }
        return true;
      });

      // 核心修复：按时间倒序排列，确保最新消息在最上方
      newContacts.sort((a, b) => new Date(b.lastTime) - new Date(a.lastTime));

      this.contactList = newContacts;
    },
    removeSession(item) {
      this.$set(this.removedSessionMap, item.userId, item.lastTime);
      this.saveRemovedSessions();
      this.contactList = this.contactList.filter(c => c.userId !== item.userId);
      if (this.currentChatUser && this.currentChatUser.id === item.userId) {
        this.currentChatUser = null;
        this.messages = [];
        this.userPets = [];
        this.currentPet = null;
        this.records = [];
      }
      this.$message.success("会话已结束");
    },
    saveRemovedSessions() {
      localStorage.setItem(`removed_sessions_${this.user.id}`, JSON.stringify(this.removedSessionMap));
    },
    selectUser(item) {
      // --- 调用后端标记已读 ---
      this.$http.post('/chat/read', {
        senderId: item.userId,
        receiverId: this.user.id
      });
      // 清除本地未读计数
      this.$set(this.unreadMap, item.userId, 0);

      this.currentChatUser = {id: item.userId, nickname: item.nickname, avatar: item.avatar};
      this.$http.get('/sysUser/list', {params: {name: item.nickname}}).then(res => {
        if (res.data && res.data.length > 0) {
          const fullInfo = res.data.find(u => u.id === item.userId);
          if (fullInfo) this.currentChatUser = fullInfo;
        }
      });
      this.loadHistory(item.userId);
      this.loadPets(item.userId);
    },
    loadHistory(uid, isSilent) {
      this.$http.get('/chat/history', {
        params: {uid1: this.user.id, uid2: uid, type: 'DOCTOR'}
      }).then(res => {
        this.messages = res.data || [];
        if (!isSilent) this.scrollToBottom();
      });
    },
    send() {
      if (!this.inputMsg.trim() || !this.currentChatUser) return;
      const msg = {
        senderId: this.user.id, receiverId: this.currentChatUser.id,
        content: this.inputMsg, msgType: 'TEXT', bizType: 'DOCTOR'
      };
      this.messages.push(msg);
      this.inputMsg = '';
      this.scrollToBottom();
      this.$http.post('/chat/send', msg);
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const d = this.$refs.chatBody;
        if (d) d.scrollTop = d.scrollHeight;
      });
    },
    formatTime(t) {
      return t ? t.substring(5, 16).replace('T', ' ') : '';
    },

    loadPets(uid) {
      this.$http.get('/petInfo/list/' + uid).then(res => {
        const list = res.data.data || res.data;
        this.userPets = list || [];
        // 默认展开第一个宠物
        if (this.userPets.length > 0) {
          this.togglePet(this.userPets[0]);
        }
        this.computeLatestWeights();
      });
    },
    async computeLatestWeights() {
      const pets = this.userPets || [];
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
    togglePet(pet) {
      if (this.currentPet && this.currentPet.id === pet.id) {
        // 如果点击已展开的，则收起（可选，如果不希望收起可以注释掉下面这行）
        this.currentPet = null;
        this.records = [];
      } else {
        this.currentPet = pet;
        this.loadCaseOrRecords();
      }
    },
    getTypeColor(type) {
      const map = {
        '病例': '#409EFF',
        '疫苗': '#67C23A',
        '生病': '#F56C6C',
        '驱虫': '#E6A23C',
        '洗护': '#409EFF',
        '体检': '#909399',
        '日常': '#333'
      };
      return map[type] || '#409EFF';
    },
    getTypeTag(type) {
      const map = {
        '病例': 'primary',
        '疫苗': 'success',
        '生病': 'danger',
        '驱虫': 'warning',
        '洗护': 'primary',
        '体检': 'info',
        '日常': ''
      };
      return map[type] || 'primary';
    },
    selectPet(pet) {
      this.currentPet = pet;
      this.loadCaseOrRecords();
    },
    loadCaseOrRecords() {
      if (!this.currentPet || !this.currentPet.id) return;
      this.$http.get('/petCase/list/' + this.currentPet.id).then(res => {
        const cases = res.data && (res.data.data || res.data) || [];
        this.records = cases.map(c => ({
          id: c.id,
          type: '病例',
          diagnosis: c.diagnosis || '',
          title: c.diagnosis || '门诊记录',
          description: [c.symptoms, c.treatment, c.prescription].filter(Boolean).join('；'),
          symptoms: c.symptoms || '',
          treatment: c.treatment || '',
          prescription: c.prescription || '',
          nextTime: c.nextTime || null,
          hospital: c.hospital || '',
          createTime: c.createTime || c.visitTime || '',
          cost: c.cost || null,
          weight: c.weight || null
        }));
      }).catch(() => {
        this.records = [];
      });
    },
    openCaseDetail(rec) {
      this.caseDetail = rec || {};
      this.caseDetailVisible = true;
    },
    openPetFromMsg(msg) {
      const data = this.parseMsgContent(msg.content);
      if (!data || !data.id) return;
      const found = this.userPets.find(p => p.id === data.id);
      if (found) {
        this.selectPet(found);
      } else {
        this.$http.get('/petInfo/list/' + this.currentChatUser.id).then(res => {
          const list = res.data.data || res.data || [];
          this.userPets = list;
          const f = this.userPets.find(p => p.id === data.id);
          if (f) this.selectPet(f);
        });
      }
    },
    viewDetail(msg, type) {
      this.detailType = type;
      this.detailData = this.parseMsgContent(msg.content);
      this.detailVisible = true;
    },
    getStatusText(status) {
      const map = {
        'UNPAID': '待付款',
        'PAID': '待发货',
        'SHIPPED': '已发货',
        'RECEIVED': '已收货',
        'COMMENTED': '已评价',
        'REFUNDING': '退款中',
        'REFUNDED': '已退款',
        'CLOSED': '已关闭'
      };
      return map[status] || status;
    }
  }
}
</script>

<style scoped src="@/assets/css/service-home.css"></style>

<style scoped>
.case-detail-dialog .el-dialog__header {
  border-bottom: 1px solid #f2f2f2;
  padding-bottom: 12px;
}

.cd-title-bar {
  display: flex;
  align-items: center;
  gap: 10px;
}

.cd-chip {
  border-radius: 999px;
}

.cd-title-text {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  flex: 1;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.cd-title-cost {
  color: #ff8f2b;
  font-weight: 600;
}

.cd-pet-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.cd-pet-avatar {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  object-fit: cover;
  border: 1px solid #eee;
}

.cd-pet-info {
  flex: 1;
  min-width: 0;
}

.cd-pet-name {
  font-weight: 600;
  color: #333;
  display: flex;
  align-items: center;
  gap: 6px;
}

.sex-icon.male {
  color: #409EFF;
}

.sex-icon.female {
  color: #F56C6C;
}

.cd-pet-meta {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.case-detail-card {
  padding: 14px 6px 6px 6px;
}

.pl-header {
  font-weight: 700;
  color: #333;
  padding: 0 14px;
  border-left: 4px solid #1890ff;
  margin-left: 10px;
}

/* 推荐商品列表样式（与客服端一致） */
.prod-select-panel {
  display: flex;
  flex-direction: column;
}

.psp-header {
  padding: 10px;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
}

.psp-list {
  overflow-y: auto;
  padding: 5px;
  height: 300px;
}

.psp-item {
  display: flex;
  padding: 10px;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  transition: 0.2s;
}

.psp-item:hover {
  background: #f0faff;
}

.psp-img {
  width: 50px;
  height: 50px;
  border-radius: 4px;
  margin-right: 10px;
  border: 1px solid #eee;
}

.psp-info {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.psp-name {
  font-size: 13px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.psp-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.psp-price {
  color: #f56c6c;
  font-weight: bold;
}

.psp-send-btn {
  color: #1890ff;
  font-size: 12px;
}

.psp-empty {
  padding: 20px;
  text-align: center;
  color: #999;
}

.cd-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #909399;
  margin-bottom: 12px;
}

.cd-meta .meta-item i {
  margin-right: 4px;
  color: #a6b1be;
}

.cd-content {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.cd-section {
  background: #fafafa;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 10px 12px;
}

.cd-sec-title {
  font-size: 13px;
  font-weight: 600;
  color: #555;
  margin-bottom: 6px;
}

.cd-sec-body {
  font-size: 13px;
  color: #333;
  line-height: 1.7;
}

.image-msg {
  max-width: 260px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #eee;
  background: #fff;
}

.img-msg {
  width: 100%;
  display: block;
}

.case-detail-card {
  padding: 5px 5px 0 5px;
}

.cd-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.cd-title {
  font-weight: bold;
  color: #333;
  flex: 1;
}

.cd-cost {
  color: #e67e22;
}

.cd-meta {
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: #909399;
  margin-bottom: 10px;
}

.cd-content .cd-section {
  margin-bottom: 8px;
}

.cd-content h4 {
  font-size: 13px;
  color: #666;
  margin: 0 0 4px 0;
}

.cd-content p {
  margin: 0;
  font-size: 13px;
  color: #333;
  line-height: 1.5;
}

.pet-card-msg {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 10px;
  width: 240px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: 0.2s;
}

.pet-card-msg:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.petc-img {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
  margin-right: 10px;
  border: 1px solid #eee;
}

.petc-info {
  flex: 1;
  overflow: hidden;
}

.petc-name {
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.petc-meta {
  font-size: 12px;
  color: #999;
}

.petc-foot {
  font-size: 11px;
  color: #909399;
  border-top: 1px dashed #eee;
  padding-top: 5px;
  text-align: right;
}

/* 宠物手风琴样式 */
.pet-list-scroll-accordion {
  flex: 1;
  overflow-y: auto;
  padding-right: 5px;
}

.pet-accordion-item {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  margin-bottom: 10px;
  overflow: hidden;
  transition: all 0.3s ease;
  background: #fff;
}

.pet-accordion-item.active {
  border-color: #cce5ff;
  background: #fbfdff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.pai-header {
  display: flex;
  align-items: center;
  padding: 10px;
  cursor: pointer;
  background: #fff;
}

.pet-accordion-item.active .pai-header {
  background: #f5faff;
  border-bottom: 1px solid #edf6ff;
}

.pai-img {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  object-fit: cover;
  margin-right: 10px;
}

.pai-info {
  flex: 1;
}

.pai-name {
  font-size: 14px;
  font-weight: bold;
  color: #333;
}

.pai-sub {
  font-size: 12px;
  color: #999;
}

.pai-arrow {
  color: #ccc;
  transition: transform 0.3s;
}

.pai-arrow.rotated {
  transform: rotate(180deg);
  color: #409EFF;
}

.pai-content {
  padding: 0 15px 15px;
  background: #fff;
}

.pai-case-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px dashed #eee;
  margin-bottom: 10px;
}

.pch-title {
  font-weight: bold;
  font-size: 13px;
  color: #333;
}

.mini-timeline {
  padding-left: 2px;
}

.mt-date {
  font-size: 12px;
  color: #409EFF;
  font-weight: bold;
  margin-bottom: 5px;
}

.mt-card {
  background: #f9f9f9;
  border-radius: 6px;
  padding: 8px;
  border: 1px solid #eee;
}

.mt-diag {
  font-weight: bold;
  font-size: 13px;
  color: #333;
  margin-bottom: 4px;
}

.mt-detail {
  font-size: 12px;
  color: #666;
  line-height: 1.4;
  margin-bottom: 2px;
}

.mt-label {
  color: #999;
  margin-right: 4px;
}

.empty-tip-small {
  text-align: center;
  color: #ccc;
  font-size: 12px;
  padding: 10px 0;
}

/* 移除冲突的布局样式，统一使用 service-home.css */
.pl-header {
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
  padding: 0 16px;
}

.pet-list-scroll {
  max-height: 200px;
  overflow-y: auto;
}

.mini-pet-card {
  display: flex;
  gap: 10px;
  padding: 8px;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  margin-bottom: 8px;
  cursor: pointer;
}

.mini-pet-card.active {
  background: #f5faff;
  border-color: #cce5ff;
}

.mini-pet-img {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  object-fit: cover;
}

.mini-pet-info {
  flex: 1;
  overflow: hidden;
}

.mp-name {
  font-size: 14px;
  font-weight: bold;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.mp-sub {
  font-size: 12px;
  color: #999;
}

.timeline-wrap {
  margin-top: 10px;
}

.tw-header {
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
}

.record-card {
  border: none;
}

.empty-tip {
  color: #999;
  font-size: 12px;
  text-align: center;
  padding: 15px;
}

.emr-empty {
  color: #bbb;
  font-size: 13px;
  text-align: center;
  padding-top: 40px;
}
</style>

