package com.ren.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author : renjiahui
 * @date : 2020/8/30 12:49
 * @desc : 注册类
 */
public class CacheRegister implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, BeanClassLoaderAware, EnvironmentAware {

    Logger LOG = LoggerFactory.getLogger(CacheRegister.class);

    public static Map<String, Integer> Ttl = new HashMap<>();
    public  void setSectionMap(Map<String, Integer> sectionMap) {
        Ttl = sectionMap;
    }

    private ResourceLoader resourceLoader;

    private ClassLoader classLoader;

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;

    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;

    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        logPackageScan(importingClassMetadata);
        registerSections(importingClassMetadata, registry);

    }

    private void logPackageScan(AnnotationMetadata metadata) {
        Map<String, Object> defaultAttrs = metadata.getAnnotationAttributes(CacheScan.class.getName(), true);
        if (defaultAttrs != null && defaultAttrs.size() > 0) {
            LOG.info("Cache package scan: " + buildPackages((String[]) defaultAttrs.get("basePackages")));
        }
    }

    private String buildPackages(String[] basePackages) {
        if (basePackages == null || basePackages.length == 0) {
            return "null";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : basePackages) {
            stringBuilder.append(s).append(",");
        }
        stringBuilder.substring(0, stringBuilder.length() - 2);
        return stringBuilder.toString();
    }

    public void registerSections(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        ClassPathScanningCandidateComponentProvider scanner = getScanner();
        scanner.setResourceLoader(this.resourceLoader);
        Set<String> basePackages;
        Map<String, Object> attrs = metadata.getAnnotationAttributes(CacheScan.class.getName());
        AnnotationTypeFilter annotationTypeFilter = new AnnotationTypeFilter(Ttl.class);
        scanner.addIncludeFilter(annotationTypeFilter);
        basePackages = getBasePackages(metadata);

        Map<String, Integer> ttlMap = new HashMap<>();

        for (String basePackage : basePackages) {

            Set<BeanDefinition> candidates = new LinkedHashSet<BeanDefinition>();

            ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

            try {
                // 这里特别注意一下类路径必须这样写
                // 获取指定包下的所有类
                basePackage = basePackage.replace(".", "/");

                Resource[] resources = resourcePatternResolver.getResources("classpath*:" + basePackage + "/**/*.class");

                MetadataReaderFactory metadata1 = new SimpleMetadataReaderFactory();
                for (Resource resource : resources) {
                    MetadataReader metadataReader = metadata1.getMetadataReader(resource);
                    ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(metadataReader);
                    sbd.setResource(resource);
                    sbd.setSource(resource);
                    candidates.add(sbd);
                }
                for (BeanDefinition beanDefinition : candidates) {
                    String classname = beanDefinition.getBeanClassName();
                    // 扫描Ttl注解和Cacheable注解
                    Method[] methods = Class.forName(classname).getMethods();
                    for (Method method : methods) {
                        Ttl ttl = method.getAnnotation(Ttl.class);
                        Cacheable cacheable = method.getAnnotation(Cacheable.class);

                        if (ttl != null && cacheable != null && cacheable.cacheNames().length > 0) {
                            ttlMap.put(cacheable.cacheNames()[0], ttl.ttl());
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        //使用容器存储扫描出来的对象(类全限定名:section对象)
        setSectionMap(ttlMap);

    }

    protected ClassPathScanningCandidateComponentProvider getScanner() {

        return new ClassPathScanningCandidateComponentProvider(false, this.environment) {

            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                if (beanDefinition.getMetadata().isIndependent()) {

                    if (beanDefinition.getMetadata().isInterface()
                            && beanDefinition.getMetadata().getInterfaceNames().length == 1
                            && Annotation.class.getName().equals(beanDefinition.getMetadata().getInterfaceNames()[0])) {
                        try {
                            Class<?> target = ClassUtils.forName(beanDefinition.getMetadata().getClassName(),
                                    CacheRegister.this.classLoader);
                            return !target.isAnnotation();
                        } catch (Exception ex) {
                            this.logger.error(
                                    "Could not load target class: " + beanDefinition.getMetadata().getClassName(), ex);

                        }
                    }
                    return true;
                }
                return false;

            }
        };
    }

    protected Set<String> getBasePackages(AnnotationMetadata importingClassMetadata) {
        Map<String, Object> attributes = importingClassMetadata
                .getAnnotationAttributes(CacheScan.class.getCanonicalName());

        Set<String> basePackages = new HashSet<String>();
        for (String pkg : (String[]) attributes.get("basePackages")) {
            if (pkg != null && !"".equals(pkg)) {
                basePackages.add(pkg);
            }
        }

        if (basePackages.isEmpty()) {
            basePackages.add(ClassUtils.getPackageName(importingClassMetadata.getClassName()));
        }
        return basePackages;
    }

}
