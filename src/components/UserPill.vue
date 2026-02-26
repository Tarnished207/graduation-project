<template>
  <div class="user-pill">
    <el-avatar :size="40" :src="getImageUrl(user && user.avatar)"></el-avatar>
    <span class="username">{{ (user && (user.nickname || user.username)) || '铲屎官' }}</span>
    <i class="el-icon-arrow-down el-icon--right"></i>
  </div>
</template>

<script>
export default {
  name: 'UserPill',
  props: {
    user: {type: Object, required: true}
  },
  methods: {
    getImageUrl(url) {
      if (!url) return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';
      if (url.startsWith('http')) return url;
      if (url.startsWith('/')) return 'http://localhost:9090' + url;
      return 'http://localhost:9090/images/' + url;
    }
  }
}
</script>

<style>
.user-pill {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 5px 14px 5px 6px;
  border-radius: 24px;
  line-height: 1;
  font-size: 14px;
  font-family: 'Noto Sans SC', 'Varela Round', sans-serif;
  color: #fff;
  background: rgba(0, 0, 0, 0.35);
  border: 1px solid rgba(255, 255, 255, 0.45);
  backdrop-filter: saturate(180%) blur(6px);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.25);
}

.user-pill .el-avatar {
  flex-shrink: 0;
}

.user-pill .username {
  display: inline-block;
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 600;
  color: inherit;
  letter-spacing: .2px;
}

.user-pill .el-icon-arrow-down {
  font-size: 14px;
  color: inherit;
  opacity: .8;
}

.nav-header.scrolled .user-pill {
  background: rgba(255, 255, 255, 0.92);
  color: #333;
  border: 1px solid #e6e6e6;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.user-pill:hover {
  background: rgba(0, 0, 0, 0.45);
}

.nav-header.scrolled .user-pill:hover {
  background: rgba(255, 255, 255, 1);
}
</style>
