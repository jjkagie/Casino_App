package com.example.final_project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("casino_app_prefs", MODE_PRIVATE)

        // skip register screen if already registered
        if (sharedPreferences.getBoolean("is_registered", false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_register)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)

        btnRegister.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(
                this,
                "Please enter a username and password",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val sharedPreferences = getSharedPreferences("casino_app_prefs", MODE_PRIVATE)

        // initialize account data
        sharedPreferences.edit()
            .putString("username", username)
            .putString("password", password)
            .putBoolean("is_registered", true)

            .putInt("balance", 5000)

            .putInt("total_games", 0)
            .putInt("total_wins", 0)
            .putInt("total_losses", 0)
            .putInt("total_profit", 0)

            .putInt("blackjack_games", 0)
            .putInt("blackjack_wins", 0)
            .putInt("blackjack_losses", 0)
            .putInt("blackjack_profit", 0)

            .putInt("highlow_games", 0)
            .putInt("highlow_wins", 0)
            .putInt("highlow_losses", 0)
            .putInt("highlow_profit", 0)

            .apply()

        Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show()

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}