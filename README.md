# 高考志愿填报系统

高考志愿填报辅助平台，支持**考生端**、**院校端**、**运营管理端**三端，提供志愿方案规划、AI 智能推荐、院校搜索、招生计划管理、录取分数线查询等功能。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 21, Spring Boot 4.0, MyBatis, Spring Security, JWT |
| 前端 | Vue 3, Vite, Element Plus, Tailwind CSS |
| 数据库 | MySQL 8.0 |
| 缓存/Session | Redis |
| AI | Spring AI + DeepSeek |
| 文档 | Knife4j (Swagger) |

## 快速启动

### 环境要求

- **JDK 21+**
- **Node.js** 18+
- **MySQL 8.0**（已启动）
- **Redis**（已启动，可选）

### 一键启动

```bash
# Windows — 双击运行
start.bat

# Git Bash / Linux / macOS
bash start.sh
```

脚本会自动完成：环境检查 → 数据库初始化 → 安装依赖 → 启动前后端 → 打开浏览器。

### 手动启动

**1. 初始化数据库**

```sql
CREATE DATABASE IF NOT EXISTS gaokao_volunteer DEFAULT CHARACTER SET utf8mb4;
```

然后导入 `gaokao_volunteer.sql`。

**2. 配置环境变量**

复制 `application.properties.example` 为 `application.properties`，设置以下环境变量：

| 变量 | 说明 |
|------|------|
| `DB_USERNAME` | 数据库用户名 |
| `DB_PASSWORD` | 数据库密码 |
| `JWT_SECRET` | JWT 签名密钥 |
| `DEEPSEEK_API_KEY` | DeepSeek API Key（AI 功能需要） |

**3. 启动后端**

```bash
cd backend
./mvnw spring-boot:run
# 后端运行在 http://localhost:8080
# API 文档: http://localhost:8080/doc.html
```

**4. 启动前端**

```bash
cd frontend/vuezhiyuan-project
npm install
npm run dev
# 前端运行在 http://localhost:5173
```

## 项目结构

```
├── start.bat                  # Windows 一键启动
├── start.sh                   # Linux/macOS 一键启动
├── gaokao_volunteer.sql       # 数据库初始化脚本
├── backend/                   # 后端 Spring Boot
│   ├── pom.xml
│   ├── mvnw / mvnw.cmd
│   └── src/main/java/com/example/gaokaoproject/
│       ├── controller/        # 接口层 (27个控制器)
│       ├── service/           # 业务逻辑层
│       ├── mapper/            # MyBatis 数据访问层
│       ├── config/            # Spring 配置
│       ├── util/              # 工具类 (JWT, 文件上传等)
│       ├── vo/                # 视图对象
│       └── handler/           # 全局异常处理
├── frontend/vuezhiyuan-project/  # 前端 Vue 3
│   └── src/
│       ├── router/            # 路由配置
│       ├── views/             # 页面组件
│       │   ├── admin/         # 运营管理端
│       │   └── institution/   # 院校端
│       ├── api/               # 接口请求
│       ├── components/        # 通用组件
│       └── stores/            # 状态管理
└── gaokaoproject/             # （备用模块）
```

## 功能模块

### 考生端

- 登录注册（JWT 认证）
- 成绩录入与管理
- AI 智能志愿推荐
- 志愿方案规划
- 院校搜索与详情
- 收藏关注

### 院校端

- 院校注册与资质认证
- 招生计划发布
- 招生简章撰写
- 录取分数数据管理
- 工作台数据概览

### 运营管理端

- 用户管理
- 院校资质审核
- 招生简章审核
- 院校信息管理
- 专业/科目/地区字典维护
- 历史录取数据管理
- 登录日志审计

## API 文档

后端启动后访问：http://localhost:8080/doc.html
