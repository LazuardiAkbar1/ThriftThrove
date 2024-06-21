package com.dicoding.capstonui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dicoding.capstonui.MainActivity
import com.dicoding.capstonui.R
import com.dicoding.capstonui.network.ApiService
import com.dicoding.capstonui.network.RetrofitClient
import com.dicoding.capstonui.signup.SignUpActivity
import com.dicoding.capstonui.viewmodel.AuthRepository
import com.dicoding.capstonui.viewmodel.AuthViewModel
import com.dicoding.capstonui.viewmodel.AuthViewModelFactory

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val repository = AuthRepository(RetrofitClient.create(""))
        val factory = AuthViewModelFactory(repository, application) // Pass application here
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)

        // Bind UI elements
        val nameEt = findViewById<EditText>(R.id.nameEt)
        val passEt = findViewById<EditText>(R.id.PassEt)
        val loginBtn = findViewById<Button>(R.id.loginBtn)
        val signUpTv = findViewById<TextView>(R.id.signUpTv)

        // Handle login button click
        loginBtn.setOnClickListener {
            val username = nameEt.text.toString()
            val password = passEt.text.toString()
            if (username.isNotEmpty() && password.isNotEmpty()) {
                viewModel.login(username, password)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle sign up text view click
        signUpTv.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        // Observe loginResponse LiveData for API response
        viewModel.loginResponse.observe(this) { response ->
            if (response.auth) {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                navigateToMainActivity()
            } else {
                Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
