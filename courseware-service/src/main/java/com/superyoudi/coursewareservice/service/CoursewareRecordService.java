package com.superyoudi.coursewareservice.service;

import com.superyoudi.coursewareservice.mapper.CoursewareRecordMapper;
import com.superyoudi.coursewareservice.pojo.entity.CoursewareRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author Sijayyx
 */
@Service
@RequiredArgsConstructor
public class CoursewareRecordService {

    private final CoursewareRecordMapper coursewareRecordMapper;

    public CoursewareRecord findCoursewareRecord(Long id) {
        return coursewareRecordMapper.findById(id);
    }

    public Long saveHtmlColumns(Long id, String ppt, String word) {
        CoursewareRecord cr = getById(id);
        if (cr == null || id == null) {
            return insert(CoursewareRecord.builder()
                    .pptHtml(ppt).wordHtml(word)
                    .build());
        }

        cr.setPptHtml(ppt);
        cr.setWordHtml(word);
        return update(cr);
    }

    public Long saveFileColumns(Long id, Long pptFileId, Long wordFileId) {
        CoursewareRecord cr = getById(id);
        if (cr == null || id == null) {
            return insert(CoursewareRecord.builder()
                    .pptFileId(pptFileId).wordFileId(wordFileId)
                    .build());
        }

        cr.setPptFileId(pptFileId);
        cr.setWordFileId(wordFileId);
        return update(cr);
    }


    public Long saveCoursewareRecord(CoursewareRecord coursewareRecord) {
        // 存在就更新，不存在就创建
        CoursewareRecord cr = getById(coursewareRecord.getId());
        if (cr == null) {
            return insert(coursewareRecord);
        }
        return update(coursewareRecord);
    }


    public CoursewareRecord getById(Long id) {
        if (id == null) return null;
        return coursewareRecordMapper.findById(id);
    }

    public Long insert(CoursewareRecord record) {
        coursewareRecordMapper.insert(record);
        return record.getId();
    }

    public Long update(CoursewareRecord record) {
        return coursewareRecordMapper.update(record);
    }

    public Long deleteById(Long id) {
        return coursewareRecordMapper.deleteById(id);
    }

}
