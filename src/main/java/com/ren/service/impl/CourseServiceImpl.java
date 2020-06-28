package com.ren.service.impl;

import com.ren.beans.Course;
import com.ren.dao.CourseDao;
import com.ren.service.CourseService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author : renjiahui
 * @date : 2020/6/27 21:49
 * @desc : 课程的业务层
 */
@Service
public class CourseServiceImpl implements CourseService {

    @Resource
    private CourseDao courseDao;

    @Override
    public Course getCourseInfo(Integer id) {
        return courseDao.getCourseInfo(id);
    }

    @Override
    public void updateCourseInfo(Course course) {
        courseDao.updateCourseInfo(course);
    }

    @Override
    public void deleteCourseInfo(Integer id) {
        courseDao.deleteCourseInfo(id);
    }
}
