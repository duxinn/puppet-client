package com.mango.plugintest;

import android.content.Context;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.mango.puppet.network.NetworkManager;
import com.mango.puppet.network.api.api.ApiClient;
import com.mango.puppet.network.i.INetwork;
import com.mango.puppetmodel.Job;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class WechatPluginTest {

    /**
     * 测试步骤:
     *
     * 1 初始化发送网络请求的模块
     * 2 初始化本地服务器接收上报内容
     *
     */

    private static final String TAG = "WechatPluginTest";

    private boolean isNetworkOK = false;
    private boolean isFinished = false;
    private String ipString = "";
    private long jobId = 0l;

    @Before
    public void prepareNetwork() {

        Log.i(TAG, "prepareNetwork");
        // 初始化发送网络请求的模块
        ApiClient.Companion.getInstance().build();

        // 初始化本地服务器接收上报内容
        isNetworkOK = false;
        isFinished = false;
        final CountDownLatch signal = new CountDownLatch(1);
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        TestServerManager.getInstance(appContext).register().startServer(new TestServerManager.ServerListener() {
            @Override
            public void onServerStart(String ip) {
                Log.i(TAG, "onServerStart");
                isNetworkOK = true;
                ipString = ip;
                signal.countDown();
            }

            @Override
            public void onServerError(String error) {
                Log.e(TAG, "onServerError");
                isNetworkOK = false;
                signal.countDown();
            }

            @Override
            public void onServerStop() {
                if (isFinished) {
                    Log.i(TAG, "onServerStop");
                } else {
                    Log.e(TAG, "onServerStop");
                }
                isNetworkOK = false;
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
        isNetworkOK = false;
        isFinished = true;
        Log.i(TAG, "server closed");
    }

    private void checkNetwork() {
        if (!isNetworkOK) {
            Log.e(TAG, "network is closed");
        }
    }

    @Test
    public void getLocalUserInfo() {
        checkNetwork();
        final CountDownLatch signal = new CountDownLatch(1);

        String jobString = "{\n" +
                "\t\"job_id\":1,\n" +
                "\t\"package_name\":\"com.tencent.mm\",\n" +
                "\t\"job_name\":\"get_local_user_info\",\n" +
                "\t\"callback\":\"\"\n" +
                "}";
        Job job = Job.fromString(jobString);
        job.job_id = jobId++;
        job.callback = "http://" + ipString + "/jobcallback";
        NetworkManager.getInstance().reportJobResult(job, new INetwork.IJobRequestResult() {
            @Override
            public void onSuccess(Job jobResult) {
                signal.countDown();
            }

            @Override
            public void onError(Job jobResult, int errorCode, String errorMessage) {
                Log.e(TAG, "add job error:" + errorMessage);
                isNetworkOK = false;
                signal.countDown();
            }

            @Override
            public void onNetworkError(Job jobResult) {
                Log.e(TAG, "add job network error");
                isNetworkOK = false;
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
