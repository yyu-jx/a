package com.superyoudi.businessservice.utils;

import com.superyoudi.businessservice.pojo.entity.Question;
import com.superyoudi.businessservice.pojo.entity.StudentAnswer;

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
}