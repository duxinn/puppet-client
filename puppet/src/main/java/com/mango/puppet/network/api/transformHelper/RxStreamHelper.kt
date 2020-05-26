package com.mango.puppet.network.api.transformHelper

import com.mango.puppet.network.api.commen.CustomException
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers

/**
 * 统一处理，线程调度
 */
class RxStreamHelper {
    companion object {
        fun <T> io_Main(): ObservableTransformer<T, T> {
            return ObservableTransformer { upstream ->
                upstream.subscribeOn(Schedulers.io())
                    //出错统一处理
                    .onErrorResumeNext(Function { throwable ->
                        Observable.error(
                            CustomException().handleException(
                                throwable
                            )
                        )
                    })
                    .observeOn(AndroidSchedulers.mainThread())
            }

        }
    }
}
