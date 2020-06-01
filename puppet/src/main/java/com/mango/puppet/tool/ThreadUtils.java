package com.mango.puppet.tool;

import android.os.Handler;
import android.os.Looper;

/**
 * ThreadUtils
 *
 * @author: hehongzhen
 * @date: 2020/06/01
 */
public class ThreadUtils {

    public static void runInMainThread(final Runnable runnable) {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (runnable != null) {
                        runnable.run();
                    }
                }
            });
        } else {
            if (runnable != null) {
                runnable.run();
            }
        }
    }
}
