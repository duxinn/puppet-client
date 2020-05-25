package com.mango.puppet.network.api.vm

import com.mango.puppet.network.api.api.ApiClient
import com.mango.puppet.network.api.api.NetService
import com.mango.puppet.network.api.commen.Destiny
import com.mango.puppet.network.api.observerCallBack.DesCallBack
import com.mango.puppet.network.api.transformHelper.RxStreamHelper
import com.mango.puppet.network.dto.BaseDTO

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
            service.reportEvent(url, name, packageName, data)
                    .compose(RxStreamHelper.io_Main())
                    .subscribe(Destiny(callBack))
        }

        fun reportEvent(name: String, packageName: String, data: Map<Any, Any>, callBack: DesCallBack<BaseDTO>) {
            service.reportEvent(name, packageName, data)
                    .compose(RxStreamHelper.io_Main())
                    .subscribe(Destiny(callBack))
        }
    }
}