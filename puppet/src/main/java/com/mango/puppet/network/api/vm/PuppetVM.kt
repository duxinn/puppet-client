package com.mango.puppet.network.api.vm

import com.mango.puppet.network.api.api.ApiClient
import com.mango.puppet.network.api.api.NetService
import com.mango.puppet.network.api.commen.Destiny
import com.mango.puppet.network.api.observerCallBack.DesCallBack
import com.mango.puppet.network.api.transformHelper.RxStreamHelper
import com.mango.puppet.network.dto.TestDTO

class PuppetVM {
    companion object {

        private val service = ApiClient.instance.getApiService(NetService::class.java)

        fun getNoParamData(desCallBack: DesCallBack<MutableList<TestDTO>>) {
            service.getNoParamData().compose(RxStreamHelper.io_Main()).subscribe(Destiny(desCallBack))
        }

        fun getHaveParamData(code: Int, desCallBack: DesCallBack<TestDTO>) {
            service.getHaveParamData(code)
                    .compose(RxStreamHelper.io_Main())
                    .subscribe(Destiny(desCallBack))
        }

        fun testPost(code: String, callBack: DesCallBack<TestDTO>) {
            service.testPost(code)
                    .compose(RxStreamHelper.io_Main())
                    .subscribe(Destiny(callBack))
        }
    }
}