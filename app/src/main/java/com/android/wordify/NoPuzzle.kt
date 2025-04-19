package com.android.wordify

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class NoPuzzle : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.no_puzzle_for_today)

        val back = findViewById<Button>(R.id.back_to_landing_today)

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