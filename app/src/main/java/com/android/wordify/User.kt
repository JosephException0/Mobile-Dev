package com.android.wordify

import android.app.Application

// Rename to WordifyApplication for clarity
class WordifyApplication : Application() {
    // Current logged-in user info
    var currentUserId: String = ""
    var currentUsername1: String = "Guest"

    // Store list of registered users
    private val users = mutableMapOf<String, UserData>()

    fun registerUser(email: String, username: String, password: String): Boolean {
        // Check if username already exists
        if (users.values.any { it.username == username }) {
            return false
        }

        // Create unique user ID (email can be used as ID)
        val userId = email

        // Create new user data
        val userData = UserData(userId, username, password, email)

        // Add to users map
        users[userId] = userData

        return true
    }

    fun loginUser(username: String, password: String): Boolean {
        // Find user by username
        val user = users.values.find { it.username == username }

        // Check if user exists and password matches
        if (user != null && user.password == password) {
            // Set current user
            currentUserId = user.userId
            currentUsername1 = user.username
            return true
        }

        return false
    }

    fun getCurrentUsername(): String {
        return currentUsername1
    }

    fun isUserLoggedIn(): Boolean {
        return currentUserId.isNotEmpty()
    }

    fun logoutUser() {
        currentUserId = ""
        currentUsername1 = "Guest"
    }

    // Get user-specific preference name
    fun getUserPreferenceName(baseName: String): String {
        return if (currentUserId.isNotEmpty()) {
            "${baseName}_${currentUserId}"
        } else {
            baseName
        }
    }
}

// Data class to store user information
data class UserData(
    val userId: String,
    val username: String,
    val password: String,
    val email: String
)
