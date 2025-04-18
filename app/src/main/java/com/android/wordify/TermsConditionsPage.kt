package com.android.wordify

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TermsConditionsPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.terms_conditions)

        val backButton: ImageButton = findViewById(R.id.back_button)
        val termsContent: TextView = findViewById(R.id.terms_content)

        // We Can modify this directly
        val termsText = """ 
            Welcome to Wordify!

            By using this application, you agree to the following terms:
            1.Usage: Wordify is a chill word game created for casual fun and relaxation.
            2.Login Options: You can play as a guest or sign up as a user—your choice!         
            3.Fair Play: Cheating isn’t really necessary, the game’s designed to be easy and enjoyable.     
            4.Data Collection: We don’t collect personal data beyond what’s needed to make the game run smoothly.
            5.Changes to Terms: These terms might change from time to time. Using the app means you’re cool with that.
            6.Liability: We're not liable for any issues that come up from using the app—but we hope you just have fun!

            If you do not agree to these terms, please discontinue using Wordify.

            Thank you for playing Wordify!
        """.trimIndent()

        termsContent.text = termsText

        backButton.setOnClickListener {
            finish()
        }
    }
}
