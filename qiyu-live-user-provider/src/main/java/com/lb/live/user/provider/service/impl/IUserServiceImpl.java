package com.lb.live.user.provider.service.impl;

import com.lb.live.user.interfaces.dto.UserDTO;
import com.lb.live.user.provider.dao.mapper.IUserMapper;
import com.lb.live.user.provider.dao.po.UserPO;
import com.lb.live.user.provider.service.IUserService;
import com.lb.live.user.provider.utils.ConvertBeanUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class IUserServiceImpl implements IUserService {

    @Resource
    private IUserMapper userMapper;

    @Override
    public UserDTO getByUserId(Long userId) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(userId);
        UserPO userPO = userMapper.selectById(userDTO);
        UserDTO convertedDTO = ConvertBeanUtils.convert(userPO, UserDTO.class);
        return convertedDTO;
    }

    @Override
    public boolean updateUserInfo(UserDTO userDTO) {
        if (userDTO.getUserId() == null || userDTO != null) {
            return false;
        }
        return userMapper.updateById(ConvertBeanUtils.convert(userDTO, UserPO.class)) > 0;
    }

    @Override
    public boolean insertOne(UserDTO userDTO) {
        if (userDTO.getUserId() == null || userDTO == null) {
            return false;
        }
        return userMapper.insert(ConvertBeanUtils.convert(userDTO, UserPO.class)) > 0;
    }
}
