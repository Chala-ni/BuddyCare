package com.example.buddycare

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.buddycare.classes.Product
import com.example.buddycare.interfaces.ProductDao
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ProductActivity : AppCompatActivity(), ProductArray.ProductEditListener {

    private lateinit var db: AppDatabase
    private lateinit var productDao: ProductDao
    private lateinit var adapter: ProductArray
    private lateinit var productList: RecyclerView
    private lateinit var productNameEditText: EditText
    private lateinit var productDescriptionEditText: EditText
    private lateinit var productPriceEditText: EditText
    private lateinit var productBrandEditText: EditText
    private lateinit var productTypeSpinner: Spinner
    private lateinit var productAgeRangeSpinner: Spinner
    private lateinit var addProductButton: Button
    private lateinit var productImageView: ImageView
    private lateinit var productTypes: Array<String>
    private lateinit var productAgeRanges: Array<String>
    private var currentPhotoPath: String? = null
    private var isUserCustomer = false
    private var isUpdatingProduct = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product)

        // Initialize Database
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "buddy-nutrition-db")
            .allowMainThreadQueries()
            .build()
        productDao = db.productDao()

        // Initialize Views
        productNameEditText = findViewById(R.id.productNameEditText)
        productDescriptionEditText = findViewById(R.id.productDescriptionEditText)
        productPriceEditText = findViewById(R.id.productPriceEditText)
        productBrandEditText = findViewById(R.id.productBrandEditText)
        productTypeSpinner = findViewById(R.id.productTypeSpinner)
        productAgeRangeSpinner = findViewById(R.id.productAgeRangeSpinner)
        addProductButton = findViewById(R.id.addProductButton)
        productList = findViewById(R.id.productList)
        productImageView = findViewById(R.id.productImageView)

        val productForm = findViewById<LinearLayout>(R.id.productForm)
        productForm.visibility = View.GONE

        productTypes = arrayOf("Food", "Treats", "Toys", "Accessories", "Supplements", "Grooming")
        val productTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, productTypes)
        productTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        productTypeSpinner.adapter = productTypeAdapter

        productAgeRanges = arrayOf("Puppy", "Adult", "Senior", "All Life Stages")
        val productAgeRangeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, productAgeRanges)
        productAgeRangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        productAgeRangeSpinner.adapter = productAgeRangeAdapter

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        productList.layoutManager = layoutManager
        // Set up Product List Adapter
        CoroutineScope(Dispatchers.IO).launch {
            val products = productDao.getAllProducts()
            withContext(Dispatchers.Main) {
                adapter = ProductArray(this@ProductActivity,
                    products as MutableList<Product>, productDao, isUserCustomer, this@ProductActivity)
                productList.adapter = adapter
            }
        }

        // Add Product Button Click Listener
        addProductButton.setOnClickListener {
            if (!isUpdatingProduct) {
                CoroutineScope(Dispatchers.IO).launch {
                    addProduct()
                }
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    updateProduct()
                }
            }
        }

        val addNewItemButton = findViewById<Button>(R.id.addNewItemButton)
        addNewItemButton.setOnClickListener {
            val productForm = findViewById<LinearLayout>(R.id.productForm)
            val bottomLayout = findViewById<LinearLayout>(R.id.bottomLayout)

            if (productForm.visibility == View.GONE) {
                productForm.visibility = View.VISIBLE
                bottomLayout.visibility = View.GONE
                addNewItemButton.text = "Back to List"
            } else {
                productForm.visibility = View.GONE
                bottomLayout.visibility = View.VISIBLE
                addNewItemButton.text = "Add New Item"
            }
        }

        // ImageView Click Listener to open gallery
        productImageView.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
        }
    }

    private suspend fun addProduct() {
        withContext(Dispatchers.Main) {
            val name = productNameEditText.text.toString()
            val description = productDescriptionEditText.text.toString()
            val priceString = productPriceEditText.text.toString()
            val brand = productBrandEditText.text.toString()
            val type = productTypeSpinner.selectedItem.toString()
            val ageRange = productAgeRangeSpinner.selectedItemPosition

            if (name.isEmpty() || description.isEmpty() || priceString.isEmpty() || brand.isEmpty()) {
                Toast.makeText(this@ProductActivity, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@withContext
            }

            var price: Double
            try {
                price = priceString.toDouble()
            } catch (e: NumberFormatException) {
                Toast.makeText(this@ProductActivity, "Invalid price format", Toast.LENGTH_SHORT).show()
                return@withContext
            }

            val newProduct = Product()
            newProduct.name = name
            newProduct.description = description
            newProduct.price = price
            newProduct.brand = brand
            newProduct.type = type
            newProduct.ageRange = ageRange
            newProduct.imageUrl = currentPhotoPath.toString()

            productDao.insertProduct(newProduct)
            refreshProductList()

            clearInputFields()
            Toast.makeText(this@ProductActivity, "Product added successfully!", Toast.LENGTH_SHORT).show()
            val productForm = findViewById<LinearLayout>(R.id.productForm)
            val bottomLayout = findViewById<LinearLayout>(R.id.bottomLayout)

            productForm.visibility = View.GONE
            bottomLayout.visibility = View.VISIBLE
        }
    }

    private suspend fun updateProduct() {
        withContext(Dispatchers.Main) {
            val productId = intent.getIntExtra("productId", -1)
            if (productId == -1) {
                Toast.makeText(this@ProductActivity, "Invalid product ID", Toast.LENGTH_SHORT).show()
                return@withContext
            }

            val name = productNameEditText.text.toString()
            val description = productDescriptionEditText.text.toString()
            val price = productPriceEditText.text.toString().toDouble()
            val brand = productBrandEditText.text.toString()
            val type = productTypeSpinner.selectedItem.toString()
            val ageRange = productAgeRangeSpinner.selectedItemPosition

            val existingProduct = productDao.getProductById(productId)

            val updatedProduct = Product()
            updatedProduct.id = productId
            updatedProduct.name = name
            updatedProduct.description = description
            updatedProduct.price = price
            updatedProduct.brand = brand
            updatedProduct.type = type
            updatedProduct.ageRange = ageRange
            if (currentPhotoPath != null && currentPhotoPath!!.isNotEmpty()) {
                updatedProduct.imageUrl = currentPhotoPath.toString()
            } else {
                updatedProduct.imageUrl = existingProduct.imageUrl
            }

            productDao.updateProduct(updatedProduct)
            refreshProductList()

            addProductButton.text = "Add Product"
            isUpdatingProduct = false

            clearInputFields()
            Toast.makeText(this@ProductActivity, "Product updated successfully", Toast.LENGTH_SHORT).show()
            val productForm = findViewById<LinearLayout>(R.id.productForm)
            val bottomLayout = findViewById<LinearLayout>(R.id.bottomLayout)

            productForm.visibility = View.GONE
            bottomLayout.visibility = View.VISIBLE
        }
    }

    private fun clearInputFields() {
        productNameEditText.text.clear()
        productDescriptionEditText.text.clear()
        productPriceEditText.text.clear()
        productBrandEditText.text.clear()
        productTypeSpinner.setSelection(0)
        productAgeRangeSpinner.setSelection(0)
        productImageView.setImageResource(R.drawable.ic_product)
        currentPhotoPath = null
    }

    private suspend fun refreshProductList() {
        val updatedProducts = productDao.getAllProducts()
        adapter.updateProducts(updatedProducts)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val selectedImageUri = data.data

                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                    productImageView.setImageBitmap(bitmap)

                    CoroutineScope(Dispatchers.IO).launch {
                        currentPhotoPath = saveImageToStorage(bitmap)
                        Log.d("ProductActivity", "Image saved to: $currentPhotoPath")
                    }

                } catch (e: IOException) {
                    Log.e("ProductActivity", "Error processing image", e)
                    Toast.makeText(this, "Error selecting image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun saveImageToStorage(bitmap: Bitmap): String? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_$timeStamp"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        try {
            val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)

            val fos = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()

            return imageFile.absolutePath

        } catch (e: IOException) {
            Log.e("ProductActivity", "Error saving image to file", e)
            return null
        }
    }

    override fun onEditProduct(product: Product) {
        productNameEditText.setText(product.name)
        productDescriptionEditText.setText(product.description)
        productPriceEditText.setText(product.price.toString())
        productBrandEditText.setText(product.brand)

        val productForm = findViewById<LinearLayout>(R.id.productForm)
        val bottomLayout = findViewById<LinearLayout>(R.id.bottomLayout)

        productForm.visibility = View.VISIBLE
        bottomLayout.visibility = View.GONE

        val typePosition = (productTypeSpinner.adapter as ArrayAdapter<String>).getPosition(product.type)
        productTypeSpinner.setSelection(typePosition)
        productAgeRangeSpinner.setSelection(product.ageRange)

        addProductButton.text = "Update Product"
        isUpdatingProduct = true

        intent.putExtra("productId", product.id)

        if (product.imageUrl != null && product.imageUrl.isNotEmpty()) {
            val bitmap = BitmapFactory.decodeFile(product.imageUrl)
            productImageView.setImageBitmap(bitmap)

            currentPhotoPath = product.imageUrl
        } else {
            productImageView.setImageResource(R.drawable.ic_user)
        }
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
}