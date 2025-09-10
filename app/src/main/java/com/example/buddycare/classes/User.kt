package com.example.buddycare.classes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var email: String = "",
    var name: String = "",
    var address: String = "",
    var phoneNumber: String = "",
    var password: String = "",
    var paymentMethod: String = ""
) {
    constructor(
        email: String,
        name: String,
        address: String,
        phoneNumber: String,
        password: String,
        paymentMethod: String
    ) : this(0, email, name, address, phoneNumber, password, paymentMethod)
}