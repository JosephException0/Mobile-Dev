package com.android.wordify

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat

class PrivacyPolicyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.privacy_policy)

        val backArrow: ImageView = findViewById(R.id.backArrow)
        val title: TextView = findViewById(R.id.privacyTitle)
        val content: TextView = findViewById(R.id.privacyContent)
        val rootLayout: ConstraintLayout = findViewById(R.id.root)

        content.text = getString(R.string.privacy_policy).trimIndent()

        val sharedPreferences = getSharedPreferences("Mode", Context.MODE_PRIVATE)
        val highContrast = sharedPreferences.getBoolean("highContrast", false)

        if (highContrast) {
            applyHighContrast(rootLayout, title, content, backArrow)
        }

        backArrow.setOnClickListener {
            startActivity(Intent(this, SettingPage::class.java))
        }
    }

    private fun applyHighContrast(
        rootLayout: ConstraintLayout,
        title: TextView,
        content: TextView,
        backArrow: ImageView
    ) {
        rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.hc_background_dev))
        title.setTextColor(ContextCompat.getColor(this, R.color.hc_text_title))
        content.setTextColor(ContextCompat.getColor(this, R.color.hc_name))

        backArrow.setColorFilter(
            ContextCompat.getColor(this, R.color.hc_text_settings),
            PorterDuff.Mode.SRC_IN
        )
    }
}
