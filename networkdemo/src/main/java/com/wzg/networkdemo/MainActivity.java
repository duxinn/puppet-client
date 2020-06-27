package com.wzg.networkdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mango.puppet.dispatch.system.SystemManager;
import com.mango.puppet.network.api.api.ApiClient;
import com.mango.puppet.network.api.observerCallBack.DesCallBack;
import com.mango.puppet.network.api.vm.PuppetVM;
import com.mango.puppet.network.server.ServerManager;
import com.mango.puppet.network.server.model.ReturnData;
import com.mango.puppet.network.utils.JsonUtils;
import com.mango.puppet.network.wsmanager.WsManager;
import com.mango.puppet.network.wsmanager.listener.WsStatusListener;
import com.mango.puppet.tool.PreferenceUtils;

import org.json.JSONException;

import java.util.LinkedList;
import java.util.List;

import okhttp3.Response;
import okio.ByteString;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = "MainActivity";
    // 用于测试网络部分
    private Button mBtnTestNetwork;
    private Button mBtnStartServer;
    private Button mBtnStopServer;
    private Button mBtnBrowser;
    private Button mBtnOpenLongConnection;
    private TextView mTvMessage;
    private String mRootUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 初始化网络
        ApiClient.Companion.getInstance().build();
        initView();
        initServer();
        PreferenceUtils.getInstance().init(this);
    }

    private void initView() {
        mBtnTestNetwork = findViewById(R.id.btn_test_interface);
        mBtnStartServer = findViewById(R.id.btn_start_server);
        mBtnStopServer = findViewById(R.id.btn_stop_server);
        mBtnBrowser = findViewById(R.id.btn_browse);
        mTvMessage = findViewById(R.id.tv_message);
        mBtnOpenLongConnection = findViewById(R.id.btn_open_long_connection);

        mBtnTestNetwork.setOnClickListener(this);
        mBtnStartServer.setOnClickListener(this);
        mBtnStopServer.setOnClickListener(this);
        mBtnBrowser.setOnClickListener(this);
        mBtnOpenLongConnection.setOnClickListener(this);
    }

    private void initServer() {
        // 开启本地服务
        ServerManager.getInstance(this).register();
    }

    private void initData() {
        PuppetVM.Companion.testNetwork("", "testJson", new DesCallBack<Object>() {
            @Override
            public void onHandleSuccess(Object objectBaseModel) {
                Toast.makeText(MainActivity.this, "请求成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onHandleError(String msg, int code) {
                Toast.makeText(MainActivity.this, "请求失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNetWorkError(Throwable e) {
                Toast.makeText(MainActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_test_interface: {
                initData();
                break;
            }
            case R.id.btn_start_server: {
                ServerManager.getInstance(this).startServer(new ServerManager.ServerListener() {
                    @Override
                    public void onServerStart(String ip) {
                        mBtnStartServer.setVisibility(View.GONE);
                        mBtnStopServer.setVisibility(View.VISIBLE);
                        mBtnBrowser.setVisibility(View.VISIBLE);

                        if (!TextUtils.isEmpty(ip)) {
                            List<String> addressList = new LinkedList<>();
                            mRootUrl = "http://" + ip + ":8080/";
                            addressList.add(mRootUrl);
                            addressList.add("http://" + ip + ":8080/login.html");
                            mTvMessage.setText(TextUtils.join("\n", addressList));
                        } else {
                            mRootUrl = null;
                            mTvMessage.setText("Did not get the server IP address");
                        }
                    }

                    @Override
                    public void onServerError(String error) {
                        mRootUrl = null;
                        mBtnStartServer.setVisibility(View.VISIBLE);
                        mBtnStopServer.setVisibility(View.GONE);
                        mBtnBrowser.setVisibility(View.GONE);
                        mTvMessage.setText(error);
                    }

                    @Override
                    public void onServerStop() {
                        mRootUrl = null;
                        mBtnStartServer.setVisibility(View.VISIBLE);
                        mBtnStopServer.setVisibility(View.GONE);
                        mBtnBrowser.setVisibility(View.GONE);
                        mTvMessage.setText("Server Stop Succeed");
                    }
                });
                break;
            }
            case R.id.btn_stop_server: {
                ServerManager.getInstance(this).stopServer();
                break;
            }
            case R.id.btn_browse: {
                // 模拟远程后台发送过来请求
                if (!TextUtils.isEmpty(mRootUrl)) {
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    intent.setData(Uri.parse(mRootUrl));
                    startActivity(intent);
                }
                break;
            }
            case R.id.btn_open_long_connection:
                WsManager.getInstance(this).startConnect().setWsStatusListener(new WsStatusListener() {
                    @Override
                    public void onOpen(Response response) {
                        super.onOpen(response);
                        Log.d(TAG, "WsManager-----onOpen\n response: " + response.toString());
                    }

                    @Override
                    public void onMessage(String text) {
                        super.onMessage(text);
                        Log.d(TAG, "WsManager-----onMessage\n text: " + text);
                        JSONObject object = JSON.parseObject(text);
                        String requestId = (String) object.get("request_id");
                        String type = (String) object.get("type");
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
                                final int watcher_status = ((com.alibaba.fastjson.JSONObject) data).getInteger("watcher_status");
                                Log.d(TAG, "setEventWatcher-----event_name: " + event_name);
                                Log.d(TAG, "setEventWatcher-----package_name: " + package_name);
                                Log.d(TAG, "setEventWatcher-----callback: " + callback);
                                Log.d(TAG, "setEventWatcher-----watcher_status: " + watcher_status);
                            } else if ("addJob".equals(type)) {
                                com.alibaba.fastjson.JSONObject jsonObject = (com.alibaba.fastjson.JSONObject) data;
                                long job_id = jsonObject.getLong("job_id");
                                String package_name = jsonObject.getString("package_name");
                                String job_name = jsonObject.getString("job_name");
                                String callback = jsonObject.getString("callback");
                                org.json.JSONObject job_data = null;
                                try {
                                    job_data = new org.json.JSONObject(JsonUtils.toJsonString(jsonObject.getJSONObject("job_data")));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Log.d(TAG, "addJob-----job_id: " + job_id);
                                Log.d(TAG, "addJob-----package_name: " + package_name);
                                Log.d(TAG, "addJob-----job_name: " + job_name);
                                Log.d(TAG, "addJob-----callback: " + callback);
                                Log.d(TAG, "addJob-----job_data: " + job_data);
                            }
                        }
                        returnData.deviceid = SystemManager.getInstance().getDeviceId();
                        returnData.response_id = requestId;
                        returnData.status = status;
                        returnData.message = message;
                        WsManager.getInstance(MainActivity.this).sendMessage(JSONObject.toJSONString(returnData));
                        Log.d(TAG, "addJob-----sendMessage");
                    }

                    @Override
                    public void onMessage(ByteString bytes) {
                        super.onMessage(bytes);
                        Log.d(TAG, "WsManager-----onMessage\n bytes: " + bytes);
                    }

                    @Override
                    public void onReconnect() {
                        super.onReconnect();
                        Log.d(TAG, "WsManager-----onReconnect\n");
                    }

                    @Override
                    public void onClosing(int code, String reason) {
                        super.onClosing(code, reason);
                        Log.d(TAG, "WsManager-----onClosing\n");
                    }

                    @Override
                    public void onClosed(int code, String reason) {
                        super.onClosed(code, reason);
                        Log.d(TAG, "WsManager-----onClosed\n");
                    }

                    @Override
                    public void onFailure(Throwable t, Response response) {
                        super.onFailure(t, response);
                        Log.d(TAG, "WsManager-----onFailure\n");
                    }
                });
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ServerManager.getInstance(this).unRegister();
    }
}
