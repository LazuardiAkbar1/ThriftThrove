package com.dicoding.capstonui.repository

import com.dicoding.capstonui.login.LoginResponse
import com.dicoding.capstonui.network.ApiService
import com.dicoding.capstonui.signup.SignUpResponse
import retrofit2.Response

class Repository(private val apiService: ApiService) {

    suspend fun signUp(username: String, email: String, password: String): Response<SignUpResponse> {
        return apiService.signUp(username, email, password)
    }

    suspend fun login(username: String, password: String): Response<LoginResponse> {
        return apiService.login(username, password)
    }
}
