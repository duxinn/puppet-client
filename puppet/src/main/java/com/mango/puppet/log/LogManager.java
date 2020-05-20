package com.mango.puppet.log;

import android.content.Context;

import com.mango.puppet.log.i.ILog;

/**
 * LogManager
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public class LogManager implements ILog {
    private static Context mContext;
    private static final LogManager ourInstance = new LogManager();

    public static LogManager getInstance() {
        return ourInstance;
    }

    private LogManager() {
    }

    private ILogListener mLogListener;

    public static void init(Context context){
        mContext = context;
    }
    /************   ILog   ************/
    @Override
    public void setNewLogListener(ILogListener listener) {
        mLogListener = listener;
    }

    @Override
    public void recordLog(String log) {
        log = LogUtil.timeString(System.currentTimeMillis() / 1000) + " "+log;
//        存储拼接上时间的log
        LogUtil.writeLog(log,mContext);
//        把拼接好的日志通知给观察者
        if (mLogListener != null) {
            mLogListener.onNewLog(log);
        }
    }

}
