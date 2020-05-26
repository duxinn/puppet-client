package com.mango.puppet.network.api.api

import com.mango.puppet.network.api.basemodel.BaseModel
import io.reactivex.Observable
import retrofit2.http.*


interface NetService {
    /**
     * 向服务端报告事件监听的注册/注销的结果(EventWatcher本身带有指定callbackUrl)
     */
    @FormUrlEncoded
    @POST
    fun reportEventWatcherCallBack(
            @Url url: String,
            @Field("event_watcher_json") eventWatcherJson: String
    ): Observable<BaseModel<Any>>

    /**
     * 向服务端报告事件监听的注册/注销的结果(EventWatcher本身未带有指定访问url)
     */
    @FormUrlEncoded
    @POST("api/reportEventWatcherCallBack")
    fun reportEventWatcherCallBack(
            @Field("event_watcher_json") eventWatcherJson: String
    ): Observable<BaseModel<Any>>

    /**
     * 向远端服务器报告事件(事件本身带有指定访问url)
     */
    @FormUrlEncoded
    @POST
    fun reportEvent(
            @Url url: String,
            @Field("event_json") eventJson: String
    ): Observable<BaseModel<Any>>

    /**
     * 向远端服务器报告事件(和远程服务器定义好接口)
     */
    @FormUrlEncoded
    @POST("api/reportEvent")
    fun reportEvent(
            @Field("event_json") eventJson: String
    ): Observable<BaseModel<Any>>

    /**
     * 向远端服务器报告任务结果(任务本身带有指定callbackUrl)
     */
    @FormUrlEncoded
    @POST
    fun reportJobResult(
            @Url url: String,
            @Field("job_json") jobJson: String
    ): Observable<BaseModel<Any>>

    /**
     * 向远端服务器报告任务结果(和远程服务器定义好接口)
     */
    @FormUrlEncoded
    @POST("api/reportJobResult")
    fun reportJobResult(
            @Field("job_json") jobJson: String
    ): Observable<BaseModel<Any>>
}