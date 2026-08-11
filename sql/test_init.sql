-- 测试用的！！！！
INSERT INTO `users` (`username`, `realname`, `password`, `role`, `avatar_url`)
VALUES ('zhangming', '张明', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'student', 'https://example.com/avatar/zhangming.jpg');

INSERT INTO `users` (`username`, `realname`, `password`, `role`, `avatar_url`)
VALUES ('wangwu', '王五', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'teacher', 'https://example.com/avatar/wangwu.jpg');

-- 2. 班级表测试数据
INSERT INTO `classes` (`name`)
VALUES ('计算机科学与技术2024级1班');

-- 3. 课程表测试数据（高等数学）
INSERT INTO `courses` (`name`, `description`, `cover_url`, `credit`, `status`, `teacher_id`)
VALUES ('高等数学', '教材: 同济大学出版社',
        'https://example.com/covers/advanced_math.jpg', 4, 'opening', 2);

-- 获取下面那段代码
SELECT CONCAT('TRUNCATE TABLE `', TABLE_NAME, '`;')
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE();

-- 清空所有表，但不删除表本身
TRUNCATE TABLE `ai_citations`;
TRUNCATE TABLE `ai_suggestions`;
TRUNCATE TABLE `chapters`;
TRUNCATE TABLE `chat_course_rel`;
TRUNCATE TABLE `chat_courseware_rec_rel`;
TRUNCATE TABLE `chat_messages`;
TRUNCATE TABLE `chat_session`;
TRUNCATE TABLE `class_student_rel`;
TRUNCATE TABLE `classes`;
TRUNCATE TABLE `course_student_rel`;
TRUNCATE TABLE `courses`;
TRUNCATE TABLE `courseware_file_rel`;
TRUNCATE TABLE `courseware_records`;
TRUNCATE TABLE `learning_path_nodes`;
TRUNCATE TABLE `learning_paths`;
TRUNCATE TABLE `mission_analysises`;
TRUNCATE TABLE `question_knowledge_rel`;
TRUNCATE TABLE `question_properties`;
TRUNCATE TABLE `questions`;
TRUNCATE TABLE `section_knowledge_rel`;
TRUNCATE TABLE `sections`;
TRUNCATE TABLE `student_ability_baselines`;
TRUNCATE TABLE `student_answers`;
TRUNCATE TABLE `student_course_progresses`;
TRUNCATE TABLE `student_help_densities`;
TRUNCATE TABLE `student_interaction_styles`;
TRUNCATE TABLE `student_knowledge_masteries`;
TRUNCATE TABLE `student_learning_attitudes`;
TRUNCATE TABLE `student_submissions`;
TRUNCATE TABLE `task_question_rel`;
TRUNCATE TABLE `task_student_rel`;
TRUNCATE TABLE `tasks`;
TRUNCATE TABLE `teacher_course_analysises`;
TRUNCATE TABLE `teacher_question_analysises`;
TRUNCATE TABLE `user_files`;
TRUNCATE TABLE `users`;
