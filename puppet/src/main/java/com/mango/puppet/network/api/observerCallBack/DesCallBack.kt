package com.mango.puppet.network.api.observerCallBack

interface DesCallBack<T> {
    fun success(any: T)
    fun failed(e: Throwable)
    fun onSubscribe()
}