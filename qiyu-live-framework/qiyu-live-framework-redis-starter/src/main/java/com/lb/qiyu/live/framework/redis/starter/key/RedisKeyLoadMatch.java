package com.lb.qiyu.live.framework.redis.starter.key;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * 该类实现了 {@link Condition} 接口，用于确定是否应加载 {@link RedisKeyBuilder}。
 * 该条件检查应用程序名称是否与 RedisKeyBuilder 类名称匹配。
 */
public class RedisKeyLoadMatch implements Condition {

    /**
     * 用于记录日志的 Logger。
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(RedisKeyLoadMatch.class);

    /**
     * RedisKeyBuilder 类名称的前缀。
     */
    private static final String PREFIX = "qiyulive";

    /**
     * 基于应用程序名称和 RedisKeyBuilder 类名称来确定是否应加载 {@link RedisKeyBuilder}。
     *
     * @param context 应用程序的上下文。
     * @param metadata 关于已注解类型的元数据。
     * @return 如果应加载 RedisKeyBuilder，则返回 {@code true}，否则返回 {@code false}。
     */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String appName = context.getEnvironment().getProperty("spring.application.name");
        if (appName == null) {
            LOGGER.error("没有匹配到应用名称，所以无法加载任何 RedisKeyBuilder 对象");
            return false;
        }
        try {
            Field classNameField = metadata.getClass().getDeclaredField("className");
            classNameField.setAccessible(true);
            String keyBuilderName = (String) classNameField.get(metadata);
            List<String> splitList = Arrays.asList(keyBuilderName.split("\\."));
            // 忽略大小写，统一用 qiyulive 开头命名
            String classSimplyName = PREFIX + splitList.get(splitList.size() - 1).toLowerCase();
            boolean matchStatus = classSimplyName.contains(appName.replaceAll("-", ""));
            LOGGER.info("keyBuilderClass is {}, matchStatus is {}", keyBuilderName, matchStatus);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
