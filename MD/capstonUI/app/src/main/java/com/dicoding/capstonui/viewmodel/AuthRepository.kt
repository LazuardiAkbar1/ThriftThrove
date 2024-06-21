package com.dicoding.capstonui.viewmodel

import com.dicoding.capstonui.login.LoginResponse
import com.dicoding.capstonui.model.Product
import com.dicoding.capstonui.model.ProductResponse
import com.dicoding.capstonui.network.ApiService
import com.dicoding.capstonui.network.RetrofitClient
import com.dicoding.capstonui.signup.SignUpResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

class AuthRepository(private val apiService: ApiService) {

    suspend fun signUp(username: String, email: String, password: String): Response<SignUpResponse> {
        return apiService.signUp(username, email, password)
    }

    suspend fun login(username: String, password: String): Response<LoginResponse> {
        return apiService.login(username, password)
    }

    suspend fun addProduct(name: RequestBody, description: RequestBody, price: RequestBody, image: MultipartBody.Part): Response<ProductResponse> {
        return apiService.addProduct(name, description, price, image)
    }

    suspend fun getProducts(token: String): Response<List<Product>> {
        return apiService.getProducts(token)
    }
}
