package org.apps.salon.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.apps.salon.databinding.ItemScheduleBinding
import org.apps.salon.model.Reservation

class ScheduleAdapter(
    private val listSchedule: ArrayList<Reservation>,
    private val onDeleteClickListener: OnDeleteClickListener
) : RecyclerView.Adapter<ScheduleAdapter.ViewHolder> () {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStore: FirebaseFirestore

    interface OnDeleteClickListener {
        fun onDeleteClick(position: Int)
    }

    inner class ViewHolder(val binding: ItemScheduleBinding): RecyclerView.ViewHolder(binding.root){

        init {
            binding.btnDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClickListener.onDeleteClick(position)
                }
            }
        }

        fun bind(reservations: Reservation){
            binding.apply {
                serviceName.text = reservations.service
                username.text = reservations.name
                phoneNumber.text = reservations.phoneNumber
                date.text = reservations.date
                time.text = reservations.time

                firebaseAuth = FirebaseAuth.getInstance()
                firebaseStore = FirebaseFirestore.getInstance()

                val currentUser = firebaseAuth.currentUser
                if (currentUser != null) {
                    val userId = currentUser.uid

                    val roleUserRef = firebaseStore.collection("users").document(userId)
                    roleUserRef.get().addOnSuccessListener { document ->
                        if (document.exists()) {
                            val roleUser = document.getString("role")
                            if (roleUser == "user"){
                                binding.btnDelete.visibility = View.GONE
                            }
                        } else {
                            val roleAdminRef = firebaseStore.collection("admin").document(userId)
                            roleAdminRef.get().addOnSuccessListener { adminDocument ->
                                if (adminDocument.exists()) {
                                    val roleAdmin = adminDocument.getString("role")
                                    if (roleAdmin == "admin"){
                                        binding.btnDelete.visibility = View.VISIBLE
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reservations = listSchedule[position]
        holder.bind(reservations)
    }

    override fun getItemCount(): Int = listSchedule.size
}