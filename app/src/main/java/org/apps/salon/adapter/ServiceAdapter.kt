package org.apps.salon.adapter

import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.apps.salon.databinding.ItemServiceBinding
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.Filter
import android.widget.Filterable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.apps.salon.ReservationActivity
import org.apps.salon.model.Reservation
import org.apps.salon.model.Service

class ServiceAdapter(
    private val listService: ArrayList<Service>,
    private val onItemCLickListener: OnItemClickListener? = null,
    private val onDeleteClickListener: OnDeleteClickListener): RecyclerView.Adapter<ServiceAdapter.ViewHolder>(), Filterable {

    private var serviceListFiltered: ArrayList<Service> = ArrayList(listService)

    interface OnDeleteClickListener {
        fun onDeleteClick(position: Int)
    }

    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStore: FirebaseFirestore

    inner class ViewHolder(val binding: ItemServiceBinding): RecyclerView.ViewHolder(binding.root){
        init {
            binding.btnDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClickListener.onDeleteClick(position)
                }
            }
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemCLickListener?.onItemClick(position)
                }
            }
        }

        fun bind(service: Service){
            binding.apply {
                name.text = service.serviceName
                slogan.text = service.serviceSlogan

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
                                binding.btnDelete.visibility = View.INVISIBLE
                            }
                        } else {
                            val roleAdminRef = firebaseStore.collection("admin").document(userId)
                            roleAdminRef.get().addOnSuccessListener { adminDocument ->
                                if (adminDocument.exists()) {
                                    val roleAdmin = adminDocument.getString("role")
                                    if (roleAdmin == "admin"){
                                        binding.btnDelete.visibility = View.VISIBLE
                                        binding.book.visibility = View.GONE
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
        val binding = ItemServiceBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val services = listService[position]
        holder.bind(services)
    }

    override fun getItemCount(): Int = listService.size

    override fun getFilter(): Filter {
        return serviceFilter
    }

    private val serviceFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList = ArrayList<Service>()

            if (constraint.isNullOrEmpty()) {
                filteredList.addAll(serviceListFiltered)
            } else {
                val filterPattern = constraint.toString().toLowerCase().trim()

                for (service in serviceListFiltered) {
                    if (service.serviceName!!.toLowerCase().contains(filterPattern)) {
                        filteredList.add(service)
                    }
                }
            }

            val results = FilterResults()
            results.values = filteredList
            return results
        }

        @Suppress("UNCHECKED_CAST")
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            listService.clear()
            if (results?.values is ArrayList<*>) {
                listService.addAll(results.values as ArrayList<Service>)
            }
            notifyDataSetChanged()
        }
    }
}