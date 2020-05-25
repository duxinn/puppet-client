package com.mango.puppet.network;

import android.content.Context;
import android.text.TextUtils;

import com.mango.puppet.network.api.api.ApiClient;
import com.mango.puppet.network.api.observerCallBack.DesCallBack;
import com.mango.puppet.network.api.vm.PuppetVM;
import com.mango.puppet.network.dto.BaseDTO;
import com.mango.puppet.network.i.INetwork;
import com.mango.puppet.network.server.ServerManager;
import com.mango.puppetmodel.Event;
import com.mango.puppetmodel.Job;

import org.jetbrains.annotations.NotNull;

/**
 * NetworkManager
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public class NetworkManager implements INetwork {
    private static final NetworkManager ourInstance = new NetworkManager();

    public static NetworkManager getInstance() {
        return ourInstance;
    }

    private NetworkManager() {
    }

    /************   INetwork   ************/
    @Override
    public void setupNetwork(Context context, ISetupResult result) {
        // 初始化网络
        ApiClient.Companion.getInstance().build();
        // 开启服务
        ServerManager.getInstance(context).register().startServer();
    }

    @Override
    public void reportJobResult(Job jobResult, IJobRequestResult requestResult) {
        PuppetVM.Companion.reportJobResult(jobResult.callback, jobResult.job_id, jobResult.package_name, jobResult.job_name, jobResult.job_status, jobResult.error_code, jobResult.error_message, jobResult.result_data, new DesCallBack<BaseDTO>() {
            @Override
            public void success(BaseDTO any) {
                if (any.isSuccess()) {
                    // todo 上报任务成功后操作
                } else {
                    // todo 上报任务失败（非网络原因）
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

    @Override
    public void reportEvent(String url, Event event, IEventRequestResult requestResult) {
        PuppetVM.Companion.reportEvent(url, event.event_name, event.package_name, event.event_data, new DesCallBack<BaseDTO>() {
            @Override
            public void success(BaseDTO any) {
                if (any.isSuccess()) {
                    // todo 上报事件成功后操作
                } else {
                    // todo 上报事件失败（非网络原因）
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

    @Override
    public void requestUploadResourceWay(IRequestResult requestResult) {

    }
}
