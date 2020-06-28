package com.ren.conf;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * @auther renjiahui
 * @date 2020/6/27 20:46
 * @desc springboot-cache的配置
 */
@EnableCaching
@SpringBootConfiguration
public class CacheConfig {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Bean
    public RedisCacheManager cacheManager() {
        StringRedisSerializer redisSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Object>(Object.class);

        ObjectMapper om = new ObjectMapper();
        // 防止在序列化的过程中丢失对象的属性
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 开启实体类和json的类型转换
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);

        // 配置序列化（解决乱码的问题）
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                // key的序列化方式
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer))
                // value的序列化方式
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer))
                // 不缓存空值(但是这里如果不缓存空值可能会引起缓存击穿问题)
                .disableCachingNullValues()
                // 10分钟过期
                .entryTtl(Duration.ofMinutes(10))
                ;
        RedisCacheManager cacheManager = RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(config)
                .build();
        return cacheManager;

    }

    /**
     * 自定义key的生成策略
     * 如果配置了keyGenerator。在进行缓存的时候，如果没有指定key，则会使用keyGenerator；如果指定了key，则优先使用key
     * @return
     */
    @Bean
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuffer key = new StringBuffer();
            key.append(target.getClass().getSimpleName() + "#" + method.getName() + "(");
            for (Object args : params) {
                key.append(args + ",");
            }
            key.deleteCharAt(key.length() - 1);
            key.append(")");
            return key.toString();
        };
    }
}
