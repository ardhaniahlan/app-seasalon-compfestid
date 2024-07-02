package org.apps.salon

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import org.apps.salon.adapter.ImageSliderAdapter
import org.apps.salon.adapter.ServiceAdapter
import org.apps.salon.databinding.FragmentHomeBinding
import org.apps.salon.model.Poster
import org.apps.salon.model.Service

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStore: FirebaseFirestore
    private lateinit var serviceAdapter: ServiceAdapter
    private lateinit var imageAdapter: ImageSliderAdapter
    private lateinit var databaseService: DatabaseReference
    private lateinit var listService: ArrayList<Service>
    private lateinit var serviceList: ArrayList<Service>
    private lateinit var dots: ArrayList<TextView>
    private val list = ArrayList<Poster>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStore = FirebaseFirestore.getInstance()

        databaseService = FirebaseDatabase.getInstance().getReference("services")
        listService = arrayListOf()
        serviceList = arrayListOf()

        fetchData()
        disableAddService()
        imageSliderDummy()

        binding.apply {
            // Contact
            thomas.setOnClickListener {
                showDialogContact("Thomas", "+628123456789")
            }
            sekar.setOnClickListener {
                showDialogContact("Sekar", "+628164829372")
            }

            // Services
            addService.setOnClickListener {
                val showAddService = AddServiceFragment()
                showAddService.show(
                    (activity as AppCompatActivity).supportFragmentManager,
                    "showAddService"
                )
            }

            // Review
            review.setOnClickListener {
                startActivity(Intent(context, ReviewActivity::class.java))
            }

            //Schedule
            schedule.setOnClickListener {
                startActivity(Intent(context, ScheduleActivity::class.java))
            }

            // Search
            etSearch.setOnQueryTextListener(object : OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    serviceAdapter.filter.filter(newText)
                    return false
                }
            })
        }
    }

    private fun imageSliderDummy(){
        list.add(
            Poster("https://cdn.pixabay.com/photo/2024/05/16/07/32/man-8765248_1280.jpg")
        )

        list.add(
            Poster("https://cdn.pixabay.com/photo/2020/11/10/19/04/nails-5730756_640.jpg")
        )

        list.add(
            Poster("https://cdn.pixabay.com/photo/2023/08/31/08/58/facial-8224799_1280.jpg")
        )

        imageAdapter = ImageSliderAdapter(list)
        binding.viewPager.adapter = imageAdapter
        dots = arrayListOf()
        setIndicator()
        binding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                selectedDot(position)
                super.onPageSelected(position)
            }
        })
    }

    private fun selectedDot(position: Int) {
        for (i in 0 until list.size){
            if (i == position){
                dots[i].setTextColor(ContextCompat.getColor(requireContext(), R.color.blue))
            } else {
                dots[i].setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }
        }
    }

    private fun setIndicator() {
        for (i in 0 until list.size){
            dots.add(TextView(context))
            dots[i].text = Html.fromHtml("&#9679", Html.FROM_HTML_MODE_LEGACY).toString()
            dots[i].textSize = 18f
            binding.dotsIndicator.addView(dots[i])
        }
    }

    private fun disableAddService(){
        val currentRole = firebaseAuth.currentUser!!.uid
        val deleteAddService = firebaseStore.collection("users").document(currentRole)
        deleteAddService.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val role = document.getString("role")
                if (role == "user"){
                    binding.addService.visibility = View.GONE
                }
            }
        }
    }

    private fun showServices(listService: ArrayList<Service>) {
        binding.rvService.layoutManager = LinearLayoutManager(context)

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            firebaseStore.collection("users").document(userId).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role")
                    recyclerBehavior(role, listService)
                } else {
                    firebaseStore.collection("admin").document(userId).get().addOnSuccessListener { adminDocument ->
                        if (adminDocument.exists()) {
                            val role = adminDocument.getString("role")
                            recyclerBehavior(role, listService)
                        } else {
                            null
                        }
                    }
                }
            }
        }
    }

    private fun recyclerBehavior(role: String?, listService: ArrayList<Service>) {
        val context = binding.rvService.context ?: return

        val onItemClickListener = if (role == "user") {
            object : ServiceAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    reservationServices(listService[position])
                }
            }
        } else {
            null
        }

        val onDeleteClickListener = object : ServiceAdapter.OnDeleteClickListener {
            override fun onDeleteClick(position: Int) {
                val serviceToDelete = listService[position]

                val builder = AlertDialog.Builder(context)
                builder.setMessage("Ingin menghapusnya?")
                    .setPositiveButton("OK") { dialog, id ->
                        val serviceId = serviceToDelete.id
                        deleteServiceFromFirebase(serviceId!!)

                        listService.removeAt(position)
                        serviceAdapter.notifyItemRemoved(position)
                    }
                    .setNegativeButton("Batal") { dialog, id ->
                        dialog.dismiss()
                    }
                val alert = builder.create()
                alert.show()
            }
        }

        serviceAdapter = ServiceAdapter(listService, onItemClickListener, onDeleteClickListener)
        binding.rvService.adapter = serviceAdapter
    }

    private fun reservationServices(service: Service){
        val intent = Intent(requireActivity(), ReservationActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.putExtra("data", service.serviceName)
        startActivity(intent)
    }

    private fun deleteServiceFromFirebase(serviceId: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("services").child(serviceId)
        databaseReference.removeValue()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Service Dihapus", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Service Gagal Dihapus: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchData(){
        databaseService.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                listService.clear()
                if (snapshot.exists()){
                    for (serviceSnap in snapshot.children){
                        val services = serviceSnap.getValue(Service::class.java)
                        listService.add(services!!)
                    }
                }
                showServices(listService)
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "${error.message}",Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDialogContact(name: String, phoneNumber: String){
        val builder = AlertDialog.Builder(context)
        builder.setMessage("Ingin menghubungi $name? \nvia WhatsApp")
            .setPositiveButton("OK") {dialog, id ->
                val url = "https://api.whatsapp.com/send?phone=$phoneNumber"
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                startActivity(intent)
            }
            .setNegativeButton("Batal") {dialog, id ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }
}