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

    @GetMapping(value = "/test")
    public String test(){
        userRpc.test();
        return "success";
    }

    @GetMapping(value = "/insertOne")
    public boolean insertOne(Long userId, String nickName) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(userId);
        userDTO.setNickName(nickName);
        return userRpc.insertOne(userDTO);
    }
    @GetMapping(value = "/updateUserInfo")
    public boolean updateUserInfo(Long userId, String nickName) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(userId);
        userDTO.setNickName(nickName);
        return userRpc.updateUserInfo(userDTO);
    }

}