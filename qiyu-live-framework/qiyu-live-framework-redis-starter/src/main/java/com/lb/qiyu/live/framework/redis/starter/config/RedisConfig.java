package com.lb.qiyu.live.framework.redis.starter.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * RedisTemplate 配置类。此类负责创建 RedisTemplate 实例，并使用自定义的序列化器来支持通用 JSON 序列化。
 */
@Configuration
@ConditionalOnClass(RedisTemplate.class)
public class RedisConfig {

    /**
     * 创建一个 RedisTemplate 实例，并为键和值设置自定义的序列化器。
     *
     * @param redisConnectionFactory Redis 的连接工厂。
     * @return 一个已配置的 RedisTemplate 实例。
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 自定义的序列化器，用于值
        IGenericJackson2JsonRedisSerializer valueSerializer = new IGenericJackson2JsonRedisSerializer();

        // 默认的序列化器，用于键
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // 为键、值、哈希键和哈希值设置序列化器
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(valueSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setHashValueSerializer(valueSerializer);

        // 初始化 RedisTemplate
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }
}