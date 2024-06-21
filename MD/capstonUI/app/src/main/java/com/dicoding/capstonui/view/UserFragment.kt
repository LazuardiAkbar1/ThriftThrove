package com.dicoding.capstonui.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.dicoding.capstonui.MainActivity
import com.dicoding.capstonui.R
import com.dicoding.capstonui.login.LoginActivity
import com.dicoding.capstonui.network.RetrofitClient
import com.dicoding.capstonui.welcome.WelcomeActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class UserFragment : Fragment() {

    private lateinit var ivUser: ImageView
    private lateinit var tvUser: TextView
    private lateinit var tvEmail: TextView
    private lateinit var btnSignOut: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user, container, false)

        ivUser = view.findViewById(R.id.ivUser)
        tvUser = view.findViewById(R.id.tvUser)
        tvEmail = view.findViewById(R.id.tvEmail)
        btnSignOut = view.findViewById(R.id.btnSignOut)

        // Fetch user profile data
        fetchUserProfile()

        val ivBack: ImageView = view.findViewById(R.id.ivBack)
        ivBack.setOnClickListener {
            navigateToMainActivity()
        }

        // Handle sign out button click
        btnSignOut.setOnClickListener {
            signOut()
        }

        return view
    }

    private fun fetchUserProfile() {
        val token = getToken()// get token from SharedPreferences or wherever it's stored
        val apiService = RetrofitClient.create(token)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getUserProfile("Bearer $token")
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val userResponse = response.body()
                        userResponse?.let {
                            tvUser.text = it.username
                            tvEmail.text = it.email
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to fetch user profile", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getToken(): String {
        val sharedPrefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPrefs.getString("token", "") ?: ""
    }


    private fun signOut() {
        val sharedPrefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.clear()
        editor.apply()

        val intent = Intent(activity, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun navigateToMainActivity() {
        val intent = Intent(requireActivity(), MainActivity::class.java)
        startActivity(intent)
    }
}

