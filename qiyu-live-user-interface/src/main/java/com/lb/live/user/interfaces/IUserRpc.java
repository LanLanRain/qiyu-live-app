package com.lb.live.user.interfaces;

import com.lb.live.user.interfaces.dto.UserDTO;

public interface IUserRpc {

    String test();

    /**
     * 根据用户id进行查询
     *
     * @param userId
     * @return
     */
    UserDTO getByUserId(Long userId);
}
