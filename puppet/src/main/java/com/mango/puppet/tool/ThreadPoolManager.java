package com.mango.puppet.tool;


import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolManager {

    private static ThreadPoolManager ourInstance = null;
    private static long KEEP_ALIVE_TIME = 1;
    private static TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    private ThreadPoolExecutor mThreadPoolExecutor;

    public static ThreadPoolManager getInstance() {
        synchronized (ThreadPoolManager.class) {
            if (ourInstance == null) {
                synchronized (ThreadPoolManager.class) {
                    ourInstance = new ThreadPoolManager();
                }
            }
        }
        return ourInstance;
    }

    private ThreadPoolManager() {
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        mThreadPoolExecutor = new ThreadPoolExecutor(
                corePoolSize,
                corePoolSize * 2,
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                new LinkedBlockingQueue<Runnable>(),
                new ThreadPoolExecutor.DiscardPolicy());
    }

    public void execute(Runnable runnable) {
        mThreadPoolExecutor.execute(runnable);
    }
}
