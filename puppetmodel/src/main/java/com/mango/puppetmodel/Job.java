package com.mango.puppetmodel;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Job
 * 任务数据模型
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public class Job {

    public static final String CANCEL_JOB = "cancel_job";
    public static final String RETRY_JOB = "retry_job";

    public static final String GET_LOCAL_USER_INFO = "get_local_user_info";
    public static final String SEND_TEXT_MESSAGE = "send_text_message";
    public static final String SEND_IMAGE = "send_image";
    public static final String SEND_TRANSFER = "send_transfer";
    public static final String SINGLE_RED = "single_red";
    public static final String CHATROOM_RED = "chatroom_red";
    public static final String CHANGE_REMARK = "change_remark";
    public static final String RECEIVE_RED = "receive_red";
    public static final String RECEIVE_TRANSFER = "receive_transfer";
    public static final String GET_BALANCE = "get_balance";
    public static final String GET_GROUP_LIST = "get_group_list";
    public static final String GET_USER_LIST = "get_user_list";
    public static final String GET_USER_INFO = "get_user_info";
    public static final String GET_GROUP_INFO = "get_group_info";
    public static final String AGREE_FRIEND_APPLY = "agree_friend_apply";
    public static final String REVOKE = "revoke";
    public static final String SEND_MUTI_TEXT = "send_muti_text";
    public static final String SEND_MUTI_IMAGE = "send_muti_image";
    public static final String SEND_NORMAL_TIMELINE = "send_normal_timeline";
    public static final String SEND_VIDEO_TIMELINE = "send_video_timeline";
    public static final String SEND_LINK_TIMELINE = "send_link_timeline";
    public static final String AGREE_TO_CHATROOM = "agree_to_chatroom";
    public static final String ADD_TO_BLACK_LIST = "add_to_black_list";
    public static final String REMOVE_FROM_CHATROOM = "remove_from_chatroom";
    public static final String ADD_TO_CHATROOM = "add_to_chatroom";
    public static final String REMOVE_FROM_BLACK_LIST = "remove_from_black_list";
    public static final String SHOW_ORIGIN_IMAGE = "show_origin_image";
    public static final String CHANGE_USER_TAG = "change_user_tag";
    public static final String CREATE_CHATROOM = "create_chatroom";
    public static final String GET_CHATROOM_QRCODE = "get_chatroom_qrcode";

    // 由远程服务端生成的任务唯一标识
    public long job_id;

    // 任务在哪个应用上执行，系统任务不需此参数
    public String package_name;

    // 任务名称 标识是什么任务
    public String job_name;

    /**
     * 任务状态
     * 0:待执行
     * 1:第一步执行成功 && 已上报
     *
     * 2:第一步执行成功 && 未上报
     * 3:完全执行成功 && 未上报
     * 4:已取消 && 未上报
     * 5:执行失败 && 未上报
     *
     * 6:执行失败 && 已上报
     */
    public int job_status;

    // 任务执行失败时的错误码 任务成功时为0
    public int error_code;

    // 任务执行失败时的原因 任务成功时为null
    public String error_message;

    // 任务所需参数
    public JSONObject job_data;

    // 任务执行结果附带参数
    public String result_data;

    // 任务回执url
    public String callback;

    public String toString() {
        String str = new Gson().toJson(this);
        try  {
            JSONObject jsonObject = new JSONObject(str);
            jsonObject.put("job_data", this.job_data);
            str = jsonObject.toString();
            return str;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return super.toString();
    }

    public static Job fromString(String string) {
        Job job = new Gson().fromJson(string, Job.class);
        try {
            JSONObject jsonObject = new JSONObject(string);
            job.job_data = jsonObject.optJSONObject("job_data");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return job;
    }
}
