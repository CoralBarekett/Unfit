package com.app.unfit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var firstNameInput: EditText
    private lateinit var lastNameInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var signupButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        // Enable back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize UI elements
        emailInput = findViewById(R.id.signup_activity_email_input)
        passwordInput = findViewById(R.id.signup_activity_password_input)
        firstNameInput = findViewById(R.id.signup_activity_first_name_input)
        lastNameInput = findViewById(R.id.signup_activity_last_name_input)
        phoneInput = findViewById(R.id.signup_activity_phone_input)
        signupButton = findViewById(R.id.signup_activity_sign_up_button)

        // Set up click listener for signup button
        signupButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val firstName = firstNameInput.text.toString().trim()
            val lastName = lastNameInput.text.toString().trim()
            val phone = phoneInput.text.toString().trim()

            if (validateInputs(email, password, firstName, lastName, phone)) {
                signupButton.isEnabled = false // Disable button during registration
                registerUser(email, password, firstName, lastName, phone)
            }
        }
    }

    private fun validateInputs(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String
    ): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            emailInput.error = "Email is required"
            isValid = false
        }

        if (password.isEmpty()) {
            passwordInput.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            passwordInput.error = "Password must be at least 6 characters"
            isValid = false
        }

        if (firstName.isEmpty()) {
            firstNameInput.error = "First name is required"
            isValid = false
        }

        if (lastName.isEmpty()) {
            lastNameInput.error = "Last name is required"
            isValid = false
        }

        if (phone.isEmpty()) {
            phoneInput.error = "Phone number is required"
            isValid = false
        }

        return isValid
    }

    private fun registerUser(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                signupButton.isEnabled = true // Re-enable button
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        saveUserToFirestore(userId, email, firstName, lastName, phone)
                    }
                } else {
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthWeakPasswordException -> "Password is too weak"
                        is FirebaseAuthInvalidCredentialsException -> "Invalid email format"
                        is FirebaseAuthUserCollisionException -> "An account already exists with this email"
                        else -> "Registration failed: ${task.exception?.message}"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    Log.w("FirebaseAuth", "Signup failed", task.exception)
                }
            }
    }

    private fun saveUserToFirestore(
        userId: String,
        email: String,
        firstName: String,
        lastName: String,
        phone: String
    ) {
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
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                navigateToHome()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving user data: ${e.message}", Toast.LENGTH_LONG).show()
                Log.w("Firestore", "Error saving user data", e)
            }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        // Clear the back stack so user can't go back to signup screen
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
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