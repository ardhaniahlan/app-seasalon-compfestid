package org.apps.salon

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import org.apps.salon.adapter.ScheduleAdapter
import org.apps.salon.databinding.ActivityMyScheduleBinding
import org.apps.salon.model.Reservation

class MyScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyScheduleBinding
    private lateinit var scheduleAdapter : ScheduleAdapter
    private lateinit var databaseReservation: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStore: FirebaseFirestore
    private lateinit var listSchedule: ArrayList<Reservation>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStore = FirebaseFirestore.getInstance()

        databaseReservation = FirebaseDatabase.getInstance().getReference("reservation")
        listSchedule = arrayListOf()


        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            fetchData(userId)
        } else {
            // Handle jika tidak ada pengguna yang login
            Toast.makeText(this, "Pengguna tidak login", Toast.LENGTH_SHORT).show()
        }
        showSchedule(listSchedule)

        binding.btnBack.setOnClickListener {
            startActivity(
                Intent(this,MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        }
    }

    private fun showSchedule(listSchedule: ArrayList<Reservation>){
        binding.rvSchedule.layoutManager = LinearLayoutManager(this)
        scheduleAdapter = ScheduleAdapter(listSchedule,null)
        binding.rvSchedule.adapter = scheduleAdapter
    }

    private fun fetchData(userId: String){
        val query = databaseReservation.orderByChild("userId").equalTo(userId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listSchedule.clear()
                if (snapshot.exists()){
                    for (reservSnap in snapshot.children){
                        val reserv = reservSnap.getValue(Reservation::class.java)
                        listSchedule.add(reserv!!)
                    }
                }
                showSchedule(listSchedule)
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MyScheduleActivity, "${error.message}",Toast.LENGTH_SHORT).show()
            }
        })
    }
}