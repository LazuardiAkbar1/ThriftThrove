package com.dicoding.capstonui.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.capstonui.MainActivity
import com.dicoding.capstonui.R
import com.dicoding.capstonui.adapter.CartAdapter
import com.dicoding.capstonui.model.CartItem
import com.dicoding.capstonui.network.RetrofitClient
import com.dicoding.capstonui.product.CheckoutActivity
import kotlinx.coroutines.launch

class CartFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private lateinit var emptyCartImage: ImageView
    private var cartItems: MutableList<CartItem> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cart, container, false)

        recyclerView = view.findViewById(R.id.recycler_view_cart)
        emptyCartImage = view.findViewById(R.id.empty_cart_image)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        fetchCartItems()

        setupCheckoutButton(view)

        val ivBack: ImageView = view.findViewById(R.id.ivBack)
        ivBack.setOnClickListener {
            navigateToMainActivity()
        }

        return view
    }

    private fun fetchCartItems() {
        val token = getToken()
        val apiService = RetrofitClient.create(token)

        lifecycleScope.launch {
            try {
                val response = apiService.getCart()
                if (response.isSuccessful) {
                    cartItems.clear()
                    cartItems.addAll(response.body() ?: emptyList())

                    // Set up RecyclerView with CartAdapter
                    cartAdapter = CartAdapter(cartItems)
                    recyclerView.adapter = cartAdapter

                    // Check if the cart is empty and show/hide the empty cart image
                    checkIfEmpty()

                    // Register the AdapterDataObserver
                    val adapter = recyclerView.adapter
                    adapter?.let {
                        it.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                            override fun onChanged() {
                                super.onChanged()
                                checkIfEmpty()
                            }

                            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                                super.onItemRangeInserted(positionStart, itemCount)
                                checkIfEmpty()
                            }

                            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                                super.onItemRangeRemoved(positionStart, itemCount)
                                checkIfEmpty()
                            }
                        })

                        // Initial check
                        checkIfEmpty()
                    }

                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to fetch cart items: ${response.errorBody()?.string()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("CartFragment", "Exception while fetching cart items", e)
                Toast.makeText(requireContext(), "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkIfEmpty() {
        val adapter = recyclerView.adapter
        emptyCartImage.visibility = if (adapter?.itemCount == 0) View.VISIBLE else View.GONE
    }

    private fun setupCheckoutButton(view: View) {
        val btnCheckout: Button = view.findViewById(R.id.btnCheckout)
        btnCheckout.setOnClickListener {
            navigateToCheckoutActivity()
        }
    }

    private fun navigateToCheckoutActivity() {
        val intent = Intent(requireActivity(), CheckoutActivity::class.java)
        startActivity(intent)
    }

    private fun getToken(): String {
        val sharedPrefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPrefs.getString("token", "") ?: ""
    }

    private fun navigateToMainActivity() {
        val intent = Intent(requireActivity(), MainActivity::class.java)
        startActivity(intent)
    }
}
