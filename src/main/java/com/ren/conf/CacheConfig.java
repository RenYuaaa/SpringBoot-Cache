package com.ren.conf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : renjiahui
 * @date : 2020/8/30 12:49
 * @desc : 该配置文件大部分使用SpringBoot默认配置, 仅加入了有期限缓存的键
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Autowired
    ResourceLoader resourceLoader;

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        return RedisCacheManager.builder(redisConnectionFactory)
                //默认的缓存配置(没有配置键的key均使用此配置)
                .cacheDefaults(getDefaultCacheConfiguration())
                .withInitialCacheConfigurations(getCacheConfigurations())
                //在spring事务正常提交时才缓存数据
                .transactionAware()
                .build();
    }

    private Map<String, RedisCacheConfiguration> getCacheConfigurations() {
        Map<String, RedisCacheConfiguration> configurationMap = new HashMap<>(16);

        //缓存键,自定义过期时间
        CacheRegister.CACHE_TTL_MAP.forEach((cacheName, ttl) -> configurationMap.put(cacheName, this.getDefaultCacheConfiguration(Duration.ofSeconds(ttl))));

        return configurationMap;
    }


    /**
     * 获取redis的缓存配置(针对于键)
     *
     * @param ttl 键过期时间
     * @return
     */
    private RedisCacheConfiguration getDefaultCacheConfiguration(Duration ttl) {
        // 获取Redis缓存配置,此处获取的为默认配置
        // 设置键过期的时间,用 java.time 下的Duration表示持续时间,进入entryTtl()方法的源码中可看到
        // 当设置为 0 即 Duration.ZERO 时表示键无过期时间,其也是默认配置
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .serializeValuesWith(RedisSerializationContext
                        .SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }

    /**
     * 获取Redis缓存配置,此处获取的为默认配置
     * 如对键值序列化方式,是否缓存null值,是否使用前缀等有特殊要求
     * 可另行调用 RedisCacheConfiguration 的构造方法
     *
     * @return
     */
    private RedisCacheConfiguration getDefaultCacheConfiguration() {
        // 注意此构造函数为 spring-data-redis-2.1.9 及以上拥有,经试验 已知spring-data-redis-2.0.9及以下版本没有此构造函数
        // 但观察源码可得核心不过是在值序列化器(valueSerializationPair)的构造中注入 ClassLoader 即可
        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext
                        .SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }

}