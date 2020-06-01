package com.mango.puppet.dispatch.job.db;

import android.content.Context;

import com.google.gson.Gson;
import com.mango.puppet.status.StatusManager;
import com.mango.puppetmodel.Job;
import com.raizlabs.android.dbflow.config.DatabaseConfig;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.Nullable;
import retrofit2.http.DELETE;

/**
 * Created by blueZhang on 2020-05-25.
 *
 * @Author: BlueZhang
 * @date: 2020-05-25
 */
public class DBManager {
    private static final ArrayList<OnJobDBChangeListener> jbListenerDatas =
            new ArrayList<>();

    private static final int ADD = 0x01;
    private static final int REMOVE = 0x02;
    private static final int UPDATE = 0x03;

    private DBManager(Context context) {
    }

    public interface OnJobDBChangeListener {
        void onJobInsert(Job job);

        void onJobDelete(Job job);

        void onJobStatusChange(Job job);
    }


    public static boolean addJobDbListener(OnJobDBChangeListener listener) {
        if (!jbListenerDatas.contains(listener)) {
            return jbListenerDatas.add(listener);
        }
        return true;
    }

    public static boolean removeJobDbListener(OnJobDBChangeListener listener) {
        return jbListenerDatas.remove(listener);
    }

    private static boolean isInit = false;
    /**
     * Call this method in Application onCreate
     *
     * @param context context
     */
    public static void init(Context context) {
        if (!isInit) {
            isInit = true;
            FlowManager.init(FlowConfig.builder(context)
                    .addDatabaseConfig(
                            DatabaseConfig.builder(AppDatabase.class)
                                    .databaseName("AppDatabase")
                                    .build())
                    .build());
        }
    }

    public static boolean insertJobIntoDb(Job job) {
        JobBean jobBean = new JobBean();
        jobBean.setJob_id(job.job_id);
        jobBean.setJob_status(job.job_status);
        jobBean.setJob_name(job.job_name);
        jobBean.setJob_json_string(new Gson().toJson(job));
        boolean ret = jobBean.insert() > 0;
        if (ret) {
            notify(ADD, job);
            logJobCount();
        }
        return ret;
    }

    public static ArrayList<Job> getAllJobsFromDb() {
        ArrayList<JobBean> jobBeans = (ArrayList<JobBean>) SQLite.
                select().
                from(JobBean.class).
                queryList();
        return handleJobBeanListToJobs(jobBeans);
    }

    public static ArrayList<Job> getReportJobs() {
        ArrayList<JobBean> jobBeans = (ArrayList<JobBean>) SQLite
                .select()
                .from(JobBean.class)
                .where(JobBean_Table.job_status.in(2, 3, 4, 5))
                .queryList();
        return handleJobBeanListToJobs(jobBeans);
    }


    public static ArrayList<Job> getJobsDependOnStatus(int status) {
        ArrayList<JobBean> jobBeans = (ArrayList<JobBean>) SQLite.
                select().
                from(JobBean.class).where(JobBean_Table.job_status.eq(status)).
                queryList();
        return handleJobBeanListToJobs(jobBeans);
    }

    public static Job getJobsById(long job_id) {
        JobBean jobBeans = SQLite
                .select()
                .from(JobBean.class)
                .where(JobBean_Table.job_id.eq(job_id))
                .querySingle();
        return handleJobBeanToJob(jobBeans, new Gson());
    }

    public static void clearDB() {
        for (Job cdJob : DBManager.getAllJobsFromDb()) {
            DBManager.deleteJob(cdJob.job_id);
        }
    }

    @Nullable
    public static Job getSingleNotDoneJobsFromDb() {
        Job jobBean = null;

        List<JobBean> beans = SQLite.
                select().
                from(JobBean.class).where(JobBean_Table.job_status.in(0, 1)).
                queryList();
        if (beans != null && beans.size() > 0) {
            jobBean = handleJobBeanToJob(beans.get(0), new Gson());
        }
        return jobBean;
    }

    public static boolean deleteJob(long jobId) {
        boolean ret = false;
        JobBean jb = SQLite.select()
                .from(JobBean.class)
                .where(JobBean_Table.job_id.eq(jobId))
                .querySingle();//区别于queryList(),返回的是实体
        if (jb != null) {
            ret = jb.delete();
            notify(REMOVE, handleJobBeanToJob(jb, new Gson()));
            logJobResultCount();
        }
        return ret;
    }


    private static void logJobResultCount() {
        ArrayList<Job> reportJobs = DBManager.getReportJobs();
        if (reportJobs != null) {
            StatusManager.getInstance().setJobResultCount(reportJobs.size());
        }
    }


    private static void logJobCount() {
        ArrayList<Job> jbs = DBManager.getJobsDependOnStatus(0);
        if (jbs == null) return;
        StatusManager.getInstance().setJobCount(jbs.size());
    }

    public static boolean updateJobStatus(Job job) {
        boolean ret = false;
        JobBean jb = SQLite.select()
                .from(JobBean.class)
                .where(JobBean_Table.job_id.eq(job.job_id))
                .querySingle();//区别于queryList(),返回的是实体

        if (jb != null) {
            jb.setJob_status(job.job_status);
            jb.setJob_name(job.job_name);
            jb.setPackage_name(job.package_name);
            jb.setJob_json_string(new Gson().toJson(job));
            ret = jb.update();
            if (ret) {
                notify(UPDATE, job);
                logJobResultCount();
            }
        }
        return ret;
    }

    public static void notify(int type, Job job) {
        if (jbListenerDatas != null) {
            for (int i = 0; i < jbListenerDatas.size(); i++) {
                if (type == ADD) {
                    jbListenerDatas.get(i).onJobInsert(job);
                } else if (type == REMOVE) {
                    jbListenerDatas.get(i).onJobDelete(job);
                } else if (type == UPDATE) {
                    jbListenerDatas.get(i).onJobStatusChange(job);
                }
            }
        }
    }

    public static ArrayList<Job> handleJobBeanListToJobs(ArrayList<JobBean> list) {
        if (list == null || list.size() <= 0) {
            return new ArrayList<>();
        }
        Gson gson = new Gson();
        ArrayList<Job> jobs = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            JobBean jobBean = list.get(i);
            jobs.add(handleJobBeanToJob(jobBean, gson));

        }
        return jobs;
    }

    @Nullable
    public static Job handleJobBeanToJob(JobBean jobBean, Gson gson) {
        if (jobBean == null) {
            return null;
        }
        return gson.fromJson(jobBean.getJob_json_string(), Job.class);
    }


}
