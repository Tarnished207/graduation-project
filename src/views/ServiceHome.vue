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
          <div class="role-badge"><i class="el-icon-service"></i> 在线客服</div>
        </div>
        <el-tooltip content="退出登录" placement="right">
          <i class="el-icon-switch-button logout-icon" @click="logout"></i>
        </el-tooltip>
      </div>

      <div class="search-wrap">
        <el-input placeholder="搜索联系人..." prefix-icon="el-icon-search" size="small" v-model="contactSearch" class="seamless-input"></el-input>
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
          <span>暂无咨询</span>
        </div>
      </div>
    </div>

    <div class="chat-container">
      <div class="chat-header">
        <div v-if="currentChatUser" class="chat-title">
          <span class="c-name">{{ currentChatUser.nickname }}</span>
        </div>
        <div v-else class="chat-title empty-title">
          <i class="el-icon-monitor"></i> 客服工作台
        </div>
      </div>

      <div class="chat-body" ref="chatBody">
        <div v-if="!currentChatUser" class="empty-state">
          <i class="el-icon-service" style="font-size: 60px; color:#e0e0e0; margin-bottom: 20px;"></i>
          <p>请从左侧选择一位客户开始接待</p>
        </div>

        <div v-else class="msg-list">
          <div v-for="(msg, i) in messages" :key="i" class="msg-row" :class="msg.senderId === user.id ? 'me' : 'other'">
            <el-avatar :size="36" :src="getImageUrl(msg.senderId === user.id ? user.avatar : currentChatUser.avatar)" class="msg-avatar" @error="errorHandler">
              <i class="el-icon-user-solid"></i>
            </el-avatar>

            <div class="bubble" v-if="msg.msgType === 'TEXT'">{{ msg.content }}</div>

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
                <el-button type="text" size="mini" @click="locateOrder(msg.content)">定位订单 &gt;</el-button>
              </div>
            </div>

            <div class="product-card-msg" v-else-if="msg.msgType === 'PRODUCT'" @click="viewDetail(msg, 'PRODUCT')">
              <img :src="getImageUrl(parseMsgContent(msg.content).image)" class="pc-img">
              <div class="pc-info">
                <div class="pc-name">{{ parseMsgContent(msg.content).name }}</div>
                <div class="pc-price">¥{{ parseMsgContent(msg.content).price }}</div>
              </div>
              <div class="pc-foot">点击查看详情</div>
            </div>

            <div class="image-msg" v-else-if="msg.msgType === 'IMAGE'">
              <img :src="getImageUrl(msg.content)" class="img-msg" @error="onImgErr">
            </div>

          </div>
        </div>
      </div>

      <div class="chat-footer" v-if="currentChatUser">
        <div class="tools-bar">
          <el-popover placement="top" width="300" trigger="click">
            <div class="emoji-grid">
              <span v-for="e in emojis" :key="e" class="emo-item" @click="appendInput(e)">{{ e }}</span>
            </div>
            <div class="icon-btn" slot="reference" title="表情">😃</div>
          </el-popover>

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

          <el-popover placement="top" width="320" trigger="click">
            <div class="quick-list">
              <div class="ql-header">常用话术</div>
              <div v-for="(phrase, idx) in quickPhrases" :key="idx" class="ql-item" @click="appendInput(phrase)">
                {{ phrase }}
              </div>
            </div>
            <div class="quick-reply-pill" slot="reference">
              <i class="el-icon-lightning"></i> 快捷语
            </div>
          </el-popover>

        </div>

        <div class="input-area">
          <textarea class="input-box" v-model="inputMsg" placeholder="请输入回复内容... (Enter发送)" @keyup.enter="send"></textarea>
          <div class="send-btn-wrap">
            <el-button type="primary" size="medium" @click="send" style="padding: 8px 25px;">发送</el-button>
          </div>
        </div>
      </div>
    </div>

    <div class="crm-panel">
      <div class="user-card-wrap" v-if="currentChatUser">
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
            <span class="val">{{ currentChatUser.createTime ? currentChatUser.createTime.substring(0,10) : '-' }}</span>
          </div>
        </div>

        <div class="sticky-note">
          <div class="note-head">
            <span><i class="el-icon-edit-outline"></i> 客户备注</span>
            <el-link type="primary" :underline="false" @click="saveMemo" style="font-size:12px">保存</el-link>
          </div>
          <el-input type="textarea" :rows="2" placeholder="在此记录客户信息..." v-model="userMemo" class="note-input" resize="none"></el-input>
        </div>
      </div>

      <div class="order-manage-wrap" v-if="currentChatUser">
        <div class="om-header">
          <span class="title">交易记录 <span class="count">({{ userOrders.length }})</span></span>
        </div>

        <div class="om-filter">
          <el-input v-model="crmSearch" placeholder="搜订单号" prefix-icon="el-icon-search" size="mini" style="width: 140px; margin-right: 10px;"></el-input>
          <el-select v-model="crmTab" placeholder="状态" size="mini" style="flex:1">
            <el-option label="全部" value="all"></el-option>
            <el-option label="待发货" value="1"></el-option>
            <el-option label="已发货" value="2"></el-option>
            <el-option label="已完成" value="3"></el-option>
            <el-option label="已取消" value="-1"></el-option>
          </el-select>
        </div>

        <div class="order-list-scroll">
          <div
              v-for="order in filteredOrders"
              :key="order.id"
              class="mini-order-card"
              :id="'order-'+order.orderNo"
              :class="{ 'highlight-anim': highlightOrderNo === order.orderNo }"
          >
            <div class="moc-top">
              <span class="moc-no" :title="order.orderNo">{{ order.orderNo }}</span>
              <span class="moc-status" :class="'s-'+order.status">{{ getStatusText(order.status) }}</span>
            </div>
            <div class="moc-mid">
              <img :src="getImageUrl(order.productImage)" class="moc-img">
              <div class="moc-info">
                <div class="moc-name" :title="order.productName">{{ order.productName }}</div>
                <div class="moc-price">¥{{ order.totalAmount }}</div>
              </div>
            </div>
            <div class="moc-bot">
              <span class="moc-date">{{ order.createTime | fmtShortDate }}</span>
              <div class="moc-btns">
                <el-link type="info" :underline="false" @click="openOrderDetail(order)">详情</el-link>
              </div>
            </div>
          </div>

          <div v-if="filteredOrders.length===0" class="empty-orders">
            无相关订单
          </div>
        </div>
      </div>

      <div v-if="!currentChatUser" class="crm-empty-placeholder">
        <i class="el-icon-s-custom"></i>
        <p>暂无客户档案</p>
      </div>
    </div>

    <el-dialog title="订单详情" :visible.sync="orderDetailVisible" width="650px" append-to-body
               custom-class="detail-dialog">
      <div class="detail-content" v-if="currentOrder.id">
        <el-steps :active="currentStep" finish-status="success" simple style="margin-bottom: 25px;">
          <el-step title="下单" icon="el-icon-shopping-cart-full"></el-step>
          <el-step title="支付" icon="el-icon-wallet"></el-step>
          <el-step title="发货" icon="el-icon-truck"></el-step>
          <el-step title="完成" icon="el-icon-circle-check"></el-step>
        </el-steps>

        <el-descriptions title="基础信息" :column="2" border size="small">
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ currentOrder.createTime | fmtDate }}</el-descriptions-item>
          <el-descriptions-item label="买家昵称">{{ currentOrder.buyerName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentOrder.buyerPhone || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="商品名称">{{ currentOrder.productName }}</el-descriptions-item>
          <el-descriptions-item label="购买数量">{{ currentOrder.quantity }} 件</el-descriptions-item>
          <el-descriptions-item label="实付金额">
            <span style="color:#f56c6c; font-weight:bold; font-size:16px">¥{{ currentOrder.totalAmount }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <div style="margin-top: 20px;">
          <div style="font-weight:bold; margin-bottom:8px; font-size:14px; color:#333;">📍 收货地址</div>
          <el-input type="textarea" v-model="currentOrder.address" rows="2" size="small"></el-input>
          <div style="font-size:12px; color:#999; margin-top:5px">* 客服可根据用户要求修改地址</div>
        </div>
      </div>
      <span slot="footer">
        <el-button @click="orderDetailVisible = false" size="small">关 闭</el-button>
        <el-button type="primary" @click="saveOrder" size="small">保存修改</el-button>
      </span>
    </el-dialog>

    <el-dialog :visible.sync="chatDetailVisible" width="700px" append-to-body custom-class="modern-detail-modal">
      <span slot="title" class="dialog-header-title">
        <i class="el-icon-goods" style="color:#ff9f43; margin-right:5px"></i> 商品详情
      </span>
      <div class="detail-container" v-if="chatDetailData.name">
        <div class="detail-top-section">
          <div class="dt-left">
            <img :src="getImageUrl(chatDetailData.image || chatDetailData.img)" class="dt-img">
          </div>
          <div class="dt-right">
            <div class="dt-title">{{ chatDetailData.name }}</div>

            <div class="dt-price-row">
              <span class="currency">¥</span><span class="num">{{ chatDetailData.price }}</span>
              <el-tag v-if="chatDetailData.stock < 10" type="danger" size="mini" effect="dark" style="margin-left:10px">
                库存紧张
              </el-tag>
            </div>

            <div class="dt-tags-row" v-if="chatDetailData.tags">
              <el-tag v-for="tag in (chatDetailData.tags || '').split(',')" :key="tag" size="small" type="info"
                      class="custom-tag">{{ tag }}
              </el-tag>
            </div>

            <div class="dt-desc-box">
              <i class="el-icon-chat-dot-round quote-icon"></i>
              <div class="dt-desc-text">{{ chatDetailData.description || '暂无简介' }}</div>
            </div>

            <div class="dt-meta">
              <span class="meta-item"><i class="el-icon-box"></i> 库存: {{ chatDetailData.stock }}</span>
              <span class="meta-item"><i class="el-icon-price-tag"></i> 分类: {{ chatDetailData.category }}</span>
            </div>
          </div>
        </div>

        <div class="section-divider"><span>详情</span></div>

        <div class="detail-scroll-area">
          <div class="detail-rich-content ql-editor"
               v-html="chatDetailData.content || '<div class=\'empty-tip\'>暂无详细图文介绍</div>'"></div>
        </div>
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
      userOrders: [],
      userMemo: '',

      // 未读消息提醒
      unreadMap: {}, // userId -> count
      blinkTimer: null,
      originalTitle: document.title,

      crmSearch: '',
      crmTab: 'all',
      highlightOrderNo: '',

      // 订单详情 (CRM)
      orderDetailVisible: false,
      currentOrder: {},
      currentStep: 0,

      // 聊天查看详情 (商品)
      chatDetailVisible: false,
      chatDetailData: {},

      // 商品推荐
      recommendList: [],
      prodSearch: '',
      prodCategory: '', // 新增：分类
      prodLoading: false,
      socket: null,
      reconnectTimer: null,

      timer: null,
      emojis: ['😀','😁','😂','🤣','😃','😄','😅','😆','😉','😊','😋','😎','😍','😘','🥰','😗','😙','😚','🙂','🤗','🤩','🤔','🤨','😐','😑','😶','🙄','😏','😣','😥','😮','🤐','😯','😪','😫','😴','😌','😛','😜','😝','🤤','😒','😓','😔','😕','🙃','🤑','😲','☹️','🙁','😖','😞','😟','😤','😢','😭','😦','😧','😨','😩','🤯','😬','😰','😱','🥵','🥶','😳','🤪','😵','😡','😠','🤬','😷','🤒','🤕','🤢','🤮','🤧','😇','🥳','🥺','🤠','🤡','🤥','🤫','🤭','🧐','🤓','😈','👿','👹','👺','💀','👻','👽','🤖','💩'],
      quickPhrases: [
        '您好，请问有什么可以帮您？',
        '亲，这边已经在帮您催促仓库发货了，请耐心等待~',
        '我们的快递默认发顺丰，一般3天内送达。',
        '请您提供一下订单号，或者商品照片，方便我为您核实。',
        '感谢您的咨询，祝您生活愉快，再见！'
      ]
    };
  },
  filters: {
    fmtDate(val) { if(!val) return ''; return val.replace('T', ' '); },
    fmtShortDate(val) { if(!val) return ''; return val.substring(5, 10); }
  },
  computed: {
    filteredContacts() {
      if(!this.contactSearch) return this.contactList;
      return this.contactList.filter(c => c.nickname.includes(this.contactSearch));
    },
    filteredOrders() {
      return this.userOrders.filter(o => {
        const matchSearch = !this.crmSearch || o.orderNo.includes(this.crmSearch);
        const matchTab = this.crmTab === 'all' ? true : String(o.status) === String(this.crmTab);
        return matchSearch && matchTab;
      });
    }
  },
  created() {
    const userStr = sessionStorage.getItem("user");
    if (userStr) {
      this.user = JSON.parse(userStr);
      const savedMap = localStorage.getItem(`removed_sessions_${this.user.id}`);
      if (savedMap) {
        try { this.removedSessionMap = JSON.parse(savedMap); } catch(e){}
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
      this.socket.onopen = () => console.log("客服端WS连接");
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
    errorHandler() { return true; },
    getImageUrl(url) {
      if (!url) return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';
      if (url.startsWith('http')) return url;
      if (url.startsWith('/')) return 'http://localhost:9090' + url;
      return 'http://localhost:9090/images/' + url;
    },
    onImgErr(e) {
      e.target.src = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';
    },
    uploadImage(e) {
      const file = e.target.files[0];
      if (!file || !this.currentChatUser) {
        e.target.value = '';
        return;
      }
      const fd = new FormData();
      fd.append('file', file);
      this.$http.post('/file/upload', fd).then(res => {
        if (res.data) {
          this.sendImage(res.data);
        } else {
          this.$message.error('上传失败');
        }
      }).finally(() => {
        e.target.value = '';
      });
    },
    sendImage(imgUrl) {
      const msg = {
        senderId: this.user.id,
        receiverId: this.currentChatUser.id,
        content: imgUrl,
        msgType: 'IMAGE',
        bizType: 'SERVICE'
      };
      this.messages.push(msg);
      this.scrollToBottom();
      this.$http.post('/chat/send', msg);
    },

    appendInput(text) { this.inputMsg += text; },

    // 加载推荐商品列表 (带分类和搜索)
    loadRecommendProducts() {
      this.prodLoading = true;
      const params = {
        pageNum: 1,
        pageSize: 20,
        name: this.prodSearch
      };
      // 如果选了分类，传给后端
      if (this.prodCategory) {
        params.category = this.prodCategory;
      }

      this.$http.get('/product/list', {params}).then(res => {
        this.recommendList = res.data.data || res.data || [];
        this.prodLoading = false;
      }).catch(() => this.prodLoading = false);
    },

    // 发送推荐商品
    sendProductCard(p) {
      const data = {
        id: p.id,
        name: p.name,
        price: p.price,
        image: p.img || p.image,
        description: p.description
      };
      // 发送消息
      const msg = {
        senderId: this.user.id,
        receiverId: this.currentChatUser.id,
        content: JSON.stringify(data), // 转 JSON
        msgType: 'PRODUCT',
        bizType: 'SERVICE'
      };
      this.messages.push(msg);
      this.scrollToBottom();
      this.$http.post('/chat/send', msg);
    },

    // 消息解析兼容
    parseMsgContent(content) {
      if (!content) return {};
      try {
        if (content.startsWith('{')) {
          return JSON.parse(content);
        }
        // 兼容旧数据
        const parts = content.split('|');
        return {
          name: parts[0] || '未知商品',
          price: parts[1] || '0',
          image: parts[2] || '',
          no: parts[0],
          orderNo: parts[0]
        };
      } catch (e) {
        return {name: '数据格式错误', price: 0};
      }
    },

    getMsgPreview(msgContent) {
      if (!msgContent) return '';
      if (msgContent.startsWith('{')) {
        // 尝试解析JSON看类型
        if (msgContent.includes('"msgType":"ORDER"') || msgContent.includes('"orderNo"')) return '[订单咨询]';
        return '[商品卡片]';
      }
      return msgContent;
    },

    async loadRecentContacts() {
      const res = await this.$http.get('/chat/allMessages', { params: { userId: this.user.id } });
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
              lastMsg: m.msgType === 'ORDER' ? '[订单咨询]' : (m.msgType === 'PRODUCT' ? '[商品咨询]' : m.content),
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
      if(this.currentChatUser && this.currentChatUser.id === item.userId) {
        this.currentChatUser = null;
        this.messages = [];
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

      this.currentChatUser = { id: item.userId, nickname: item.nickname, avatar: item.avatar };
      this.$http.get('/sysUser/list', { params: { name: item.nickname } }).then(res => {
        if(res.data && res.data.length>0) {
          const fullInfo = res.data.find(u => u.id === item.userId);
          if(fullInfo) {
            this.currentChatUser = fullInfo;
            this.userMemo = localStorage.getItem('memo_'+fullInfo.id) || '';
          }
        }
      });
      this.loadHistory(item.userId);
      this.loadOrders(item.userId);
    },

    saveMemo() {
      if(this.currentChatUser) {
        localStorage.setItem('memo_'+this.currentChatUser.id, this.userMemo);
        this.$message.success("备注已保存");
      }
    },

    loadHistory(uid, isSilent) {
      this.$http.get('/chat/history', {
        params: { uid1: this.user.id, uid2: uid, type: 'SERVICE' }
      }).then(res => {
        if(JSON.stringify(this.messages) !== JSON.stringify(res.data)) {
          this.messages = res.data;
          if(!isSilent) this.scrollToBottom();
        }
      });
    },

    send() {
      if (!this.inputMsg.trim() || !this.currentChatUser) return;
      const msg = {
        senderId: this.user.id, receiverId: this.currentChatUser.id,
        content: this.inputMsg, msgType: 'TEXT', bizType: 'SERVICE'
      };
      this.messages.push(msg);
      this.inputMsg = '';
      this.scrollToBottom();
      this.$http.post('/chat/send', msg);
    },

    loadOrders(uid) {
      this.$http.get('/orders/list').then(res => {
        let all = res.data.data || res.data;
        this.userOrders = all.filter(o => o.userId === uid);
      });
    },

    // 查看CRM中的订单详情
    openOrderDetail(row) {
      this.currentOrder = JSON.parse(JSON.stringify(row));
      this.calcStep();
      this.orderDetailVisible = true;
    },

    // 查看聊天记录中的商品详情
    viewDetail(msg, type) {
      const data = this.parseMsgContent(msg.content);
      if (type === 'PRODUCT') {
        this.chatDetailData = data;
        // 如果有ID，异步查详情
        if (data.id) {
          this.$http.get('/product/list', {params: {name: data.name}}).then(res => {
            const list = res.data.data || res.data;
            const found = list.find(p => p.id === data.id);
            if (found) this.chatDetailData = found;
          });
        }
        this.chatDetailVisible = true;
      }
    },

    locateOrder(content) {
      const res = this.parseMsgContent(content);
      const no = res.no || res.orderNo;
      if (no && no !== '-') {
        this.highlightOrderNo = no;
        setTimeout(() => {
          const el = document.getElementById('order-' + no);
          if(el) el.scrollIntoView({behavior:'smooth', block:'center'});

          const target = this.userOrders.find(o => o.orderNo == no);
          if (target) this.openOrderDetail(target);
          else this.$message.warning("右侧列表中未找到该订单，可能已过期");
        }, 100);
        setTimeout(() => { this.highlightOrderNo = ''; }, 2000);
      }
    },

    shipOrder(order) {
      this.$http.post('/orders/ship', { id: order.id }).then(res => {
        if(res.data) { this.$message.success("发货成功"); this.loadOrders(this.currentChatUser.id); }
      });
    },
    saveOrder() {
      this.$http.post('/orders/update', this.currentOrder).then(res => {
        if(res.data) {
          this.$message.success("订单已更新");
          this.orderDetailVisible = false;
          this.loadOrders(this.currentChatUser.id);
        }
      });
    },
    calcStep() {
      const s = this.currentOrder.status;
      this.currentStep = (s===0?1 : s===1?2 : s===2?3 : s===3?4 : 0);
    },

    scrollToBottom() { this.$nextTick(() => { const d = this.$refs.chatBody; if(d) d.scrollTop = d.scrollHeight; }); },
    formatTime(t) { return t ? t.substring(5, 16).replace('T', ' ') : ''; },
    getStatusText(s) { return {0:'待支付', 1:'待发货', 2:'已发货', 3:'已完成', '-1':'已取消'}[s]; }
  },
  watch: {
    // 自动保存备注
  }
};
</script>

<style scoped src="@/assets/css/service-home.css"></style>
<style scoped>
</style>
