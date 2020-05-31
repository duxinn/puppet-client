package com.wzg.trojandemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.mango.transmit.TransmitManager;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList<String> list = new ArrayList<>();
        list.add(getPackageName());

        TransmitManager.getInstance().setRegister(this, list);
        TransmitManager.getInstance().setTransmitReceiver(DataReceiver.getInstance());
    }
}
