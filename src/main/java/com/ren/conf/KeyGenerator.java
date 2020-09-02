package com.ren.conf;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author : renjiahui
 * @date : 2020/8/30 12:58
 * @desc : 自定义key的生成策略
 */
@Configuration
class CacheKeyGenerator {

    /**
     * 自定义key的生成策略
     * 如果配置了keyGenerator。在进行缓存的时候，如果没有指定key，则会使用keyGenerator；如果指定了key，则优先使用key
     *
     * @return
     */
    @Bean("myKeyGenerator")
    public KeyGenerator keyGenerator() {

        return new KeyGenerator() {
            @Override
            public Object generate(Object o, Method method, Object... params) {
                return method.getName() + "[" + Arrays.asList(params) + "]";
            }
        };
    }
}
