package com.mango.puppet.network.api.api

import com.mango.puppet.network.api.basemodel.BaseModel
import com.mango.puppet.network.dto.TestDTO
import io.reactivex.Observable
import retrofit2.http.*


interface NetService {

    @GET("api/test/noParam/get")
    fun getNoParamData(): Observable<BaseModel<MutableList<TestDTO>>>

    @GET("api/test/haveParam/get")
    fun getHaveParamData(@Query("param") code: Int): Observable<BaseModel<TestDTO>>

    @FormUrlEncoded
    @POST("api/test/post")
    fun testPost(
            @Field("code") code: String
    ): Observable<BaseModel<TestDTO>>
}