package com.ren.dao;

import com.ren.beans.Course;
import io.lettuce.core.dynamic.annotation.Param;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.UpdateProvider;
import org.springframework.stereotype.Repository;

/**
 * @author : renjiahui
 * @date : 2020/6/27 22:12
 * @desc : 课程的相关操作
 */
@Repository
public interface CourseDao {


    @Select("select * from course where id = #{id}")
    Course getCourseInfo(@Param("id") Integer id);

    @UpdateProvider(type = CourseProvider.class, method = "updateCourseInfo")
    void updateCourseInfo(Course course);

    @Delete("delete from course where id = #{id} ")
    void deleteCourseInfo(@Param("id") Integer id);


    class CourseProvider {
        public String updateCourseInfo(Course course) {
            StringBuilder sb = new StringBuilder();
            sb.append("update course set ");
            if (course.getName() != null) {
                sb.append(" name = #{name},");
            }

            if (course.getOrgId() != null) {
                sb.append(" org_id = #{orgId},");
            }

            if (course.getStatus() != null) {
                sb.append(" status = #{status},");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append("where id = #{id}");
            return sb.toString();
        }
    }
}
