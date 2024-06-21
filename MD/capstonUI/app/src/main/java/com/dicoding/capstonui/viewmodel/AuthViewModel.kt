package com.dicoding.capstonui.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dicoding.capstonui.login.LoginResponse
import com.dicoding.capstonui.model.Product
import com.dicoding.capstonui.model.ProductResponse
import com.dicoding.capstonui.network.ApiService
import com.dicoding.capstonui.network.RetrofitClient
import com.dicoding.capstonui.signup.SignUpResponse
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

class AuthViewModel(private val repository: AuthRepository, application: Application) : AndroidViewModel(application) {

    private val _loginResponse = MutableLiveData<LoginResponse>()
    val loginResponse: LiveData<LoginResponse> get() = _loginResponse

    private val _signUpResponse = MutableLiveData<SignUpResponse>()
    val signUpResponse: LiveData<SignUpResponse> get() = _signUpResponse

    private val _productResponse = MutableLiveData<ProductResponse>()
    val productResponse get() = _productResponse

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> get() = _products

    private val _status = MutableLiveData<Status>()
    val status: LiveData<Status> = _status

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val response = repository.login(username, password)
                handleLoginResponse(response)
            } catch (e: Exception) {
                _loginResponse.postValue(LoginResponse(false, null))
            }
        }
    }

    fun signUp(username: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = repository.signUp(username, email, password)
                handleSignUpResponse(response)
            } catch (e: Exception) {
                _signUpResponse.postValue(SignUpResponse(false, e.message ?: "Sign up failed", null))
            }
        }
    }

    private fun handleLoginResponse(response: Response<LoginResponse>) {
        if (response.isSuccessful) {
            _loginResponse.postValue(response.body())
            response.body()?.token?.let { saveToken(it) }
        } else {
            _loginResponse.postValue(LoginResponse(false, null))
        }
    }

    private fun handleSignUpResponse(response: Response<SignUpResponse>) {
        if (response.isSuccessful) {
            _signUpResponse.postValue(response.body())
        } else {
            _signUpResponse.postValue(SignUpResponse(false, "Sign up failed", null))
        }
    }

    fun addProduct(name: String, description: String, price: String, image: MultipartBody.Part) {
        val namePart = name.toRequestBody("text/plain".toMediaTypeOrNull())
        val descriptionPart = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val pricePart = price.toRequestBody("text/plain".toMediaTypeOrNull())

        viewModelScope.launch {
            try {
                val token = getTokenFromPreferences()
                val response = repository.addProduct(namePart, descriptionPart, pricePart, image)
                if (response.isSuccessful) {
                    _productResponse.postValue(response.body())
                    // Handle success here if needed
                } else {
                    // Handle failure
                    Log.e("AuthViewModel", "Failed to add product: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                // Handle exception
                Log.e("AuthViewModel", "Exception occurred: ${e.message}", e)
            }
        }
    }

    fun getProducts(token: String) {
        viewModelScope.launch {
            try {
                val response = repository.getProducts(token)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _products.postValue(it)
                    }
                } else {
                    // Handle failure
                }
            } catch (e: Exception) {
                // Handle exception
            }
        }
    }

    private fun getTokenFromPreferences(): String {
        val sharedPrefs = getApplication<Application>().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPrefs.getString("token", "") ?: ""
    }

    enum class Status {
        LOADING, SUCCESS, ERROR
    }

    private fun saveToken(token: String) {
        val sharedPrefs = getApplication<Application>().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("token", token).apply()
    }
}
