# 作业相似度匹配检测重构说明

## 1. 从 Hash 检测升级的原因

原有文件 Hash 检测只能发现“文件内容完全一致”的提交。如果学生对文本或文件正文做少量改写，Hash 会完全不同，系统无法给教师提供核查线索。因此本阶段将核心检测能力升级为“基于提交正文的文本相似度检测”：

- 文本作业直接比较 `submission.content`。
- 文件作业先提取文件正文，再进行相似度比较。
- 单选题、判断题不参与相似度检测。
- 文件 Hash 保留为完全相同文件的辅助核查方式，不再作为主要重复性检测逻辑。

## 2. 检测流程

学生提交作业后，系统在提交服务中调用规则引擎：

1. 保存提交记录和文件记录。
2. `RuleEngine` 执行迟交判断和客观题判分。
3. `SimilarityDetectionRule` 判断作业类型，仅处理文本作业和文件作业。
4. `TextExtractor` 提取提交文本，文件作业支持 `txt`、`md` 等纯文本文件和 `docx`。
5. `TextNormalizer` 去除空白、大小写差异和常见标点。
6. `TextSimilarityCalculator` 使用字符 n-gram 与余弦相似度计算得分。
7. `SimilarityDetectionService` 只比较同一 `assignment_id` 下其他学生的提交。
8. 相似度达到阈值时写入 `duplicate_records`，状态为 `PENDING_REVIEW`。
9. 系统向课程负责人、课程教师和助教发送“发现高度相似提交”通知。

文件无法提取正文时，提交不会失败，系统只记录操作日志说明该文件未参与相似度检测。

## 3. 规则系统体现

规则模块继续保持独立：

- `SimilarityDetectionRule`：相似度检测规则入口。
- `TextExtractor`：文本提取构件。
- `TextNormalizer`：文本标准化构件。
- `TextSimilarityCalculator`：相似度计算构件。
- `SimilarityMatchResult`：规则匹配结果对象。
- `SimilarityDetectionService`：同作业提交查询、匹配记录保存和通知触发。

Controller 不包含相似度判断逻辑，提交服务只负责在提交保存后调用 `RuleEngine`，符合规则系统风格。

## 4. 管道-过滤器体现

作业提交处理可以解释为一条处理管道：

权限校验 -> 提交保存 -> 文件保存 -> 文本提取 -> 文本标准化 -> 相似度计算 -> 规则结果写回 -> 通知教师

每个步骤职责单一，文本提取失败不会中断核心提交数据保存。

## 5. 数据库变更

复用 `duplicate_records` 表，并扩展为“相似度核查记录”：

- `source_submission_id`
- `matched_submission_id`
- `source_student_id`
- `matched_student_id`
- `detection_type`：`TEXT_SIMILARITY` 或 `EXACT_HASH`
- `similarity_score`
- `threshold`
- `status`：`PENDING_REVIEW`、`CONFIRMED`、`IGNORED`
- `remark`

旧字段 `file_hash`、`submission_ids` 继续保留，用于兼容完全相同文件的辅助记录。

## 6. 接口变更

- `GET /api/assignments/{assignmentId}/similarity-records`：教师、助教、管理员查看作业相似度记录。
- `GET /api/submissions/{submissionId}/similarity-records`：教师、助教、管理员查看某次提交的相似度记录。
- `PUT /api/similarity-records/{id}/confirm`：确认相似提醒。
- `PUT /api/similarity-records/{id}/ignore`：忽略相似提醒。

待批改任务、提交详情和学生提交聚合接口增加 `hasSimilarityAlert`、`similarityScore` 字段。

## 7. 前端展示变化

- 待批改列表显示“高度相似”标签和最高相似度。
- 批改详情显示相似度匹配记录。
- 课程详情提交 Tab 显示相似度核查状态。
- 作业管理页入口从“重复”改为“相似度”。
- 通知中心支持 `SIMILARITY_ALERT` 类型。
- 学生端只显示“已进入教师核查”，不展示匹配学生姓名和全班记录。

## 8. 测试结果

已增加规则模块测试：

- 文本完全相同，相似度为 `1.0000`。
- 文本少量改写，相似度不低于 `0.9000`。
- 主题不同文本，相似度低于 `0.9000`。
- 迟交判断、单选题判分、判断题判分和 Hash 辅助规则保留原测试覆盖。

验证命令：

```powershell
cd D:\软件体系结构\backend
$env:JAVA_HOME='C:\Program Files\Java\jdk1.8.0_192'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn -q test
mvn -q -DskipTests package

cd D:\软件体系结构\frontend
npm run build
```

