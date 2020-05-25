package com.mango.puppet.dispatch.job;

import android.content.Context;
import android.util.Log;

import com.mango.puppet.dispatch.job.db.DBManager;
import com.mango.puppet.dispatch.job.i.IJob;
import com.mango.puppet.network.NetworkManager;
import com.mango.puppet.network.i.INetwork;
import com.mango.puppet.plugin.PluginManager;
import com.mango.puppet.plugin.i.IPluginControl;
import com.mango.puppet.plugin.i.IPluginJob;
import com.mango.puppet.status.StatusManager;
import com.mango.puppetmodel.Job;

import java.util.ArrayList;

/**
 * JobManager
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public class JobManager implements IJob,
        IPluginJob.IPluginJobCallBack {
    private static final JobManager ourInstance = new JobManager();

    enum STATUS {
        /**
         * status stop 执行完成任务休息中
         */
        STOP(0),
        /**
         * status error 上报任务失败过多终止执行
         */
        ERROE(-1),
        /**
         * status stop 执行中
         */
        RUNNING(1);
        private int code;

        STATUS(int code) {
            this.code = code;
        }

        public int getCode() {
            return this.code;
        }
    }

    private STATUS status = STATUS.STOP;


    public static JobManager getInstance() {
        return ourInstance;
    }

    private JobManager() {

    }

    /************   IJob   ************/
    @Override
    public void addJob(Job job) {
        boolean b = DBManager.insertJobIntoDb(job);
        if (!b) {
            Log.e("JobManager", "DBManager.insertJobIntoDb error ");
        }
    }

    @Override
    public void receiveJobResult(Job jobResult) {
        status = STATUS.STOP;
        boolean b = DBManager.updateJobStatus(jobResult);
        if (!b) {
            Log.e("JobManager", "DBManager.updateJobStatus error ");
        } else {
            executeJob();
        }
        log();
    }

    @Override
    public boolean startJobSystem(Context context) {
        DBManager.init(context);
        executeJob();
        ReportManager.getInstance().startJobSystem();
        return true;
    }

    @Override
    public void onFinished(Job job, boolean isSucceed, String failReason) {
        if (!isSucceed) {
            job.job_status = 4;
            boolean b = DBManager.updateJobStatus(job);
            status = STATUS.ERROE;
            log();
        }
    }

    private void log() {
        StatusManager.getInstance().setJobEngineStatus(status.getCode());
    }

    public void executeJob() {
        Job currentJob = DBManager.getSingleNotDoneJobsFromDb();
        if (currentJob != null && status != STATUS.RUNNING) {
            if (DBManager.getAllErrorJob().size() > 5 ||
                    DBManager.getJobsDependOnStatus(4).size() > 0) {
                status = STATUS.ERROE;
            } else {
                status = STATUS.RUNNING;
                PluginManager.getInstance().distributeJob(currentJob, this);
            }
        }
        log();
    }
}
