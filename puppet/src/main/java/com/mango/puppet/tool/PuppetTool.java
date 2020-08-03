package com.mango.puppet.tool;

import com.mango.puppetmodel.Job;

import java.util.HashMap;
import java.util.Map;

public class PuppetTool {
    private static String[] weixinPageJob = new String[]{
            Job.SCAN_ADD_GROUP,
            Job.URL_ADD_CHATROOM,
            Job.RECEIVE_RED,
            Job.RECEIVE_TRANSFER,
            Job.CHATROOM_RED,
            Job.SINGLE_RED,
            Job.GET_BALANCE,
            Job.SEND_VIDEO_TIMELINE,
            Job.SEND_NORMAL_TIMELINE,
            Job.SEND_LINK_TIMELINE,
            Job.CREATE_CHATROOM,
            Job.SEND_TRANSFER,
            Job.GET_CHATROOM_QRCODE
    };
    private static Map map = new HashMap();

    static {
        map.put("com.tencent.mm", weixinPageJob);
    }

    //是否需要唤醒屏幕
    public static boolean needWakeUp(Job job) {
        boolean isNeed = false;
        String[] list = (String[]) map.get(job.package_name);
        if (list == null) {
            return true;
        }
        for (String s : list) {
            if (s.equals(job.job_name)) {
                isNeed = true;
            }
        }
        return isNeed;
    }
}
