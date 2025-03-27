package com.android.wordify

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TermsConditionsPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.terms_conditions)

        val backButton: Button = findViewById(R.id.back_button)
        val termsContent: TextView = findViewById(R.id.terms_content)

        // We Can modify this directly
        val termsText = """ 
            Welcome to Wordify!

            By using this application, you agree to the following terms:

            1. **Usage**: Wordify is a word-guessing game designed for entertainment. 
            2. **Account**: No registration is required, but progress may be saved locally.
            3. **Fair Play**: Cheating, hacking, or exploiting the app is strictly prohibited.
            4. **Data Collection**: We do not collect personal data beyond what is required for gameplay analytics.
            5. **Changes to Terms**: These terms may be updated, and continued use of the app implies acceptance of new terms.
            6. **Liability**: The developers are not responsible for any damages resulting from app usage.

            If you do not agree to these terms, please discontinue using Wordify.

            Thank you for playing Wordify!
        """.trimIndent()

        termsContent.text = termsText

        backButton.setOnClickListener {
            finish()
        }
    }
}
