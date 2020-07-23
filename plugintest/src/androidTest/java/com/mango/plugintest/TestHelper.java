package com.mango.plugintest;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.test.platform.app.InstrumentationRegistry;

import com.mango.puppet.network.api.api.ApiClient;
import com.mango.puppet.network.api.observerCallBack.DesCallBack;
import com.mango.puppetmodel.Job;

import org.junit.Assert;

import java.util.concurrent.CountDownLatch;

/**
 * TestHelper
 *
 * @author: hehongzhen
 * @date: 2020/07/23
 */
class TestHelper {

    public static final String TAG = "TestHelper";

    private static boolean isEverythingOK = false;
    private static boolean isFinished = false;
    private static String ipString = "";

    public static void beforeClass() {

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

    public static void afterClass() {
        isEverythingOK = false;
        isFinished = true;
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        TestServerManager.getInstance(appContext).stopServer();
        Log.i(TAG, "server closed");
    }

    public static boolean isSomethingWrong() {
        if (!isEverythingOK) {
            Log.e(TAG, "something wrong");
        }
        return isEverythingOK;
    }

    public static void setSomethingWrong() {
        isEverythingOK = false;
    }

    public static void logI(String string) {
        if (!TextUtils.isEmpty(string)) {
            Log.i(TAG, string);
        } else {
            Log.e(TAG, "logI null");
        }
    }

    public static void logE(String string) {
        if (!TextUtils.isEmpty(string)) {
            Log.e(TAG, string);
        } else {
            Log.e(TAG, "logE null");
        }
    }

    public static void testSingleStepJob(final Job originJob, final TestSingleStepJobHandler testSingleStepJobHandler) {
        if (!isSomethingWrong()) {
            return;
        }

        if (testSingleStepJobHandler == null) {
            Log.e(TAG, "testJobHandler null");
            return;
        }

        if (originJob == null) {
            Log.e(TAG, "job null");
            return;
        }

        final int[] callbackTimes = {0};
        final CountDownLatch signal = new CountDownLatch(1);
        RequestHandler.getInstance().dealRequest(ipString, originJob, false, new DesCallBack<Object>() {
            @Override
            public void onHandleSuccess(@Nullable Object objectBaseModel) {
                Log.i(TAG, "add job ok:" + originJob.job_name);
            }

            @Override
            public void onHandleError(@Nullable String msg, int code) {
                Log.e(TAG, "add job server error:" +  originJob.job_name + " msg:" + msg);
                isEverythingOK = false;
                signal.countDown();
            }

            @Override
            public void onNetWorkError(@Nullable Throwable e) {
                Log.e(TAG, "add job network error:" + originJob.job_name);
                isEverythingOK = false;
                signal.countDown();
            }
        }, new ResultCallBack() {
            @Override
            public void onHandleResponseSuccess(Job job) {
                callbackTimes[0]++;
                if (callbackTimes[0] == 1) {

                    if (!originJob.job_name.equals(job.job_name)) {
                        TestHelper.logE("job name not equal:" + originJob.job_name);
                        TestHelper.setSomethingWrong();
                    }

                    if (job.job_status != 3) {
                        TestHelper.logE("job status wrong:" + originJob.job_name + " status:" + job.job_status);
                        TestHelper.setSomethingWrong();
                    }

                    testSingleStepJobHandler.onSingleStepSuccess(job);
                } else {
                    Log.e(TAG, "callback more times:" + job.job_name);
                    setSomethingWrong();
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        signal.countDown();
                    }
                }, 2000);
            }
        });

        try {
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.e(TAG, "signal.await InterruptedException");
        }
    }

    public static void testDoubleStepJob(final Job originJob, final TestDoubleStepJobHandler testDoubleStepJobHandler) {
        if (!isSomethingWrong()) {
            return;
        }

        if (testDoubleStepJobHandler == null) {
            Log.e(TAG, "testJobHandler null");
            return;
        }

        if (originJob == null) {
            Log.e(TAG, "job null");
            return;
        }

        final int[] callbackTimes = {0};
        final CountDownLatch signal = new CountDownLatch(1);
        RequestHandler.getInstance().dealRequest(ipString, originJob, true, new DesCallBack<Object>() {
            @Override
            public void onHandleSuccess(@Nullable Object objectBaseModel) {
                Log.i(TAG, "add job ok:" + originJob.job_name);
            }

            @Override
            public void onHandleError(@Nullable String msg, int code) {
                Log.e(TAG, "add job server error:" +  originJob.job_name + " msg:" + msg);
                isEverythingOK = false;
                signal.countDown();
            }

            @Override
            public void onNetWorkError(@Nullable Throwable e) {
                Log.e(TAG, "add job network error:" + originJob.job_name);
                isEverythingOK = false;
                signal.countDown();
            }
        }, new ResultCallBack() {
            @Override
            public void onHandleResponseSuccess(Job job) {
                callbackTimes[0]++;
                if (callbackTimes[0] == 1) {

                    if (!originJob.job_name.equals(job.job_name)) {
                        TestHelper.logE("job name not equal:" + originJob.job_name);
                        TestHelper.setSomethingWrong();
                    }

                    if (job.job_status != 2) {
                        TestHelper.logE("job status wrong:" + originJob.job_name + " status:" + job.job_status);
                        TestHelper.setSomethingWrong();
                    }

                    testDoubleStepJobHandler.onFirstStepSuccess(job);
                    Log.i(TAG, "first step:" + job.job_name);
                } else if (callbackTimes[0] == 2){

                    if (!originJob.job_name.equals(job.job_name)) {
                        TestHelper.logE("job name not equal:" + originJob.job_name);
                        TestHelper.setSomethingWrong();
                    }

                    if (job.job_status != 3) {
                        TestHelper.logE("job status wrong:" + originJob.job_name + " status:" + job.job_status);
                        TestHelper.setSomethingWrong();
                    }

                    Log.i(TAG, "second step:" + job.job_name);
                    testDoubleStepJobHandler.onSecondStepSuccess(job);
                } else {
                    Log.e(TAG, "callback more times:" + job.job_name);
                    setSomethingWrong();
                }
                if (callbackTimes[0] == 2) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            signal.countDown();
                        }
                    }, 3000);
                }
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
