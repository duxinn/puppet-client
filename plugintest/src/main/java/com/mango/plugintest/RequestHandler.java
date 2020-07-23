package com.mango.plugintest;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import com.mango.puppet.network.api.observerCallBack.DesCallBack;
import com.mango.puppet.network.api.vm.PuppetVM;
import com.mango.puppet.network.server.CoreService;
import com.mango.puppet.network.utils.JsonUtils;
import com.mango.puppetmodel.Job;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * RequestHandler
 *
 * @author: hehongzhen
 * @date: 2020/07/21
 */
class RequestHandler extends BroadcastReceiver {

    public static final String RequestHandlerAction = "RequestHandlerAction";
    private static long jobId = System.currentTimeMillis();
    @SuppressLint("StaticFieldLeak")
    private static final RequestHandler instance = new RequestHandler();

    private HashMap<String, CallbackBean> resultCallBackHashMap = new HashMap<>();
    private Context context;

    public static RequestHandler getInstance() {
        return instance;
    }

    public void register() {
        IntentFilter filter = new IntentFilter(RequestHandlerAction);
        context.registerReceiver(this, filter);
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public void dealRequest(String ipString, Job job, boolean isDoubleStep, DesCallBack<Object> requestCallback, ResultCallBack resultCallBack) {
        job.job_id = jobId++;
        job.no_repeat = 1;
        job.callback = "http://" + ipString + ":" + TestService.PORT + "/test/receive_job_result";
        String url = "http://" + ipString + ":" + CoreService.PORT + "/dispatch/addJob";
        CallbackBean callbackBean = new CallbackBean();
        callbackBean.isDoubleStep = isDoubleStep;
        callbackBean.resultCallBack = resultCallBack;
        resultCallBackHashMap.put(String.valueOf(job.job_id), callbackBean);
        PuppetVM.Companion.reportJobResult(url, job, requestCallback);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (RequestHandlerAction.equals(action)) {
            HashMap map = (HashMap) intent.getSerializableExtra("result");
            com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject(map);

            long job_id = jsonObject.getLong("job_id");
            String package_name = jsonObject.getString("package_name");
            String job_name = jsonObject.getString("job_name");
            String callback = jsonObject.getString("callback");
            int noRepeat = jsonObject.getIntValue("no_repeat");
            int job_status = jsonObject.getIntValue("job_status");
            String result_data = jsonObject.getString("result_data");
            JSONObject job_data = null;
            try {
                job_data = new JSONObject(JsonUtils.toJsonString(jsonObject.getJSONObject("job_data")));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Job job = new Job();
            job.job_id = job_id;
            job.package_name = package_name;
            job.job_name = job_name;
            job.no_repeat = noRepeat;
            job.callback = callback;
            job.job_status = job_status;
            job.result_data = result_data;
            if (job_data != null) {
                job.job_data = job_data;
            } else {
                job.job_data = new JSONObject();
            }

            CallbackBean callbackBean = resultCallBackHashMap.get(String.valueOf(job_id));
            assert callbackBean != null;
            callbackBean.resultCallBack.onHandleResponseSuccess(job);
            callbackBean.currentCallbackTimes = callbackBean.currentCallbackTimes + 1;
            if (!callbackBean.isDoubleStep || callbackBean.currentCallbackTimes >= 2) {
                final String jobId = String.valueOf(job_id);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        resultCallBackHashMap.remove(jobId);
                    }
                }, 5000);
            }
        }
    }

    public static class CallbackBean {

        ResultCallBack resultCallBack;
        boolean isDoubleStep = false;
        int currentCallbackTimes = 0;
    }
}
