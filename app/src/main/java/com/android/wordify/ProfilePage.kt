package com.android.wordify

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat

class ProfilePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_page)

        val profile_back_button: Button = findViewById(R.id.profile_button)
        val nameView = findViewById<TextView>(R.id.textView3)
        val app = application as WordifyApplication
        nameView.text = app.getCurrentUsername()

        val gamesPlayedView = findViewById<TextView>(R.id.game_played_edit)
        val winsView = findViewById<TextView>(R.id.wins_edit)
        val currentStreakView = findViewById<TextView>(R.id.current_streak_edit)
        val bestStreakView = findViewById<TextView>(R.id.best_streak_edit)

        val gamesPlayed = app.getGamesPlayed()
        val winsTotal = app.getWinsTotal()
        val currentStreak = app.getCurrentStreak()
        val bestStreak = app.getBestStreak()

        gamesPlayedView.text = gamesPlayed.toString()
        winsView.text = winsTotal.toString()
        currentStreakView.text = currentStreak.toString()
        bestStreakView.text = bestStreak.toString()



        profile_back_button.setOnClickListener {
            val intent = Intent(this, LandingPage::class.java)
            startActivity(intent)
        }
    }

}