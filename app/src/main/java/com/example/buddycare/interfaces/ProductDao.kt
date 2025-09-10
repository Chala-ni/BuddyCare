package com.example.buddycare.interfaces

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.buddycare.classes.Product

@Dao
interface ProductDao {
    @Insert
    suspend fun insertProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("SELECT * FROM products")
    suspend fun getAllProducts(): List<Product>

    @Query("SELECT * FROM products WHERE brand = :brand")
    suspend fun getProductsByBrand(brand: String): List<Product>

    @Query("SELECT * FROM products WHERE type = :type")
    suspend fun getProductsByType(type: String): List<Product>

    @Query("SELECT * FROM products WHERE ageRange = :ageRange")
    suspend fun getProductsByAgeRange(ageRange: Int): List<Product>

    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: Int): Product

    @Query("SELECT * FROM products WHERE LOWER(name) LIKE '%' || :name || '%'")
    suspend fun getProductsByName(name: String): List<Product>
}