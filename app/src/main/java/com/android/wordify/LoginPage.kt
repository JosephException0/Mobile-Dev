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

        val username = intent.getStringExtra(RegisterPage.EXTRA_USERNAME)
        val password = intent.getStringExtra(RegisterPage.EXTRA_PASSWORD)


        if (!username.isNullOrEmpty()) {
            usernameField.setText(username)
        }

        if (!password.isNullOrEmpty()) {
            passwordField.setText(password)
        }

        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterPage::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            val enteredUsername = usernameField.text.toString().trim()
            val enteredPassword = passwordField.text.toString().trim()

            if (validateLogin(enteredUsername, enteredPassword)) {
                val intent = Intent(this, LandingPage::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun validateLogin(username: String, password: String): Boolean {
        if (username.isEmpty()) {
            showToast("Username is required")
            return false
        }

        if (password.isEmpty()) {
            showToast("Password is required")
            return false
        }

        val app = application as WordifyApplication
        val loginSuccess = app.loginUser(username, password)

        if (!loginSuccess) {
            showToast("Invalid username or password")
            return false
        }

        showToast("Welcome, ${app.getCurrentUsername()}!")
        return true
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}