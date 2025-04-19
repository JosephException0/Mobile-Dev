package com.android.wordify

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterPage : AppCompatActivity() {

    companion object {
        const val EXTRA_USERNAME = "com.android.wordify.USERNAME"
        const val EXTRA_PASSWORD = "com.android.wordify.PASSWORD"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_screen)

        val emailField: EditText = findViewById(R.id.register_email)
        val usernameField: EditText = findViewById(R.id.register_username)
        val passwordField: EditText = findViewById(R.id.register_password)
        val signUpButton: Button = findViewById(R.id.signup_button)
        val backToLoginButton: Button = findViewById(R.id.back_to_login)

        // Handle registration
        signUpButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val username = usernameField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (validateInputs(email, username, password)) {
                // Register user in application
                val app = application as WordifyApplication
                val registerSuccess = app.registerUser(email, username, password)

                if (registerSuccess) {
                    showToast("Registration Successful!")

                    // Save registered users to persistent storage
                    saveRegisteredUsers()

                    // Redirect to LoginPage with username and password
                    val intent = Intent(this, LoginPage::class.java).apply {
                        putExtra(EXTRA_USERNAME, username)
                        putExtra(EXTRA_PASSWORD, password)
                    }
                    startActivity(intent)
                    finish()
                } else {
                    showToast("Username already exists!")
                }
            }
        }

        // Back to login page
        backToLoginButton.setOnClickListener {
            val intent = Intent(this, LoginPage::class.java)
            startActivity(intent)
        }
    }

    // Save registered users to SharedPreferences
    private fun saveRegisteredUsers() {
        // You would implement a more secure way to store users in a real app
        // This is just for demonstration purposes
        val prefs = getSharedPreferences("WordifyUsers", MODE_PRIVATE)
        val editor = prefs.edit()

        // In a real app, you'd encrypt passwords and use a more robust storage solution
        // For now, we'll just save the fact that registration occurred
        editor.putBoolean("has_registered_users", true)
        editor.apply()
    }

    // Validate inputs
    private fun validateInputs(email: String, username: String, password: String): Boolean {
        if (email.isEmpty()) {
            showToast("Email is required")
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Enter a valid email")
            return false
        }

        if (username.isEmpty()) {
            showToast("Username is required")
            return false
        }

        if (password.isEmpty()) {
            showToast("Password is required")
            return false
        }

        if (password.length < 6) {
            showToast("Password must be at least 6 characters")
            return false
        }

        return true
    }

    // Function to show toast messages
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}