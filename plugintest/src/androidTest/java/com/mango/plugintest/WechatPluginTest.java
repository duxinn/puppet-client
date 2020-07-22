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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
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

    private static boolean isEverythingOK = false;
    private static boolean isFinished = false;
    private static String ipString = "";

    private static String selfWxid = "";

    @BeforeClass
    public static void prepareNetwork() {

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

    @AfterClass
    public static void closeNetwork() {
        isEverythingOK = false;
        isFinished = true;
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        TestServerManager.getInstance(appContext).stopServer();
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
                "\t\"job_id\":0,\n" +
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
                        && !TextUtils.isEmpty(user.field_nickname)
                        && !TextUtils.isEmpty(user.field_pyInitial)
                        && !TextUtils.isEmpty(user.field_quanPin)
                        && !TextUtils.isEmpty(user.field_username)
                        && !TextUtils.isEmpty(user.icon_url)) {
                    isOK = true;
                }
                if (isOK) {
                    selfWxid = user.field_username;
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

    @Test
    public void getUserList() {
        checkPrevious();
        final CountDownLatch signal = new CountDownLatch(1);

        String jobString = "{\n" +
                "\t\"job_id\":0,\n" +
                "\t\"package_name\":\"com.tencent.mm\",\n" +
                "\t\"job_name\":\"get_user_list\",\n" +
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
                JSONArray jsonArray = jsonObject.optJSONArray("users");
                ArrayList<User> userArrayList = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        User user = new Gson().fromJson(jsonArray.get(i).toString(), User.class);
                        userArrayList.add(user);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                boolean isOK = true;
                boolean hasAlias = false;
                boolean hasLocalIcon = false;
                for (User user : userArrayList) {
                    if (TextUtils.isEmpty(user.field_nickname)
                            || TextUtils.isEmpty(user.field_pyInitial)
                            || TextUtils.isEmpty(user.field_quanPin)
                            || TextUtils.isEmpty(user.field_username)) {
                        isOK = false;
                        Log.e(TAG, "\n" + "user.field_username:" + user.field_username
                                + "\n" + "user.field_alias:" + user.field_alias
                                + "\n" + "user.field_nickname:" + user.field_nickname
                                + "\n" + "user.field_pyInitial:" + user.field_pyInitial
                                + "\n" + "user.field_quanPin:" + user.field_quanPin
                                + "\n" + "user.icon_url:" + user.icon_url);
                        break;
                    }
                    if (!user.field_username.equals(selfWxid)) {
                        if (TextUtils.isEmpty(user.field_encryptUsername)) {
                            isOK = false;
                            Log.e(TAG, "\n" + "user.field_username:" + user.field_username
                                    + "\n" + "user.field_encryptUsername:" + user.field_encryptUsername);
                            break;
                        }
                    }

                    if (!TextUtils.isEmpty(user.field_alias)) {
                        hasAlias = true;
                    }

                    if (!TextUtils.isEmpty(user.icon_url)) {
                        hasLocalIcon = true;
                    }
                }

                if (!hasAlias) {
                    isOK = false;
                    Log.e(TAG, job.job_name + " no one has alias");
                }

                if (!hasLocalIcon) {
                    isOK = false;
                    Log.e(TAG, job.job_name + " no one has icon_url");
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
