package com.superyoudi.businessservice;

import com.superyoudi.businessservice.mapper.CoursesMapper;
import com.superyoudi.businessservice.pojo.entity.Courses;
import com.superyoudi.businessservice.pojo.vo.CoursesVO;
import com.superyoudi.businessservice.service.impl.CourseServiceImpl;
import com.superyoudi.common.exception.CourseNotExistException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

    // Mock Mapper 依赖
    @Mock
    private CoursesMapper coursesMapper;

    // 将mock注入到待测试Service
    @InjectMocks
    private CourseServiceImpl courseService;

    /**
     * 测试：课程存在，正常返回VO
     */
    @Test
    void getCourseInfo_Success() {
        // 1.构造模拟DO数据
        Courses mockCourse = new Courses();
        mockCourse.setId(1l);
        mockCourse.setName("Java基础课程");
        mockCourse.setTeacherName("张老师");

        // 模拟mapper返回数据
        Mockito.when(coursesMapper.selectCourseDetailById(Mockito.anyLong()))
                .thenReturn(mockCourse);

        // 2.执行方法
        CoursesVO vo = courseService.getCourseInfo(1l);

        // 3.断言校验
        Assertions.assertNotNull(vo);
        Assertions.assertEquals(1, vo.getId());
        Assertions.assertEquals("Java基础课程", vo.getName());
        Assertions.assertEquals("张老师", vo.getTeacherName());
    }

    /**
     * 测试：课程不存在，预期抛出异常
     */
    @Test
    void getCourseInfo_NotFound_ThrowException() {
        // mock返回null
        Mockito.when(coursesMapper.selectCourseDetailById(999l))
                .thenReturn(null);

        // 断言抛出指定异常
        CourseNotExistException exception = Assertions.assertThrows(CourseNotExistException.class, () -> {
            courseService.getCourseInfo(999l);
        });
        Assertions.assertEquals("课程不存在", exception.getMessage());
    }
}
