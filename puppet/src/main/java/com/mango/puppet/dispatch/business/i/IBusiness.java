package com.mango.puppet.dispatch.business.i;

import com.mango.puppetmodel.UploadResourceModel;

import java.util.List;

/**
 * IBusiness
 * 业务调度
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public interface IBusiness {

    /**
     * 获取资源文件上传方式
     * @param result 结果
     */
    void getUploadResourceWay(IUploadResourceWayResult result);

    interface IUploadResourceWayResult {

        void onSuccess(List<UploadResourceModel> modelList);

        void onError();
    }
}
