package com.superyoudi.common.constants;

/**
 * @author Sijayyx
 */
public final class CoursewareGenConstants {
    /// 用户ID
    public static final String KEY_USER_ID = "user_id";
    /// 课件生成记录ID
    public static final String KEY_COURSEWARE_REC_ID = "courseware_rec_id";
    /// 用户输入
    public static final String KEY_INPUT = "input";
    /// HITL的用户反馈信息（如果有）
    public static final String KEY_HITL_USER_FEEDBACK = "hitl_user_feedback";
    /// HITL的批准状态
    public static final String KEY_HITL_APPROVE_STATUS = "hitl_approve_status";
    /// 用户聊天消息的附加信息
    public static final String KEY_USER_ADDITIONAL_DATA = "additional_data";
    /// AI的流式输出
    public static final String KEY_RESPONSE = "response";
    /// 生成PPT和Word所需信息
    public static final String KEY_GENERATION_INFO = "generation_info";
    /// PPT大纲，Markdown格式
    public static final String KEY_PPT_OUTLINE = "ppt_outline";
    /// 是否可以生成PPT: TRUE | FALSE
    public static final String KEY_SHOULD_GENERATE = "should_generate";
    /// Agents的生成结果
    public static final String KEY_AGENTS_INFO = "agents_info";
    /// 监督者节点
    public static final String NODE_SUPERVISOR = "supervisorNode";
    /// HTML大纲生成节点
    public static final String NODE_OUTLINE_GENERATION = "htmlOutlineGenNode";
    /// HITL节点
    public static final String NODE_PPT_HITL_NODE = "pptHitlNode";
    /// PPT生成节点
    public static final String NODE_PPT_GENERATION = "pptGenerationNode";
    /// Word生成节点
    public static final String NODE_WORD_GENERATION = "wordGenerationNode";
    /// 子Agents入口节点
    public static final String NODE_SUB_AGENTS_ENTRY = "subAgentsEntryNode";
    /// 子Agents出口节点
    public static final String NODE_SUB_AGENTS_EXIT = "subAgentsExitNode";
    /// PPT子图
    public static final String SUBGRAPH_PPT_GENERATION = "pptGenSubGraph";
}
