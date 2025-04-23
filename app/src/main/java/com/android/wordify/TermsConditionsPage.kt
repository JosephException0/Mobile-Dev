package com.android.wordify

import android.content.Context
import android.graphics.PorterDuff
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout

class TermsConditionsPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.terms_conditions)

        val backButton: ImageButton = findViewById(R.id.back_button)
        val termsContent: TextView = findViewById(R.id.terms_content)
        val cardContainer: CardView = findViewById(R.id.card_container)
        val title: TextView = findViewById(R.id.title)
        val rootLayout: ConstraintLayout = findViewById(R.id.root)

        val sharedPreferences = getSharedPreferences("Mode", Context.MODE_PRIVATE)
        val nightMode = sharedPreferences.getBoolean("night", false)
        val highContrast = sharedPreferences.getBoolean("highContrast", false)

        if (nightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // Apply high contrast if enabled
        if (highContrast) {
            applyHighContrastMode(
                rootLayout,
                cardContainer,
                title,
                termsContent,
                backButton
            )
        }

        val termsText = """ 
            Welcome to Wordify!

            By using this application, you agree to the following terms:
            1.Usage: Wordify is a chill word game created for casual fun and relaxation.
            2.Login Options: You can play as a guest or sign up as a user—your choice!         
            3.Fair Play: Cheating isn't really necessary, the game's designed to be easy and enjoyable.     
            4.Data Collection: We don't collect personal data beyond what's needed to make the game run smoothly.
            5.Changes to Terms: These terms might change from time to time. Using the app means you're cool with that.
            6.Liability: We're not liable for any issues that come up from using the app—but we hope you just have fun!

            If you do not agree to these terms, please discontinue using Wordify.

            Thank you for playing Wordify!
        """.trimIndent()

        termsContent.text = termsText

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun applyHighContrastMode(
        rootLayout: ConstraintLayout,
        cardContainer: CardView,
        title: TextView,
        termsContent: TextView,
        backButton: ImageButton
    ) {
        // Set background colors
        rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.hc_background_dev))
        cardContainer.setCardBackgroundColor(ContextCompat.getColor(this, R.color.hc_background_dev))

        // Set text colors
        title.setTextColor(ContextCompat.getColor(this, R.color.hc_text_title))
        termsContent.setTextColor(ContextCompat.getColor(this, R.color.hc_name))

        // Set back button icon color
        backButton.setColorFilter(
            ContextCompat.getColor(this, R.color.hc_text_settings),
            PorterDuff.Mode.SRC_IN
        )
    }
}