package com.lb.live.user.provider.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "qiyu.rmq.producer")
public class RocketMQProducerProperties {
    private String namesrvAddr;
    private String producerGroup;
    private int retryTimes;
    private int sendTimeout;

    public String getNamesrvAddr() {
        return namesrvAddr;
    }

    public void setNamesrvAddr(String namesrvAddr) {
        this.namesrvAddr = namesrvAddr;
    }

    public String getProducerGroup() {
        return producerGroup;
    }

    public void setProducerGroup(String producerGroup) {
        this.producerGroup = producerGroup;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public int getSendTimeout() {
        return sendTimeout;
    }

    public void setSendTimeout(int sendTimeout) {
        this.sendTimeout = sendTimeout;
    }

    @Override
    public String toString() {
        return "RocketMQProducerProperties{" +
                "namesrvAddr='" + namesrvAddr + '\'' +
                ", producerGroup='" + producerGroup + '\'' +
                ", retryTimes=" + retryTimes +
                ", sendTimeout=" + sendTimeout +
                '}';
    }
}
