package com.mango.puppet.network.server.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.mango.puppet.dispatch.event.EventManager;
import com.mango.puppet.dispatch.job.JobManager;

import com.mango.puppetmodel.EventWatcher;
import com.mango.puppetmodel.Job;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestMapping;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.http.HttpResponse;
import com.yanzhenjie.andserver.util.MediaType;

import org.jetbrains.annotations.NotNull;

@RestController
@RequestMapping(path = "/dispatch")
class PuppetController {

    /**
     * 注册/注销事件监听
     *
     * @param response
     * @param eventWatcherJson
     * @return
     */
    @PostMapping(path = "/setEventWatcher", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    void distributeEventWatcher(HttpResponse response, @RequestParam(name = "event_watcher") String eventWatcherJson) {
        final EventWatcher eventWatcher = JSON.parseObject(eventWatcherJson, new TypeReference<EventWatcher>() {});
        EventManager.getInstance().setEventWatcher(eventWatcher.package_name, eventWatcher.event_name, eventWatcher.watcher_status == 1);
    }

    /**
     * 分发任务
     *
     * @param response
     * @param jobJson
     */
    @PostMapping(path = "/addJob", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    void distributeJob(HttpResponse response, @RequestParam(name = "job") String jobJson) {
        final Job job = JSON.parseObject(jobJson, new TypeReference<Job>() {});
        JobManager.getInstance().addJob(job);
    }
}
