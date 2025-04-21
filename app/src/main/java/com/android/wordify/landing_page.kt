package com.android.wordify

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
                    // Only show logout dialog for non-guest users
                    if (!app.isGuestUser()) {
                        showLogoutDialog()
                    } else {
                        showToast("Already using guest account")
                    }
                }

                R.id.Settings -> {
                    val intent = Intent(this, SettingPage::class.java)
                    startActivity(intent)
                }
            }
            drawerLayout.closeDrawer(GravityCompat.END)
            true
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            // This will minimize the app
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

            // Log out the current user (switches to guest account)
            app.logoutUser()

            // Restart this activity to refresh the UI with guest info
            val intent = Intent(this, LandingPage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        dialog.show()
    }
}