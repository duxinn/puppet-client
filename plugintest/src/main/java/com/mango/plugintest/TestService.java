package com.mango.plugintest;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.yanzhenjie.andserver.AndServer;
import com.yanzhenjie.andserver.Server;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

public class TestService extends Service {

    private Server mServer;
    public static final int PORT = 8081;

    @Override
    public void onCreate() {
        mServer = AndServer.webServer(this)
            .port(PORT)
            .timeout(10, TimeUnit.SECONDS)
            .listener(new Server.ServerListener() {
                @Override
                public void onStarted() {
                    InetAddress address = NetUtils.getLocalIPAddress();
                    if (address != null) {
                        TestServerManager.onServerStart(TestService.this, address.getHostAddress());
                    }
                }

                @Override
                public void onStopped() {
                    TestServerManager.onServerStop(TestService.this);
                }

                @Override
                public void onException(Exception e) {
                    TestServerManager.onServerError(TestService.this, e.getMessage());
                }
            })
            .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startServer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopServer();
        super.onDestroy();
    }

    /**
     * Start server.
     */
    private void startServer() {
        mServer.startup();
    }

    /**
     * Stop server.
     */
    private void stopServer() {
        mServer.shutdown();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}