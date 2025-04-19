package com.android.wordify

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class NoPuzzle : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.no_puzzle_for_today)

        val back = findViewById<Button>(R.id.back_to_landing_today)
        val sharedPreferences = getSharedPreferences("Mode", Context.MODE_PRIVATE)
        val nightMode = sharedPreferences.getBoolean("night", false)

        if (nightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        back.setOnClickListener {
            finish()
        }

        val seeStats = findViewById<Button>(R.id.see_stats)

        seeStats.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


    }
}