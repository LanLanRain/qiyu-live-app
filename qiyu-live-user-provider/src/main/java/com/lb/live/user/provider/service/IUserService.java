package com.lb.live.user.provider.service;

import com.lb.live.user.interfaces.dto.UserDTO;

public interface IUserService {
    /**
     * 根据用户id进行查询
     *
     * @param userId
     * @return
     */
    UserDTO getByUserId(Long userId);
}