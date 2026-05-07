package com.example.final_project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var btnBackSettings: TextView
    private lateinit var tvUsername: TextView
    private lateinit var tvProfileBalance: TextView
    private lateinit var tvTotalGames: TextView
    private lateinit var tvTotalWins: TextView
    private lateinit var tvTotalLosses: TextView
    private lateinit var tvTotalProfit: TextView
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        btnBackSettings = findViewById(R.id.btnBackSettings)
        tvUsername = findViewById(R.id.tvUsername)
        tvProfileBalance = findViewById(R.id.tvProfileBalance)
        tvTotalGames = findViewById(R.id.tvTotalGames)
        tvTotalWins = findViewById(R.id.tvTotalWins)
        tvTotalLosses = findViewById(R.id.tvTotalLosses)
        tvTotalProfit = findViewById(R.id.tvTotalProfit)
        btnLogout = findViewById(R.id.btnLogout)

        loadProfileData()
        setupClicks()
    }

    private fun loadProfileData() {
        val sharedPreferences = getSharedPreferences("casino_app_prefs", MODE_PRIVATE)

        val username = sharedPreferences.getString("username", "Player")
        val balance = sharedPreferences.getInt("balance", 5000)
        val totalGames = sharedPreferences.getInt("total_games", 0)
        val totalWins = sharedPreferences.getInt("total_wins", 0)
        val totalLosses = sharedPreferences.getInt("total_losses", 0)
        val totalProfit = sharedPreferences.getInt("total_profit", 0)

        tvUsername.text = "Username: $username"
        tvProfileBalance.text = "Balance: $$balance"
        tvTotalGames.text = "Total Games: $totalGames"
        tvTotalWins.text = "Total Wins: $totalWins"
        tvTotalLosses.text = "Total Losses: $totalLosses"
        tvTotalProfit.text = "Total Profit / Loss: ${formatMoney(totalProfit)}"
    }

    private fun setupClicks() {
        btnBackSettings.setOnClickListener {
            finish()
        }

        btnLogout.setOnClickListener {
            val sharedPreferences = getSharedPreferences("casino_app_prefs", MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()

            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }
    }

    private fun formatMoney(amount: Int): String {
        return if (amount >= 0) {
            "+$$amount"
        } else {
            "-$${-amount}"
        }
    }
}