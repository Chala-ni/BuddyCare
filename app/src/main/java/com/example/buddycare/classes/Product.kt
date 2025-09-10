package com.example.buddycare.classes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var name: String,
    var description: String,
    var price: Double,
    var brand: String,
    var type: String,
    var ageRange: Int, // e.g., puppy, adult, senior
    var imageUrl: String
) {
    constructor() : this(
        0,
        "",
        "",
        0.0,
        "",
        "",
        0,
        ""
    )
}