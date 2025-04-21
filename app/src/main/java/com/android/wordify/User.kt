package com.android.wordify

import android.app.Application
import android.content.Context

class WordifyApplication : Application() {
    // Current logged-in user info
    var currentUserId: String = ""
    var currentUsername1: String = "Guest"

    // Guest account constants
    private val GUEST_ID = "guest_user"
    private val GUEST_USERNAME = "Guest"

    // Store list of registered users
    private val users = mutableMapOf<String, UserData>()

    // Statistics constants
    private val STATS_PREFS_BASE = "WordifyUserStats"
    private val KEY_GAMES_PLAYED = "games_played"
    private val KEY_WINS_TOTAL = "wins_total"
    private val KEY_CURRENT_STREAK = "current_streak"
    private val KEY_BEST_STREAK = "best_streak"

    // User storage constants
    private val USERS_PREFS = "WordifyUsers"
    private val USER_PREFIX = "user_"
    private val EMAIL_INDEX = 0
    private val PASSWORD_INDEX = 1

    override fun onCreate() {
        super.onCreate()
        // Create guest account
        createGuestAccount()
        // Load saved users from SharedPreferences
        loadSavedUsers()
        // Try to restore previous login state
        if (!restoreLoginState()) {
            // If no saved login, default to guest account
            switchToGuestAccount()
        }
    }

    private fun createGuestAccount() {
        // Create a guest account that is always available but not saved to SharedPreferences
        val guestData = UserData(GUEST_ID, GUEST_USERNAME, "", "guest@wordify.app")
        users[GUEST_ID] = guestData
    }

    fun switchToGuestAccount() {
        // Switch to guest account without clearing saved login state
        currentUserId = GUEST_ID
        currentUsername1 = GUEST_USERNAME
    }

    private fun loadSavedUsers() {
        val prefs = getSharedPreferences(USERS_PREFS, Context.MODE_PRIVATE)
        // Get all saved preferences
        val allEntries = prefs.all

        // Look for user entries
        for ((key, value) in allEntries) {
            if (key.startsWith(USER_PREFIX)) {
                val username = key.substring(USER_PREFIX.length) // Remove "user_" prefix
                val parts = (value as String).split(",")

                if (parts.size == 2) {
                    val email = parts[EMAIL_INDEX]
                    val password = parts[PASSWORD_INDEX]

                    // Add to users map
                    users[email] = UserData(email, username, password, email)
                }
            }
        }
    }

    fun registerUser(email: String, username: String, password: String): Boolean {
        // Check if username already exists
        if (users.values.any { it.username == username && it.userId != GUEST_ID }) {
            return false
        }

        // Create unique user ID (email can be used as ID)
        val userId = email

        // Create new user data
        val userData = UserData(userId, username, password, email)

        // Add to users map
        users[userId] = userData

        // Save user to persistent storage
        saveUser(username, email, password)

        return true
    }

    private fun saveUser(username: String, email: String, password: String) {
        val prefs = getSharedPreferences(USERS_PREFS, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // Save user details (email,password)
        editor.putString("$USER_PREFIX$username", "$email,$password")
        editor.apply()
    }

    fun loginUser(username: String, password: String): Boolean {
        // Find user by username
        val user = users.values.find { it.username == username }

        // Check if user exists and password matches
        if (user != null && user.password == password) {
            // Set current user
            currentUserId = user.userId
            currentUsername1 = user.username

            // Save current user login state
            saveLoginState(user.userId, user.username)
            return true
        }

        return false
    }

    private fun saveLoginState(userId: String, username: String) {
        // Don't save guest account as logged in
        if (userId == GUEST_ID) {
            return
        }

        val prefs = getSharedPreferences(USERS_PREFS, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // Save logged in user info
        editor.putString("current_user_id", userId)
        editor.putString("current_username", username)
        editor.apply()
    }

    // Check for saved login state and restore it
    fun restoreLoginState(): Boolean {
        val prefs = getSharedPreferences(USERS_PREFS, Context.MODE_PRIVATE)
        val savedUserId = prefs.getString("current_user_id", "")
        val savedUsername = prefs.getString("current_username", "")

        if (!savedUserId.isNullOrEmpty() && !savedUsername.isNullOrEmpty()) {
            currentUserId = savedUserId
            currentUsername1 = savedUsername
            return true
        }

        return false
    }

    fun getCurrentUsername(): String {
        return currentUsername1
    }

    fun isUserLoggedIn(): Boolean {
        return currentUserId.isNotEmpty() && currentUserId != GUEST_ID
    }

    fun isGuestUser(): Boolean {
        return currentUserId == GUEST_ID
    }

    fun logoutUser() {
        // Instead of clearing completely, switch to guest account
        switchToGuestAccount()

        // Clear login state in SharedPreferences
        val prefs = getSharedPreferences(USERS_PREFS, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.remove("current_user_id")
        editor.remove("current_username")
        editor.apply()
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