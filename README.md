# 暖心记账 LedgerOrigin

一个原生 Android 记账 APP，采用暖色调（橙黄米色）设计。后端使用 Go + Gin + dbr + MySQL。

## 技术栈

| 端 | 技术 |
|----|------|
| 前端 | 原生 Android (Java)、Retrofit、MPAndroidChart、Material Components |
| 后端 | Go + Gin + dbr (gocraft) + JWT + bcrypt |
| 数据库 | MySQL 8.0+ |

## 功能模块（满足 5 个独立功能模块 + 登录注册）

1. **登录/注册**：JWT 鉴权，SharedPreferences 保存登录状态
2. **记账（交易流水）**：收入/支出记录的增删改查
3. **分类管理**：自定义收支分类的增删改查（Spinner 选图标）
4. **账户管理**：多资金账户增删改查（ListView 展示）
5. **统计分析**：按月统计、分类饼图（MPAndroidChart）
6. **预算管理**：分类预算设置、进度条展示、超支 Notification 提醒

## 涉及的 Android 技术点

- Activity 组件、多种布局管理、基础 UI 控件（TextView/EditText/ProgressBar 等）
- ListView、Spinner、RecyclerView
- Intent 页面跳转与传参（Serializable）
- 事件处理、Handler 机制（启动页延迟跳转）
- Fragment、Fragment 实现底部 TAB、Fragment 与 Activity 通信
- SharedPreferences 存储（登录态）
- MySQL 数据库（通过后端 API）
- 广播机制（LocalBroadcastManager + 自定义广播）
- Service / IntentService（SyncService 后台同步）
- Notification（预算提醒）
- Menu（底部导航菜单）
- XML / JSON 解析（Gson）
- Android 网络通信（Retrofit + OkHttp）
- 属性动画（饼图动画）

## 目录结构

```
LedgerOrigin/
├── backend/                Go 后端
│   ├── main.go             入口
│   ├── config/             数据库连接配置
│   ├── models/             数据模型
│   ├── handlers/           业务处理器
│   ├── middleware/         JWT 中间件
│   ├── routes/             路由
│   └── schema.sql          MySQL 建表脚本
└── android/                Android 客户端
    └── app/src/main/
        ├── java/com/ledger/origin/
        │   ├── ui/         各功能模块 UI
        │   ├── net/        Retrofit 接口与客户端
        │   ├── model/      数据模型
        │   ├── service/    后台同步服务
        │   └── util/       工具类
        └── res/            暖色调资源
```

## 运行步骤

### 1. 启动数据库

```bash
mysql -u root -p < backend/schema.sql
```

按需通过环境变量配置数据库连接（见下一步），默认连接 `root@127.0.0.1:3306/ledger_origin`。

### 2. 启动后端

```bash
cd backend
go mod tidy

# 通过环境变量配置数据库与 JWT 密钥（参考 backend/.env.example）
# Windows PowerShell 示例：
#   $env:DB_PASSWORD="你的密码"; $env:JWT_SECRET="一段足够长的随机串"
# Linux/macOS 示例：
#   export DB_PASSWORD=你的密码 JWT_SECRET=一段足够长的随机串

go run main.go
```

服务监听 `:8080`。未设置环境变量时使用开发用默认值，生产环境务必覆盖。

### 3. 运行 Android

- 用 Android Studio 打开 `android/` 目录
- `app/src/main/java/com/ledger/origin/net/ApiClient.java` 中 `BASE_URL`：
  - 模拟器：`http://10.0.2.2:8080/`（默认）
  - 真机：改成电脑局域网 IP，如 `http://192.168.x.x:8080/`
- 运行到模拟器或真机

构建好的调试包：`android/app/build/outputs/apk/debug/app-debug.apk`

## 接口一览

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/register | 注册（自动创建默认分类与账户） |
| POST | /api/login | 登录 |
| GET/POST/PUT/DELETE | /api/transactions | 交易记录 CRUD |
| GET/POST/PUT/DELETE | /api/categories | 分类 CRUD |
| GET/POST/PUT/DELETE | /api/accounts | 账户 CRUD |
| GET/POST/PUT/DELETE | /api/budgets | 预算 CRUD |
| GET | /api/stats/overview | 月度收支概览 |
| GET | /api/stats/category | 分类统计（饼图数据） |
| GET | /api/stats/daily | 每日收支统计 |

除注册/登录外均需在 Header 携带 `Authorization: Bearer <token>`。
