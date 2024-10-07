package com.lb.live.user.provider.service.impl;

import com.lb.live.user.interfaces.constants.UserTagFiledNameConstants;
import com.lb.live.user.interfaces.constants.UserTagsEnum;
import com.lb.live.user.interfaces.utils.TagInfoUtils;
import com.lb.live.user.provider.dao.mapper.IUserTagMapper;
import com.lb.live.user.provider.dao.po.UserTagPo;
import com.lb.live.user.provider.service.IUserTagService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class IUserTagServiceImpl implements IUserTagService {

    @Resource
    private IUserTagMapper userTagMapper;

    @Override
    public boolean setTag(Long userId, UserTagsEnum userTagsEnum) {
        return userTagMapper.setTag(userId, userTagsEnum.getFieldName(), userTagsEnum.getTag()) > 0;
    }

    @Override
    public boolean cancelTag(Long userId, UserTagsEnum userTagsEnum) {
        return userTagMapper.cancelTag(userId, userTagsEnum.getFieldName(), userTagsEnum.getTag()) > 0;
    }

    @Override
    public boolean containTag(Long userId, UserTagsEnum userTagsEnum) {
        UserTagPo userTagPo = userTagMapper.selectById(userId);
        if (userTagPo == null) {
            return false;
        }
        String fieldName = userTagsEnum.getFieldName();
        if (fieldName.equals(UserTagFiledNameConstants.TAG_INFO_01)) {
            return TagInfoUtils.isContain(userTagPo.getTagInfo01(), userTagsEnum.getTag());
        } else if (fieldName.equals(UserTagFiledNameConstants.TAG_INFO_02)) {
            return TagInfoUtils.isContain(userTagPo.getTagInfo02(), userTagsEnum.getTag());
        } else if (fieldName.equals(UserTagFiledNameConstants.TAG_INFO_03)) {
            return TagInfoUtils.isContain(userTagPo.getTagInfo03(), userTagsEnum.getTag());
        }
        return false;
    }

}
