package com.dicoding.capstonui.model

import com.google.gson.annotations.SerializedName

data class CartItem(
    @SerializedName("id")
    val id: Int,

    @SerializedName("user_id")
    val user_id: Int,

    @SerializedName("item_id")
    val item_id: Int,

    @SerializedName("item_name")
    val item_name: String,

    @SerializedName("price")
    val price:String,

    @SerializedName("image")
    val image:String,

    @SerializedName("quantity")
    val quantity: Int
)
