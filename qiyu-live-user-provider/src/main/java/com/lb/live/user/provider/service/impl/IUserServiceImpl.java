package com.lb.live.user.provider.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.lb.live.common.interfaces.topic.UserProviderTopicNames;
import com.lb.live.common.interfaces.utils.ConvertBeanUtils;
import com.lb.live.user.interfaces.constants.CacheAsyncDeleteCode;
import com.lb.live.user.interfaces.dto.UserCacheAsyncDeleteDTO;
import com.lb.live.user.interfaces.dto.UserDTO;
import com.lb.live.user.provider.dao.mapper.IUserMapper;
import com.lb.live.user.provider.dao.po.UserPO;
import com.lb.live.user.provider.service.IUserService;
import com.lb.qiyu.live.framework.redis.starter.key.UserProviderCacheKeyBuilder;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class IUserServiceImpl implements IUserService {

    @Resource
    private IUserMapper userMapper;

    @Resource
    private RedisTemplate<String, UserDTO> redisTemplate;

    @Resource
    private UserProviderCacheKeyBuilder cacheKeyBuilder;
    @Autowired
    private MQProducer mqProducer;

    /**
     * 获取指定用户 ID 对应的用户信息。
     * 该方法首先检查传入的用户 ID 是否为 null，如果是，则直接返回 null。
     * 接着，它使用 cacheKeyBuilder 构建与用户信息相关的键，并从 Redis 缓存中获取该键的值。
     * 如果 Redis 缓存中存在该键的值，则将其转换为 UserDTO 类型并返回。
     * 如果 Redis 缓存中不存在该键的值，则该方法从数据库中查询该用户信息，
     * 并将查询到的结果存入 Redis 缓存中。
     * 最后，该方法返回用户数据传输对象。
     *
     * @param userId 要查询的用户 ID。
     * @return 与指定用户 ID 对应的用户信息。如果用户 ID 为 null 或在 Redis 缓存和数据库中都未找到该用户信息，则返回 null。
     */
    @Override
    public UserDTO getByUserId(Long userId) {
        // 1. 检查传入的用户ID是否为null，如果是则直接返回null
        if (userId == null) {
            return null;
        }
        // 2. 使用cacheKeyBuilder构建与用户信息相关的键
        String userInfoKey = cacheKeyBuilder.buildUserInfoKey(userId);
        // 3. 从Redis中获取对应键的值，并转换为UserDTO类型
        UserDTO userDTO = (UserDTO) redisTemplate.opsForValue().get(userInfoKey);
        // 4. 如果从Redis中获取到的用户数据不为null，则直接返回
        if (userDTO == null) {
            return null;
        }
        // 5. 如果从Redis中未获取到用户数据，则从数据库中查询
        userDTO = ConvertBeanUtils.convert(userMapper.selectById(userId), UserDTO.class);
        // 6. 如果从数据库中查询到的用户数据不为null，则将其存入Redis中
        if (userDTO != null) {
            redisTemplate.opsForValue().set(userInfoKey, userDTO, 30, TimeUnit.MINUTES);
        }
        // 7. 最后返回用户数据传输对象
        return userDTO;
    }

    /**
     * 更新数据库和 Redis 缓存中的用户信息。
     * 如果更新成功，它会向消息队列发送一条消息以进行异步缓存删除。
     *
     * @param userDTO 包含更新信息的用户数据传输对象。
     * @return 如果更新和缓存删除成功，则返回 {@code true}；否则返回 {@code false}。
     * @throws RuntimeException 如果在消息队列发送操作期间发生异常。
     */
    @Override
    public boolean updateUserInfo(UserDTO userDTO) {
        // 检查用户 ID 和用户数据是否有效
        if (userDTO.getUserId() == null || userDTO != null) {
            return false;
        }

        // 更新数据库中的用户信息
        int updateStatus = userMapper.updateById(ConvertBeanUtils.convert(userDTO, UserPO.class));

        // 如果更新成功，则继续进行缓存删除和消息队列发送
        if (updateStatus > 0) {
            // 构建与用户信息相关的键
            String userInfoKey = cacheKeyBuilder.buildUserInfoKey(userDTO.getUserId());

            // 从 Redis 缓存中删除用户信息
            redisTemplate.delete(userInfoKey);

            // 为缓存删除消息准备数据
            UserCacheAsyncDeleteDTO userCacheAsyncDeleteDTO = new UserCacheAsyncDeleteDTO();
            userCacheAsyncDeleteDTO.setCode(CacheAsyncDeleteCode.USER_INFO_DELETE.getCode());
            Map<String, Object> jsonParam = new HashMap<>();
            jsonParam.put("userId", userDTO.getUserId());
            userCacheAsyncDeleteDTO.setJson(JSON.toJSONString(jsonParam));

            // 创建消息队列的消息
            Message message = new Message();
            message.setTopic(UserProviderTopicNames.CACHE_ASYNC_DELETE_TOPIC);
            message.setBody(JSON.toJSONString(userCacheAsyncDeleteDTO).getBytes());

            // 为缓存删除设置 1 秒的延迟
            message.setDelayTimeLevel(1);

            try {
                // 向消息队列发送消息
                mqProducer.send(message);
            } catch (MQClientException | RemotingException | MQBrokerException | InterruptedException e) {
                // 捕获在消息队列发送操作期间发生的任何异常并将其包装在 RuntimeException 中抛出
                throw new RuntimeException(e);
            }
        }

        // 如果更新和缓存删除成功，则返回 true
        return true;
    }

    @Override
    public boolean insertOne(UserDTO userDTO) {
        if (userDTO.getUserId() == null || userDTO == null) {
            return false;
        }
        return userMapper.insert(ConvertBeanUtils.convert(userDTO, UserPO.class)) > 0;
    }

    /**
     * 批量查询用户信息，并从 Redis 缓存中获取数据。
     * 如果 Redis 缓存中存在所有用户信息，则直接返回。
     * 如果 Redis 缓存中存在部分用户信息，则从数据库中查询剩余的用户信息并返回。
     *
     * @param userIdList 要查询的用户 ID 列表。
     * @return 包含查询到的用户信息的 Map，其中键为用户 ID，值为 UserDTO 对象。
     */
    @Override
    public Map<Long, UserDTO> batchQueryUserInfo(List<Long> userIdList) {
        // 1. 输入参数校验
        if (userIdList == null || userIdList.isEmpty()) {
            return Maps.newHashMap();
        }

        // 2. 过滤出 ID 大于 10000 的用户 ID
        userIdList = userIdList.stream().filter(id -> id > 10000).collect(Collectors.toList());
        if (userIdList.isEmpty()) {
            return Maps.newHashMap();
        }

        // 3. 构建 Redis 键列表
        List<String> keyList = new ArrayList<>();
        userIdList.forEach(userId -> keyList.add(cacheKeyBuilder.buildUserInfoKey(userId)));

        // 4. 从 Redis 缓存中获取用户信息
        List<UserDTO> userDTOList = redisTemplate.opsForValue().multiGet(keyList).stream()
                .filter(x -> x != null)
                .collect(Collectors.toList());

        // 5. 如果 Redis 缓存中存在所有用户信息，则直接返回
        if (!CollectionUtils.isEmpty(userDTOList) && userDTOList.size() == userIdList.size()) {
            return userDTOList.stream().collect(Collectors.toMap(UserDTO::getUserId, x -> x));
        }

        // 6. 获取 Redis 缓存中存在的用户 ID
        List<Long> userIdInCacheList = userDTOList.stream().map(UserDTO::getUserId).collect(Collectors.toList());

        // 7. 获取需要从数据库中查询的用户 ID
        List<Long> userIdNotInCacheList = userIdList.stream()
                .filter(x -> !userIdInCacheList.contains(x))
                .collect(Collectors.toList());

        // 8. 多线程查询数据库，并将查询结果存入 Redis 缓存
        Map<Long, List<Long>> userIdMap = userIdNotInCacheList.stream()
                .collect(Collectors.groupingBy(userId -> userId % 100));
        List<UserDTO> dbQueryResult = new CopyOnWriteArrayList<>();
        userIdMap.values().parallelStream().forEach(queryUserIdList -> {
            dbQueryResult.addAll(ConvertBeanUtils.convertList(userMapper.selectBatchIds(queryUserIdList), UserDTO.class));
        });

        // 9. 如果查询结果不为空，则将查询结果存入 Redis 缓存并设置过期时间
        if (!CollectionUtils.isEmpty(dbQueryResult)) {
            Map<String, UserDTO> saveCacheMap = dbQueryResult.stream()
                    .collect(Collectors.toMap(userDto -> cacheKeyBuilder.buildUserInfoKey(userDto.getUserId()), x -> x));
            redisTemplate.opsForValue().multiSet(saveCacheMap);

            // 对命令执行批量过期设置操作
            redisTemplate.executePipelined(new SessionCallback<Object>() {
                @Override
                public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                    for (String redisKey : saveCacheMap.keySet()) {
                        operations.expire((K) redisKey, createRandomTime(), TimeUnit.SECONDS);
                    }
                    return null;
                }
            });

            // 10. 将查询结果添加到返回列表中
            userDTOList.addAll(dbQueryResult);
        }

        // 11. 返回查询到的用户信息
        return userDTOList.stream().collect(Collectors.toMap(UserDTO::getUserId, x -> x));
    }

    public int createRandomTime() {
        int randomNumSecond = ThreadLocalRandom.current().nextInt(10000);
        return randomNumSecond + 30 * 60;
    }
}
