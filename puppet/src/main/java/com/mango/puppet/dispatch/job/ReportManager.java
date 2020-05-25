package com.mango.puppet.dispatch.job;

import android.content.Context;
import android.util.Log;

import com.mango.puppet.dispatch.job.db.DBManager;
import com.mango.puppet.dispatch.job.i.IJob;
import com.mango.puppet.network.NetworkManager;
import com.mango.puppet.network.i.INetwork;
import com.mango.puppet.status.StatusManager;
import com.mango.puppetmodel.Job;

import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by blueZhang on 2020-05-25.
 *
 * @Author: BlueZhang
 * @date: 2020-05-25
 */
public class ReportManager implements
        DBManager.OnJobDBChangeListener,
        INetwork.IJobRequestResult {
    private static final ReportManager instance = new ReportManager();

    private ReportManager() {

    }

    public static ReportManager getInstance() {
        return instance;
    }

    private ScheduledThreadPoolExecutor scheduled
            = null;

    @Override
    public void onJobInsert(Job job) {

    }

    @Override
    public void onJobDelete(Job job) {

    }

    @Override
    public void onJobStatusChange(Job job) {
        if (job.job_status != 0 && job.job_status != 1) {
            reportToService(job);
        }
    }

    @Override
    public void onSuccess(Job jobResult) {
        removeFromDb(jobResult);
    }

    private void removeFromDb(Job jobResult) {
        boolean b = DBManager.deleteJob(jobResult.job_id);
        if (b) {
            Log.e("ReportManager", "DBManager.deleteJob error ");
        }
    }

    @Override
    public void onError(Job jobResult, int errorCode, String errorMessage) {
        removeFromDb(jobResult);
    }

    @Override
    public void onNetworkError(Job jobResult) {
        onErrorReport();
    }

    public void reportToService(Job job) {
        NetworkManager.getInstance().reportJobResult(job, this);
    }


    public void startJobSystem() {
        DBManager.addJobDbListener(ReportManager.this);
        startJobSystem();
    }


    private void reportAllJob() {
        ArrayList<Job> reportJobs = DBManager.getReportJobs();
        if (reportJobs == null || reportJobs.size() == 0) return;
        for (int i = 0; i < reportJobs.size(); i++) {
            reportToService(reportJobs.get(i));
        }
    }


    public void onErrorReport() {
        if (scheduled == null) {
            scheduled = new ScheduledThreadPoolExecutor(2);
            scheduled.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    reportAllJob();
                }
            }, 5, 5, TimeUnit.SECONDS);
        }
    }

}
