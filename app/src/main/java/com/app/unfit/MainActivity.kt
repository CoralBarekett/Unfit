package com.app.unfit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // ✅ Check if user is already logged in and redirect to HomeActivity
        if (auth.currentUser != null) {
            navigateToHome()
        }

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Setup UI Elements
        val emailInput = findViewById<EditText>(R.id.main_activity_email_input_edit_text)
        val passwordInput = findViewById<EditText>(R.id.main_activity_password_input_edit_text)
        val signupButton = findViewById<Button>(R.id.main_activity_signUp_button)
        val loginButton = findViewById<Button>(R.id.main_activity_login_button)
        val googleSignInButton = findViewById<Button>(R.id.main_activity_signIn_button)

        // Handle Email Signup
        signupButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isNotEmpty() && password.length >= 6) {
                registerUser(email, password)
            } else {
                Toast.makeText(this, "Enter a valid email and password (min 6 chars)", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle Email Login
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Enter your email and password", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle Google Sign-In
        googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }

        // Adjust layout for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // ✅ Redirect to HomeActivity if user is already logged in
    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish() // Prevent user from going back to login screen
    }

    // 🔹 Email & Password Signup
    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT).show()
                    Log.d("FirebaseAuth", "Signup successful")
                    navigateToHome() // ✅ Redirect to Home after signup
                } else {
                    Toast.makeText(this, "Signup failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    Log.w("FirebaseAuth", "Signup failed", task.exception)
                }
            }
    }

    // 🔹 Email & Password Login
    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    Log.d("FirebaseAuth", "Login successful")
                    navigateToHome() // ✅ Redirect to Home after login
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    Log.w("FirebaseAuth", "Login failed", task.exception)
                }
            }
    }

    // 🔹 Google Sign-In Methods
    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w("GoogleSignIn", "Google sign in failed", e)
            }
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseAuth", "Sign in successful")
                    navigateToHome() // ✅ Redirect to Home after Google login
                } else {
                    Log.w("FirebaseAuth", "Sign in failed", task.exception)
                }
            }
    }
}