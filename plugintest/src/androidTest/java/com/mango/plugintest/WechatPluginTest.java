package com.mango.plugintest;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.gson.Gson;
import com.mango.puppet.network.api.api.ApiClient;
import com.mango.puppet.network.api.observerCallBack.DesCallBack;
import com.mango.puppetmodel.Job;
import com.mango.puppetmodel.wechat.User;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class WechatPluginTest {

    /**
     * 测试步骤:
     * <p>
     * 1 初始化发送网络请求的模块
     * 2 初始化本地服务器接收上报内容
     */

    public static final String TAG = "WechatPluginTest";

    private boolean isEverythingOK = false;
    private boolean isFinished = false;
    private String ipString = "";

    @Before
    public void prepareNetwork() {

        Log.i(TAG, "prepareNetwork");

        // 初始化发送网络请求的模块
        ApiClient.Companion.getInstance().build();

        // 初始化本地服务器接收上报内容
        isEverythingOK = false;
        isFinished = false;
        final CountDownLatch signal = new CountDownLatch(1);
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        RequestHandler.getInstance().setContext(appContext);
        RequestHandler.getInstance().register();
        TestServerManager.getInstance(appContext).register().startServer(new TestServerManager.ServerListener() {
            @Override
            public void onServerStart(String ip) {
                Log.i(TAG, "onServerStart");
                isEverythingOK = true;
                ipString = ip;
                signal.countDown();
            }

            @Override
            public void onServerError(String error) {
                Log.e(TAG, "onServerError");
                isEverythingOK = false;
                signal.countDown();
            }

            @Override
            public void onServerStop() {
                if (isFinished) {
                    Log.i(TAG, "onServerStop");
                } else {
                    Log.e(TAG, "onServerStop");
                }
                isEverythingOK = false;
                signal.countDown();
            }
        });

        try {
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.e(TAG, "signal.await InterruptedException");
        }

    }

    @After
    public void closeNetwork() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        TestServerManager.getInstance(appContext).stopServer();
        isEverythingOK = false;
        isFinished = true;
        Log.i(TAG, "server closed");
    }

    private void checkPrevious() {
        if (!isEverythingOK) {
            Log.e(TAG, "something wrong");
        }
    }

    @Test
    public void getLocalUserInfo() {
        checkPrevious();
        final CountDownLatch signal = new CountDownLatch(1);

        String jobString = "{\n" +
                "\t\"job_id\":1,\n" +
                "\t\"package_name\":\"com.tencent.mm\",\n" +
                "\t\"job_name\":\"get_local_user_info\",\n" +
                "\t\"callback\":\"\"\n" +
                "}";
        Job job = Job.fromString(jobString);
        RequestHandler.getInstance().dealRequest(ipString, job, new DesCallBack<Object>() {
            @Override
            public void onHandleSuccess(@Nullable Object objectBaseModel) {
                Log.i(TAG, "add job ok");
            }

            @Override
            public void onHandleError(@Nullable String msg, int code) {
                Log.e(TAG, "add job error:" + msg);
                isEverythingOK = false;
                signal.countDown();
            }

            @Override
            public void onNetWorkError(@Nullable Throwable e) {
                Log.e(TAG, "add job network error");
                isEverythingOK = false;
                signal.countDown();
            }
        }, new ResultCallBack() {
            @Override
            public void onHandleResponseSuccess(Job job) {

                String result = job.result_data;
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONObject userObject = jsonObject.optJSONObject("user");
                User user = new Gson().fromJson(userObject.toString(), User.class);
                boolean isOK = false;
                if (!TextUtils.isEmpty(user.field_alias)
                        && !TextUtils.isEmpty(user.field_encryptUsername)
                        && !TextUtils.isEmpty(user.field_nickname)
                        && !TextUtils.isEmpty(user.field_pyInitial)
                        && !TextUtils.isEmpty(user.field_quanPin)
                        && !TextUtils.isEmpty(user.field_username)
                        && !TextUtils.isEmpty(user.icon_url)) {
                    isOK = true;
                }
                if (isOK) {
                    Log.i(TAG, job.job_name + " everything is OK");
                } else {
                    Log.e(TAG, job.job_name + " sth wrong");
                    isEverythingOK = false;
                }
                signal.countDown();
            }
        });

        try {
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.e(TAG, "signal.await InterruptedException");
        }
    }

}
