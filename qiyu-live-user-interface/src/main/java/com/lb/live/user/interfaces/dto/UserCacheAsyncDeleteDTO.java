package com.lb.live.user.interfaces.dto;

import java.io.Serial;
import java.io.Serializable;

/**
 * 此类表示用于异步删除用户缓存数据的传输对象 (DTO)。
 * 它包含两个字段：code 和 json，用于区分不同业务场景
 * 并为缓存删除过程提供附加数据。
 */
public class UserCacheAsyncDeleteDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -2291922809338528918L;

    /**
     * 不同业务场景由此代码标识，以便于区分不同的延迟消息。
     */
    private int code;

    /**
     * 作为 JSON 字符串的附加数据，用于缓存删除过程。
     */
    private String json;

    /**
     * 返回与当前业务场景关联的代码。
     *
     * @return 代码
     */
    public int getCode() {
        return code;
    }

    /**
     * 设置当前业务场景的代码。
     *
     * @param code 要设置的代码
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * 返回作为 JSON 字符串的缓存删除过程的附加数据。
     *
     * @return JSON 字符串
     */
    public String getJson() {
        return json;
    }

    /**
     * 设置作为 JSON 字符串的缓存删除过程的附加数据。
     *
     * @param json 要设置的 JSON 字符串
     */
    public void setJson(String json) {
        this.json = json;
    }
}
