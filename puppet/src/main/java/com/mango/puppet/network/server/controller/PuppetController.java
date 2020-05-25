package com.mango.puppet.network.server.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.mango.puppet.network.api.observerCallBack.DesCallBack;
import com.mango.puppet.network.api.vm.PuppetVM;
import com.mango.puppet.network.dto.BaseDTO;
import com.mango.puppet.plugin.PluginManager;
import com.mango.puppet.plugin.i.IPluginControl;
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
     * @param response
     * @param eventWatcherJson
     * @return
     */
    @PostMapping(path = "/distributeEventWatcher", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    void distributeEventWatcher(HttpResponse response, @RequestParam(name = "event_watcher") String eventWatcherJson) {
        final EventWatcher eventWatcher = JSON.parseObject(eventWatcherJson, new TypeReference<EventWatcher>() {});
        PluginManager.getInstance().distributeEventWatcher(eventWatcher, new IPluginControl.IPluginControlResult() {
            @Override
            public void onFinished(boolean isSucceed, String failReason) {
                eventWatcher.error_code = isSucceed ? 0 : 1;
                eventWatcher.error_message = failReason;
                String eventWatcherJsonString = JSON.toJSONString(eventWatcher);
                PuppetVM.Companion.reportEventWatcherCallBack(eventWatcher.call_back, eventWatcherJsonString, new DesCallBack<BaseDTO>() {
                    @Override
                    public void success(BaseDTO any) {
                        if (any.isSuccess()) {
                            // todo 注册或注销事件监听callback回调成功
                        } else {
                            // todo 注册或注销事件监听callback回调失败（非网络原因）
                        }
                    }

                    @Override
                    public void failed(@NotNull Throwable e) {

                    }

                    @Override
                    public void onSubscribe() {

                    }
                });
            }
        });
    }

    /**
     * 分发任务
     * @param response
     * @param jobJson
     */
    @PostMapping(path = "/distributeJob", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    void distributeJob(HttpResponse response, @RequestParam(name = "job") String jobJson) {
            final Job job = JSON.parseObject(jobJson, new TypeReference<Job>() {});
            PluginManager.getInstance().distributeJob(job, new IPluginControl.IPluginControlResult() {
                @Override
                public void onFinished(boolean isSucceed, String failReason) {
                    job.error_code = isSucceed ? 0 : 1;
                    job.error_message = failReason;
                    String jobJsonString = JSON.toJSONString(job);
                    PuppetVM.Companion.reportJobResult(job.callback, jobJsonString, new DesCallBack<BaseDTO>() {
                        @Override
                        public void success(BaseDTO any) {
                            if (any.isSuccess()) {
                                // todo 分发任务callback回调成功
                            } else {
                                // todo 分发任务callback回调失败（非网络原因）
                            }
                        }

                        @Override
                        public void failed(@NotNull Throwable e) {

                        }

                        @Override
                        public void onSubscribe() {

                        }
                    });
                }
            });
    }
}
