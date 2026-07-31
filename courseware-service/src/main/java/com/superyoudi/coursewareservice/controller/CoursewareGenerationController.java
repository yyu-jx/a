package com.superyoudi.coursewareservice.controller;

import com.superyoudi.common.entity.Result;
import com.superyoudi.coursewareservice.dto.CoursewareGenData;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Sijayyx
 */
@RestController
@RequestMapping("/course/preparing")
public class CoursewareGenerationController {


    @PostMapping("/export")
    public Result<String> exportCourseware(@RequestBody CoursewareGenData data) {


        return Result.success("success");
    }

}
