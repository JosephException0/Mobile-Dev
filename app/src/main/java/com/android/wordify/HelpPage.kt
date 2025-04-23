package com.android.wordify

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class HelpPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.help_page)

        val back = findViewById<Button>(R.id.back_button_help)

        back.setOnClickListener {
            finish()
        }
    }
}
