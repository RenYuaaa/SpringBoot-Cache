# Spring Boot Cache


## 一、介绍

spring cache 是spring3版本之后引入的一项技术，可以简化对于缓存层的操作，spring cache与springcloud stream类似，都是基于抽象层，可以任意切换其实现。
    
其核心是``CacheManager``、``Cache``这两个接口，所有由spring整合的cache都要实现这两个接口、Redis的实现类则是 ``RedisCache ``  和 ``RedisManager``



## 二、使用

### Ⅰ、查询

1. 需要导入的依赖

```pom
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

2. 编写对于cache的配置

```java
@EnableCaching
@SpringBootConfiguration
public class CacheConfig {

    @Autowired
    private RedisConnectionFactory connectionFactory;

    // 如果有多个CacheManager的话需要使用@Primary直接指定那个是默认的
    @Bean 
    public RedisCacheManager cacheManager() {
        RedisSerializer<String> redisSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);

        ObjectMapper om = new ObjectMapper();
        // 防止在序列化的过程中丢失对象的属性
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 开启实体类和json的类型转换
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);

        // 配置序列化（解决乱码的问题）
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()   .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer))             .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer))
                // 不缓存空值(不缓存空值可能会引起缓存击穿问题)
                .disableCachingNullValues()
                // 1分钟过期
                .entryTtl(Duration.ofMinutes(1))
                ;
        RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
        return cacheManager;
    }
}
```

进行以上配置即可使用springboot cache了，还有一个key的生成策略的配置（可选）

```java
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
```

> 注意：如果配置了``KeyGenerator`` ，在进行缓存的时候如果不指定key的话，最后会把生成的key缓存起来，如果同时配置了``KeyGenerator`` 和key则优先使用key。

3. 在controller或者service的类上面添加``@CacheConfig``，注解里面的参数详情见下表：

   | 参数名        | 参数值                                               | 作用                                                |
   | ------------- | ---------------------------------------------------- | --------------------------------------------------- |
   | cacheNames    | 可以随意填写，一般是一个模块或者一个很重要的功能名称 | 无具体作用，只是用来区分缓存，方便管理              |
   | keyGenerator  | 就是自己配置的KeyGenerator的名称                     | 全局key都会以他的策略去生成                         |
   | cacheManager  | 自己配置的CacheManager                               | 用来操作Cache对象的，很多对于缓存的配置也由他去管理 |
   | cacheResolver |                                                      |                                                     |

4. 在标有@CacheConfig的类里面编写一个查询单个对象的方法并添加``@Cacheable``注解

   ```java
   @Cacheable(key = "#id", unless = "#result == null") 
   @PatchMapping("/course/{id}")
   public Course courseInfo(@PathVariable Integer id) {
       log.info("进来了 .. ");
       return courseService.getCourseInfo(id);
   }
   ```
   执行完该方法后，执行结果将会被缓存到Redis
   
   @Cacheable注解中参数详情见下表：   

| 参数名       | 作用                                                         |
| ------------ | ------------------------------------------------------------ |
| cacheNames   |                                                              |
| key          | 这里的key的优先级是最高的，可以覆盖掉全局配置的key，如果不配置的话使用的就是全局的key |
| keyGenerator |                                                              |
| cacheManager |                                                              |
| condition    | 满足什么条件会进行缓存，里面可以写简单的表达式进行逻辑判断   |
| unless       | 满足什么条件不进行缓存，里面可以写简单的表达式进行逻辑判断   |
| sync         | 加入缓存的这个操作是否是同步的                               |

### Ⅱ、 修改

   1. 编写一个修改的方法，参数传对象，返回值也改成这个对象

      ```java
      @PutMapping("/course")
      public Course modifyCoruse(@RequestBody Course course) {
          courseService.updateCourse(course);
          return course;
      }
      ```
      
   2. 在方法上面添加 ``@CachePut(key = "#course.id")`` 注解，这个注解表示将方法的返回值更新到缓存中，注解中的参数和 ``@Cacheable ``  中的一样，这里就略过了。

### Ⅲ、 删除

1. 编写删除方法，在方法上添加``@CacheEvict`` 注解

   ```java
   @CacheEvict(key = "#id")
   @DeleteMapping("/course/{id}")
   public void removeCourse(@PathVariable Integer id) {
       courseService.remove(id);
   }
   ```

   ``CacheEvict``  的参数信息见下表：

| 参数名           | 描述                                                         |
| ---------------- | ------------------------------------------------------------ |
| allEntries       | 是否删除该命名空间下面的全部缓存，默认是false                |
| beforeInvocation | 在执行删除方法前就执行清空缓存操作，默认是false，如果删除方法执行报错该注解则不执行 |



## 三、 基于代码的cache的使用

因为我们有配置的CacheManager，所以可以利用RedisCacheManager对象去手动操作cache，首先将CacheManager注入进来：

```java
@Resource 
private CacheManager cacheManager;

@PatchMapping("/course2/{id}")
public Course course2(@PathVariable Integer id) {
    // 获取指定命名空间的cache
    Cache cache = cacheManager.getCache("course");
    // 通过key获取对应的value
    Cache.ValueWrapper wrapper = cache.get(2);
    if (wrapper == null) {
        // 查询数据库
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
```

> 如果还看不明白，请去GitHub拉取源码 https://github.com/RenYuaaa/SpringBoot-Cache.git
