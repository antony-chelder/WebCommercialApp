package com.prettyfoxy.datafoxy

import java.util.*

data class PushEntity(
    val user_id : String,
    val timezone : String,
    val created_at : Date,
    val idfa : String? = null,
    val push_token : String? = null,
    val updated_at : String? = null
)
data class RequestPushToken(
    val push_token: String
)
