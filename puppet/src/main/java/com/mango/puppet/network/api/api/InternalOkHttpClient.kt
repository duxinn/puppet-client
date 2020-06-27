package com.mango.puppet.network.api.api

import android.text.TextUtils
import com.mango.puppet.BuildConfig
import com.mango.puppet.dispatch.system.SystemManager
import com.mango.puppet.network.api.interceptor.PreIntercepet
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class InternalOkHttpClient {
    companion object {
        fun getOkhttpClient(): OkHttpClient {
            var okHttpClient: OkHttpClient? = null

            if (okHttpClient == null) {
                okHttpClient = OkHttpClient.Builder()
                        .addInterceptor {
                            chain ->
                            val builder = chain.request().newBuilder()
                            builder.run {
                                addHeader("p", "android")
                            }
                            if (!TextUtils.isEmpty(SystemManager.getInstance().deviceId)) {
                                builder.addHeader("deviceid", SystemManager.getInstance().deviceId)
                            }
                            val request = builder.build()
                            chain.proceed(request)
                        }
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