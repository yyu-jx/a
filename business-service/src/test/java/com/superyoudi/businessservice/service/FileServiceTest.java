package com.superyoudi.businessservice.service;

import com.superyoudi.businessservice.mapper.UserFileMapper;
import com.superyoudi.businessservice.pojo.entity.UserFile;
import com.superyoudi.common.constants.UserFileConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_FILE_ID = 100L;
    private static final String TEST_FILE_DESC = "测试文件描述";
    private static final String TEST_ORIGINAL_NAME = "test.pdf";
    @TempDir
    Path tempDir;
    @Mock
    private UserFileMapper userFileMapper;
    @InjectMocks
    private FileService fileService;
    @Mock
    private MultipartFile multipartFile;

    @BeforeEach
    void setUp() {
        // 设置上传和导出路径为临时目录
        ReflectionTestUtils.setField(fileService, "uploadPath", tempDir.resolve("uploads").toString());
        ReflectionTestUtils.setField(fileService, "exportPath", tempDir.resolve("exports").toString());

        // 创建目录
        try {
            Files.createDirectories(tempDir.resolve("uploads"));
            Files.createDirectories(tempDir.resolve("exports"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void deleteUserFile_ShouldDeleteBothFileAndRecord_WhenFileExists() throws IOException {
        // Given
        Path testFile = tempDir.resolve("uploads").resolve("test.pdf");
        Files.createFile(testFile);

        // 使用文件系统的绝对路径，而不是URI
        String filePath = testFile.toAbsolutePath().toString();

        UserFile userFile = UserFile.builder()
                .id(TEST_FILE_ID)
                .userId(TEST_USER_ID)
                .fileName("test.pdf")
                .fileUrl(filePath)  // 使用文件系统路径
                .build();

        when(userFileMapper.findById(TEST_FILE_ID)).thenReturn(userFile);
        when(userFileMapper.deleteById(TEST_FILE_ID)).thenReturn(1L);

        // When
        fileService.deleteUserFile(TEST_FILE_ID);

        // Then
        verify(userFileMapper).findById(TEST_FILE_ID);
        verify(userFileMapper).deleteById(TEST_FILE_ID);
        assertThat(Files.exists(testFile)).isFalse();
    }

    @Test
    void deleteUserFile_ShouldDeleteRecordOnly_WhenFileNotExists() {
        // Given
        String nonExistentPath = tempDir.resolve("uploads").resolve("nonexistent.pdf").toAbsolutePath().toString();

        UserFile userFile = UserFile.builder()
                .id(TEST_FILE_ID)
                .userId(TEST_USER_ID)
                .fileName("test.pdf")
                .fileUrl(nonExistentPath)  // 使用不存在的文件路径
                .build();

        when(userFileMapper.findById(TEST_FILE_ID)).thenReturn(userFile);
        when(userFileMapper.deleteById(TEST_FILE_ID)).thenReturn(1L);

        // When
        fileService.deleteUserFile(TEST_FILE_ID);

        // Then
        verify(userFileMapper).findById(TEST_FILE_ID);
        verify(userFileMapper).deleteById(TEST_FILE_ID);
    }

    @Test
    void deleteUserFile_ShouldHandleIOException_WhenDeleteFails() throws IOException {
        // Given
        Path testFile = tempDir.resolve("uploads").resolve("protected.pdf");
        Files.createFile(testFile);
        String filePath = testFile.toAbsolutePath().toString();

        UserFile userFile = UserFile.builder()
                .id(TEST_FILE_ID)
                .userId(TEST_USER_ID)
                .fileName("protected.pdf")
                .fileUrl(filePath)
                .build();

        when(userFileMapper.findById(TEST_FILE_ID)).thenReturn(userFile);
        when(userFileMapper.deleteById(TEST_FILE_ID)).thenReturn(1L);

        // 在Windows上，将文件设置为只读可能导致删除失败
        if (testFile.toFile().setReadOnly()) {
            // When
            fileService.deleteUserFile(TEST_FILE_ID);

            // Then - 即使物理文件删除失败，记录也应该被删除
            verify(userFileMapper).findById(TEST_FILE_ID);
            verify(userFileMapper).deleteById(TEST_FILE_ID);
        }
    }

    @Test
    void deleteUserFile_ShouldContinueDeletingRecord_WhenPhysicalFileDoesNotExist() {
        // Given
        String nonExistentPath = tempDir.resolve("uploads").resolve("notexists.pdf").toAbsolutePath().toString();

        UserFile userFile = UserFile.builder()
                .id(TEST_FILE_ID)
                .userId(TEST_USER_ID)
                .fileName("test.pdf")
                .fileUrl(nonExistentPath)
                .build();

        when(userFileMapper.findById(TEST_FILE_ID)).thenReturn(userFile);
        when(userFileMapper.deleteById(TEST_FILE_ID)).thenReturn(1L);

        // When
        fileService.deleteUserFile(TEST_FILE_ID);

        // Then - 记录被删除，即使物理文件不存在
        verify(userFileMapper).findById(TEST_FILE_ID);
        verify(userFileMapper).deleteById(TEST_FILE_ID);
    }

    @Test
    void saveUserFile_ShouldGenerateUniqueFileName() throws IOException {
        // Given
        when(multipartFile.getOriginalFilename()).thenReturn(TEST_ORIGINAL_NAME);
        when(multipartFile.getSize()).thenReturn(1024L);
        doNothing().when(multipartFile).transferTo(any(Path.class));

        ArgumentCaptor<Path> pathCaptor = ArgumentCaptor.forClass(Path.class);
        when(userFileMapper.insert(any(UserFile.class))).thenReturn(1L);

        // When
        fileService.saveUserFile(multipartFile, TEST_USER_ID, TEST_FILE_DESC, UserFileConstants.SOURCE_UPLOAD);

        // Then
        verify(multipartFile).transferTo(pathCaptor.capture());
        Path savedPath = pathCaptor.getValue();
        String fileName = savedPath.getFileName().toString();

        // 验证文件名包含时间戳和UUID
        assertThat(fileName).matches("\\d+_\\w+-\\w+-\\w+-\\w+-\\w+\\.pdf");
    }

    @Test
    void saveUserFile_ShouldSuccess_WhenUploadSource() throws IOException {
        // Given
        String source = UserFileConstants.SOURCE_UPLOAD;
        when(multipartFile.getOriginalFilename()).thenReturn(TEST_ORIGINAL_NAME);
        when(multipartFile.getSize()).thenReturn(1024L);
        doNothing().when(multipartFile).transferTo(any(Path.class));

        ArgumentCaptor<UserFile> userFileCaptor = ArgumentCaptor.forClass(UserFile.class);
        when(userFileMapper.insert(userFileCaptor.capture())).thenReturn(1L);

        // When
        fileService.saveUserFile(multipartFile, TEST_USER_ID, TEST_FILE_DESC, source);

        // Then
        verify(multipartFile).transferTo(any(Path.class));
        verify(userFileMapper).insert(any(UserFile.class));

        UserFile savedFile = userFileCaptor.getValue();
        assertThat(savedFile.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(savedFile.getFileName()).isEqualTo(TEST_ORIGINAL_NAME);
        assertThat(savedFile.getFileDesc()).isEqualTo(TEST_FILE_DESC);
        assertThat(savedFile.getSize()).isEqualTo(1024L);
        assertThat(savedFile.getType()).isEqualTo(UserFileConstants.TYPE_PDF);
        assertThat(savedFile.getSource()).isEqualTo(source);
        assertThat(savedFile.getFileUrl()).isNotNull();
    }

    @Test
    void saveUserFile_ShouldSuccess_WhenGeneratedSource() throws IOException {
        // Given
        String source = UserFileConstants.SOURCE_GENERATED;
        when(multipartFile.getOriginalFilename()).thenReturn(TEST_ORIGINAL_NAME);
        when(multipartFile.getSize()).thenReturn(1024L);
        doNothing().when(multipartFile).transferTo(any(Path.class));
        when(userFileMapper.insert(any(UserFile.class))).thenReturn(1L);

        // When
        fileService.saveUserFile(multipartFile, TEST_USER_ID, TEST_FILE_DESC, source);

        // Then
        verify(multipartFile).transferTo(any(Path.class));
        verify(userFileMapper).insert(any(UserFile.class));
    }

    @Test
    void saveUserFile_ShouldThrowException_WhenFileIsNull() {
        // When & Then
        assertThatThrownBy(() -> fileService.saveUserFile(null, TEST_USER_ID, TEST_FILE_DESC, UserFileConstants.SOURCE_UPLOAD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("文件不能为null");
    }

    @Test
    void saveUserFile_ShouldThrowException_WhenUserIdIsNull() {
        // When & Then
        assertThatThrownBy(() -> fileService.saveUserFile(multipartFile, null, TEST_FILE_DESC, UserFileConstants.SOURCE_UPLOAD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId不能为null");
    }

    @Test
    void saveUserFile_ShouldThrowException_WhenSourceIsNull() {
        // When & Then
        assertThatThrownBy(() -> fileService.saveUserFile(multipartFile, TEST_USER_ID, TEST_FILE_DESC, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("文件来源不能为null");
    }

    @Test
    void saveUserFile_ShouldThrowException_WhenSourceIsInvalid() {
        // When & Then
        assertThatThrownBy(() -> fileService.saveUserFile(multipartFile, TEST_USER_ID, TEST_FILE_DESC, "invalid_source"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("文件来源不合法");
    }

    @Test
    void saveUserFile_ShouldHandleIOException_WhenTransferFails() throws IOException {
        // Given
        when(multipartFile.getOriginalFilename()).thenReturn(TEST_ORIGINAL_NAME);
        doThrow(new IOException("磁盘空间不足")).when(multipartFile).transferTo(any(Path.class));

        // When & Then
        assertThatThrownBy(() -> fileService.saveUserFile(multipartFile, TEST_USER_ID, TEST_FILE_DESC, UserFileConstants.SOURCE_UPLOAD))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("文件存放失败");

        verify(userFileMapper, never()).insert(any(UserFile.class));
    }

    @Test
    void saveUserFile_ShouldThrowException_WhenInsertFails() throws IOException {
        // Given
        when(multipartFile.getOriginalFilename()).thenReturn(TEST_ORIGINAL_NAME);
        when(multipartFile.getSize()).thenReturn(1024L);
        doNothing().when(multipartFile).transferTo(any(Path.class));
        when(userFileMapper.insert(any(UserFile.class))).thenReturn(0L);

        // When & Then
        assertThatThrownBy(() -> fileService.saveUserFile(multipartFile, TEST_USER_ID, TEST_FILE_DESC, UserFileConstants.SOURCE_UPLOAD))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("文件保存失败");
    }

    @Test
    void saveUserFile_ShouldHandleFileWithoutExtension() throws IOException {
        // Given
        String fileNameWithoutExt = "testfile";
        when(multipartFile.getOriginalFilename()).thenReturn(fileNameWithoutExt);
        when(multipartFile.getSize()).thenReturn(1024L);
        doNothing().when(multipartFile).transferTo(any(Path.class));

        ArgumentCaptor<UserFile> userFileCaptor = ArgumentCaptor.forClass(UserFile.class);
        when(userFileMapper.insert(userFileCaptor.capture())).thenReturn(1L);

        // When
        fileService.saveUserFile(multipartFile, TEST_USER_ID, TEST_FILE_DESC, UserFileConstants.SOURCE_UPLOAD);

        // Then
        UserFile savedFile = userFileCaptor.getValue();
        assertThat(savedFile.getType()).isEqualTo(UserFileConstants.TYPE_OTHER);
    }

    @Test
    void saveUserFile_ShouldHandleNullOriginalFilename() throws IOException {
        // Given
        when(multipartFile.getOriginalFilename()).thenReturn(null);
        when(multipartFile.getSize()).thenReturn(1024L);
        doNothing().when(multipartFile).transferTo(any(Path.class));

        ArgumentCaptor<UserFile> userFileCaptor = ArgumentCaptor.forClass(UserFile.class);
        when(userFileMapper.insert(userFileCaptor.capture())).thenReturn(1L);

        // When
        fileService.saveUserFile(multipartFile, TEST_USER_ID, TEST_FILE_DESC, UserFileConstants.SOURCE_UPLOAD);

        // Then
        UserFile savedFile = userFileCaptor.getValue();
        assertThat(savedFile.getType()).isEqualTo(UserFileConstants.TYPE_OTHER);
    }


    @Test
    void getUserFileList_ShouldReturnFiles_WhenUserExists() {
        // Given
        List<UserFile> expectedFiles = Arrays.asList(
                UserFile.builder().userId(TEST_USER_ID).fileName("file1.pdf").build(),
                UserFile.builder().userId(TEST_USER_ID).fileName("file2.docx").build()
        );
        when(userFileMapper.findByUserId(TEST_USER_ID)).thenReturn(expectedFiles);

        // When
        List<UserFile> result = fileService.getUserFileList(TEST_USER_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(expectedFiles);
        verify(userFileMapper).findByUserId(TEST_USER_ID);
    }

    @Test
    void getUserFileList_ShouldReturnEmptyList_WhenUserHasNoFiles() {
        // Given
        when(userFileMapper.findByUserId(TEST_USER_ID)).thenReturn(List.of());

        // When
        List<UserFile> result = fileService.getUserFileList(TEST_USER_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(userFileMapper).findByUserId(TEST_USER_ID);
    }

    @Test
    void getUserFileList_ShouldThrowException_WhenUserIdIsNull() {
        // When & Then
        assertThatThrownBy(() -> fileService.getUserFileList(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId不能为null");
    }


    @Test
    void deleteUserFile_ShouldLogWarning_WhenFileNotFound() {
        // Given
        when(userFileMapper.findById(TEST_FILE_ID)).thenReturn(null);

        // When
        fileService.deleteUserFile(TEST_FILE_ID);

        // Then
        verify(userFileMapper).findById(TEST_FILE_ID);
        verify(userFileMapper, never()).deleteById(anyLong());
    }

    @Test
    void deleteUserFile_ShouldThrowException_WhenFileIdIsNull() {
        // When & Then
        assertThatThrownBy(() -> fileService.deleteUserFile(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fileId不能为null");
    }


}