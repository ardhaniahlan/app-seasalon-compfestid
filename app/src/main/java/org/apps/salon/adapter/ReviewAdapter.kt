package org.apps.salon.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.apps.salon.databinding.ItemReviewBinding
import org.apps.salon.model.Review

class ReviewAdapter(private val listReview: ArrayList<Review>) : RecyclerView.Adapter<ReviewAdapter.ViewHolder> () {
    class ViewHolder(val binding: ItemReviewBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(review: Review){
            binding.apply {
                name.text = review.customerName
                rating.text = review.rating?.toInt().toString()
                comment.text = review.comment
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = listReview.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reviews = listReview[position]
        holder.bind(reviews)
    }
}