package com.superyoudi.coursewareservice.controller;

import com.superyoudi.common.dto.UserFileRefDTO;
import com.superyoudi.common.entity.Result;
import com.superyoudi.coursewareservice.pojo.dto.CoursewareGenData;
import com.superyoudi.coursewareservice.pojo.entity.CoursewareRecord;
import com.superyoudi.coursewareservice.service.CoursewareFileRefService;
import com.superyoudi.coursewareservice.service.CoursewareGenerationService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @author Sijayyx
 */
@RestController
@RequestMapping("/course/preparing")
public class CoursewareGenerationController {


    private final CoursewareGenerationService coursewareGenerationService;
    private final CoursewareFileRefService coursewareFileRefService;

    public CoursewareGenerationController(CoursewareGenerationService coursewareGenerationService, CoursewareFileRefService coursewareFileRefService) {
        this.coursewareGenerationService = coursewareGenerationService;
        this.coursewareFileRefService = coursewareFileRefService;
    }

    /// 获取课件生成引用的表
    @GetMapping("/refs/{coursewareRecordId}")
    public Result<List<UserFileRefDTO>> getCoursewareFileRefs(@PathVariable Long coursewareRecordId) {
        List<UserFileRefDTO> refedFiles = coursewareFileRefService.getRefedFiles(coursewareRecordId);
        return Result.success(refedFiles);
    }

    /// 上传文件并添加文件引用
    /// TODO 更新接口文档
    @PostMapping("/refs")
    public Result<Void> uploadFile(@RequestParam Long coursewareRecordId, @RequestParam MultipartFile file,
                                   @RequestParam Long userId, @RequestParam String fileDesc, @RequestParam String source) {
        coursewareFileRefService.saveFileAndRef(coursewareRecordId, file, userId, fileDesc, source);
        return Result.success();
    }

    /// 删除文件引用，但不删除文件本身
    @DeleteMapping("/refs/{fileRefId}")
    public Result<Void> deleteCoursewareFileRef(@PathVariable Long fileRefId) {
        coursewareFileRefService.deleteFileRef(fileRefId);
        return Result.success();
    }

    /// 获取课件生成记录
    @GetMapping("/record/{coursewareRecordId}")
    public Result<CoursewareRecord> recoverCoursewareRecord(@PathVariable Long coursewareRecordId) {
        return Result.success(coursewareGenerationService.recoverCoursewareRecord(coursewareRecordId));
    }


    /// 导出压缩包，返回压缩包文件名
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
