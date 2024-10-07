package com.lb.live.user.provider.rpc;

import com.lb.live.user.interfaces.constants.UserTagsEnum;
import com.lb.live.user.interfaces.rpc.IUserTagRpc;
import com.lb.live.user.provider.service.IUserTagService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class UserTagRpcImpl implements IUserTagRpc {
    
    @Resource
    private IUserTagService userTagService;
    
    @Override
    public boolean setTag(Long userId, UserTagsEnum userTagsEnum) {
        return userTagService.setTag(userId, userTagsEnum);
    }

    @Override
    public boolean cancelTag(Long userId, UserTagsEnum userTagsEnum) {
        return userTagService.cancelTag(userId, userTagsEnum);
    }

    @Override
    public boolean containTag(Long userId, UserTagsEnum userTagsEnum) {
        return userTagService.containTag(userId, userTagsEnum);
    }
}