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
        var registeredUsername: String? = null
        var registeredPassword: String? = null
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
                registeredUsername = username
                registeredPassword = password
                showToast("Registration Successful!")

                // Redirect to LoginPage
                val intent = Intent(this, LoginPage::class.java)
                startActivity(intent)
                finish()
            }
        }

        // Back to login page
        backToLoginButton.setOnClickListener {
            val intent = Intent(this, LoginPage::class.java)
            startActivity(intent)
        }
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
