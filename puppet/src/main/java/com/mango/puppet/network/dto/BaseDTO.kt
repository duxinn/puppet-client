package com.mango.puppet.network.dto

data class BaseDTO(
        // 后台特殊返回成败标识符
        var status: Int,
        var msg: String,
        var data: String,
        var isSuccess: Boolean = status == 0
)