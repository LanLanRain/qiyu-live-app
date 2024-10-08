package com.lb.live.user.provider.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lb.live.user.provider.dao.po.UserTagPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface IUserTagMapper extends BaseMapper<UserTagPO> {
    @Update("update t_user_tag set ${fieldName} = ${fieldName} | #{tag} where user_id = #{userId} and ${fieldName} & #{tag} = 0")
    int setTag(Long userId, String fieldName, long tag);//and后面是保证是不存在tag标签才设置，保证第一次设置返回的才是true

    @Update("update t_user_tag set ${fieldName} = ${fieldName} &~ #{tag} where user_id = #{userId} and ${fieldName} & #{tag} = #{tag}")
    int cancelTag(Long userId, String fieldName, long tag);//and后面是保证是存在tag标签才撤销，保证第一次撤销返回的才是true
}