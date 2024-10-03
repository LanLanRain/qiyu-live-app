package com.lb.live.user.provider.config;

import jakarta.annotation.Resource;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MQProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 该类负责配置和初始化 RocketMQ 生产者。
 * 生产者作为 Spring Bean 创建并用于向 RocketMQ 代理发送消息。
 * 配置包括设置名称服务器地址、生产者组、重试次数等，
 * 并创建用于异步发送的线程池。
 */
@Configuration
public class RocketMQProducerConfig {

    /**
     * 用于记录事件和错误的日志器。
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(RocketMQProducerConfig.class);

    /**
     * RocketMQ 生产者的属性。
     */
    @Resource
    private RocketMQProducerProperties producerProperties;

    /**
     * 应用程序的名称，用于命名生产者线程。
     */
    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * 创建并初始化 RocketMQ 生产者 Bean。
     *
     * @return 初始化的 RocketMQ 生产者。
     */
    @Bean
    public MQProducer mqProducer() {
        // 创建用于异步发送的线程池执行器。
        ThreadPoolExecutor asyncThreadPoolExecutor = new ThreadPoolExecutor(100, 150, 3, TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(1000), r -> {
            Thread thread = new Thread(r);
            thread.setName(applicationName + ":rmq-producer:" + ThreadLocalRandom.current().nextInt(1000));
            return thread;
        });

        // 初始化 RocketMQ 生产者。
        DefaultMQProducer defaultMQProducer = new DefaultMQProducer();
        try {
            // 设置名称服务器地址。
            defaultMQProducer.setNamesrvAddr(producerProperties.getNamesrvAddr());
            // 设置生产者组。
            defaultMQProducer.setProducerGroup(producerProperties.getProducerGroup());
            // 设置同步发送消息时重试的次数。
            defaultMQProducer.setRetryTimesWhenSendFailed(producerProperties.getRetryTimes());
            // 设置异步发送消息时重试的次数。
            defaultMQProducer.setRetryTimesWhenSendAsyncFailed(producerProperties.getRetryTimes());
            // 设置在当前代理不可用时是否重试发送消息到另一个代理。
            defaultMQProducer.setRetryAnotherBrokerWhenNotStoreOK(true);
            // 设置异步发送的线程池。
            defaultMQProducer.setAsyncSenderExecutor(asyncThreadPoolExecutor);
            defaultMQProducer.start();
            LOGGER.info("mq生产者启动成功,nameSrv is {}", producerProperties.getNamesrvAddr());
        } catch (MQClientException e) {
            throw new RuntimeException(e);
        }

        // 返回初始化的生产者。
        return defaultMQProducer;
    }
}
