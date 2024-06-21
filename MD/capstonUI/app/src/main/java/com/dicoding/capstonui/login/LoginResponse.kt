package com.dicoding.capstonui.login

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("auth")
    val auth: Boolean,

    @SerializedName("token")
    val token: String? = null
)