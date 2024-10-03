package com.lb.live.user.provider.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "qiyu.rmq.consumer")
public class RocketMQConsumerProperties {
    private String namesrvAddr;
    private String consumerGroup;

    public String getNamesrvAddr() {
        return namesrvAddr;
    }

    public void setNamesrvAddr(String namesrvAddr) {
        this.namesrvAddr = namesrvAddr;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    @Override
    public String toString() {
        return "RocketMQConsumerProperties{" +
                "namesrvAddr='" + namesrvAddr + '\'' +
                ", consumerGroup='" + consumerGroup + '\'' +
                '}';
    }
}
