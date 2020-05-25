package com.mango.puppet.network.api.commen

import android.util.Log
import com.mango.puppet.network.api.observerCallBack.DesCallBack
import io.reactivex.Observer
import io.reactivex.disposables.Disposable


/**
 * subscribe的时候使用这个接口
 */
class Destiny<T>(callBack: DesCallBack<T>) : Observer<T> {
    private var callBack: DesCallBack<T>? = callBack

    override fun onComplete() {

    }

    override fun onSubscribe(d: Disposable) {
        callBack!!.onSubscribe()
    }

    override fun onNext(t: T) {
        callBack?.success(t)
    }

    override fun onError(e: Throwable) {
        // 网络错误
        e.printStackTrace()
        if (e.message != null) {
            Log.e("Destiny", e.message!!)
        }
        callBack?.failed(e)
    }

}