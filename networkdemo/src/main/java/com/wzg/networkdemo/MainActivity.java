package com.wzg.networkdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mango.puppet.network.api.basemodel.BaseModel;
import com.mango.puppet.network.api.observerCallBack.DesCallBack;
import com.mango.puppet.network.api.vm.PuppetVM;
import com.mango.puppet.network.server.ServerManager;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ServerManager.ServerListener, View.OnClickListener {
    // 用于测试网络部分
    private Button mBtnTestNetwork;
    private Button mBtnStartServer;
    private Button mBtnStopServer;
    private Button mBtnBrowser;
    private TextView mTvMessage;
    private String mRootUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initServer();
    }

    private void initView() {
        mBtnTestNetwork = findViewById(R.id.btn_test_interface);
        mBtnStartServer = findViewById(R.id.btn_start_server);
        mBtnStopServer = findViewById(R.id.btn_stop_server);
        mBtnBrowser = findViewById(R.id.btn_browse);
        mTvMessage = findViewById(R.id.tv_message);

        mBtnTestNetwork.setOnClickListener(this);
        mBtnStartServer.setOnClickListener(this);
        mBtnStopServer.setOnClickListener(this);
        mBtnBrowser.setOnClickListener(this);
    }

    private void initServer() {
        // 开启本地服务
        ServerManager.getInstance(this).register();
    }

    private void initData() {
        PuppetVM.Companion.reportEvent("http://www.puppet.com", "xxx", new DesCallBack<Object>() {
            @Override
            public void onHandleSuccess(Object objectBaseModel) {

            }

            @Override
            public void onHandleError(String msg, int code) {

            }

            @Override
            public void onNetWorkError(Throwable e) {

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
                ServerManager.getInstance(this).startServer();
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
        }
    }

    /**
     * 这部分接口回调根据用户需求  看是否需要服务状态的回调
     */
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ServerManager.getInstance(this).unRegister();
    }
}
