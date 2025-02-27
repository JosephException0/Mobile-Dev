package com.android.wordify

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SettingPage : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setting_page)

        val close_settings: Button = findViewById(R.id.setting_close_button)
        val dev_page: Button = findViewById(R.id.Developer_button)

        close_settings.setOnClickListener {
            val intent = Intent(this, LandingPage::class.java)
            startActivity(intent)
        }
        dev_page.setOnClickListener {
            val intent = Intent(this, developer_page::class.java)
            startActivity(intent)
        }
    }
}
