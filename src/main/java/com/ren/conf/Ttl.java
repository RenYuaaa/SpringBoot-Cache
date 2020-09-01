package com.ren.conf;

import java.lang.annotation.*;

/**
 * @author : renjiahui
 * @date : 2020/8/30 12:38
 * @desc : 自定义注解：SpringBoot-Cache自定义过期时间的注解
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Ttl {

    /**
     * SpringBoot-Cache自定义过期时间
     * @return
     */
    int ttl();

}
