package com.dicoding.capstonui.model


import com.google.gson.annotations.SerializedName

data class CheckoutRequest(
    @SerializedName("address") val address: String,
    @SerializedName("name") val name: String
)