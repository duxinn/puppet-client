package com.mango.puppet.network.api.api

import com.google.gson.JsonObject
import com.mango.puppet.network.api.basemodel.BaseModel
import com.mango.puppet.bean.QinNiuInfo
import io.reactivex.Observable
import retrofit2.http.*


interface NetService {

    /**
     * 向远端服务器报告事件(事件本身带有指定访问url)
     */
    @POST
    fun reportEvent(
            @Url url: String,
            @Body info: JsonObject
    ): Observable<BaseModel<Any>>

    /**
     * 向远端服务器报告事件(和远程服务器定义好接口)
     */
    @POST("api/reportEvent")
    fun reportEvent(
            @Body info: JsonObject
    ): Observable<BaseModel<Any>>

    /**
     * 向远端服务器报告任务结果(任务本身带有指定callbackUrl)
     */
    @POST
    fun reportJobResult(
            @Url url: String,
            @Body info: JsonObject
    ): Observable<BaseModel<Any>>

    /**
     * 获取七牛token
     */
    @GET
    fun getQiNiuInfo(
            @Url url: String
    ): Observable<BaseModel<QinNiuInfo>>



    /**
     * 向远端服务器报告任务结果(和远程服务器定义好接口)
     */
    @POST("api/reportJobResult")
    fun reportJobResult(
            @Body info: JsonObject
    ): Observable<BaseModel<Any>>
}