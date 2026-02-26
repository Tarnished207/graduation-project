<template>
  <div v-if="showChatWidget">

    <div class="chat-fab" @click.stop="toggleChat" ref="toggleBtn">
      <i class="el-icon-chat-dot-round" v-if="!chatVisible"></i>
      <i class="el-icon-close" v-else></i>
      <span v-if="!chatVisible" style="margin-left:4px">咨询</span>
      <span class="dot-tip" v-if="!chatVisible && totalUnread > 0"></span>
    </div>

    <transition name="slide-up">
      <div class="chat-window-pro" v-if="chatVisible" ref="chatWindow" @click.stop>

        <div class="cw-sidebar">
          <div class="cw-header">
            <i class="el-icon-service"></i> 官方团队
          </div>
          <div class="cw-list">
            <div v-for="cs in csList" :key="cs.id" class="cw-item" :class="{active: targetId === cs.id}"
                 @click="switchChatTarget(cs)">
              <div class="avatar-box">
                <el-avatar :size="36" :src="getImageUrl(cs.avatar)" shape="square"></el-avatar>
                <div v-if="cs.role==='SERVICE'" class="role-badge cs">服</div>
                <div v-if="cs.role==='DOCTOR'" class="role-badge doc">医</div>
              </div>
              <div class="cw-info">
                <div class="cw-name">{{ cs.nickname || cs.username }}</div>
                <div class="cw-role">{{ getRoleName(cs.role) }}</div>
              </div>
              <el-badge :value="unreadMap[cs.id]" v-if="unreadMap[cs.id] > 0" class="cw-unread"/>
            </div>
            <div v-if="csList.length === 0" class="cw-empty">暂无客服在线</div>
          </div>
        </div>

        <div class="cw-main">
          <div class="cw-top">
            <div class="top-info">
              <span class="t-name" v-if="targetUser">{{ targetUser.nickname || targetUser.username }}</span>
              <span class="t-name" v-else>请选择咨询对象</span>
            </div>
            <i class="el-icon-close close-btn-mini" @click="chatVisible = false" title="关闭窗口"></i>
          </div>

          <div class="cw-body" ref="chatArea" @click="closeAllPopovers">
            <div v-if="!targetId" class="select-guide">
              <i class="el-icon-service" style="font-size: 40px; color:#ddd; margin-bottom:10px"></i>
              <p>👈 请在左侧选择一位 客服 或 医生</p>
            </div>

            <div v-else class="msg-list">
              <div v-for="(msg, index) in messages" :key="index" class="msg"
                   :class="msg.senderId == user.id ? 'right' : 'left'">

                <div class="avatar-s" v-if="msg.senderId != user.id">
                  <img :src="getImageUrl(targetUser.avatar)" class="msg-ava">
                </div>

                <div class="msg-content">
                  <div class="bubble" v-if="msg.msgType === 'TEXT'">{{ msg.content }}</div>

                  <div class="order-bubble" v-else-if="msg.msgType === 'ORDER'" @click.stop="viewDetail(msg, 'ORDER')">
                    <div class="ob-head"><i class="el-icon-s-order"></i> 订单咨询</div>
                    <div class="ob-content">
                      <div class="ob-prod">{{ parseMsgContent(msg.content).name }}</div>
                      <div class="ob-price-row">
                        <span class="ob-price">¥{{ parseMsgContent(msg.content).price }}</span>
                        <span class="ob-status" :class="'st-' + parseMsgContent(msg.content).status">
                          {{ getStatusText(parseMsgContent(msg.content).status) }}
                        </span>
                      </div>
                      <div class="ob-no-row">单号：{{ parseMsgContent(msg.content).no }}</div>
                    </div>
                  </div>

                  <div class="product-bubble" v-else-if="msg.msgType === 'PRODUCT'"
                       @click.stop="viewDetail(msg, 'PRODUCT')">
                    <img :src="getImageUrl(parseMsgContent(msg.content).image)" class="pb-img">
                    <div class="pb-info">
                      <div class="pb-name">{{ parseMsgContent(msg.content).name }}</div>
                      <div class="pb-price">¥{{ parseMsgContent(msg.content).price }}</div>
                    </div>
                    <div class="pb-foot">点击查看详情</div>
                  </div>

                  <div class="image-bubble" v-else-if="msg.msgType === 'IMAGE'">
                    <img :src="getImageUrl(msg.content)" class="ib-img" @error="onImgErr">
                  </div>
                </div>

                <div class="avatar-s" v-if="msg.senderId == user.id">
                  <img :src="getImageUrl(user.avatar)" class="msg-ava">
                </div>
              </div>
            </div>
          </div>

          <div class="cw-footer" v-if="targetId">
            <div class="cw-tools">
              <i class="el-icon-picture-outline tool-btn" title="发送图片" @click.stop="$refs.imgInput.click()"></i>
              <input type="file" ref="imgInput" accept="image/*" style="display:none" @change="uploadImage">
              <el-popover placement="top" width="260" trigger="manual" v-model="showEmoji"
                          popper-class="chat-popper-zindex">
                <div class="emoji-grid">
                  <span v-for="e in emojis" :key="e" class="emo-item" @click="appendEmoji(e)">{{ e }}</span>
                </div>
                <i class="el-icon-sunny tool-btn" slot="reference" title="表情" @click.stop="toggleTool('emoji')"></i>
              </el-popover>

              <el-popover placement="top" width="340" trigger="manual" v-model="showOrder"
                          popper-class="chat-popper-zindex">
                <div class="mini-list">
                  <div class="ml-header">我的订单 (按紧急程度排序)</div>
                  <div v-for="o in myOrderList" :key="o.id" class="ml-item" @click="sendOrderCard(o)">
                    <div class="ml-info">
                      <div class="ml-name">{{ o.productName }}</div>
                      <div class="ml-sub-row">
                        <span class="ml-price">¥{{ o.totalAmount }}</span>
                        <span class="ml-status-badge" :class="'st-' + o.status">{{ getStatusText(o.status) }}</span>
                      </div>
                      <div class="ml-no-mini">单号: {{ o.orderNo }}</div>
                    </div>
                    <i class="el-icon-s-promotion send-icon"></i>
                  </div>
                  <div v-if="myOrderList.length===0" style="padding:20px;text-align:center;color:#999">暂无订单</div>
                </div>
                <div class="badge-wrapper" slot="reference" @click.stop="toggleTool('order')">
                  <i class="el-icon-s-order tool-btn" title="发送订单"></i>
                  <span class="dot-tip" v-if="hasPendingOrders"></span>
                </div>
              </el-popover>
            </div>

            <div class="cw-input-box">
              <input v-model="inputMsg" placeholder="请输入消息..." @keyup.enter="sendMessage"
                     @focus="closeAllPopovers">
              <button @click="sendMessage" :disabled="!inputMsg.trim()">发送</button>
            </div>
          </div>
        </div>
      </div>
    </transition>

    <el-dialog
        :visible.sync="detailVisible"
        :title="detailType === 'PRODUCT' ? '商品详情' : '订单详情'"
        width="680px"
        top="8vh"
        custom-class="modern-detail-modal chat-detail-modal"
        append-to-body>

      <div v-if="detailType === 'PRODUCT'" class="cd-container">
        <div class="cd-product-layout">
          <img :src="getImageUrl(currentDetail.image || currentDetail.img)" class="cd-img-lg">
          <div class="cd-info-right">
            <div class="cd-title">{{ currentDetail.name }}</div>
            <div class="cd-price">¥ {{ currentDetail.price }}</div>
            <div class="cd-desc" v-if="currentDetail.description">{{ currentDetail.description }}</div>
            <el-button type="primary" size="small" round @click="goToMall">
              去商城查看
            </el-button>
          </div>
        </div>
        <div class="cd-content ql-editor" v-html="currentDetail.content" v-if="currentDetail.content"></div>
      </div>

      <div v-if="detailType === 'ORDER'" class="cd-container order-detail-layout">
        <div class="order-steps">
          <el-steps :active="getOrderStep(currentDetail.status)" finish-status="success" align-center
                    style="margin-bottom: 25px;">
            <el-step title="下单" icon="el-icon-shopping-cart-full"></el-step>
            <el-step title="支付" icon="el-icon-wallet"></el-step>
            <el-step title="发货" icon="el-icon-truck"></el-step>
            <el-step title="完成" icon="el-icon-circle-check"></el-step>
          </el-steps>
        </div>

        <el-descriptions title="订单信息" :column="2" border size="small" class="margin-top">
          <el-descriptions-item label="订单号">{{ currentDetail.no || currentDetail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ currentDetail.createTime | fmtDate }}</el-descriptions-item>
          <el-descriptions-item label="买家昵称">{{ currentDetail.buyerName || user.nickname }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{
              currentDetail.buyerPhone || user.phone || '未填写'
            }}
          </el-descriptions-item>
          <el-descriptions-item label="商品名称">{{
              currentDetail.name || currentDetail.productName
            }}
          </el-descriptions-item>
          <el-descriptions-item label="购买数量">
            <span style="font-weight:bold;">x {{ currentDetail.quantity }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="实付金额">
            <span style="color:#f56c6c; font-weight:bold;">¥{{
                currentDetail.price || currentDetail.totalAmount
              }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <div class="address-box">
          <div class="addr-title"><i class="el-icon-location-information"></i> 收货地址</div>
          <div class="addr-text">{{ currentDetail.address }}</div>
        </div>

        <div class="od-product-card">
          <img :src="getImageUrl(currentDetail.image || currentDetail.img)" class="od-img-sm">
          <div class="od-info-box">
            <div class="od-name-row">{{ currentDetail.name || currentDetail.productName }}</div>
            <div class="od-desc-row" v-if="currentDetail.description">{{ currentDetail.description }}</div>
            <div class="od-price-row">¥ {{ currentDetail.price || currentDetail.totalAmount }}</div>
          </div>
        </div>
      </div>
    </el-dialog>

  </div>
</template>

<script>
export default {
  name: "Chat",
  data() {
    return {
      user: {},
      chatVisible: false,
      inputMsg: '',
      messages: [],
      targetId: null,
      targetUser: null,
      csList: [],
      myOrderList: [],
      chatTimer: null,
      emojis: ['😀', '😁', '😂', '🤣', '😃', '😄', '😅', '😆', '😉', '😊', '😋', '😎', '😍', '😘', '🥰', '😗', '😙', '😚', '🙂', '🤗', '🤩', '🤔', '🤨', '😐', '😑', '😶', '🙄', '😏', '😣', '😥', '😮', '🤐', '😯', '😪', '😫', '😴', '😌', '😛', '😜', '😝', '🤤', '😒', '😓', '😔', '😕', '🙃', '🤑', '😲', '☹️', '🙁', '😖', '😞', '😟', '😤', '😢', '😭', '😦', '😧', '😨', '😩', '🤯', '😬', '😰', '😱', '🥵', '🥶', '😳', '🤪', '😵', '😡', '😠', '🤬', '😷', '🤒', '🤕', '🤢', '🤮', '🤧', '😇', '🥳', '🥺', '🤠', '🤡', '🤥', '🤫', '🤭', '🧐', '🤓', '😈', '👿', '👹', '👺', '💀', '👻', '👽', '🤖', '💩'],

      detailVisible: false,
      detailType: '',
      currentDetail: {},

      showEmoji: false,
      showOrder: false,
      socket: null,
      reconnectTimer: null,
      unreadMap: {},
      originalTitle: document.title
    }
  },
  filters: {
    fmtDate(val) {
      if (!val) return '-';
      return val.replace('T', ' ');
    }
  },
  computed: {
    showChatWidget() {
      if (!this.user || !this.user.id) return false;
      if (this.user.role !== 'USER') return false;
      const noChatRoutes = ['Login', 'Register', 'AdminLogin'];
      const routeRole = (this.$route && this.$route.meta && this.$route.meta.role) || 'USER';
      return routeRole === 'USER' && !noChatRoutes.includes(this.$route.name);
    },
    hasPendingOrders() {
      return this.myOrderList.some(o => o.status === 0 || o.status === 1);
    },
    totalUnread() {
      const map = this.unreadMap || {};
      return Object.values(map).reduce((sum, v) => sum + v, 0);
    }
  },
  watch: {
    $route() {
      this.refreshUser();
    },
    chatVisible(val) {
      if (val) {
        setTimeout(() => document.addEventListener('click', this.handleOutsideClick), 0);
        if (this.targetId && (this.unreadMap[this.targetId] || 0) > 0) {
          this.markCurrentChatRead();
        }
      } else {
        document.removeEventListener('click', this.handleOutsideClick);
        this.closeAllPopovers();
      }
    },
    totalUnread(val) {
      if (this.$root) {
        this.$root.$emit('consult-unread-changed', val);
      }
    }
  },
  created() {
    this.refreshUser();
    window.addEventListener('setItem', () => this.refreshUser());

    // 【新增】监听全局事件，用于从商城唤起聊天
    this.$root.$on('open-consult', this.handleConsult);

    // 初始化WebSocket
    if (this.user.id) {
      this.initWebSocket();
    }
  },
  mounted() {
    // 移除旧的轮询
    // this.chatTimer = setInterval(() => {
    //   if (this.chatVisible && this.targetId) this.loadChatHistory(true);
    // }, 2000);
  },
  beforeDestroy() {
    if (this.socket) this.socket.close();
    if (this.reconnectTimer) clearTimeout(this.reconnectTimer);
    document.removeEventListener('click', this.handleOutsideClick);
    this.$root.$off('open-consult', this.handleConsult);
  },
  methods: {
    goToMall() {
      this.detailVisible = false;
      this.$router.push('/front/mall').catch(err => {
      });
    },
    // 移除闪烁相关方法
    refreshUser() {
      const u = sessionStorage.getItem("user");
      this.user = u ? JSON.parse(u) : {};
      if (this.user.id) {
        this.loadInitialData();
        this.loadUnreadCount();
        if (!this.socket) this.initWebSocket();
      } else {
        if (this.socket) {
          this.socket.close();
          this.socket = null;
        }
      }
    },

    // WebSocket初始化
    initWebSocket() {
      if (typeof (WebSocket) === "undefined") {
        console.log("您的浏览器不支持WebSocket");
        return;
      }
      if (!this.user.id) return;

      const wsUrl = `ws://localhost:9090/ws/chat/${this.user.id}`;
      this.socket = new WebSocket(wsUrl);

      this.socket.onopen = () => {
        console.log("WebSocket已连接");
      };

      this.socket.onmessage = (msg) => {
        try {
          const data = JSON.parse(msg.data);
          const isCurrentChat =
              this.chatVisible &&
              this.targetId &&
              (data.senderId === this.targetId || data.receiverId === this.targetId);

          if (isCurrentChat) {
            this.messages.push(data);
            this.scrollToBottom();
            if (data.senderId === this.targetId) {
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
            if (senderId !== this.user.id) {
              if (!this.unreadMap[senderId]) this.$set(this.unreadMap, senderId, 0);
              this.unreadMap[senderId]++;
            }
            this.loadUnreadCount();
          }
        } catch (e) {
        }
      };

      this.socket.onclose = () => {
        console.log("WebSocket已关闭，尝试重连...");
        this.reconnectTimer = setTimeout(() => {
          this.initWebSocket();
        }, 3000);
      };

      this.socket.onerror = () => {
        console.log("WebSocket发生了错误");
      };
    },

    markCurrentChatRead() {
      if (!this.targetId || !this.user.id) return;
      this.$http.post('/chat/read', {
        senderId: this.targetId,
        receiverId: this.user.id
      }).then(() => {
        this.$set(this.unreadMap, this.targetId, 0);
        this.loadUnreadCount();
      });
    },

    // 处理咨询请求
    handleConsult(product) {
      this.chatVisible = true;
      this.loadUnreadCount();

      // 自动选择咨询对象逻辑：优先选择客服
      const isCurrentService = this.targetUser && this.targetUser.role === 'SERVICE';
      if (!isCurrentService) {
        // 尝试寻找客服
        const serviceRep = this.csList.find(u => u.role === 'SERVICE');
        if (serviceRep) {
          this.switchChatTarget(serviceRep);
        } else if (!this.targetId && this.csList.length > 0) {
          // 如果当前未选中任何人且没找到客服，默认选中列表第一位（可能是医生）
          this.switchChatTarget(this.csList[0]);
        }
      }

      setTimeout(() => {
        if (product && this.targetId) {
          this.sendProductCard(product);
        }
      }, 500);
    },

    handleOutsideClick(e) {
      const chatWindow = this.$refs.chatWindow;
      const toggleBtn = this.$refs.toggleBtn;
      const isPopover = e.target.closest('.el-popover');
      const isDetailModal = e.target.closest('.chat-detail-modal');
      const isDialogWrapper = e.target.closest('.el-dialog__wrapper');

      if (isDetailModal || isDialogWrapper) return;

      if (chatWindow && !chatWindow.contains(e.target) &&
          toggleBtn && !toggleBtn.contains(e.target) &&
          !isPopover) {
        this.chatVisible = false;
        this.closeAllPopovers();
      } else if (!isPopover && !e.target.closest('.tool-btn')) {
        this.closeAllPopovers();
      }
    },

    toggleTool(type) {
      if (type === 'emoji') {
        this.showEmoji = !this.showEmoji;
        this.showOrder = false;
      } else if (type === 'order') {
        this.showOrder = !this.showOrder;
        this.showEmoji = false;
      }
    },

    closeAllPopovers() {
      this.showEmoji = false;
      this.showOrder = false;
    },

    loadInitialData() {
      this.$http.get('/sysUser/list').then(res => {
        this.csList = res.data.filter(u => ['SERVICE', 'DOCTOR'].includes(u.role));
      });
      this.$http.get('/orders/list').then(res => {
        const list = res.data.data || res.data;
        let myOrders = list.filter(o => o.userId === this.user.id);
        myOrders.sort((a, b) => {
          const weightA = (a.status === 0 ? 10 : (a.status === 1 ? 9 : 0));
          const weightB = (b.status === 0 ? 10 : (b.status === 1 ? 9 : 0));
          if (weightA !== weightB) return weightB - weightA;
          return b.id - a.id;
        });
        this.myOrderList = myOrders.slice(0, 20);
      });
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
    getCartItemImage(item) {
      if (item.image) return this.getImageUrl(item.image);
      const prod = this.allProducts.find(p => p.id === item.productId);
      if (prod && prod.image) return this.getImageUrl(prod.image);
      return 'http://localhost:9090/images/default.png';
    },
    toggleChat() {
      this.chatVisible = !this.chatVisible;
      if (this.chatVisible) {
        this.loadUnreadCount();
      }
    },
    switchChatTarget(cs) {
      this.targetId = cs.id;
      this.targetUser = cs;
      this.$http.post('/chat/read', {
        senderId: cs.id,
        receiverId: this.user.id
      }).then(() => {
        this.$set(this.unreadMap, cs.id, 0);
        this.loadUnreadCount();
      });
      this.loadChatHistory(false).then(data => {
        this.scrollToBottom();
      });
    },
    loadChatHistory(isSilent = false) {
      if (!this.targetId || !this.user.id) return Promise.resolve([]);
      return this.$http.get('/chat/history', {
        params: {uid1: this.user.id, uid2: this.targetId, type: this.targetUser.role}
      }).then(res => {
        if (res.data && JSON.stringify(res.data) !== JSON.stringify(this.messages)) {
          this.messages = res.data;
          if (!isSilent) this.scrollToBottom();
        }
        return res.data || [];
      });
    },
    sendMessage() {
      if (!this.inputMsg.trim()) return;
      this.doSend({
        senderId: this.user.id, receiverId: this.targetId,
        content: this.inputMsg, msgType: 'TEXT', bizType: this.targetUser.role
      });
    },
    doSend(msg) {
      this.messages.push(msg);
      this.inputMsg = '';
      this.scrollToBottom();
      this.$http.post('/chat/send', msg);
      this.closeAllPopovers();
    },
    appendEmoji(e) {
      this.inputMsg += e;
    },
    sendOrderCard(o) {
      const data = {
        id: o.id, no: o.orderNo, name: o.productName, price: o.totalAmount, status: o.status,
        image: o.image || o.img || o.productImage, createTime: o.createTime, address: o.address,
        buyerName: this.user.nickname, buyerPhone: this.user.phone,
        quantity: o.quantity,
        description: o.productDescription
      };
      this.doSend({
        senderId: this.user.id, receiverId: this.targetId,
        content: JSON.stringify(data), msgType: 'ORDER', bizType: this.targetUser.role
      });
    },
    sendProductCard(p) {
      const data = {
        id: p.id, name: p.name, price: p.price, image: p.img || p.image, description: p.description
      };
      this.doSend({
        senderId: this.user.id, receiverId: this.targetId,
        content: JSON.stringify(data), msgType: 'PRODUCT', bizType: this.targetUser.role
      });
    },
    uploadImage(e) {
      const file = e.target.files[0];
      if (!file || !this.targetId) {
        e.target.value = '';
        return;
      }
      const fd = new FormData();
      fd.append('file', file);
      this.$http.post('/file/upload', fd).then(res => {
        if (res.data) {
          const msg = {
            senderId: this.user.id,
            receiverId: this.targetId,
            content: res.data,
            msgType: 'IMAGE',
            bizType: this.targetUser.role
          };
          this.doSend(msg);
        } else {
          this.$message.error('上传失败');
        }
      }).finally(() => {
        e.target.value = '';
      });
    },
    parseMsgContent(content) {
      if (!content) return {};
      try {
        if (content.startsWith('{')) return JSON.parse(content);
        const parts = content.split('|');
        return {name: parts[0] || '未知', price: parts[1] || '0', image: parts[2] || '', status: '99', no: '-'};
      } catch (e) {
        return {name: '错误', price: 0};
      }
    },
    viewDetail(msg, type) {
      const data = this.parseMsgContent(msg.content);
      this.detailType = type;
      this.currentDetail = data;
      this.detailVisible = true;
      if (type === 'PRODUCT' && data.id) {
        this.$http.get('/product/list', {params: {name: data.name}}).then(res => {
          const list = res.data.data || res.data;
          const found = list.find(p => p.id === data.id);
          if (found) this.currentDetail = found;
        });
      }
    },
    getStatusText(status) {
      return {0: '待支付', 1: '待发货', 2: '已发货', 3: '已完成', '-1': '已取消'}[status] || '未知';
    },
    getOrderStep(status) {
      const s = Number(status);
      if (s === -1) return 0;
      return s + 1;
    },
    getRoleName(role) {
      return {'SERVICE': '商品客服', 'DOCTOR': '宠物医生'}[role] || '客服';
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const d = this.$refs.chatArea;
        if (d) d.scrollTop = d.scrollHeight;
      });
    },
    loadUnreadCount() {
      if (!this.user.id) return;
      this.$http.get('/chat/allMessages', {params: {userId: this.user.id}}).then(res => {
        const msgs = res.data || [];
        const temp = {};
        msgs.forEach(m => {
          if (m.receiverId === this.user.id && (m.isRead === 0 || m.isRead === null)) {
            if (!temp[m.senderId]) temp[m.senderId] = 0;
            temp[m.senderId]++;
          }
        });
        this.unreadMap = temp;
      });
    }
  }
}
</script>

<style>
.chat-popper-zindex {
  z-index: 10001 !important;
}

.chat-detail-modal {
  border-radius: 12px;
  overflow: hidden;
}
</style>

<style scoped>
.chat-fab {
  position: fixed;
  bottom: 40px;
  right: 40px;
  background: #333;
  color: white;
  padding: 12px 24px;
  border-radius: 50px;
  cursor: pointer;
  z-index: 9999;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: 0.3s;
}

.chat-fab:hover {
  transform: scale(1.05);
  background: #ff9f43;
}

.chat-window-pro {
  position: fixed;
  bottom: 100px;
  right: 40px;
  width: 750px;
  height: 550px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 15px 50px rgba(0, 0, 0, 0.2);
  z-index: 1500;
  display: flex;
  overflow: hidden;
  font-family: sans-serif;
  border: 1px solid #eee;
}

.cw-sidebar {
  width: 180px;
  background: #f9f9f9;
  border-right: 1px solid #eee;
  display: flex;
  flex-direction: column;
}

.cw-header {
  padding: 15px;
  font-weight: bold;
  color: #666;
  border-bottom: 1px solid #eee;
  background: #fafafa;
}

.cw-list {
  flex: 1;
  overflow-y: auto;
}

.cw-item {
  display: flex;
  align-items: center;
  padding: 12px 15px;
  cursor: pointer;
  transition: 0.2s;
  border-left: 3px solid transparent;
}

.cw-item:hover {
  background: #eee;
}

.cw-item.active {
  background: #e6f7ff;
  border-left-color: #1890ff;
}

.cw-item .cw-info {
  margin-left: 10px;
  overflow: hidden;
}

.cw-item .cw-name {
  font-size: 13px;
  font-weight: bold;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cw-item .cw-role {
  font-size: 11px;
  color: #999;
}

.role-badge {
  position: absolute;
  bottom: -2px;
  right: -2px;
  font-size: 10px;
  color: white;
  border-radius: 4px;
  padding: 1px 3px;
  transform: scale(0.8);
}

.role-badge.cs {
  background: #E6A23C;
}

.role-badge.doc {
  background: #409EFF;
}

.cw-empty {
  padding: 20px;
  text-align: center;
  color: #999;
  font-size: 12px;
}

.avatar-box {
  position: relative;
}

.cw-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.cw-top {
  height: 50px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: #fbfbfb;
}

.t-name {
  font-weight: bold;
  font-size: 14px;
  color: #333;
}

.t-status {
  font-size: 12px;
  color: #666;
  margin-left: 10px;
}

.close-btn-mini {
  font-size: 18px;
  color: #999;
  cursor: pointer;
  transition: 0.2s;
}

.close-btn-mini:hover {
  color: #f56c6c;
  transform: rotate(90deg);
}

.cw-body {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
}

.select-guide {
  text-align: center;
  margin-top: 100px;
  color: #999;
}

.msg-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.msg {
  display: flex;
  gap: 10px;
  max-width: 80%;
}

.msg.left {
  align-self: flex-start;
}

.msg.right {
  align-self: flex-end;
  justify-content: flex-end;
}

.msg-ava {
  width: 36px;
  height: 36px;
  border-radius: 4px;
}

.bubble {
  padding: 10px 14px;
  border-radius: 8px;
  background: white;
  font-size: 13px;
  line-height: 1.5;
  color: #333;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.right .bubble {
  background: #95ec69;
}

.image-bubble .ib-img {
  max-width: 240px;
  border-radius: 8px;
  border: 1px solid #eee;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.order-bubble {
  background: white;
  border: 1px solid #d9ecff;
  border-radius: 8px;
  width: 250px;
  overflow: hidden;
  cursor: pointer;
  transition: 0.2s;
}

.order-bubble:active {
  transform: scale(0.98);
}

.ob-head {
  background: #f0faff;
  padding: 8px 12px;
  font-size: 12px;
  color: #1890ff;
  font-weight: bold;
}

.ob-content {
  padding: 12px;
}

.ob-prod {
  font-size: 13px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
  line-height: 1.4;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.ob-price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.ob-price {
  color: #f56c6c;
  font-weight: bold;
  font-size: 14px;
}

.ob-status {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
  border: 1px solid transparent;
}

.st-0 {
  color: #ff4d4f;
  background: #fff1f0;
  border-color: #ffccc7;
}

.st-1 {
  color: #13c2c2;
  background: #e6fffb;
  border-color: #87e8de;
}

.st-2 {
  color: #fa8c16;
  background: #fff7e6;
  border-color: #ffd591;
}

.st-3 {
  color: #1890ff;
  background: #e6f7ff;
  border-color: #91d5ff;
}

.st--1 {
  color: #909399;
  background: #f5f5f5;
  border-color: #d9d9d9;
}

.ob-no-row {
  font-size: 11px;
  color: #999;
  font-family: monospace;
  border-top: 1px dashed #eee;
  padding-top: 6px;
}

.product-bubble {
  background: white;
  border-radius: 8px;
  padding: 8px;
  width: 200px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: 0.2s;
}

.product-bubble:active {
  transform: scale(0.98);
}

.pb-img {
  width: 100%;
  height: 120px;
  object-fit: cover;
  border-radius: 4px;
  margin-bottom: 5px;
}

.pb-name {
  font-size: 13px;
  font-weight: bold;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.pb-price {
  color: #f56c6c;
  font-weight: bold;
  margin-top: 5px;
}

.pb-foot {
  font-size: 10px;
  color: #aaa;
  text-align: right;
  margin-top: 5px;
  border-top: 1px dashed #eee;
  padding-top: 3px;
}

.cw-footer {
  border-top: 1px solid #eee;
  padding: 10px;
  background: #fff;
}

.cw-tools {
  display: flex;
  gap: 15px;
  margin-bottom: 8px;
  padding-left: 5px;
}

.tool-btn {
  font-size: 20px;
  color: #666;
  cursor: pointer;
  transition: 0.2s;
}

.tool-btn:hover {
  color: #1890ff;
}

.badge-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  cursor: pointer;
}

.dot-tip {
  position: absolute;
  top: -2px;
  right: -2px;
  width: 8px;
  height: 8px;
  background: #f56c6c;
  border-radius: 50%;
}

.cw-input-box {
  display: flex;
  gap: 10px;
}

.cw-input-box input {
  flex: 1;
  border: 1px solid #eee;
  background: #f9f9f9;
  border-radius: 4px;
  padding: 8px 12px;
  outline: none;
}

.cw-input-box button {
  border: none;
  background: #1890ff;
  color: white;
  padding: 0 20px;
  border-radius: 4px;
  cursor: pointer;
}

.cw-input-box button:disabled {
  background: #ccc;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 5px;
  max-height: 200px;
  overflow-y: auto;
  padding: 10px;
}

.emo-item {
  cursor: pointer;
  font-size: 20px;
  padding: 5px;
  text-align: center;
}

.emo-item:hover {
  background: #f0f0f0;
}

.mini-list {
  max-height: 280px;
  overflow-y: auto;
  padding: 5px;
}

.ml-header {
  font-size: 12px;
  color: #999;
  margin-bottom: 5px;
  padding-left: 5px;
  border-bottom: 1px solid #eee;
  padding-bottom: 5px;
}

.ml-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px;
  border-bottom: 1px dashed #eee;
  cursor: pointer;
}

.ml-item:hover {
  background: #f0faff;
}

.ml-info {
  flex: 1;
  overflow: hidden;
  margin-right: 10px;
}

.ml-name {
  font-size: 12px;
  font-weight: bold;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 3px;
}

.ml-sub-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ml-price {
  color: #f56c6c;
  font-size: 12px;
  font-weight: bold;
}

.ml-no-mini {
  font-size: 10px;
  color: #ccc;
  margin-top: 2px;
  font-family: monospace;
}

.send-icon {
  color: #1890ff;
}

.ml-img-sm {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  border: 1px solid #eee;
}

.ml-status-badge {
  font-size: 10px;
  padding: 1px 4px;
  border-radius: 3px;
}

.slide-up-enter-active, .slide-up-leave-active {
  transition: all 0.3s;
}

.slide-up-enter, .slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

/* --- 详情弹窗样式 --- */
.cd-container {
  padding: 20px;
}
/* 商品详情布局 */
.cd-product-layout {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.cd-img-lg {
  width: 240px;
  height: 240px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
}

.cd-info-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.cd-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.cd-price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 8px;
  font-family: 'DIN', sans-serif;
}

.cd-desc {
  font-size: 13px;
  color: #666;
  background: #f5f5f5;
  padding: 10px;
  border-radius: 6px;
  line-height: 1.5;
  margin-bottom: 10px;
}

.cd-content {
  font-size: 14px;
  line-height: 1.6;
  color: #555;
  max-height: 250px;
  overflow-y: auto;
  padding-top: 10px;
  border-top: 1px dashed #eee;
}

/* 订单详情布局 (美化版) */
.order-detail-layout {
  padding: 15px;
}

.order-steps {
  margin-bottom: 30px;
}

.address-box {
  background: #fdfdfd;
  padding: 15px;
  border-radius: 8px;
  border: 1px dashed #eee;
  margin-top: 20px;
}

.addr-title {
  font-weight: bold;
  margin-bottom: 8px;
  font-size: 14px;
  color: #333;
}

.addr-text {
  color: #666;
  font-size: 13px;
  line-height: 1.5;
}

.od-product-card {
  background: #fafafa;
  border-radius: 8px;
  padding: 12px;
  display: flex;
  gap: 12px;
  margin-top: 20px;
  align-items: center;
  border: 1px solid #f0f0f0;
}

.od-img-sm {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  object-fit: cover;
  border: 1px solid #eee;
}

.od-info-box {
  flex: 1;
}

.od-name-row {
  font-size: 13px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.od-desc-row {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  max-width: 400px;
}

.od-price-row {
  color: #f56c6c;
  font-weight: bold;
  font-size: 14px;
}
</style>
