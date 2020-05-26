package com.mango.puppet.network.api.api

import com.mango.puppet.BuildConfig
import com.mango.puppet.network.api.interceptor.PreIntercepet
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class InternalOkHttpClient {
    companion object {
        fun getOkhttpClient(): OkHttpClient {
            var okHttpClient: OkHttpClient? = null

            if (okHttpClient == null) {
                okHttpClient = OkHttpClient.Builder()
                        .retryOnConnectionFailure(true)
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)

                        .build()
            }

            if (BuildConfig.DEBUG) {
                okHttpClient = okHttpClient!!.newBuilder().addInterceptor(PreIntercepet().setLevel(PreIntercepet.Level.BODY)).build()
            }
            return okHttpClient!!
        }
    }
}