package com.dicoding.capstonui.model

import com.google.gson.annotations.SerializedName

data class CartResponse (
    @SerializedName("message")
    val message: String,

    @SerializedName("cart_id")
    val cartId: Int
)