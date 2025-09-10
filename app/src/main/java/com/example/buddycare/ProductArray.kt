package com.example.buddycare

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.buddycare.classes.Product
import com.example.buddycare.interfaces.ProductDao
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

class ProductArray(
    private val context: Context,
    private val products: MutableList<Product>,
    private val productDao: ProductDao,
    private val isUserCustomer: Boolean,
    private val editListener: ProductEditListener?
) : RecyclerView.Adapter<ProductArray.ViewHolder>() {

    interface ProductEditListener {
        fun onEditProduct(product: Product)
    }

    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val listItemView = LayoutInflater.from(context).inflate(R.layout.product, parent, false)
        return ViewHolder(listItemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentProduct = products[position]

        val bitmap = getBitmapFromUrl(currentProduct.imageUrl)
        holder.productImageView.setImageBitmap(bitmap)
        holder.productNameTextView.text = currentProduct.name
        holder.productDetailsTextView.text = currentProduct.description
        holder.productPriceTextView.text = "$" + String.format("%.2f", currentProduct.price)

        if (isUserCustomer) {
            holder.addToCartButton.visibility = View.VISIBLE
            holder.editButton.visibility = View.GONE
            holder.deleteButton.visibility = View.GONE

            holder.addToCartButton.setOnClickListener {
                val sharedPreferences = context.getSharedPreferences("cart", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()

                val cartProducts = loadCartFromSharedPrefs(sharedPreferences)

                val currentProductId = currentProduct.id.toString()
                var qty = cartProducts[currentProductId] ?: "0"
                qty = (qty.toInt() + 1).toString()
                cartProducts[currentProductId] = qty

                saveCartToSharedPrefs(editor, cartProducts)

                Toast.makeText(context, "Product added to cart", Toast.LENGTH_SHORT).show()
                Log.d("cart", cartProducts.toString())
            }
        } else {
            holder.editButton.visibility = View.VISIBLE
            holder.deleteButton.visibility = View.VISIBLE
            holder.addToCartButton.visibility = View.GONE

            holder.editButton.setOnClickListener {
                if (editListener != null) {
                    editListener.onEditProduct(currentProduct)
                }
            }

            holder.deleteButton.setOnClickListener {
                AlertDialog.Builder(context)
                    .setTitle("Delete Product")
                    .setMessage("Are you want to delete this product?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes) { _, _ ->
                        CoroutineScope(Dispatchers.IO).launch {
                            deleteProduct(currentProduct)
                        }
                    }
                    .setNegativeButton(android.R.string.no, null)
                    .show()
            }
        }
    }

    override fun getItemCount(): Int {
        return products.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImageView: ImageView = itemView.findViewById(R.id.productImageView)
        val productNameTextView: TextView = itemView.findViewById(R.id.productNameTextView)
        val productDetailsTextView: TextView = itemView.findViewById(R.id.productDetailsTextView)
        val productPriceTextView: TextView = itemView.findViewById(R.id.productPriceTextView)
        val editButton: Button = itemView.findViewById(R.id.editButton)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
        val addToCartButton: Button = itemView.findViewById(R.id.addToCartButton)
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

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun deleteProduct(product: Product) {
        withContext(Dispatchers.IO) {
            productDao.deleteProduct(product)
        }
        withContext(Dispatchers.Main) {
            val index = products.indexOf(product)
            if (index != -1) {
                products.removeAt(index)
                notifyDataSetChanged()
                Toast.makeText(context, "Product Deleted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun updateProducts(newProducts: List<Product>) {
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }

    private fun loadCartFromSharedPrefs(sharedPreferences: SharedPreferences): MutableMap<String, String> {
        val cartProducts = mutableMapOf<String, String>()
        val cartString = sharedPreferences.getString("cart", null)
        if (cartString != null) {
            cartProducts.putAll(deserializeCart(cartString))
        }
        return cartProducts
    }

    private fun saveCartToSharedPrefs(editor: SharedPreferences.Editor, cartProducts: MutableMap<String, String>) {
        val cartStringToSave = serializeCart(cartProducts)
        editor.putString("cart", cartStringToSave)
        editor.apply()
    }

    private fun deserializeCart(cartString: String): Map<String, String> {
        val cartProducts = mutableMapOf<String, String>()
        val products = cartString.split(",")
        for (product in products) {
            val keyValue = product.split(":")
            if (keyValue.size == 2) {
                cartProducts[keyValue[0]] = keyValue[1]
            }
        }
        return cartProducts
    }

    private fun serializeCart(cartProducts: MutableMap<String, String>): String {
        val cartString = StringBuilder()
        for ((key, value) in cartProducts) {
            cartString.append(key).append(":").append(value).append(",")
        }
        if (cartString.length > 0 && cartString[cartString.length - 1] == ',') {
            cartString.deleteCharAt(cartString.length - 1)
        }
        return cartString.toString()
    }
}