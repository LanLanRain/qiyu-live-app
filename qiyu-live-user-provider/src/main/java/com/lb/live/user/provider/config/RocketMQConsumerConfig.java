package com.lb.live.user.provider.config;

import com.alibaba.fastjson.JSON;
import com.lb.live.common.interfaces.topic.UserProviderTopicNames;
import com.lb.live.user.interfaces.constants.CacheAsyncDeleteCode;
import com.lb.live.user.interfaces.dto.UserCacheAsyncDeleteDTO;
import com.lb.qiyu.live.framework.redis.starter.key.UserProviderCacheKeyBuilder;
import jakarta.annotation.Resource;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RocketMQConsumerConfig implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQConsumerConfig.class);

    @Resource
    private RocketMQConsumerProperties consumerProperties;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private UserProviderCacheKeyBuilder userProviderCacheKeyBuilder;

    @Override
    public void afterPropertiesSet() {
        initConsumer();
    }

    /**
     * 初始化 RocketMQ 消费者以处理缓存删除请求。
     * <p>
     * 此方法设置消费者属性、订阅指定主题、启动消费者。
     * 它还注册了一个消息侦听器来处理传入的消息并执行缓存删除操作。
     *
     * @throws RuntimeException 如果在初始化消费者时发生错误。
     */
    private void initConsumer() {
        try {
            // 初始化我们的 RocketMQ 消费者
            DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();

            // 设置 RocketMQ 名称服务器的地址
            defaultMQPushConsumer.setNamesrvAddr(consumerProperties.getNamesrvAddr());

            // 设置消费者组
            defaultMQPushConsumer.setConsumerGroup(consumerProperties.getConsumerGroup());

            // 设置一次消费的消息批次的最大大小
            defaultMQPushConsumer.setConsumeMessageBatchMaxSize(1);

            // 设置消费的起始点
            defaultMQPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);

            // 订阅指定的主题
            defaultMQPushConsumer.subscribe(UserProviderTopicNames.CACHE_ASYNC_DELETE_TOPIC, "*");

            // 注册一个消息侦听器来处理传入的消息
            defaultMQPushConsumer.setMessageListener((MessageListenerConcurrently) (msgs, context) -> {
                String json = new String(msgs.get(0).getBody());
                UserCacheAsyncDeleteDTO userCacheAsyncDeleteDTO = JSON.parseObject(json, UserCacheAsyncDeleteDTO.class);
                if (CacheAsyncDeleteCode.USER_INFO_DELETE.getCode() == userCacheAsyncDeleteDTO.getCode()) {
                    Long userId = JSON.parseObject(userCacheAsyncDeleteDTO.getJson()).getLong("userId");
                    redisTemplate.delete(userProviderCacheKeyBuilder.buildUserInfoKey(userId));
                    LOGGER.info("延迟删除用户信息缓存，userId is {}", userId);
                } else if (CacheAsyncDeleteCode.USER_TAG_DELETE.getCode() == userCacheAsyncDeleteDTO.getCode()) {
                    Long userId = JSON.parseObject(userCacheAsyncDeleteDTO.getJson()).getLong("userId");
                    redisTemplate.delete(userProviderCacheKeyBuilder.buildTagKey(userId));
                    LOGGER.info("延迟删除用户标签缓存，userId is {}", userId);
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            });

            defaultMQPushConsumer.start();
            LOGGER.info("mq消费者启动成功,nameSrv is {}", consumerProperties.getNamesrvAddr());
        } catch (MQClientException e) {
            throw new RuntimeException(e);
        }
    }
}