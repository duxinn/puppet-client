package com.mango.puppet.dispatch.job.db;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by blueZhang on 2020-05-25.
 *
 * @Author: BlueZhang
 * @date: 2020-05-25
 */
@Table(database = AppDatabase.class)
public class JobBean extends BaseModel {
    // 由远程服务端生成的任务唯一标识
    @PrimaryKey(autoincrement = true)
    private long job_id;

    // 任务在哪个应用上执行，系统任务不需此参数
    @Column
    private String package_name;

    // 任务名称 标识是什么任务
    @Column
    private String job_name;

    // 任务状态 0:待执行 1:正在执行 2:执行成功 3:已取消 4:执行失败
    @Column
    private int job_status;

    @Column
    private String job_json_string;

    public long getJob_id() {
        return job_id;
    }

    public void setJob_id(long job_id) {
        this.job_id = job_id;
    }

    public String getPackage_name() {
        return package_name;
    }

    public void setPackage_name(String package_name) {
        this.package_name = package_name;
    }

    public String getJob_name() {
        return job_name;
    }

    public void setJob_name(String job_name) {
        this.job_name = job_name;
    }

    public int getJob_status() {
        return job_status;
    }

    public void setJob_status(int job_status) {
        this.job_status = job_status;
    }

    public String getJob_json_string() {
        return job_json_string;
    }

    public void setJob_json_string(String job_json_string) {
        this.job_json_string = job_json_string;
    }
}
