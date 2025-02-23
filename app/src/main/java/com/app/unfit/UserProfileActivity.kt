package com.app.unfit

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // UI Elements
    private lateinit var profileImage: ImageView
    private lateinit var usernameText: TextView
    private lateinit var emailText: TextView
    private lateinit var editProfileButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Profile"

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize UI elements
        initializeViews()

        // Load user data
        loadUserData()

        // Set up edit profile button
        setupEditButton()
    }

    private fun initializeViews() {
        profileImage = findViewById(R.id.profile_image)
        usernameText = findViewById(R.id.profile_username)
        emailText = findViewById(R.id.profile_email)
        editProfileButton = findViewById(R.id.profile_edit_button)
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e("UserProfileActivity", "No user is signed in")
            finish()
            return
        }

        // Display email from Firebase Auth
        emailText.text = currentUser.email

        // Fetch additional user data from Firestore
        db.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Get user data
                    val firstName = document.getString("firstName") ?: ""
                    val lastName = document.getString("lastName") ?: ""

                    // Display full name
                    val fullName = "$firstName $lastName"
                    usernameText.text = fullName

                    // You can load profile image here if you have one
                    // For now using default image set in XML
                } else {
                    Log.d("UserProfileActivity", "No such document")
                    usernameText.text = "User"
                }
            }
            .addOnFailureListener { e ->
                Log.w("UserProfileActivity", "Error loading user data", e)
                Toast.makeText(
                    this,
                    "Error loading profile data",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun setupEditButton() {
        editProfileButton.setOnClickListener {
            // TODO: Implement edit profile functionality
            Toast.makeText(this, "Edit profile coming soon!", Toast.LENGTH_SHORT).show()
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