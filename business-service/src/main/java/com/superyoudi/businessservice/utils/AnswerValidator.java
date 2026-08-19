package com.superyoudi.businessservice.utils;

import com.superyoudi.aiservice.ai.AnswerCorrectionModel;
import com.superyoudi.businessservice.pojo.entity.Question;
import com.superyoudi.businessservice.pojo.entity.StudentAnswer;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Sijayyx
 */
public class AnswerValidator {

    /**
     * 判断选择题答案是否正确
     *
     * @param choiceAnswer        正确答案
     * @param studentChoiceAnswer 学生答案
     * @return 正确答案的数量
     */
    public static int validateChoiceAnswer(Question.ChoiceAnswer choiceAnswer, StudentAnswer.ChoiceAnswer studentChoiceAnswer) {
        // 判断是单选还是多选
        if (choiceAnswer.getSelect() != null) {
            // 单选
            return choiceAnswer.getSelect().equals(studentChoiceAnswer.getSelect()) ? 1 : 0;
        } else {
            // 多选
            int correctCount = 0;
            for (String correct : choiceAnswer.getCorrect()) {
                if (studentChoiceAnswer.getSelects().contains(correct)) {
                    correctCount++;
                }
            }

            return correctCount;
        }
    }

    /**
     * 为填空题评分
     *
     * @param correctionModel    模型
     * @param question           题干
     * @param totalScore         题目总分
     * @param questionFillAnswer 正确答案
     * @param studentFillAnswer  学生答案
     * @return 最终得分
     */
    public static int validateFillAnswer(AnswerCorrectionModel correctionModel, String question, int totalScore,
                                         Question.FillAnswer questionFillAnswer, StudentAnswer.FillAnswer studentFillAnswer) {

        Assert.notNull(questionFillAnswer, "FillAnswer cannot be null");
        Assert.notNull(studentFillAnswer, "StudentFillAnswer cannot be null");

        List<AnswerCorrectionModel.FillAnswerEntry> entries = new ArrayList<>();

        // 构建学生答案
        Map<String, String> studentAnswerMap = new HashMap<>();
        if (studentFillAnswer.getBlanks() != null) {
            studentAnswerMap = studentFillAnswer.getBlanks().stream()
                    .collect(Collectors.toMap(
                            StudentAnswer.FillAnswer.Blank::getIndex,
                            StudentAnswer.FillAnswer.Blank::getInput,
                            (v1, v2) -> v1
                    ));
        }

        // 遍历正确答案，匹配学生答案
        for (Question.FillAnswer.Blank blank : questionFillAnswer.getBlanks()) {
            String index = blank.getIndex();
            String refAnswer = blank.getAnswer();
            String studentAnswer = studentAnswerMap.getOrDefault(index, null);

            entries.add(new AnswerCorrectionModel.FillAnswerEntry(
                    Integer.parseInt(index),
                    refAnswer,
                    studentAnswer
            ));
        }

        return correctionModel.doFillAnswerCorrection(totalScore, question, entries);
    }

    /**
     * 为主观题评分
     *
     * @param correctionModel 模型
     * @param question        题干
     * @param totalScore      题目总分
     * @param correctAnswer   正确答案
     * @param studentAnswer   学生答案
     * @return 最终得分
     */
    public static int validateSubjectiveAnswer(AnswerCorrectionModel correctionModel, String question, int totalScore, String correctAnswer, String studentAnswer) {
        return correctionModel.doSubjectiveAnswerCorrection(totalScore, question, correctAnswer, studentAnswer);
    }


    /**
     * 判断 判断题答案是否正确
     *
     * @param trueFalseAnswer        正确答案
     * @param studentTrueFalseAnswer 学生答案
     * @return 是否正确
     */
    public static boolean validateTrueFalseAnswer(Question.TrueFalseAnswer trueFalseAnswer, StudentAnswer.TrueFalseAnswer studentTrueFalseAnswer) {
        return trueFalseAnswer.getResult().equals(studentTrueFalseAnswer.getChoose());
    }

    /**
     * 计算学生选择题目得分
     *
     * @param totalScore         题目分数
     * @param correct            正确答案
     * @param correctAnswerCount 学生选对的数量
     * @return 最终得分
     */
    public static int calcStudentAnswerScore(int totalScore, Question.ChoiceAnswer correct, int correctAnswerCount) {

        float correctSelectCount = 1;
        if (correct.getCorrect() != null)
            correctSelectCount = correct.getCorrect().size();

        float correctRate = (float) correctAnswerCount / correctSelectCount;
        return (int) (totalScore * correctRate);
    }

    /// 题目评分策略接口
    @FunctionalInterface
    public interface CorrectionStrategy {
        /// 计算得分
        int calcScore(Question question, Object correctAnswer, Object studentAnswer, int questionScore);
    }
}