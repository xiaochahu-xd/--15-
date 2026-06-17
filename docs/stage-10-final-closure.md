# 第 10 阶段：业务闭环补强与最终验收材料

## 1. 本阶段完成内容

| 类别 | 完成内容 |
| --- | --- |
| 通知数据 | `notifications` 表新增 `target_type`、`target_id`、`course_id`，支持通知精确定位到课程、作业、提交、成绩、疑似重复记录 |
| 通知接口 | `GET /api/notifications` 支持 `courseId` 参数；新增 `GET /api/courses/{courseId}/notifications` |
| 通知路由 | 通知中心点击通知后先按 `target_type/target_id/course_id` 跳转，缺少目标字段时按旧 `type` 回退 |
| 课程详情 | 课程详情页的“课程通知”只查询当前课程通知，并由后端校验课程访问权限 |
| 学生提交 | 新增 `GET /api/student/submissions`，按当前登录学生聚合提交、课程、作业、成绩、文件、重复状态 |
| 前端页面 | 重写通知中心与学生“我的提交”页，增加类型、状态、课程、关键字筛选 |
| 疑似重复提醒 | 文件 Hash 疑似重复记录生成后，通知课程负责人、课程教师和助教，但不自动判 0 分 |
| 验收材料 | 新增最终测试报告与 5-7 分钟演示指南 |

## 2. 后端新增或调整接口

| 接口 | 权限 | 说明 |
| --- | --- | --- |
| `GET /api/notifications?courseId={courseId}` | 已登录 | 查询当前用户通知，可按课程过滤 |
| `GET /api/courses/{courseId}/notifications` | 已登录且有课程访问权 | 查询课程相关通知；管理员可看课程全量通知，普通用户只看自己的通知 |
| `GET /api/student/submissions` | 学生 | 查询当前学生全部提交聚合记录，不接收 `studentId` 参数 |

`GET /api/student/submissions` 支持参数：`courseId`、`assignmentType`、`status`、`keyword`、`page`、`size`。

返回字段包括：`submissionId`、`assignmentId`、`assignmentTitle`、`assignmentType`、`courseId`、`courseName`、`submitTime`、`deadline`、`late`、`duplicate`、`autoScore`、`finalScore`、`gradeStatus`、`comment`、`fileName`、`fileHash`、`status`。

## 3. 数据库变更

`notifications` 表新增：

| 字段 | 作用 |
| --- | --- |
| `target_type` | 通知目标类型，如 `COURSE`、`ASSIGNMENT`、`SUBMISSION`、`GRADE`、`DUPLICATE_RECORD` |
| `target_id` | 目标记录 ID |
| `course_id` | 课程维度过滤与课程详情页通知展示 |

初始化脚本已保留 `ALTER TABLE` 兼容旧库，`spring.sql.init.continue-on-error=true` 可跳过已存在字段。

## 4. 通知精确路由规则

| 目标类型 | 跳转规则 |
| --- | --- |
| `COURSE_APPLICATION` | 管理员进入课程审批，教师进入我的课程 |
| `COURSE` | 进入 `/courses/{targetId}` |
| `ASSIGNMENT` | 优先进入 `/courses/{courseId}?tab=assignments` |
| `SUBMISSION` | 学生进入个人提交详情，教师/管理员/助教进入提交详情 |
| `GRADE` | 学生进入我的成绩，教师/助教进入成绩统计 |
| `DUPLICATE_RECORD` | 优先进入课程详情的提交情况 Tab |
| `SYSTEM` 或缺少目标字段 | 进入仪表盘或旧类型对应入口 |

## 5. 架构说明

| 课程体系结构风格 | 系统体现 |
| --- | --- |
| 层次系统 | Controller、Service、Mapper、Entity、DTO 分层清晰 |
| 面向对象 | 用户、课程、作业、提交、成绩、通知、重复记录均为独立对象模型 |
| 仓库风格 | MySQL 保存核心业务状态，Mapper 作为仓库访问入口 |
| 规则系统 | `rule/` 模块独立执行迟交、客观题判分、Hash 重复检测 |
| 事件系统 | `event/` 模块统一分发课程申请、作业发布、提交、批改、退回等事件 |
| 管道-过滤器 | 作业提交按“权限校验、提交保存、文件保存、规则执行、事件通知、日志记录”顺序处理 |
| 批处理 | Excel 导出按“读取成绩、汇总数据、生成文件、记录日志、返回下载”处理 |

## 6. 验证结果

| 命令 | 结果 |
| --- | --- |
| `mvn -q test` | 通过 |
| `mvn -q -DskipTests package` | 通过 |
| `npm run build` | 通过，Vite 仅提示 ECharts 相关 chunk 体积较大 |

## 7. 仍可增强点

1. 通知中心和学生提交聚合页本阶段为英文文案，可后续统一为中文文案。
2. 前端生产包存在 ECharts 相关大 chunk 警告，可在正式部署前做手动分包。
3. 当前 Excel 导出为同步下载，课程人数很大时可扩展为异步导出任务和下载中心。
