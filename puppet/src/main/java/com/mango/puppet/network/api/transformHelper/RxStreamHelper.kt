package com.mango.puppet.network.api.transformHelper

import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * 统一处理，线程调度
 */
class RxStreamHelper {
    companion object {
        fun <T> io_Main(): ObservableTransformer<T, T> {
            return ObservableTransformer { upstream ->
                upstream.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            }

        }
    }
}
