package com.superyoudi.businessservice.service.impl;

import com.superyoudi.businessservice.mapper.UserFileMapper;
import com.superyoudi.businessservice.pojo.entity.UserFile;
import com.superyoudi.common.constants.UserFileConstants;
import com.superyoudi.common.dto.UserFileRefDTO;
import com.superyoudi.coursewareservice.mapper.CoursewareFileRelMapper;
import com.superyoudi.coursewareservice.pojo.entity.CoursewareFileRel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * @author Sijayyx
 */
@ExtendWith(MockitoExtension.class)
class CoursewareFileRefServiceImplTest {

    private final Long TEST_COURSEWARE_REC_ID = 1L;
    private final Long TEST_FILE_ID_1 = 100L;
    private final Long TEST_FILE_ID_2 = 101L;
    private final Long TEST_REL_ID_1 = 1000L;
    private final Long TEST_REL_ID_2 = 1001L;
    @Mock
    private CoursewareFileRelMapper coursewareFileRelMapper;
    @Mock
    private UserFileMapper userFileMapper;
    @InjectMocks
    private CoursewareFileRefServiceImpl coursewareFileRefService;
    private CoursewareFileRel testRel1;
    private CoursewareFileRel testRel2;
    private UserFile testFile1;
    private UserFile testFile2;

    @BeforeEach
    void setUp() {
        testRel1 = CoursewareFileRel.builder()
                .id(TEST_REL_ID_1)
                .coursewareRecordId(TEST_COURSEWARE_REC_ID)
                .fileId(TEST_FILE_ID_1)
                .build();

        testRel2 = CoursewareFileRel.builder()
                .id(TEST_REL_ID_2)
                .coursewareRecordId(TEST_COURSEWARE_REC_ID)
                .fileId(TEST_FILE_ID_2)
                .build();

        testFile1 = UserFile.builder()
                .id(TEST_FILE_ID_1)
                .fileName("测试文件1.pdf")
                .fileDesc("测试文件描述1")
                .fileUrl("/files/test1.pdf")
                .type(UserFileConstants.TYPE_PDF)
                .source(UserFileConstants.SOURCE_UPLOAD)
                .size(1024L)
                .build();

        testFile2 = UserFile.builder()
                .id(TEST_FILE_ID_2)
                .fileName("测试文件2.docx")
                .fileDesc("测试文件描述2")
                .fileUrl("/files/test2.docx")
                .type(UserFileConstants.TYPE_DOCX)
                .source(UserFileConstants.SOURCE_GENERATED)
                .size(2048L)
                .build();
    }

    // ========== getRefedFiles 测试 ==========
    @Test
    void getRefedFiles_WhenCoursewareHasFiles_ShouldReturnFileRefDTOList() {
        // Arrange
        List<CoursewareFileRel> rels = Arrays.asList(testRel1, testRel2);
        when(coursewareFileRelMapper.findByCoursewareRecordId(TEST_COURSEWARE_REC_ID))
                .thenReturn(rels);
        when(userFileMapper.findById(TEST_FILE_ID_1)).thenReturn(testFile1);
        when(userFileMapper.findById(TEST_FILE_ID_2)).thenReturn(testFile2);

        // Act
        List<UserFileRefDTO> result = coursewareFileRefService.getRefedFiles(TEST_COURSEWARE_REC_ID);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        UserFileRefDTO dto1 = result.get(0);
        assertEquals(TEST_REL_ID_1, dto1.getFileRefRelId());
        assertEquals(TEST_FILE_ID_1, dto1.getFileId());
        assertEquals("测试文件1.pdf", dto1.getFileName());
        assertEquals("测试文件描述1", dto1.getFileDesc());
        assertEquals("/files/test1.pdf", dto1.getFileUrl());
        assertEquals(UserFileConstants.TYPE_PDF, dto1.getType());

        UserFileRefDTO dto2 = result.get(1);
        assertEquals(TEST_REL_ID_2, dto2.getFileRefRelId());
        assertEquals(TEST_FILE_ID_2, dto2.getFileId());
        assertEquals("测试文件2.docx", dto2.getFileName());
        assertEquals("测试文件描述2", dto2.getFileDesc());
        assertEquals("/files/test2.docx", dto2.getFileUrl());
        assertEquals(UserFileConstants.TYPE_DOCX, dto2.getType());

        verify(coursewareFileRelMapper, times(1))
                .findByCoursewareRecordId(TEST_COURSEWARE_REC_ID);
        verify(userFileMapper, times(1)).findById(TEST_FILE_ID_1);
        verify(userFileMapper, times(1)).findById(TEST_FILE_ID_2);
    }

    @Test
    void getRefedFiles_WhenCoursewareHasNoFiles_ShouldReturnEmptyList() {
        // Arrange
        when(coursewareFileRelMapper.findByCoursewareRecordId(TEST_COURSEWARE_REC_ID))
                .thenReturn(Collections.emptyList());

        // Act
        List<UserFileRefDTO> result = coursewareFileRefService.getRefedFiles(TEST_COURSEWARE_REC_ID);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(coursewareFileRelMapper, times(1))
                .findByCoursewareRecordId(TEST_COURSEWARE_REC_ID);
        verify(userFileMapper, never()).findById(anyLong());
    }

    @Test
    void getRefedFiles_WhenFileNotFound_ShouldFilterOutNull() {
        // Arrange
        List<CoursewareFileRel> rels = Arrays.asList(testRel1, testRel2);
        when(coursewareFileRelMapper.findByCoursewareRecordId(TEST_COURSEWARE_REC_ID))
                .thenReturn(rels);
        when(userFileMapper.findById(TEST_FILE_ID_1)).thenReturn(testFile1);
        when(userFileMapper.findById(TEST_FILE_ID_2)).thenReturn(null);

        // Act
        List<UserFileRefDTO> result = coursewareFileRefService.getRefedFiles(TEST_COURSEWARE_REC_ID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_FILE_ID_1, result.get(0).getFileId());

        verify(coursewareFileRelMapper, times(1))
                .findByCoursewareRecordId(TEST_COURSEWARE_REC_ID);
        verify(userFileMapper, times(1)).findById(TEST_FILE_ID_1);
        verify(userFileMapper, times(1)).findById(TEST_FILE_ID_2);
    }


    // ========== addFileRef 测试 ==========
    @Test
    void addFileRef_WhenSuccess_ShouldInsertRecord() {
        // Arrange
        when(coursewareFileRelMapper.insert(any(CoursewareFileRel.class))).thenReturn(1L);

        // Act
        coursewareFileRefService.addFileRef(TEST_COURSEWARE_REC_ID, TEST_FILE_ID_1);

        // Assert
        ArgumentCaptor<CoursewareFileRel> captor = ArgumentCaptor.forClass(CoursewareFileRel.class);
        verify(coursewareFileRelMapper, times(1)).insert(captor.capture());

        CoursewareFileRel captured = captor.getValue();
        assertNull(captured.getId()); // 新插入时ID为null
        assertEquals(TEST_COURSEWARE_REC_ID, captured.getCoursewareRecordId());
        assertEquals(TEST_FILE_ID_1, captured.getFileId());
    }

    @Test
    void addFileRef_WhenInsertFails_ShouldThrowException() {
        // Arrange
        when(coursewareFileRelMapper.insert(any(CoursewareFileRel.class))).thenReturn(0L);

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> coursewareFileRefService.addFileRef(TEST_COURSEWARE_REC_ID, TEST_FILE_ID_1)
        );
        assertEquals("课件生成的文件引用创建失败", exception.getMessage());
        verify(coursewareFileRelMapper, times(1)).insert(any(CoursewareFileRel.class));
    }

    @Test
    void addFileRef_WhenInsertReturnsNull_ShouldThrowException() {
        // Arrange
        when(coursewareFileRelMapper.insert(any(CoursewareFileRel.class))).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> coursewareFileRefService.addFileRef(TEST_COURSEWARE_REC_ID, TEST_FILE_ID_1)
        );
        assertEquals("课件生成的文件引用创建失败", exception.getMessage());
        verify(coursewareFileRelMapper, times(1)).insert(any(CoursewareFileRel.class));
    }


    // ========== deleteFileRef 测试 ==========
    @Test
    void deleteFileRef_WhenSuccess_ShouldDeleteRecord() {
        // Arrange
        when(coursewareFileRelMapper.deleteById(TEST_REL_ID_1)).thenReturn(1L);

        // Act
        coursewareFileRefService.deleteFileRef(TEST_REL_ID_1);

        // Assert
        verify(coursewareFileRelMapper, times(1)).deleteById(TEST_REL_ID_1);
    }

    @Test
    void deleteFileRef_WhenDeleteFails_ShouldThrowException() {
        // Arrange
        when(coursewareFileRelMapper.deleteById(TEST_REL_ID_1)).thenReturn(0L);

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> coursewareFileRefService.deleteFileRef(TEST_REL_ID_1)
        );
        assertEquals("课件生成的文件引用删除失败", exception.getMessage());
        verify(coursewareFileRelMapper, times(1)).deleteById(TEST_REL_ID_1);
    }

    @Test
    void deleteFileRef_WhenDeleteReturnsNull_ShouldThrowException() {
        // Arrange
        when(coursewareFileRelMapper.deleteById(TEST_REL_ID_1)).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> coursewareFileRefService.deleteFileRef(TEST_REL_ID_1)
        );
        assertEquals("课件生成的文件引用删除失败", exception.getMessage());
        verify(coursewareFileRelMapper, times(1)).deleteById(TEST_REL_ID_1);
    }

    // ========== 集成场景测试 ==========
    @Test
    void addAndGetFileRef_ShouldWorkTogether() {
        // Arrange
        when(coursewareFileRelMapper.insert(any(CoursewareFileRel.class))).thenReturn(1L);
        when(coursewareFileRelMapper.findByCoursewareRecordId(TEST_COURSEWARE_REC_ID))
                .thenReturn(Arrays.asList(testRel1));
        when(userFileMapper.findById(TEST_FILE_ID_1)).thenReturn(testFile1);

        // Act
        coursewareFileRefService.addFileRef(TEST_COURSEWARE_REC_ID, TEST_FILE_ID_1);
        List<UserFileRefDTO> result = coursewareFileRefService.getRefedFiles(TEST_COURSEWARE_REC_ID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_FILE_ID_1, result.get(0).getFileId());
        assertEquals("测试文件1.pdf", result.get(0).getFileName());

        verify(coursewareFileRelMapper, times(1)).insert(any(CoursewareFileRel.class));
        verify(coursewareFileRelMapper, times(1))
                .findByCoursewareRecordId(TEST_COURSEWARE_REC_ID);
        verify(userFileMapper, times(1)).findById(TEST_FILE_ID_1);
    }

    @Test
    void deleteFileRef_ShouldRemoveAssociation() {
        // Arrange
        when(coursewareFileRelMapper.deleteById(TEST_REL_ID_1)).thenReturn(1L);

        // Act
        coursewareFileRefService.deleteFileRef(TEST_REL_ID_1);

        // Assert
        verify(coursewareFileRelMapper, times(1)).deleteById(TEST_REL_ID_1);
        // 验证删除后查询应该返回空列表
        when(coursewareFileRelMapper.findByCoursewareRecordId(TEST_COURSEWARE_REC_ID))
                .thenReturn(Collections.emptyList());

        List<UserFileRefDTO> result = coursewareFileRefService.getRefedFiles(TEST_COURSEWARE_REC_ID);
        assertTrue(result.isEmpty());
    }

    @Test
    void addFileRef_MultipleTimes_ShouldInsertMultipleRecords() {
        // Arrange
        when(coursewareFileRelMapper.insert(any(CoursewareFileRel.class))).thenReturn(1L);
        when(coursewareFileRelMapper.findByCoursewareRecordId(TEST_COURSEWARE_REC_ID))
                .thenReturn(Arrays.asList(testRel1, testRel2));
        when(userFileMapper.findById(TEST_FILE_ID_1)).thenReturn(testFile1);
        when(userFileMapper.findById(TEST_FILE_ID_2)).thenReturn(testFile2);

        // Act
        coursewareFileRefService.addFileRef(TEST_COURSEWARE_REC_ID, TEST_FILE_ID_1);
        coursewareFileRefService.addFileRef(TEST_COURSEWARE_REC_ID, TEST_FILE_ID_2);
        List<UserFileRefDTO> result = coursewareFileRefService.getRefedFiles(TEST_COURSEWARE_REC_ID);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(coursewareFileRelMapper, times(2)).insert(any(CoursewareFileRel.class));
    }
}