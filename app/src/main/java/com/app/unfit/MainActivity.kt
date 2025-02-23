package com.app.unfit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // Auto-redirect if user is logged in
        if (auth.currentUser != null) {
            navigateToHome()
        }

        // Setup Buttons
        val signUpButton = findViewById<Button>(R.id.main_activity_signup_button)
        val loginButton = findViewById<Button>(R.id.main_activity_login_button)

        // Navigate to Sign Up Activity
        signUpButton.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        // Navigate to Login Activity
        loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    // Redirect logged-in users to home screen
    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}