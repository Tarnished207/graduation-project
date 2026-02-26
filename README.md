# Pet Care Platform (宠物服务平台)

## 项目简介

Pet Care Platform 是一个基于 Java Spring Boot 和 Vue.js 开发的综合性宠物服务与医疗管理系统。致力于为宠物主人提供从档案管理、健康监测到在线咨询、商品购买的一站式服务，同时也为医生和客服提供高效的工作台，为管理员提供全面的运营监控。

## 项目架构

本项目采用前后端分离架构设计，主要包括以下组件：

*   **API 层**: 使用 Spring Boot (Spring MVC) 构建 RESTful API 接口
*   **业务逻辑层**: 处理复杂的业务流程（如订单流转、问诊逻辑）
*   **数据访问层**: 使用 MyBatis-Plus 简化数据库操作
*   **实时通讯**: 基于 WebSocket 实现即时聊天与状态同步
*   **数据可视化**: 使用 ECharts 展示运营统计报表
*   **鉴权系统**: 使用 JWT (JSON Web Token) + RBAC 进行身份与权限控制
*   **文件存储**: 本地文件系统存储用户头像与宠物影像

## 项目目录结构

```text
.
├── pet-client/                         # 前端项目 (Vue 2)
│   ├── public/                         # 静态资源入口
│   ├── src/
│   │   ├── assets/                     # 静态资源 (CSS, Images)
│   │   ├── components/                 # 公共组件 (Chat.vue, UserPill.vue)
│   │   ├── router/                     # 路由配置 (按角色分模块)
│   │   ├── views/                      # 页面视图
│   │   │   ├── Login.vue               # 登录页
│   │   │   ├── UserHome.vue            # 用户端主页
│   │   │   ├── DoctorHome.vue          # 医生工作台
│   │   │   ├── ServiceHome.vue         # 客服工作台
│   │   │   └── Manage.vue              # 后台管理主页
│   │   ├── App.vue                     # 根组件
│   │   └── main.js                     # 前端入口 (Axios配置, 权限拦截)
│   └── package.json                    # 前端依赖配置
├── pet-server/                         # 后端项目 (Spring Boot)
│   ├── src/main/
│   │   ├── java/com/example/petserver/
│   │   │   ├── annotation/             # 自定义注解 (@RequireRole)
│   │   │   ├── common/                 # 通用结果封装 (Result)
│   │   │   ├── config/                 # 配置类 (WebConfig, WebSocketConfig)
│   │   │   ├── controller/             # 控制器 (处理 HTTP 请求)
│   │   │   ├── entity/                 # 实体类 (数据库映射)
│   │   │   ├── mapper/                 # 数据访问接口
│   │   │   ├── service/                # 业务逻辑接口与实现
│   │   │   ├── utils/                  # 工具类 (JwtUtils)
│   │   │   └── websocket/              # WebSocket 服务端 (消息推送)
│   │   └── resources/
│   │       ├── mapper/                 # MyBatis XML 映射文件
│   │       └── application.yml         # 后端配置文件
│   └── pom.xml                         # Maven 依赖配置
└── graduation_project_files/           # 文件上传存储目录
```

## 技术栈

### 主要技术

*   **后端语言**: Java (JDK 1.8)
*   **后端框架**: Spring Boot 2.7.18
*   **前端框架**: Vue.js 2.6
*   **UI 组件库**: Element UI 2.15
*   **数据库**: MySQL 8.0
*   **ORM 框架**: MyBatis-Plus 3.5.3
*   **实时通信**: Javax WebSocket
*   **图表库**: ECharts 6.0, Chart.js

### 核心依赖

*   **org.springframework.boot/spring-boot-starter-web**: Web 核心组件
*   **org.springframework.boot/spring-boot-starter-websocket**: WebSocket 支持
*   **com.baomidou/mybatis-plus-boot-starter**: MyBatis 增强工具
*   **io.jsonwebtoken/jjwt**: JWT 令牌生成与解析
*   **com.alibaba/fastjson**: JSON 处理
*   **org.projectlombok/lombok**: 简化 Java 代码
*   **axios**: 前端 HTTP 客户端
*   **vue-router**: 前端路由管理

## 功能模块

### 1. 用户模块 (User Module)
*   用户注册/登录（JWT 认证）
*   个人信息管理（头像、密码修改）
*   账号状态管理（禁用/强制下线）
*   多角色支持（用户、医生、客服、管理员）

### 2. 宠物档案模块 (Pet Health Module)
*   宠物基础信息管理（品种、年龄、体重）
*   电子病例管理（Pet Case）
*   健康记录追踪（疫苗、体检）
*   喂养计划制定（定时提醒）

### 3. 商城交易模块 (E-commerce Module)
*   宠物用品展示与搜索
*   购物车管理（添加、修改数量）
*   订单流程（下单、支付模拟、发货、完成）
*   商品推荐（基于规则的推荐）

### 4. 即时咨询模块 (Live Chat Module)
*   **WebSocket 实时连接**: 毫秒级消息传输
*   **多端适配**: 用户端、医生端、客服端独立界面
*   **会话管理**: 自动生成会话列表
*   **消息状态同步**: 未读消息红点提示，点击即焚（标记已读）
*   **自动问候**: 建立咨询时自动发送首条消息

### 5. 后台管理模块 (Admin Dashboard)
*   **用户管理**: 查看列表、封禁违规账号
*   **商品管理**: 上架/下架、编辑商品信息
*   **订单管理**: 查看销售状态、发货处理
*   **数据监控**: 
    *   用户注册量统计
    *   订单销售额趋势
    *   宠物分类占比
    *   可视化大屏展示 (ECharts)

## 项目启动

### 环境准备

*   **JDK**: 1.8+
*   **Node.js**: 14+
*   **MySQL**: 8.0+
*   **Maven**: 3.6+

### 环境变量配置

1.  **数据库配置**: 修改 `pet-server/src/main/resources/application.yml`
    ```yaml
    spring:
      datasource:
        url: jdbc:mysql://localhost:3306/pet_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=GMT%2B8
        username: root
        password: your_password
    ```
2.  **文件存储配置**: 确保 `e:/graduation project/graduation_project_files/` 目录存在（或修改 `FileController` 中的路径）。

### 启动步骤

#### 1. 启动后端服务
```bash
cd pet-server
mvn clean install
mvn spring-boot:run
# 服务将启动在 http://localhost:9090
```

#### 2. 启动前端服务
```bash
cd pet-client
npm install
npm run serve
# 服务将启动在 http://localhost:8080
```

## 项目特点

*   **实时交互**: 抛弃传统的轮询机制，采用 WebSocket 实现全双工通信，聊天体验流畅。
*   **数据一致性**: 解决了 JS Long 类型精度丢失问题，确保 ID 传输准确。
*   **安全可靠**: 实现了完善的 JWT 认证体系和 RBAC 权限控制，支持后端强制下线。
*   **功能完备**: 覆盖了宠物“医、食、住、行”全生命周期的管理需求。
*   **直观监控**: 集成 ECharts 可视化图表，让运营数据一目了然。

## 开发规范

*   **代码风格**: 后端遵循阿里巴巴 Java 开发手册，前端遵循 Vue 风格指南。
*   **接口规范**: 统一使用 `Result<T>` 包装响应数据，状态码明确。
*   **异常处理**: 全局异常捕获，友好的错误提示信息。

## 许可证

本项目采用 MIT 许可证。