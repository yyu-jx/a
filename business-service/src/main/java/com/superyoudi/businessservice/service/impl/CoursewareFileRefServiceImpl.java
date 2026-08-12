package com.superyoudi.businessservice.service.impl;

import com.superyoudi.businessservice.mapper.UserFileMapper;
import com.superyoudi.businessservice.pojo.entity.UserFile;
import com.superyoudi.businessservice.service.FileService;
import com.superyoudi.common.dto.UserFileRefDTO;
import com.superyoudi.coursewareservice.mapper.CoursewareFileRelMapper;
import com.superyoudi.coursewareservice.pojo.entity.CoursewareFileRel;
import com.superyoudi.coursewareservice.service.CoursewareFileRefService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

/**
 * @author Sijayyx
 */
@Slf4j
@Service
public class CoursewareFileRefServiceImpl implements CoursewareFileRefService {

    private final CoursewareFileRelMapper coursewareFileRelMapper;
    private final UserFileMapper userFileMapper;
    private final FileService fileService;

    public CoursewareFileRefServiceImpl(CoursewareFileRelMapper coursewareFileRelMapper, UserFileMapper userFileMapper, CoursewareFileRefService coursewareFileRefService, FileService fileService) {
        this.coursewareFileRelMapper = coursewareFileRelMapper;
        this.userFileMapper = userFileMapper;
        this.fileService = fileService;
    }

    @Override
    public void saveFileAndRef(@NonNull Long coursewareRecId, @NonNull MultipartFile file, @NonNull Long userId, String fileDesc, @NonNull String source) {
        Long fileId = fileService.saveUserFile(file, userId, fileDesc, source);
        addFileRef(coursewareRecId, fileId);
    }

    @Override
    public List<UserFileRefDTO> getRefedFiles(@NonNull Long coursewareRecId) {
        List<CoursewareFileRel> rels = coursewareFileRelMapper.findByCoursewareRecordId(coursewareRecId);

        if (rels == null || rels.isEmpty()) {
            return null;
        }

        List<UserFileRefDTO> list = rels.stream()
                .map(rel -> {
                    UserFile file = userFileMapper.findById(rel.getFileId());
                    if (file == null) {
                        log.error("未找到对应的文件: {}", rel.getFileId());
                        return null;
                    }

                    return UserFileRefDTO.builder()
                            .fileRefRelId(rel.getId())
                            .fileId(rel.getFileId())
                            .fileName(file.getFileName())
                            .fileDesc(file.getFileDesc())
                            .fileUrl(file.getFileUrl())
                            .type(file.getType())
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();

        log.info("课件生成 {} 获取 {} 条文件引用", coursewareRecId, list.size());
        return list;

    }

    @Override
    public void addFileRef(@NonNull Long coursewareRecId, @NonNull Long fileId) {
        CoursewareFileRel coursewareFileRel = new CoursewareFileRel(null, coursewareRecId, fileId);
        Long rows = coursewareFileRelMapper.insert(coursewareFileRel);
        if (rows == null || rows == 0) {
            log.error("课件生成的文件引用 {} 创建失败", coursewareFileRel);
            throw new RuntimeException("课件生成的文件引用创建失败");
        }
    }

    @Override
    public void deleteFileRef(@NonNull Long fileRefId) {

        Long row = coursewareFileRelMapper.deleteById(fileRefId);
        if (row == null || row == 0) {
            log.error("课件生成的文件引用 {} 删除失败", fileRefId);
            throw new RuntimeException("课件生成的文件引用删除失败");
        }
    }
}
