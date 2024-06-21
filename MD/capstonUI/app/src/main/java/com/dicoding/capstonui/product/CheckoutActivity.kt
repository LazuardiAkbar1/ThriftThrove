package com.dicoding.capstonui.product

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dicoding.capstonui.R
import com.dicoding.capstonui.model.CheckoutRequest
import com.dicoding.capstonui.network.RetrofitClient
import kotlinx.coroutines.launch

class CheckoutActivity : AppCompatActivity() {

    private lateinit var etAddress: EditText
    private lateinit var etName: EditText  // Added for name input
    private lateinit var btnConfirmCheckout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        etAddress = findViewById(R.id.etAddress)
        etName = findViewById(R.id.etName)  // Initialize EditText for name
        btnConfirmCheckout = findViewById(R.id.btnConfirmCheckout)

        btnConfirmCheckout.setOnClickListener {
            val address = etAddress.text.toString()
            val name = etName.text.toString()  // Get name from EditText

            // Validate input
            if (address.isNotEmpty() && name.isNotEmpty()) {
                // Perform checkout with the selected address and name
                performCheckout(address, name)
            } else {
                Toast.makeText(this, "Harap isi alamat dan nama", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performCheckout(address: String, name: String) {
        // Call the API to perform the checkout
        val token = getToken()
        val apiService = RetrofitClient.create(token)

        val checkoutRequest = CheckoutRequest(
            address = address,
            name = name  // Pass name to CheckoutRequest
        )

        lifecycleScope.launch {
            try {
                val response = apiService.checkout(token, checkoutRequest)
                if (response.isSuccessful) {
                    Toast.makeText(this@CheckoutActivity, "Checkout successful!", Toast.LENGTH_SHORT).show()
                    navigateToPaymentLink()
                } else {
                    Toast.makeText(this@CheckoutActivity, "Checkout failed: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("CheckoutActivity", "Exception during checkout", e)
                Toast.makeText(this@CheckoutActivity, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getToken(): String {
        val sharedPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPrefs.getString("token", "") ?: ""
    }

    private fun navigateToPaymentLink() {
        val paymentLinkUrl = "https://app.sandbox.midtrans.com/payment-links/1718901725595"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(paymentLinkUrl))
        startActivity(intent)
    }
}