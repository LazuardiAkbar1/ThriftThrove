package com.dicoding.capstonui.product

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.dicoding.capstonui.MainActivity
import com.dicoding.capstonui.R
import com.dicoding.capstonui.model.Product
import com.dicoding.capstonui.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var product: Product

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        // Retrieve product from intent extras
        product = intent.getParcelableExtra("product") ?: run {
            Toast.makeText(this, "Product data is missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Check if description is null before using it
        val description = product.description ?: "Description not available"

        // Setup views with product data
        setupViews()


    }

    private fun setupViews() {
        findViewById<TextView>(R.id.tvNameProduct).text = product.name ?: "Name not available"
        findViewById<TextView>(R.id.tvDescriptionProduct).text = product.description ?: "Description not available"
        findViewById<TextView>(R.id.tvPriceProductDetails).text = "Rp.${product.price}"
        findViewById<TextView>(R.id.tvUserName).text = product.username ?: "Username not available"
        findViewById<TextView>(R.id.tvUserEmail).text=product.email ?: "Email not available"

        Glide.with(this)
            .load(product.image)
            .into(findViewById(R.id.ivProductDetails))

        // Setup other views as needed

        // Setup click listener for the back button
        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            navigateBackToExplore()
        }

        findViewById<Button>(R.id.btnRazorpay).setOnClickListener {
            Toast.makeText(this, "Buy button clicked!", Toast.LENGTH_SHORT).show()
            addToCart()
        }

        // Setup other click listeners as needed
    }


    private fun addToCart() {
        val token = getToken()
        if (token.isEmpty()) {
            runOnUiThread {
                Toast.makeText(this@ProductDetailActivity, "Token is missing", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val apiService = RetrofitClient.create(token)

        lifecycleScope.launch {
            try {
                val response = apiService.addItemToCart(
                    itemId = product.id,
                    quantity = 1 // Default quantity, adjust as needed
                )
                Log.d("ProductDetailActivity", "API response: $response")
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@ProductDetailActivity,
                        "Product added to cart!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@ProductDetailActivity,
                        "Failed to add product to cart: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("ProductDetailActivity", "API error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@ProductDetailActivity,
                    "An error occurred: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("ProductDetailActivity", "Exception: ", e)
            }
        }
    }

    private fun getToken(): String {
        // Replace with your actual implementation to retrieve token
        val sharedPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPrefs.getString("token", "") ?: ""
    }

    private fun navigateBackToExplore() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("openFragment", "explore")
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigateBackToExplore()
    }
}
