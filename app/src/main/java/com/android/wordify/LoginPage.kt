package com.android.wordify

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class LoginPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_screen)

        val goingToregister: Button = findViewById(R.id.going_to_register)

        goingToregister.setOnClickListener {
            val intent = Intent(this, RegisterPage::class.java)
            startActivity(intent)
        }

        val donelogin: Button = findViewById(R.id.login_button)

        donelogin.setOnClickListener{
            val intent = Intent( this, LandingPage::class.java)
            startActivity(intent)
        }

    }
}
