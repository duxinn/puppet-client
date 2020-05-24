package com.mango.puppet.network.api.basemodel

import com.google.gson.annotations.SerializedName

open class BaseModel<T> {
    @SerializedName("data")
    var data: T? = null
    var code: Int? = null
    var msg: String? = null
}