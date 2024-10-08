package com.lb.live.user.provider.service.impl;

import com.alibaba.fastjson.JSON;
import com.lb.live.common.interfaces.topic.UserProviderTopicNames;
import com.lb.live.common.interfaces.utils.ConvertBeanUtils;
import com.lb.live.user.interfaces.constants.CacheAsyncDeleteCode;
import com.lb.live.user.interfaces.constants.UserTagFiledNameConstants;
import com.lb.live.user.interfaces.constants.UserTagsEnum;
import com.lb.live.user.interfaces.dto.UserCacheAsyncDeleteDTO;
import com.lb.live.user.interfaces.dto.UserTagDTO;
import com.lb.live.user.interfaces.utils.TagInfoUtils;
import com.lb.live.user.provider.dao.mapper.IUserTagMapper;
import com.lb.live.user.provider.dao.po.UserTagPO;
import com.lb.live.user.provider.service.IUserTagService;
import com.lb.qiyu.live.framework.redis.starter.key.UserProviderCacheKeyBuilder;
import jakarta.annotation.Resource;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class IUserTagServiceImpl implements IUserTagService {
    @Resource
    private MQProducer mqProducer;

    @Resource
    private IUserTagMapper userTagMapper;

    @Resource
    private RedisTemplate<String, UserTagDTO> redisTemplate;

    @Resource
    private UserProviderCacheKeyBuilder userProviderCacheKeyBuilder;

    @Override
    public boolean setTag(Long userId, UserTagsEnum userTagsEnum) {
        boolean updateStatus = userTagMapper.setTag(userId, userTagsEnum.getFieldName(), userTagsEnum.getTag()) > 0;
        if (updateStatus) {
            deleteUserTagDTOFromRedis(userId);
            return true;
        }
        String setNxKey = userProviderCacheKeyBuilder.buildTagLockKey(userId);
        String setNxResult = redisTemplate.execute((RedisCallback<String>) connection -> {
            RedisSerializer keySerializer = redisTemplate.getKeySerializer();
            RedisSerializer valueSerializer = redisTemplate.getValueSerializer();
            return (String) connection.execute("set", keySerializer.serialize(setNxKey),
                    valueSerializer.serialize("-1"),
                    "NX".getBytes(StandardCharsets.UTF_8),
                    "EX".getBytes(StandardCharsets.UTF_8),
                    "3".getBytes(StandardCharsets.UTF_8));
        });
        if (!"OK".equals(setNxResult)) {
            return false;
        }
        UserTagPO userTagPO = userTagMapper.selectById(userId);
        if (userTagPO != null) {
            return false;
        }
        userTagPO = new UserTagPO();
        userTagPO.setUserId(userId);
        userTagMapper.insert(userTagPO);
        updateStatus = userTagMapper.setTag(userId, userTagsEnum.getFieldName(), userTagsEnum.getTag()) > 0;
        redisTemplate.delete(setNxKey);
        return updateStatus;
    }

    @Override
    public boolean cancelTag(Long userId, UserTagsEnum userTagsEnum) {
        boolean cancelStatus = userTagMapper.cancelTag(userId, userTagsEnum.getFieldName(), userTagsEnum.getTag()) > 0;
        if (!cancelStatus) {
            return false;
        }
        deleteUserTagDTOFromRedis(userId);
        return true;
    }

    @Override
    public boolean containTag(Long userId, UserTagsEnum userTagsEnum) {
        UserTagDTO userTagDTO = this.queryByUserIdFromRedis(userId);
        if (userTagDTO == null) {
            return false;
        }
        String queryFieldName = userTagsEnum.getFieldName();
        //需要根据标签枚举中的fieldName来识别需要匹配MySQL表中哪个字段的标签值
        if (UserTagFiledNameConstants.TAG_INFO_01.equals(queryFieldName)) {
            return TagInfoUtils.isContain(userTagDTO.getTagInfo01(), userTagsEnum.getTag());
        } else if (UserTagFiledNameConstants.TAG_INFO_02.equals(queryFieldName)) {
            return TagInfoUtils.isContain(userTagDTO.getTagInfo02(), userTagsEnum.getTag());
        } else if (UserTagFiledNameConstants.TAG_INFO_03.equals(queryFieldName)) {
            return TagInfoUtils.isContain(userTagDTO.getTagInfo03(), userTagsEnum.getTag());
        }
        return false;
    }

    /**
     * 从redis中删除用户标签对象
     *
     * @param userId
     */
    private void deleteUserTagDTOFromRedis(Long userId) {
        String redisKey = userProviderCacheKeyBuilder.buildTagKey(userId);
        redisTemplate.delete(redisKey);

        UserCacheAsyncDeleteDTO userCacheAsyncDeleteDTO = new UserCacheAsyncDeleteDTO();
        userCacheAsyncDeleteDTO.setCode(CacheAsyncDeleteCode.USER_TAG_DELETE.getCode());
        Map<String,Object> jsonParam = new HashMap<>();
        jsonParam.put("userId",userId);
        userCacheAsyncDeleteDTO.setJson(JSON.toJSONString(jsonParam));

        Message message = new Message();
        message.setTopic(UserProviderTopicNames.CACHE_ASYNC_DELETE_TOPIC);
        message.setBody(JSON.toJSONString(userCacheAsyncDeleteDTO).getBytes());
        //延迟一秒进行缓存的二次删除
        message.setDelayTimeLevel(1);
        try {
            mqProducer.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从redis中查询用户标签对象
     *
     * @param userId
     * @return
     */
    private UserTagDTO queryByUserIdFromRedis(Long userId) {
        String redisKey = userProviderCacheKeyBuilder.buildTagKey(userId);
        // [5.8] 用户标签引入Redis缓存
        UserTagDTO userTagDTO = redisTemplate.opsForValue().get(redisKey);
        if (userTagDTO != null) {
            return userTagDTO;
        }
        UserTagPO userTagPO = userTagMapper.selectById(userId);
        if (userTagPO == null) {
            return null;
        }
        userTagDTO = ConvertBeanUtils.convert(userTagPO, UserTagDTO.class);
        redisTemplate.opsForValue().set(redisKey, userTagDTO);
        redisTemplate.expire(redisKey,30, TimeUnit.MINUTES);
        return userTagDTO;
    }
}
