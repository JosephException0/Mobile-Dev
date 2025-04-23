package com.android.wordify

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat

class developer_page : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.developer_page)

        val devBackButton: Button = findViewById(R.id.setting_close_button)
        devBackButton.setOnClickListener {
            val intent = Intent(this, SettingPage::class.java)
            startActivity(intent)
        }

        val githubIcon1: ImageView = findViewById(R.id.githubIcon1)
        val githubIcon2: ImageView = findViewById(R.id.githubIcon2)

        githubIcon1.setOnClickListener {
            openGitHub("https://github.com/JosephException0")
        }

        githubIcon2.setOnClickListener {
            openGitHub("https://github.com/Fishdips11")
        }
    }
    override fun onResume() {
        super.onResume()
        applyHighContrastIfEnabled()
    }
    private fun openGitHub(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun applyHighContrastIfEnabled() {
        val sharedPreferences = getSharedPreferences("Mode", Context.MODE_PRIVATE)
        val isHighContrast = sharedPreferences.getBoolean("highContrast", false)

        if (isHighContrast) {

            val name1 = findViewById<TextView>(R.id.textViewName1)
            val prog1 = findViewById<TextView>(R.id.textViewProgram1)
            val about1 = findViewById<TextView>(R.id.textViewAbout1)
            val name2 = findViewById<TextView>(R.id.textViewName2)
            val prog2 = findViewById<TextView>(R.id.textViewProgram2)
            val about2 = findViewById<TextView>(R.id.textViewAbout2)
            val bg = findViewById<ConstraintLayout>(R.id.root_dev)
            val title = findViewById<TextView>(R.id.textViewTitle)
            val card1 = findViewById<LinearLayout>(R.id.cardDev1)
            val card2 = findViewById<LinearLayout>(R.id.cardDev2)
            val backButton = findViewById<Button>(R.id.setting_close_button)

            name1?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
            name2?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
            prog1?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
            prog2?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
            about1?.setTextColor(ContextCompat.getColor(this, R.color.hc_text_dev))
            about2?.setTextColor(ContextCompat.getColor(this, R.color.hc_text_dev))
            bg?.setBackgroundColor(ContextCompat.getColor(this, R.color.hc_background_dev))
            title?.setTextColor(ContextCompat.getColor(this, R.color.hc_text_title))
            card1?.setBackgroundColor(ContextCompat.getColor(this, R.color.hc_card_dev))
            card2?.setBackgroundColor(ContextCompat.getColor(this, R.color.hc_card_dev))


            backButton?.compoundDrawablesRelative?.getOrNull(0)?.mutate()?.let { drawable ->
                drawable.setTint(ContextCompat.getColor(this, R.color.hc_text_settings))
            }
        }
    }
}
