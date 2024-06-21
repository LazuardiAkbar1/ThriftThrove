package com.dicoding.capstonui.signup

import android.content.Context
import com.google.gson.annotations.SerializedName

data class SignUpResult(
    @SerializedName("id")
    val id: Int,

    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String
)