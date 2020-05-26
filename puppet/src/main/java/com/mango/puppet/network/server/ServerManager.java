package com.mango.puppet.network.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.mango.puppet.status.StatusManager;

public class ServerManager extends BroadcastReceiver {

    private static final String ACTION = "com.mango.puppetsystem.receiver";

    private static final String CMD_KEY = "CMD_KEY";
    private static final String MESSAGE_KEY = "MESSAGE_KEY";

    private static final int CMD_VALUE_START = 1;
    private static final int CMD_VALUE_ERROR = 2;
    private static final int CMD_VALUE_STOP = 4;

    private static final ServerManager instance = new ServerManager();

    private static Context mContext;
    private static Intent mService;
    private static ServerListener mServerListener;

    public static ServerManager getInstance(Context context) {
        if (mService == null) {
            mContext = context;
            mServerListener = (ServerListener) context;
            mService = new Intent(context, CoreService.class);
        }
        return instance;
    }

    private ServerManager() {
    }

    public static void onServerStart(Context context, String hostAddress) {
        sendBroadcast(context, CMD_VALUE_START, hostAddress);
    }

    public static void onServerError(Context context, String error) {
        sendBroadcast(context, CMD_VALUE_ERROR, error);
    }

    public static void onServerStop(Context context) {
        sendBroadcast(context, CMD_VALUE_STOP);
    }

    private static void sendBroadcast(Context context, int cmd) {
        sendBroadcast(context, cmd, null);
    }

    private static void sendBroadcast(Context context, int cmd, String message) {
        Intent broadcast = new Intent(ACTION);
        broadcast.putExtra(CMD_KEY, cmd);
        broadcast.putExtra(MESSAGE_KEY, message);
        context.sendBroadcast(broadcast);
    }


    public ServerManager register() {
        IntentFilter filter = new IntentFilter(ACTION);
        mContext.registerReceiver(this, filter);
        return instance;
    }

    public void unRegister() {
        mContext.unregisterReceiver(this);
    }

    public void startServer(ServerListener serverListener) {
        mServerListener = serverListener;
        mContext.startService(mService);
    }

    public void stopServer() {
        // 停止服务
        mContext.stopService(mService);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION.equals(action)) {
            int cmd = intent.getIntExtra(CMD_KEY, 0);
            switch (cmd) {
                case CMD_VALUE_START: {
                    String ip = intent.getStringExtra(MESSAGE_KEY);
                    if (mServerListener != null) {
                        mServerListener.onServerStart(ip);
                    }
                    break;
                }
                case CMD_VALUE_ERROR: {
                    String error = intent.getStringExtra(MESSAGE_KEY);
                    if (mServerListener != null) {
                        mServerListener.onServerError(error);
                    }
                    break;
                }
                case CMD_VALUE_STOP: {
                    if (mServerListener != null) {
                        mServerListener.onServerStop();
                    }
                    break;
                }
            }
        }
    }

    public interface ServerListener {
       void onServerStart(String ip);
       void onServerError(String error);
       void onServerStop();
    }
}