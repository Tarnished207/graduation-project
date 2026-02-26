<template>
  <div id="app">
    <router-view/>

    <Chat />
  </div>
</template>

<script>
// 引入刚才做好的聊天组件
import Chat from "@/components/Chat";

export default {
  name: "App",
  components: {
    Chat // 注册组件
  }
}
</script>

<style>
/* 全局样式：去除浏览器默认边距 */
html, body, #app {
  height: 100%;
  margin: 0;
  padding: 0;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "微软雅黑", Arial, sans-serif;
}
</style>