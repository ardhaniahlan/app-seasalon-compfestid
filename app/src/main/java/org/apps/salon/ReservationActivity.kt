package org.apps.salon

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import org.apps.salon.databinding.ActivityReservationBinding
import org.apps.salon.model.Reservation
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReservationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReservationBinding
    private lateinit var databaseReservation: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStore: FirebaseFirestore
    private var selectedDate: Calendar? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReservationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val receivedData = intent.getStringExtra("data")
        binding.services.text = receivedData.toString()

        databaseReservation = FirebaseDatabase.getInstance().getReference("reservation")

        binding.apply {
            btnBatal.setOnClickListener {
                startActivity(Intent(this@ReservationActivity,MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            }
            btnReset.setOnClickListener {
                binding.editTextName.text.clear()
                binding.editTextPhoneNumber.text.clear()
                binding.date.text = "Select Date"
                binding.time.text = "Select Time"
            }

            firebaseAuth = FirebaseAuth.getInstance()
            firebaseStore = FirebaseFirestore.getInstance()
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                val userId = currentUser.uid
                btnSubmit.setOnClickListener {
                    saveData(userId)
                }
                val fullnameref = firebaseStore.collection("users").document(userId)
                fullnameref.get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        val fullname = document.getString("fullName")
                        val phoneNumber = document.getString("phoneNumber")
                        binding.editTextName.setText(fullname)
                        binding.editTextPhoneNumber.setText(phoneNumber)
                    }
                }
            }

            date.setOnClickListener {
                showDatePickerDialog(binding.date)
            }
            time.setOnClickListener {
                showTimePickerDialog(binding.time)
            }
        }
    }

    private fun saveData(userId: String) {
        val customerName = binding.editTextName.text.toString().trim()
        val phoneNumber = binding.editTextPhoneNumber.text.toString().trim()
        val receivedData = intent.getStringExtra("data")
        binding.services.text = receivedData.toString()
        val service = binding.services.text.toString().trim()
        val date = binding.date.text.toString().trim()
        val time = binding.time.text.toString().trim()

        if (customerName.isNotEmpty() && phoneNumber.isNotEmpty() && date.isNotEmpty() && time.isNotEmpty()){
            val id = databaseReservation.push().key
            val reservation = Reservation(id!!,customerName,phoneNumber,service,date,time,userId)
            databaseReservation.child(id).setValue(reservation)
                .addOnCompleteListener{
                    Toast.makeText(this, "Data Disimpan", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Data Gagal Disimpan", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Lengkapi Field", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDatePickerDialog(date: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDate!!.time)
                date.text = formattedDate
            },
            year, month, day
        )
        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    private fun showTimePickerDialog(time: TextView) {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val selectedCalendar = selectedDate ?: calendar
        val isToday = calendar.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == selectedCalendar.get(Calendar.DAY_OF_YEAR)

        val hour = if (isToday && currentHour < 9) 9 else if (isToday && currentHour > 21) 21 else currentHour
        val minute = 0

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, _ ->
                val validatedHour = if (selectedHour < 9) 9 else if (selectedHour > 21) 21 else selectedHour
                val formattedTime = String.format("%02d:00", validatedHour)
                time.text = formattedTime
            },
            hour,
            minute,
            true
        )

        timePickerDialog.setOnShowListener {
            val timePicker = timePickerDialog.findViewById<TimePicker>(
                resources.getIdentifier("timePicker", "id", "android")
            )

            if (timePicker != null) {
                val minutePicker = timePicker.findViewById<NumberPicker>(
                    resources.getIdentifier("minute", "id", "android")
                )
                if (minutePicker != null) {
                    minutePicker.minValue = 0
                    minutePicker.maxValue = 0 // Force minutes to be zero
                }

                timePicker.setOnTimeChangedListener { _, selectedHour, _ ->
                    val validatedHour = when {
                        selectedHour < 9 -> 9
                        selectedHour > 21 -> 21
                        else -> selectedHour
                    }
                    timePicker.hour = validatedHour
                }

                timePicker.setOnTimeChangedListener { _, selectedHour, selectedMinute ->
                    if (isToday && (selectedHour < currentHour || (selectedHour == currentHour && selectedMinute < currentMinute))) {
                        timePicker.hour = currentHour
                        timePicker.minute = currentMinute
                    }
                }

                if (isToday) {
                    timePicker.hour = hour
                } else {
                    timePicker.hour = if (hour < 9) 9 else if (hour > 21) 21 else hour
                }
            }
        }

        timePickerDialog.setButton(
            DialogInterface.BUTTON_POSITIVE,
            getString(android.R.string.ok)
        ) { dialog, _ ->
            val timePicker = (dialog as TimePickerDialog).findViewById<TimePicker>(
                resources.getIdentifier("timePicker", "id", "android")
            )
            val selectedHour = timePicker?.hour ?: currentHour
            val selectedMinute = timePicker?.minute ?: currentMinute

            if (isToday && (selectedHour < currentHour || (selectedHour == currentHour && selectedMinute < currentMinute))) {
                val formattedTime = String.format("%02d:00", currentHour)
                time.text = formattedTime
            } else {
                val validatedHour = if (selectedHour < 9) 9 else if (selectedHour > 21) 21 else selectedHour
                val formattedTime = String.format("%02d:00", validatedHour)
                time.text = formattedTime
            }
        }
        timePickerDialog.show()
    }
}