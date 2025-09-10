package com.example.buddycare

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.buddycare.classes.Order
import com.example.buddycare.classes.OrderItem
import com.example.buddycare.AppDatabase
import kotlinx.coroutines.*

class BillArray(private val context: Context, private val orderList: List<Order>, private val db: AppDatabase) :
    RecyclerView.Adapter<BillArray.OrderViewHolder>() {

    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.bill, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orderList[position]
        holder.orderIdTextView.text = order.id.toString()
        holder.totalAmountTextView.text = String.format("$%.2f", order.totalPrice)

        // Fetch Order Items for this order using coroutine
        CoroutineScope(Dispatchers.IO).launch {
            val orderItems = db.orderItemDao().getOrderItemsByOrder(order.id)

            withContext(Dispatchers.Main) {
                // Set up nested RecyclerView for Order Items
                val layoutManager = LinearLayoutManager(context)
                holder.orderItemsRecyclerView.layoutManager = layoutManager
                val billItemAdapter = BillItemArray(orderItems as List<OrderItem>, db)
                holder.orderItemsRecyclerView.adapter = billItemAdapter
                billItemAdapter.notifyDataSetChanged() // Notify the adapter that the data has changed
            }
        }
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderIdTextView: TextView = itemView.findViewById(R.id.custom_order_id)
        val totalAmountTextView: TextView = itemView.findViewById(R.id.custom_total_amount)
        val orderItemsRecyclerView: RecyclerView = itemView.findViewById(R.id.orderItemsRecyclerView)
    }
}