package com.mango.puppetsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mango.puppet.network.api.observerCallBack.DesCallBack;
import com.mango.puppet.network.api.vm.PuppetVM;
import com.mango.puppet.network.dto.TestDTO;
import com.mango.puppet.network.server.ServerManager;

import java.io.DataOutputStream;
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

    private ServerManager mServerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        hasRoot();
        initView();
        initServer();
    }

    // 判断及申请root权限
    public static boolean hasRoot() {
        Boolean flag = true;
        Process process = null;
        DataOutputStream os = null;
        try {
            String cmd = "su";
            process = Runtime.getRuntime().exec(cmd);
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
            int exitValue = process.waitFor();
            if (exitValue == 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            flag = false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return flag;
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
        mServerManager = new ServerManager(this);
        mServerManager.register();
        mBtnStartServer.performClick();
    }

    private void initData() {
        PuppetVM.Companion.getNoParamData(new DesCallBack<List<TestDTO>>() {
            @Override
            public void onSubscribe() {

            }

            @Override
            public void success(List<TestDTO> any) {
                if (any.isEmpty()) return;
                Log.e("MainActivity", any.toString());
            }

            @Override
            public void failed(Throwable e) {
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
                mServerManager.startServer();
                break;
            }
            case R.id.btn_stop_server: {
                mServerManager.stopServer();
                break;
            }
            case R.id.btn_browse: {
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
        mServerManager.unRegister();
    }
}
