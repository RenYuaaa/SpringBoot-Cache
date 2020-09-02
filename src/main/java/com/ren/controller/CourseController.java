package com.ren.controller;

import com.ren.beans.Course;
import com.ren.conf.Ttl;
import com.ren.service.CourseService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author : renjiahui
 * @date : 2020/6/27 21:49
 * @desc : 课程的接口层
 */
@Slf4j
@RestController
@CacheConfig(cacheManager = "cacheManager", cacheNames = "course")
public class CourseController {

    @Resource
    private CourseService courseService;
    @Resource
    private CacheManager cacheManager;

    /**
     * 获取课程信息
     * 这里使用@Cacheable注解
     * 这里的unless不建议加上去，可能会造成缓存击穿问题
     *
     * @param id 课程id
     * @return 课程信息
     */
    @ApiOperation(value = "获取课程信息")
    @GetMapping("/api/course/getCourseInfo/{id}")
    @Cacheable(cacheNames = "course:info", keyGenerator = "myKeyGenerator", unless = "#result = null")
    @Ttl(ttl = 1800)
    public Course getCourseInfo(@PathVariable Integer id) {
        log.info("==========请求进来了==========");
        return courseService.getCourseInfo(id);
    }

    /**
     * 修改课程信息
     * 这里使用@CachePut注解
     *
     * @param course
     * @return
     */
    @PostMapping("/api/course")
    @CachePut(key = "#course.id")
    public Course updateCourseInfo(@RequestBody Course course) {
        courseService.updateCourseInfo(course);
        return course;
    }

    /**
     * 删除指定课程信息
     * 这里使用@CacheEvict注解
     *
     * @param id 课程id
     */
    @DeleteMapping("/api/course/{id}")
    @CacheEvict(key = "#id")
    public void deleteCourseInfo(@PathVariable Integer id) {
        courseService.deleteCourseInfo(id);
    }

    /**
     * 获取课程信息
     * 这里使用的是cacheManager的形式进行查缓存的。上面那个查询是使用注解形式进行查询的
     *
     * @param id
     * @return
     */
    @PatchMapping("/course2/{id}")
    public Course course2(@PathVariable Integer id) {

        // 获取指定命名空间的cache
        Cache cache = cacheManager.getCache("course");

        // 通过key获取对应的value
        Cache.ValueWrapper wrapper = cache.get(2);
        if (wrapper == null) {
            Course course = courseService.getCourseInfo(id);

            // 加入缓存
            cache.put(course.getId(), course);
            return course;
        } else {

            // 将缓存的结果返回
            // 因为配置了enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
            // 所以在进行强转的时候不会报错
            return (Course) wrapper.get();
        }
    }
}