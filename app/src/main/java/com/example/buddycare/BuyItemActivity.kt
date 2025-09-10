package com.example.buddycare

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher

import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.buddycare.classes.Product
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*

class BuyItemActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var adapter: ProductArray
    private lateinit var allProducts: List<Product> // Store all products for filtering
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_customer_product)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Database
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "buddy-nutrition-db")
            .build()

        // Get the RecyclerView
        val productList = findViewById<RecyclerView>(R.id.productList)

        // Set up the LayoutManager
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        productList.layoutManager = layoutManager

        // Fetch all products initially
        coroutineScope.launch {
            allProducts = db.productDao().getAllProducts()
            adapter = ProductArray(this@BuyItemActivity,
                allProducts as MutableList<Product>, db.productDao(), true, null)
            productList.adapter = adapter
        }

        // Search functionality
        val searchBar = findViewById<TextInputEditText>(R.id.search_bar)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not used in this implementation
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let { filterProducts(it.toString()) }
            }

            override fun afterTextChanged(s: Editable?) {
                // Not used in this implementation
            }
        })
    }

    private fun filterProducts(query: String) {
        coroutineScope.launch {
            val filteredList: List<Product> = if (query.isEmpty()) {
                // Show all products if the query is empty
                db.productDao().getAllProducts()
            } else {
                // Filter based on product name (case-insensitive)
                db.productDao().getProductsByName(query.toLowerCase())
            }

            // Update the adapter with the filtered OR complete list
            adapter.updateProducts(filteredList)
        }
    }

    override fun onDestroy() {
        coroutineScope.cancel()
        super.onDestroy()
    }
}