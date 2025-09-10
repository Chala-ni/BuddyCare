package com.example.buddycare

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.buddycare.classes.Order
import java.util.*

class BillActivity : AppCompatActivity() {
    private lateinit var orderRecyclerView: RecyclerView
    private lateinit var orderAdapter: BillArray
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var db: AppDatabase // Database instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)

        // Correctly initialize AppDatabase
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "buddy-nutrition-db")
            .allowMainThreadQueries() // Only for simple apps, NOT recommended for production
            .build()

        orderRecyclerView = findViewById(R.id.orderRecyclerView)
        orderRecyclerView.layoutManager = LinearLayoutManager(this)

        sharedPreferences = getSharedPreferences("user_preferences", MODE_PRIVATE)
        val userType = sharedPreferences.getString("usertype", "")

        if (userType == "admin") {
            val orders: List<Order> = db.orderDao().getAllOrders() as List<Order>
            // Initialize the adapter and pass the database instance
            orderAdapter = BillArray(this, orders, db)
            orderRecyclerView.adapter = orderAdapter
        } else {
            val userId = sharedPreferences.getInt("userId", 0)
            val orders: List<Order> = db.orderDao().getOrdersByUser(userId) as List<Order>
            // Initialize the adapter and pass the database instance
            orderAdapter = BillArray(this, orders, db)
            orderRecyclerView.adapter = orderAdapter
        }
    }
}