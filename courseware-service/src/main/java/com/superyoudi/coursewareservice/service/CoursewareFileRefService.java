package com.superyoudi.coursewareservice.service;

import com.superyoudi.common.dto.UserFileRefDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author Sijayyx
 */
@Service
public interface CoursewareFileRefService {

    List<UserFileRefDTO> getRefedFiles(Long coursewareRecId);

    void addFileRef(Long coursewareRecId, Long fileId);

    void saveFileAndRef(Long coursewareRecId, MultipartFile file, Long userId, String fileDesc, String source);

    void deleteFileRef(Long fileRefId);
}
