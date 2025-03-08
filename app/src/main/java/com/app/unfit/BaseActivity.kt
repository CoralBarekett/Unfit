package com.app.unfit

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

/**
 * Base activity that handles the common Instagram-style bottom navigation bar
 * All activities that need the bottom navigation should extend this class
 */
abstract class BaseActivity : AppCompatActivity() {

    // Navigation buttons
    private lateinit var navHome: ImageButton
    private lateinit var navSearch: ImageButton
    private lateinit var navAdd: ImageButton
    private lateinit var navActivity: ImageButton
    private lateinit var navProfile: ImageButton

    // Content container
    private lateinit var contentContainer: FrameLayout

    // Flag to control if the bottom navigation should be shown
    private var showBottomNav = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

        // Initialize the content container
        contentContainer = findViewById(R.id.base_content_container)

        // Check if activity wants to show the bottom navigation
        showBottomNav = shouldShowBottomNav()

        // Initialize the bottom navigation
        if (showBottomNav) {
            setupBottomNavigation()
        } else {
            findViewById<View>(R.id.bottom_navigation).visibility = View.GONE
        }

        // Inflate the activity's layout into the content container
        View.inflate(this, getContentLayoutId(), contentContainer)
    }

    /**
     * Setup the bottom navigation bar
     */
    private fun setupBottomNavigation() {
        // Initialize navigation buttons
        navHome = findViewById(R.id.nav_home)
        navSearch = findViewById(R.id.nav_search)
        navAdd = findViewById(R.id.nav_add)
        navActivity = findViewById(R.id.nav_activity)
        navProfile = findViewById(R.id.nav_profile)

        // Reset all icons to their default state
        navHome.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_home))
        navSearch.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_search))
        navAdd.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_add))
        navActivity.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_activity))
        navProfile.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_profile))

        // Highlight the current tab
        when (getNavigationMenuItemId()) {
            R.id.nav_home -> navHome.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_home_filled))
            R.id.nav_search -> navSearch.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_search_filled))
            R.id.nav_add -> navAdd.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_add_filled))
            R.id.nav_activity -> navActivity.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_activity_filled))
            R.id.nav_profile -> navProfile.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_profile_filled))
        }

        // Set click listeners
        navHome.setOnClickListener { navigateTo(HomeActivity::class.java) }
        navSearch.setOnClickListener { navigateTo(SearchActivity::class.java) }
        navAdd.setOnClickListener { navigateTo(AddPostActivity::class.java) }
        navActivity.setOnClickListener { navigateTo(ActivityFeedActivity::class.java) }
        navProfile.setOnClickListener { navigateTo(UserProfileActivity::class.java) }
    }

    /**
     * Navigate to the specified activity if we're not already on it
     */
    private fun navigateTo(targetActivity: Class<out AppCompatActivity>) {
        if (this.javaClass == targetActivity) {
            return // Already on this screen
        }

        val intent = Intent(this, targetActivity)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    /**
     * Each activity must provide its content layout resource ID
     */
    abstract fun getContentLayoutId(): Int

    /**
     * Each activity specifies which navigation item should be highlighted
     */
    abstract fun getNavigationMenuItemId(): Int

    /**
     * Activities can override this to hide the bottom navigation
     */
    open fun shouldShowBottomNav(): Boolean {
        return true
    }

    /**
     * Stub classes for activities that might not exist yet
     * Create these activities as needed
     */
    class SearchActivity : BaseActivity() {
        override fun getContentLayoutId() = R.layout.activity_search
        override fun getNavigationMenuItemId() = R.id.nav_search
    }

    class AddPostActivity : BaseActivity() {
        override fun getContentLayoutId() = R.layout.activity_add_post
        override fun getNavigationMenuItemId() = R.id.nav_add
    }

    class ActivityFeedActivity : BaseActivity() {
        override fun getContentLayoutId() = R.layout.activity_feed
        override fun getNavigationMenuItemId() = R.id.nav_activity
    }
}
