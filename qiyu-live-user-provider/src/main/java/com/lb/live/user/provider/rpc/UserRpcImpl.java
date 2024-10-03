package com.lb.live.user.provider.rpc;

import com.lb.live.user.interfaces.rpc.IUserRpc;
import com.lb.live.user.interfaces.dto.UserDTO;
import com.lb.live.user.provider.service.IUserService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;
import java.util.Map;

@DubboService
public class UserRpcImpl implements IUserRpc {

    @Resource
    private IUserService userService;

    @Override
    public String test() {
        System.out.println("this is a dubbo test");
        return "success";
    }

    @Override
    public UserDTO getByUserId(Long userId) {
        return userService.getByUserId(userId);
    }

    @Override
    public boolean updateUserInfo(UserDTO userDTO) {
        return userService.updateUserInfo(userDTO);
    }

    @Override
    public boolean insertOne(UserDTO userDTO) {
        return userService.insertOne(userDTO);
    }

    @Override
    public Map<Long, UserDTO> batchQueryUserInfo(List<Long> userIdList) {
        return userService.batchQueryUserInfo(userIdList);
    }
}
