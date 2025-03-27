package com.android.wordify

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SettingPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setting_page)

        val closeSettings: Button = findViewById(R.id.setting_close_button)
        val settingsList: ListView = findViewById(R.id.settings_list)

        // List items
        val settingsOptions = listOf(
            "Privacy Policy",
            "Terms & Conditions",
            "Bug Reports & Feedback",
            "Developers"
        )


        val adapter = ArrayAdapter(this, R.layout.list_item, R.id.list_item_text, settingsOptions)
        settingsList.adapter = adapter

        // Click listener for list items, commented indices means that it still doesn't have its page
        settingsList.setOnItemClickListener { _, _, position, _ ->
            when (position) {
//                0 -> startActivity(Intent(this, ::class.java))
                1 -> startActivity(Intent(this, TermsConditionsPage::class.java))
//                2 -> startActivity(Intent(this, ::class.java))
                3 -> startActivity(Intent(this, developer_page::class.java))
            }
        }

        closeSettings.setOnClickListener {
            startActivity(Intent(this, LandingPage::class.java))
        }
    }
}
