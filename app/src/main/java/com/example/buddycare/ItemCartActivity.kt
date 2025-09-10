package com.example.buddycare

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.buddycare.classes.CartItem
import com.example.buddycare.classes.Order
import com.example.buddycare.classes.OrderItem
import com.example.buddycare.interfaces.OrderDao
import com.example.buddycare.interfaces.OrderItemDao
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class ItemCartActivity : AppCompatActivity(), ItemCartArray.CartUpdateListener {

    private lateinit var db: AppDatabase
    private lateinit var adapter: ItemCartArray
    private lateinit var cartTotalTextView: TextView
    private lateinit var sharedPreferences: SharedPreferences

    @SuppressLint("DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_cart)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        cartTotalTextView = findViewById(R.id.textView2)
        sharedPreferences = getSharedPreferences("user_preferences", Context.MODE_PRIVATE)

        val checkoutButton: MaterialButton = findViewById(R.id.button)
        checkoutButton.setOnClickListener { showConfirmationDialog() }

        // Initialize Database
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "buddy-nutrition-db")
            .allowMainThreadQueries()
            .build()

        // Get RecyclerView
        val cartItemList: RecyclerView = findViewById(R.id.cartItemList)

        // Set up LayoutManager
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        cartItemList.layoutManager = layoutManager

        // Set up Adapter
        adapter = ItemCartArray(this, db.productDao())
        adapter.setCartUpdateListener(this)
        cartItemList.adapter = adapter

        // Calculate and set initial total
        //CoroutineScope(Dispatchers.IO).launch {
            //val initialTotal = adapter.calculateCartTotal()
            //withContext(Dispatchers.Main) {
                //cartTotalTextView.text = String.format("$ %.2f", initialTotal)
            //}
        //}
    }

    @SuppressLint("DefaultLocale")
    override fun onCartUpdated(cartTotal: Double) {
        cartTotalTextView.text = String.format("$ %.2f", cartTotal)
    }

    private fun showConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Confirm Checkout")
            .setMessage("Are you sure you want to proceed with the checkout?")
            .setPositiveButton("Proceed") { _, _ -> processCheckout() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun processCheckout() {
        CoroutineScope(Dispatchers.IO).launch {
            val userId = sharedPreferences.getInt("userId", 0)

            // 2. Create a new Order
            val newOrder = Order()
            newOrder.userId = userId
            newOrder.orderDate = getCurrentDate()
            newOrder.totalPrice = adapter.calculateCartTotal()

            // 3. Insert Order into the database
            val orderDao: OrderDao = db.orderDao()
            val orderId = orderDao.insertOrder(newOrder) // Get generated order ID

            // 4. Insert Order Items
            val orderItemDao: OrderItemDao = db.orderItemDao()
            val cartItems = adapter.getCartItems() // Get cart items from adapter
            for (cartItem in cartItems) {
                val orderItem = OrderItem()
                orderItem.orderId = orderId.toInt()
                orderItem.productId = cartItem.product.id
                orderItem.quantity = cartItem.quantity
                orderItem.subtotal = cartItem.product.price * cartItem.quantity
                orderItemDao.insertOrderItem(orderItem)
            }

            // 5. Clear the cart (from SharedPreferences and adapter)
            withContext(Dispatchers.Main) {
                clearCart()
            }

            // 6. Show success message
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ItemCartActivity, "Order placed successfully!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@ItemCartActivity, BillActivity::class.java)
                startActivity(intent)
                finish() // Optional: Close CartActivity after successful checkout
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged", "DefaultLocale")
    private fun clearCart() {
        val sharedPreferences = getSharedPreferences("cart", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
        adapter.clearCartItems() // Assuming you add a clearCartItems() method to your adapter
        adapter.notifyDataSetChanged()

        // Update cart total
        cartTotalTextView.text = String.format("$ %.2f", 0.00)
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }
}