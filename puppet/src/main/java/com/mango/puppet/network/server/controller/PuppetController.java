package com.mango.puppet.network.server.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.mango.puppet.dispatch.event.EventManager;
import com.mango.puppet.dispatch.job.JobManager;

import com.mango.puppet.network.server.model.ReturnData;
import com.mango.puppetmodel.EventWatcher;
import com.mango.puppetmodel.Job;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestMapping;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.framework.body.StringBody;
import com.yanzhenjie.andserver.http.HttpResponse;
import com.yanzhenjie.andserver.http.ResponseBody;
import com.yanzhenjie.andserver.util.MediaType;


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
    void distributeEventWatcher(HttpResponse httpResponse, @RequestParam(name = "event_watcher") String eventWatcherJson) {
        try {
            final EventWatcher eventWatcher = JSON.parseObject(eventWatcherJson, new TypeReference<EventWatcher>() {});
            ResponseBody body = new StringBody(JSON.toJSONString(eventWatcher));
            httpResponse.setBody(body);
            EventManager.getInstance().setEventWatcher(eventWatcher.package_name, eventWatcher.event_name, eventWatcher.watcher_status == 1);
        } catch (Exception e){
            ReturnData returnData = new ReturnData();
            returnData.setStatus(1);
            returnData.setErrorMsg(e.getMessage());
            ResponseBody body = new StringBody(JSON.toJSONString(returnData));
            httpResponse.setBody(body);
        }
    }



    /**
     * 分发任务
     *
     * @param httpResponse
     * @param jobJson
     */
    @PostMapping(path = "/addJob", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    void distributeJob(HttpResponse httpResponse, @RequestParam(name = "job") String jobJson) {
        try {
            final Job job = JSON.parseObject(jobJson, new TypeReference<Job>() {});
            ResponseBody body = new StringBody(JSON.toJSONString(job));
            httpResponse.setBody(body);
            JobManager.getInstance().addJob(job);
        } catch (Exception e) {
            ReturnData returnData = new ReturnData();
            returnData.setStatus(1);
            returnData.setErrorMsg(e.getMessage());
            ResponseBody body = new StringBody(JSON.toJSONString(returnData));
            httpResponse.setBody(body);
        }

    }
}
