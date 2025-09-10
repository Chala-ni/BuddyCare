package com.example.buddycare

import android.content.Intent
import android.os.Bundle
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.room.Room
import com.example.buddycare.classes.User
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*

class SignUpActivity : AppCompatActivity() {

    private lateinit var emailEditText: TextInputEditText
    private lateinit var nameEditText: TextInputEditText
    private lateinit var addressEditText: TextInputEditText
    private lateinit var phoneEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var paymentMethodSpinner: Spinner
    private lateinit var registerButton: MaterialButton
    private lateinit var loginButton: MaterialButton
    private lateinit var db: AppDatabase // Declare a variable for the database instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)

        // Initialize the database instance
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "buddy-nutrition-db")
            .allowMainThreadQueries() // Allow database operations on the main thread (for simplicity)
            .build()

        // Set up onApplyWindowInsetsListener for system bars padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize UI elements
        emailEditText = findViewById(R.id.email)
        nameEditText = findViewById(R.id.name)
        addressEditText = findViewById(R.id.address)
        phoneEditText = findViewById(R.id.phone)
        passwordEditText = findViewById(R.id.password)
        confirmPasswordEditText = findViewById(R.id.confirm_password)
        paymentMethodSpinner = findViewById(R.id.payment_method_spinner)
        registerButton = findViewById(R.id.register_button)
        loginButton = findViewById(R.id.login_button)

        // Set up payment method spinner
        val paymentMethods = arrayOf("Cash", "Card")
        val adapter = SpinnerArray(this, paymentMethods)
        paymentMethodSpinner.adapter = adapter

        // Set up register button click listener
        registerButton.setOnClickListener {
            if (validateInput()) {
                // Get user input
                val email = emailEditText.text.toString().trim()
                val name = nameEditText.text.toString().trim()
                val address = addressEditText.text.toString().trim()
                val phone = phoneEditText.text.toString().trim()
                val password = passwordEditText.text.toString().trim()
                val paymentMethod = paymentMethods[paymentMethodSpinner.selectedItemPosition]

                // Create User object and register
                val user = User(email, name, address, phone, password, paymentMethod)
                registerUser(user)
            }
        }
        val intent = Intent(this, SignInActivity::class.java)
        loginButton.setOnClickListener {
            startActivity(intent)
        }
    }

    // Function to validate user input
    private fun validateInput(): Boolean {
        var isValid = true

        // Email validation
        val email = emailEditText.text.toString().trim()
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Please enter a valid email address"
            isValid = false
        } else {
            emailEditText.error = null
        }

        // Name validation
        val name = nameEditText.text.toString().trim()
        if (name.isEmpty()) {
            nameEditText.error = "Please enter your name"
            isValid = false
        } else {
            nameEditText.error = null
        }

        // Address validation
        val address = addressEditText.text.toString().trim()
        if (address.isEmpty()) {
            addressEditText.error = "Please enter your address"
            isValid = false
        } else {
            addressEditText.error = null
        }

        // Phone number validation
        val phone = phoneEditText.text.toString().trim()
        if (phone.isEmpty()) {
            phoneEditText.error = "Please enter your phone number"
            isValid = false
        } else {
            phoneEditText.error = null
        }

        // Password validation
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()
        if (password.isEmpty() || password.length < 8) {
            passwordEditText.error = "Password must be at least 8 characters long"
            isValid = false
        } else if (password != confirmPassword) {
            confirmPasswordEditText.error = "Passwords do not match"
            isValid = false
        } else {
            passwordEditText.error = null
            confirmPasswordEditText.error = null
        }

        return isValid
    }

    // Function to register user
    private fun registerUser(user: User) {
        CoroutineScope(Dispatchers.IO).launch {
            // Check if the email already exists in the database
            val existingUser = db.userDao().getUserByEmail(user.email)
            if (existingUser != null) {
                // Email already exists, show a dialog box
                withContext(Dispatchers.Main) {
                    showAlertDialog("Email Already Exists", "An account with this email address already exists. Please try a different email address.", "ok")
                }
            } else {
                // Insert the new user into the database
                db.userDao().insertUser(user)
                // Update UI on the main thread:
                withContext(Dispatchers.Main) {
                    // Show a success message or navigate to the next activity
                    showAlertDialog("Success", "User registered successfully!", "ok")
                }
            }
        }
    }

    private fun showAlertDialog(title: String, message: String, positiveButtonText: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButtonText) { dialog, which ->
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
                finish()
            }
            .show()
    }
}