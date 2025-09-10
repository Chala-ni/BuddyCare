package com.example.buddycare

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.buddycare.classes.OrderItem
import com.example.buddycare.classes.Product
import kotlinx.coroutines.*

class BillItemArray(private val orderItemsList: List<OrderItem>, private val db: AppDatabase) :
    RecyclerView.Adapter<BillItemArray.BillItemViewHolder>() {

    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.bill_item, parent, false)
        return BillItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: BillItemViewHolder, position: Int) {
        val orderItem = orderItemsList[position]

        // Fetch product from database using coroutine
        CoroutineScope(Dispatchers.IO).launch {
            val product = db.productDao().getProductById(orderItem.productId)
            withContext(Dispatchers.Main) {
                if (product != null) {
                    holder.itemIdTextView.text = product.id.toString()
                    holder.itemNameTextView.text = product.name
                    holder.itemAmountTextView.text = String.format("$%.2f", product.price)
                    holder.itemQuantityTextView.text = "x${orderItem.quantity}"
                    holder.itemTotalTextView.text = String.format("$%.2f", orderItem.subtotal)
                } else {
                    // Handle the case where the product is not found (maybe set some default text)
                    holder.itemIdTextView.text = "N/A"
                    holder.itemNameTextView.text = "Product Not Found"
                    holder.itemAmountTextView.text = "N/A"
                    holder.itemQuantityTextView.text = "N/A"
                    holder.itemTotalTextView.text = "N/A"
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return orderItemsList.size
    }

    class BillItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemIdTextView: TextView = itemView.findViewById(R.id.item_id)
        val itemNameTextView: TextView = itemView.findViewById(R.id.item_name)
        val itemAmountTextView: TextView = itemView.findViewById(R.id.item_amount)
        val itemQuantityTextView: TextView = itemView.findViewById(R.id.item_qty)
        val itemTotalTextView: TextView = itemView.findViewById(R.id.item_total)
    }
}