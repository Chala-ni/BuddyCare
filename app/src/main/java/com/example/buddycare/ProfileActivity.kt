package com.example.buddycare

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.room.Room
import com.example.buddycare.classes.User
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*

class ProfileActivity : AppCompatActivity() {
    private lateinit var emailEditText: TextInputEditText
    private lateinit var nameEditText: TextInputEditText
    private lateinit var addressEditText: TextInputEditText
    private lateinit var phoneEditText: TextInputEditText
    private lateinit var currentPasswordEditText: TextInputEditText
    private lateinit var newPasswordEditText: TextInputEditText
    private lateinit var confirmNewPasswordEditText: TextInputEditText
    private lateinit var changePasswordButton: MaterialButton
    private lateinit var updateAccountButton: MaterialButton
    private lateinit var db: AppDatabase
    private lateinit var currentUser: User
    private lateinit var paymentMethodSpinner: Spinner
    private lateinit var paymentMethods: Array<String>
    private var isEditing = false
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "buddy-nutrition-db")
            .allowMainThreadQueries()
            .build()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sharedPreferences = getSharedPreferences("user_preferences", MODE_PRIVATE)

        // Initialize UI elements
        emailEditText = findViewById(R.id.email)
        nameEditText = findViewById(R.id.name)
        addressEditText = findViewById(R.id.address)
        phoneEditText = findViewById(R.id.phone)
        currentPasswordEditText = findViewById(R.id.current_password)
        newPasswordEditText = findViewById(R.id.new_password)
        confirmNewPasswordEditText = findViewById(R.id.confirm_new_password)
        paymentMethodSpinner = findViewById(R.id.payment_method_spinner)
        changePasswordButton = findViewById(R.id.change_password_button)
        updateAccountButton = findViewById(R.id.update_account_button)

        paymentMethods = arrayOf("Cash", "Card")
        val adapter = SpinnerArray(this, paymentMethods)
        paymentMethodSpinner.adapter = adapter

        // Get userId from intent
        val userId = sharedPreferences.getInt("userId", 0)
        if (userId == 0) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Fetch user data from database on a background thread using coroutine
        CoroutineScope(Dispatchers.IO).launch {
            currentUser = db.userDao().getUserById(userId)!!
            withContext(Dispatchers.Main) {
                if (currentUser != null) {
                    populateUserData(currentUser)
                } else {
                    Toast.makeText(this@ProfileActivity, "User not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        isEditing = false

        // Change password button click listener
        changePasswordButton.setOnClickListener {
            toggleChangePasswordFields()
        }

        // Update account details button click listener
        updateAccountButton.setOnClickListener {
            if (isEditing) {
                if (validateInput()) {
                    updateUserData()
                    disableEditFields()
                    updateAccountButton.text = "Update Account Details"
                    isEditing = false
                }
            } else {
                enableEditFields()
                updateAccountButton.text = "Save"
                isEditing = true
            }
        }
    }

    // Populate user data in UI
    private fun populateUserData(user: User) {
        emailEditText.setText(user.email)
        nameEditText.setText(user.name)
        addressEditText.setText(user.address)
        phoneEditText.setText(user.phoneNumber)
        if (user.paymentMethod != null) {
            val position = getSpinnerPosition(user.paymentMethod)
            paymentMethodSpinner.setSelection(position)
        }
    }

    private fun getSpinnerPosition(paymentMethod: String): Int {
        for (i in paymentMethods.indices) {
            if (paymentMethods[i] == paymentMethod) {
                return i
            }
        }
        return 0
    }

    // Toggle change password fields visibility
    private fun toggleChangePasswordFields() {
        val changePasswordFields = findViewById<View>(R.id.change_password_fields)
        changePasswordFields.visibility = if (changePasswordFields.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    // Enable edit fields
    private fun enableEditFields() {
        emailEditText.isEnabled = true
        nameEditText.isEnabled = true
        addressEditText.isEnabled = true
        phoneEditText.isEnabled = true
        paymentMethodSpinner.isEnabled = true
    }

    // Disable edit fields
    private fun disableEditFields() {
        emailEditText.isEnabled = false
        nameEditText.isEnabled = false
        addressEditText.isEnabled = false
        phoneEditText.isEnabled = false
        paymentMethodSpinner.isEnabled = false
    }

    // Validate user input for update
    private fun validateInput(): Boolean {
        var isValid = true

        val email = emailEditText.text.toString().trim()
        val name = nameEditText.text.toString().trim()
        val address = addressEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()
        val currentPassword = currentPasswordEditText.text.toString().trim()
        val newPassword = newPasswordEditText.text.toString().trim()
        val confirmNewPassword = confirmNewPasswordEditText.text.toString().trim()

        if (email.isEmpty()) {
            emailEditText.error = "Email cannot be empty"
            isValid = false
        } else if (!isValidEmail(email)) {
            emailEditText.error = "Invalid email address"
            isValid = false
        }

        if (name.isEmpty()) {
            nameEditText.error = "Name cannot be empty"
            isValid = false
        }

        if (address.isEmpty()) {
            addressEditText.error = "Address cannot be empty"
            isValid = false
        }

        if (phone.isEmpty()) {
            phoneEditText.error = "Phone number cannot be empty"
            isValid = false
        } else if (!isValidPhone(phone)) {
            phoneEditText.error = "Invalid phone number"
            isValid = false
        }

        if (currentPassword.isNotEmpty() && newPassword.isNotEmpty() && confirmNewPassword.isNotEmpty()) {
            if (currentPassword != currentUser.password) {
                currentPasswordEditText.error = "Incorrect current password"
                isValid = false
            } else if (newPassword != confirmNewPassword) {
                newPasswordEditText.error = "New passwords don't match"
                confirmNewPasswordEditText.error = "New passwords don't match"
                isValid = false
            } else if (newPassword.length < 8) {
                newPasswordEditText.error = "New password must be at least 8 characters"
                isValid = false
            }
        }

        return isValid
    }

    // Validate email address
    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}".toRegex()
        return email.matches(emailPattern)
    }

    // Validate phone number
    private fun isValidPhone(phone: String): Boolean {
        val phonePattern = "[0-9]{10,13}".toRegex() // Assuming phone number can be 10-13 digits
        return phone.matches(phonePattern)
    }

    // Update user data in database
    private fun updateUserData() {
        val newEmail = emailEditText.text.toString().trim()
        val newName = nameEditText.text.toString().trim()
        val newAddress = addressEditText.text.toString().trim()
        val newPhone = phoneEditText.text.toString().trim()
        val newPaymentMethod = paymentMethods[paymentMethodSpinner.selectedItemPosition]

        // Update user object
        currentUser.email = newEmail
        currentUser.name = newName
        currentUser.address = newAddress
        currentUser.phoneNumber = newPhone
        currentUser.paymentMethod = newPaymentMethod

        // Update password if changed
        val currentPassword = currentPasswordEditText.text.toString().trim()
        val newPassword = newPasswordEditText.text.toString().trim()
        val confirmNewPassword = confirmNewPasswordEditText.text.toString().trim()

        if (newPassword.isNotEmpty() && confirmNewPassword.isNotEmpty()) {
            if (currentPassword == currentUser.password && newPassword == confirmNewPassword) {
                currentUser.password = newPassword
            } else {
                Toast.makeText(this, "Incorrect current password or passwords don't match", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Update user data in the database on a background thread using coroutine
        CoroutineScope(Dispatchers.IO).launch {
            db.userDao().updateUser(currentUser)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ProfileActivity, "Account details updated successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }
}