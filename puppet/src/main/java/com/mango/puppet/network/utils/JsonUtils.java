package com.mango.puppet.network.utils;

import com.alibaba.fastjson.JSON;
import com.mango.puppet.network.server.model.ReturnData;

import java.lang.reflect.Type;

public class JsonUtils {

    /**
     * Business is successful.
     *
     * @param data return data.
     *
     * @return json.
     */
    public static String successfulJson(Object data) {
        ReturnData returnData = new ReturnData();
        returnData.setErrorCode(200);
        returnData.setStatus(0);
        returnData.setData(data);
        return JSON.toJSONString(returnData);
    }

    /**
     * Business is failed.
     *
     * @param code error code.
     * @param message message.
     *
     * @return json.
     */
    public static String failedJson(int code, String message) {
        ReturnData returnData = new ReturnData();
        returnData.setErrorCode(code);
        returnData.setErrorMsg(message);
        returnData.setStatus(1);
        return JSON.toJSONString(returnData);
    }

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