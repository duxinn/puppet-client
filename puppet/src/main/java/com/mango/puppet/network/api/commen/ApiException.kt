package com.mango.puppet.network.api.commen

/**
 * 和服务端约定的错误
 */
class ApiException(message: String?, cause: Throwable?) : Exception(message, cause) {
    var code: Int? = 0
    var msg: String? = null

    constructor(code: Int, message: String?, cause: Throwable?) : this(message, cause) {
        this.code = code
        this.msg = message
    }

    constructor(code: Int, msg: String) : this(msg, Throwable(msg)) {
        this.code = code
        this.msg = msg
    }

    fun msg(msg: String) {
        this.msg = msg
    }

    fun code(code: Int) {
        this.code = code
    }

}