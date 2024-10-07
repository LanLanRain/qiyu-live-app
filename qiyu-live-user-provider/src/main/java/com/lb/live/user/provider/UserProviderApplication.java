package com.lb.live.user.provider;

import com.lb.live.user.interfaces.constants.UserTagsEnum;
import com.lb.live.user.provider.service.IUserTagService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDubbo
@EnableDiscoveryClient
public class UserProviderApplication implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(UserProviderApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
    }

    @Resource
    private IUserTagService userTagService;

    @Override
    public void run(String... args) throws Exception {
        long userId = 1113L;//需要数据库中有此条记录
        userTagService.setTag(userId, UserTagsEnum.IS_VIP);
        System.out.println("###################" + userTagService.containTag(userId, UserTagsEnum.IS_VIP));

        userTagService.cancelTag(userId, UserTagsEnum.IS_VIP);
        System.out.println("###################" + userTagService.containTag(userId, UserTagsEnum.IS_VIP));
    }
}
