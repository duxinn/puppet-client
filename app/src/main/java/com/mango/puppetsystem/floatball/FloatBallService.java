package com.mango.puppetsystem.floatball;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.mango.puppetsystem.R;


public class FloatBallService extends Service {

    private FloatBall floatBall;

    private WindowManager windowManager;

    private WindowManager.LayoutParams floatBallParams;

    private long mCurrentMS;

    public static boolean flag;
    private float x;
    private float y;
    private int injectStatus;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        FloatBallService.flag = true;
        init();
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        showFloatBall();
        super.onCreate();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        init();
        injectStatus = intent.getIntExtra("injectStatus", 0);
        //点击关闭悬浮窗 显示悬浮球
        if (flag) {
            floatBall.setVisibility(View.VISIBLE);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void init() {
        if (floatBall == null) {
            floatBall = new FloatBall(this);
        }

        if (injectStatus == 1) {
            floatBall.setTextColor(getResources().getColor(R.color.color_298a28));
            floatBall.setmText("已运行");
        } else {
            floatBall.setTextColor(getResources().getColor(R.color.white));
            floatBall.setmText("未运行");
        }
        floatBall.setmHeight((int) getResources().getDimension(R.dimen.dimens_50_dp));
        floatBall.setmWidth((int) getResources().getDimension(R.dimen.dimens_50_dp));
        floatBall.setOnTouchListener(touchListener);
    }

    View.OnTouchListener touchListener = new View.OnTouchListener() {
        float startX;
        float startY;
        float tempX;
        float tempY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mCurrentMS = System.currentTimeMillis();
                    startX = event.getRawX();
                    startY = event.getRawY();
                    tempX = event.getRawX();
                    tempY = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    x = event.getRawX() - startX;
                    y = event.getRawY() - startY;
                    //计算偏移量，刷新视图
                    floatBallParams.x += x;
                    floatBallParams.y += y;
                    floatBall.setDragState();
                    windowManager.updateViewLayout(floatBall, floatBallParams);
                    startX = event.getRawX();
                    startY = event.getRawY();
                    break;
                case MotionEvent.ACTION_UP:
                    long moveTime = System.currentTimeMillis() - mCurrentMS;
                    if (Math.abs(x) > 0 && Math.abs(y) > 0) {
                        if (moveTime < 150) {
                            onFloadBallClick();
                        }
                        return true;
                    } else {
                        if (moveTime > 150) {
                            return true;
                        }
                        onFloadBallClick();
                    }

                    break;
            }
            return true;
        }
    };

    private void onFloadBallClick() {
        Intent intent = new Intent(FloatBallService.this, FloatWindowService.class);
        startService(intent);
        if (floatBall != null) {
            floatBall.setVisibility(View.GONE);
            flag = false;
        }
    }

    //显示浮动小球
    public void showFloatBall() {
        if (floatBallParams == null) {
            floatBallParams = new WindowManager.LayoutParams();
            floatBallParams.width = floatBall.mWidth;
            floatBallParams.height = floatBall.mHeight;
            floatBallParams.gravity = Gravity.CENTER;
            floatBallParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            floatBallParams.format = PixelFormat.RGBA_8888;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                floatBallParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                floatBallParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            }
            windowManager.addView(floatBall, floatBallParams);
        }

    }

    @Override
    public void onDestroy() {
        if (windowManager != null && floatBall != null) {
            windowManager.removeView(floatBall);
        }
        super.onDestroy();
    }

}