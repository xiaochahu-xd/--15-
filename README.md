# 智能课程作业提交与批改管理系统

本项目是《软件体系结构》课程实践项目，采用 Vue 3 + Spring Boot + MySQL 实现。系统不使用 AI 助手，“智能”能力主要体现在迟交自动判断、单选题/判断题自动判分、作业内容相似度核查、成绩统计、Excel 导出和事件通知。

## 1. 技术栈

| 模块 | 技术 |
| --- | --- |
| 前端 | Vue 3、Vite、TypeScript、Pinia、Vue Router、Element Plus、ECharts |
| 后端 | Spring Boot 2.7.18、Spring Security、JWT、MyBatis Plus、Apache POI |
| 数据库 | MySQL 8.x |
| 构建工具 | Maven、npm |
| 推荐运行环境 | JDK 8、Node.js 18+、MySQL 8.x |

## 2. 项目结构

```text
.
├── backend/                  # Spring Boot 后端
│   ├── src/main/java/com/coursework/system/
│   │   ├── auth/             # 登录、JWT、权限
│   │   ├── course/           # 课程申请、审批、成员管理
│   │   ├── assignment/       # 作业、题目、教师附件
│   │   ├── submission/       # 作业提交、文件上传
│   │   ├── grading/          # 批改与成绩
│   │   ├── duplicate/        # 相似度核查记录
│   │   ├── rule/             # 规则系统
│   │   ├── event/            # 事件系统
│   │   ├── notification/     # 通知中心
│   │   └── statistics/       # 统计与 Excel 导出
│   └── src/main/resources/
│       ├── application.yml   # 后端配置
│       └── db/init.sql       # 数据库表结构与兼容迁移
├── frontend/                 # Vue 前端
│   ├── src/api/              # 接口封装
│   ├── src/components/ui/    # 公共组件
│   ├── src/layouts/          # 统一布局
│   ├── src/router/           # 路由权限
│   ├── src/stores/           # Pinia 状态
│   └── src/views/            # 业务页面
└── docs/                     # 阶段说明、自测和验收材料
```

## 3. 主要功能

- 登录认证：JWT 登录、当前用户信息、角色识别。
- RBAC 权限：管理员、教师、助教、学生四类角色，后端强制校验。
- 课程流程：教师申请创建课程，管理员审批，教师管理课程成员。
- 作业管理：发布文本、文件、单选题、判断题作业，文件作业支持教师上传本地附件或模板。
- 作业提交：学生提交文本、文件、单选题答案、判断题答案。
- 规则系统：迟交判断、客观题自动判分、内容相似度核查、成绩等级预留。
- 批改成绩：教师/助教人工批改、退回修改、学生查看成绩和评语。
- 通知事件：课程申请、审批、作业发布、提交、批改、退回、相似度提醒。
- 统计导出：课程统计、作业统计、成绩列表、ECharts 图表、Excel 导出。

## 4. 数据库配置

先启动 MySQL，然后创建数据库。只需要创建一次，后续启动不会重复手工创建。

```sql
CREATE DATABASE IF NOT EXISTS coursework_system
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

检查 `backend/src/main/resources/application.yml`，按本机 MySQL 修改用户名和密码：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/coursework_system?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: root

app:
  file-storage:
    root: uploads
  similarity:
    threshold: 0.90
    ngram-size: 2
```

后端启动时会自动执行 `backend/src/main/resources/db/init.sql`：

- 自动创建项目所需表。
- 自动补齐后续阶段新增字段。
- `continue-on-error: true` 用于兼容重复执行建表或加字段语句。

## 5. 后端启动

进入后端目录：

```powershell
cd backend
```

如果电脑上有多个 JDK，建议显式指定 JDK 8：

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk1.8.0_192'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
java -version
```

开发方式启动：

```powershell
mvn spring-boot:run
```

后端默认地址：

```text
http://localhost:8080
```

也可以打包运行：

```powershell
mvn -DskipTests package
java -jar target/coursework-system-backend-0.0.1-SNAPSHOT-boot.jar
```

## 6. 前端启动

进入前端目录：

```powershell
cd frontend
```

安装依赖：

```powershell
npm install
```

启动开发服务器：

```powershell
npm run dev
```

前端默认地址：

```text
http://localhost:5173
```

生产构建：

```powershell
npm run build
```

## 7. 演示账号

系统启动后会自动初始化基础账号，默认密码均为 `123456`。

| 角色 | 用户名 | 密码 | 用途 |
| --- | --- | --- | --- |
| 管理员 | `admin` | `123456` | 课程审批、用户管理、系统日志 |
| 教师 | `teacher01` | `123456` | 课程申请、作业发布、成员管理、批改、统计 |
| 助教 | `assistant01` | `123456` | 查看授权课程、批改授权课程提交 |
| 学生 A | `student01` | `123456` | 作业查看、提交、成绩查看 |
| 学生 B | `student02` | `123456` | 迟交和错误答案演示 |
| 学生 C | `student03` | `123456` | 相似提交演示 |

## 8. 推荐演示流程

1. 使用 `admin` 登录，查看控制台、课程审批、系统日志。
2. 使用 `teacher01` 登录，进入课程申请，申请创建“软件体系结构”课程。
3. 使用 `admin` 审批通过课程。
4. 使用 `teacher01` 管理课程成员，添加 `assistant01`、`student01`、`student02`、`student03`。
5. 教师发布文本作业、文件作业、单选题作业、判断题作业。
6. 学生查看课程作业并提交文本、文件、客观题答案。
7. 查看迟交自动判断、单选题/判断题自动判分。
8. 两名学生提交高度相似文本或文件正文，教师查看“相似度核查”。
9. 教师或助教进入待批改列表，查看高度相似提示并人工批改。
10. 学生查看成绩、评语和通知。
11. 教师进入成绩统计，查看图表并导出 Excel。

## 9. 相似度核查说明

原文件 Hash 检测只能发现完全相同文件。本项目已升级为“作业内容相似度匹配检测”：

- 文本作业：直接比较学生提交文本。
- 文件作业：提取文件正文后比较，当前支持纯文本文件和 `docx`。
- 单选题、判断题：不参与相似度检测。
- 检测范围：同一 `assignment_id` 下不同学生的提交。
- 默认阈值：`0.90`，可在 `app.similarity.threshold` 修改。
- 结果语义：只标记“高度相似提交”，提醒教师核查，不自动判定作弊，不自动判 0 分。
- 文件 Hash 保留为完全相同文件的辅助线索。

## 10. 架构说明

本系统报告中可按以下课程体系结构风格组织：

- 分层系统：Controller、Service、Mapper、Entity/DTO 分层。
- 面向对象：用户、课程、作业、提交、成绩、通知、核查记录等均为独立对象模型。
- 仓库风格：MySQL 作为中心仓库保存核心业务数据。
- 规则系统：`rule/` 模块独立实现迟交、自动判分、相似度核查等规则。
- 事件系统：`event/` 模块通过 EventManager 分发课程、作业、提交、批改等事件。
- 管道-过滤器：作业提交流程按权限校验、提交保存、文件处理、规则执行、通知日志顺序处理。
- 批处理：Excel 导出按读取成绩、汇总数据、生成文件、记录日志、返回下载结果执行。

## 11. 测试命令

后端测试：

```powershell
cd backend
mvn test
```

后端打包：

```powershell
cd backend
mvn -DskipTests package
```

前端构建：

```powershell
cd frontend
npm run build
```

## 12. 常见问题

### 12.1 直接在 cmd 输入 SQL 报错

`CREATE DATABASE ...` 不能直接在 Windows cmd 里执行，需要先进入 MySQL：

```powershell
mysql -u root -p
```

然后再执行建库 SQL。

### 12.2 每次启动都要创建数据库吗

不需要。数据库只需创建一次。后端每次启动会自动检查并创建表结构，不需要重复手工建库。

### 12.3 Maven 提示找不到 `spring-boot` 插件

通常是因为命令不在 `backend` 目录执行。请先进入后端目录：

```powershell
cd backend
mvn spring-boot:run
```

### 12.4 启动后数据库连接失败

检查：

- MySQL 服务是否启动。
- `coursework_system` 数据库是否已创建。
- `application.yml` 中的 `username`、`password` 是否与本机一致。
- MySQL 端口是否为 `3306`。

## 13. 端口

| 服务 | 默认端口 |
| --- | --- |
| 后端 API | `8080` |
| 前端 Vite | `5173` |
| MySQL | `3306` |

