package com.example.buddycare.interfaces

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.buddycare.classes.Order
import java.util.List

@Dao
interface OrderDao {
    @Insert
    fun insertOrder(order: Order): Long

    @Update
    fun updateOrder(order: Order)

    @Delete
    fun deleteOrder(order: Order)

    @Query("SELECT * FROM orders WHERE userId = :userId")
    fun getOrdersByUser(userId: Int): List<Order>

    @Query("SELECT * FROM orders WHERE id = :id")
    fun getOrderById(id: Int): Order

    @Query("SELECT * FROM orders")
    fun getAllOrders(): List<Order>
}