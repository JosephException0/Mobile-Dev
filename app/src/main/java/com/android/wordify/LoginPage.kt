package com.android.wordify

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_screen)

        val usernameField: EditText = findViewById(R.id.login_username)
        val passwordField: EditText = findViewById(R.id.login_password)
        val loginButton: Button = findViewById(R.id.login_button)
        val registerButton: Button = findViewById(R.id.going_to_register)

        // Navigate to Register Page
        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterPage::class.java)
            startActivity(intent)
        }

        // Handle Login Button Click
        loginButton.setOnClickListener {
            val username = usernameField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (validateLogin(username, password)) {
                // Proceed to landing page
                val intent = Intent(this, LandingPage::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    // Validate Login Inputs
    private fun validateLogin(username: String, password: String): Boolean {
        if (username.isEmpty()) {
            showToast("Username is required")
            return false
        }

        if (password.isEmpty()) {
            showToast("Password is required")
            return false
        }

        if (username != RegisterPage.registeredUsername || password != RegisterPage.registeredPassword) {
            showToast("Invalid username or password")
            return false
        }

        return true
    }

    // Function to show toast messages
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
