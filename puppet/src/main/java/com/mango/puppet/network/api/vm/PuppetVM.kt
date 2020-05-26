package com.mango.puppet.network.api.vm

import android.text.TextUtils
import com.mango.puppet.network.api.api.ApiClient
import com.mango.puppet.network.api.api.NetService
import com.mango.puppet.network.api.basemodel.BaseModel
import com.mango.puppet.network.api.commen.Destiny
import com.mango.puppet.network.api.observerCallBack.DesCallBack
import com.mango.puppet.network.api.transformHelper.RxStreamHelper

class PuppetVM {
    companion object {

        private val service = ApiClient.instance.getApiService(NetService::class.java)

        fun reportEventWatcherCallBack(url: String, eventWatcherJsonString: String, callBack: DesCallBack<BaseModel<Any>>) {
            if (TextUtils.isEmpty(url)) {
                service.reportEventWatcherCallBack(eventWatcherJsonString)
                        .compose(RxStreamHelper.io_Main())
                        .subscribe(Destiny(callBack))
            } else {
                service.reportEventWatcherCallBack(url, eventWatcherJsonString)
                        .compose(RxStreamHelper.io_Main())
                        .subscribe(Destiny(callBack))
            }
        }

        fun reportEvent(url: String, eventJsonString: String, callBack: DesCallBack<BaseModel<Any>>) {
            if (TextUtils.isEmpty(url)) {
                service.reportEvent(eventJsonString)
                        .compose(RxStreamHelper.io_Main())
                        .subscribe(Destiny(callBack))
            } else {
                service.reportEvent(url, eventJsonString)
                        .compose(RxStreamHelper.io_Main())
                        .subscribe(Destiny(callBack))
            }
        }

        fun reportJobResult(url: String, jobJsonString: String, callBack: DesCallBack<Any>) {
            if (TextUtils.isEmpty(url)) {
                service.reportJobResult(jobJsonString)
                        .compose(RxStreamHelper.io_Main())
                        .subscribe(Destiny(callBack))
            } else {
                service.reportJobResult(url, jobJsonString)
                        .compose(RxStreamHelper.io_Main())
                        .subscribe(Destiny(callBack))
            }
        }
    }
}