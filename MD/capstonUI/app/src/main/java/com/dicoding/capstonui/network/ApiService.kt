package com.dicoding.capstonui.network

import com.dicoding.capstonui.login.LoginResponse
import com.dicoding.capstonui.model.CartItem
import com.dicoding.capstonui.model.CartResponse
import com.dicoding.capstonui.model.CheckoutRequest
import com.dicoding.capstonui.model.Product
import com.dicoding.capstonui.signup.SignUpResponse
import com.dicoding.capstonui.model.ProductResponse
import com.dicoding.capstonui.model.UserResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path


interface ApiService {
    @FormUrlEncoded
    @POST("signup")
    suspend fun signUp(
        @Field("username") username: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<SignUpResponse>

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    @POST("items")
    @Multipart
    suspend fun addProduct(
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("price") price: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<ProductResponse>

    @GET("items")
    suspend fun getProducts(
        @Header("Authorization") token: String,
    ): Response<List<Product>>

    @FormUrlEncoded
    @POST("cart")
    suspend fun addItemToCart(
        @Field("itemId") itemId: Int,
        @Field("quantity") quantity: Int,
    ): Response<CartResponse>

    @GET("cart")
    suspend fun getCart(): Response<List<CartItem>>

    @DELETE("cart/{itemId}")
    suspend fun deleteCartItem(
        @Path("itemId") itemId: Int,
    ): Response<CartResponse>


    @POST("/checkout")
    suspend fun checkout(
        @Header("Authorization") token: String,
        @Body request: CheckoutRequest
    ): Response<ApiResponse>

    @GET("user/info")
    suspend fun getUserInfo(@Header("Authorization") token: String): Response<Map<String, String>>

    @GET("items/user")
    suspend fun getUploadedProducts(
        @Header("Authorization") token: String
    ): Response<List<Product>>

    @GET("profile")
    suspend fun getUserProfile(
        @Header("Authorization") token: String,
    ): Response<UserResponse>
}
/*    @GET("/items")
    suspend fun getItems(): Response<List<RouteListingPreference.Item>>

    @GET("/items/{id}")
    suspend fun getItemById(@Path("id") id: String): Response<RouteListingPreference.Item>

    @POST("/items")
    @Multipart
    suspend fun addItem(
        @Part image: MultipartBody.Part,
        @Part("item") item: RequestBody,
        @Header("Authorization") token: String
    ): Response<ApiResponse>

    @POST("/cart")
    suspend fun addItemToCart(@Body cartItem: CartItem, @Header("Authorization") token: String): Response<ApiResponse>

    @PUT("/items/{id}")
    @Multipart
    suspend fun updateItem(
        @Path("id") id: String,
        @Part image: MultipartBody.Part,
        @Part("item") item: RequestBody,
        @Header("Authorization") token: String
    ): Response<ApiResponse>

    @DELETE("/items/{id}")
    suspend fun deleteItem(@Path("id") id: String, @Header("Authorization") token: String): Response<ApiResponse>

    @GET("/itemsown")
    suspend fun getItemsByOwnerId(@Header("Authorization") token: String): Response<List<RouteListingPreference.Item>>

    @POST("/checkout")
    suspend fun checkout(@Header("Authorization") token: String): Response<ApiResponse>

    @PUT("/cart")
    suspend fun updateCartItem(@Body cartItem: CartItem, @Header("Authorization") token: String): Response<ApiResponse>

    @GET("/cart")
    suspend fun getCart(@Header("Authorization") token: String): Response<List<CartItem>>

    @DELETE("/cart")
    suspend fun deleteCartItem(@Body cartItem: CartItem, @Header("Authorization") token: String): Response<ApiResponse>

    @GET("/track/{orderId}")
    suspend fun trackOrder(@Path("orderId") orderId: String, @Header("Authorization") token: String): Response<OrderStatus>*/
//}
