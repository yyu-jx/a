package com.superyoudi.coursewareservice.controller;

import com.superyoudi.common.entity.Result;
import com.superyoudi.coursewareservice.dto.CoursewareGenData;
import com.superyoudi.coursewareservice.service.CoursewareGenerationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author Sijayyx
 */
@RestController
@RequestMapping("/course/preparing")
public class CoursewareGenerationController {


    private final CoursewareGenerationService coursewareGenerationService;

    public CoursewareGenerationController(CoursewareGenerationService coursewareGenerationService) {
        this.coursewareGenerationService = coursewareGenerationService;
    }

    @PostMapping("/export")
    public Result<String> exportCourseware(@RequestBody CoursewareGenData data) {
        try {
            coursewareGenerationService.exportCourseware(data);
            return Result.success();
        } catch (IOException e) {
            return Result.error("课件生成失败");
        }

    }

}
