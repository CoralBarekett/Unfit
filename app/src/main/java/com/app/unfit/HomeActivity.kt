package com.app.unfit

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.app.unfit.model.Post

class HomeActivity : AppCompatActivity() {

    private var posts: MutableList<Post> = mutableListOf()
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: PostsRecyclerView
    private lateinit var recyclerView: RecyclerView

    private val testPosts = listOf(
        Post(
            id = "test1",
            userName = "coral.bareket",
            imageUrl = "https://picsum.photos/400/300?random=1",
            description = "Beautiful sunset view! #nature #evening",
            isLiked = false,
            isSaved = false
        ),
        Post(
            id = "test2",
            userName = "yambalas",
            imageUrl = "https://picsum.photos/400/300?random=2",
            description = "Morning coffee is the best ☕ #coffee #morning",
            isLiked = false,
            isSaved = false
        ),
        Post(
            id = "test3",
            userName = "eliordayari",
            imageUrl = "https://picsum.photos/400/300?random=3",
            description = "Weekend vibes 🎉 #weekend #fun",
            isLiked = false,
            isSaved = false
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        Log.d("HomeActivity", "onCreate started")

        try {
            initializeFirebase()
            setupLogout()
            setupProfileButton()
            setupRecyclerView()
            fetchPosts()

            findViewById<FloatingActionButton>(R.id.home_activity_fab_add_test_data).setOnClickListener {
                addTestPosts()
            }
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error in onCreate", e)
            Toast.makeText(this, "Error initializing Home screen", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupProfileButton() {
        findViewById<ImageButton>(R.id.home_activity_profile_button).setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initializeFirebase() {
        try {
            Log.d("HomeActivity", "Initializing Firebase components")
            auth = FirebaseAuth.getInstance()
            db = FirebaseFirestore.getInstance()

            val currentUser = auth.currentUser
            Log.d("HomeActivity", "Current user: ${currentUser?.email ?: "No user logged in"}")

            if (currentUser == null) {
                Log.d("HomeActivity", "No user found, redirecting to login")
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                return
            }
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error initializing Firebase", e)
            Toast.makeText(
                this,
                "Error initializing app. Please restart.",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }

    private fun setupLogout() {
        findViewById<Button>(R.id.home_activity_logout_button).setOnClickListener {
            // Show loading indicator or disable button
            findViewById<Button>(R.id.home_activity_logout_button).isEnabled = false

            Thread {
                try {
                    Log.d("HomeActivity", "Logging out user")
                    // Sign out from Firebase
                    auth.signOut()

                    // Switch back to main thread for UI operations
                    runOnUiThread {
                        // Navigate back to MainActivity
                        val intent = Intent(this, MainActivity::class.java)
                        // Clear the back stack so user can't go back to HomeActivity
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                } catch (e: Exception) {
                    Log.e("HomeActivity", "Error during logout", e)
                    runOnUiThread {
                        Toast.makeText(
                            this,
                            "Error logging out. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Re-enable the button
                        findViewById<Button>(R.id.home_activity_logout_button).isEnabled = true
                    }
                }
            }.start()
        }
    }

    private fun setupRecyclerView() {
        Log.d("HomeActivity", "Setting up RecyclerView")
        recyclerView = findViewById(R.id.recycler_view_posts)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PostsRecyclerView(posts)
        recyclerView.adapter = adapter
        Log.d("HomeActivity", "RecyclerView setup completed")
    }

    private fun fetchPosts() {
        Log.d("HomeActivity", "Starting to fetch posts")
        db.collection("posts").get()
            .addOnSuccessListener { documents ->
                Log.d("HomeActivity", "Received ${documents.size()} documents from Firestore")
                posts.clear()

                documents.forEach { document ->
                    try {
                        val post = document.toObject(Post::class.java)
                        posts.add(post)
                        Log.d("HomeActivity", "Successfully converted document ${document.id} to Post: $post")
                    } catch (e: Exception) {
                        Log.e("HomeActivity", "Error converting document ${document.id}", e)
                    }
                }

                Log.d("HomeActivity", "Total posts added to list: ${posts.size}")
                adapter.notifyDataSetChanged()

                if (posts.isEmpty()) {
                    Log.d("HomeActivity", "No posts found in database")
                    Toast.makeText(
                        this,
                        "Welcome! No posts available yet. Click + to add test posts!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("HomeActivity", "Error fetching posts", e)
                Toast.makeText(this, "Error loading posts: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun addTestPosts() {
        Log.d("HomeActivity", "Starting to add test posts")

        testPosts.forEach { post ->
            db.collection("posts")
                .document(post.id)
                .set(post)
                .addOnSuccessListener {
                    Log.d("HomeActivity", "Successfully added test post ${post.id}")
                }
                .addOnFailureListener { e ->
                    Log.e("HomeActivity", "Error adding test post ${post.id}", e)
                }
        }

        // Fetch posts again after a short delay to allow for database updates
        Handler(Looper.getMainLooper()).postDelayed({
            fetchPosts()
            Toast.makeText(this, "Test posts added!", Toast.LENGTH_SHORT).show()
        }, 1000)
    }

    override fun onStart() {
        super.onStart()
        // Check authentication state
        if (auth.currentUser == null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("HomeActivity", "onResume called")
        // Refresh posts when activity resumes
        fetchPosts()
    }
}