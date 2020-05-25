package com.mango.puppet.network.api.observerCallBack

interface DesCallBack<T> {
    fun onHandleSuccess(t: T?)
    fun onHandleError(msg: String?, code: Int)
    fun onNetWorkError(e: Throwable?)
}