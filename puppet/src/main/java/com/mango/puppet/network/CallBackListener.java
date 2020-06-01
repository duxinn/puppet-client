package com.mango.puppet.network;

import android.os.Handler;
import android.util.Log;

import com.mango.puppet.dispatch.job.JobManager;
import com.mango.puppet.dispatch.job.db.DBManager;
import com.mango.puppet.log.LogManager;
import com.mango.puppet.network.i.INetwork;
import com.mango.puppetmodel.Job;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.mango.puppetmodel.Job.CANCEL_JOB;
import static com.mango.puppetmodel.Job.RETRY_JOB;

public class CallBackListener {
    private static final CallBackListener ourInstance = new CallBackListener();

    public static CallBackListener getInstance() {
        return ourInstance;
    }

    private CallBackListener() {
    }

    // 单个成功任务回调使用
    private Job mSuccessJob;
    private INetwork.IJobRequestResult mSuccessResult;
    // 任务回调数
    private int mSuccessCallCount;

    // 多个任务成功
    private ArrayList<Job> mOriginJobList = new ArrayList<>();
    private ArrayList<Job> mSuccessJobList = new ArrayList<>();
    private ArrayList<INetwork.IJobRequestResult> mSuccessResultList = new ArrayList<>();

    // 单个失败任务回调使用
    private Job mFailedJob;
    private INetwork.IJobRequestResult mFailedResult;
    // 任务回调数
    private int mFailedCallCount;

    /*****下发任务后，5s内收到执行后的上报回调，认为通过*****/
    /*****测任务下发-执行-上报模块之间链路连通性*****/
    public void sendSuccessJob() {
        mSuccessJob = null;
        mSuccessResult = null;
        mSuccessCallCount = 0;
        DBManager.clearDB();

        final Job job = new Job();
        job.package_name = "com.wzg.trojandemo";
        job.job_name = "A任务";
        job.job_id = 1;
        JobManager.getInstance().addJob(job);

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

    /*****下发任务后，执行任务延迟5s，杀死App后开启，10s内收到执行任务后的上报回调，认为通过*****/
    /*****测任务执行成功完成后，数据库是否清掉该任务*****/
    public void sendDelayJob() {
        DBManager.clearDB();
        mSuccessJob = null;
        mSuccessResult = null;
        mSuccessCallCount = 0;

        final Job job = new Job();
        job.package_name = "com.wzg.trojandemo";
        job.job_name = "B任务";
        job.job_id = 2;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                JobManager.getInstance().addJob(job);
            }
        }, 5000);

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
                            LogManager.getInstance().recordLog("延迟5s下发任务，重启App后收到上报回调测试通过");
                            return;
                        }
                    }
                }
                LogManager.getInstance().recordLog("延迟5s下发任务，重启App后收到上报回调测试未通过");
            }
        }, 10000);
    }

    /*****连续下发4个任务，A，B，C，D，10s内收到4个任务的执行上报回调（依次为A，B，C，D），认为通过*****/
    /*****测任务顺序性，先进先出*****/

    public void sendContinuousJob() {
        DBManager.clearDB();
        mOriginJobList.clear();
        mSuccessJobList.clear();
        mSuccessResultList.clear();
        mSuccessCallCount = 0;

        Job jobA = new Job();
        jobA.package_name = "com.wzg.trojandemo";
        jobA.job_name = "A任务";
        jobA.job_id = 3;

        Job jobB = new Job();
        jobB.package_name = "com.wzg.trojandemo";
        jobB.job_name = "B任务";
        jobB.job_id = 4;

        Job jobC = new Job();
        jobC.package_name = "com.wzg.trojandemo";
        jobC.job_name = "C任务";
        jobC.job_id = 5;

        Job jobD = new Job();
        jobD.package_name = "com.wzg.trojandemo";
        jobD.job_name = "D任务";
        jobD.job_id = 6;
        JobManager.getInstance().addJob(jobA);
        JobManager.getInstance().addJob(jobB);
        JobManager.getInstance().addJob(jobC);
        JobManager.getInstance().addJob(jobD);

        mOriginJobList.add(jobA);
        mOriginJobList.add(jobB);
        mOriginJobList.add(jobC);
        mOriginJobList.add(jobD);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("CallBackListener", "=======收到回调");
                for (int i = 0; i < mOriginJobList.size(); i++) {
                    Log.e("CallBackListener", "jobId: " + mOriginJobList.get(i).job_id);
                    if (mSuccessCallCount == 4
                            && mSuccessJobList.get(i) != null
                            && mSuccessResultList.get(i) != null
                            && mSuccessJobList.get(i).job_status == 3
                    ) {
                        if (mOriginJobList.get(i).job_id == mSuccessJobList.get(i).job_id) {
                            mSuccessResultList.get(i).onSuccess(mSuccessJobList.get(i));
                            Job j = DBManager.getJobsById(mSuccessJobList.get(i).job_id);
                            if (j != null) {
                                LogManager.getInstance().recordLog("任务有序性测试未通过");
                            } else if (i == 3) {
                                LogManager.getInstance().recordLog("任务有序性测试通过");
                                return;
                            }
                        }

                    }
                }
                LogManager.getInstance().recordLog("任务有序性测试未通过");
            }
        }, 8000);
    }

    /*****下发任务后，任务执行完上报回调出错；重启App，3s内收到的是之前未上报的任务回调结果，认为通过*****/
    /*****测回调结果错误时，数据库缓存及重试机制*****/
    public void sendFailedJob() {
        DBManager.clearDB();
        mFailedJob = null;
        mFailedResult = null;
        mFailedCallCount = 0;

        final Job job = new Job();
        job.package_name = "com.wzg.trojandemo";
        job.job_name = "失败的任务";
        job.job_id = 1;
        JobManager.getInstance().addJob(job);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mFailedJob != null
                        && mFailedResult != null
                        && mFailedCallCount == 1) {
                    if (mFailedJob.job_status == 5) {
                        mFailedResult.onNetworkError(mFailedJob);
                        Job j = DBManager.getJobsById(job.job_id);
                        if (j != null) {
                            LogManager.getInstance().recordLog("任务执行失败-上报失败-数据库有缓存且能够开启重试机制-通过");
                            return;
                        }
                    }
                }
                LogManager.getInstance().recordLog("任务执行失败-上报失败-数据库有缓存且能够开启重试机制-通过未通过");
            }
        }, 3000);
    }

    /*****测取消/重试任务是否成功*****/
    /*****发送异常且上报的任务*****/
    public void sendFailedAndReportJob() {
        DBManager.clearDB();
        mFailedJob = null;
        mFailedResult = null;
        mFailedCallCount = 0;

        final Job job = new Job();
        job.package_name = "com.wzg.trojandemo";
        job.job_name = "失败的任务";
        job.job_id = 1;
        JobManager.getInstance().addJob(job);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mFailedJob != null
                        && mFailedResult != null
                ) {
                    if (mFailedJob.job_status == 5) {
                        mFailedResult.onSuccess(mFailedJob);
                        LogManager.getInstance().recordLog("失败的任务上报成功");
                    }
                }
            }
        }, 3000);
    }

    /*****测试该用例时先调用sendFailedAndReportJob*****/
    public void sendCancelJob() {
        final Job cancelJob = new Job();
        cancelJob.package_name = "com.wzg.trojandemo";
        cancelJob.job_name = CANCEL_JOB;
        cancelJob.job_id = 2;
        try {
            cancelJob.job_data = new JSONObject();
            cancelJob.job_data.put("cancel_job_id", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JobManager.getInstance().addJob(cancelJob);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (DBManager.getJobsById(mFailedJob.job_id) == null) {
                    LogManager.getInstance().recordLog("取消任务测试通过");
                } else {
                    LogManager.getInstance().recordLog("取消任务测试未通过");
                }
            }
        }, 3000);

    }

    /*****测试该用例时先调用sendFailedAndReportJob*****/
    public void sendRetryJob() {
        final Job retryJob = new Job();
        retryJob.package_name = "com.wzg.trojandemo";
        retryJob.job_name = RETRY_JOB;
        retryJob.job_id = 3;
        try {
            retryJob.job_data = new JSONObject();
            retryJob.job_data.put("retry_job_id", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JobManager.getInstance().addJob(retryJob);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mSuccessJob != null && mSuccessResult != null) {
                    if (mSuccessJob.job_status == 3) {
                        mSuccessResult.onSuccess(mSuccessJob);
                        LogManager.getInstance().recordLog("重试任务上报完成");
                        mSuccessJob = null;
                        mSuccessResult = null;
                        mSuccessCallCount = 0;
                    }
                }
                if (mFailedJob != null
                        && mFailedResult != null
                ) {
                    if (mFailedJob.job_status == 5) {
                        LogManager.getInstance().recordLog("重试任务测试通过");
                    } else {
                        LogManager.getInstance().recordLog("重试任务测试未通过");
                    }
                }
            }
        }, 3000);
    }

    /*****注册事件成功回调*****/
    public void sendEventWatcher() {

    }

    void reportJobResult(final Job jobResult, final INetwork.IJobRequestResult iJobRequestResult) {
        // 任务执行成功未上报
        if (jobResult.job_status == 3) {
            mSuccessJob = jobResult;
            mSuccessResult = iJobRequestResult;
            mSuccessCallCount++;
            mSuccessJobList.add(jobResult);
            mSuccessResultList.add(iJobRequestResult);
        }

        // 失败的任务
        if (jobResult.job_status == 5) {
            mFailedJob = jobResult;
            mFailedResult = iJobRequestResult;
            mFailedCallCount++;
        }

    }

}
