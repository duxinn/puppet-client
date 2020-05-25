package com.mango.puppet.network.i;

import android.content.Context;

import com.mango.puppetmodel.Event;
import com.mango.puppetmodel.Job;

/**
 * INetwork
 * 网络层与本期其他模块交互时提供的接口
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public interface INetwork {

    /**
     * 启动本地server/长连接
     * @param result 启动结果
     */
    void setupNetwork(Context context, ISetupResult result);

    /**
     * 上报任务结果
     *
     * @param jobResult 任务结果
     */
    void reportJobResult(Job jobResult);

    /**
     * 上报新事件
     *
     * @param url   上报新事件的远程服务器url
     * @param event 新事件
     * @param requestResult 请求结果
     */
    void reportEvent(String url, Event event, IEventRequestResult requestResult);

    /**
     * 获取上传资源文件的方式及参数
     * @param requestResult 请求结果
     */
    void requestUploadResourceWay(IRequestResult requestResult);

    interface ISetupResult {

        void onSuccess();

        void onFailure();
    }

    interface IJobRequestResult {

        /**
         * 上报结果成功
         * @param jobResult 任务结果
         */
        void onSuccess(Job jobResult);

        /**
         * 远程控制器返回的逻辑错误
         * @param jobResult 任务结果
         * @param errorCode 错误码
         * @param errorMessage 错误信息
         */
        void onError(Job jobResult, int errorCode, String errorMessage);

        /**
         * 网络错误
         * @param jobResult 任务结果
         */
        void onNetworkError(Job jobResult);
    }

    interface IEventRequestResult {

        /**
         * 上报新事件成功
         * @param event 新事件
         */
        void onSuccess(Event event);

        /**
         * 远程控制器返回的逻辑错误
         * @param event 新事件
         * @param errorCode 错误码
         * @param errorMessage 错误信息
         */
        void onError(Event event, int errorCode, String errorMessage);

        /**
         * 网络错误
         * @param event 新事件
         */
        void onNetworkError(Event event);
    }


    interface IRequestResult {

        /**
         * 请求成功
         * @param result 结果数据
         */
        void onSuccess(Object result);

        /**
         * 远程控制器返回的逻辑错误
         *
         * @param errorCode 错误码
         * @param errorMessage 错误信息
         */
        void onError(int errorCode, String errorMessage);

        /**
         * 网络错误
         */
        void onNetworkError();
    }
}
