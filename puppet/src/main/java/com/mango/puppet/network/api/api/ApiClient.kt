package com.mango.puppet.network.api.api

import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class ApiClient {
    private var BASE_URL_MAIN = "http://10.154.41.171:8080/"
    private var retrofit: Retrofit? = null
    private var interceptor: Interceptor? = null

    private var netInterceptor: Interceptor? = null

    private var apiService: Any? = null

    companion object {
        val instance: ApiClient by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            ApiClient()
        }
    }


    public fun <T> getApiService(clazz: Class<T>): T {
        if (apiService == null) {
            apiService = retrofit!!.create(clazz)
        }
        return apiService as T
    }


    /**
     * return the retrofit finally
     */
    fun build() {
        val okHttpClient = InternalOkHttpClient.getOkhttpClient()
        if (interceptor != null) {
            okHttpClient.newBuilder().addInterceptor(interceptor!!)
        }
        if (netInterceptor != null) {
            okHttpClient.newBuilder().addInterceptor(netInterceptor!!)
        }
        if (this.retrofit == null) {
            this.retrofit = Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .baseUrl(BASE_URL_MAIN)
                    .client(okHttpClient)
                    .build()
        }
    }
}