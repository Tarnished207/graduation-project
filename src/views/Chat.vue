<template>
  <div class="user-chat-page">
    <div class="chat-layout">
      <div class="left-list">
        <div class="list-header">
          <span class="title">医生列表</span>
          <el-input size="small" v-model="search" placeholder="搜索医生..." prefix-icon="el-icon-search"></el-input>
        </div>
        <div class="doctor-list">
          <div class="doc-item" v-for="d in filteredDoctors" :key="d.id"
               :class="{active: currentDoctor && currentDoctor.id===d.id}" @click="selectDoctor(d)">
            <el-avatar :size="36" :src="getImageUrl(d.avatar)" class="doc-avatar"></el-avatar>
            <div class="doc-info">
              <div class="doc-name">{{ d.nickname || d.username }}</div>
            </div>
            <el-badge :value="unreadMap[d.id]" class="unread-badge" v-if="unreadMap[d.id] > 0"/>
          </div>
          <div v-if="filteredDoctors.length===0" class="empty-tip">暂无医生</div>
        </div>
      </div>

      <div class="chat-main">
        <div class="chat-top">
          <div v-if="currentDoctor" class="chat-title">
            <span class="c-name">{{ currentDoctor.nickname || currentDoctor.username }}</span>
          </div>
          <div v-else class="chat-title empty-title"><i class="el-icon-chat-dot-square"></i> 选择医生开始咨询</div>
        </div>

        <div class="chat-body" ref="chatBody">
          <div v-if="!currentDoctor" class="empty-state">
            <i class="el-icon-first-aid-kit" style="font-size: 60px; color:#e0e0e0; margin-bottom: 20px;"></i>
            <p>请选择医生</p>
          </div>
          <div v-else class="msg-list">
            <div class="msg-row" v-for="(msg,i) in messages" :key="i" :class="msg.senderId===user.id?'me':'other'">
              <el-avatar :size="36" :src="getImageUrl(msg.senderId===user.id?user.avatar:currentDoctor.avatar)"
                         class="msg-avatar"></el-avatar>
              <div class="bubble" v-if="msg.msgType==='TEXT'">{{ msg.content }}</div>
              <div class="image-msg" v-else-if="msg.msgType==='IMAGE'">
                <img :src="getImageUrl(msg.content)" class="img-msg" @error="onImgErr">
              </div>
              <div class="product-card-msg" v-else-if="msg.msgType==='PRODUCT'">
                <img :src="getImageUrl(parseMsgContent(msg.content).image)" class="pc-img">
                <div class="pc-info">
                  <div class="pc-name">{{ parseMsgContent(msg.content).name }}</div>
                  <div class="pc-price">¥{{ parseMsgContent(msg.content).price }}</div>
                </div>
                <div class="pc-foot">点击商品查看详情</div>
              </div>
              <div class="pet-card-msg" v-else-if="msg.msgType==='PET'">
                <img :src="getImageUrl(parseMsgContent(msg.content).avatar)" class="petc-img">
                <div class="petc-info">
                  <div class="petc-name">{{ parseMsgContent(msg.content).nickname }}</div>
                  <div class="petc-meta">{{ parseMsgContent(msg.content).breed || '未知品种' }} ·
                    {{ parseMsgContent(msg.content).age }}岁
                  </div>
                </div>
                <div class="petc-foot">已发送宠物名片</div>
              </div>
            </div>
          </div>
        </div>

        <div class="chat-footer" v-if="currentDoctor">
          <div class="tools-bar">
            <div class="icon-btn" title="发送图片">
              <i class="el-icon-picture-outline" @click="$refs.imgInput.click()"></i>
              <input type="file" ref="imgInput" accept="image/*" style="display:none" @change="uploadImage">
            </div>
            <el-popover placement="top" width="360" trigger="click" @show="loadMyPets">
              <div class="pet-select-panel">
                <div class="ps-header">我的宠物</div>
                <div class="ps-list">
                  <div class="ps-item" v-for="p in myPets" :key="p.id" @click="sendPetCard(p)">
                    <img :src="getImageUrl(p.avatar)" class="ps-img">
                    <div class="ps-info">
                      <div class="ps-name">{{ p.nickname }}</div>
                      <div class="ps-sub">{{ p.breed || '未知品种' }} · {{ p.age }}岁</div>
                    </div>
                    <el-button type="text" size="mini" class="ps-send">发送</el-button>
                  </div>
                  <div v-if="myPets.length===0" class="empty-tip">暂无宠物，去“我的宠物”添加</div>
                </div>
              </div>
              <div class="icon-btn" slot="reference" title="发送宠物名片">
                <i class="el-icon-s-promotion" style="color:#67c23a"></i>
              </div>
            </el-popover>
          </div>
          <div class="input-area">
            <textarea class="input-box" v-model="inputMsg" placeholder="请输入内容... (Enter发送)"
                      @keyup.enter="send"></textarea>
            <div class="send-btn-wrap">
              <el-button type="primary" size="medium" @click="send" style="padding: 8px 25px;">发送</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      user: {},
      doctors: [],
      search: '',
      currentDoctor: null,
      messages: [],
      inputMsg: '',
      myPets: [],
      // 未读消息提醒
      unreadMap: {},
      blinkTimer: null,
      originalTitle: document.title,
      socket: null,
      reconnectTimer: null
    };
  },
  created() {
    const u = sessionStorage.getItem("user");
    if (u) {
      this.user = JSON.parse(u);
      this.loadDoctors();
      this.initWebSocket();
    } else {
      this.$router.push("/login");
    }
  },
  destroyed() {
    if (this.socket) this.socket.close();
    if (this.reconnectTimer) clearTimeout(this.reconnectTimer);
  },
  computed: {
    filteredDoctors() {
      if (!this.search) return this.doctors;
      return this.doctors.filter(d => (d.nickname || d.username || '').includes(this.search));
    }
  },
  methods: {
    // WebSocket
    initWebSocket() {
      if (typeof (WebSocket) === "undefined") {
        console.log("不支持WS");
        return;
      }
      if (!this.user.id) return;

      const wsUrl = `ws://localhost:9090/ws/chat/${this.user.id}`;
      this.socket = new WebSocket(wsUrl);
      this.socket.onopen = () => console.log("用户端WS连接");
      this.socket.onmessage = (msg) => {
        try {
          const data = JSON.parse(msg.data);
          // 使用 == 兼容 String 和 Number 类型的 ID 比较
          if (this.currentDoctor && (data.senderId == this.currentDoctor.id || data.receiverId == this.currentDoctor.id)) {
            this.messages.push(data);
            this.scrollToBottom();
            if (data.senderId == this.currentDoctor.id) {
              this.$http.post('/chat/read', {
                senderId: data.senderId,
                receiverId: this.user.id
              }).then(() => {
                this.$set(this.unreadMap, data.senderId, 0);
                this.loadUnreadCount();
              });
            } else {
              this.loadUnreadCount();
            }
          } else {
            const senderId = data.senderId;
            if (senderId != this.user.id) {
              if (!this.unreadMap[senderId]) this.$set(this.unreadMap, senderId, 0);
              this.unreadMap[senderId]++;
            }
            this.loadUnreadCount();
          }
        } catch (e) {
        }
      };
      this.socket.onclose = () => {
        this.reconnectTimer = setTimeout(() => this.initWebSocket(), 3000);
      };
    },

    getImageUrl(url) {
      if (!url) return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';
      if (url.startsWith('http')) return url;
      if (url.startsWith('/')) return 'http://localhost:9090' + url;
      return 'http://localhost:9090/images/' + url;
    },
    onImgErr(e) {
      e.target.src = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';
    },
    loadDoctors() {
      this.$http.get('/sysUser/list').then(res => {
        const list = res.data || [];
        this.doctors = list.filter(u => u.role === 'DOCTOR');
        // 加载未读数
        this.loadUnreadCount();
      });
    },
    selectDoctor(d) {
      this.currentDoctor = d;
      // 标记已读
      this.$http.post('/chat/read', {
        senderId: d.id,
        receiverId: this.user.id
      });
      // 清除本地未读
      this.$set(this.unreadMap, d.id, 0);

      this.loadHistory(d.id);
    },
    loadHistory(uid) {
      this.$http.get('/chat/history', {params: {uid1: this.user.id, uid2: uid, type: 'DOCTOR'}}).then(res => {
        this.messages = res.data || [];
        this.scrollToBottom();
      });

      // 加载所有未读数
      this.loadUnreadCount();
    },
    loadUnreadCount() {
      this.$http.get('/chat/allMessages', {params: {userId: this.user.id}}).then(res => {
        const msgs = res.data || [];
        const tempUnread = {};
        msgs.forEach(m => {
          if (m.receiverId == this.user.id && (m.isRead === 0 || m.isRead === null)) {
            if (!tempUnread[m.senderId]) tempUnread[m.senderId] = 0;
            tempUnread[m.senderId]++;
          }
        });
        this.unreadMap = tempUnread;
      });
    },
    send() {
      if (!this.inputMsg.trim() || !this.currentDoctor) return;
      const msg = {
        senderId: this.user.id, receiverId: this.currentDoctor.id,
        content: this.inputMsg, msgType: 'TEXT', bizType: 'USER'
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
    loadMyPets() {
      this.$http.get('/petInfo/list/' + this.user.id).then(res => {
        const list = res.data.data || res.data || [];
        this.myPets = list;
      });
    },
    parseMsgContent(content) {
      try {
        if (content && content.startsWith('{')) {
          return JSON.parse(content);
        }
        const parts = (content || '').split('|');
        return {
          name: parts[0] || '',
          price: parts[1] || '',
          image: parts[2] || ''
        };
      } catch (e) {
        return {};
      }
    },
    sendPetCard(p) {
      const data = {
        id: p.id, nickname: p.nickname, breed: p.breed, age: p.age, avatar: p.avatar, msgType: 'PET'
      };
      const msg = {
        senderId: this.user.id, receiverId: this.currentDoctor.id,
        content: JSON.stringify(data), msgType: 'PET', bizType: 'USER'
      };
      this.messages.push(msg);
      this.scrollToBottom();
      this.$http.post('/chat/send', msg);
      this.$message.success("已发送宠物名片");
    },
    uploadImage(e) {
      const file = e.target.files[0];
      if (!file || !this.currentDoctor) {
        e.target.value = '';
        return;
      }
      const fd = new FormData();
      fd.append('file', file);
      this.$http.post('/file/upload', fd).then(res => {
        if (res.data) this.sendImage(res.data);
        else this.$message.error('上传失败');
      }).finally(() => {
        e.target.value = '';
      });
    },
    sendImage(imgUrl) {
      const msg = {
        senderId: this.user.id,
        receiverId: this.currentDoctor.id,
        content: imgUrl,
        msgType: 'IMAGE',
        bizType: 'USER'
      };
      this.messages.push(msg);
      this.scrollToBottom();
      this.$http.post('/chat/send', msg);
    },
    // 处理咨询请求
    handleConsult(product) {
      this.chatVisible = true;
      // 强制切换到第一个客服
      if (this.csList && this.csList.length > 0) {
        // 如果当前聊天对象不是客服列表中的人，或者没有聊天对象，则切换
        const isChattingWithCS = this.targetId && this.csList.some(cs => cs.id === this.targetId);

        if (!isChattingWithCS) {
          const cs = this.csList[0];
          this.targetId = cs.id;
          this.targetName = cs.nickname || cs.username;
          this.targetAvatar = cs.avatar;
          this.messages = []; // 切换对象，清空当前视图消息
          this.loadChatHistory(true); // 加载与该客服的历史记录
        }
      } else {
        this.$message.warning("暂无在线客服");
        return;
      }

      setTimeout(() => {
        if (this.targetId) {
          this.sendProductCard(product);
        }
      }, 500);
    }
  }
}
</script>

<style scoped>
.user-chat-page {
  padding: 20px;
}

.chat-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 20px;
  height: calc(100vh - 80px);
}

.left-list, .chat-main {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.list-header {
  padding: 12px;
  border-bottom: 1px solid #eee;
}

.title {
  font-weight: bold;
  color: #333;
  margin-right: 10px;
}

.doctor-list {
  padding: 10px;
  overflow-y: auto;
  height: calc(100% - 60px);
}

.doc-item {
  display: flex;
  gap: 10px;
  padding: 8px;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  margin-bottom: 8px;
  cursor: pointer;
}

.doc-item.active {
  background: #f5faff;
  border-color: #cce5ff;
}

.doc-avatar {
  flex-shrink: 0;
}

.doc-info {
  flex: 1;
  overflow: hidden;
}

.doc-name {
  font-size: 14px;
  font-weight: bold;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.doc-sub {
  font-size: 12px;
  color: #999;
}

.chat-main {
  display: grid;
  grid-template-rows: 60px 1fr 110px;
}

.chat-top {
  border-bottom: 1px solid #eee;
  padding: 0 15px;
  display: flex;
  align-items: center;
}

.chat-body {
  overflow-y: auto;
  padding: 15px;
  background: #fafafa;
}

.msg-list .msg-row {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}

.msg-row.me {
  flex-direction: row-reverse;
}

.bubble {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 8px 12px;
  max-width: 60%;
}

.image-msg .img-msg {
  max-width: 260px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border: 1px solid #eee;
}

.chat-footer .tools-bar {
  display: flex;
  gap: 10px;
  padding: 8px 12px;
  border-top: 1px solid #eee;
  background: #fbfbfb;
}

.icon-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border-radius: 8px;
  color: #666;
  cursor: pointer;
  transition: 0.2s;
  border: 1px solid transparent;
}

.icon-btn:hover {
  background: #e6f7ff;
  color: #1890ff;
  transform: translateY(-1px);
  border-color: #cce5ff;
}

.chat-footer .input-area {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
  padding: 10px 12px;
}

.input-box {
  width: 100%;
  height: 70px;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 8px 12px;
}

.pet-card-msg {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 10px;
  width: 240px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
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

.product-card-msg {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 10px;
  width: 240px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.pc-img {
  width: 100%;
  height: 120px;
  object-fit: cover;
  border-radius: 6px;
  margin-bottom: 8px;
  border: 1px solid #eee;
}

.pc-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pc-name {
  font-weight: bold;
  color: #333;
  font-size: 13px;
  max-width: 65%;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.pc-price {
  color: #f56c6c;
  font-weight: 700;
}

.pc-foot {
  font-size: 11px;
  color: #999;
  text-align: right;
  border-top: 1px dashed #eee;
  padding-top: 5px;
}

.ps-header {
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
}

.ps-list {
  max-height: 280px;
  overflow-y: auto;
}

.ps-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  border-bottom: 1px dashed #f0f0f0;
}

.ps-img {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid #eee;
}

.ps-info {
  flex: 1;
  overflow: hidden;
}

.ps-name {
  font-size: 14px;
  font-weight: bold;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.ps-sub {
  font-size: 12px;
  color: #999;
}

.ps-send {
  color: #1890ff;
}

.empty-tip {
  color: #999;
  font-size: 12px;
  text-align: center;
  padding: 15px;
}
</style>
