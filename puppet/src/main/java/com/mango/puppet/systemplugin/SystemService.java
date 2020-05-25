package com.mango.puppet.systemplugin;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import androidx.annotation.Nullable;

/**
 * SystemService
 * 用于监听屏幕 电池的变化
 *
 * @author: hehongzhen
 * @date: 2020/05/20
 */
public class SystemService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        /* 注册屏幕唤醒时的广播 */
        IntentFilter mScreenOnFilter = new IntentFilter("android.intent.action.SCREEN_ON");
        SystemService.this.registerReceiver(mReceiver, mScreenOnFilter);

        /* 注册机器锁屏时的广播 */
        IntentFilter mScreenOffFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
        SystemService.this.registerReceiver(mReceiver, mScreenOffFilter);

        /* 注册机器锁屏时的广播 */
        IntentFilter mBatteryFilter = new IntentFilter("android.intent.action.BATTERY_CHANGED");
        SystemService.this.registerReceiver(mReceiver, mBatteryFilter);
    }

    public void onDestroy() {
        super.onDestroy();
        SystemService.this.unregisterReceiver(mReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.SCREEN_ON")) {
                SystemPluginManager.getInstance().onScreenChange(false);
            } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                SystemPluginManager.getInstance().onScreenChange(true);
            } else if (action.equals("android.intent.action.BATTERY_CHANGED")) {
                int intLevel = intent.getIntExtra("level", 0);
                int intScale = intent.getIntExtra("scale", 100);
                SystemPluginManager.getInstance().onBatteryChange(intLevel, intScale);
            }
        }
    };
}
