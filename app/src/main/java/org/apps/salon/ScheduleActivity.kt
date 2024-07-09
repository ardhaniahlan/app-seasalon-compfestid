package org.apps.salon

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import org.apps.salon.adapter.ScheduleAdapter
import org.apps.salon.databinding.ActivityScheduleBinding
import org.apps.salon.model.Reservation

class ScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScheduleBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStore: FirebaseFirestore
    private lateinit var scheduleAdapter : ScheduleAdapter
    private lateinit var databaseReservation: DatabaseReference
    private lateinit var listSchedule: ArrayList<Reservation>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStore = FirebaseFirestore.getInstance()

        databaseReservation = FirebaseDatabase.getInstance().getReference("reservation")
        listSchedule = arrayListOf()

        fetchData()
        showSchedule(listSchedule)

        binding.btnBack.setOnClickListener {
            startActivity(Intent(this@ScheduleActivity, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        }
    }

    private fun fetchData(){
        databaseReservation.addValueEventListener(object : ValueEventListener {
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
                Toast.makeText(this@ScheduleActivity, "${error.message}",Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showSchedule(listSchedule: ArrayList<Reservation>){
        binding.rvSchedule.layoutManager = LinearLayoutManager(this)

        val onDeleteClickListener = object : ScheduleAdapter.OnDeleteClickListener {
            override fun onDeleteClick(position: Int) {
                val reservationToDelete = listSchedule[position]

                val builder = AlertDialog.Builder(this@ScheduleActivity)
                builder.setMessage("Ingin menghapusnya?")
                    .setPositiveButton("OK") { dialog, id ->
                        val reservationId = reservationToDelete.id
                        deleteReservationFromFirebase(reservationId!!)

                        listSchedule.removeAt(position)
                        scheduleAdapter.notifyItemRemoved(position)
                    }
                    .setNegativeButton("Batal") { dialog, id ->
                        dialog.dismiss()
                    }
                val alert = builder.create()
                alert.show()
            }
        }

        scheduleAdapter = ScheduleAdapter(listSchedule,onDeleteClickListener)
        binding.rvSchedule.adapter = scheduleAdapter
    }

    private fun deleteReservationFromFirebase(reservationId: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("reservation").child(reservationId)
        databaseReference.removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Reservation Dihapus", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Reservation Gagal Dihapus: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}