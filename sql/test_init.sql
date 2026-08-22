-- 测试用的！！！！
INSERT INTO `users` (`username`, `realname`, `password`, `role`, `avatar_url`)
VALUES ('zhangming', '张明', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'student',
        'https://example.com/avatar/zhangming.jpg');

INSERT INTO `users` (`username`, `realname`, `password`, `role`, `avatar_url`)
VALUES ('wangwu', '王五', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'teacher',
        'https://example.com/avatar/wangwu.jpg');

INSERT INTO `users` (`username`, `realname`, `password`, `role`, `avatar_url`)
VALUES ('xiaoming', '小明', '123456', 'student',
        'https://example.com/avatar/xiaoming.jpg');

-- 2. 班级表测试数据
INSERT INTO `classes` (`name`)
VALUES ('计算机科学与技术2024级1班');

-- 课程数据
INSERT INTO course_data (name, info)
VALUES ('高等数学',
        '{"publisher": "同济大学出版社", "authors": ["同济大学数学系"], "edition": "第7版", "chapters": ["函数与极限", "导数与微分", "微分中值定理与导数的应用", "不定积分", "定积分", "定积分的应用", "微分方程"]}');

INSERT INTO course_data (name, info)
VALUES ('Java程序设计',
        '{"publisher": "清华大学出版社", "authors": ["张孝祥", "周志明"], "edition": "第2版", "chapters": ["Java基础语法", "面向对象编程", "集合框架", "IO流", "多线程"]}');


-- ------------------------------------------------

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
TRUNCATE TABLE `course_data`;
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

