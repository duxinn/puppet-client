package com.mango.puppet.network.api.vm

import android.text.TextUtils
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mango.puppet.network.api.api.ApiClient
import com.mango.puppet.network.api.api.NetService
import com.mango.puppet.network.api.basemodel.BaseModel
import com.mango.puppet.network.api.commen.Destiny
import com.mango.puppet.network.api.observerCallBack.DesCallBack
import com.mango.puppet.network.api.transformHelper.RxStreamHelper
import com.mango.puppetmodel.Event
import com.mango.puppetmodel.EventWatcher
import com.mango.puppetmodel.Job

class PuppetVM {
    companion object {

        private val service = ApiClient.instance.getApiService(NetService::class.java)

        fun reportEvent(url: String, event : Event, callBack: DesCallBack<Any>) {
            val jsonObject = JsonParser().parse(event.toString()) as JsonObject
            if (TextUtils.isEmpty(url)) {
                service.reportEvent(jsonObject)
                        .compose(RxStreamHelper.io_Main())
                        .subscribe(Destiny(callBack))
            } else {
                service.reportEvent(url, jsonObject)
                        .compose(RxStreamHelper.io_Main())
                        .subscribe(Destiny(callBack))
            }
        }

        fun reportJobResult(url: String, job: Job, callBack: DesCallBack<Any>) {
            val jsonObject = JsonParser().parse(job.toString()) as JsonObject
            if (TextUtils.isEmpty(url)) {
                service.reportJobResult(jsonObject)
                        .compose(RxStreamHelper.io_Main())
                        .subscribe(Destiny(callBack))
            } else {
                service.reportJobResult(url, jsonObject)
                        .compose(RxStreamHelper.io_Main())
                        .subscribe(Destiny(callBack))
            }
        }

        fun testNetwork(url: String, eventJsonString: String, callBack: DesCallBack<Any>) {
//            if (TextUtils.isEmpty(url)) {
//                service.reportEvent(eventJsonString)
//                        .compose(RxStreamHelper.io_Main())
//                        .subscribe(Destiny(callBack))
//            } else {
//                service.reportEvent(url,eventJsonString)
//                        .compose(RxStreamHelper.io_Main())
//                        .subscribe(Destiny(callBack))
//            }
        }
    }
}