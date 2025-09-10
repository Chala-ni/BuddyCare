package com.example.buddycare

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MenuActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var floatAnimation: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
        sharedPreferences = getSharedPreferences("user_preferences", MODE_PRIVATE)


        // Set up animation
        floatAnimation = AnimationUtils.loadAnimation(this, R.anim.float_up)

        // Set up click listeners for icons
        findViewById<View>(R.id.user_icon).setOnClickListener { onUserIconClick() }
        findViewById<View>(R.id.cart_icon).setOnClickListener { onCartIconClick() }

        // Set up click listeners for buttons
        findViewById<View>(R.id.button_1).setOnClickListener { onButton1Click() }
        findViewById<View>(R.id.button_2).setOnClickListener { onButton2Click() }
        findViewById<View>(R.id.button_3).setOnClickListener { onButton3Click() }
        findViewById<View>(R.id.button_4).setOnClickListener { onButton4Click() }

        findViewById<View>(R.id.logout_button).setOnClickListener { onLogoutButtonClick() }



        // Hide top bar for admin
        val userType = sharedPreferences.getString("usertype", "")
        val topBar = findViewById<ConstraintLayout>(R.id.top_bar)
        if (userType == "admin") {
            topBar.visibility = View.GONE // Hide top bar for admin
        }


        // Set up window insets listener
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun onLogoutButtonClick() {
        // Clear shared preferences (user data)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        // Navigate to login activity
        findViewById<View>(R.id.logout_button).startAnimation(floatAnimation)
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish() // Finish MenuActivity to prevent going back
    }

    // Function to handle user icon click
    private fun onUserIconClick() {
        // TODO: Implement user icon click action
        findViewById<View>(R.id.user_icon).startAnimation(floatAnimation)
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    // Function to handle cart icon click
    private fun onCartIconClick() {
        // TODO: Implement cart icon click action
        findViewById<View>(R.id.cart_icon).startAnimation(floatAnimation)
        val intent = Intent(this, ItemCartActivity::class.java)
        startActivity(intent)
    }

    // Function to handle button 1 click
    private fun onButton1Click() {
        // TODO: Implement button 1 click action
        findViewById<View>(R.id.button_1).startAnimation(floatAnimation)
        val userType = sharedPreferences.getString("usertype", "")

        if (userType == "admin") {
            startActivity(Intent(this, ProductActivity::class.java))
        } else {
            startActivity(Intent(this, BuyItemActivity::class.java))
        }
    }

    // Function to handle button 2 click
    private fun onButton2Click() {
        // TODO: Implement button 2 click action
        findViewById<View>(R.id.button_2).startAnimation(floatAnimation)
        val intent = Intent(this, BillActivity::class.java)
        startActivity(intent)
    }

    // Function to handle button 3 click
    private fun onButton3Click() {
        // TODO: Implement button 3 click action
        findViewById<View>(R.id.button_3).startAnimation(floatAnimation)
        val intent = Intent(this, ReviewActivity::class.java)
        startActivity(intent)
    }

    // Function to handle button 4 click
    private fun onButton4Click() {
        // Implement button 4 click action
        findViewById<View>(R.id.button_4).startAnimation(floatAnimation)
        val intent = Intent(this, BlogActivity::class.java)
        startActivity(intent)
    }
}