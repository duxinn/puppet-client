package com.mango.puppet;

import android.content.Context;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.mango.puppet.dispatch.business.BusinessManager;
import com.mango.puppet.dispatch.business.i.IBusiness;
import com.mango.puppet.dispatch.event.EventManager;
import com.mango.puppet.log.LogManager;
import com.mango.puppet.log.i.ILog;
import com.mango.puppet.network.i.INetwork;
import com.mango.puppet.plugin.PluginManager;
import com.mango.puppet.plugin.i.IPluginControl;
import com.mango.puppet.status.StatusManager;
import com.mango.puppet.status.i.IStatusListener;
import com.mango.puppetmodel.Event;
import com.mango.puppetmodel.UploadResourceModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    StatusManager mStatusManager;
    Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    String packageName = appContext.getPackageName();

    @Before
    public void init() {
        mStatusManager = StatusManager.getInstance();
        mStatusManager.setStatusListener(new IStatusListener() {
            @Override
            public void onNetworkStatusChanged(boolean isNetOk) {
                Log.i("OnStatuaTest", "onNetworkStatusChanged: ---" + isNetOk);
            }

            @Override
            public void onJobEngineStatusChanged(int status) {
                Log.i("OnStatuaTest", "onJobEngineStatusChanged: ----" + status);
            }

            @Override
            public void onJobCountChanged(int count) {
                Log.i("OnStatuaTest", "onJobCountChanged: ----" + count);
            }

            @Override
            public void onJobResultCountChanged(int count) {
                Log.i("OnStatuaTest", "onJobResultCountChanged: ------" + count);
            }

            @Override
            public void onEventWatcherChanged() {
                Log.i("OnStatuaTest", "onEventWatcherChanged: ----has changed");
            }

            @Override
            public void onPluginRunningChanged() {
                Log.i("OnStatuaTest", "onPluginRunningChanged: -----has changed");
            }
        });
    }

    @Test
    public void useAppContext() {
        // Context of the app under test.
        assertEquals("com.mango.puppet.test", appContext.getPackageName());
    }

    @Test
    public void ILogUsingTest() {
        String log = "124test";
        LogManager mLog = LogManager.getInstance();
        mLog.init(appContext);
        mLog.setNewLogListener(new ILog.ILogListener() {
            @Override
            public void onNewLog(String log) {
                Log.e("writeLog", log);
            }
        });
        mLog.recordLog(log);
    }

    @Test
    public void networkStatusUsingTest() {
        int netWorkStatus;
        mStatusManager.setNetworkStatus(1);
        netWorkStatus = mStatusManager.getNetworkStatus();
        assertEquals("success", 1, netWorkStatus);
        mStatusManager.setNetworkStatus(1);
        mStatusManager.setNetworkStatus(0);
        netWorkStatus = mStatusManager.getNetworkStatus();
        assertEquals("success", 0, netWorkStatus);
    }

    @Test
    public void jobCountUsingTest() {
        int jobCount;
        mStatusManager.setJobCount(3);
        jobCount = mStatusManager.getJobCount();
        assertEquals("success", 3, jobCount);
        mStatusManager.setJobCount(3);
        mStatusManager.setJobCount(4);
        jobCount = mStatusManager.getJobCount();
        assertEquals("success", 4, jobCount);
    }

    @Test
    public void jobResultCountUsingTest() {
        int jobResultCount;
        mStatusManager.setJobResultCount(2);
        jobResultCount = mStatusManager.getJobResultCount();
        assertEquals("success", 2, jobResultCount);
        mStatusManager.setJobResultCount(2);
        mStatusManager.setJobResultCount(9);
        jobResultCount = mStatusManager.getJobResultCount();
        assertEquals("success", 9, jobResultCount);
    }

    @Test
    public void eventWatchUsingTest() {
        int size;
        Boolean isRunning;
        String eventName1 = "first";
        String eventName2 = "secong";
        mStatusManager.setEventWatcher(packageName, eventName1, true);
        mStatusManager.setEventWatcher(packageName, eventName2, false);
        size = mStatusManager.getAllEventWatcher().size();
        assertEquals("success", 1, size);
        size = mStatusManager.getApplicationEventWatcher(packageName).size();
        assertEquals("success", 1, size);
        isRunning = mStatusManager.getEventWatcher(packageName, eventName1);
        assertEquals("success", true, isRunning);
        isRunning = mStatusManager.getEventWatcher(packageName, eventName2);
        assertEquals("success", false, isRunning);
        mStatusManager.setEventWatcher(packageName, eventName2, true);
        isRunning = mStatusManager.getEventWatcher(packageName, eventName2);
        assertEquals("success", true, isRunning);
    }

    @Test
    public void pluginRunningTesting() {
        int size;
        Boolean isRunning;
        String packageName1 = "com.mango.puppet.test1";
        mStatusManager.setPluginRunning(packageName, true);
        mStatusManager.setPluginRunning(packageName1, false);
        size = mStatusManager.getAllRunningPlugin().size();
        assertEquals("success", 1, size);
        isRunning = mStatusManager.isPluginRunning(packageName1);
        assertEquals("success", false, isRunning);
        isRunning = mStatusManager.isPluginRunning(packageName);
        assertEquals("success", true, isRunning);
        mStatusManager.setPluginRunning(packageName1, true);
        isRunning = mStatusManager.isPluginRunning(packageName1);
        assertEquals("success", true, isRunning);
    }

    @Test
    public void businessManager() {
        final String TAG = "onBusinessTest";
        List<UploadResourceModel> modelsList = new ArrayList<>();
        UploadResourceModel model = new UploadResourceModel();
        Map<String,String> map = new HashMap<>();

        BusinessManager.getInstance().init(appContext);
        final IBusiness.IUploadResourceWayResult mResult = new IBusiness.IUploadResourceWayResult() {
            @Override
            public void onSuccess(List<UploadResourceModel> modelList) {
                Log.i(TAG, "onSuccess: ------");
            }

            @Override
            public void onError() {
                Log.i(TAG, "onError: --------");
            }
        };
        BusinessManager.getInstance().getUploadResourceWay(mResult);
        INetwork.IRequestResult requestResult = new INetwork.IRequestResult() {
            @Override
            public void onSuccess(Object result) {
                if (result instanceof List) {
                    if (((List) result).size() > 0 && ((List) result).get(0) instanceof UploadResourceModel) {
                        mResult.onSuccess((List) result);
                    }
                } else {
                    mResult.onError();
                }
            }

            @Override
            public void onError(int errorCode, String errorMessage) {
                if (mResult != null) {
                    mResult.onError();
                    LogManager.getInstance().recordLog("请求资源时路径时，返回资源错误");
                }
            }

            @Override
            public void onNetworkError() {
                LogManager.getInstance().recordLog("请求资源时路径时，网络连接失败");
            }
        };
        mResult.onSuccess(modelsList);
        model.channel = "123";
        map.put("123","success");
        model.parameter = map;
        modelsList.add(model);
        requestResult.onSuccess(modelsList);
        requestResult.onError(1,"resource error");
        requestResult.onNetworkError();
    }

    @Test
    public void EventUsingTest(){
        String url = "api";
        EventManager.getInstance().startEventSystem(appContext);
        EventManager.getInstance().setEventWatcher(packageName,"testEventUsing1",true,url);
        EventManager.getInstance().setEventWatcher(packageName,"testEventUsing1",false,url);
        EventManager.getInstance().setEventWatcher(packageName,"testEventUsing2",false,url);

        Event event=new Event();
        Map<String,String> map = new HashMap<>();
        event.event_status = 1;
        event.package_name = packageName;
        event.event_name = "testEventUsing3";
        map.put(event.event_name,"1");
        event.event_data = map;
        final IPluginControl.IPluginControlResult iPluginControlResult = new IPluginControl.IPluginControlResult() {
            @Override
            public void onFinished(boolean isSucceed, String failReason) {
                Log.i("OnEventManager", "onFinished: -----" + isSucceed + failReason);
            }
        };
        INetwork.IEventRequestResult requestResult = new INetwork.IEventRequestResult() {
            @Override
            public void onSuccess(Event event) {
                event.event_status = 1;
                PluginManager.getInstance().distributeEvent(event, iPluginControlResult);
            }

            @Override
            public void onError(Event event, int errorCode, String errorMessage) {
                event.event_status = 1;
                PluginManager.getInstance().distributeEvent(event, iPluginControlResult);
            }

            @Override
            public void onNetworkError(Event event) {
                event.event_status = 0;
                PluginManager.getInstance().distributeEvent(event, iPluginControlResult);
            }
        };
        requestResult.onSuccess(event);
        requestResult.onError(event,1,"event error");
        requestResult.onNetworkError(event);
    }
}
