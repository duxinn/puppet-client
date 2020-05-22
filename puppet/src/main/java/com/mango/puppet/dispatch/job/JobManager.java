package com.mango.puppet.dispatch.job;

import android.content.Context;

import com.mango.puppet.dispatch.job.i.IJob;
import com.mango.puppetmodel.Job;

/**
 * JobManager
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public class JobManager implements IJob {
    private static final JobManager ourInstance = new JobManager();

    public static JobManager getInstance() {
        return ourInstance;
    }

    private JobManager() {
    }

    /************   IJob   ************/
    @Override
    public void addJob(Job job) {

    }

    @Override
    public void receiveJobResult(Job jobResult) {

    }

    @Override
    public boolean startJobSystem(Context context) {
        return false;
    }
}
