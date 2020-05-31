package com.mango.puppet.network;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.mango.puppet.network.api.api.ApiClient;
import com.mango.puppet.network.api.observerCallBack.DesCallBack;
import com.mango.puppet.network.api.vm.PuppetVM;
import com.mango.puppet.network.i.INetwork;
import com.mango.puppet.network.server.ServerManager;
import com.mango.puppet.status.StatusManager;
import com.mango.puppetmodel.Event;
import com.mango.puppetmodel.Job;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static com.mango.puppet.status.StatusManager.SERVER_ERROR;
import static com.mango.puppet.status.StatusManager.SERVER_START;
import static com.mango.puppet.status.StatusManager.SERVER_STOP;

/**
 * NetworkManager
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public class NetworkManager implements INetwork{
    private static final NetworkManager ourInstance = new NetworkManager();

    public static NetworkManager getInstance() {
        return ourInstance;
    }

    private NetworkManager() {
    }

    /************   INetwork   ************/
    @Override
    public void setupNetwork(Context context, ISetupResult result) {

        final ISetupResult[] iSetupResult = {result};

        // 初始化网络
        ApiClient.Companion.getInstance().build();
        // 开启服务
        ServerManager.getInstance(context).register().startServer(new ServerManager.ServerListener() {
            @Override
            public void onServerStart(String ip) {
                if (iSetupResult[0] != null) {
                    iSetupResult[0].onSuccess();
                    iSetupResult[0] = null;
                }
                StatusManager.getInstance().setNetworkStatus(SERVER_START);
            }

            @Override
            public void onServerError(String error) {
                if (iSetupResult[0] != null) {
                    iSetupResult[0].onFailure();
                    iSetupResult[0] = null;
                }
                StatusManager.getInstance().setNetworkStatus(SERVER_ERROR);
            }

            @Override
            public void onServerStop() {
                StatusManager.getInstance().setNetworkStatus(SERVER_STOP);
            }
        });
    }

    @Override
    public void reportJobResult(final Job jobResult, final IJobRequestResult iJobRequestResult) {
        String jobJsonString = JSON.toJSONString(jobResult);
        PuppetVM.Companion.reportJobResult(jobResult.callback, jobJsonString, new DesCallBack<Object>() {
            @Override
            public void onHandleSuccess(@Nullable Object objectBaseModel) {
                iJobRequestResult.onSuccess(jobResult);
            }

            @Override
            public void onHandleError(@Nullable String msg, int code) {
                iJobRequestResult.onError(jobResult, code, msg);
            }

            @Override
            public void onNetWorkError(@Nullable Throwable e) {
                iJobRequestResult.onNetworkError(jobResult);
            }
        });
    }

    @Override
    public void reportEvent(String url, final Event event, final IEventRequestResult requestResult) {
        String eventJsonString = JSON.toJSONString(event);
        PuppetVM.Companion.reportEvent(url, eventJsonString, new DesCallBack<Object>() {
            @Override
            public void onHandleSuccess(@Nullable Object objectBaseModel) {
                requestResult.onSuccess(event);
            }

            @Override
            public void onHandleError(@Nullable String msg, int code) {
                requestResult.onError(event, code, msg);
            }

            @Override
            public void onNetWorkError(@Nullable Throwable e) {
                requestResult.onNetworkError(event);
            }
        });
    }

    @Override
    public void requestUploadResourceWay(ArrayList<String> supportChannels, IRequestResult requestResult) {
        // TODO 提供供本地客户端调用的接口
    }
}
