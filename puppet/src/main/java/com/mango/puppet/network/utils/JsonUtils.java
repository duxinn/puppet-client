package com.mango.puppet.network.utils;

import com.alibaba.fastjson.JSON;

import java.lang.reflect.Type;

public class JsonUtils {

    /**
     * Converter object to json string.
     *
     * @param data the object.
     *
     * @return json string.
     */
    public static String toJsonString(Object data) {
        return JSON.toJSONString(data);
    }

    /**
     * Parse json to object.
     *
     * @param json json string.
     * @param type the type of object.
     * @param <T> type.
     *
     * @return object.
     */
    public static <T> T parseJson(String json, Type type) {
        return JSON.parseObject(json, type);
    }
}