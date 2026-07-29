-- 创建数据库
CREATE DATABASE IF NOT EXISTS edu_ai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE edu_ai;

-- ====================== 业务数据 ======================
-- 1. 用户表 users
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  `username` VARCHAR(64) NOT NULL COMMENT '用户名/学号',
  `realname` VARCHAR(32) NOT NULL COMMENT '真实名称',
  `password` VARCHAR(128) NOT NULL COMMENT '密码哈希值',
  `role` ENUM('student','teacher','researcher') NOT NULL COMMENT '角色：学生/教师/研究员',
  `avatarUrl` VARCHAR(512) DEFAULT '' COMMENT '头像url',
  `createdAt` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updatedAt` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  UNIQUE KEY uk_username (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 班级表 classes
DROP TABLE IF EXISTS `classes`;
CREATE TABLE `classes` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  `name` VARCHAR(64) NOT NULL COMMENT '班级名称',
  `createAt` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- 3. 课程表 courses
DROP TABLE IF EXISTS `courses`;
CREATE TABLE `courses` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  `name` VARCHAR(128) NOT NULL COMMENT '课程名称',
  `description` TEXT COMMENT '课程描述',
  `coverUrl` VARCHAR(512) DEFAULT '' COMMENT '封面图',
  `credit` TINYINT NOT NULL DEFAULT 0 COMMENT '学分',
  `status` ENUM('opening','archived') NOT NULL COMMENT '课程状态：上课中/已结课',
  `teacherId` BIGINT NOT NULL COMMENT '任课教师ID',
  `createdAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updatedAt` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_teacher (`teacherId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- 4. 章表 chapters
DROP TABLE IF EXISTS `chapters`;
CREATE TABLE `chapters` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  `courseId` BIGINT NOT NULL COMMENT '关联课程id',
  `name` VARCHAR(128) NOT NULL COMMENT '章节名称',
  `summary` TEXT COMMENT '章节简介',
  `sortOrder` INT NOT NULL DEFAULT 0 COMMENT '同课程下章节排序',
  `createdAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updatedAt` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_course (`courseId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='章表';

-- 5. 节表 sections
DROP TABLE IF EXISTS `sections`;
CREATE TABLE `sections` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  `chapterId` BIGINT NOT NULL COMMENT '关联章id',
  `name` VARCHAR(128) NOT NULL COMMENT '小节名称',
  `summary` TEXT COMMENT '小节简介',
  `sortOrder` INT NOT NULL DEFAULT 0 COMMENT '同章节下小节排序',
  `createdAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updatedAt` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_chapter (`chapterId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='节表';

-- 6. 节-知识点关联表 sectionKnowledgeRel
DROP TABLE IF EXISTS `sectionKnowledgeRel`;
CREATE TABLE `sectionKnowledgeRel` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  `sectionId` BIGINT NOT NULL COMMENT '小节id',
  `neo4jKpId` VARCHAR(64) NOT NULL COMMENT 'Neo4j知识点唯一id',
  `createdAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_section (`sectionId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小节关联知识点';

-- 7. 学习路径表 learningPaths
DROP TABLE IF EXISTS `learningPaths`;
CREATE TABLE `learningPaths` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `studentId` BIGINT NOT NULL COMMENT '学生ID',
  `courseId` BIGINT NOT NULL COMMENT '课程ID',
  `additionalInfo` TEXT COMMENT 'AI附加描述',
  `createdAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updatedAt` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_stu_course (`studentId`,`courseId`),
  KEY idx_student (`studentId`),
  KEY idx_course (`courseId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习路径';

-- 8. 学习路径节点表 learningPathNodes
DROP TABLE IF EXISTS `learningPathNodes`;
CREATE TABLE `learningPathNodes` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `pathId` BIGINT NOT NULL COMMENT '学习路径id',
  `knowledgeId` VARCHAR(64) NOT NULL COMMENT 'Neo4j知识点ID',
  `status` ENUM('untested','proceeding','completed') NOT NULL COMMENT '未进行/进行中/完成',
  `relatedTaskId` BIGINT DEFAULT NULL COMMENT '关联作业ID',
  `createdAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updatedAt` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_path (`pathId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习路径节点';

-- 9. 课程学生关联表 courseStudentRel
DROP TABLE IF EXISTS `courseStudentRel`;
CREATE TABLE `courseStudentRel` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `courseId` BIGINT NOT NULL,
  `studentId` BIGINT NOT NULL,
  UNIQUE KEY uk_course_stu (`courseId`,`studentId`),
  KEY idx_course (`courseId`),
  KEY idx_student (`studentId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程学生关联';

-- 10. 班级学生关联表 classStudentRel
DROP TABLE IF EXISTS `classStudentRel`;
CREATE TABLE `classStudentRel` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `classId` BIGINT NOT NULL,
  `studentId` BIGINT NOT NULL,
  UNIQUE KEY uk_class_stu (`classId`,`studentId`),
  KEY idx_class (`classId`),
  KEY idx_student (`studentId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级学生关联';

-- 11. 任务表 tasks
DROP TABLE IF EXISTS `tasks`;
CREATE TABLE `tasks` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `courseId` BIGINT NOT NULL,
  `assignerId` BIGINT NOT NULL COMMENT '布置人ID',
  `title` VARCHAR(256) NOT NULL COMMENT '标题',
  `description` TEXT COMMENT '说明',
  `type` ENUM('homework','exam','qna') NOT NULL COMMENT '作业/考试/问答',
  `totalScore` INT NOT NULL DEFAULT 0 COMMENT '总分',
  `startTime` DATETIME NOT NULL,
  `deadline` DATETIME DEFAULT NULL,
  `status` ENUM('draft','published','closed') NOT NULL COMMENT '草稿/已发布/已截止',
  `afterSubmitStrategy` ENUM('auto_all','auto_objective','manual') NOT NULL COMMENT '批改策略',
  `createdAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updatedAt` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_course (`courseId`),
  KEY idx_assigner (`assignerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务作业考试';

-- 12. 题目表 questions
DROP TABLE IF EXISTS `questions`;
CREATE TABLE `questions` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `courseId` BIGINT NOT NULL,
  `teacherId` BIGINT NOT NULL,
  `type` ENUM('choice','fill','subjective','true_false') NOT NULL COMMENT '题型',
  `content` TEXT NOT NULL COMMENT '题干',
  `answer` TEXT COMMENT '答案JSON',
  `createdAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updatedAt` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_course (`courseId`),
  KEY idx_teacher (`teacherId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目';

-- 13. 任务学生关联表 taskStudentRel
DROP TABLE IF EXISTS `taskStudentRel`;
CREATE TABLE `taskStudentRel` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `taskId` BIGINT NOT NULL,
  `studentId` BIGINT NOT NULL,
  UNIQUE KEY uk_task_stu (`taskId`,`studentId`),
  KEY idx_task (`taskId`),
  KEY idx_student (`studentId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务学生关联';

-- 14. 任务题目关联表 taskQuestionRel
DROP TABLE IF EXISTS `taskQuestionRel`;
CREATE TABLE `taskQuestionRel` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `taskId` BIGINT NOT NULL,
  `questionId` BIGINT NOT NULL,
  `score` INT NOT NULL DEFAULT 0 COMMENT '单题分值',
  `index` INT NOT NULL DEFAULT 0 COMMENT '题号',
  KEY idx_task (`taskId`),
  KEY idx_question (`questionId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务题目关联';

-- 15. 学生提交表 studentSubmissions
DROP TABLE IF EXISTS `studentSubmissions`;
CREATE TABLE `studentSubmissions` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `taskId` BIGINT NOT NULL,
  `studentId` BIGINT NOT NULL,
  `status` ENUM('pending','submitted','passed','returned','draft') NOT NULL,
  `submittedTime` DATETIME DEFAULT NULL,
  `gradedTime` DATETIME DEFAULT NULL,
  `totalScore` INT DEFAULT NULL,
  `comment` TEXT COMMENT '评语',
  `createdAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_task (`taskId`),
  KEY idx_student (`studentId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生提交记录';

-- 16. 学生单题答题表 studentAnswers
DROP TABLE IF EXISTS `studentAnswers`;
CREATE TABLE `studentAnswers` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `submissionId` BIGINT NOT NULL,
  `questionId` BIGINT NOT NULL,
  `answer` TEXT COMMENT '学生答案JSON',
  `score` INT DEFAULT NULL COMMENT '单题得分',
  `comment` TEXT COMMENT '单题评语',
  `timeSpent` INT DEFAULT 0 COMMENT '耗时(秒)',
  `attemptCount` INT NOT NULL DEFAULT 0 COMMENT '修改次数',
  `helpRequestCount` INT NOT NULL DEFAULT 0 COMMENT '求助次数',
  `errorType` VARCHAR(64) DEFAULT NULL COMMENT '错误类型',
  `createdAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_submit (`submissionId`),
  KEY idx_question (`questionId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单题作答';

-- 17. AI会话 chatSession
DROP TABLE IF EXISTS `chatSession`;
CREATE TABLE `chatSession` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `userId` BIGINT NOT NULL,
  `createdAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_user (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI会话';

-- 18. 对话消息 chatMessages
DROP TABLE IF EXISTS `chatMessages`;
CREATE TABLE `chatMessages` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `chatId` BIGINT NOT NULL,
  `msgSeq` INT NOT NULL COMMENT '会话内序号',
  `msgType` ENUM('text','media','file','system') NOT NULL,
  `senderId` BIGINT NOT NULL,
  `message` TEXT COMMENT '消息JSON/URL',
  `sentAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_chat (`chatId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话消息';

-- 19. 会话课程关联 chatCourseRel
DROP TABLE IF EXISTS `chatCourseRel`;
CREATE TABLE `chatCourseRel` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `chatId` BIGINT NOT NULL,
  `courseId` BIGINT NOT NULL,
  `userId` BIGINT NOT NULL COMMENT '冗余学生ID',
  UNIQUE KEY uk_chat_course (`chatId`),
  KEY idx_course (`courseId`),
  KEY idx_user (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话绑定课程学生';

-- 20. AI建议 aiSuggestions
DROP TABLE IF EXISTS `aiSuggestions`;
CREATE TABLE `aiSuggestions` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `receiverId` BIGINT NOT NULL COMMENT '接收人ID',
  `title` VARCHAR(256) DEFAULT NULL,
  `message` TEXT NOT NULL COMMENT 'Markdown正文',
  `displayType` ENUM('info','warning') NOT NULL,
  `displayLocation` VARCHAR(128) NOT NULL COMMENT '展示位置标识',
  `createdAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_receiver (`receiverId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI提示建议';

-- 21. AI引用 aiCitations
DROP TABLE IF EXISTS `aiCitations`;
CREATE TABLE `aiCitations` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `chatMessageId` BIGINT DEFAULT NULL,
  `suggestionsId` BIGINT DEFAULT NULL,
  `title` VARCHAR(256) NOT NULL,
  `dataType` ENUM('file','url','text_quote','student_record','student_progress','knowledge','exercise','exam') NOT NULL,
  `data` TEXT COMMENT '引用数据JSON',
  KEY idx_msg (`chatMessageId`),
  KEY idx_sug (`suggestionsId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI引用资源';

-- 22. 用户文件 userFiles
DROP TABLE IF EXISTS `userFiles`;
CREATE TABLE `userFiles` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `userId` BIGINT NOT NULL,
  `fileName` VARCHAR(256) NOT NULL,
  `fileDesc` TEXT,
  `fileUrl` VARCHAR(512) NOT NULL,
  `size` BIGINT NOT NULL COMMENT '字节大小',
  `type` ENUM('pdf','pptx','docx','media','other') NOT NULL,
  `source` ENUM('upload','generated') NOT NULL COMMENT '文件来源',
  `updateAt` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `createTime` DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_user (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户上传/AI生成文件';

-- 23. 课件生成记录表 coursewareRecords
DROP TABLE IF EXISTS `coursewareRecords`;
CREATE TABLE `coursewareRecords` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `userId` BIGINT NOT NULL COMMENT '教师ID',
  `chatId` BIGINT NOT NULL COMMENT 'AI对话ID',
  `pptFileId` BIGINT DEFAULT NULL COMMENT 'PPT文件ID',
  `wordFileId` BIGINT DEFAULT NULL COMMENT '教案文件ID',
  `pptHtml` TEXT COMMENT 'PPT原始HTML',
  `wordHtml` TEXT COMMENT '教案原始HTML',
  `updatedAt` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `createdAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_user (`userId`),
  KEY idx_chat (`chatId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI课件生成记录';

-- 24. 课件文件关联表 coursewareFileRel
DROP TABLE IF EXISTS `coursewareFileRel`;
CREATE TABLE `coursewareFileRel` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `coursewareRecordId` BIGINT NOT NULL,
  `fileId` BIGINT NOT NULL,
  KEY idx_record (`coursewareRecordId`),
  KEY idx_file (`fileId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课件引用文件关联';

-- ====================== 统计与分析数据 ======================
-- 25. 教师课程分析 teacherCourseAnalysises
DROP TABLE IF EXISTS `teacherCourseAnalysises`;
CREATE TABLE `teacherCourseAnalysises` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `courseId` BIGINT NOT NULL,
  `totalStudents` INT NOT NULL DEFAULT 0,
  `avgScoreRate` DECIMAL(5,2) DEFAULT 0 COMMENT '得分率',
  `avgProgress` DECIMAL(5,2) DEFAULT 0 COMMENT '平均进度',
  `taskCount` INT NOT NULL DEFAULT 0,
  `submissionRate` DECIMAL(5,2) DEFAULT 0 COMMENT '提交率',
  `openingQuestions` INT NOT NULL DEFAULT 0 COMMENT '待回答问题数',
  `alertStudentCount` INT NOT NULL DEFAULT 0 COMMENT '预警学生数',
  `weakestKnowledge` VARCHAR(64) DEFAULT NULL COMMENT '薄弱知识点Neo4jID',
  `updatedAt` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_course (`courseId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师课程分析';

-- 26. 学生课程进度 studentCourseProgresses
DROP TABLE IF EXISTS `studentCourseProgresses`;
CREATE TABLE `studentCourseProgresses` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `studentId` BIGINT NOT NULL,
  `courseId` BIGINT NOT NULL,
  `totalProgress` DECIMAL(5,2) DEFAULT 0 COMMENT '课程总进度',
  `interactionCount` INT NOT NULL DEFAULT 0 COMMENT '互动次数',
  `avgTaskScore` DECIMAL(5,2) DEFAULT 0 COMMENT '作业均分',
  `knowledgeCoverRate` DECIMAL(5,2) DEFAULT 0 COMMENT '知识点覆盖度',
  `taskCompletionRate` DECIMAL(5,2) DEFAULT 0 COMMENT '任务完成度',
  `updatedAt` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_stu_course (`studentId`,`courseId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生课程学习进度';

-- 27. 学生知识点掌握度 studentKnowledgeMasteries
DROP TABLE IF EXISTS `studentKnowledgeMasteries`;
CREATE TABLE `studentKnowledgeMasteries` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `studentId` BIGINT NOT NULL,
  `courseId` BIGINT DEFAULT NULL,
  `knowledgeId` VARCHAR(64) NOT NULL,
  `masterRate` DECIMAL(5,2) DEFAULT 0 COMMENT '掌握度',
  `questionCorrectCount` INT NOT NULL DEFAULT 0,
  `questionTotalCount` INT NOT NULL DEFAULT 0,
  `updatedAt` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_stu_kp (`studentId`,`knowledgeId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生知识点掌握情况';

-- 28. 学生能力基线 studentAbilityBaselines
DROP TABLE IF EXISTS `studentAbilityBaselines`;
CREATE TABLE `studentAbilityBaselines` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `studentId` BIGINT NOT NULL,
  `difficultyLevel` VARCHAR(32) NOT NULL COMMENT '最近发展区难度层级',
  `firstContactAccuracy` DECIMAL(5,2) DEFAULT 0 COMMENT '首次接触正确率',
  `stabilityRate` DECIMAL(5,2) DEFAULT 0 COMMENT '稳定率',
  `updatedAt` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_student (`studentId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生能力基线画像';

-- 29. 学生学习态度 studentLearningAttitudes
DROP TABLE IF EXISTS `studentLearningAttitudes`;
CREATE TABLE `studentLearningAttitudes` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `studentId` BIGINT NOT NULL,
  `kbViewCount` INT NOT NULL DEFAULT 0 COMMENT '知识库浏览次数',
  `errorReviewCount` INT NOT NULL DEFAULT 0 COMMENT '错题复习次数',
  `interactionCount` INT NOT NULL DEFAULT 0 COMMENT '交互提问次数',
  `guidePushCount` INT NOT NULL DEFAULT 0 COMMENT '系统引导推送次数',
  `guideAcceptCount` INT NOT NULL DEFAULT 0 COMMENT '接受引导次数',
  `attitudeScore` DECIMAL(5,2) DEFAULT 0 COMMENT '学习态度评分',
  `updatedAt` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_student (`studentId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生学习态度画像';

-- 30. 学生求助密度 studentHelpDensities
DROP TABLE IF EXISTS `studentHelpDensities`;
CREATE TABLE `studentHelpDensities` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `studentId` BIGINT NOT NULL,
  `topDifficultyLevel` VARCHAR(32) DEFAULT NULL COMMENT '高频求助难度层级',
  `helpCount` INT NOT NULL DEFAULT 0 COMMENT '总求助次数',
  `topHelpType` VARCHAR(64) DEFAULT NULL COMMENT '高频求助类型',
  `updatedAt` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_student (`studentId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生求助密度画像';

-- 31. 学生交互风格 studentInteractionStyles
DROP TABLE IF EXISTS `studentInteractionStyles`;
CREATE TABLE `studentInteractionStyles` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `studentId` BIGINT NOT NULL,
  `avgTextLength` INT DEFAULT 0 COMMENT '平均发文字数',
  `seriousnessScore` DECIMAL(5,2) DEFAULT 0 COMMENT '认真度评分',
  `directAnswerCount` INT NOT NULL DEFAULT 0 COMMENT '直接要答案次数',
  `conceptInquiryCount` INT NOT NULL DEFAULT 0 COMMENT '概念提问次数',
  `confirmationCount` INT NOT NULL DEFAULT 0 COMMENT '确认提问次数',
  `otherQueryCount` INT NOT NULL DEFAULT 0 COMMENT '其他提问次数',
  `emotionPositiveCount` INT NOT NULL DEFAULT 0 COMMENT '积极情绪对话次数',
  `emotionNeutralCount` INT NOT NULL DEFAULT 0 COMMENT '中性情绪对话次数',
  `emotionNegativeCount` INT NOT NULL DEFAULT 0 COMMENT '消极情绪对话次数',
  `totalInterations` INT NOT NULL DEFAULT 0 COMMENT '总交互次数',
  `studentInteractionStyle` TEXT COMMENT 'AI评估交互风格描述',
  `preferredAiStyle` TEXT COMMENT '推荐AI交互风格',
  `updatedAt` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_student (`studentId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生交互风格画像';

-- 32. 任务分析 missionAnalysises
DROP TABLE IF EXISTS `missionAnalysises`;
CREATE TABLE `missionAnalysises` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `taskId` BIGINT NOT NULL,
  `totalSubmitted` INT NOT NULL DEFAULT 0,
  `avgScore` DECIMAL(5,2) DEFAULT 0,
  `maxScore` INT DEFAULT 0,
  `minScore` INT DEFAULT 0,
  `avgTimeSpent` INT DEFAULT 0 COMMENT '平均耗时秒',
  `updatedAt` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_task (`taskId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务整体分析';

-- 33. 题目属性 questionProperties
DROP TABLE IF EXISTS `questionProperties`;
CREATE TABLE `questionProperties` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `questionId` BIGINT NOT NULL,
  `analysis` TEXT COMMENT '题目解析',
  `source` ENUM('ai','manual','import') NOT NULL COMMENT '题目来源',
  `difficulty` ENUM('easy','medium','hard') NOT NULL COMMENT '难度',
  UNIQUE KEY uk_question (`questionId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目扩展属性';

-- 34. 题目知识点关联 questionKnowledgeRel
DROP TABLE IF EXISTS `questionKnowledgeRel`;
CREATE TABLE `questionKnowledgeRel` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `questionId` BIGINT NOT NULL,
  `knowledgeNode` VARCHAR(64) NOT NULL COMMENT 'Neo4j知识点ID',
  `order` INT NOT NULL DEFAULT 0 COMMENT '权重排序',
  KEY idx_question (`questionId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目关联知识点';

-- 35. 教师题目分析 teacherQuestionAnalysises
DROP TABLE IF EXISTS `teacherQuestionAnalysises`;
CREATE TABLE `teacherQuestionAnalysises` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `questionId` BIGINT NOT NULL,
  `studentCount` INT NOT NULL DEFAULT 0 COMMENT '作答人数',
  `avgCorrectRate` DECIMAL(5,2) DEFAULT 0 COMMENT '平均正确率',
  `avgTimeSpent` INT DEFAULT 0 COMMENT '平均耗时',
  `abandonRate` DECIMAL(5,2) DEFAULT 0 COMMENT '弃题率',
  `avgHelpRequestCount` DECIMAL(5,2) DEFAULT 0 COMMENT '平均求助次数',
  `aiQualityRating` DECIMAL(5,2) DEFAULT 0 COMMENT 'AI题目质量评分',
  `aiDifficultyRating` DECIMAL(5,2) DEFAULT 0 COMMENT 'AI评估难度',
  `topErrorType` VARCHAR(64) DEFAULT NULL COMMENT '高频错误类型',
  `updatedAt` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_question (`questionId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单题学情分析';