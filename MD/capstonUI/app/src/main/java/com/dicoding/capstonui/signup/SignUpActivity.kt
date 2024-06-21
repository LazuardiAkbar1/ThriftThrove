package com.dicoding.capstonui.signup

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dicoding.capstonui.R
import com.dicoding.capstonui.login.LoginActivity
import com.dicoding.capstonui.network.ApiService
import com.dicoding.capstonui.network.RetrofitClient
import com.dicoding.capstonui.viewmodel.AuthRepository
import com.dicoding.capstonui.viewmodel.AuthViewModel
import com.dicoding.capstonui.viewmodel.AuthViewModelFactory

class SignUpActivity : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Initialize ViewModel
        val repository = AuthRepository(RetrofitClient.create(""))
        val factory = AuthViewModelFactory(repository, application)
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)

        // Bind UI elements
        val nameEt = findViewById<EditText>(R.id.nameEt_signUpPage)
        val emailEt = findViewById<EditText>(R.id.emailEt_signUpPage)
        val passEt = findViewById<EditText>(R.id.PassEt_signUpPage)
        val cPassEt = findViewById<EditText>(R.id.cPassEt_signUpPage)
        val signUpBtn = findViewById<Button>(R.id.signUpBtn_signUpPage)

        // Handle sign up button click
        signUpBtn.setOnClickListener {
            val name = nameEt.text.toString()
            val email = emailEt.text.toString()
            val password = passEt.text.toString()
            val confirmPassword = cPassEt.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && password == confirmPassword) {
                viewModel.signUp(name, email, password)
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe signUpResponse LiveData for API response
        viewModel.signUpResponse.observe(this) { response ->
            if (response.auth) {
                Toast.makeText(this, "Sign up successful, please log in", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Sign up failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
