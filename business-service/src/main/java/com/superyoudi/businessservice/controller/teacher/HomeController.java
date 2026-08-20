package com.superyoudi.businessservice.controller.teacher;

import com.superyoudi.businessservice.service.HomeService;
import com.superyoudi.common.entity.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("teacherHome")
@RequestMapping
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    /**
     * 教师端获取ai建议
     *
     * @param teacherId
     * @return 建议
     * @author gaoyuxin
     */
    @GetMapping("/ai/suggestions/teacher")
    public Result<String> getTeacherSuggestions( Long teacherId) {
        return Result.success(homeService.generateSuggestions(teacherId));
    }
}
