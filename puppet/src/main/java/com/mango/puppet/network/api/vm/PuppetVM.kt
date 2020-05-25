package com.mango.puppet.network.api.vm

import android.text.TextUtils
import com.mango.puppet.network.api.api.ApiClient
import com.mango.puppet.network.api.api.NetService
import com.mango.puppet.network.api.commen.Destiny
import com.mango.puppet.network.api.observerCallBack.DesCallBack
import com.mango.puppet.network.api.transformHelper.RxStreamHelper
import com.mango.puppet.network.dto.BaseDTO
import org.json.JSONObject

class PuppetVM {
    companion object {

        private val service = ApiClient.instance.getApiService(NetService::class.java)

        fun getNoParamData(desCallBack: DesCallBack<MutableList<BaseDTO>>) {
            service.getNoParamData().compose(RxStreamHelper.io_Main()).subscribe(Destiny(desCallBack))
        }

        fun getHaveParamData(code: Int, desCallBack: DesCallBack<BaseDTO>) {
            service.getHaveParamData(code)
                    .compose(RxStreamHelper.io_Main())
                    .subscribe(Destiny(desCallBack))
        }

        fun reportEvent(url: String, name: String, packageName: String, data: Map<Any, Any>, callBack: DesCallBack<BaseDTO>) {
            if (TextUtils.isEmpty(url)) {
                service.reportEvent(name, packageName, data)
                        .compose(RxStreamHelper.io_Main())
                        .subscribe(Destiny(callBack))
            } else {
                service.reportEvent(url, name, packageName, data)
                        .compose(RxStreamHelper.io_Main())
                        .subscribe(Destiny(callBack))
            }
        }

        fun reportJobResult(url: String, jobId: Long, packageName: String, jobName: String, jobStatus: Int, errorCode: Int, errorMessage: String, resultData: JSONObject, callBack: DesCallBack<BaseDTO>) {
            if (TextUtils.isEmpty(url)) {
                service.reportJobResult(jobId, packageName, jobName, jobStatus, errorCode, errorMessage, resultData)
                        .compose(RxStreamHelper.io_Main())
                        .subscribe(Destiny(callBack))
            } else {
                service.reportJobResult(url, jobId, packageName, jobName, jobStatus, errorCode, errorMessage, resultData)
                        .compose(RxStreamHelper.io_Main())
                        .subscribe(Destiny(callBack))
            }
        }
    }
}