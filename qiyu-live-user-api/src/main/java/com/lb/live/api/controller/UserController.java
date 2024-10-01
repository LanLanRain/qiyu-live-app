package com.lb.live.api.controller;

import com.lb.live.user.interfaces.IUserRpc;
import com.lb.live.user.interfaces.dto.UserDTO;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/user")
public class UserController {

    @DubboReference(timeout = 5000)
    private IUserRpc userRpc;

    @GetMapping(value = "/getUserInfo")
    public UserDTO getUserInfo(Long userId) {
        return userRpc.getByUserId(userId);
    }

    @GetMapping(value = "/test")
    public String test(){
        userRpc.test();
        return "success";
    }
}