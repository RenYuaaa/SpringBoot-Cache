package com.ren;

import com.ren.conf.CacheScan;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author : renjiahui
 * @date : 2020/8/30 12:49
 * @desc : 启动类
 */
@SpringBootApplication
@MapperScan("com.ren.dao")
@EnableSwagger2
@CacheScan(basePackages = {"com.ren.controller"})
public class SpringBootCacheApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootCacheApplication.class, args);
    }

}
