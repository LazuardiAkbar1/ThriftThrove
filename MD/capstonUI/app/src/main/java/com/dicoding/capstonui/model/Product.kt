package com.dicoding.capstonui.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Product(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("price")
    val price: String? = null,

    @SerializedName("image")
    val image: String? = null,

    @SerializedName("owner_id")
    val owner_id: Int? = null,

    @SerializedName("created_at")
    val created_at: String? = null,

    @SerializedName("username")
    val username: String? = null,

    @SerializedName("email")
    val email: String? = null
): Parcelable