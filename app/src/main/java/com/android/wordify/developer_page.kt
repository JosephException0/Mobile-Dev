package com.android.wordify

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class developer_page : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.developer_page)

        val devBackButton: Button = findViewById(R.id.setting_close_button)
        devBackButton.setOnClickListener {
            val intent = Intent(this, SettingPage::class.java)
            startActivity(intent)
        }

        val githubIcon1: ImageView = findViewById(R.id.githubIcon1)
        val githubIcon2: ImageView = findViewById(R.id.githubIcon2)

        githubIcon1.setOnClickListener {
            openGitHub("https://github.com/JosephException0")
        }

        githubIcon2.setOnClickListener {
            openGitHub("https://github.com/Fishdips11")
        }
    }
    private fun openGitHub(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}
