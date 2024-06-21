import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.capstonui.R
import com.dicoding.capstonui.adapter.ProductAdapter
import com.dicoding.capstonui.model.Product
import com.dicoding.capstonui.network.ApiService
import com.dicoding.capstonui.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class UploadedProductsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_uploaded_products, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ProductAdapter(object : ProductAdapter.OnItemClickListener {
            override fun onItemClick(product: Product) {
                // Tindakan ketika item diklik, misalnya navigasi ke detail produk
            }
        })
        recyclerView.adapter = adapter

        fetchUploadedProducts()

        return view
    }

    private fun fetchUploadedProducts() {
        val token = getToken()
        val apiService = RetrofitClient.create(token)

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getUploadedProducts(token)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val products = response.body()
                        products?.let {
                            adapter.submitList(it)
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to fetch uploaded products", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getToken(): String {
        val sharedPrefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPrefs.getString("token", "") ?: ""
    }
}
