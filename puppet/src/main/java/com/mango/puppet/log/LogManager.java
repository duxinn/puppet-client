package com.mango.puppet.log;

import com.mango.puppet.log.i.ILog;

/**
 * LogManager
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public class LogManager implements ILog {
    private static final LogManager ourInstance = new LogManager();

    public static LogManager getInstance() {
        return ourInstance;
    }

    private LogManager() {
    }

    /************   ILog   ************/
    @Override
    public void setNewLogListener(ILogListener listener) {

    }

    @Override
    public void recordLog(String log) {

    }
}
