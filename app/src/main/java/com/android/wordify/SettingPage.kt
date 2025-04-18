package com.android.wordify

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat

class SettingPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before setting content view
        val sharedPreferences = getSharedPreferences("Mode", Context.MODE_PRIVATE)
        val nightMode = sharedPreferences.getBoolean("night", false)
        val highContrastMode = sharedPreferences.getBoolean("highContrast", false)

        if (nightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.setting_page)

        val closeSettings: Button = findViewById(R.id.setting_close_button)
        val settingsList: ListView = findViewById(R.id.settings_list)
        val darkMode = findViewById<SwitchCompat>(R.id.switch_dark_mode)
        val highContrastSwitch = findViewById<SwitchCompat>(R.id.switch_high_contrast)

        darkMode.isChecked = nightMode
        highContrastSwitch.isChecked = highContrastMode

        darkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != nightMode) {
                sharedPreferences.edit().apply {
                    putBoolean("night", isChecked)
                    apply()
                }

                AppCompatDelegate.setDefaultNightMode(
                    if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )

                restartActivity()
            }
        }

        highContrastSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != highContrastMode) {
                sharedPreferences.edit().apply {
                    putBoolean("highContrast", isChecked)
                    apply()
                }
                restartActivity()
            }
        }

        val settingsOptions = listOf(
            "Privacy Policy",
            "Terms & Conditions",
            "Bug Reports & Feedback",
            "Developers"
        )

        val adapter = ArrayAdapter(this, R.layout.list_item, R.id.list_item_text, settingsOptions)
        settingsList.adapter = adapter

        settingsList.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> startActivity(Intent(this, PrivacyPolicyActivity::class.java))
                1 -> startActivity(Intent(this, TermsConditionsPage::class.java))
                2 -> showBugReportDialog()
                3 -> startActivity(Intent(this, developer_page::class.java))
            }
        }

        closeSettings.setOnClickListener {
            startActivity(Intent(this, LandingPage::class.java))
        }
    }

    private fun restartActivity() {
        val intent = intent
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun showBugReportDialog() {
        val dialogView = layoutInflater.inflate(R.layout.bugs_report, null)
        val editText = dialogView.findViewById<EditText>(R.id.feedback_input)

        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Bug Report / Feedback")
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialogView.findViewById<Button>(R.id.submit_button).setOnClickListener {
            val message = editText.text.toString().trim()
            if (message.isNotEmpty()) {
                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:")
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("olimbajayz789@gmail.com", "fishdips11@gmail.com"))
                    putExtra(Intent.EXTRA_SUBJECT, "Wordify Bug Report / Feedback")
                    putExtra(Intent.EXTRA_TEXT, message)
                }

                try {
                    startActivity(Intent.createChooser(emailIntent, "Send email..."))
                    dialog.dismiss()
                } catch (e: android.content.ActivityNotFoundException) {
                    Toast.makeText(this, "No email app found.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter your feedback.", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }
}
