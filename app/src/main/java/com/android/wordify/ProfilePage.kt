package com.android.wordify

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProfilePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_page)

        val profile_back_button: Button = findViewById(R.id.profile_button)
        val nameView = findViewById<TextView>(R.id.textView3)
        val app = application as WordifyApplication
        nameView.text = app.getCurrentUsername()

        profile_back_button.setOnClickListener {
            val intent = Intent(this, LandingPage::class.java)
            startActivity(intent)
        }
    }
}
