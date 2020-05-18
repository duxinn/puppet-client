package com.mango.puppetsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.io.DataOutputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hasRoot();
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
}
