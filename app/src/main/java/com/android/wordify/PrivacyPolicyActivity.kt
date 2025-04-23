package com.android.wordify

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PrivacyPolicyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.privacy_policy)

        val privacyContent: TextView = findViewById(R.id.privacyContent)
        privacyContent.text = getString(R.string.privacy_policy).trimIndent()

        val devBackArrow: ImageView = findViewById(R.id.backArrow)
        devBackArrow.setOnClickListener {
            startActivity(Intent(this, SettingPage::class.java))
        }
    }
}
