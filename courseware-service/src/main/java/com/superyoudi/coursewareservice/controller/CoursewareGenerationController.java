package com.superyoudi.coursewareservice.controller;

import com.superyoudi.common.entity.Result;
import com.superyoudi.coursewareservice.dto.CoursewareGenData;
import com.superyoudi.coursewareservice.dto.CoursewareRecord;
import com.superyoudi.coursewareservice.service.CoursewareGenerationService;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/record/{coursewareRecordId}")
    public Result<CoursewareRecord> recoverCoursewareRecord(@PathVariable Long coursewareRecordId) {
        return Result.success(coursewareGenerationService.recoverCoursewareRecord(coursewareRecordId));
    }


    @PostMapping("/export")
    public Result<String> exportCourseware(@RequestBody CoursewareGenData data) {
        try {
            String zipName = coursewareGenerationService.exportCourseware(data);
            return Result.success(zipName);
        } catch (IOException e) {
            return Result.error("课件生成失败");
        }

    }

}
