package com.mango.puppetsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.mango.puppet.network.api.observerCallBack.DesCallBack;
import com.mango.puppet.network.api.vm.PuppetVM;
import com.mango.puppet.network.dto.TestDTO;

import java.io.DataOutputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button mTestNetworkBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hasRoot();
        initView();
    }

    private void initView() {
        mTestNetworkBtn = findViewById(R.id.test_network_btn);
        mTestNetworkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initData();
            }
        });
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

}
