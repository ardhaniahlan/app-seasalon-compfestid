package org.apps.salon

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.apps.salon.databinding.FragmentPersonBinding

class PersonFragment : Fragment() {

    private lateinit var binding: FragmentPersonBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStore: FirebaseFirestore
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPersonBinding.inflate(layoutInflater)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStore = FirebaseFirestore.getInstance()
        preferencesManager = PreferencesManager(requireContext())
        welcomeName()

        binding.apply {
            mySchedule.setOnClickListener {
                startActivity(Intent(context,MyScheduleActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            }

            btnLogout.setOnClickListener {
                logoutUser()
            }
        }

        return binding.root
    }

    private fun logoutUser() {
        firebaseAuth.signOut()
        preferencesManager.setLoggedIn(false)

        startActivity(Intent(requireActivity(), LoginActivity::class.java))
        requireActivity().finish()
    }

    private fun welcomeName(){
    val currentUser = firebaseAuth.currentUser
    if (currentUser != null) {
        val userId = currentUser.uid

        val fullnameref = firebaseStore.collection("users").document(userId)
        fullnameref.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val fullname = document.getString("fullName")
                val phoneNumber = document.getString("phoneNumber")
                val role = document.getString("role")
                binding.fullname.text = fullname
                binding.phoneNumber.text = phoneNumber
                binding.role.text = role
            } else {
                val fullnamereA = firebaseStore.collection("admin").document(userId)
                fullnamereA.get().addOnSuccessListener { adminDocument ->
                    if (adminDocument.exists()) {
                        val fullname = adminDocument.getString("fullName")
                        val phoneNumber = adminDocument.getString("phoneNumber")
                        val role = adminDocument.getString("role")
                        binding.fullname.text = fullname
                        binding.phoneNumber.text = phoneNumber
                        binding.role.text = role
                        binding.mySchedule.visibility = View.GONE
                    }
                }
            }
        }
        }
    }

}