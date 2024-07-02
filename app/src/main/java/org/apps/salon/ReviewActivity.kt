package org.apps.salon

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import org.apps.salon.adapter.ReviewAdapter
import org.apps.salon.databinding.ActivityReviewBinding
import org.apps.salon.model.Review

class ReviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReviewBinding
    private lateinit var databaseReview: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStore: FirebaseFirestore
    private lateinit var reviewAdapter : ReviewAdapter
    private val viewModel by viewModels<ReviewFragmentViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStore = FirebaseFirestore.getInstance()

        databaseReview = FirebaseDatabase.getInstance().getReference("reviews")

        viewModel.isAdmin.observe(this){
            if (it) {
                binding.apply {
                    etCustomerName.visibility = View.GONE
                    ratingBar.visibility = View.GONE
                    etComment.visibility = View.GONE
                    btnSubmit.visibility = View.GONE
                }
            }
        }
        viewModel.checkAdminStatus(firebaseAuth,firebaseStore)

//        showLoading(true)
        showReviews()
//        showLoading(false)

        binding.apply {
            btnSubmit.setOnClickListener {
                saveData()
            }
            btnBack.setOnClickListener {
                startActivity(Intent(this@ReviewActivity, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            }
        }
    }

    private fun saveData(){
        val customerName = binding.etCustomerName.text.toString().trim()
        val rating = binding.ratingBar.rating
        val comment = binding.etComment.text.toString().trim()

        if (customerName.isNotEmpty() && comment.isNotEmpty()) {
            val id = databaseReview.push().key
            val reviews = Review(id!!,customerName,rating,comment)
            viewModel.saveData(databaseReview,reviews)
            Toast.makeText(this, "Data Disimpan", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Lengkapi Field", Toast.LENGTH_SHORT).show()
        }

        binding.etCustomerName.text.clear()
        binding.ratingBar.rating = 0f
        binding.etComment.text.clear()
    }

    private fun showReviews(){
        binding.rvReviews.layoutManager = LinearLayoutManager(this)
        viewModel.reviews.observe(this){
            reviewAdapter = ReviewAdapter(it)
            binding.rvReviews.adapter = reviewAdapter
        }
    }

//    private fun showLoading(isLoading: Boolean){
//        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
//    }


}