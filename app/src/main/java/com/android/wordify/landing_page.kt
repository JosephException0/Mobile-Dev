package com.android.wordify

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class LandingPage : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var app: WordifyApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.landing_page)

        app = application as WordifyApplication
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        val gameScreenView = findViewById<Button>(R.id.button_daily)
        val UnligameScreen = findViewById<Button>(R.id.button_unlimited)

        val menu = navigationView.menu
        val logoutItem = menu.findItem(R.id.Logout)

        if (app.isGuestUser()) {
            logoutItem.setTitle(R.string.menu_login)
        } else {
            logoutItem.setTitle(R.string.menu_logout)
        }

        gameScreenView.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        UnligameScreen.setOnClickListener {
            val intent = Intent(this, UnlimitedMode::class.java)
            startActivity(intent)
        }

        val drawerButton: Button = findViewById(R.id.drawer_button)
        drawerButton.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                drawerLayout.openDrawer(GravityCompat.END)
            }
        }

        val headerView: View = navigationView.getHeaderView(0)
        val closeButton: Button = headerView.findViewById(R.id.close)

        closeButton.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
        }

        navigationView.setNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.profile -> {
                    val intent = Intent(this, ProfilePage::class.java)
                    startActivity(intent)
                }

                R.id.Help -> {
                    val intent = Intent(this, HelpPage::class.java)
                    startActivity(intent)
                }

                R.id.Logout -> {
                    if (!app.isGuestUser()) {
                        showLogoutDialog()
                    } else {
                        val intent = Intent(this, LoginPage::class.java)
                        startActivity(intent)
                    }
                }

                R.id.Settings -> {
                    val intent = Intent(this, SettingPage::class.java)
                    startActivityForResult(intent, 1001)
                }
            }
            drawerLayout.closeDrawer(GravityCompat.END)
            true
        }

        applyThemeBasedOnHighContrast()
    }

    override fun onResume() {
        super.onResume()
        applyThemeBasedOnHighContrast()
    }

    private fun applyThemeBasedOnHighContrast() {
        val sharedPreferences = getSharedPreferences("Mode", Context.MODE_PRIVATE)
        val isHighContrast = sharedPreferences.getBoolean("highContrast", false)

        if (isHighContrast) {
            applyHighContrastTheme()
        } else {
            resetToDefaultTheme()
        }
    }

    private fun applyHighContrastTheme() {
        val title = findViewById<TextView>(R.id.title_text)
        val subtitle = findViewById<TextView>(R.id.subtitle_text)
        val bg = findViewById<DrawerLayout>(R.id.drawer_layout)
        val bg1 = findViewById<LinearLayout>(R.id.groupContainer)

        title?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
        subtitle?.setTextColor(ContextCompat.getColor(this, R.color.hc_text_dev))
        bg?.setBackgroundColor(ContextCompat.getColor(this, R.color.hc_background_dev))
        bg1?.setBackgroundColor(ContextCompat.getColor(this, R.color.hc_background_dev))
    }

    private fun resetToDefaultTheme() {
        val title = findViewById<TextView>(R.id.title_text)
        val subtitle = findViewById<TextView>(R.id.subtitle_text)
        val bg = findViewById<DrawerLayout>(R.id.drawer_layout)
        val bg1 = findViewById<LinearLayout>(R.id.groupContainer)

        title?.setTextColor(ContextCompat.getColor(this, R.color.text_lp))
        subtitle?.setTextColor(ContextCompat.getColor(this, R.color.text_lp))
        bg?.setBackgroundColor(ContextCompat.getColor(this, R.color.background_lp))
        bg1?.setBackgroundColor(ContextCompat.getColor(this, R.color.background_lp))
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            moveTaskToBack(true)
        }
    }

    private fun showLogoutDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_logout)
        dialog.setCancelable(true)

        val btnCancel: Button = dialog.findViewById(R.id.btn_cancel)
        val btnConfirm: Button = dialog.findViewById(R.id.btn_confirm)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnConfirm.setOnClickListener {
            dialog.dismiss()
            app.logoutUser()
            val intent = Intent(this, LandingPage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        dialog.show()
    }
}
