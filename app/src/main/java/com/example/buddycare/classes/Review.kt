package com.example.buddycare.classes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var productId: Int, // foreign key referencing the Products table
    var reviewText: String,
    var rating: Int // e.g., 1-6 stars
) {
    constructor() : this(0, 0, "", 0)
}