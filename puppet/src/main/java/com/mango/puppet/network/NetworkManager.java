package com.mango.puppet.network;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mango.puppet.dispatch.event.EventManager;
import com.mango.puppet.dispatch.job.JobManager;
import com.mango.puppet.dispatch.system.SystemManager;
import com.mango.puppet.log.LogManager;
import com.mango.puppet.network.api.api.ApiClient;
import com.mango.puppet.network.api.observerCallBack.DesCallBack;
import com.mango.puppet.network.api.vm.PuppetVM;
import com.mango.puppet.network.i.INetwork;
import com.mango.puppet.network.server.ServerManager;
import com.mango.puppet.network.server.model.ReturnData;
import com.mango.puppet.network.utils.JsonUtils;
import com.mango.puppet.network.wsmanager.WsManager;
import com.mango.puppet.network.wsmanager.listener.WsStatusListener;
import com.mango.puppet.status.StatusManager;
import com.mango.puppet.tool.ThreadUtils;
import com.mango.puppetmodel.Event;
import com.mango.puppetmodel.Job;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;

import java.util.ArrayList;

import okhttp3.Response;
import okio.ByteString;

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
    private final static String TAG = "NetworkManager";

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
    public void setupNetwork(final Context context, Boolean isLocalServer, ISetupResult result) {
        LogManager.getInstance().recordDebugLog("启动本地server/长连接");
        final ISetupResult[] iSetupResult = {result};

        if (isLocalServer) {
            if (status == SERVER_START) {
                if (result != null) {
                    result.onSuccess();
                    return;
                }
            }

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
        } else {
            WsManager.getInstance(context).setWsStatusListener(new WsStatusListener() {
                @Override
                public void onOpen(Response response) {
                    super.onOpen(response);
                    Log.d(TAG, "WsManager-----onOpen\n");
                    if (iSetupResult[0] != null) {
                        iSetupResult[0].onSuccess();
                        iSetupResult[0] = null;
                    }
                    StatusManager.getInstance().setNetworkStatus(SERVER_START);
                    status = SERVER_START;
                    LogManager.getInstance().recordLog("webSocket onOpen");
                }

                @Override
                public void onMessage(String text) {
                    super.onMessage(text);
//                    Log.d(TAG, "WsManager-----onMessage(String): " + text + "\n");
                    JSONObject object = JSON.parseObject(text);
                    String requestId = object.getString("request_id");
                    String type = object.getString("type");
                    Object data = object.get("data");

                    ReturnData returnData = new ReturnData();
                    int status = 0;
                    String message = "";
                    if (!(data instanceof com.alibaba.fastjson.JSONObject)) {
                        status = 1;
                        message = "参数为空";
                    }
                    if (status == 0) {
                        if ("setEventWatcher".equals(type)) {
                            final String event_name = ((com.alibaba.fastjson.JSONObject) data).getString("event_name");
                            final String package_name = ((com.alibaba.fastjson.JSONObject) data).getString("package_name");
                            final String callback = ((com.alibaba.fastjson.JSONObject) data).getString("callback");
                            final int watcher_status = ((com.alibaba.fastjson.JSONObject) data).getIntValue("watcher_status");
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
                                status = 0;
                            } else {
                                status = 1;
                                message = "参数错误";
                            }
                        } else if ("addJob".equals(type)) {
                            com.alibaba.fastjson.JSONObject jsonObject = (com.alibaba.fastjson.JSONObject)data;
                            long job_id = jsonObject.getLong("job_id");
                            String package_name = jsonObject.getString("package_name");
                            String job_name = jsonObject.getString("job_name");
                            String callback = jsonObject.getString("callback");
                            int no_repeat = jsonObject.getIntValue("no_repeat");
                            int fail_stop = jsonObject.getIntValue("fail_stop");
                            org.json.JSONObject job_data = null;
                            try {
                                job_data = new org.json.JSONObject(JsonUtils.toJsonString(jsonObject.getJSONObject("job_data")));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            if (!TextUtils.isEmpty(package_name)
                                    && !TextUtils.isEmpty(job_name)
                                    && !TextUtils.isEmpty(callback)) {
                                Job job = new Job();
                                job.job_id = job_id;
                                job.package_name = package_name;
                                job.job_name = job_name;
                                job.callback = callback;
                                job.no_repeat = no_repeat;
                                job.fail_stop = fail_stop;
                                if (job_data != null) {
                                    job.job_data = job_data;
                                } else {
                                    job.job_data = new org.json.JSONObject();
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
                    }

                    returnData.deviceid = SystemManager.getInstance().getDeviceId();
                    returnData.response_id = requestId;
                    returnData.status = status;
                    returnData.message = message;
                    WsManager.getInstance(context).sendMessage(JSONObject.toJSONString(returnData));
                }

                @Override
                public void onMessage(ByteString bytes) {
                    super.onMessage(bytes);
//                    Log.d(TAG, "WsManager-----onMessage(ByteString): " + bytes + "\n");
                }

                @Override
                public void onReconnect() {
                    super.onReconnect();
                    Log.d(TAG, "WsManager-----onReconnect\n");
                }

                @Override
                public void onClosing(int code, String reason) {
                    super.onClosing(code, reason);
                    Log.d(TAG, "WsManager-----onClosing reason: " + reason + "\n");
                }

                @Override
                public void onClosed(int code, String reason) {
                    super.onClosed(code, reason);
                    Log.d(TAG, "WsManager-----onClosed reason: " + reason + "\n");
                    StatusManager.getInstance().setNetworkStatus(SERVER_STOP);
                    status = SERVER_STOP;
                    LogManager.getInstance().recordLog("webSocket onClosed");
                }

                @Override
                public void onFailure(Throwable t, Response response) {
                    super.onFailure(t, response);
                    Log.d(TAG, "WsManager-----onFailure Throwable: " + t.getMessage());
                    if (iSetupResult[0] != null) {
                        iSetupResult[0].onFailure();
                        iSetupResult[0] = null;
                    }
                    StatusManager.getInstance().setNetworkStatus(SERVER_ERROR);
                    status = SERVER_ERROR;
                    LogManager.getInstance().recordLog("webSocket onFailure");
                }
            }).startConnect();
        }
    }

    @Override
    public void reportJobResult(final Job jobResult, final IJobRequestResult iJobRequestResult) {
        LogManager.getInstance().recordDebugLog("上报任务结果" + jobResult.job_id);
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
                LogManager.getInstance().recordLog(jobResult.job_name + "任务上报失败" + code + msg);
            }

            @Override
            public void onNetWorkError(@Nullable Throwable e) {
                if (iJobRequestResult != null) {
                    iJobRequestResult.onNetworkError(jobResult);
                    LogManager.getInstance().recordLog(jobResult.job_name + "网络错误" + e.getLocalizedMessage());
                }
            }
        });
    }

    @Override
    public void reportEvent(String url, final Event event, final IEventRequestResult requestResult) {
        LogManager.getInstance().recordDebugLog("上报新事件" + event.event_name);
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
                    LogManager.getInstance().recordLog(event.event_name + "事件上报失败" + code + msg);
                }
            }

            @Override
            public void onNetWorkError(@Nullable Throwable e) {
                if (requestResult != null) {
                    requestResult.onNetworkError(event);
                    LogManager.getInstance().recordLog(event.event_name + "网络错误" + e.getLocalizedMessage());
                }
            }
        });
    }

    @Override
    public void requestUploadResourceWay(ArrayList<String> supportChannels, IRequestResult requestResult) {
        // TODO 提供供本地客户端调用的接口
    }
}
