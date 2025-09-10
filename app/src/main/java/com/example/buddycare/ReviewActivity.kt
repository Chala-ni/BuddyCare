package com.example.buddycare

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import kotlinx.coroutines.*
import com.example.buddycare.classes.Product

class ReviewActivity : AppCompatActivity() {
    private lateinit var reviewRecyclerView: RecyclerView
    private lateinit var reviewArray: ReviewArray
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        reviewRecyclerView = findViewById(R.id.reviewRecyclerView)
        reviewRecyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize database
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "buddy-nutrition-db")
            .allowMainThreadQueries()
            .build()

        // Fetch products from database using coroutine
        CoroutineScope(Dispatchers.IO).launch {
            val products: List<Product> = db.productDao().getAllProducts()
            withContext(Dispatchers.Main) {
                // Pass the ReviewDao instance to the adapter
                reviewArray = ReviewArray(products as MutableList<Product>, this@ReviewActivity, db.reviewDao())
                reviewRecyclerView.adapter = reviewArray
            }
        }
    }
}