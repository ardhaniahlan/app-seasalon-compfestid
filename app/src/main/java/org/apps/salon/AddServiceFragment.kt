package org.apps.salon

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.apps.salon.databinding.FragmentAddServiceBinding
import org.apps.salon.model.Service

class AddServiceFragment : DialogFragment() {

    private lateinit var binding: FragmentAddServiceBinding
    private lateinit var databaseService: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddServiceBinding.inflate(layoutInflater)
        databaseService = FirebaseDatabase.getInstance().getReference("services")

        binding.apply {
            btnSubmit.setOnClickListener {
                saveData()
                startActivity(Intent(context,MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            }
        }
        return binding.root
    }

    private fun saveData(){
        val serviceName = binding.serviceName.text.toString()
        val serviceSlogan = binding.serviceSlogan.text.toString()

        if (serviceName.isNotEmpty() && serviceSlogan.isNotEmpty()){
            val id = databaseService.push().key
            val service = Service(id!!, serviceName, serviceSlogan)
            databaseService.child(id).setValue(service)
                .addOnCompleteListener{
                    Toast.makeText(context, "Data Disimpan", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Data Gagal Disimpan", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "Lengkapi Field", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}