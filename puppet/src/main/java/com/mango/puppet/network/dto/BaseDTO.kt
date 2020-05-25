package com.mango.puppet.network.dto

data class BaseDTO(
        // 后台特殊返回成败标识符
        var status: Int,
        var isSuccess: Boolean = status == 0
)