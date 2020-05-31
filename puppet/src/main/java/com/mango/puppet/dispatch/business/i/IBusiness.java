package com.mango.puppet.dispatch.business.i;

import com.mango.puppet.plugin.UploadChannelInfo;

import java.util.ArrayList;
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
    void getUploadResourceWay(ArrayList<String> supportChannels, IUploadResourceWayResult result);

    interface IUploadResourceWayResult {

        void onSuccess(List<UploadChannelInfo> modelList);

        void onError();
    }
}
