package com.android.wordify

import android.app.Application
import android.content.Context

class WordifyApplication : Application() {
    // Current logged-in user info
    var currentUserId: String = ""
    var currentUsername1: String = "Guest"

    // Store list of registered users
    private val users = mutableMapOf<String, UserData>()

    // Statistics constants
    private val STATS_PREFS_BASE = "WordifyUserStats"
    private val KEY_GAMES_PLAYED = "games_played"
    private val KEY_WINS_TOTAL = "wins_total"
    private val KEY_CURRENT_STREAK = "current_streak"
    private val KEY_BEST_STREAK = "best_streak"

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

    // Get user statistics preference name
    private fun getStatsPreferenceName(): String {
        return getUserPreferenceName(STATS_PREFS_BASE)
    }

    // Statistics functions
    fun getGamesPlayed(): Int {
        val prefs = getApplicationContext().getSharedPreferences(getStatsPreferenceName(), Context.MODE_PRIVATE)
        return prefs.getInt(KEY_GAMES_PLAYED, 0)
    }

    fun getWinsTotal(): Int {
        val prefs = getApplicationContext().getSharedPreferences(getStatsPreferenceName(), Context.MODE_PRIVATE)
        return prefs.getInt(KEY_WINS_TOTAL, 0)
    }

    fun getCurrentStreak(): Int {
        val prefs = getApplicationContext().getSharedPreferences(getStatsPreferenceName(), Context.MODE_PRIVATE)
        return prefs.getInt(KEY_CURRENT_STREAK, 0)
    }

    fun getBestStreak(): Int {
        val prefs = getApplicationContext().getSharedPreferences(getStatsPreferenceName(), Context.MODE_PRIVATE)
        return prefs.getInt(KEY_BEST_STREAK, 0)
    }

    fun recordGamePlayed(isWin: Boolean) {
        val prefs = getApplicationContext().getSharedPreferences(getStatsPreferenceName(), Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // Increment games played
        val gamesPlayed = prefs.getInt(KEY_GAMES_PLAYED, 0) + 1
        editor.putInt(KEY_GAMES_PLAYED, gamesPlayed)

        if (isWin) {
            // Increment total wins
            val winsTotal = prefs.getInt(KEY_WINS_TOTAL, 0) + 1
            editor.putInt(KEY_WINS_TOTAL, winsTotal)

            // Increment current streak
            val currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0) + 1
            editor.putInt(KEY_CURRENT_STREAK, currentStreak)

            // Update best streak if needed
            val bestStreak = prefs.getInt(KEY_BEST_STREAK, 0)
            if (currentStreak > bestStreak) {
                editor.putInt(KEY_BEST_STREAK, currentStreak)
            }
        } else {
            // Reset current streak on loss
            editor.putInt(KEY_CURRENT_STREAK, 0)
        }

        editor.apply()
    }

    // Reset all statistics (for testing)
    fun resetAllStats() {
        val prefs = getApplicationContext().getSharedPreferences(getStatsPreferenceName(), Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putInt(KEY_GAMES_PLAYED, 0)
        editor.putInt(KEY_WINS_TOTAL, 0)
        editor.putInt(KEY_CURRENT_STREAK, 0)
        editor.putInt(KEY_BEST_STREAK, 0)
        editor.apply()
    }
}

// Data class to store user information
data class UserData(
    val userId: String,
    val username: String,
    val password: String,
    val email: String
)