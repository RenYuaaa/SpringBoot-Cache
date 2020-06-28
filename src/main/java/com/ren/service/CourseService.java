package com.ren.service;

import com.ren.beans.Course;

/**
 * @author : renjiahui
 * @date : 2020/6/27 21:49
 * @desc : 课程的业务层
 */
public interface CourseService {

    /**
     * 获取课程信息
     *
     * @param id 课程id
     * @return 课程信息
     */
    Course getCourseInfo(Integer id);

    /**
     * 修改课程信息
     *
     * @param course 要修改的信息
     */
    void updateCourseInfo(Course course);

    /**
     * 删除课程信息
     *
     * @param id 课程id
     */
    void deleteCourseInfo(Integer id);
}
