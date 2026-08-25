package com.superyoudi.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * wordAction为对教案执行的动作，generate表示新生成一份教案，edit表示对已有教案进行修改，other表示其它操作，none表示不执行任何操作。
 * wordActionInfo为对教案执行动作的信息与要求，若无任何动作，则设置为null。
 * pptAction为对PPT执行的动作，generate表示新生成一个PPT，edit表示对已有PPT进行修改，other表示其它操作，none表示不执行任何操作。
 * pptActionInfo为对PPT执行动作的信息与要求，若无任何动作，则设置为null。
 * title为课程标题
 * basicInfo为教学信息：如授课对象，年级，课程时长;
 * teachingTarget为教学目标：应该掌握的具体知识点;
 * coreKnowledges为核心知识点：最核心的1-3个知识点;
 * hardKnowlegdes为重难点：需要明确讲授或强调的内容;
 * teachingFlow为教学流程：较详细的课堂教学安排;
 * interactionDesign为互动设计: 可选，课堂安排中的互动设计，比如提问与小组讨论等;
 * additionalInfo为额外信息
 *
 * @author Sijayyx
 */
@Data
@AllArgsConstructor
@Builder
public class CoursewareGenerationRequest {
    private String wordAction;
    private String wordActionInfo;
    private String pptAction;
    private String pptActionInfo;
    private String title;
    private String basicInfo;
    private String teachingTarget;
    private String coreKnowledges;
    private String hardKnowleges;
    private String teachingFlow;
    private String interactionDesign;
    private String additionalInfo;

    public CoursewareGenerationRequest.DTO toDTO(Long coursewareRecId) {
        return DTO.builder()
                .coursewareRecId(coursewareRecId)
                .title(title)
                .basicInfo(basicInfo)
                .teachingTarget(teachingTarget)
                .coreKnowledges(coreKnowledges)
                .hardKnowleges(hardKnowleges)
                .teachingFlow(teachingFlow)
                .interactionDesign(interactionDesign)
                .additionalInfo(additionalInfo)
                .build();
    }

    @Data
    @AllArgsConstructor
    @Builder
    public static class DTO {
        private Long coursewareRecId;
        private String title;
        private String basicInfo;
        private String teachingTarget;
        private String coreKnowledges;
        private String hardKnowleges;
        private String teachingFlow;
        private String interactionDesign;
        private String additionalInfo;
    }
}
