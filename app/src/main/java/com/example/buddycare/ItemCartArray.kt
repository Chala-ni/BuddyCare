package com.example.buddycare

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.buddycare.classes.CartItem
import com.example.buddycare.classes.Product
import com.example.buddycare.interfaces.ProductDao
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.*

class ItemCartArray(private val context: Context, private val productDao: ProductDao) :
    RecyclerView.Adapter<ItemCartArray.ViewHolder>() {

    private var cartItems: MutableList<CartItem> = mutableListOf()
    private var sharedPreferences: SharedPreferences =
        context.getSharedPreferences("cart", Context.MODE_PRIVATE)
    private var cartUpdateListener: CartUpdateListener? = null

    interface CartUpdateListener {
        fun onCartUpdated(cartTotal: Double)
    }
    init {
        loadCartItemsFromSharedPrefs();
    }

    fun setCartUpdateListener(listener: CartUpdateListener) {
        cartUpdateListener = listener
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImageView: ImageView = itemView.findViewById(R.id.productImageView)
        val productNameTextView: TextView = itemView.findViewById(R.id.productNameTextView)
        val productDetailsTextView: TextView = itemView.findViewById(R.id.productDetailsTextView)
        val productPriceTextView: TextView = itemView.findViewById(R.id.productPriceTextView)
        val itemTotalTextView: TextView = itemView.findViewById(R.id.itemTotal)
        val qtyTextView: TextView = itemView.findViewById(R.id.qtyTextView)
        val minusButton: ImageButton = itemView.findViewById(R.id.minusButton)
        val plusButton: ImageButton = itemView.findViewById(R.id.plusButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val listItemView = LayoutInflater.from(context).inflate(R.layout.cart, parent, false)
        return ViewHolder(listItemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = cartItems[position]
        val currentProduct = currentItem.product

        val bitmap = getBitmapFromUrl(currentProduct.imageUrl)
        holder.productImageView.setImageBitmap(bitmap)
        holder.productNameTextView.text = currentProduct.name
        holder.productDetailsTextView.text = currentProduct.description
        holder.productPriceTextView.text = "$" + String.format("%.2f", currentProduct.price)
        holder.qtyTextView.text = currentItem.quantity.toString()

        val itemTotal = currentProduct.price * currentItem.quantity
        holder.itemTotalTextView.text = "$" + String.format("%.2f", itemTotal)

        holder.minusButton.setOnClickListener {
            adjustQuantity(position, -1)
        }

        holder.plusButton.setOnClickListener {
            adjustQuantity(position, 1)
        }
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }

    private fun loadCartItemsFromSharedPrefs() {
        cartItems.clear()

        val cartString = sharedPreferences.getString("cart", null)
        if (cartString != null) {
            val cartProducts = deserializeCart(cartString)

            CoroutineScope(Dispatchers.IO).launch {
                for ((key, value) in cartProducts) {
                    val productId = key.toInt()
                    val quantity = value.toInt()

                    val product = getProductById(productId)

                    if (product != null) {
                        val cartItem = CartItem(product, quantity)
                        cartItems.add(cartItem)
                    }
                }
                withContext(Dispatchers.Main) {
                    notifyDataSetChanged()
                    cartUpdateListener?.onCartUpdated(calculateCartTotal())
                }
            }
        }
    }

    private suspend fun getProductById(productId: Int): Product? {
        return withContext(Dispatchers.IO) {
            productDao.getProductById(productId)
        }
    }

    private fun deserializeCart(cartString: String): HashMap<String, String> {
        val cartProducts = HashMap<String, String>()
        val products = cartString.split(",")
        for (product in products) {
            val keyValue = product.split(":")
            if (keyValue.size == 2) {
                cartProducts[keyValue[0]] = keyValue[1]
            }
        }
        return cartProducts
    }

    private fun serializeCart(cartProducts: HashMap<String, String>): String {
        val cartString = StringBuilder()
        for ((key, value) in cartProducts) {
            cartString.append(key).append(":").append(value).append(",")
        }
        if (cartString.length > 0 && cartString[cartString.length - 1] == ',') {
            cartString.deleteCharAt(cartString.length - 1)
        }
        return cartString.toString()
    }

    private fun getBitmapFromUrl(url: String): Bitmap? {
        try {
            val file = File(url)
            return BitmapFactory.decodeStream(FileInputStream(file))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return null
        }
    }

    private fun removeItem(position: Int) {
        cartItems.removeAt(position)
        notifyItemRemoved(position)
        saveCartToSharedPrefs()

        cartUpdateListener?.onCartUpdated(calculateCartTotal())
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun adjustQuantity(position: Int, quantityChange: Int) {
        val cartItem = cartItems[position]
        val newQuantity = cartItem.quantity + quantityChange

        if (newQuantity <= 0) {
            removeItem(position)
        } else {
            cartItem.quantity = newQuantity
            notifyDataSetChanged()
            saveCartToSharedPrefs()
        }

        cartUpdateListener?.onCartUpdated(calculateCartTotal())
    }

    private fun saveCartToSharedPrefs() {
        val cartProducts = HashMap<String, String>()
        for (cartItem in cartItems) {
            cartProducts[cartItem.product.id.toString()] = cartItem.quantity.toString()
        }

        val editor = sharedPreferences.edit()
        editor.putString("cart", serializeCart(cartProducts))
        editor.apply()
    }

    fun calculateCartTotal(): Double {
        var total = 0.0
        for (item in cartItems) {
            total += item.product.price * item.quantity
        }
        return total
    }

    fun getCartItems(): List<CartItem> {
        return cartItems
    }

    fun clearCartItems() {
        cartItems.clear()
    }
}