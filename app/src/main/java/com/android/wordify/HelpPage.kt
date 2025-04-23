package com.android.wordify

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class HelpPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.help_page)

        val back = findViewById<Button>(R.id.help_back)
        val rootLayout = findViewById<ScrollView>(R.id.root_hp)
        val title = findViewById<TextView>(R.id.title)

        val intros = listOf(
            findViewById<TextView>(R.id.intro),
            findViewById<TextView>(R.id.intro2),
            findViewById<TextView>(R.id.intro3),
            findViewById<TextView>(R.id.intro4)
        )

        val descriptions = listOf(
            findViewById<TextView>(R.id.text_intro),
            findViewById<TextView>(R.id.text_dictionary),
            findViewById<TextView>(R.id.text_games),
            findViewById<TextView>(R.id.text_progress)
        )

        val sharedPreferences = getSharedPreferences("Mode", Context.MODE_PRIVATE)
        val highContrast = sharedPreferences.getBoolean("highContrast", false)

        if (highContrast) {
            applyHighContrast(rootLayout, title, intros, descriptions, back)
        }

        back.setOnClickListener {
            finish()
        }
    }

    private fun applyHighContrast(
        root: ScrollView,
        title: TextView,
        intros: List<TextView>,
        descriptions: List<TextView>,
        backBtn: Button
    ) {
        root.setBackgroundColor(ContextCompat.getColor(this, R.color.hc_background_dev))
        title.setTextColor(ContextCompat.getColor(this, R.color.hc_text_title))

        for (intro in intros) {
            intro.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
        }

        for (desc in descriptions) {
            desc.setTextColor(ContextCompat.getColor(this, R.color.hc_text_dev))
        }

        backBtn?.compoundDrawablesRelative?.getOrNull(0)?.mutate()?.let { drawable ->
            drawable.setTint(ContextCompat.getColor(this, R.color.hc_text_settings))
        }
    }
}
