package com.example.buddycare.classes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "order_items")
data class OrderItem(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var orderId: Int, // foreign key referencing the Orders table
    var productId: Int, // foreign key referencing the Products table
    var quantity: Int,
    var subtotal: Double
) {
    constructor() : this(0, 0, 0, 0, 0.0)
}