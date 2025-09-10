package com.example.buddycare

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.buddycare.classes.Review

class ReviewListAdapter(private val reviews: List<Review>, private val context: Context) :
    RecyclerView.Adapter<ReviewListAdapter.ReviewViewHolder>() {

    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.custom_review_item, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]

        // Assuming you'll fetch customer name from somewhere else (e.g., User table)
        holder.customerNameTextView.text = "Customer " + (position + 1) // For now, just showing a placeholder
        holder.reviewTextView.text = review.reviewText

        setRatingStars(holder.ratingStarsLinearLayout, review.rating.toFloat())
    }

    // Helper function to set star icons based on  rating
    private fun setRatingStars(starIconsLinearLayout: LinearLayout, rating: Float) {
        for (i in 0 until starIconsLinearLayout.childCount) {
            val starIcon = starIconsLinearLayout.getChildAt(i) as ImageView
            if (i < rating.toInt()) {
                starIcon.setImageResource(R.drawable.ic_star_solid)
            } else if (i == rating.toInt() && rating - rating.toInt() >= 0.5) {
                starIcon.setImageResource(R.drawable.ic_star_half)
            } else {
                starIcon.setImageResource(R.drawable.ic_star_solid_grey)
            }
        }
    }

    override fun getItemCount(): Int {
        return reviews.size
    }

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val customerNameTextView: TextView = itemView.findViewById(R.id.customer_name)
        val reviewTextView: TextView = itemView.findViewById(R.id.review)
        val ratingStarsLinearLayout: LinearLayout = itemView.findViewById(R.id.linearLayout2)
    }
}