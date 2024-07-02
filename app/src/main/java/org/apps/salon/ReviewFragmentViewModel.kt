package org.apps.salon

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.play.integrity.internal.c
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import org.apps.salon.model.Review

class ReviewFragmentViewModel : ViewModel() {
    val isAdmin = MutableLiveData<Boolean>()

    private val _reviews = MutableLiveData<ArrayList<Review>>()
    val reviews: LiveData<ArrayList<Review>> get() = _reviews

    private val databaseReview: DatabaseReference = FirebaseDatabase.getInstance().getReference("reviews")

    init {
        fetchData()
    }

    fun checkAdminStatus(firebaseAuth: FirebaseAuth, firebaseStore: FirebaseFirestore){
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val roleUserRef = firebaseStore.collection("admin").document(userId)
            roleUserRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val roleUser = document.getString("role")
                    isAdmin.value = (roleUser == "admin")
                }
            }
        }
    }

    fun saveData(databaseReview: DatabaseReference, review: Review){
        databaseReview.child(review.id!!).setValue(review)
    }

    private fun fetchData(){
        databaseReview.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val listReview = ArrayList<Review>()
                if (snapshot.exists()){
                    for (reviewSnap in snapshot.children){
                        val reviews = reviewSnap.getValue(Review::class.java)
                        if (reviews != null){
                            listReview.add(reviews)
                        }
                    }
                }
                _reviews.value = listReview
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}