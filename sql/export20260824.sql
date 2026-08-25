-- MySQL dump 10.13  Distrib 8.0.46, for Linux (x86_64)
--
-- 备份的导出文件
-- Host: localhost    Database: edu_ai
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT = @@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS = @@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION = @@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE = @@TIME_ZONE */;
/*!40103 SET TIME_ZONE = '+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS = @@UNIQUE_CHECKS, UNIQUE_CHECKS = 0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0 */;
/*!40101 SET @OLD_SQL_MODE = @@SQL_MODE, SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES = @@SQL_NOTES, SQL_NOTES = 0 */;

--
-- Table structure for table `ai_citations`
--

DROP TABLE IF EXISTS `ai_citations`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_citations`
(
    `id`              bigint                                                                                             NOT NULL AUTO_INCREMENT,
    `chat_message_id` bigint DEFAULT NULL,
    `suggestion_id`   bigint DEFAULT NULL,
    `title`           varchar(256)                                                                                       NOT NULL,
    `data_type`       enum ('file','url','text_quote','student_record','student_progress','knowledge','exercise','exam') NOT NULL,
    `data`            text COMMENT '引用数据JSON',
    PRIMARY KEY (`id`),
    KEY `idx_msg` (`chat_message_id`),
    KEY `idx_sug` (`suggestion_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 3
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='AI引用资源';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_citations`
--

LOCK TABLES `ai_citations` WRITE;
/*!40000 ALTER TABLE `ai_citations`
    DISABLE KEYS */;
INSERT INTO `ai_citations`
VALUES (1, 45, NULL, '知识点: 导数', 'knowledge',
        '{\"name\": \"导数\", \"description\": \"函数在某点的瞬时变化率，也是切线斜率\"}'),
       (2, 77, NULL, '知识点: 导数', 'knowledge',
        '{\"name\": \"导数\", \"description\": \"函数在某点的瞬时变化率，也是切线斜率\"}');
/*!40000 ALTER TABLE `ai_citations`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_suggestions`
--

DROP TABLE IF EXISTS `ai_suggestions`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_suggestions`
(
    `id`               bigint                  NOT NULL AUTO_INCREMENT,
    `receiver_id`      bigint                  NOT NULL COMMENT '接收人ID',
    `title`            varchar(256) DEFAULT NULL,
    `message`          text                    NOT NULL COMMENT 'Markdown正文',
    `display_type`     enum ('info','warning') NOT NULL,
    `display_location` varchar(128)            NOT NULL COMMENT '展示位置标识',
    `created_at`       datetime     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_receiver` (`receiver_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='AI提示建议';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_suggestions`
--

LOCK TABLES `ai_suggestions` WRITE;
/*!40000 ALTER TABLE `ai_suggestions`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_suggestions`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chapters`
--

DROP TABLE IF EXISTS `chapters`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chapters`
(
    `id`             bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `course_data_id` bigint       NOT NULL COMMENT '关联课程数据id',
    `name`           varchar(128) NOT NULL COMMENT '章节名称',
    `summary`        text COMMENT '章节简介',
    `sort_order`     int          NOT NULL DEFAULT '0' COMMENT '排序',
    `created_at`     datetime              DEFAULT CURRENT_TIMESTAMP,
    `updated_at`     datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_course` (`course_data_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 9
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='章表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chapters`
--

LOCK TABLES `chapters` WRITE;
/*!40000 ALTER TABLE `chapters`
    DISABLE KEYS */;
INSERT INTO `chapters`
VALUES (2, 2, '函数与极限', NULL, 1, NULL, NULL),
       (3, 2, '导数与微分', NULL, 2, NULL, NULL),
       (4, 2, '微分中值定理与导数的应用', NULL, 3, NULL, NULL),
       (5, 2, '不定积分', NULL, 4, NULL, NULL),
       (6, 2, '定积分', NULL, 5, NULL, NULL),
       (7, 2, '定积分的应用', NULL, 6, NULL, NULL),
       (8, 2, '微分方程', NULL, 7, NULL, NULL);
/*!40000 ALTER TABLE `chapters`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_course_rel`
--

DROP TABLE IF EXISTS `chat_course_rel`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_course_rel`
(
    `id`        bigint NOT NULL AUTO_INCREMENT,
    `chat_id`   bigint NOT NULL,
    `course_id` bigint NOT NULL,
    `user_id`   bigint NOT NULL COMMENT '冗余学生ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_chat_course` (`chat_id`),
    KEY `idx_course` (`course_id`),
    KEY `idx_user` (`user_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 4
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='会话绑定课程学生';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_course_rel`
--

LOCK TABLES `chat_course_rel` WRITE;
/*!40000 ALTER TABLE `chat_course_rel`
    DISABLE KEYS */;
INSERT INTO `chat_course_rel`
VALUES (3, 13, 1, 3);
/*!40000 ALTER TABLE `chat_course_rel`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_courseware_rec_rel`
--

DROP TABLE IF EXISTS `chat_courseware_rec_rel`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_courseware_rec_rel`
(
    `id`                   bigint NOT NULL AUTO_INCREMENT,
    `chat_id`              bigint NOT NULL COMMENT '会话ID',
    `courseware_record_id` bigint NOT NULL COMMENT '课件生成记录ID',
    `user_id`              bigint NOT NULL COMMENT '教师ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_chat` (`chat_id`),
    UNIQUE KEY `uk_courseware_record` (`courseware_record_id`) COMMENT '一个课件记录只关联一个会话',
    KEY `idx_user` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='会话-课件生成记录关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_courseware_rec_rel`
--

LOCK TABLES `chat_courseware_rec_rel` WRITE;
/*!40000 ALTER TABLE `chat_courseware_rec_rel`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `chat_courseware_rec_rel`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_messages`
--

DROP TABLE IF EXISTS `chat_messages`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_messages`
(
    `id`       bigint                       NOT NULL AUTO_INCREMENT,
    `chat_id`  bigint                       NOT NULL,
    `msg_seq`  int                          NOT NULL COMMENT '会话内序号',
    `msg_type` enum ('text','media','file') NOT NULL,
    `role`     enum ('user','ai','system')  NOT NULL,
    `message`  text COMMENT '消息JSON/URL',
    `sent_at`  datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_chat` (`chat_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 108
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='对话消息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_messages`
--

LOCK TABLES `chat_messages` WRITE;
/*!40000 ALTER TABLE `chat_messages`
    DISABLE KEYS */;
INSERT INTO `chat_messages`
VALUES (78, 12, 1, 'text', 'user', '请调用test方法', '2026-08-23 14:09:26'),
       (79, 12, 2, 'text', 'system', '{\"type\":\"task\",\"data\":\"test message\"}', '2026-08-23 14:09:27'),
       (80, 12, 3, 'text', 'ai',
        'test 方法已成功调用。请问您接下来需要我协助完成什么任务？例如：\n\n1. 获取课程列表或分析特定课程\n2. 布置作业或批改任务\n3. 生成课件或教案\n4. 查询知识点或进行教学分析',
        '2026-08-23 14:09:27'),
       (81, 12, 4, 'text', 'user', '请调用test方法', '2026-08-23 14:10:38'),
       (82, 12, 5, 'text', 'ai',
        'test 方法已成功调用。请问您接下来需要我协助完成什么任务？例如：\n\n1. 获取课程列表或分析特定课程\n2. 布置作业或批改任务\n3. 生成课件或教案\n4. 查询知识点或进行教学分析',
        '2026-08-23 14:10:39'),
       (83, 12, 6, 'text', 'user', '请调用test方法', '2026-08-23 14:11:55'),
       (84, 12, 7, 'text', 'system', '{\"type\":\"task\",\"data\":\"test message\"}', '2026-08-23 14:11:57'),
       (85, 12, 8, 'text', 'ai',
        '测试方法已成功调用并返回结果 \"Done\"。\n\n请问您还需要我协助进行其他操作吗？例如：\n1. 获取课程列表或分析特定课程数据\n2. 布置新的教学任务\n3. 生成课件或教案',
        '2026-08-23 14:12:00'),
       (86, 12, 9, 'text', 'user', '请调用test方法', '2026-08-23 14:12:18'),
       (87, 12, 10, 'text', 'system', '{\"type\":\"task\",\"data\":\"test message\"}', '2026-08-23 14:12:21'),
       (88, 12, 11, 'text', 'ai',
        'test方法已成功调用并返回结果。\n\n需要我帮您进行其他操作吗？例如：\n- 获取课程列表或分析特定课程\n- 布置作业任务\n- 生成课件',
        '2026-08-23 14:12:23'),
       (89, 12, 12, 'text', 'user', '请调用test方法', '2026-08-23 14:13:32'),
       (90, 12, 13, 'text', 'system', '{\"type\":\"task\",\"data\":\"test message\"}', '2026-08-23 14:13:34'),
       (91, 12, 14, 'text', 'ai',
        'test 方法已成功调用并返回 \"Done\"。\n\n请问您接下来需要我协助处理什么任务？例如：\n1. 获取课程列表或分析特定课程\n2. 布置作业或批改任务\n3. 生成课件或教案',
        '2026-08-23 14:13:36'),
       (92, 12, 15, 'text', 'user', '请调用test方法', '2026-08-23 14:18:22'),
       (93, 12, 16, 'text', 'system', '{\"type\":\"task\",\"data\":\"test message\"}', '2026-08-23 14:18:24'),
       (94, 12, 17, 'text', 'ai',
        'test方法调用成功。\n\n您接下来需要我协助处理什么教学工作吗？例如：\n1. 生成课件或教案\n2. 布置作业或考试任务\n3. 分析课程或学生数据',
        '2026-08-23 14:18:25'),
       (95, 12, 18, 'text', 'user', '请调用test方法', '2026-08-23 14:18:45'),
       (96, 12, 19, 'text', 'system', '{\"type\":\"task\",\"data\":\"test message\"}', '2026-08-23 14:18:48'),
       (97, 12, 20, 'text', 'ai',
        '测试方法已成功调用并返回结果 \"Done\"。\n\n如果您有其他需求，例如生成课件、布置任务或进行教学分析，请随时告诉我！',
        '2026-08-23 14:18:49'),
       (98, 13, 1, 'text', 'user', '你好', '2026-08-24 14:12:33'),
       (99, 13, 2, 'text', 'ai',
        '你好！很高兴能作为你的AI导师为你提供帮助。\n\n无论你是遇到了**难懂的知识点**，还是想进行**针对性的练习**，亦或是需要我帮你梳理**学习路径**，都可以随时告诉我。\n\n我们可以从以下几个方向开始：\n1. **知识讲解**：比如“能不能帮我解释一下勾股定理？”或“导数的几何意义是什么？”\n2. **题目解析**：你可以把不会做的题发给我，我会引导你一步步思考。\n3. **学习规划**：如果你正在学习某个特定章节，我可以帮你生成相关的练习题或总结重点。\n\n你想先聊聊哪方面的内容呢？',
        '2026-08-24 14:12:35'),
       (100, 13, 3, 'text', 'user', '什么是虚拟内存？', '2026-08-24 14:13:21'),
       (101, 13, 4, 'text', 'ai',
        '你好！很高兴为你解答这个问题。\n\n**虚拟内存（Virtual Memory）** 是现代操作系统中一项非常核心的技术。简单来说，它允许计算机使用**硬盘空间**来扩展**物理内存（RAM）**的容量。\n\n为了让你更轻松地理解，我们可以用一个生活中的例子来类比：\n\n### 🏠 生活类比：厨房与储藏室\n想象你在做一个复杂的料理（运行大型程序）：\n*   **物理内存（RAM）** 就像你的**厨房操作台**。它的面积有限，但取用食材非常快，效率高。\n*   **硬盘** 就像旁边的**大储藏室**。容量很大，可以存放很多东西，但去那里拿东西比较慢。\n\n如果没有虚拟内存，当操作台上放满了食材（内存满了），你就没法再处理新的菜了，哪怕储藏室里还有空间。\n\n有了**虚拟内存**，系统会像一个聪明的助手：\n1.  把暂时不用的食材（不常用的数据）从操作台搬到储藏室里暂存。\n2.  腾出操作台的空间给正在急需处理的食材。\n3.  当你需要之前搬走的数据时，它再快速取回来放回操作台。\n\n这样，虽然速度比纯在操作台上慢一点（因为要往返储藏室），但你可以在有限的操作台上同时处理更多的事情，感觉就像拥有了一个无限大的操作台。\n\n---\n\n### 💻 技术层面的核心概念\n\n在实际的计算机科学中，虚拟内存主要实现了以下几个功能：\n\n1.  **扩展内存容量**：\n    这是最直接的作用。即使你的电脑只有 8GB 的物理内存，通过设置几 GB 的虚拟内存文件（如 Windows 的 pagefile.sys 或 macOS 的 swapfile），你可以运行那些总需求超过 8GB 的大型软件。\n\n2.  **内存隔离与安全**：\n    每个程序都认为自己拥有整个连续的内存空间（虚拟地址空间）。实际上，这些空间在物理内存中是分散的。这防止了一个程序错误地修改另一个程序的内存，提高了系统的稳定性。\n\n3.  **简化编程**：\n    程序员不需要手动管理物理内存的地址分配，操作系统会自动处理“哪些数据该留在内存，哪些该换出到硬盘”的问题。\n\n### ⚖️ 优缺点分析\n\n| 优点 | 缺点 |\n| :--- | :--- |\n| **多任务处理**：可以同时运行更多、更大的程序。 | **速度变慢**：硬盘的速度远慢于内存。如果频繁地在内存和硬盘之间交换数据（称为“抖动”或 Paging Thrashing），电脑会变得非常卡顿。 |\n| **利用率高**：不必为每个程序预留固定的物理内存块。 | **磨损硬盘**：频繁的读写会加速固态硬盘（SSD）或机械硬盘（HDD）的损耗（虽然现代 SSD 寿命已很长）。 |\n\n### 💡 小贴士\n如果你的电脑经常提示“内存不足”，或者打开几个程序后就变得非常卡，通常意味着你的**物理内存太小**，导致系统不得不大量使用虚拟内存。这时候，**增加物理内存条（RAM）** 是比扩大虚拟内存文件大小更有效的解决办法。\n\n你对这个概念还有什么疑问吗？比如想了解它是如何具体工作的，或者想知道怎么查看自己电脑的虚拟内存状态？',
        '2026-08-24 14:13:28'),
       (102, 13, 5, 'text', 'user', 'hi', '2026-08-24 14:35:18'),
       (103, 13, 6, 'text', 'ai', '你好！有什么我可以帮你的吗？😊', '2026-08-24 14:35:18'),
       (104, 13, 7, 'text', 'user', 'hi', '2026-08-24 14:36:29'),
       (105, 13, 8, 'text', 'ai',
        '你好！很高兴再次见到你。\n\n今天有什么具体的问题想讨论，或者需要我协助讲解某个知识点吗？随时告诉我！',
        '2026-08-24 14:36:30'),
       (106, 13, 9, 'text', 'user', '你好！', '2026-08-24 14:40:43'),
       (107, 13, 10, 'text', 'ai',
        '你好！很高兴能继续为你提供帮助。\n\n无论是学习上的疑问，还是想探讨某个具体的知识点，都可以随时告诉我。你想聊点什么？',
        '2026-08-24 14:40:44');
/*!40000 ALTER TABLE `chat_messages`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_session`
--

DROP TABLE IF EXISTS `chat_session`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_session`
(
    `id`         bigint NOT NULL AUTO_INCREMENT,
    `user_id`    bigint NOT NULL,
    `title`      varchar(256) DEFAULT '' COMMENT '会话标题',
    `created_at` datetime     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 14
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='AI会话';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_session`
--

LOCK TABLES `chat_session` WRITE;
/*!40000 ALTER TABLE `chat_session`
    DISABLE KEYS */;
INSERT INTO `chat_session`
VALUES (12, 1, '', NULL),
       (13, 3, '', NULL);
/*!40000 ALTER TABLE `chat_session`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `class_student_rel`
--

DROP TABLE IF EXISTS `class_student_rel`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `class_student_rel`
(
    `id`         bigint NOT NULL AUTO_INCREMENT,
    `class_id`   bigint NOT NULL,
    `student_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_class_stu` (`class_id`, `student_id`),
    KEY `idx_class` (`class_id`),
    KEY `idx_student` (`student_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='班级学生关联';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `class_student_rel`
--

LOCK TABLES `class_student_rel` WRITE;
/*!40000 ALTER TABLE `class_student_rel`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `class_student_rel`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `classes`
--

DROP TABLE IF EXISTS `classes`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `classes`
(
    `id`         bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`       varchar(64) NOT NULL COMMENT '班级名称',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 2
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='班级表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `classes`
--

LOCK TABLES `classes` WRITE;
/*!40000 ALTER TABLE `classes`
    DISABLE KEYS */;
INSERT INTO `classes`
VALUES (1, '计算机科学与技术2024级1班', '2026-08-15 05:29:54', '2026-08-15 05:29:54');
/*!40000 ALTER TABLE `classes`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_data`
--

DROP TABLE IF EXISTS `course_data`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_data`
(
    `id`         bigint                                  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`       varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程名称',
    `info`       text COLLATE utf8mb4_unicode_ci COMMENT '课程的教材等信息',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 3
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='课程数据表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_data`
--

LOCK TABLES `course_data` WRITE;
/*!40000 ALTER TABLE `course_data`
    DISABLE KEYS */;
INSERT INTO `course_data`
VALUES (1, '高等数学',
        '{\"publisher\": \"同济大学出版社\", \"authors\": [\"同济大学数学系\"], \"edition\": \"第7版\", \"chapters\": [\"函数与极限\", \"导数与微分\", \"微分中值定理与导数的应用\", \"不定积分\", \"定积分\", \"定积分的应用\", \"微分方程\"]}',
        '2026-08-15 05:29:54');
/*!40000 ALTER TABLE `course_data`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_student_rel`
--

DROP TABLE IF EXISTS `course_student_rel`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_student_rel`
(
    `id`         bigint NOT NULL AUTO_INCREMENT,
    `course_id`  bigint NOT NULL,
    `student_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_course_stu` (`course_id`, `student_id`),
    KEY `idx_course` (`course_id`),
    KEY `idx_student` (`student_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 2
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='课程学生关联';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_student_rel`
--

LOCK TABLES `course_student_rel` WRITE;
/*!40000 ALTER TABLE `course_student_rel`
    DISABLE KEYS */;
INSERT INTO `course_student_rel`
VALUES (1, 1, 3);
/*!40000 ALTER TABLE `course_student_rel`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `courses`
--

DROP TABLE IF EXISTS `courses`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `courses`
(
    `id`             bigint                      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `course_data_id` bigint                      NOT NULL COMMENT '关联课程数据ID',
    `name`           varchar(128)                NOT NULL COMMENT '课程名称',
    `description`    text COMMENT '课程描述',
    `cover_url`      varchar(512)                         DEFAULT '' COMMENT '封面图',
    `credit`         tinyint                     NOT NULL DEFAULT '0' COMMENT '学分',
    `status`         enum ('opening','archived') NOT NULL COMMENT '课程状态：上课中/已结课',
    `teacher_id`     bigint                      NOT NULL COMMENT '任课教师ID',
    `created_at`     datetime                             DEFAULT CURRENT_TIMESTAMP,
    `updated_at`     datetime                             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_teacher` (`teacher_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 3
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='课程表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `courses`
--

LOCK TABLES `courses` WRITE;
/*!40000 ALTER TABLE `courses`
    DISABLE KEYS */;
INSERT INTO `courses`
VALUES (1, 1, '高等数学', '同济大学出版社', 'https://example.com/images/course_java.jpg', 3, 'opening', 1,
        '2026-08-15 05:42:32', '2026-08-24 08:53:09');
/*!40000 ALTER TABLE `courses`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `courseware_file_rel`
--

DROP TABLE IF EXISTS `courseware_file_rel`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `courseware_file_rel`
(
    `id`                   bigint NOT NULL AUTO_INCREMENT,
    `courseware_record_id` bigint NOT NULL,
    `file_id`              bigint NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_courseware` (`courseware_record_id`),
    KEY `idx_file` (`file_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='课件引用文件关联';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `courseware_file_rel`
--

LOCK TABLES `courseware_file_rel` WRITE;
/*!40000 ALTER TABLE `courseware_file_rel`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `courseware_file_rel`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `courseware_records`
--

DROP TABLE IF EXISTS `courseware_records`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `courseware_records`
(
    `id`           bigint NOT NULL AUTO_INCREMENT,
    `user_id`      bigint NOT NULL COMMENT '教师ID',
    `chat_id`      bigint NOT NULL COMMENT 'AI对话ID',
    `ppt_file_id`  bigint   DEFAULT NULL COMMENT 'PPT文件ID',
    `word_file_id` bigint   DEFAULT NULL COMMENT '教案文件ID',
    `ppt_html`     text COMMENT 'PPT原始HTML',
    `word_html`    text COMMENT '教案原始HTML',
    `created_at`   datetime DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_chat` (`chat_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='课件生成记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `courseware_records`
--

LOCK TABLES `courseware_records` WRITE;
/*!40000 ALTER TABLE `courseware_records`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `courseware_records`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `learning_path_nodes`
--

DROP TABLE IF EXISTS `learning_path_nodes`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `learning_path_nodes`
(
    `id`              bigint                                     NOT NULL AUTO_INCREMENT,
    `node_name`       varchar(128)                               NOT NULL COMMENT '节点名称',
    `path_id`         bigint                                     NOT NULL COMMENT '学习路径id',
    `knowledge_ids`   text                                       NOT NULL COMMENT '知识点ID列表(JSON数组)',
    `status`          enum ('untested','proceeding','completed') NOT NULL COMMENT '未进行/进行中/完成',
    `related_task_id` bigint   DEFAULT NULL COMMENT '关联作业ID',
    `created_at`      datetime DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_path` (`path_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 11
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='学习路径节点';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `learning_path_nodes`
--

LOCK TABLES `learning_path_nodes` WRITE;
/*!40000 ALTER TABLE `learning_path_nodes`
    DISABLE KEYS */;
INSERT INTO `learning_path_nodes`
VALUES (1, '函数基础与连续性', 1, '[\"函数\",\"初等函数\",\"连续\",\"连续函数运算\",\"间断点\"]', 'untested', NULL,
        NULL, NULL),
       (2, '数列与函数极限', 1, '[\"数列极限\",\"函数极限\",\"无穷大\",\"无穷小\",\"极限运算法则\"]', 'untested', NULL,
        NULL, NULL),
       (3, '重要极限与存在性定理', 1, '[\"重要极限\",\"夹逼准则\",\"介值定理\",\"有界性与最值定理\"]', 'untested', NULL,
        NULL, NULL),
       (4, '导数概念与计算法则', 1, '[\"导数\",\"微分\",\"求导法则\",\"洛必达法则\",\"高阶导数\"]', 'untested', NULL,
        NULL, NULL),
       (5, '特殊函数求导', 1, '[\"参数方程求导\",\"隐函数求导\"]', 'untested', NULL, NULL, NULL),
       (6, '导数的应用：单调性与极值', 1, '[\"函数单调性\",\"函数极值\",\"曲线凹凸性\",\"曲率\"]', 'untested', NULL,
        NULL, NULL),
       (7, '微分中值定理', 1, '[\"罗尔定理\",\"拉格朗日中值定理\"]', 'untested', NULL, NULL, NULL),
       (8, '不定积分与原函数', 1, '[\"原函数\",\"不定积分\",\"换元积分法\",\"分部积分法\",\"有理函数积分\"]',
        'untested', NULL, NULL, NULL),
       (9, '定积分与牛顿-莱布尼茨公式', 1, '[\"定积分\",\"牛顿-莱布尼茨公式\"]', 'untested', NULL, NULL, NULL),
       (10, '泰勒公式', 1, '[\"泰勒公式\"]', 'untested', NULL, NULL, NULL);
/*!40000 ALTER TABLE `learning_path_nodes`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `learning_paths`
--

DROP TABLE IF EXISTS `learning_paths`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `learning_paths`
(
    `id`              bigint NOT NULL AUTO_INCREMENT,
    `student_id`      bigint NOT NULL COMMENT '学生ID',
    `course_id`       bigint NOT NULL COMMENT '课程ID',
    `current_node_id` bigint                                                 DEFAULT NULL COMMENT '当前正在进行的learning_path_node_id，NULL表示未开始',
    `current_stage`   enum ('introducing','explaining','testing','finished') DEFAULT NULL COMMENT '当前教学阶段：introducing-引入, explaining-讲解, testing-测验, finished-结束',
    `additional_info` text COMMENT 'AI附加描述',
    `created_at`      datetime                                               DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      datetime                                               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_stu_course` (`student_id`, `course_id`),
    KEY `idx_student` (`student_id`),
    KEY `idx_course` (`course_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 2
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='学习路径';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `learning_paths`
--

LOCK TABLES `learning_paths` WRITE;
/*!40000 ALTER TABLE `learning_paths`
    DISABLE KEYS */;
INSERT INTO `learning_paths`
VALUES (1, 3, 1, NULL, 'introducing',
        '该路径从函数基础出发，依次涵盖极限理论、导数及其应用、积分学及微分中值定理，最后延伸至高阶与复杂运算，构建完整的微积分知识体系。',
        NULL, NULL);
/*!40000 ALTER TABLE `learning_paths`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mission_analysises`
--

DROP TABLE IF EXISTS `mission_analysises`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mission_analysises`
(
    `id`              bigint NOT NULL AUTO_INCREMENT,
    `task_id`         bigint NOT NULL,
    `total_submitted` int    NOT NULL DEFAULT '0' COMMENT '提交人数',
    `avg_score`       decimal(5, 2)   DEFAULT NULL COMMENT '平均分',
    `max_score`       int             DEFAULT NULL COMMENT '最高分',
    `min_score`       int             DEFAULT NULL COMMENT '最低分',
    `avg_time_spent`  int    NOT NULL DEFAULT '0' COMMENT '平均耗时(秒)',
    `updated_at`      datetime        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task` (`task_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='任务整体统计分析';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mission_analysises`
--

LOCK TABLES `mission_analysises` WRITE;
/*!40000 ALTER TABLE `mission_analysises`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `mission_analysises`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `path_node_task_rel`
--

DROP TABLE IF EXISTS `path_node_task_rel`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `path_node_task_rel`
(
    `id`           bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `student_id`   bigint NOT NULL COMMENT '学生ID',
    `path_node_id` bigint NOT NULL COMMENT '路径节点ID',
    `task_id`      bigint NOT NULL COMMENT '任务ID',
    `create_at`    datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_path_node_task` (`path_node_id`, `task_id`),
    KEY `idx_path_node` (`path_node_id`),
    KEY `idx_task` (`task_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='路径节点任务关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `path_node_task_rel`
--

LOCK TABLES `path_node_task_rel` WRITE;
/*!40000 ALTER TABLE `path_node_task_rel`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `path_node_task_rel`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `question_knowledge_rel`
--

DROP TABLE IF EXISTS `question_knowledge_rel`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `question_knowledge_rel`
(
    `id`             bigint      NOT NULL AUTO_INCREMENT,
    `question_id`    bigint      NOT NULL,
    `knowledge_node` varchar(64) NOT NULL COMMENT 'Neo4j知识点ID',
    `sort`           int         NOT NULL DEFAULT '0' COMMENT '重要程度排序',
    PRIMARY KEY (`id`),
    KEY `idx_question` (`question_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='题目关联知识点';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question_knowledge_rel`
--

LOCK TABLES `question_knowledge_rel` WRITE;
/*!40000 ALTER TABLE `question_knowledge_rel`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `question_knowledge_rel`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `question_properties`
--

DROP TABLE IF EXISTS `question_properties`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `question_properties`
(
    `id`          bigint                        NOT NULL AUTO_INCREMENT,
    `question_id` bigint                        NOT NULL,
    `analysis`    text COMMENT '题目解析',
    `source`      enum ('ai','manual','import') NOT NULL COMMENT '题目来源',
    `difficulty`  enum ('easy','medium','hard') NOT NULL COMMENT '难度',
    `updated_at`  datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_question` (`question_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='题目扩展属性';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question_properties`
--

LOCK TABLES `question_properties` WRITE;
/*!40000 ALTER TABLE `question_properties`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `question_properties`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `questions`
--

DROP TABLE IF EXISTS `questions`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `questions`
(
    `id`         bigint                                           NOT NULL AUTO_INCREMENT,
    `course_id`  bigint                                           NOT NULL,
    `teacher_id` bigint                                           NOT NULL,
    `type`       enum ('choice','fill','subjective','true_false') NOT NULL COMMENT '题型',
    `content`    text                                             NOT NULL COMMENT '题干',
    `answer`     text COMMENT '答案JSON',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_course` (`course_id`),
    KEY `idx_teacher` (`teacher_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='题目';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `questions`
--

LOCK TABLES `questions` WRITE;
/*!40000 ALTER TABLE `questions`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `questions`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `section_knowledge_rel`
--

DROP TABLE IF EXISTS `section_knowledge_rel`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `section_knowledge_rel`
(
    `id`             bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `course_data_id` bigint      NOT NULL COMMENT '课程数据ID，冗余',
    `section_id`     bigint      NOT NULL COMMENT '小节id',
    `neo4j_kp_id`    varchar(64) NOT NULL COMMENT 'Neo4j知识点唯一id',
    `created_at`     datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_section` (`section_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 51
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='小节关联知识点';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `section_knowledge_rel`
--

LOCK TABLES `section_knowledge_rel` WRITE;
/*!40000 ALTER TABLE `section_knowledge_rel`
    DISABLE KEYS */;
INSERT INTO `section_knowledge_rel`
VALUES (1, 1, 2, '函数', NULL),
       (2, 1, 2, '初等函数', NULL),
       (3, 1, 3, '数列极限', NULL),
       (4, 1, 4, '函数极限', NULL),
       (5, 1, 4, '无穷小', NULL),
       (6, 1, 5, '无穷大', NULL),
       (7, 1, 6, '极限运算法则', NULL),
       (8, 1, 7, '夹逼准则', NULL),
       (9, 1, 7, '重要极限', NULL),
       (10, 1, 8, '连续', NULL),
       (11, 1, 8, '间断点', NULL),
       (12, 1, 9, '连续函数运算', NULL),
       (13, 1, 10, '有界性与最值定理', NULL),
       (14, 1, 10, '介值定理', NULL),
       (15, 1, 11, '导数', NULL),
       (16, 1, 12, '求导法则', NULL),
       (17, 1, 13, '高阶导数', NULL),
       (18, 1, 14, '隐函数求导', NULL),
       (19, 1, 14, '参数方程求导', NULL),
       (20, 1, 15, '微分', NULL),
       (21, 1, 16, '罗尔定理', NULL),
       (22, 1, 16, '拉格朗日中值定理', NULL),
       (23, 1, 17, '洛必达法则', NULL),
       (24, 1, 18, '泰勒公式', NULL),
       (25, 1, 19, '函数单调性', NULL),
       (26, 1, 19, '曲线凹凸性', NULL),
       (27, 1, 20, '函数极值', NULL),
       (28, 1, 21, '曲率', NULL),
       (29, 1, 22, '原函数', NULL),
       (30, 1, 22, '不定积分', NULL),
       (31, 1, 23, '换元积分法', NULL),
       (32, 1, 24, '分部积分法', NULL),
       (33, 1, 25, '有理函数积分', NULL),
       (34, 1, 26, '定积分', NULL),
       (35, 1, 27, '牛顿-莱布尼茨公式', NULL),
       (36, 1, 28, '定积分换元法', NULL),
       (37, 1, 28, '定积分分部积分法', NULL),
       (38, 1, 29, '反常积分', NULL),
       (39, 1, 30, '元素法', NULL),
       (40, 1, 31, '平面图形面积', NULL),
       (41, 1, 31, '旋转体体积', NULL),
       (42, 1, 32, '变力做功', NULL),
       (43, 1, 33, '微分方程', NULL),
       (44, 1, 34, '可分离变量方程', NULL),
       (45, 1, 35, '齐次方程', NULL),
       (46, 1, 36, '一阶线性微分方程', NULL),
       (47, 1, 37, '可降阶方程', NULL),
       (48, 1, 38, '高阶线性微分方程', NULL),
       (49, 1, 39, '常系数齐次线性方程', NULL),
       (50, 1, 40, '常系数非齐次线性方程', NULL);
/*!40000 ALTER TABLE `section_knowledge_rel`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sections`
--

DROP TABLE IF EXISTS `sections`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sections`
(
    `id`         bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `chapter_id` bigint       NOT NULL COMMENT '关联章id',
    `name`       varchar(128) NOT NULL COMMENT '小节名称',
    `summary`    text COMMENT '小节简介',
    `sort_order` int          NOT NULL DEFAULT '0' COMMENT '排序',
    `created_at` datetime              DEFAULT CURRENT_TIMESTAMP,
    `updated_at` datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_chapter` (`chapter_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 41
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='节表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sections`
--

LOCK TABLES `sections` WRITE;
/*!40000 ALTER TABLE `sections`
    DISABLE KEYS */;
INSERT INTO `sections`
VALUES (2, 2, '映射与函数', NULL, 1, NULL, NULL),
       (3, 2, '数列的极限', NULL, 2, NULL, NULL),
       (4, 2, '函数的极限', NULL, 3, NULL, NULL),
       (5, 2, '无穷小与无穷大', NULL, 4, NULL, NULL),
       (6, 2, '极限运算法则', NULL, 5, NULL, NULL),
       (7, 2, '极限存在准则与两个重要极限', NULL, 6, NULL, NULL),
       (8, 2, '函数的连续性与间断点', NULL, 7, NULL, NULL),
       (9, 2, '连续函数的运算与初等函数的连续性', NULL, 8, NULL, NULL),
       (10, 2, '闭区间上连续函数的性质', NULL, 9, NULL, NULL),
       (11, 3, '导数概念', NULL, 1, NULL, NULL),
       (12, 3, '函数的求导法则', NULL, 2, NULL, NULL),
       (13, 3, '高阶导数', NULL, 3, NULL, NULL),
       (14, 3, '隐函数及由参数方程所确定的函数的导数', NULL, 4, NULL, NULL),
       (15, 3, '函数的微分', NULL, 5, NULL, NULL),
       (16, 4, '微分中值定理', NULL, 1, NULL, NULL),
       (17, 4, '洛必达法则', NULL, 2, NULL, NULL),
       (18, 4, '泰勒公式', NULL, 3, NULL, NULL),
       (19, 4, '函数的单调性与曲线的凹凸性', NULL, 4, NULL, NULL),
       (20, 4, '函数的极值与最大值最小值', NULL, 5, NULL, NULL),
       (21, 4, '曲率', NULL, 6, NULL, NULL),
       (22, 5, '不定积分的概念与性质', NULL, 1, NULL, NULL),
       (23, 5, '换元积分法', NULL, 2, NULL, NULL),
       (24, 5, '分部积分法', NULL, 3, NULL, NULL),
       (25, 5, '有理函数的积分', NULL, 4, NULL, NULL),
       (26, 6, '定积分的概念与性质', NULL, 1, NULL, NULL),
       (27, 6, '微积分基本公式', NULL, 2, NULL, NULL),
       (28, 6, '定积分的换元法和分部积分法', NULL, 3, NULL, NULL),
       (29, 6, '反常积分', NULL, 4, NULL, NULL),
       (30, 7, '定积分的元素法', NULL, 1, NULL, NULL),
       (31, 7, '定积分在几何学上的应用', NULL, 2, NULL, NULL),
       (32, 7, '定积分在物理学上的应用', NULL, 3, NULL, NULL),
       (33, 8, '微分方程的基本概念', NULL, 1, NULL, NULL),
       (34, 8, '可分离变量的微分方程', NULL, 2, NULL, NULL),
       (35, 8, '齐次方程', NULL, 3, NULL, NULL),
       (36, 8, '一阶线性微分方程', NULL, 4, NULL, NULL),
       (37, 8, '可降阶的高阶微分方程', NULL, 5, NULL, NULL),
       (38, 8, '高阶线性微分方程', NULL, 6, NULL, NULL),
       (39, 8, '常系数齐次线性微分方程', NULL, 7, NULL, NULL),
       (40, 8, '常系数非齐次线性微分方程', NULL, 8, NULL, NULL);
/*!40000 ALTER TABLE `sections`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student_ability_baselines`
--

DROP TABLE IF EXISTS `student_ability_baselines`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_ability_baselines`
(
    `id`                     bigint  NOT NULL AUTO_INCREMENT,
    `student_id`             bigint  NOT NULL,
    `difficulty_level`       tinyint NOT NULL DEFAULT '1' COMMENT '最近发展区难度层级',
    `first_contact_accuracy` decimal(5, 2)    DEFAULT '0.00' COMMENT '首次接触知识点正确率',
    `stability_rate`         decimal(5, 2)    DEFAULT '0.00' COMMENT '多次学习稳定率',
    `updated_at`             datetime         DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student` (`student_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='学生能力基线';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_ability_baselines`
--

LOCK TABLES `student_ability_baselines` WRITE;
/*!40000 ALTER TABLE `student_ability_baselines`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `student_ability_baselines`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student_answers`
--

DROP TABLE IF EXISTS `student_answers`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_answers`
(
    `id`                 bigint NOT NULL AUTO_INCREMENT,
    `submission_id`      bigint NOT NULL,
    `question_id`        bigint NOT NULL,
    `answer`             text COMMENT '学生答案JSON',
    `score`              int             DEFAULT NULL COMMENT '单题得分',
    `comment`            text COMMENT '单题评语',
    `time_spent`         int             DEFAULT '0' COMMENT '耗时(秒)',
    `attempt_count`      int    NOT NULL DEFAULT '0' COMMENT '修改次数',
    `help_request_count` int    NOT NULL DEFAULT '0' COMMENT '求助次数',
    `error_type`         varchar(64)     DEFAULT NULL COMMENT '错误类型',
    `created_at`         datetime        DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_submit` (`submission_id`),
    KEY `idx_question` (`question_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='单题作答';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_answers`
--

LOCK TABLES `student_answers` WRITE;
/*!40000 ALTER TABLE `student_answers`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `student_answers`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student_course_progresses`
--

DROP TABLE IF EXISTS `student_course_progresses`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_course_progresses`
(
    `id`                   bigint NOT NULL AUTO_INCREMENT,
    `student_id`           bigint NOT NULL,
    `course_id`            bigint NOT NULL,
    `total_progress`       decimal(5, 2)   DEFAULT '0.00' COMMENT '课程总进度',
    `interaction_count`    int    NOT NULL DEFAULT '0' COMMENT '互动次数',
    `avg_task_score`       decimal(5, 2)   DEFAULT NULL COMMENT '平均作业分数',
    `knowledge_cover_rate` decimal(5, 2)   DEFAULT '0.00' COMMENT '知识点覆盖率',
    `task_completion_rate` decimal(5, 2)   DEFAULT '0.00' COMMENT '任务完成率',
    `updated_at`           datetime        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_stu_course` (`student_id`, `course_id`),
    KEY `idx_student` (`student_id`),
    KEY `idx_course` (`course_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 5
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='学生课程学习进度';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_course_progresses`
--

LOCK TABLES `student_course_progresses` WRITE;
/*!40000 ALTER TABLE `student_course_progresses`
    DISABLE KEYS */;
INSERT INTO `student_course_progresses`
VALUES (1, 3, 1, 0.00, 0, 0.00, 0.00, 0.00, '2026-08-24 10:00:00');
/*!40000 ALTER TABLE `student_course_progresses`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student_help_densities`
--

DROP TABLE IF EXISTS `student_help_densities`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_help_densities`
(
    `id`                   bigint  NOT NULL AUTO_INCREMENT,
    `student_id`           bigint  NOT NULL,
    `top_difficulty_level` tinyint NOT NULL DEFAULT '1' COMMENT '高频求助难度层级',
    `help_count`           int     NOT NULL DEFAULT '0' COMMENT '总求助次数',
    `top_help_type`        varchar(64)      DEFAULT NULL COMMENT '高频求助类型',
    `updated_at`           datetime         DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student` (`student_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='学生求助密度分析';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_help_densities`
--

LOCK TABLES `student_help_densities` WRITE;
/*!40000 ALTER TABLE `student_help_densities`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `student_help_densities`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student_interaction_styles`
--

DROP TABLE IF EXISTS `student_interaction_styles`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_interaction_styles`
(
    `id`                        bigint NOT NULL AUTO_INCREMENT,
    `student_id`                bigint NOT NULL,
    `avg_text_length`           int    NOT NULL DEFAULT '0' COMMENT '平均发送文本长度',
    `seriousness_score`         decimal(5, 2)   DEFAULT '0.00' COMMENT '认真礼貌得分',
    `direct_answer_count`       int    NOT NULL DEFAULT '0' COMMENT '直接求答案次数',
    `concept_inquiry_count`     int    NOT NULL DEFAULT '0' COMMENT '概念提问次数',
    `confirmation_count`        int    NOT NULL DEFAULT '0' COMMENT '确认提问次数',
    `other_query_count`         int    NOT NULL DEFAULT '0' COMMENT '其他提问次数',
    `emotion_positive_count`    int    NOT NULL DEFAULT '0' COMMENT '积极情绪提问次数',
    `emotion_neutral_count`     int    NOT NULL DEFAULT '0' COMMENT '中性情绪提问次数',
    `emotion_negative_count`    int    NOT NULL DEFAULT '0' COMMENT '消极情绪提问次数',
    `total_interactions`        int    NOT NULL DEFAULT '0' COMMENT '总交互次数',
    `student_interaction_style` text COMMENT 'AI评估交互风格描述',
    `preferred_ai_style`        text COMMENT '推荐AI交互风格',
    `updated_at`                datetime        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student` (`student_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='学生交互风格画像';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_interaction_styles`
--

LOCK TABLES `student_interaction_styles` WRITE;
/*!40000 ALTER TABLE `student_interaction_styles`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `student_interaction_styles`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student_knowledge_masteries`
--

DROP TABLE IF EXISTS `student_knowledge_masteries`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_knowledge_masteries`
(
    `id`                     bigint      NOT NULL AUTO_INCREMENT,
    `student_id`             bigint      NOT NULL,
    `course_id`              bigint               DEFAULT NULL,
    `knowledge_id`           varchar(64) NOT NULL COMMENT 'Neo4j知识点ID',
    `master_rate`            decimal(5, 2)        DEFAULT '0.00' COMMENT '掌握度百分比',
    `question_correct_count` int         NOT NULL DEFAULT '0' COMMENT '答对次数',
    `question_total_count`   int         NOT NULL DEFAULT '0' COMMENT '总答题次数',
    `updated_at`             datetime             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_stu_knowledge` (`student_id`, `knowledge_id`),
    KEY `idx_student` (`student_id`),
    KEY `idx_course` (`course_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='学生知识点掌握情况';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_knowledge_masteries`
--

LOCK TABLES `student_knowledge_masteries` WRITE;
/*!40000 ALTER TABLE `student_knowledge_masteries`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `student_knowledge_masteries`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student_learning_attitudes`
--

DROP TABLE IF EXISTS `student_learning_attitudes`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_learning_attitudes`
(
    `id`                 bigint NOT NULL AUTO_INCREMENT,
    `student_id`         bigint NOT NULL,
    `kb_view_count`      int    NOT NULL DEFAULT '0' COMMENT '知识库浏览次数',
    `error_review_count` int    NOT NULL DEFAULT '0' COMMENT '错题复习次数',
    `interaction_count`  int    NOT NULL DEFAULT '0' COMMENT 'AI交互提问次数',
    `guide_push_count`   int    NOT NULL DEFAULT '0' COMMENT '系统引导推送次数',
    `guide_accept_count` int    NOT NULL DEFAULT '0' COMMENT '接受引导次数',
    `attitude_score`     decimal(5, 2)   DEFAULT '0.00' COMMENT '学习态度评分',
    `updated_at`         datetime        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student` (`student_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='学生学习态度统计';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_learning_attitudes`
--

LOCK TABLES `student_learning_attitudes` WRITE;
/*!40000 ALTER TABLE `student_learning_attitudes`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `student_learning_attitudes`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student_submissions`
--

DROP TABLE IF EXISTS `student_submissions`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_submissions`
(
    `id`             bigint                                                   NOT NULL AUTO_INCREMENT,
    `task_id`        bigint                                                   NOT NULL,
    `student_id`     bigint                                                   NOT NULL,
    `status`         enum ('pending','submitted','passed','returned','draft') NOT NULL,
    `submitted_time` datetime DEFAULT NULL,
    `graded_time`    datetime DEFAULT NULL,
    `total_score`    int      DEFAULT NULL,
    `comment`        text COMMENT '评语',
    `created_at`     datetime DEFAULT CURRENT_TIMESTAMP,
    `updated_at`     datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_task` (`task_id`),
    KEY `idx_student` (`student_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='学生提交记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_submissions`
--

LOCK TABLES `student_submissions` WRITE;
/*!40000 ALTER TABLE `student_submissions`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `student_submissions`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `task_analysises`
--

DROP TABLE IF EXISTS `task_analysises`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `task_analysises`
(
    `id`              bigint NOT NULL AUTO_INCREMENT,
    `task_id`         bigint NOT NULL,
    `total_submitted` int    NOT NULL DEFAULT '0' COMMENT '提交人数',
    `avg_score`       decimal(5, 2)   DEFAULT NULL COMMENT '平均分',
    `max_score`       int             DEFAULT NULL COMMENT '最高分',
    `min_score`       int             DEFAULT NULL COMMENT '最低分',
    `avg_time_spent`  int    NOT NULL DEFAULT '0' COMMENT '平均耗时(秒)',
    `updated_at`      datetime        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task` (`task_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='任务整体统计分析';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task_analysises`
--

LOCK TABLES `task_analysises` WRITE;
/*!40000 ALTER TABLE `task_analysises`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `task_analysises`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `task_question_rel`
--

DROP TABLE IF EXISTS `task_question_rel`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `task_question_rel`
(
    `id`            bigint NOT NULL AUTO_INCREMENT,
    `task_id`       bigint NOT NULL,
    `question_id`   bigint NOT NULL,
    `score`         int    NOT NULL DEFAULT '0' COMMENT '单题分值',
    `question_sort` int    NOT NULL DEFAULT '0' COMMENT '题号',
    PRIMARY KEY (`id`),
    KEY `idx_task_question` (`task_id`, `question_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='任务题目关联';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task_question_rel`
--

LOCK TABLES `task_question_rel` WRITE;
/*!40000 ALTER TABLE `task_question_rel`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `task_question_rel`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `task_student_rel`
--

DROP TABLE IF EXISTS `task_student_rel`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `task_student_rel`
(
    `id`         bigint NOT NULL AUTO_INCREMENT,
    `task_id`    bigint NOT NULL,
    `student_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_stu` (`task_id`, `student_id`),
    KEY `idx_task` (`task_id`),
    KEY `idx_student` (`student_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='任务学生关联';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task_student_rel`
--

LOCK TABLES `task_student_rel` WRITE;
/*!40000 ALTER TABLE `task_student_rel`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `task_student_rel`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tasks`
--

DROP TABLE IF EXISTS `tasks`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tasks`
(
    `id`                    bigint                                      NOT NULL AUTO_INCREMENT,
    `course_id`             bigint                                               DEFAULT NULL COMMENT '关联课程ID',
    `assigner_id`           bigint                                      NOT NULL COMMENT '布置人ID',
    `title`                 varchar(256)                                NOT NULL COMMENT '标题',
    `description`           text COMMENT '说明',
    `type`                  enum ('homework','exam','qna')              NOT NULL COMMENT '作业/考试/问答',
    `total_score`           int                                         NOT NULL DEFAULT '0' COMMENT '总分',
    `start_time`            datetime                                    NOT NULL,
    `deadline`              datetime                                             DEFAULT NULL,
    `status`                enum ('draft','published','closed')         NOT NULL COMMENT '草稿/已发布/已截止',
    `after_submit_strategy` enum ('auto_all','auto_objective','manual') NOT NULL COMMENT '批改策略',
    `created_at`            datetime                                             DEFAULT CURRENT_TIMESTAMP,
    `updated_at`            datetime                                             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_course` (`course_id`),
    KEY `idx_assigner` (`assigner_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='任务作业考试';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tasks`
--

LOCK TABLES `tasks` WRITE;
/*!40000 ALTER TABLE `tasks`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `tasks`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teacher_course_analysises`
--

DROP TABLE IF EXISTS `teacher_course_analysises`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teacher_course_analysises`
(
    `id`                  bigint NOT NULL AUTO_INCREMENT,
    `course_id`           bigint NOT NULL,
    `total_students`      int    NOT NULL DEFAULT '0' COMMENT '总学生数',
    `avg_score_rate`      decimal(5, 2)   DEFAULT '0.00' COMMENT '平均得分率',
    `avg_progress`        decimal(5, 2)   DEFAULT '0.00' COMMENT '平均学习进度',
    `task_count`          int    NOT NULL DEFAULT '0' COMMENT '任务总数',
    `submission_rate`     decimal(5, 2)   DEFAULT '0.00' COMMENT '作业提交率',
    `opening_questions`   int    NOT NULL DEFAULT '0' COMMENT '待处理提问数',
    `alert_student_count` int    NOT NULL DEFAULT '0' COMMENT '预警学生数量',
    `weakest_knowledge`   varchar(64)     DEFAULT NULL COMMENT '最薄弱知识点Neo4jID',
    `updated_at`          datetime        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_course` (`course_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 3
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='教师课程统计分析';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teacher_course_analysises`
--

LOCK TABLES `teacher_course_analysises` WRITE;
/*!40000 ALTER TABLE `teacher_course_analysises`
    DISABLE KEYS */;
INSERT INTO `teacher_course_analysises`
VALUES (1, 1, 1, 0.00, 0.00, 0, 0.00, 0, 1, NULL, '2026-08-24 10:00:00');
/*!40000 ALTER TABLE `teacher_course_analysises`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teacher_question_analysises`
--

DROP TABLE IF EXISTS `teacher_question_analysises`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teacher_question_analysises`
(
    `id`                     bigint NOT NULL AUTO_INCREMENT,
    `question_id`            bigint NOT NULL,
    `student_count`          int    NOT NULL DEFAULT '0' COMMENT '作答总人数',
    `avg_correct_rate`       decimal(5, 2)   DEFAULT '0.00' COMMENT '平均正确率',
    `avg_time_spent`         int    NOT NULL DEFAULT '0' COMMENT '平均耗时',
    `abandon_rate`           decimal(5, 2)   DEFAULT '0.00' COMMENT '弃题率',
    `avg_help_request_count` decimal(5, 2)   DEFAULT '0.00' COMMENT '平均求助次数',
    `ai_quality_rating`      decimal(5, 2)   DEFAULT NULL COMMENT 'AI题目质量评分',
    `ai_difficulty_rating`   decimal(5, 2)   DEFAULT NULL COMMENT 'AI预估难度分',
    `top_error_type`         varchar(64)     DEFAULT NULL COMMENT '高频错误类型',
    `updated_at`             datetime        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_question` (`question_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='题目学情分析统计';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teacher_question_analysises`
--

LOCK TABLES `teacher_question_analysises` WRITE;
/*!40000 ALTER TABLE `teacher_question_analysises`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `teacher_question_analysises`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_files`
--

DROP TABLE IF EXISTS `user_files`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_files`
(
    `id`         bigint                                     NOT NULL AUTO_INCREMENT,
    `user_id`    bigint                                     NOT NULL,
    `file_name`  varchar(256)                               NOT NULL,
    `file_desc`  text,
    `file_url`   varchar(512)                               NOT NULL,
    `size`       bigint                                     NOT NULL COMMENT '字节大小',
    `type`       enum ('pdf','pptx','docx','media','other') NOT NULL,
    `source`     enum ('upload','generated')                NOT NULL COMMENT '来源：上传/AI生成',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='用户文件';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_files`
--

LOCK TABLES `user_files` WRITE;
/*!40000 ALTER TABLE `user_files`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `user_files`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users`
(
    `id`         bigint                                  NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`   varchar(64)                             NOT NULL COMMENT '用户名/学号',
    `realname`   varchar(32)                             NOT NULL COMMENT '真实名称',
    `password`   varchar(128)                            NOT NULL COMMENT '密码哈希值',
    `role`       enum ('student','teacher','researcher') NOT NULL COMMENT '角色：学生/教师/研究员',
    `avatar_url` varchar(512) DEFAULT '' COMMENT '头像url',
    `created_at` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 4
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users`
    DISABLE KEYS */;
INSERT INTO `users`
VALUES (1, 'zhangming', '张明', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'student',
        'https://example.com/avatar/zhangming.jpg', '2026-08-15 05:29:54', '2026-08-15 05:29:54'),
       (2, 'wangwu', '王五', '1', 'teacher', 'https://example.com/avatar/wangwu.jpg', '2026-08-15 05:29:54',
        '2026-08-24 06:43:20'),
       (3, 'xiaoming', '小明', '123456', 'student', 'https://example.com/avatar/xiaoming.jpg', '2026-08-19 05:42:55',
        '2026-08-19 05:42:55');
/*!40000 ALTER TABLE `users`
    ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE = @OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE = @OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS = @OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT = @OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS = @OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION = @OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES = @OLD_SQL_NOTES */;

-- Dump completed on 2026-08-24 10:06:09
