package com.ren.conf;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author : renjiahui
 * @date : 2020/8/30 12:44
 * @desc : 自定义注解：SpringBoot-Cache启动类扫描用注解
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({CacheRegister.class})
public @interface CacheScan {// 启动类上面@CacheScan(basePackages={"扫描的类路径"}) 类似@ComponentScan(basePackages = {"com.migu.*"})

    String[] basePackages() default {};
}
