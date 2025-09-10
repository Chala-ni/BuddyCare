package com.example.buddycare

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.buddycare.classes.Product
import com.example.buddycare.classes.Review
import com.example.buddycare.interfaces.ReviewDao
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

class ReviewArray(private var productList: MutableList<Product>, private val context: Context, private val reviewDao: ReviewDao) : RecyclerView.Adapter<ReviewArray.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.review_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.productNameTextView.text = product.name

        // Calculate and set the average rating
        CoroutineScope(Dispatchers.IO).launch {
            val averageRating = calculateAverageRating(product.id)
            withContext(Dispatchers.Main) {
                setRatingStars(holder.starIconsLinearLayout, averageRating)
            }
        }

        // Load the image using the getBitmapFromUrl method
        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = getBitmapFromUrl(product.imageUrl)
            withContext(Dispatchers.Main) {
                if (bitmap != null) {
                    holder.productImageView.setImageBitmap(bitmap)
                } else {
                    // Set a default image if bitmap is null (loading failed)
                    holder.productImageView.setImageResource(R.drawable.ic_star_solid_grey) // Use a placeholder image
                }
            }
        }

        // Initially hide the review list
        holder.reviewListRecyclerView.visibility = View.GONE

        // Handle "See All Reviews" button click
        holder.seeAllReviewsButton.setOnClickListener {
            toggleReviewsVisibility(holder)
        }

        // Handle "Review Now" button click
        holder.reviewNowButton.setOnClickListener {
            showReviewDialog(product.id, holder)
        }
    }

    private fun toggleReviewsVisibility(holder: ProductViewHolder) {
        if (holder.reviewListRecyclerView.visibility == View.VISIBLE) {
            holder.reviewListRecyclerView.visibility = View.GONE
            holder.seeAllReviewsButton.text = "All Reviews" // Change button text back
        } else {
            // Only fetch reviews when showing the list
            CoroutineScope(Dispatchers.IO).launch {
                val reviews = reviewDao.getReviewsByProduct(holder.adapterPosition + 1)
                withContext(Dispatchers.Main) {
                    val reviewListAdapter = ReviewListAdapter(reviews as List<Review>, context)
                    holder.reviewListRecyclerView.adapter = reviewListAdapter
                    holder.reviewListRecyclerView.layoutManager = LinearLayoutManager(context)
                    holder.reviewListRecyclerView.visibility = View.VISIBLE
                    holder.seeAllReviewsButton.text = "Hide Reviews" // Change button text
                }
            }
        }
    }

    // Helper method to show the custom dialog for adding a review
    private fun showReviewDialog(productId: Int, holder: ProductViewHolder) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.custom_review, null)

        val starIconsLinearLayout = dialogView.findViewById<LinearLayout>(R.id.starIcons)
        val reviewEditText = dialogView.findViewById<TextView>(R.id.editTextTextMultiLine)
        val rateButton = dialogView.findViewById<Button>(R.id.button2)

        setRatingStars(starIconsLinearLayout, 0F)

        for (i in 0 until starIconsLinearLayout.childCount) {
            val starIcon = starIconsLinearLayout.getChildAt(i) as ImageView
            val rating = i + 1

            starIcon.setOnClickListener {
                setRatingStars(starIconsLinearLayout, rating.toFloat())
            }
        }

        // Directly handle the "Rate Now" button click in the dialog
        rateButton.setOnClickListener {
            val reviewText = reviewEditText.text.toString()
            val rating = getSelectedRating(starIconsLinearLayout)

            if (reviewText.isEmpty()) {
                Toast.makeText(context, "Please enter a review", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (rating == 0) {
                Toast.makeText(context, "Please select a rating", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newReview = Review()
            newReview.productId = productId
            newReview.reviewText = reviewText
            newReview.rating = rating

            CoroutineScope(Dispatchers.IO).launch {
                reviewDao.insertReview(newReview)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Review submitted successfully!", Toast.LENGTH_SHORT).show()
                    notifyDataSetChanged()
                }
            }
        }

        // Create and show the dialog (without the Submit/Cancel buttons)
        AlertDialog.Builder(context)
            .setView(dialogView)
            .show()
    }

    private suspend fun calculateAverageRating(productId: Int): Float {
        val reviews = reviewDao.getReviewsByProduct(productId)
        if (reviews.isEmpty()) {
            return 0f
        }

        var totalRating = 0
        for (review in reviews) {
            totalRating += review.rating
        }
        return totalRating.toFloat() / reviews.size
    }

    private suspend fun getBitmapFromUrl(url: String): Bitmap? {
        try {
            val file = File(url)
            return BitmapFactory.decodeStream(FileInputStream(file))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return null
        }
    }

    private fun getSelectedRating(starIconsLinearLayout: LinearLayout): Int {
        var rating = 0
        for (i in 0 until starIconsLinearLayout.childCount) {
            val starIcon = starIconsLinearLayout.getChildAt(i) as ImageView
            // Check for a tag indicating the selected state
            if (starIcon.tag != null && starIcon.tag.toString() == "selected") {
                rating++
            } else {
                break // Stop counting if a star isn't selected
            }
        }
        return rating
    }

    private fun setRatingStars(starIconsLinearLayout: LinearLayout, rating: Float) {
        for (i in 0 until starIconsLinearLayout.childCount) {
            val starIcon = starIconsLinearLayout.getChildAt(i) as ImageView
            if (i < rating) {
                starIcon.setImageResource(R.drawable.ic_star_solid)
                starIcon.tag = "selected" // Set a tag to indicate selected
            } else {
                starIcon.setImageResource(R.drawable.ic_star_solid_grey)
                starIcon.tag = null // Clear the tag
            }
        }
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    fun setProducts(products: List<Product>) {
        productList.clear()
        productList.addAll(products)
        notifyDataSetChanged()
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImageView: ImageView = itemView.findViewById(R.id.imageView)
        val productNameTextView: TextView = itemView.findViewById(R.id.review_product_name)
        val starIconsLinearLayout: LinearLayout = itemView.findViewById(R.id.starIcons)
        val seeAllReviewsButton: Button = itemView.findViewById(R.id.see_all)
        val reviewNowButton: Button = itemView.findViewById(R.id.review_now)
        val reviewListRecyclerView: RecyclerView = itemView.findViewById(R.id.review_list)
    }
}