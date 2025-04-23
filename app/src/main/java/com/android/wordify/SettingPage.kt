package com.android.wordify

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat

class SettingPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before setting content view
        val sharedPreferences = getSharedPreferences("Mode", Context.MODE_PRIVATE)
        val nightMode = sharedPreferences.getBoolean("night", false)
        val highContrastMode = sharedPreferences.getBoolean("highContrast", false)

        // Set the theme before super.onCreate()
        if (highContrastMode) {
            applyHighContrastTheme()
        } else {
            AppCompatDelegate.setDefaultNightMode(
                if (nightMode) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
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
            if (isChecked) {
                highContrastSwitch.isChecked = false
                sharedPreferences.edit()
                    .putBoolean("night", true)
                    .putBoolean("highContrast", false)
                    .apply()
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                applyNormalTheme() // Ensure normal theme is applied when switching to dark mode
            } else {
                sharedPreferences.edit().putBoolean("night", false).apply()
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                applyNormalTheme()
            }
            restartActivityWithFade()
        }

        highContrastSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                darkMode.isChecked = false
                sharedPreferences.edit()
                    .putBoolean("highContrast", true)
                    .putBoolean("night", false)
                    .apply()
                applyHighContrastTheme()
            } else {
                sharedPreferences.edit().putBoolean("highContrast", false).apply()
                applyNormalTheme()
                // Restore dark mode if it was previously enabled
                val nightMode = sharedPreferences.getBoolean("night", false)
                AppCompatDelegate.setDefaultNightMode(
                    if (nightMode) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
            }
            restartActivityWithFade()
        }

        // Apply high contrast colors if needed
        if (highContrastMode) {
            applyHighContrastColors()
        } else {
            applyNormalTheme()
        }

        val settingsOptions = listOf(
            "Privacy Policy",
            "Terms & Conditions",
            "Bug Reports & Feedback",
            "Developers"
        )

        val adapter = HighContrastArrayAdapter(this, R.layout.list_item, R.id.list_item_text, settingsOptions)
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
            finish()
        }
    }

    private fun applyHighContrastTheme() {
        // This will ensure no dark mode is applied when high contrast is on
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        applyHighContrastColors()
    }

    private fun applyHighContrastColors() {
        // Get references to all views
        val rootLayout = findViewById<LinearLayout>(R.id.root_layout)
        val titleText = findViewById<TextView>(R.id.settings_title)
        val darkModeSwitch = findViewById<SwitchCompat>(R.id.switch_dark_mode)
        val highContrastSwitch = findViewById<SwitchCompat>(R.id.switch_high_contrast)
        val hintText = findViewById<TextView>(R.id.hint_text)
        val divider = findViewById<View>(R.id.divider)
        val backButton = findViewById<Button>(R.id.setting_close_button)

        // Apply high contrast colors
        rootLayout?.setBackgroundColor(ContextCompat.getColor(this, R.color.hc_background_settings))
        titleText?.setTextColor(ContextCompat.getColor(this, R.color.hc_text_settings))
        darkModeSwitch?.setTextColor(ContextCompat.getColor(this, R.color.hc_text_settings))
        highContrastSwitch?.setTextColor(ContextCompat.getColor(this, R.color.hc_text_settings))
        hintText?.setTextColor(ContextCompat.getColor(this, R.color.hc_hintText_settings))
        divider?.setBackgroundColor(ContextCompat.getColor(this, R.color.hc_divider))
        backButton?.setTextColor(ContextCompat.getColor(this, R.color.hc_text_settings))

        // Override arrow color for high contrast mode
        backButton?.compoundDrawablesRelative?.getOrNull(0)?.mutate()?.let { drawable ->
            drawable.setTint(ContextCompat.getColor(this, R.color.hc_text_settings))
        }
    }

    private fun applyNormalTheme() {
        val backButton = findViewById<Button>(R.id.setting_close_button)
        // Reset arrow to use original XML tint
        backButton?.compoundDrawablesRelative?.getOrNull(0)?.mutate()?.let { drawable ->
            drawable.clearColorFilter()
        }
    }

    private fun restartActivityWithFade() {
        // Ensure we're in a clean state before restarting
        if (getSharedPreferences("Mode", Context.MODE_PRIVATE).getBoolean("highContrast", false)) {
            applyHighContrastColors()
        } else {
            applyNormalTheme()
        }

        val intent = Intent(this, SettingPage::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
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