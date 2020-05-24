package com.mango.puppet.network.api.commen

/**
 * 实现dto到前端自定义vo转换
 */
interface Transform<T> {
    fun transform(): T
}