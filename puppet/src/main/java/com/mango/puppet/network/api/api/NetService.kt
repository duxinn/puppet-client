package com.mango.puppet.network.api.api

import com.mango.puppet.network.api.basemodel.BaseModel
import com.mango.puppet.network.dto.BaseDTO
import com.mango.puppetmodel.Job
import io.reactivex.Observable
import org.json.JSONObject
import retrofit2.http.*


interface NetService {

    @GET("api/test/noParam/get")
    fun getNoParamData(): Observable<BaseModel<MutableList<BaseDTO>>>

    @GET("api/test/haveParam/get")
    fun getHaveParamData(@Query("param") code: Int): Observable<BaseModel<BaseDTO>>

    @FormUrlEncoded
    @POST("api/test/post")
    fun testPost(
            @Field("code") code: String
    ): Observable<BaseModel<BaseDTO>>

    /**
     * 向远端服务器报告事件(事件本身带有指定访问url)
     */
    @FormUrlEncoded
    @POST
    fun reportEvent(
            @Url url: String,
            @Field("name") name: String,
            @Field("package_name") packageName: String,
            @Field("data") eventData: Map<Any, Any>
    ): Observable<BaseModel<BaseDTO>>

    /**
     * 向远端服务器报告事件(和远程服务器定义好接口)
     */
    @FormUrlEncoded
    @POST("api/reportEvent")
    fun reportEvent(
            @Field("name") name: String,
            @Field("package_name") packageName: String,
            @Field("data") eventData: Map<Any, Any>
    ): Observable<BaseModel<BaseDTO>>

    /**
     * 向远端服务器报告任务结果(任务本身带有指定callbackUrl)
     */
    @FormUrlEncoded
    @POST
    fun reportJobResult(
            @Url url: String,
            @Field("job_id") jobId: Long,
            @Field("package_name") packageName: String,
            @Field("job_name") jobName: String,
            @Field("job_status") jobStatus: Int,
            @Field("error_code") errorCode: Int,
            @Field("error_message") errorMessage: String,
            @Field("result_data") resultData: JSONObject
    ): Observable<BaseModel<BaseDTO>>

    /**
     * 向远端服务器报告任务结果(和远程服务器定义好接口)
     */
    @FormUrlEncoded
    @POST("api/reportJobResult")
    fun reportJobResult(
            @Field("job_id") jobId: Long,
            @Field("package_name") packageName: String,
            @Field("job_name") jobName: String,
            @Field("job_status") jobStatus: Int,
            @Field("error_code") errorCode: Int,
            @Field("error_message") errorMessage: String,
            @Field("result_data") resultData: JSONObject
    ): Observable<BaseModel<BaseDTO>>
}