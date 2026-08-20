-- 创建数据库
CREATE DATABASE IF NOT EXISTS edu_ai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE edu_ai;

-- ====================== 一、基础业务数据表 ======================
-- 1. 用户表 users
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`
(
    `id`         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `username`   VARCHAR(64)                             NOT NULL COMMENT '用户名/学号',
    `realname`   VARCHAR(32)                             NOT NULL COMMENT '真实名称',
    `password`   VARCHAR(128)                            NOT NULL COMMENT '密码哈希值',
    `role`       ENUM ('student','teacher','researcher') NOT NULL COMMENT '角色：学生/教师/研究员',
    `avatar_url` VARCHAR(512) DEFAULT '' COMMENT '头像url',
    `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY uk_username (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';

-- 2. 班级表 classes
DROP TABLE IF EXISTS `classes`;
CREATE TABLE `classes`
(
    `id`         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `name`       VARCHAR(64) NOT NULL COMMENT '班级名称',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='班级表';

-- 3. 课程表 course
DROP TABLE IF EXISTS `courses`;
CREATE TABLE `courses`
(
    `id`             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `course_data_id` BIGINT                      NOT NULL COMMENT '关联课程数据ID',
    `name`           VARCHAR(128)                NOT NULL COMMENT '课程名称',
    `description`    TEXT COMMENT '课程描述',
    `cover_url`      VARCHAR(512)                         DEFAULT '' COMMENT '封面图',
    `credit`         TINYINT                     NOT NULL DEFAULT 0 COMMENT '学分',
    `status`         ENUM ('opening','archived') NOT NULL COMMENT '课程状态：上课中/已结课',
    `teacher_id`     BIGINT                      NOT NULL COMMENT '任课教师ID',
    `created_at`     DATETIME                             DEFAULT CURRENT_TIMESTAMP,
    `updated_at`     DATETIME                             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_teacher (`teacher_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='课程表';

-- 4. 章表 chapters
DROP TABLE IF EXISTS `chapters`;
CREATE TABLE `chapters`
(
    `id`             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `course_data_id` BIGINT       NOT NULL COMMENT '关联课程数据id',
    `name`           VARCHAR(128) NOT NULL COMMENT '章节名称',
    `summary`        TEXT COMMENT '章节简介',
    `sort_order`     INT          NOT NULL DEFAULT 0 COMMENT '排序',
    `created_at`     DATETIME              DEFAULT CURRENT_TIMESTAMP,
    `updated_at`     DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_course (`course_data_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='章表';

-- 5. 节表 sections
DROP TABLE IF EXISTS `sections`;
CREATE TABLE `sections`
(
    `id`         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `chapter_id` BIGINT       NOT NULL COMMENT '关联章id',
    `name`       VARCHAR(128) NOT NULL COMMENT '小节名称',
    `summary`    TEXT COMMENT '小节简介',
    `sort_order` INT          NOT NULL DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME              DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_chapter (`chapter_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='节表';

-- 6. 节-知识点关联表 section_knowledge_rel
DROP TABLE IF EXISTS `section_knowledge_rel`;
CREATE TABLE `section_knowledge_rel`
(
    `id`             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `course_data_id` BIGINT      NOT NULL COMMENT '课程数据ID，冗余',
    `section_id`     BIGINT      NOT NULL COMMENT '小节id',
    `neo4j_kp_id`    VARCHAR(64) NOT NULL COMMENT 'Neo4j知识点唯一id',
    `created_at`     DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_section (`section_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='小节关联知识点';

-- 7. 学习路径表 learning_paths
DROP TABLE IF EXISTS `learning_paths`;
CREATE TABLE `learning_paths`
(
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY,
    `student_id`      BIGINT NOT NULL COMMENT '学生ID',
    `course_id`       BIGINT NOT NULL COMMENT '课程ID',
    `current_node_id` BIGINT                                                    DEFAULT NULL COMMENT '当前正在进行的learning_path_node_id，NULL表示未开始',
    `current_stage`   ENUM ('introducing', 'explaining', 'testing', 'finished') DEFAULT NULL COMMENT '当前教学阶段：introducing-引入, explaining-讲解, testing-测验, finished-结束',
    `additional_info` TEXT COMMENT 'AI附加描述',
    `created_at`      DATETIME                                                  DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      DATETIME                                                  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_stu_course (`student_id`, `course_id`),
    KEY idx_student (`student_id`),
    KEY idx_course (`course_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='学习路径';

-- 8. 学习路径节点表 learning_path_nodes
DROP TABLE IF EXISTS `learning_path_nodes`;
CREATE TABLE `learning_path_nodes`
(
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY,
    `node_name`       VARCHAR(128)                               NOT NULL COMMENT '节点名称',
    `path_id`         BIGINT                                     NOT NULL COMMENT '学习路径id',
    `knowledge_ids`   TEXT                                       NOT NULL COMMENT '知识点ID列表(JSON数组)',
    `status`          ENUM ('untested','proceeding','completed') NOT NULL COMMENT '未进行/进行中/完成',
    `related_task_id` BIGINT   DEFAULT NULL COMMENT '关联作业ID',
    `created_at`      DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_path (`path_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='学习路径节点';

-- 9. 课程学生关联表 course_student_rel
DROP TABLE IF EXISTS `course_student_rel`;
CREATE TABLE `course_student_rel`
(
    `id`         BIGINT AUTO_INCREMENT PRIMARY KEY,
    `course_id`  BIGINT NOT NULL,
    `student_id` BIGINT NOT NULL,
    UNIQUE KEY uk_course_stu (`course_id`, `student_id`),
    KEY idx_course (`course_id`),
    KEY idx_student (`student_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='课程学生关联';

-- 10. 班级学生关联表 class_student_rel
DROP TABLE IF EXISTS `class_student_rel`;
CREATE TABLE `class_student_rel`
(
    `id`         BIGINT AUTO_INCREMENT PRIMARY KEY,
    `class_id`   BIGINT NOT NULL,
    `student_id` BIGINT NOT NULL,
    UNIQUE KEY uk_class_stu (`class_id`, `student_id`),
    KEY idx_class (`class_id`),
    KEY idx_student (`student_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='班级学生关联';

-- 11. 任务表 tasks
DROP TABLE IF EXISTS `tasks`;
CREATE TABLE `tasks`
(
    `id`                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    `course_id`             BIGINT COMMENT '关联课程ID',
    `assigner_id`           BIGINT COMMENT '布置人ID',
    `title`                 VARCHAR(256)                                NOT NULL COMMENT '标题',
    `description`           TEXT COMMENT '说明',
    `type`                  ENUM ('homework','exam','qna')              NOT NULL COMMENT '作业/考试/问答',
    `total_score`           INT                                         NOT NULL DEFAULT 0 COMMENT '总分',
    `start_time`            DATETIME                                    NOT NULL,
    `deadline`              DATETIME                                             DEFAULT NULL,
    `status`                ENUM ('draft','published','closed')         NOT NULL COMMENT '草稿/已发布/已截止',
    `after_submit_strategy` ENUM ('auto_all','auto_objective','manual') NOT NULL COMMENT '批改策略',
    `created_at`            DATETIME                                             DEFAULT CURRENT_TIMESTAMP,
    `updated_at`            DATETIME                                             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_course (`course_id`),
    KEY idx_assigner (`assigner_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='任务作业考试';

-- 12. 题目表 questions
DROP TABLE IF EXISTS `questions`;
CREATE TABLE `questions`
(
    `id`         BIGINT AUTO_INCREMENT PRIMARY KEY,
    `course_id`  BIGINT                                           NOT NULL,
    `teacher_id` BIGINT                                           NOT NULL,
    `type`       ENUM ('choice','fill','subjective','true_false') NOT NULL COMMENT '题型',
    `content`    TEXT                                             NOT NULL COMMENT '题干',
    `answer`     TEXT COMMENT '答案JSON',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_course (`course_id`),
    KEY idx_teacher (`teacher_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='题目';

-- 13. 任务学生关联表 task_student_rel
DROP TABLE IF EXISTS `task_student_rel`;
CREATE TABLE `task_student_rel`
(
    `id`         BIGINT AUTO_INCREMENT PRIMARY KEY,
    `task_id`    BIGINT NOT NULL,
    `student_id` BIGINT NOT NULL,
    UNIQUE KEY uk_task_stu (`task_id`, `student_id`),
    KEY idx_task (`task_id`),
    KEY idx_student (`student_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='任务学生关联';

-- 14. 任务题目关联表 task_question_rel
DROP TABLE IF EXISTS `task_question_rel`;
CREATE TABLE `task_question_rel`
(
    `id`            BIGINT AUTO_INCREMENT PRIMARY KEY,
    `task_id`       BIGINT NOT NULL,
    `question_id`   BIGINT NOT NULL,
    `score`         INT    NOT NULL DEFAULT 0 COMMENT '单题分值',
    `question_sort` INT    NOT NULL DEFAULT 0 COMMENT '题号',
    KEY idx_task_question (`task_id`, `question_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='任务题目关联';

-- 15. 学生提交表 student_submissions
DROP TABLE IF EXISTS `student_submissions`;
CREATE TABLE `student_submissions`
(
    `id`             BIGINT AUTO_INCREMENT PRIMARY KEY,
    `task_id`        BIGINT                                                   NOT NULL,
    `student_id`     BIGINT                                                   NOT NULL,
    `status`         ENUM ('pending','submitted','passed','returned','draft') NOT NULL,
    `submitted_time` DATETIME DEFAULT NULL,
    `graded_time`    DATETIME DEFAULT NULL,
    `total_score`    INT      DEFAULT NULL,
    `comment`        TEXT COMMENT '评语',
    `created_at`     DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at`     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_task (`task_id`),
    KEY idx_student (`student_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='学生提交记录';

-- 16. 学生单题答题表 student_answers
DROP TABLE IF EXISTS `student_answers`;
CREATE TABLE `student_answers`
(
    `id`                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    `submission_id`      BIGINT NOT NULL,
    `question_id`        BIGINT NOT NULL,
    `answer`             TEXT COMMENT '学生答案JSON',
    `score`              INT                                                    DEFAULT NULL COMMENT '单题得分',
    `comment`            TEXT                                                   DEFAULT NULL COMMENT '单题评语',
    `time_spent`         INT                                                    DEFAULT 0 COMMENT '耗时(秒)',
    `attempt_count`      INT    NOT NULL                                        DEFAULT 0 COMMENT '修改次数',
    `help_request_count` INT    NOT NULL                                        DEFAULT 0 COMMENT '求助次数',
    `error_type`         ENUM ('concept', 'method', 'calc', 'misread', 'other') DEFAULT NULL COMMENT '错误类型: concept 概念理解错误 | method 解题方法错误 | calc 计算错误 | misread 审题错误 | other 其它',
    `created_at`         DATETIME                                               DEFAULT CURRENT_TIMESTAMP,
    KEY idx_submit (`submission_id`),
    KEY idx_question (`question_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='单题作答';

-- 17. AI会话 chat_session
DROP TABLE IF EXISTS `chat_session`;
CREATE TABLE `chat_session`
(
    `id`         BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id`    BIGINT NOT NULL,
    `title`      VARCHAR(256) DEFAULT '' COMMENT '会话标题',
    `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='AI会话';

-- 18. 对话消息 chat_messages
DROP TABLE IF EXISTS `chat_messages`;
CREATE TABLE `chat_messages`
(
    `id`       BIGINT AUTO_INCREMENT PRIMARY KEY,
    `chat_id`  BIGINT                       NOT NULL,
    `msg_seq`  INT                          NOT NULL COMMENT '会话内序号',
    `msg_type` ENUM ('text','media','file') NOT NULL,
    `role`     ENUM ('user','ai','system')  NOT NULL,
    `message`  TEXT COMMENT '消息JSON/URL',
    `sent_at`  DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_chat (`chat_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='对话消息';

-- 19. 会话课程关联 chat_course_rel
DROP TABLE IF EXISTS `chat_course_rel`;
CREATE TABLE `chat_course_rel`
(
    `id`        BIGINT AUTO_INCREMENT PRIMARY KEY,
    `chat_id`   BIGINT NOT NULL,
    `course_id` BIGINT NOT NULL,
    `user_id`   BIGINT NOT NULL COMMENT '冗余学生ID',
    UNIQUE KEY uk_chat_course (`chat_id`),
    KEY idx_course (`course_id`),
    KEY idx_user (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='会话绑定课程学生';

-- 20. AI建议表 ai_suggestions
DROP TABLE IF EXISTS `ai_suggestions`;
CREATE TABLE `ai_suggestions`
(
    `id`               BIGINT AUTO_INCREMENT PRIMARY KEY,
    `receiver_id`      BIGINT                  NOT NULL COMMENT '接收人ID',
    `title`            VARCHAR(256) DEFAULT NULL,
    `message`          TEXT                    NOT NULL COMMENT 'Markdown正文',
    `display_type`     ENUM ('info','warning') NOT NULL,
    `display_location` VARCHAR(128)            NOT NULL COMMENT '展示位置标识',
    `created_at`       DATETIME     DEFAULT CURRENT_TIMESTAMP,
    KEY idx_receiver (`receiver_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='AI提示建议';

-- 21. AI引用表 ai_citations
DROP TABLE IF EXISTS `ai_citations`;
CREATE TABLE `ai_citations`
(
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY,
    `chat_message_id` BIGINT DEFAULT NULL,
    `suggestion_id`   BIGINT DEFAULT NULL,
    `title`           VARCHAR(256)                                                                                               NOT NULL,
    `data_type`       ENUM ('none', 'file','url','text_quote','student_record','student_progress','knowledge','question','exam') NOT NULL,
    `data`            TEXT COMMENT '引用数据JSON',
    KEY idx_msg (`chat_message_id`),
    KEY idx_sug (`suggestion_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='AI引用资源';

-- 22. 用户文件 user_files
DROP TABLE IF EXISTS `user_files`;
CREATE TABLE `user_files`
(
    `id`         BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id`    BIGINT                                     NOT NULL,
    `file_name`  VARCHAR(256)                               NOT NULL,
    `file_desc`  TEXT,
    `file_url`   VARCHAR(512)                               NOT NULL,
    `size`       BIGINT                                     NOT NULL COMMENT '字节大小',
    `type`       ENUM ('pdf','pptx','docx','media','other') NOT NULL,
    `source`     ENUM ('upload','generated')                NOT NULL COMMENT '来源：上传/AI生成',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_user (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户文件';

-- 23. 课件生成记录表 courseware_records
DROP TABLE IF EXISTS `courseware_records`;
CREATE TABLE `courseware_records`
(
    `id`           BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id`      BIGINT NOT NULL COMMENT '教师ID',
    `chat_id`      BIGINT NOT NULL COMMENT 'AI对话ID',
    `ppt_file_id`  BIGINT   DEFAULT NULL COMMENT 'PPT文件ID',
    `word_file_id` BIGINT   DEFAULT NULL COMMENT '教案文件ID',
    `ppt_html`     TEXT COMMENT 'PPT原始HTML',
    `word_html`    TEXT COMMENT '教案原始HTML',
    `created_at`   DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_user (`user_id`),
    KEY idx_chat (`chat_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='课件生成记录';

-- 24. 文件课件关联表 courseware_file_rel
DROP TABLE IF EXISTS `courseware_file_rel`;
CREATE TABLE `courseware_file_rel`
(
    `id`                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    `courseware_record_id` BIGINT NOT NULL,
    `file_id`              BIGINT NOT NULL,
    KEY idx_courseware (`courseware_record_id`),
    KEY idx_file (`file_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='课件引用文件关联';

-- ====================== 二、统计与分析数据表 ======================
-- 25. 教师课程分析表 teacher_course_analysises
DROP TABLE IF EXISTS `teacher_course_analysises`;
CREATE TABLE `teacher_course_analysises`
(
    `id`                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    `course_id`           BIGINT NOT NULL,
    `total_students`      INT    NOT NULL DEFAULT 0 COMMENT '总学生数',
    `avg_score_rate`      DECIMAL(5, 2)   DEFAULT 0.00 COMMENT '平均得分率',
    `avg_progress`        DECIMAL(5, 2)   DEFAULT 0.00 COMMENT '平均学习进度',
    `task_count`          INT    NOT NULL DEFAULT 0 COMMENT '任务总数',
    `submission_rate`     DECIMAL(5, 2)   DEFAULT 0.00 COMMENT '作业提交率',
    `opening_questions`   INT    NOT NULL DEFAULT 0 COMMENT '待处理提问数',
    `alert_student_count` INT    NOT NULL DEFAULT 0 COMMENT '预警学生数量',
    `weakest_knowledge`   VARCHAR(64)     DEFAULT NULL COMMENT '最薄弱知识点Neo4jID',
    `updated_at`          DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_course (`course_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='教师课程统计分析';

-- 26. 学生课程进度表 student_course_progresses
DROP TABLE IF EXISTS `student_course_progresses`;
CREATE TABLE `student_course_progresses`
(
    `id`                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    `student_id`           BIGINT NOT NULL,
    `course_id`            BIGINT NOT NULL,
    `total_progress`       DECIMAL(5, 2)   DEFAULT 0.00 COMMENT '课程总进度',
    `interaction_count`    INT    NOT NULL DEFAULT 0 COMMENT '互动次数',
    `avg_task_score`       DECIMAL(5, 2)   DEFAULT NULL COMMENT '平均作业分数',
    `knowledge_cover_rate` DECIMAL(5, 2)   DEFAULT 0.00 COMMENT '知识点覆盖率',
    `task_completion_rate` DECIMAL(5, 2)   DEFAULT 0.00 COMMENT '任务完成率',
    `updated_at`           DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_stu_course (`student_id`, `course_id`),
    KEY idx_student (`student_id`),
    KEY idx_course (`course_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='学生课程学习进度';

-- 27. 学生知识点掌握度 student_knowledge_masteries
DROP TABLE IF EXISTS `student_knowledge_masteries`;
CREATE TABLE `student_knowledge_masteries`
(
    `id`                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    `student_id`             BIGINT      NOT NULL,
    `course_id`              BIGINT               DEFAULT NULL,
    `knowledge_id`           VARCHAR(64) NOT NULL COMMENT 'Neo4j知识点ID',
    `master_rate`            DECIMAL(5, 2)        DEFAULT 0.00 COMMENT '掌握度百分比',
    `question_correct_count` INT         NOT NULL DEFAULT 0 COMMENT '答对次数',
    `question_total_count`   INT         NOT NULL DEFAULT 0 COMMENT '总答题次数',
    `updated_at`             DATETIME             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_stu_knowledge (`student_id`, `knowledge_id`),
    KEY idx_student (`student_id`),
    KEY idx_course (`course_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='学生知识点掌握情况';

-- 28. 学生能力基线 student_ability_baselines
DROP TABLE IF EXISTS `student_ability_baselines`;
CREATE TABLE `student_ability_baselines`
(
    `id`                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    `student_id`             BIGINT  NOT NULL,
    `difficulty_level`       TINYINT NOT NULL DEFAULT 1 COMMENT '最近发展区难度层级',
    `first_contact_accuracy` DECIMAL(5, 2)    DEFAULT 0.00 COMMENT '首次接触知识点正确率',
    `stability_rate`         DECIMAL(5, 2)    DEFAULT 0.00 COMMENT '多次学习稳定率',
    `updated_at`             DATETIME         DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_student (`student_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='学生能力基线';

-- 29. 学生学习态度 student_learning_attitudes
DROP TABLE IF EXISTS `student_learning_attitudes`;
CREATE TABLE `student_learning_attitudes`
(
    `id`                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    `student_id`         BIGINT NOT NULL,
    `kb_view_count`      INT    NOT NULL DEFAULT 0 COMMENT '知识库浏览次数',
    `error_review_count` INT    NOT NULL DEFAULT 0 COMMENT '错题复习次数',
    `interaction_count`  INT    NOT NULL DEFAULT 0 COMMENT 'AI交互提问次数',
    `guide_push_count`   INT    NOT NULL DEFAULT 0 COMMENT '系统引导推送次数',
    `guide_accept_count` INT    NOT NULL DEFAULT 0 COMMENT '接受引导次数',
    `attitude_score`     DECIMAL(5, 2)   DEFAULT 0.00 COMMENT '学习态度评分',
    `updated_at`         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_student (`student_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='学生学习态度统计';

-- 30. 学生求助密度 student_help_densities
DROP TABLE IF EXISTS `student_help_densities`;
CREATE TABLE `student_help_densities`
(
    `id`                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    `student_id`           BIGINT  NOT NULL,
    `top_difficulty_level` TINYINT NOT NULL DEFAULT 1 COMMENT '高频求助难度层级',
    `help_count`           INT     NOT NULL DEFAULT 0 COMMENT '总求助次数',
    `top_help_type`        VARCHAR(64)      DEFAULT NULL COMMENT '高频求助类型',
    `updated_at`           DATETIME         DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_student (`student_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='学生求助密度分析';

-- 31. 学生交互风格 student_interaction_styles
DROP TABLE IF EXISTS `student_interaction_styles`;
CREATE TABLE `student_interaction_styles`
(
    `id`                        BIGINT AUTO_INCREMENT PRIMARY KEY,
    `student_id`                BIGINT NOT NULL,
    `avg_text_length`           INT    NOT NULL DEFAULT 0 COMMENT '平均发送文本长度',
    `seriousness_score`         DECIMAL(5, 2)   DEFAULT 0.00 COMMENT '认真礼貌得分',
    `direct_answer_count`       INT    NOT NULL DEFAULT 0 COMMENT '直接求答案次数',
    `concept_inquiry_count`     INT    NOT NULL DEFAULT 0 COMMENT '概念提问次数',
    `confirmation_count`        INT    NOT NULL DEFAULT 0 COMMENT '确认提问次数',
    `other_query_count`         INT    NOT NULL DEFAULT 0 COMMENT '其他提问次数',
    `emotion_positive_count`    INT    NOT NULL DEFAULT 0 COMMENT '积极情绪提问次数',
    `emotion_neutral_count`     INT    NOT NULL DEFAULT 0 COMMENT '中性情绪提问次数',
    `emotion_negative_count`    INT    NOT NULL DEFAULT 0 COMMENT '消极情绪提问次数',
    `total_interactions`        INT    NOT NULL DEFAULT 0 COMMENT '总交互次数',
    `student_interaction_style` TEXT COMMENT 'AI评估交互风格描述',
    `preferred_ai_style`        TEXT COMMENT '推荐AI交互风格',
    `updated_at`                DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_student (`student_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='学生交互风格画像';

-- 32. 任务分析表 mission_analysises
DROP TABLE IF EXISTS `mission_analysises`;
CREATE TABLE `mission_analysises`
(
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY,
    `task_id`         BIGINT NOT NULL,
    `total_submitted` INT    NOT NULL DEFAULT 0 COMMENT '提交人数',
    `avg_score`       DECIMAL(5, 2)   DEFAULT NULL COMMENT '平均分',
    `max_score`       INT             DEFAULT NULL COMMENT '最高分',
    `min_score`       INT             DEFAULT NULL COMMENT '最低分',
    `avg_time_spent`  INT    NOT NULL DEFAULT 0 COMMENT '平均耗时(秒)',
    `updated_at`      DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_task (`task_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='任务整体统计分析';

-- 33. 题目属性表 question_properties
DROP TABLE IF EXISTS `question_properties`;
CREATE TABLE `question_properties`
(
    `id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
    `question_id` BIGINT                        NOT NULL,
    `analysis`    TEXT COMMENT '题目解析',
    `source`      ENUM ('ai','manual','import') NOT NULL COMMENT '题目来源',
    `difficulty`  ENUM ('easy','medium','hard') NOT NULL COMMENT '难度',
    `updated_at`  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_question (`question_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='题目扩展属性';

-- 34. 题目知识点关联 question_knowledge_rel
DROP TABLE IF EXISTS `question_knowledge_rel`;
CREATE TABLE `question_knowledge_rel`
(
    `id`             BIGINT AUTO_INCREMENT PRIMARY KEY,
    `question_id`    BIGINT      NOT NULL,
    `knowledge_node` VARCHAR(64) NOT NULL COMMENT 'Neo4j知识点ID',
    `sort`           INT         NOT NULL DEFAULT 0 COMMENT '重要程度排序',
    KEY idx_question (`question_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='题目关联知识点';

-- 35. 题目学情分析 teacher_question_analysises
DROP TABLE IF EXISTS `teacher_question_analysises`;
CREATE TABLE `teacher_question_analysises`
(
    `id`                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    `question_id`            BIGINT NOT NULL,
    `student_count`          INT    NOT NULL DEFAULT 0 COMMENT '作答总人数',
    `avg_correct_rate`       DECIMAL(5, 2)   DEFAULT 0.00 COMMENT '平均正确率',
    `avg_time_spent`         INT    NOT NULL DEFAULT 0 COMMENT '平均耗时',
    `abandon_rate`           DECIMAL(5, 2)   DEFAULT 0.00 COMMENT '弃题率',
    `avg_help_request_count` DECIMAL(5, 2)   DEFAULT 0.00 COMMENT '平均求助次数',
    `ai_quality_rating`      DECIMAL(5, 2)   DEFAULT NULL COMMENT 'AI题目质量评分',
    `ai_difficulty_rating`   DECIMAL(5, 2)   DEFAULT NULL COMMENT 'AI预估难度分',
    `top_error_type`         VARCHAR(64)     DEFAULT NULL COMMENT '高频错误类型',
    `updated_at`             DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_question (`question_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='题目学情分析统计';

-- 36. 会话 课件生成记录 关联表 chat_courseware_rec_rel
DROP TABLE IF EXISTS `chat_courseware_rec_rel`;
CREATE TABLE `chat_courseware_rec_rel`
(
    `id`                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    `chat_id`              BIGINT NOT NULL COMMENT '会话ID',
    `courseware_record_id` BIGINT NOT NULL COMMENT '课件生成记录ID',
    `user_id`              BIGINT NOT NULL COMMENT '教师ID',
    UNIQUE KEY uk_chat (`chat_id`),
    UNIQUE KEY uk_courseware_record (`courseware_record_id`) COMMENT '一个课件记录只关联一个会话',
    KEY idx_user (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='会话-课件生成记录关联表';

-- 37. 课程数据表 course_data
DROP TABLE IF EXISTS course_data;
CREATE TABLE IF NOT EXISTS `course_data`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`       VARCHAR(255) NOT NULL COMMENT '课程名称',
    `info`       TEXT COMMENT '课程的教材等信息',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='课程数据表';

-- 38. 学习路径节点 任务 关联表 path_node_task_rel
DROP TABLE IF EXISTS path_node_task_rel;
CREATE TABLE `path_node_task_rel`
(
    `id`           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `student_id`   BIGINT NOT NULL COMMENT '学生ID',
    `path_node_id` BIGINT NOT NULL COMMENT '路径节点ID',
    `task_id`      BIGINT NOT NULL COMMENT '任务ID',
    `create_at`    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_path_node (`path_node_id`),
    KEY idx_task (`task_id`),
    UNIQUE KEY uk_path_node_task (`path_node_id`, `task_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='路径节点任务关联表';