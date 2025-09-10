package com.example.buddycare.interfaces

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.buddycare.classes.OrderItem
import java.util.List

@Dao
interface OrderItemDao {
    @Insert
    suspend fun insertOrderItem(orderItem: OrderItem)

    @Update
    suspend fun updateOrderItem(orderItem: OrderItem)

    @Delete
    suspend fun deleteOrderItem(orderItem: OrderItem)

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getOrderItemsByOrder(orderId: Int): List<OrderItem>
}