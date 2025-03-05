package com.app.unfit

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class UserProfileActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    // UI Elements
    private lateinit var profileImage: ImageView
    private lateinit var usernameText: TextView
    private lateinit var emailText: TextView
    private lateinit var editProfileButton: Button
    private lateinit var editProfileImageButton: ImageButton
    private lateinit var editUsernameButton: ImageButton
    private lateinit var editEmailButton: ImageButton
    private lateinit var backButton: ImageView

    private var selectedImageUri: Uri? = null
    private var currentFirstName: String = ""
    private var currentLastName: String = ""

    // Image picker launcher
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            profileImage.setImageURI(it)
            uploadProfileImage(it)
        }
    }

    override fun getContentLayoutId(): Int {
        return R.layout.activity_profile_content
    }

    override fun getNavigationMenuItemId(): Int {
        return R.id.nav_profile
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Initialize UI elements
        initializeViews()

        // Load user data
        loadUserData()

        // Set up buttons
        setupButtons()
    }

    private fun initializeViews() {
        profileImage = findViewById(R.id.profile_image)
        usernameText = findViewById(R.id.profile_username)
        emailText = findViewById(R.id.profile_email)
        editProfileButton = findViewById(R.id.profile_edit_button)
        editProfileImageButton = findViewById(R.id.edit_profile_image_button)
        editUsernameButton = findViewById(R.id.edit_username_button)
        editEmailButton = findViewById(R.id.edit_email_button)
        backButton = findViewById(R.id.profile_activity_back_button)
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
                    currentFirstName = document.getString("firstName") ?: ""
                    currentLastName = document.getString("lastName") ?: ""
                    val profileImageUrl = document.getString("profileImageUrl")

                    // Display full name
                    val fullName = "$currentFirstName $currentLastName"
                    usernameText.text = fullName

                    // Load profile image if available
                    profileImageUrl?.let {
                        // You can use Glide or Picasso here to load the image
                        // For example with Glide:
                        // Glide.with(this).load(it).into(profileImage)

                        // Note: You'll need to add the Glide dependency to your build.gradle:
                        // implementation 'com.github.bumptech.glide:glide:4.15.1'
                    }
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

    private fun setupButtons() {
        // Back button
        backButton.setOnClickListener {
            finish()
        }

        // Edit profile button
        editProfileButton.setOnClickListener {
            Toast.makeText(this, "Edit profile coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Edit profile image button
        editProfileImageButton.setOnClickListener {
            pickImage.launch("image/*")
        }

        // Edit username button
        editUsernameButton.setOnClickListener {
            showEditNameDialog()
        }

        // Edit email button
        editEmailButton.setOnClickListener {
            showEditEmailDialog()
        }
    }

    private fun showEditNameDialog() {
        // Create dialog view manually since we don't have the layout resource
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_name, null)
        val firstNameEditText = dialogView.findViewById<EditText>(R.id.edit_first_name)
        val lastNameEditText = dialogView.findViewById<EditText>(R.id.edit_last_name)

        // Pre-fill with current values
        firstNameEditText.setText(currentFirstName)
        lastNameEditText.setText(currentLastName)

        AlertDialog.Builder(this)
            .setTitle("Edit Name")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newFirstName = firstNameEditText.text.toString().trim()
                val newLastName = lastNameEditText.text.toString().trim()

                if (newFirstName.isNotEmpty()) {
                    updateUserName(newFirstName, newLastName)
                } else {
                    Toast.makeText(this, "First name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditEmailDialog() {
        // Create dialog view manually since we don't have the layout resource
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_email, null)
        val emailEditText = dialogView.findViewById<EditText>(R.id.edit_email)
        val passwordEditText = dialogView.findViewById<EditText>(R.id.edit_password)

        // Pre-fill with current email
        emailEditText.setText(auth.currentUser?.email)

        AlertDialog.Builder(this)
            .setTitle("Edit Email")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, which ->
                val newEmail = emailEditText.text.toString().trim()
                val password = passwordEditText.text.toString()

                if (newEmail.isNotEmpty() && password.isNotEmpty()) {
                    updateUserEmail(newEmail, password)
                } else {
                    Toast.makeText(this, "Email and password are required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateUserName(firstName: String, lastName: String) {
        val currentUser = auth.currentUser ?: return

        val userUpdates = hashMapOf<String, Any>(
            "firstName" to firstName,
            "lastName" to lastName
        )

        db.collection("users").document(currentUser.uid)
            .update(userUpdates)
            .addOnSuccessListener {
                // Update local variables
                currentFirstName = firstName
                currentLastName = lastName

                // Update UI
                usernameText.text = "$firstName $lastName"

                Toast.makeText(this, "Name updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.w("UserProfileActivity", "Error updating name", e)
                Toast.makeText(this, "Failed to update name", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserEmail(newEmail: String, password: String) {
        val currentUser = auth.currentUser ?: return

        // First reauthenticate the user
        // Note: In a production app, you should use FirebaseUser.reauthenticate() method
        // For simplicity, we're directly updating the email

        currentUser.updateEmail(newEmail)
            .addOnSuccessListener {
                // Update UI
                emailText.text = newEmail

                // Update Firestore if needed
                db.collection("users").document(currentUser.uid)
                    .update("email", newEmail)

                Toast.makeText(this, "Email updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.w("UserProfileActivity", "Error updating email", e)
                Toast.makeText(this, "Failed to update email: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadProfileImage(imageUri: Uri) {
        val currentUser = auth.currentUser ?: return
        val storageRef = storage.reference.child("profile_images/${currentUser.uid}")


        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    // Update Firestore with the image URL
                    db.collection("users").document(currentUser.uid)
                        .update("profileImageUrl", uri.toString())
                        .addOnSuccessListener {
                            Toast.makeText(this, "Profile image updated", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e("UserProfileActivity", "Error updating profile image URL", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("UserProfileActivity", "Error uploading profile image", e)
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
    }
}
