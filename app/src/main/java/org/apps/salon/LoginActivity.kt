package org.apps.salon

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.apps.salon.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStore: FirebaseFirestore
    private lateinit var preferencesManager: PreferencesManager

    override fun onStart() {
        super.onStart()
        if (preferencesManager.isLoggedIn()){
            goToMainActivity()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStore = FirebaseFirestore.getInstance()
        preferencesManager = PreferencesManager(this)

        binding.apply {
            tvRegister.setOnClickListener {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            }

            btnSubmit.setOnClickListener {
                val email = binding.etEmail.text.toString()
                val password = binding.etPassword.text.toString()

                when {
                    email.isEmpty() -> {
                        binding.etEmail.error = "Email Harus Diisi"
                        binding.etEmail.requestFocus()
                    }

                    !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                        binding.etEmail.error = "Email Tidak Valid"
                        binding.etEmail.requestFocus()
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
                            loginAdmin(email,password)
                            loginUser(email, password)
                    }
                }
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        firebaseStore = FirebaseFirestore.getInstance()
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful){
                        val currentUser = firebaseAuth.currentUser
                        if (currentUser != null) {
                            val userId = currentUser.uid
                            val fullnameref = firebaseStore.collection("users").document(userId)
                            fullnameref.get().addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val fullname = document.getString("fullName")
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Selamat Datang $fullname",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    preferencesManager.setLoggedIn(true)

                                    startActivity(Intent(this, MainActivity::class.java))
                                    }
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this@LoginActivity,
                    "User tidak ditemukan",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun loginAdmin(email: String, password: String) {
        firebaseStore = FirebaseFirestore.getInstance()
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    val currentUser = firebaseAuth.currentUser
                    if (currentUser != null) {
                        val userId = currentUser.uid
                        val fullnameref = firebaseStore.collection("admin").document(userId)
                        fullnameref.get().addOnSuccessListener { document ->
                            if (document.exists()) {
                                val fullname = document.getString("fullName")
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Selamat Datang $fullname",
                                    Toast.LENGTH_SHORT
                                ).show()

                                preferencesManager.setLoggedIn(true)
                                startActivity(Intent(this, MainActivity::class.java))
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this@LoginActivity,
                    "Admin tidak ditemukan",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
