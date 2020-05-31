package com.mango.puppet.network;

import android.content.Context;

import com.mango.puppet.log.LogManager;
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
public class NetworkManager implements INetwork {
    private static final NetworkManager ourInstance = new NetworkManager();

    public static NetworkManager getInstance() {
        return ourInstance;
    }

    private int status = SERVER_STOP;

    private NetworkManager() {
    }

    /************   INetwork   ************/


    @Override
    public void setupApi() {
        // 初始化网络
        ApiClient.Companion.getInstance().build();
    }


    @Override
    public void setupNetwork(Context context, ISetupResult result) {

        if (status == SERVER_START) {
            if (result != null) {
                result.onSuccess();
                return;
            }
        }

        final ISetupResult[] iSetupResult = {result};

        // 开启服务
        ServerManager.getInstance(context).register().startServer(new ServerManager.ServerListener() {
            @Override
            public void onServerStart(String ip) {
                if (iSetupResult[0] != null) {
                    iSetupResult[0].onSuccess();
                    iSetupResult[0] = null;
                }
                StatusManager.getInstance().setNetworkStatus(SERVER_START);
                status = SERVER_START;
            }

            @Override
            public void onServerError(String error) {
                if (iSetupResult[0] != null) {
                    iSetupResult[0].onFailure();
                    iSetupResult[0] = null;
                }
                StatusManager.getInstance().setNetworkStatus(SERVER_ERROR);
                status = SERVER_ERROR;
            }

            @Override
            public void onServerStop() {
                StatusManager.getInstance().setNetworkStatus(SERVER_STOP);
                status = SERVER_STOP;
            }
        });
    }

    @Override
    public void reportJobResult(final Job jobResult, final IJobRequestResult iJobRequestResult) {
        PuppetVM.Companion.reportJobResult(jobResult.callback, jobResult, new DesCallBack<Object>() {
            @Override
            public void onHandleSuccess(@Nullable Object objectBaseModel) {
                if (iJobRequestResult != null) {
                    iJobRequestResult.onSuccess(jobResult);
                }
            }

            @Override
            public void onHandleError(@Nullable String msg, int code) {
                if (iJobRequestResult != null) {
                    iJobRequestResult.onError(jobResult, code, msg);
                }
                LogManager.getInstance().recordLog(jobResult.job_name+"任务上报失败"+code+msg);
            }

            @Override
            public void onNetWorkError(@Nullable Throwable e) {
                if (iJobRequestResult != null) {
                    iJobRequestResult.onNetworkError(jobResult);
                    LogManager.getInstance().recordLog(jobResult.job_name+"网络错误"+e.getLocalizedMessage());
                }
            }
        });
    }

    @Override
    public void reportEvent(String url, final Event event, final IEventRequestResult requestResult) {
        PuppetVM.Companion.reportEvent(url, event, new DesCallBack<Object>() {
            @Override
            public void onHandleSuccess(@Nullable Object objectBaseModel) {
                if (requestResult != null) {
                    requestResult.onSuccess(event);
                }
            }

            @Override
            public void onHandleError(@Nullable String msg, int code) {
                if (requestResult != null) {
                    requestResult.onError(event, code, msg);
                    LogManager.getInstance().recordLog(event.event_name+"事件上报失败"+code+msg);
                }
            }

            @Override
            public void onNetWorkError(@Nullable Throwable e) {
                if (requestResult != null) {
                    requestResult.onNetworkError(event);
                    LogManager.getInstance().recordLog(event.event_name+"网络错误"+e.getLocalizedMessage());
                }
            }
        });
    }

    @Override
    public void requestUploadResourceWay(ArrayList<String> supportChannels, IRequestResult requestResult) {
        // TODO 提供供本地客户端调用的接口
    }
}
