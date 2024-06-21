package com.dicoding.capstonui.signup

import com.google.gson.annotations.SerializedName

data class SignUpResponse(
    @SerializedName("auth")
    val auth: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("user")
    val user: SignUpResult? = null
)

