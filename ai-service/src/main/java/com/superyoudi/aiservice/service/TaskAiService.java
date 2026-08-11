package com.superyoudi.aiservice.service;

import com.alibaba.fastjson.JSON;
import com.superyoudi.common.dto.ErrorQuestionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskAiService {

    private final ChatClient chatClient;

    public String diagnose(List<ErrorQuestionVO> errorQuestionList ) {

        String systemPrompt = """
        你是一名学习学情分析助手。
        根据学生的错题集合，生成一份简短学情诊断报告。
        要求：
        1. 找出错误率高的知识模块，给出简洁的复习建议；
        2. 如果错题列表为空，输出：学生暂无错题记录，继续保持良好的学习状态；
        3. 输出纯中文自然文本，不要markdown，简短；
        4. 不要多余解释，直接输出诊断报告。
        """;

        String userInput;
        if(CollectionUtils.isEmpty(errorQuestionList)){
            userInput = "该学生没有错题记录";
        }else{
            // 限制最多20条，防止token过大
            List<ErrorQuestionVO> limitList = errorQuestionList.stream().limit(20).toList();
            userInput = "学生错题数据：" + JSON.toJSONString(limitList);
        }

        return chatClient.prompt()
                .system(systemPrompt)
                .user(userInput)
                .call()
                .content();
    }
}
