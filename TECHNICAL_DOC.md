# TMS 技术文档 (Technical Documentation)

> TMS (Teamwork Management System) — 基于频道模式的团队沟通协作 + 轻量级任务看板 + 团队博文 Wiki + i18n 国际化翻译管理的响应式 Web 开源平台。

本面向开发者与运维人员，深入解读 TMS 的代码结构、架构设计、核心模块实现、安全机制、实时通讯、数据模型、API 设计、部署运维等关键技术细节，便于二次开发、定制化与维护。

---

## 1. 项目概览

| 维度 | 说明 |
|------|------|
| 项目名称 | TMS (Teamwork Management System) |
| GroupId / ArtifactId | `com.lhjz` / `tms` |
| 版本 | `1.0.0-SNAPSHOT` |
| 主类 | [Application.java](file:///workspace/src/main/java/com/lhjz/portal/Application.java) |
| 打包方式 | `war`（可独立运行也可部署到外部 Tomcat） |
| 代码仓库 | [GitHub](https://github.com/xiweicheng/tms) / [Gitee](https://gitee.com/xiweicheng/tms) |
| 许可证 | MIT |

### 1.1 三大核心功能域

1. **团队协作沟通** — 基于 WebSocket 实时通讯，类似 Slack / BearyChat 的频道化沟通。
2. **团队博文 Wiki** — 类似精简版 Confluence / 蚂蚁笔记，支持 Markdown、富文本、电子表格、思维导图、白板等多种创作方式。
3. **国际化翻译管理** — 专业的多语言翻译项目管理工具，支持导入导出与版本历史。

---

## 2. 技术栈

### 2.1 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 1.8 | 主开发语言 |
| Spring Boot | 2.4.13 | 应用框架（parent POM） |
| Spring Security | 跟随 Boot | 认证授权 |
| Spring Security OAuth2 | 2.4.1 | OAuth2 客户端 / 资源服务 |
| Spring Data JPA | 跟随 Boot | 数据访问层 |
| Spring WebSocket (STOMP) | 跟随 Boot | 实时通讯 |
| Thymeleaf | 跟随 Boot | 服务端模板引擎 |
| Spring Boot Actuator | 跟随 Boot | 运行时监控 |
| Lombok | 跟随 Boot | 实体样板代码精简 |
| Fastjson | 1.2.83 | JSON 序列化 |
| Gson | 跟随 Boot | JSON 处理 |
| markedj | 1.0.9 | Markdown 解析（服务端渲染） |
| Apache POI (poi-ooxml) | 3.17 | Excel 导入导出 |
| OpenCSV | 4.1 | CSV 解析 |
| Jsoup | 1.15.3 | HTML 解析与清洗 |
| Mammoth | 1.4.0 | Word (.docx) 文档解析 |
| Commons IO / BeanUtils / Lang | 2.14.0 / 1.9.4 / 2.6 | 通用工具 |
| Janino | 3.1.7 | 运行时表达式编译 |
| joda-time | 2.10.14 | 日期处理 |
| OGNL | 3.1.29 | 表达式求值（修复 NoClassDefFoundError） |

### 2.2 前端

HTML5 / CSS3 / JavaScript、Bootstrap、Vue.js、Markdown 编辑器、思维导图 / 白板 / 画图 / 在线表格（LuckySheet 等）、FontAwesome 图标。前端工程独立维护后压缩打包进主仓库 `src/main/resources/static`。

### 2.3 数据库与部署

- **数据库**：MySQL（默认）、PostgreSQL（可选，通过 `prod-pg` profile 切换）
- **连接池**：HikariCP
- **部署**：传统 War 包部署到 Tomcat 8.5（`Dockerfile` 基于 `tomcat:8.5.73-jdk8`）、Docker Compose、Kubernetes

---

## 3. 代码结构

源码根包：`com.lhjz.portal`，详见 [src/main/java/com/lhjz/portal](file:///workspace/src/main/java/com/lhjz/portal)。

```
com.lhjz.portal
├── Application.java            # Spring Boot 启动类
├── ServletInitializer.java     # WAR 部署到外部容器时的初始化器
├── base/                       # 基础设施层
│   ├── BaseController.java     #   控制器基类（日志、用户、异常处理）
│   ├── BaseDao.java            #   DAO 基类
│   └── BaseService.java       #   服务基类
├── component/                  # 组件层（Spring 组件 / 拦截器 / 处理器）
│   ├── core/                   #   核心组件抽象（IChatMsg / MailQueue 等）
│   ├── Ajax*Handler.java       #   Ajax 感知的登录/登出/成功/失败处理器
│   ├── LoginSuccessHandler.java
│   ├── SpringSecurityAuditorAware.java  # JPA 审计人填充
│   ├── TmsUserApprovalHandler.java      # OAuth2 用户授权审批
│   ├── WsChannelInterceptor.java       # WebSocket 入站通道拦截器
│   ├── WsHandshakeInterceptor.java     # WebSocket 握手拦截器
│   ├── AsyncTask.java          #   异步任务执行器
│   ├── CustomApplicationRunner.java    # 启动后初始化任务
│   ├── MailSender / CustomMailSender.java  # 邮件发送
│   └── ...
├── config/                     # 配置层
│   ├── SecurityConfig.java     #   Spring Security 安全配置
│   ├── WsConfig.java           #   WebSocket (STOMP) 配置
│   ├── BeanConfig.java         #   通用 Bean 配置
│   ├── DataInitConfig.java     #   数据初始化
│   └── OAuth2ClientConfig.java #   OAuth2 客户端配置
├── constant/
│   └── SysConstant.java        #   系统常量
├── controller/                 # 表现层（REST 控制器）
├── entity/                     # JPA 实体（领域模型）
│   └── security/               #   安全相关实体（User / Authority / Group / OAuth2）
├── exception/
│   └── BizException.java       #   业务异常
├── model/                      # 传输模型 / DTO / Payload
├── pojo/                       # 表单 / 枚举 / 排序对象
├── repository/                 # Spring Data JPA 仓储
├── service/                   # 业务服务层
│   └── impl/                   #   服务实现（含分布式锁实现等）
└── util/                       # 工具类（30+ 工具类）
```

### 资源目录

```
src/main/resources/
├── application*.properties     # 多环境配置
├── logback.xml                 # 日志配置
├── messages*.properties        # i18n 消息资源
├── static/                     # 前端静态资源（前端工程打包产物）
│   ├── image / img / fonts /
│   └── page/                   #   前端源码与构建产物
└── md2pdf/                     # Markdown 转 PDF 的 Node.js 子工程
    ├── index.js
    └── package.json
```

---

## 4. 系统架构

TMS 是典型的 Spring Boot 分层 Web 应用，结合 STOMP over WebSocket 实现实时能力。

### 4.1 分层架构

```
┌─────────────────────────────────────────────────────┐
│  浏览器 / 移动端（Bootstrap + Vue.js 响应式 SPA）    │
└───────────────┬──────────────────────┬──────────────┘
        HTTP / HTTPS            WebSocket (STOMP/SockJS)
                │                      │
┌───────────────▼──────────────────────▼──────────────┐
│            表现层 Controller（@RestController）        │
│   AdminController / ChatController / BlogController   │
│   TranslateController / TodoController / ...           │
└───────────────┬──────────────────────────────────────┘
                │
┌───────────────▼──────────────────────────────────────┐
│           业务层 Service（含分布式锁 BlogLock）        │
│   ChatChannelService / ChannelService / FileService   │
└───────────────┬──────────────────────────────────────┘
                │
┌───────────────▼──────────────────────────────────────┐
│     数据访问层 Spring Data JPA Repository（30+ 个）    │
└───────────────┬──────────────────────────────────────┘
                │
┌───────────────▼──────────────────────────────────────┐
│  MySQL / PostgreSQL  (HikariCP 连接池)                  │
└──────────────────────────────────────────────────────┘
```

### 4.2 启动流程

[Application.java](file:///workspace/src/main/java/com/lhjz/portal/Application.java) 使用 `@SpringBootApplication` 进行组件扫描与自动装配，并注册 `OpenEntityManagerInViewFilter` 以在视图渲染阶段保持 JPA `EntityManager` 开放（解决懒加载异常）。生产以 WAR 部署时由 `ServletInitializer` 完成 Spring Boot servlet 初始化。

启动后由 [CustomApplicationRunner](file:///workspace/src/main/java/com/lhjz/portal/component/CustomApplicationRunner.java) 执行数据初始化等任务（与 [DataInitConfig](file:///workspace/src/main/java/com/lhjz/portal/config/DataInitConfig.java) 配合）。

---

## 5. 安全机制

### 5.1 Spring Security 配置

[SecurityConfig.java](file:///workspace/src/main/java/com/lhjz/portal/config/SecurityConfig.java) 是安全核心，采用两套 `WebSecurityConfigurerAdapter` 链（通过 `@Order` 区分优先级，仅在 `dev / prod / prod-pg` profile 下激活）：

- **SecurityConfiguration（ORDER=-10，优先）**：保护 `/admin/**`、`/api/**`、`/ws/**`，要求认证；其余静态资源（`/admin/css/**`、`/admin/img/**`、`/landing/**`、`/page/**` 等）放行。
  - 登录页 `/admin/login`，登录处理 URL `/admin/signin`，登出 `/admin/logout`。
  - 成功 / 失败处理器均为 Ajax 感知（`AjaxSimpleUrlAuthenticationFailureHandler`、`LoginSuccessHandler`），便于前后端分离式交互。
  - 启用 **Remember-Me**，基于 `JdbcTokenRepositoryImpl` 持久化令牌，有效期 14 天（1209600 秒）。
  - 关闭 CSRF（前后端通过 token / 同源策略保护）。
- **SecurityConfiguration2（ORDER=-9）**：对 `/` 与 `/free/**` 放行，供公开访问。

密码编码器 `PasswordEncoder` 在 [BeanConfig](file:///workspace/src/main/java/com/lhjz/portal/config/BeanConfig.java) 中定义，认证用户来源为 JDBC（`auth.jdbcAuthentication().dataSource(...)`），用户表为 `User`、`Authority`、`PersistentLogin`。

### 5.2 JPA 审计

[SpringSecurityAuditorAware](file:///workspace/src/main/java/com/lhjz/portal/component/SpringSecurityAuditorAware.java) 实现 AuditorAware，从 SecurityContext 取当前用户名注入到实体的 `@CreatedBy` / `@LastModifiedBy`；配合 `@CreatedDate` / `@LastModifiedDate` 与 `@EntityListeners(AuditingEntityListener.class)` 实现自动审计字段填充（见 [Blog.java](file:///workspace/src/main/java/com/lhjz/portal/entity/Blog.java) 等实体）。

### 5.3 OAuth2

[OAuth2ClientConfig](file:///workspace/src/main/java/com/lhjz/portal/config/OAuth2ClientConfig.java) + `TmsUserApprovalHandler` 提供 OAuth2 客户端与资源服务能力，相关实体位于 `entity/security/oauth2/`（`OauthClientDetails`、`OauthAccessToken`、`OauthRefreshToken`、`OauthCode` 等）。

### 5.4 权限模型

- **用户 (User) ↔ 权限 (Authority)**：直接授权。
- **用户组 (Group)**：通过 `GroupMember` 关联用户，通过 `GroupAuthority` 关联权限，实现基于组的批量授权。
- **博文级权限 (BlogAuthority)** 与 **空间级权限 (SpaceAuthority)**：细粒度资源权限，控制博文的可读 / 可编辑、空间的可见性。
- 控制器方法级安全通过 `@EnableGlobalMethodSecurity(securedEnabled = true)` 启用，可使用 `@Secured` 注解。

---

## 6. 实时通讯 (WebSocket / STOMP)

### 6.1 配置

[WsConfig.java](file:///workspace/src/main/java/com/lhjz/portal/config/WsConfig.java)：

- 启用简单 Broker，目的地前缀：`/channel`（频道消息）、`/direct`（私聊）、`/blog`（博文动态）。
- 应用目的地前缀：`/chat`（客户端发送消息前缀）。
- STOMP 端点：`/ws`、`/ws-lock`，`setAllowedOriginPatterns("*")` 跨域，`withSockJS()` 提供 SockJS 回退。
- 入站通道注册 `WsChannelInterceptor`，在消息进入业务前做认证 / 鉴权 / 上下文处理。

### 6.2 握手与拦截

- [WsHandshakeInterceptor](file:///workspace/src/main/java/com/lhjz/portal/component/WsHandshakeInterceptor.java)：握手阶段将 HTTP Session / 安全上下文信息带入 WebSocket Session attributes，供后续消息处理使用。
- [WsChannelInterceptor](file:///workspace/src/main/java/com/lhjz/portal/component/WsChannelInterceptor.java)：在 STOMP 消息入站时校验用户身份、做权限检查。

### 6.3 消息抽象

`component/core/` 定义消息处理抽象：
- `IChatMsg` / `ChatMsgImpl` — 频道消息处理实现，将 STOMP 消息持久化并广播。
- `MailQueue` / `MailQueueImpl` / `MailItem` — 异步邮件队列，将通知邮件排队发送。
- `ChatMsgItem` — 单条聊天消息的内部表示。

广播通过 `SimpMessagingTemplate.convertAndSend("/channel/xxx", payload)` 推送，前端订阅对应目的地即可实时接收。

---

## 7. 数据模型

实体位于 [entity](file:///workspace/src/main/java/com/lhjz/portal/entity) 包，使用 JPA 注解 + Lombok，统一审计字段。共 30+ 实体，按业务域分组：

### 7.1 安全域

| 实体 | 说明 |
|------|------|
| [User](file:///workspace/src/main/java/com/lhjz/portal/entity/security/User.java) | 用户，含用户名、密码（加密）、邮箱、头像、在线状态等 |
| Authority / AuthorityId | 用户直接权限（复合主键） |
| Group | 用户组 |
| GroupAuthority / GroupMember | 组权限 / 组成员 |
| PersistentLogin | Remember-Me 持久化令牌 |
| oauth2/* | OAuth2 客户端、令牌、授权码、审批 |

### 7.2 沟通域

| 实体 | 说明 |
|------|------|
| Channel | 频道 |
| ChannelGroup | 频道分组 |
| Chat | 聊天消息基类（内容、类型、时间） |
| ChatChannel | 频道消息（关联频道） |
| ChatDirect | 一对一私聊 |
| ChatAt | @ 提及 |
| ChatReply | 消息回复（话题二级） |
| ChatLabel / Label | 消息标签 |
| ChatPin | 固定消息 |
| ChatStow | 收藏消息 |
| ChatChannelFollower | 频道关注 |

### 7.3 博文 Wiki 域

| 实体 | 说明 |
|------|------|
| [Blog](file:///workspace/src/main/java/com/lhjz/portal/entity/Blog.java) | 博文，唯一约束 `uuid`，索引 `title / uuid / shareId`，支持 `BlogType`（Markdown / 富文本 / 表格 / 思维导图 / 画图 / 白板）、`Editor`、`Status`、父子嵌套、版本号 `@Version` 乐观锁 |
| BlogAuthority | 博文权限 |
| BlogFollower | 博文关注 |
| BlogHistory | 历史版本（支持比较与回退） |
| BlogNews | 博文动态流 |
| BlogStow | 博文收藏 |
| Space / SpaceAuthority | 博文空间与空间权限 |
| Dir | 博文目录（拖拽排序、五级嵌套） |
| Tag | 博文标签 |
| Comment | 评论（含评论投票） |
| File | 上传文件实体 |

### 7.4 任务与日程域

| 实体 | 说明 |
|------|------|
| Todo | 待办事项 |
| Schedule | 日程安排与提醒 |
| Gantt | 甘特图（项目规划） |
| Project | 翻译项目 / 业务项目 |
| Link | 频道外链 |

### 7.5 i18n 翻译域

| 实体 | 说明 |
|------|------|
| Translate | 翻译项目 |
| TranslateItem | 翻译条目 |
| TranslateItemHistory | 翻译历史 |
| Language | 语言定义 |

### 7.6 系统域

| 实体 | 说明 |
|------|------|
| Log | 操作审计日志（Action + Target + targetId） |
| Setting | 系统设置 |
| Feedback | 用户反馈 |

### 7.7 审计与日志

[BaseController](file:///workspace/src/main/java/com/lhjz/portal/base/BaseController.java) 提供 `log(Action, Target, targetId, ...)` 方法，将关键操作统一写入 `Log` 实体，形成完整操作变更历史。`Action` / `Target` 为枚举（见 `pojo/Enum.java`），覆盖创建、更新、删除、登录、分享等动作与博文、频道、用户、翻译等目标对象。

---

## 8. 控制器与 API 设计

控制器位于 [controller](file:///workspace/src/main/java/com/lhjz/portal/controller) 包，统一继承 `BaseController`，返回 `RespBody`（统一响应体）。主要控制器与路由前缀：

| 控制器 | 路由前缀 | 职责 |
|--------|----------|------|
| [AdminController](file:///workspace/src/main/java/com/lhjz/portal/controller/AdminController.java) | `admin/` | 后台管理、登录页、健康检查、用户/项目/语言/反馈/动态/翻译/导入/设置管理 |
| [ApiController](file:///workspace/src/main/java/com/lhjz/portal/controller/ApiController.java) | `api/` | 第三方集成入口（Jenkins 消息、外部应用消息投递到频道） |
| [BlogController](file:///workspace/src/main/java/com/lhjz/portal/controller/BlogController.java) | `admin/blog` | 博文 CRUD、评论、权限、收藏、关注、历史、导出、投票、分享 |
| [ChatController](file:///workspace/src/main/java/com/lhjz/portal/controller/ChatController.java) | `admin/chat` | 聊天消息收发与处理 |
| [ChatChannelController](file:///workspace/src/main/java/com/lhjz/portal/controller/ChatChannelController.java) | `admin/chat/channel` | 频道管理 |
| [ChatDirectController](file:///workspace/src/main/java/com/lhjz/portal/controller/ChatDirectController.java) | — | 私聊 |
| [ChatLabelController](file:///workspace/src/main/java/com/lhjz/portal/controller/ChatLabelController.java) | — | 消息标签 |
| [ChannelController](file:///workspace/src/main/java/com/lhjz/portal/controller/ChannelController.java) | — | 频道配置 |
| [ChannelGroupController](file:///workspace/src/main/java/com/lhjz/portal/controller/ChannelGroupController.java) | — | 频道分组 |
| [ChannelTaskController](file:///workspace/src/main/java/com/lhjz/portal/controller/ChannelTaskController.java) | — | 频道任务看板 |
| [TranslateController](file:///workspace/src/main/java/com/lhjz/portal/controller/TranslateController.java) | `admin/translate` | 翻译项目与条目管理、导入导出、Base64 |
| [LanguageController](file:///workspace/src/main/java/com/lhjz/portal/controller/LanguageController.java) | — | 语言管理 |
| [TodoController](file:///workspace/src/main/java/com/lhjz/portal/controller/TodoController.java) | `admin/todo` | 待办事项 |
| [ScheduleController](file:///workspace/src/main/java/com/lhjz/portal/controller/ScheduleController.java) | — | 日程 |
| [GanttController](file:///workspace/src/main/java/com/lhjz/portal/controller/GanttController.java) | — | 甘特图 |
| [SpaceController](file:///workspace/src/main/java/com/lhjz/portal/controller/SpaceController.java) | — | 博文空间 |
| [SpaceHomeController](file:///workspace/src/main/java/com/lhjz/portal/controller/SpaceHomeController.java) | `free/space/home` | 空间公开页 |
| [FileController](file:///workspace/src/main/java/com/lhjz/portal/controller/FileController.java) | — | 文件上传下载 |
| [ImportController](file:///workspace/src/main/java/com/lhjz/portal/controller/ImportController.java) | `admin/import` | CSV / Excel 导入为 Markdown 表格、导出 |
| [HomeController](file:///workspace/src/main/java/com/lhjz/portal/controller/HomeController.java) | `free/home` | 公开博文列表 |
| [RootController](file:///workspace/src/main/java/com/lhjz/portal/controller/RootController.java) | `/` | 根路由、登录注册、公开 wiki |
| [SettingController](file:///workspace/src/main/java/com/lhjz/portal/controller/SettingController.java) | — | 系统设置 |
| [UserController](file:///workspace/src/main/java/com/lhjz/portal/controller/UserController.java) | — | 用户管理 |
| [UserTaskController](file:///workspace/src/main/java/com/lhjz/portal/controller/UserTaskController.java) | `admin/user/task` | 用户任务 |
| [Oauth2ResourceController](file:///workspace/src/main/java/com/lhjz/portal/controller/Oauth2ResourceController.java) | — | OAuth2 资源接口 |
| [FeedbackController](file:///workspace/src/main/java/com/lhjz/portal/controller/FeedbackController.java) | — | 用户反馈 |

### 8.1 路由可见性约定

- `admin/**` — 需登录（受 Security 保护）。
- `api/**` — 受保护，对外集成接口（Jenkins 等）。
- `ws/**` — WebSocket 端点，需认证。
- `free/**` — 公开访问（着陆页、公开博文 / 空间）。

### 8.2 统一响应

所有接口统一返回 `RespBody`（见 [model](file:///workspace/src/main/java/com/lhjz/portal/model) 包），`BaseController` 内通过 `@ExceptionHandler` 统一异常处理，把 `BizException` 等转换为标准 JSON 响应，前端据此做 Toastr 提示。

---

## 9. 业务服务层

| 服务 | 接口 | 实现 | 职责 |
|------|------|------|------|
| ChatChannelService | [接口](file:///workspace/src/main/java/com/lhjz/portal/service/ChatChannelService.java) | [impl](file:///workspace/src/main/java/com/lhjz/portal/service/impl/ChatChannelServiceImpl.java) | 频道消息持久化与广播调度 |
| ChannelService | [接口](file:///workspace/src/main/java/com/lhjz/portal/service/ChannelService.java) | [impl](file:///workspace/src/main/java/com/lhjz/portal/service/impl/ChannelServiceImpl.java) | 频道生命周期管理 |
| FileService | [接口](file:///workspace/src/main/java/com/lhjz/portal/service/FileService.java) | [impl](file:///workspace/src/main/java/com/lhjz/portal/service/impl/FileServiceImpl.java) | 文件上传 / 下载 / 图片处理 |
| BlogLockService | [接口](file:///workspace/src/main/java/com/lhjz/portal/service/BlogLockService.java) | [impl](file:///workspace/src/main/java/com/lhjz/portal/service/impl/BlogLockServiceImpl.java) | 多人协作编辑时的分布式锁（基于 WebSocket 锁端点 `/ws-lock`） |

### 9.1 多人协同编辑锁

[BlogLockServiceImpl](file:///workspace/src/main/java/com/lhjz/portal/service/impl/BlogLockServiceImpl.java) 通过 `WsLockPayload`（model 包）在 `/ws-lock` 端点上协调多个客户端对同一篇博文的编辑权，避免并发覆盖；锁通过 WebSocket 心跳维持，断开自动释放。

---

## 10. 工具类

[util](file:///workspace/src/main/java/com/lhjz/portal/util) 包提供 30+ 工具类：

| 类 | 用途 |
|----|------|
| AuthUtil | 认证 / 当前用户获取 |
| DateUtil | 日期与时间（joda-time 封装） |
| FileUtil | 文件 IO（基于 commons-io） |
| ImageUtil | 图片缩放 / 头像处理 |
| ExcelUtil | POI Excel 读写（导入导出） |
| HtmlUtil | Jsoup HTML 清洗 / 转换 |
| JsonUtil | Fastjson / Gson 封装 |
| MapUtil / CollectionUtil | 集合操作 |
| SqlUtil | SQL 拼接与防注入 |
| SHA1 / EncoderUtil | 编码与摘要 |
| StringUtil | 字符串处理 |
| TemplateUtil | FreeMarker 模板渲染（邮件等） |
| WebUtil | Web 请求工具（IP、UA、Referer） |
| LuckySheetUtil | LuckySheet 在线表格数据解析 |
| ChineseUtil | 中文处理 |
| ValidateUtil | 校验工具 |
| ThreadUtil | 线程 / 异步工具 |

---

## 11. 配置体系

### 11.1 多 Profile 配置文件

| 文件 | 说明 |
|------|------|
| [application.properties](file:///workspace/src/main/resources/application.properties) | 主配置，默认 `spring.profiles.active=dev`，并 `include=tms` |
| application-dev.properties | 开发环境 |
| application-prod.properties | 生产（MySQL） |
| application-prod-pg.properties | 生产（PostgreSQL） |
| application-tms.properties | TMS 业务参数（共用） |

### 11.2 关键配置

- 数据源：MySQL `jdbc:mysql://localhost:3306/tms`，HikariCP（最大池 30、最小空闲 10、连接超时 30s）。
- JPA：`ddl-auto=update`（自动维护表结构）、`show-sql=false`。
- 服务：`server.port=80`，`context-path=/`。
- 模板：Thymeleaf `cache=false`（开发）、`mode=HTML`。
- 监控：Actuator `base-path=/manage`，默认关闭所有端点（`enabled-by-default=false`），按需开放。
- 邮件：`spring.mail.*`（默认 163 邮箱）。
- 上传：单文件 10MB，单请求 50MB。
- OAuth2：客户端 `client-id=tms` / `client-secret=tms`，`auto-approve-scopes=.*`。
- Jackson：`default-property-inclusion=non-null`。

### 11.3 国际化资源

- `messages.properties` / `messages_en_US.properties` — 系统级 i18n。
- `spring.messages.basename=messages,views`，编码 `ISO-8859-1`（Spring 默认）。

---

## 12. 部署与运维

### 12.1 构建产物

`mvn clean package` 产出 `target/tms-1.0.0-SNAPSHOT.war`。`spring-boot-maven-plugin` 支持 `repackage`，war 可独立 `java -jar` 运行，也可部署到外部 Tomcat（`spring-boot-starter-tomcat` 为 `provided`，由容器提供）。

### 12.2 传统部署

```bash
mvn clean package
# 将 war 部署到 Tomcat 8.5 webapps/ROOT.war
```

[Dockerfile](file:///workspace/Dockerfile) 即基于 `tomcat:8.5.73-jdk8`，将 war 复制到 `webapps/ROOT.war`。

### 12.3 Docker Compose 部署

[docker-compose.yml](file:///workspace/docker-compose.yml) 启动两个服务：`web`（TMS 应用，8090→8080）与 `db`（MySQL，3307→3306），通过 `backend` 网络互通。PostgreSQL 版本使用 [docker-compose-pg.yml](file:///workspace/docker-compose-pg.yml)。

```bash
docker-compose up -d
```

### 12.4 Kubernetes 部署

[k8s/deploy.yaml](file:///workspace/k8s/deploy.yaml) 定义 `Deployment`（单副本，含 `tms-mysql` 与 `tms-tomcat` 两个容器），[k8s/svc.yaml](file:///workspace/k8s/svc.yaml) 定义 Service。镜像来自 `registry.cn-hangzhou.aliyuncs.com/xiweicheng/tms-*:v1`。

### 12.5 部署脚本

| 脚本 | 说明 |
|------|------|
| [deploy.sh](file:///workspace/deploy.sh) | MySQL 部署脚本 |
| [deploy-pg.sh](file:///workspace/deploy-pg.sh) | PostgreSQL 部署脚本 |
| [deploy2local.sh](file:///workspace/deploy2local.sh) | 本地部署脚本 |
| [src/main/resources/deploy.sh](file:///workspace/src/main/resources/deploy.sh) | 容器内部署辅助 |

### 12.6 数据库初始化

- MySQL：[db/mysql/db.sql](file:///workspace/db/mysql/db.sql) + [db/mysql/Dockerfile](file:///workspace/db/mysql/Dockerfile)（构建带初始化数据的镜像）。
- PostgreSQL：[db/postgres/db.sql](file:///workspace/db/postgres/db.sql) + [db/postgres/Dockerfile](file:///workspace/db/postgres/Dockerfile)。
- 运行时由 `ddl-auto=update` 维护表结构。

### 12.7 访问地址

- 默认：`http://localhost/tms`（context-path 已改为 `/`，直接 `http://localhost/`）。
- Docker Compose：`http://localhost:8090/`。

---

## 13. 监控与日志

- **日志**：[logback.xml](file:///workspace/src/main/resources/logback.xml) 配置，输出到 `log/tms/tms.log`；代码中统一使用 SLF4J。
- **运行时监控**：Spring Boot Actuator，端点基路径 `/manage`，默认关闭，需显式开启（可在生产 profile 中按需 `management.endpoints.web.exposure.include=...`）。
- **操作审计**：`Log` 实体记录所有关键操作（创建 / 更新 / 删除 / 登录 / 分享等），可追溯。

---

## 14. Markdown 转 PDF 子工程

[src/main/resources/md2pdf](file:///workspace/src/main/resources/md2pdf) 是一个独立 Node.js 工程（`package.json` + `index.js`），用于将博文 Markdown 渲染为 PDF 导出，与后端 Java 进程协作完成导出能力（博文支持导出 PDF / Markdown / HTML / Excel / PNG）。

---

## 15. 开发指南

### 15.1 本地开发

1. 克隆仓库，准备 MySQL（或 PostgreSQL）。
2. 修改 `application-dev.properties` 数据库连接。
3. 运行 [Application.java](file:///workspace/src/main/java/com/lhjz/portal/Application.java) 主类（IDE 中直接 Run）。
4. 访问 `http://localhost/`。

### 15.2 新增业务模块

1. 在 `entity/` 新增 JPA 实体（继承审计字段）。
2. 在 `repository/` 新增 `JpaRepository` 接口。
3. 在 `service/`（+ `impl/`）新增业务服务。
4. 在 `controller/` 新增 `@RestController`，继承 `BaseController`，返回 `RespBody`。
5. 通过 `log(Action, Target, targetId)` 记录操作审计。
6. 通过 `SimpMessagingTemplate` 推送实时消息到对应 Broker 目的地。

### 15.3 编码规范要点

- 实体统一使用 Lombok `@Data` / `@ToString`，避免手写 getter/setter。
- 控制器返回统一 `RespBody`，异常走 `BaseController` 统一处理。
- 实时通讯走 STOMP Broker 目的地，不直接操作 Session。
- 文件上传遵循 `spring.servlet.multipart.*` 限制，通过 `FileService` 统一处理。

---

## 16. 测试

- 依赖：`spring-boot-starter-test`（排除 JUnit 4）、`testng` 6.8.13、`spring-security-test`、`junit`（test scope）。
- 测试目录：`src/test/`，可使用 TestNG 或 JUnit 编写单元 / 集成测试。
- `json-path` 用于对 JSON 响应做断言。

---

## 17. 已知注意事项

1. **第三方依赖版权**：项目使用多个第三方开源依赖，商用前需确认依赖 License（见 [README.md](file:///workspace/README.md) 免责声明）。
2. **Spring Boot 版本**：parent 为 2.4.13（CODE_WIKI 中提及的 1.5.14 为历史版本，以 pom.xml 实际为准）。
3. **CSRF 关闭**：当前安全配置关闭了 CSRF，生产部署建议在网关层或同源策略下保护，或按需开启。
4. **ddl-auto=update**：便于开发，生产建议改为 `validate` 并使用迁移工具（如 Flyway / Liquibase）管理 schema。
5. **OGNL 依赖**：显式引入 `ognl:3.1.29` 修复 `NoClassDefFoundError`。
6. **Actuator 端点默认关闭**：上线前按需开启健康检查 / 信息端点。

---

## 18. 总结

TMS 是一款功能完整、架构清晰的 Spring Boot 团队协作平台，覆盖沟通、博文 Wiki、i18n 翻译三大场景，具备实时通讯、细粒度权限、多人协同编辑、多格式导出、多数据库与多云部署能力。本技术文档可作为二次开发与运维的参考基线，建议配合 [CODE_WIKI.md](file:///workspace/CODE_WIKI.md) 与 [FEATURES.md](file:///workspace/FEATURES.md) 一起阅读。
