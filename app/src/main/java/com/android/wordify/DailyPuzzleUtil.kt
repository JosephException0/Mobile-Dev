package com.android.wordify

import android.content.Context
import android.content.Intent
import java.util.*

object DailyPuzzleUtil {
    private const val PREFS_NAME = "WordifyDailyTimer"
    private const val KEY_LAST_COMPLETED = "last_completed_time"
    private const val KEY_IS_COMPLETED_TODAY = "is_completed_today"
    private const val HOURS_24 = 24 * 60 * 60 * 1000 // 24 hours in milliseconds

    // Check if daily puzzle is completed and redirect if needed
    fun checkAndRedirectIfNeeded(context: Context): Boolean {
        if (isPuzzleCompletedToday(context)) {
            // User already completed today's puzzle, redirect to NoPuzzle
            val intent = Intent(context, NoPuzzle::class.java)
            context.startActivity(intent)
            return true // Redirect happened
        }
        return false // No redirect needed
    }

    // Check if the puzzle has been completed today
    fun isPuzzleCompletedToday(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastCompletedTime = prefs.getLong(KEY_LAST_COMPLETED, 0)
        val isCompletedToday = prefs.getBoolean(KEY_IS_COMPLETED_TODAY, false)

        if (!isCompletedToday) return false

        val currentTime = System.currentTimeMillis()
        val timeElapsed = currentTime - lastCompletedTime

        // Return true if less than 24 hours have passed since completion
        return timeElapsed < HOURS_24
    }

    // Mark puzzle as completed for today
    fun markPuzzleCompleted(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        editor.putLong(KEY_LAST_COMPLETED, System.currentTimeMillis())
        editor.putBoolean(KEY_IS_COMPLETED_TODAY, true)
        editor.apply()
    }

    // Reset daily puzzle status (for testing or admin purposes)
    fun resetDailyPuzzleStatus(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        editor.remove(KEY_LAST_COMPLETED)
        editor.putBoolean(KEY_IS_COMPLETED_TODAY, false)
        editor.apply()
    }

    // Get time remaining until next puzzle in milliseconds
    fun getTimeRemainingForNextPuzzle(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastCompletedTime = prefs.getLong(KEY_LAST_COMPLETED, 0)

        if (lastCompletedTime == 0L) return 0

        val currentTime = System.currentTimeMillis()
        val nextPuzzleTime = lastCompletedTime + HOURS_24

        return Math.max(0, nextPuzzleTime - currentTime)
    }

    // Format remaining time as HH:MM:SS
    fun formatTimeRemaining(timeInMillis: Long): String {
        val seconds = (timeInMillis / 1000) % 60
        val minutes = (timeInMillis / (1000 * 60)) % 60
        val hours = (timeInMillis / (1000 * 60 * 60)) % 24

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}