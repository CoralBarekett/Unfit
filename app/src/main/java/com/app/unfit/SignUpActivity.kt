package com.app.unfit

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        // Enable back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val emailInput = findViewById<EditText>(R.id.signup_activity_email_input)
        val passwordInput = findViewById<EditText>(R.id.signup_activity_password_input)
        val firstNameInput = findViewById<EditText>(R.id.signup_activity_first_name_input)
        val lastNameInput = findViewById<EditText>(R.id.signup_activity_last_name_input)
        val phoneInput = findViewById<EditText>(R.id.signup_activity_phone_input)
        val signupButton = findViewById<Button>(R.id.signup_activity_sign_up_button)

        signupButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val firstName = firstNameInput.text.toString().trim()
            val lastName = lastNameInput.text.toString().trim()
            val phone = phoneInput.text.toString().trim()

            if (email.isNotEmpty() && password.length >= 6 && firstName.isNotEmpty() && lastName.isNotEmpty() && phone.isNotEmpty()) {
                registerUser(email, password, firstName, lastName, phone)
            }
        }
    }

    private fun registerUser(email: String, password: String, firstName: String, lastName: String, phone: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        saveUserToFirestore(userId, email, firstName, lastName, phone)
                    }
                } else {
                    Log.w("FirebaseAuth", "Signup failed", task.exception)
                }
            }
    }

    private fun saveUserToFirestore(userId: String, email: String, firstName: String, lastName: String, phone: String) {
        val user = hashMapOf(
            "userId" to userId,
            "email" to email,
            "firstName" to firstName,
            "lastName" to lastName,
            "phone" to phone
        )

        db.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                finish() // Close activity after success
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error saving user data", e)
            }
    }

    // Handle back button click
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}