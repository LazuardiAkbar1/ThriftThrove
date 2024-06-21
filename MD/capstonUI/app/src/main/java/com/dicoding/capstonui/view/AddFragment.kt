package com.dicoding.capstonui.view

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dicoding.capstonui.MainActivity
import com.dicoding.capstonui.R
import com.dicoding.capstonui.network.ApiService
import com.dicoding.capstonui.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class AddFragment : Fragment() {

    private lateinit var etNameProduct: EditText
    private lateinit var etDescription: EditText
    private lateinit var etPrice: EditText
    private lateinit var ivProductImagePreview: ImageView
    private lateinit var btnGallery: Button
    private lateinit var btnAddProduct: Button
    private lateinit var ivBack: ImageView

    private var selectedImageFile: File? = null

    private val REQUEST_CODE_GALLERY = 100
    private val REQUEST_PERMISSION = 101

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_add_product, container, false)

        // Initialize views
        etNameProduct = view.findViewById(R.id.etNameProduct)
        etDescription = view.findViewById(R.id.etDescription)
        etPrice = view.findViewById(R.id.etPrice)
        ivProductImagePreview = view.findViewById(R.id.ivProductImagePreview)
        btnGallery = view.findViewById(R.id.btnGallery)
        btnAddProduct = view.findViewById(R.id.btnAddProduct)
        ivBack = view.findViewById(R.id.ivBack)

        // Set click listeners
        btnGallery.setOnClickListener { openGallery() }
        btnAddProduct.setOnClickListener { addProduct() }
        ivBack.setOnClickListener { navigateToMainActivity() }

        return view
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_GALLERY)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permission Denied: You cannot access gallery without permission",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                ivProductImagePreview.setImageURI(uri)
                selectedImageFile = uriToFile(uri)
            }
        }
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
        val file = File(requireContext().cacheDir, "selected_image.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        outputStream.close()
        inputStream?.close()
        return file
    }

    private fun addProduct() {
        val name = etNameProduct.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val price = etPrice.text.toString().trim()

        if (name.isEmpty() || description.isEmpty() || price.isEmpty() || selectedImageFile == null) {
            Toast.makeText(
                requireContext(),
                "Please fill all fields and select an image",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val token = getToken()

        val nameRequestBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
        val descriptionRequestBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val priceRequestBody = price.toRequestBody("text/plain".toMediaTypeOrNull())
        val imageRequestBody = selectedImageFile!!.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imageMultipartBody = MultipartBody.Part.createFormData("image", selectedImageFile!!.name, imageRequestBody)

        val apiService = RetrofitClient.create(token)

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.addProduct(nameRequestBody, descriptionRequestBody, priceRequestBody, imageMultipartBody)
                if (response.isSuccessful) {
                    val productId = response.body()?.id
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Product added successfully! ID: $productId", Toast.LENGTH_SHORT).show()
                        navigateToMainActivity()
                    }
                } else {
                    requireActivity().runOnUiThread {
                        val errorBody = response.errorBody()?.string()
                        Toast.makeText(requireContext(), "Failed to add product: $errorBody", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
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
