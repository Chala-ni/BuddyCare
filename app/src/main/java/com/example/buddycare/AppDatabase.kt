package com.example.buddycare

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.buddycare.classes.Order
import com.example.buddycare.classes.OrderItem
import com.example.buddycare.classes.Product
import com.example.buddycare.classes.Review
import com.example.buddycare.classes.User
import com.example.buddycare.interfaces.OrderDao
import com.example.buddycare.interfaces.OrderItemDao
import com.example.buddycare.interfaces.ProductDao
import com.example.buddycare.interfaces.ReviewDao
import com.example.buddycare.interfaces.UserDao

@Database(entities = [User::class, Product::class, Order::class, OrderItem::class, Review::class], exportSchema = false, version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
    abstract fun orderItemDao(): OrderItemDao
    abstract fun reviewDao(): ReviewDao
}