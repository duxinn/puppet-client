package com.mango.puppet.network.api.basemodel

import com.google.gson.annotations.SerializedName

open class BaseModel<T> {

    @SerializedName("status")
    var code = 0

    @SerializedName("message")
    var msg: String? = null

    @SerializedName("data")
    var data: T? = null

    open fun isSuccess(): Boolean {
        return code == 0
    }
}