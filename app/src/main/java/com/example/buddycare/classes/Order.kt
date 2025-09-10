package com.example.buddycare.classes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var userId: Int, // foreign key referencing the Users table
    var orderDate: String,
    var totalPrice: Double
) {
    constructor() : this(0, 0, "", 0.0)
}