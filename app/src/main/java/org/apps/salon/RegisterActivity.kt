package org.apps.salon

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.apps.salon.databinding.ActivityRegisterBinding
import org.apps.salon.model.User

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStore = FirebaseFirestore.getInstance()

        binding.apply {
            tvLogin.setOnClickListener {
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            }

            btnSubmit.setOnClickListener {
                val fullname = binding.etUsername.text.toString()
                val email = binding.etEmail.text.toString()
                val phoneNumber = binding.etPhoneNumber.text.toString()
                val password = binding.etPassword.text.toString()

                when {
                    fullname.isEmpty() -> {
                        binding.etUsername.error = "Fullname Harus Diisi"
                        binding.etUsername.requestFocus()
                    }

                    email.isEmpty() -> {
                        binding.etEmail.error = "Email Harus Diisi"
                        binding.etEmail.requestFocus()
                    }

                    !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                        binding.etEmail.error = "Email Tidak Valid"
                        binding.etEmail.requestFocus()
                    }

                    phoneNumber.isEmpty() -> {
                        binding.etPhoneNumber.error = "Phone Number Harus Diisi"
                        binding.etPhoneNumber.requestFocus()
                    }

                    password.isEmpty() -> {
                        binding.etPassword.error = "Password Harus Diisi"
                        binding.etPassword.requestFocus()
                    }

                    password.length < 6 -> {
                        binding.etPassword.error = "Password minimal 6 digit"
                        binding.etPassword.requestFocus()
                    }

                    else -> {
                        registerUser(fullname, email, phoneNumber, password, "user")
                        registerAdmin()
                    }
                }
            }
        }
    }

    private fun registerAdmin() {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStore = FirebaseFirestore.getInstance()

        val adminEmail = "thomas.n@compfest.id"
        val adminPassword = "Admin123"
        firebaseAuth.createUserWithEmailAndPassword(adminEmail, adminPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    user?.let {
                        val admin = hashMapOf(
                            "fullName" to "Thomas N",
                            "email" to adminEmail,
                            "phoneNumber" to "08123456789",
                            "role" to "admin"
                        )

                        firebaseStore.collection("admin").document(it.uid).set(admin)
                    }
                }
            }
    }


    private fun registerUser(fullName: String, email: String, phoneNumber: String, password: String, role: String) {
        firebaseAuth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(this){
                if (it.isSuccessful){

                    firebaseAuth = FirebaseAuth.getInstance()
                    firebaseStore = FirebaseFirestore.getInstance()

                    val userId = firebaseAuth.currentUser?.uid
                    if (userId != null){
                        val user = User(
                            id = userId,
                            fullName = fullName,
                            email = email,
                            phoneNumber = phoneNumber,
                            password = password,
                            role = role
                        )
                        firebaseStore.collection("users").document(userId).set(user)
                            .addOnCompleteListener{
                                Toast.makeText(this, "Registrasi Berhasil",Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, LoginActivity::class.java))
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Registrasi Gagal",Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Register Error", Toast.LENGTH_SHORT).show()
                }
            }
    }
}