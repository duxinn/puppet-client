package com.mango.puppet.dispatch.job;

import android.util.Log;

import com.mango.puppet.dispatch.job.db.DBManager;
import com.mango.puppet.plugin.PluginManager;
import com.mango.puppet.plugin.i.IPluginJob;
import com.mango.puppetmodel.Job;

/**
 * ExecutorManager
 *
 * 任务执行者
 *
 * @author: hehongzhen
 * @date: 2020/05/28
 */
public class ExecutorManager implements DBManager.OnJobDBChangeListener, IPluginJob.IPluginJobCallBack {
    private static final ExecutorManager ourInstance = new ExecutorManager();

    public static ExecutorManager getInstance() {
        return ourInstance;
    }

    // 是否下发了任务
    private boolean isDistributedJob = false;

    private ExecutorManager() {
    }

    /**
     * 1 监听数据库变化
     * 2 判断是否执行任务
     */
    void start() {
        DBManager.addJobDbListener(ExecutorManager.this);
        boolean flag = canDistributeJob();
        if (flag) {
            distributeJob();
        }
    }

    /* DBManager.OnJobDBChangeListener */
    @Override
    public void onJobInsert(Job job) {
        boolean flag = canDistributeJob();
        if (flag) {
            distributeJob();
        }
    }

    @Override
    public void onJobDelete(Job job) {
        boolean flag = canDistributeJob();
        if (flag) {
            distributeJob();
        }
    }

    @Override
    public void onJobStatusChange(Job job) {
        boolean flag = canDistributeJob();
        if (flag) {
            distributeJob();
        }
    }

    /* IPluginJob.IPluginJobCallBack */
    @Override
    public void onFinished(Job job, boolean isSucceed, String failReason) {
        if (!isSucceed) {
            isDistributedJob = false;
            job.job_status = 5;
            boolean b = DBManager.updateJobStatus(job);
            if (!b) {
                Log.e("JobManager", "DBManager.updateJobStatus error ");
            }
        }
    }

    void receiveJobResult() {
        isDistributedJob = false;
    }

    /* private */

    private boolean canDistributeJob() {
        if (DBManager.getJobsDependOnStatus(5).size() > 0 ||
                DBManager.getJobsDependOnStatus(6).size() > 0) {
            JobManager.getInstance().setStatus(JobManager.STATUS.STOP);
            return false;
        } else if (DBManager.getReportJobs().size() > 5) {
            JobManager.getInstance().setStatus(JobManager.STATUS.WAIT);
            return false;
        } else if (isDistributedJob) {
            JobManager.getInstance().setStatus(JobManager.STATUS.RUNNING);
            return false;
        } else {
            return true;
        }
    }

    private void distributeJob() {
        Job currentJob = DBManager.getSingleNotDoneJobsFromDb();
        if (currentJob != null) {
            JobManager.getInstance().setStatus(JobManager.STATUS.RUNNING);
            isDistributedJob = true;
            PluginManager.getInstance().distributeJob(currentJob, this);
        } else {
            JobManager.getInstance().setStatus(JobManager.STATUS.REST);
        }
    }
}
