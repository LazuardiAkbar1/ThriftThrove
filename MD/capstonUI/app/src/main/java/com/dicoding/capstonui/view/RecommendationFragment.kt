package com.dicoding.capstonui.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.capstonui.R
import com.dicoding.capstonui.adapter.ProductAdapter
import com.dicoding.capstonui.model.Product
import com.dicoding.capstonui.network.RetrofitClient
import com.dicoding.capstonui.product.ProductDetailActivity
import com.dicoding.capstonui.utils.Preference
import com.dicoding.capstonui.viewmodel.AuthRepository
import com.dicoding.capstonui.viewmodel.AuthViewModel
import com.dicoding.capstonui.viewmodel.AuthViewModelFactory

class RecommendationFragment : Fragment(), ProductAdapter.OnItemClickListener {

    private lateinit var viewModel: AuthViewModel
    private lateinit var productAdapter: ProductAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    override fun onItemClick(product: Product) {
        val intent = Intent(activity, ProductDetailActivity::class.java)
        intent.putExtra("product", product)
        startActivity(intent)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recommendation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        progressBar = view.findViewById(R.id.progressBar)

        // Configure RecyclerView with GridLayoutManager for two columns
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        productAdapter = ProductAdapter(this)
        recyclerView.adapter = productAdapter

        // Initialize ViewModel and fetch products
        val repository =
            AuthRepository(RetrofitClient.create(Preference.getToken(requireContext())))
        val factory = AuthViewModelFactory(repository, requireActivity().application)
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)

        val token = Preference.getToken(requireContext())
        viewModel.getProducts(token)

        // Observe ViewModel states and update UI accordingly
        viewModel.status.observe(viewLifecycleOwner, Observer { status ->
            when (status) {
                AuthViewModel.Status.LOADING -> progressBar.visibility = View.VISIBLE
                AuthViewModel.Status.SUCCESS -> progressBar.visibility = View.GONE
                AuthViewModel.Status.ERROR -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(activity, "Error in getting data", Toast.LENGTH_SHORT).show()
                }
            }
        })

        viewModel.products.observe(viewLifecycleOwner, Observer { products ->
            products?.let {
                // Sort products by price before submitting to the adapter
                val sortedProducts = sortProductsByPrice(it)
                productAdapter.submitList(sortedProducts)
            } ?: run {
                // Handle case when products is null or empty
                Toast.makeText(activity, "No products available", Toast.LENGTH_SHORT).show()
            }
        })

    }

    // Function to sort products by price
    private fun sortProductsByPrice(products: List<Product>): List<Product> {
        return products.sortedBy { it.price }
    }
}
