package com.superyoudi.coursewareservice;

import com.superyoudi.coursewareservice.pojo.entity.CoursewareRecord;
import com.superyoudi.coursewareservice.mapper.CoursewareRecordMapper;
import com.superyoudi.coursewareservice.service.CoursewareRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Sijayyx
 */
@ExtendWith(MockitoExtension.class)
class CoursewareRecordServiceTest {

    private final Long TEST_ID = 1L;
    private final Long TEST_PPT_FILE_ID = 100L;
    private final Long TEST_WORD_FILE_ID = 200L;
    private final String TEST_PPT_HTML = "<html><body>PPT Content</body></html>";
    private final String TEST_WORD_HTML = "<html><body>Word Content</body></html>";
    @Mock
    private CoursewareRecordMapper coursewareRecordMapper;
    @InjectMocks
    private CoursewareRecordService coursewareRecordService;
    private CoursewareRecord testRecord;

    @BeforeEach
    void setUp() {
        testRecord = CoursewareRecord.builder()
                .id(TEST_ID)
                .pptHtml(TEST_PPT_HTML)
                .wordHtml(TEST_WORD_HTML)
                .pptFileId(TEST_PPT_FILE_ID)
                .wordFileId(TEST_WORD_FILE_ID)
                .build();
    }

    // ========== findCoursewareRecord 测试 ==========
    @Test
    void findCoursewareRecord_WhenRecordExists_ShouldReturnRecord() {
        // Arrange
        when(coursewareRecordMapper.findById(TEST_ID)).thenReturn(testRecord);

        // Act
        CoursewareRecord result = coursewareRecordService.findCoursewareRecord(TEST_ID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_ID, result.getId());
        assertEquals(TEST_PPT_HTML, result.getPptHtml());
        verify(coursewareRecordMapper, times(1)).findById(TEST_ID);
    }

    @Test
    void findCoursewareRecord_WhenRecordNotExists_ShouldReturnNull() {
        // Arrange
        when(coursewareRecordMapper.findById(TEST_ID)).thenReturn(null);

        // Act
        CoursewareRecord result = coursewareRecordService.findCoursewareRecord(TEST_ID);

        // Assert
        assertNull(result);
        verify(coursewareRecordMapper, times(1)).findById(TEST_ID);
    }

    // ========== saveHtmlColumns 测试 ==========
    @Test
    void saveHtmlColumns_WhenIdIsNull_ShouldInsertNewRecord() {
        // Arrange
        when(coursewareRecordMapper.insert(any(CoursewareRecord.class))).thenReturn(TEST_ID);

        // Act
        Long result = coursewareRecordService.saveHtmlColumns(null, TEST_PPT_HTML, TEST_WORD_HTML);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_ID, result);
        verify(coursewareRecordMapper, never()).findById(any());
        verify(coursewareRecordMapper, times(1)).insert(any(CoursewareRecord.class));
        verify(coursewareRecordMapper, never()).update(any(CoursewareRecord.class));
    }

    @Test
    void saveHtmlColumns_WhenRecordNotExists_ShouldInsertNewRecord() {
        // Arrange
        when(coursewareRecordMapper.findById(TEST_ID)).thenReturn(null);
        when(coursewareRecordMapper.insert(any(CoursewareRecord.class))).thenReturn(TEST_ID);

        // Act
        Long result = coursewareRecordService.saveHtmlColumns(TEST_ID, TEST_PPT_HTML, TEST_WORD_HTML);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_ID, result);
        verify(coursewareRecordMapper, times(1)).findById(TEST_ID);
        verify(coursewareRecordMapper, times(1)).insert(any(CoursewareRecord.class));
        verify(coursewareRecordMapper, never()).update(any(CoursewareRecord.class));
    }

    @Test
    void saveHtmlColumns_WhenRecordExists_ShouldUpdateExistingRecord() {
        // Arrange
        when(coursewareRecordMapper.findById(TEST_ID)).thenReturn(testRecord);
        when(coursewareRecordMapper.update(any(CoursewareRecord.class))).thenReturn(1L);

        // Act
        Long result = coursewareRecordService.saveHtmlColumns(TEST_ID, "New PPT", "New Word");

        // Assert
        assertNotNull(result);
        assertEquals(1L, result);
        verify(coursewareRecordMapper, times(1)).findById(TEST_ID);
        verify(coursewareRecordMapper, never()).insert(any(CoursewareRecord.class));
        verify(coursewareRecordMapper, times(1)).update(any(CoursewareRecord.class));
    }

    @Test
    void saveHtmlColumns_WhenRecordExists_ShouldUpdateWithCorrectValues() {
        // Arrange
        when(coursewareRecordMapper.findById(TEST_ID)).thenReturn(testRecord);
        when(coursewareRecordMapper.update(any(CoursewareRecord.class))).thenReturn(1L);

        // Act
        String newPpt = "Updated PPT";
        String newWord = "Updated Word";
        coursewareRecordService.saveHtmlColumns(TEST_ID, newPpt, newWord);

        // Assert
        verify(coursewareRecordMapper).update(argThat(record ->
                record.getPptHtml().equals(newPpt) &&
                        record.getWordHtml().equals(newWord)
        ));
    }

    // ========== saveFileColumns 测试 ==========
    @Test
    void saveFileColumns_WhenIdIsNull_ShouldInsertNewRecord() {
        // Arrange
        when(coursewareRecordMapper.insert(any(CoursewareRecord.class))).thenReturn(TEST_ID);

        // Act
        Long result = coursewareRecordService.saveFileColumns(null, TEST_PPT_FILE_ID, TEST_WORD_FILE_ID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_ID, result);
        verify(coursewareRecordMapper, never()).findById(any());
        verify(coursewareRecordMapper, times(1)).insert(any(CoursewareRecord.class));
        verify(coursewareRecordMapper, never()).update(any(CoursewareRecord.class));
    }

    @Test
    void saveFileColumns_WhenRecordNotExists_ShouldInsertNewRecord() {
        // Arrange
        when(coursewareRecordMapper.findById(TEST_ID)).thenReturn(null);
        when(coursewareRecordMapper.insert(any(CoursewareRecord.class))).thenReturn(TEST_ID);

        // Act
        Long result = coursewareRecordService.saveFileColumns(TEST_ID, TEST_PPT_FILE_ID, TEST_WORD_FILE_ID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_ID, result);
        verify(coursewareRecordMapper, times(1)).findById(TEST_ID);
        verify(coursewareRecordMapper, times(1)).insert(any(CoursewareRecord.class));
        verify(coursewareRecordMapper, never()).update(any(CoursewareRecord.class));
    }

    @Test
    void saveFileColumns_WhenRecordExists_ShouldUpdateExistingRecord() {
        // Arrange
        when(coursewareRecordMapper.findById(TEST_ID)).thenReturn(testRecord);
        when(coursewareRecordMapper.update(any(CoursewareRecord.class))).thenReturn(1L);

        // Act
        Long newPptFileId = 300L;
        Long newWordFileId = 400L;
        Long result = coursewareRecordService.saveFileColumns(TEST_ID, newPptFileId, newWordFileId);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result);
        verify(coursewareRecordMapper, times(1)).findById(TEST_ID);
        verify(coursewareRecordMapper, never()).insert(any(CoursewareRecord.class));
        verify(coursewareRecordMapper, times(1)).update(any(CoursewareRecord.class));
    }

    @Test
    void saveFileColumns_WhenRecordExists_ShouldUpdateWithCorrectValues() {
        // Arrange
        when(coursewareRecordMapper.findById(TEST_ID)).thenReturn(testRecord);
        when(coursewareRecordMapper.update(any(CoursewareRecord.class))).thenReturn(1L);

        // Act
        Long newPptFileId = 300L;
        Long newWordFileId = 400L;
        coursewareRecordService.saveFileColumns(TEST_ID, newPptFileId, newWordFileId);

        // Assert
        verify(coursewareRecordMapper).update(argThat(record ->
                record.getPptFileId().equals(newPptFileId) &&
                        record.getWordFileId().equals(newWordFileId)
        ));
    }

    // ========== saveCoursewareRecord 测试 ==========
    @Test
    void saveCoursewareRecord_WhenRecordNotExists_ShouldInsertNewRecord() {
        // Arrange
        when(coursewareRecordMapper.findById(TEST_ID)).thenReturn(null);
        when(coursewareRecordMapper.insert(any(CoursewareRecord.class))).thenReturn(TEST_ID);

        // Act
        Long result = coursewareRecordService.saveCoursewareRecord(testRecord);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_ID, result);
        verify(coursewareRecordMapper, times(1)).findById(TEST_ID);
        verify(coursewareRecordMapper, times(1)).insert(testRecord);
        verify(coursewareRecordMapper, never()).update(any(CoursewareRecord.class));
    }

    @Test
    void saveCoursewareRecord_WhenRecordExists_ShouldUpdateExistingRecord() {
        // Arrange
        when(coursewareRecordMapper.findById(TEST_ID)).thenReturn(testRecord);
        when(coursewareRecordMapper.update(any(CoursewareRecord.class))).thenReturn(1L);

        // Act
        CoursewareRecord updatedRecord = CoursewareRecord.builder()
                .id(TEST_ID)
                .pptHtml("Updated HTML")
                .wordHtml("Updated Word")
                .build();
        Long result = coursewareRecordService.saveCoursewareRecord(updatedRecord);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result);
        verify(coursewareRecordMapper, times(1)).findById(TEST_ID);
        verify(coursewareRecordMapper, never()).insert(any(CoursewareRecord.class));
        verify(coursewareRecordMapper, times(1)).update(updatedRecord);
    }

    // ========== getById 测试 ==========
    @Test
    void getById_WhenIdIsNull_ShouldReturnNull() {
        // Act
        CoursewareRecord result = coursewareRecordService.getById(null);

        // Assert
        assertNull(result);
        verify(coursewareRecordMapper, never()).findById(any());
    }

    @Test
    void getById_WhenIdIsNotNull_ShouldCallMapper() {
        // Arrange
        when(coursewareRecordMapper.findById(TEST_ID)).thenReturn(testRecord);

        // Act
        CoursewareRecord result = coursewareRecordService.getById(TEST_ID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_ID, result.getId());
        verify(coursewareRecordMapper, times(1)).findById(TEST_ID);
    }

    // ========== insert 测试 ==========
    @Test
    void insert_ShouldCallMapperInsert() {
        // Arrange
        when(coursewareRecordMapper.insert(testRecord)).thenReturn(TEST_ID);

        // Act
        Long result = coursewareRecordService.insert(testRecord);

        // Assert
        assertEquals(TEST_ID, result);
        verify(coursewareRecordMapper, times(1)).insert(testRecord);
    }

    // ========== update 测试 ==========
    @Test
    void update_ShouldCallMapperUpdate() {
        // Arrange
        when(coursewareRecordMapper.update(testRecord)).thenReturn(1L);

        // Act
        Long result = coursewareRecordService.update(testRecord);

        // Assert
        assertEquals(1L, result);
        verify(coursewareRecordMapper, times(1)).update(testRecord);
    }

    @Test
    void update_WhenRecordNotExists_ShouldReturnZero() {
        // Arrange
        when(coursewareRecordMapper.update(testRecord)).thenReturn(0L);

        // Act
        Long result = coursewareRecordService.update(testRecord);

        // Assert
        assertEquals(0L, result);
        verify(coursewareRecordMapper, times(1)).update(testRecord);
    }

    // ========== deleteById 测试 ==========
    @Test
    void deleteById_ShouldCallMapperDelete() {
        // Arrange
        when(coursewareRecordMapper.deleteById(TEST_ID)).thenReturn(1L);

        // Act
        Long result = coursewareRecordService.deleteById(TEST_ID);

        // Assert
        assertEquals(1L, result);
        verify(coursewareRecordMapper, times(1)).deleteById(TEST_ID);
    }

    @Test
    void deleteById_WhenRecordNotExists_ShouldReturnZero() {
        // Arrange
        when(coursewareRecordMapper.deleteById(TEST_ID)).thenReturn(0L);

        // Act
        Long result = coursewareRecordService.deleteById(TEST_ID);

        // Assert
        assertEquals(0L, result);
        verify(coursewareRecordMapper, times(1)).deleteById(TEST_ID);
    }
}