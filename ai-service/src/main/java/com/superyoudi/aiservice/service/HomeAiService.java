package com.superyoudi.aiservice.service;

import com.superyoudi.common.dto.StudentCourseProgress;
import com.superyoudi.common.dto.StudentKnowledgeMastery;
import com.superyoudi.common.dto.TeacherHomeAnalysisDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeAiService {


    private final ChatClient chatClient;

    /**
     * 生成学生学习建议
     *
     * @param progressList      学生课程进度列表
     * @param weakKnowledgeList 学生薄弱知识点列表
     * @return 学生学习建议
     */
    public String generateStudentSuggest(List<StudentCourseProgress> progressList, List<StudentKnowledgeMastery> weakKnowledgeList) {


        // 1. 构造系统提示词（角色、规则、输出格式约束）
        String systemPrompt = buildSystemPrompt();

        // 2. 组装用户业务数据上下文
        String userContext = buildUserContext(progressList, weakKnowledgeList);

        // 3. 调用大模型
        String result = chatClient.prompt()
                .system(systemPrompt)
                .user(userContext)
                .call()
                .content();

        return result;
    }

    private String buildSystemPrompt() {
        return """
            你是一名专业的在线教育学习指导老师。
            根据学生【各门课程整体学习进度】和【薄弱知识点清单】，生成个性化学习建议。
            要求严格遵守下面所有规则：
            ## 输入数据说明
            1. 课程进度数据：包含课程整体完成进度、互动次数、任务平均分、知识点覆盖率、任务完成率。
            2. 薄弱知识点：包含所属课程ID、知识点名称、掌握率、答题正误情况。
            3. 知识点通过courseId归属到对应课程。
            
            ## 输出强制要求
            1. 先整体评价学生当前学习状态；
            2. 按课程分组分析：优先展示进度较差课程，列出该课程下薄弱知识点；
            3. 给出明确学习优先级：先复习哪些知识点，再推进哪些新课程；
            4. 建议需要具体可行，不要空话；
            5. 禁止输出JSON、markdown代码块，直接输出自然中文段落；
            6. 语言简洁清晰，面向学生阅读，篇幅控制在30到50字。
            
            不要编造不存在的数据，只基于提供的数据进行分析。
            """;
    }

    private String buildUserContext(List<StudentCourseProgress> progressList,
                                    List<StudentKnowledgeMastery> weakKnowledgeList) {
        StringBuilder sb = new StringBuilder();
        sb.append("【学生课程学习进度数据】\n");
        for (StudentCourseProgress p : progressList) {
            sb.append("课程ID：").append(p.getCourseId()).append("\n");
            sb.append("课程总进度：").append(p.getTotalProgress()).append("\n");
            sb.append("互动次数：").append(p.getInteractionCount()).append("\n");
            sb.append("作业平均分：").append(p.getAvgTaskScore()).append("\n");
            sb.append("知识点覆盖率：").append(p.getKnowledgeCoverRate()).append("\n");
            sb.append("任务完成率：").append(p.getTaskCompletionRate()).append("\n");
            sb.append("------------------------\n");
        }

        sb.append("\n【学生薄弱知识点列表】\n");
        for (StudentKnowledgeMastery wk : weakKnowledgeList) {
            sb.append("课程ID：").append(wk.getCourseId()).append("\n");
            sb.append("知识点名称：").append(wk.getKnowledgeName()).append("\n");
            sb.append("知识点掌握率：").append(wk.getMasterRate()).append("\n");
            sb.append("答题正确数：").append(wk.getQuestionCorrectCount()).append("\n");
            sb.append("答题总数：").append(wk.getQuestionTotalCount()).append("\n");
            sb.append("错误率：").append(wk.getErrorRate()).append("\n");
            sb.append("------------------------\n");
        }

        sb.append("\n请基于以上数据生成学生个性化学习建议。");
        return sb.toString();
    }

    /**
     * 生成教师教学建议
     *
     * @param weakCourse 薄弱课程列表
     * @return 教师教学建议
     */
    public String generateTeacherSuggest(TeacherHomeAnalysisDTO weakCourse) {
        String prompt = buildAiPrompt(List.of(weakCourse));

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();

    }

    private String buildAiPrompt(List<TeacherHomeAnalysisDTO> weakCourses) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是资深教学分析师，请根据以下得分率最低的课程学情数据，为教师生成Markdown格式的教学优化建议,20字左右。\n");
        prompt.append("要求：逐门指出核心问题、薄弱知识点，给出可落地的教学改进方案；预警学生数大于10的课程需提醒出来。\n\n");
        prompt.append("课程学情数据：\n\n");

        for (int i = 0; i < weakCourses.size(); i++) {
            TeacherHomeAnalysisDTO item = weakCourses.get(i);
            prompt.append(String.format("### 薄弱课程：《%s》\n", item.getCourseName()));
            prompt.append("- 总学生数：").append(item.getTotalStudents()).append("\n");
            prompt.append("- 平均得分率：").append(item.getAvgScoreRate()).append("%\n");
            prompt.append("- 平均学习进度：").append(item.getAvgProgress()).append("%\n");
            prompt.append("- 作业提交率：").append(item.getSubmissionRate()).append("%\n");
            prompt.append("- 预警学生数：").append(item.getAlertStudentCount()).append("\n");
            prompt.append("- 最薄弱知识点：").append(item.getWeakestKnowledgeName()).append("\n\n");
        }

        prompt.append("请直接输出Markdown正文，不要多余解释。");
        return prompt.toString();
    }
}
