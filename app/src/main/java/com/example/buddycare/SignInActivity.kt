package com.example.buddycare

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.room.Room
import com.example.buddycare.classes.User
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*

class SignInActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var emailIconImageView: ImageView
    private lateinit var passwordIconImageView: ImageView

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.password)
        loginButton = findViewById(R.id.login_button)
        registerButton = findViewById(R.id.register_button)
        emailIconImageView = findViewById(R.id.email_icon)
        passwordIconImageView = findViewById(R.id.password_icon)
        val sharedPreferences = getSharedPreferences("user_preferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Initialize the database
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "buddy-nutrition-db")
            .allowMainThreadQueries() // This is for demo purposes only, do not use on the main thread in production
            .build()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (!isValidEmail(email)) {
                emailEditText.error = "Invalid email address"
            } else {
                emailEditText.error = null
            }

            if (!isValidPassword(password)) {
                passwordEditText.error = "Password must be at least 8 characters long"
            } else {
                passwordEditText.error = null
            }

            if (isValidEmail(email) && isValidPassword(password)) {
                if (email == "admin@admin.com" && password == "admin123") {
                    showAlertDialog("Admin Login", "Admin login successful!", "OK")
                    // Save user ID and user type to SharedPreferences
                    editor.putInt("userId", 0)
                    editor.putString("usertype", "admin")
                    editor.apply()
                    // Handle admin login
                    val intent = Intent(this, MenuActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        val user = db.userDao().getUserByEmail(email)
                        withContext(Dispatchers.Main) {
                            if (user != null && user.password == password) {
                                showAlertDialog("Login", "Login successful!", "OK")
                                // Save user ID and user type to SharedPreferences
                                editor.putInt("userId", user.id)
                                editor.putString("usertype", "customer")
                                editor.apply()
                                // Handle user login
                                val intent = Intent(this@SignInActivity, MenuActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                showAlertDialog("Error", "Invalid credentials", "OK")
                            }
                        }
                    }
                }
            }
        }

        val intent = Intent(this, SignUpActivity::class.java)
        registerButton.setOnClickListener {
            startActivity(intent)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
        return email.matches(emailRegex.toRegex())
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }

    private fun showAlertDialog(title: String, message: String, positiveButtonText: String) {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(positiveButtonText) { dialog, _ ->
            // Dismiss the dialog
            dialog.dismiss()
        }
        builder.show()
    }
}