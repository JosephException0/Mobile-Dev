package com.android.wordify

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class HelpPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.help_page)
        val back = findViewById<ImageButton>(R.id.back_button_help)

        back.setOnClickListener {
            startActivity(Intent(this, LandingPage::class.java))
        }
    }
}
