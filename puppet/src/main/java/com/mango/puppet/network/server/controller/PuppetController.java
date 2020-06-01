package com.mango.puppet.network.server.controller;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.mango.puppet.dispatch.event.EventManager;
import com.mango.puppet.dispatch.job.JobManager;
import com.mango.puppet.network.server.model.ReturnData;
import com.mango.puppet.tool.ThreadUtils;
import com.mango.puppetmodel.Job;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestBody;
import com.yanzhenjie.andserver.annotation.RequestMapping;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.framework.body.JsonBody;
import com.yanzhenjie.andserver.http.HttpResponse;
import com.yanzhenjie.andserver.http.ResponseBody;
import com.yanzhenjie.andserver.util.MediaType;

import org.json.JSONObject;


@RestController
@RequestMapping(path = "/dispatch")
class PuppetController {

    /**
     * 注册/注销事件监听
     *
     * @param httpResponse
     * @param eventWatcherJson
     * @return
     */
    @PostMapping(path = "/setEventWatcher", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    void distributeEventWatcher(HttpResponse httpResponse,
                                @RequestParam(name = "event_name") final String event_name,
                                @RequestParam(name = "package_name") final String package_name,
                                @RequestParam(name = "watcher_status") final int watcher_status,
                                @RequestParam(name = "callback") final String callback) {
        ReturnData returnData = new ReturnData();
        if (!TextUtils.isEmpty(event_name)
                && !TextUtils.isEmpty(package_name)
                && !TextUtils.isEmpty(callback)
                && (watcher_status == 1 || watcher_status == 0)) {
            ThreadUtils.runInMainThread(new Runnable() {
                @Override
                public void run() {
                    EventManager.getInstance().setEventWatcher(package_name, event_name, watcher_status == 1, callback);
                }
            });
            returnData.status = 0;
        } else {
            returnData.status = 1;
            returnData.message = "参数错误";
        }
        ResponseBody body = new JsonBody(JSON.toJSONString(returnData));
        httpResponse.setBody(body);
    }

    /**
     * 分发任务
     *
     * @param httpResponse
     * @param jobJson
     */
    @PostMapping(path = "/addJob", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    void distributeJob(HttpResponse httpResponse,
                       @RequestBody JSONObject jsonObject) {

        int status = 0;
        String message = "";
        if (jsonObject == null) {
            status = 1;
            message = "参数为空";
        }

        if (status == 0) {
            long job_id = jsonObject.optLong("job_id");
            String package_name = jsonObject.optString("package_name");
            String job_name = jsonObject.optString("job_name");
            String callback = jsonObject.optString("callback");
            JSONObject job_data = jsonObject.optJSONObject("job_data");

            if (!TextUtils.isEmpty(package_name)
                    && !TextUtils.isEmpty(job_name)
                    && !TextUtils.isEmpty(callback)) {
                Job job = new Job();
                job.job_id = job_id;
                job.package_name = package_name;
                job.job_name = job_name;
                job.callback = callback;
                if (job_data != null) {
                    job.job_data = job_data;
                } else {
                    job.job_data = new JSONObject();
                }
                boolean flag = JobManager.getInstance().addJob(job);
                if (!flag) {
                    status = 2;
                    message = "任务已存在";
                }
            } else {
                status = 3;
                message = "参数错误";
            }
        }

        ReturnData returnData = new ReturnData();
        returnData.status = status;
        returnData.message = message;
        ResponseBody body = new JsonBody(JSON.toJSONString(returnData));
        httpResponse.setBody(body);
    }
}
