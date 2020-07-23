package com.mango.plugintest;

import android.text.TextUtils;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.gson.Gson;
import com.mango.puppetmodel.Job;
import com.mango.puppetmodel.wechat.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WechatPluginInfoTest {

    /**
     * 测试用例编写文档
     * <p>
     * 1 在开始跑测试前 @BeforeClass中调用TestHelper.beforeClass(); 用以启动与puppetsystem通信的网络模块
     * <p>
     * 2 在测试结束后 @AfterClass中调用TestHelper.afterClass(); 用以关闭与puppetsystem通信的网络模块
     * <p>
     * 3 测试任务时 单步任务调用testSingleStepJob() 双步任务调用testDoubleStepJob()
     * 只需构造Job的package_name、 job_name、 job_data即可,Job的其余参数会自动补全
     * 在返回Job结果后 需分析数据并打印日志
     * 正确逻辑处打日志调用TestHelper.logI()
     * 错误逻辑处打日志调用TestHelper.logE()
     * 如果Job 返回的数据有问题则需调用TestHelper.setSomethingWrong();
     * <p>
     * 4 测试任务顺序根据方法名称进行比较 当一个测试未通过时 将会自动停止后续测试
     * <p>
     * 5 若后边的测试需要用到前面测试得到的参数 可设置成员变量 但必须为静态变量
     * <p>
     * 6 所有日志未出现错误日志视为测试通过
     * <p>
     * 7 不允许出现警告
     */

    private static String selfWxid = "";

    @BeforeClass
    public static void beforeTest() {
        TestHelper.beforeClass();
    }

    @AfterClass
    public static void afterTest() {
        TestHelper.afterClass();
    }

    @Test
    public void aaGetLocalUserInfo() {
        String jobString = "{\n" +
                "\t\"job_id\":0,\n" +
                "\t\"package_name\":\"com.tencent.mm\",\n" +
                "\t\"job_name\":\"get_local_user_info\",\n" +
                "\t\"callback\":\"\"\n" +
                "}";
        Job job = Job.fromString(jobString);
        TestHelper.testSingleStepJob(job, new TestSingleStepJobHandler() {
            @Override
            public void onSingleStepSuccess(Job job) {
                String result = job.result_data;
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Assert.assertNotNull(jsonObject);
                JSONObject userObject = jsonObject.optJSONObject("user");
                Assert.assertNotNull(userObject);
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
                    TestHelper.logI(job.job_name + " everything is OK");
                } else {
                    TestHelper.logE(job.job_name + " sth wrong");
                    TestHelper.setSomethingWrong();
                }
            }
        });
    }

    @Test
    public void abGetUserList() {
        String jobString = "{\n" +
                "\t\"job_id\":0,\n" +
                "\t\"package_name\":\"com.tencent.mm\",\n" +
                "\t\"job_name\":\"get_user_list\",\n" +
                "\t\"callback\":\"\"\n" +
                "}";
        Job job = Job.fromString(jobString);
        TestHelper.testSingleStepJob(job, new TestSingleStepJobHandler() {
            @Override
            public void onSingleStepSuccess(Job job) {
                String result = job.result_data;
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Assert.assertNotNull(jsonObject);
                JSONArray jsonArray = jsonObject.optJSONArray("users");
                ArrayList<User> userArrayList = new ArrayList<>();
                Assert.assertNotNull(jsonArray);
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
                for (User user : userArrayList) {
                    if (TextUtils.isEmpty(user.field_nickname)
                            || TextUtils.isEmpty(user.field_pyInitial)
                            || TextUtils.isEmpty(user.field_quanPin)
                            || TextUtils.isEmpty(user.field_username)
                            || TextUtils.isEmpty(user.icon_url)) {
                        TestHelper.logE("\n" + "user.field_username:" + user.field_username
                                + "\n" + "user.field_alias:" + user.field_alias
                                + "\n" + "user.field_nickname:" + user.field_nickname
                                + "\n" + "user.field_pyInitial:" + user.field_pyInitial
                                + "\n" + "user.field_quanPin:" + user.field_quanPin
                                + "\n" + "user.icon_url:" + user.icon_url);
                    }
                    if (!user.field_username.equals(selfWxid)) {
                        if (TextUtils.isEmpty(user.field_encryptUsername)) {
                            isOK = false;
                            TestHelper.logE("\n" + "user.field_username:" + user.field_username
                                    + "\n" + "user.field_encryptUsername:" + user.field_encryptUsername);
                        }
                    }

                    if (!TextUtils.isEmpty(user.field_alias)) {
                        hasAlias = true;
                    }
                }

                if (!hasAlias) {
                    isOK = false;
                    TestHelper.logE(job.job_name + " no one has alias");
                }

                if (isOK) {
                    TestHelper.logI(job.job_name + " everything is OK");
                } else {
                    TestHelper.logE(job.job_name + " sth wrong");
                    TestHelper.setSomethingWrong();
                }
            }
        });
    }

    @Test
    public void acSearchFriend() {
        String jobString = "{\n" +
                "\t\"job_id\":0,\n" +
                "\t\"package_name\":\"com.tencent.mm\",\n" +
                "\t\"job_name\":\"search_friend\",\n" +
                "\t\"callback\":\"\",\n" +
                "\t\"job_data\":{\n" +
                "        \"phone\":\"18612690351\"\n" +
                "\t}\n" +
                "}\n";
        Job job = Job.fromString(jobString);
        TestHelper.testDoubleStepJob(job, new TestDoubleStepJobHandler() {
            @Override
            public void onFirstStepSuccess(Job job) {

            }

            @Override
            public void onSecondStepSuccess(Job job) {
                String result = job.result_data;
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Assert.assertNotNull(jsonObject);
                String iconUrl = jsonObject.optString("iconUrl");
                String field_encryptUsername = jsonObject.optString("field_encryptUsername");
                String nickname = jsonObject.optString("nickname");
                if (TextUtils.isEmpty(iconUrl)
                        || TextUtils.isEmpty(field_encryptUsername)
                        || TextUtils.isEmpty(nickname)
                        || !iconUrl.startsWith("http")) {
                    TestHelper.logE("\n" + "iconUrl:" + iconUrl
                            + "\n" + "field_encryptUsername:" + field_encryptUsername
                            + "\n" + "nickname:" + nickname);
                    TestHelper.logE(job.job_name + " sth wrong");
                    TestHelper.setSomethingWrong();
                } else {
                    TestHelper.logI(job.job_name + " everything is OK");
                }
            }
        });
    }
}
