package com.mango.puppet.network.api.commen

import android.util.Log
import com.mango.puppet.network.api.basemodel.BaseModel
import com.mango.puppet.network.api.observerCallBack.DesCallBack
import io.reactivex.Observer
import io.reactivex.disposables.Disposable


/**
 * subscribe的时候使用这个接口
 */
class Destiny<T>(callBack: DesCallBack<T>) : Observer<BaseModel<T>> {
    private var callBack: DesCallBack<T>? = callBack

    override fun onComplete() {

    }

    override fun onError(e: Throwable) {
        e.printStackTrace()
        if (e.message != null) {
            Log.e("Destiny", e.message!!)
        }
        callBack?.onNetWorkError(e)
    }

    override fun onNext(t: BaseModel<T>) {
        if (t.isSuccess()) {
            callBack?.onHandleSuccess(t.data)
        } else {
            callBack?.onHandleError(t.msg, t.code)
        }
    }

    override fun onSubscribe(d: Disposable) {
    }

}