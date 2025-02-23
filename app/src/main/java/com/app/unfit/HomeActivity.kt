package com.app.unfit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()

        val welcomeText = findViewById<TextView>(R.id.home_activity_welcome_text)
        val logoutButton = findViewById<Button>(R.id.home_activity_logout_button)

        // Show user's email
        val user = auth.currentUser
        welcomeText.text = "Welcome, ${user?.email}"

        // Logout Button
        logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Close HomeActivity
        }
    }
}