package com.mango.puppet.network;

import android.os.Handler;

import com.mango.puppet.dispatch.job.JobManager;
import com.mango.puppet.dispatch.job.db.DBManager;
import com.mango.puppet.log.LogManager;
import com.mango.puppet.network.i.INetwork;
import com.mango.puppetmodel.Job;

public class CallBackListener {
    private static final CallBackListener ourInstance = new CallBackListener();

    public static CallBackListener getInstance() {
        return ourInstance;
    }

    private CallBackListener() {
    }

    /*****下发任务后，10s内收到执行后的上报回调，认为通过*****/
    private Job mSuccessJob;
    private INetwork.IJobRequestResult mSuccessResult;
    private int mSuccessCallCount;

    public void sendSuccessJob() {
        DBManager.clearDB();
        final Job job = new Job();
        job.package_name = "com.wzg.trojandemo";
        job.job_name = "First任务";
        job.job_id = 1;
        JobManager.getInstance().addJob(job);

        mSuccessJob = null;
        mSuccessResult = null;
        mSuccessCallCount = 0;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mSuccessJob != null
                        && mSuccessResult != null
                        && mSuccessCallCount == 1) {
                    if (mSuccessJob.job_status == 3) {
                        mSuccessResult.onSuccess(mSuccessJob);
                        Job j = DBManager.getJobsById(job.job_id);
                        if (j == null) {
                            LogManager.getInstance().recordLog("任务下发-执行-上报通过");
                            return;
                        }
                    }
                }
                LogManager.getInstance().recordLog("任务下发-执行-上报未通过");
            }
        }, 5000);
    }
    /**********/

    /*****下发任务后，执行任务延迟5s，杀死App后开启，10s内收到执行任务后的上报回调，认为通过*****/
    public void sendDelayJob() {
        final Job job = new Job();
        job.package_name = "com.wzg.trojandemo";
        job.job_name = "Second任务";
        job.job_id = 2;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                JobManager.getInstance().addJob(job);
            }
        }, 5000);

        mSuccessJob = null;
        mSuccessResult = null;
        mSuccessCallCount = 0;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mSuccessJob != null
                        && mSuccessResult != null
                        && mSuccessCallCount == 1) {
                    if (mSuccessJob.job_status == 3) {
                        mSuccessResult.onSuccess(mSuccessJob);
                        Job j = DBManager.getJobsById(job.job_id);
                        if (j == null) {
                            LogManager.getInstance().recordLog("延迟5s下发任务，重启App后收到上报回调通过");
                            return;
                        }
                    }
                }
                LogManager.getInstance().recordLog("延迟5s下发任务，重启App后收到上报回调未通过");
            }
        }, 10000);
    }

    void reportJobResult(final Job jobResult, final INetwork.IJobRequestResult iJobRequestResult) {
        mSuccessJob = jobResult;
        mSuccessResult = iJobRequestResult;
        mSuccessCallCount++;
    }

}
