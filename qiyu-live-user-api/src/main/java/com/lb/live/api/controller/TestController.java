package com.lb.live.api.controller;

import com.lb.live.user.interfaces.IUserRpc;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @DubboReference
    private IUserRpc userRpc;

    @RequestMapping("/dubbo")
    public String dubbo() {
        return userRpc.test();
    }
}
