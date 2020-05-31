package com.mango.puppetmodel;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Event
 * 事件模型
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public class Event {

    // 事件名称
    public String event_name;

    // 监听哪个应用，系统应用不需此参数
    public String package_name;

    // 事件内容
    public JSONObject event_data;

    // 事件上报结果(仅本地控制器使用) 0:新事件未上报 1:上报已成功
    public int event_status;

    public String toString() {
        String str = new Gson().toJson(this);
        try  {
            JSONObject jsonObject = new JSONObject(str);
            jsonObject.put("event_data", this.event_data);
            str = jsonObject.toString();
            return str;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return super.toString();
    }

    public static Event fromString(String string) {
        Event event = new Gson().fromJson(string, Event.class);
        try {
            JSONObject jsonObject = new JSONObject(string);
            event.event_data = jsonObject.optJSONObject("event_data");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return event;
    }
}
