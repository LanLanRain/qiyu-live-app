package com.lb.live.user.provider.rpc;

import com.lb.live.user.interfaces.IUserRpc;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class UserRpcImpl implements IUserRpc {
    @Override
    public String test() {
        System.out.println("this is a dubbo test");
        return "success";
    }
}
