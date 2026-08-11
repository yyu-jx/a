package com.superyoudi.businessservice.service.impl;

import com.superyoudi.businessservice.mapper.StudentAnswerMapper;
import com.superyoudi.businessservice.mapper.StudentSubmissionMapper;
import com.superyoudi.businessservice.pojo.entity.StudentAnswer;
import com.superyoudi.businessservice.pojo.entity.StudentSubmission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AnswerHelpRecordService {

    private final StudentSubmissionMapper studentSubmissionMapper;
    private final StudentAnswerMapper studentAnswerMapper;

    @Transactional
    public void recordHelpCount(Long studentId, Long questionId, Long taskId) {
        if (studentId == null || questionId == null || taskId == null) {
            return;
        }

        StudentSubmission studentSubmission = studentSubmissionMapper.selectByTaskAndStudentId(taskId, studentId);

        Long submissionId;
        if (studentSubmission == null) {
            StudentSubmission draft = new StudentSubmission();
            LocalDateTime now = LocalDateTime.now();
            draft.setTaskId(taskId);
            draft.setStudentId(studentId);
            draft.setStatus("draft");
            draft.setCreatedAt(now);
            draft.setSubmittedTime(now);
            studentSubmissionMapper.insert(draft);
            submissionId = draft.getId();
        } else {
            submissionId = studentSubmission.getId();
        }

        StudentAnswer answerDO = studentAnswerMapper.selectAnswerBySubQid(submissionId, questionId);
        if (answerDO != null) {
            studentAnswerMapper.incrementHelpCount(answerDO.getId());
        } else {
            studentAnswerMapper.insertEmptyAnswerRecord(submissionId, questionId, 1L);
        }
    }
}
