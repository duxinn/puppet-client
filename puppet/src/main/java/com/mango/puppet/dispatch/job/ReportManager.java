package com.mango.puppet.dispatch.job;

import android.util.Log;

import com.mango.puppet.dispatch.job.db.DBManager;
import com.mango.puppet.network.NetworkManager;
import com.mango.puppet.network.i.INetwork;
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

    /* DBManager.OnJobDBChangeListener */
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

    /* INetwork.IJobRequestResult */
    @Override
    public void onSuccess(Job jobResult) {
        if (jobResult.job_status == 2) {
            jobResult.job_status = 1;
            DBManager.updateJobStatus(jobResult);
        } else if (jobResult.job_status == 3
                || jobResult.job_status == 4) {
            removeFromDb(jobResult);
        } else if (jobResult.job_status == 5) {
            jobResult.job_status = 6;
            DBManager.updateJobStatus(jobResult);
        } else if (jobResult.job_status == 1) {
            // 两步任务第一步上报成功前 第二步执行完成了
        } else {
            throw new RuntimeException("status:" + jobResult.job_status);
        }
    }

    @Override
    public void onError(Job jobResult, int errorCode, String errorMessage) {
        if (jobResult.job_status == 2) {
            jobResult.job_status = 1;
            DBManager.updateJobStatus(jobResult);
        } else if (jobResult.job_status == 3
                || jobResult.job_status == 4) {
            removeFromDb(jobResult);
        } else if (jobResult.job_status == 5) {
            jobResult.job_status = 6;
            DBManager.updateJobStatus(jobResult);
        } else if (jobResult.job_status == 1) {
            // 两步任务第一步上报成功前 第二步执行完成了
        } else {
            throw new RuntimeException("status:" + jobResult.job_status);
        }
    }

    @Override
    public void onNetworkError(Job jobResult) {
        onErrorReport();
    }

    private void reportToService(Job job) {
        NetworkManager.getInstance().reportJobResult(job, this);
    }

    public void reportToServiceNoCallback(Job job) {
        NetworkManager.getInstance().reportJobResult(job, null);
    }

    public void start() {
        DBManager.addJobDbListener(ReportManager.this);
        reportAllJob();
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

    private void removeFromDb(Job jobResult) {
        boolean b = DBManager.deleteJob(jobResult.job_id);
        if (b) {
            Log.e("ReportManager", "DBManager.deleteJob error ");
        }
    }

}
