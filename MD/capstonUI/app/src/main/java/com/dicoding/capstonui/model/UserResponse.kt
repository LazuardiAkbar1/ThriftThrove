package com.dicoding.capstonui.model

import com.dicoding.capstonui.signup.SignUpResult
import com.google.gson.annotations.SerializedName

data class UserResponse (
    @SerializedName("id")
    val id: Int,

    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email:String
)