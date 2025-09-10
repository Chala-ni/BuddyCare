package com.example.buddycare.interfaces

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.buddycare.classes.Review
import java.util.List

@Dao
interface ReviewDao {
    @Insert
    suspend fun insertReview(review: Review)

    @Update
    suspend fun updateReview(review: Review)

    @Delete
    suspend fun deleteReview(review: Review)

    @Query("SELECT * FROM reviews WHERE productId = :productId")
    suspend fun getReviewsByProduct(productId: Int): List<Review>
}