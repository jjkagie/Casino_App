package com.example.final_project

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var tvBalance: TextView
    private lateinit var tvGamesPlayedValue: TextView
    private lateinit var tvWinsValue: TextView
    private lateinit var tvLossesValue: TextView
    private lateinit var tvProfitValue: TextView

    private lateinit var navLeaderboard: LinearLayout
    private lateinit var navPlay: LinearLayout
    private lateinit var navSettings: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvWelcome = findViewById(R.id.tvWelcome)
        tvBalance = findViewById(R.id.tvBalance)
        tvGamesPlayedValue = findViewById(R.id.tvGamesPlayedValue)
        tvWinsValue = findViewById(R.id.tvWinsValue)
        tvLossesValue = findViewById(R.id.tvLossesValue)
        tvProfitValue = findViewById(R.id.tvProfitValue)

        navLeaderboard = findViewById(R.id.navLeaderboard)
        navPlay = findViewById(R.id.navPlay)
        navSettings = findViewById(R.id.navSettings)

        setupNavigation()
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }

    private fun loadUserData() {
        val sharedPreferences = getSharedPreferences("casino_app_prefs", MODE_PRIVATE)

        val username = sharedPreferences.getString("username", "Player")
        val balance = sharedPreferences.getInt("balance", 5000)
        val totalGames = sharedPreferences.getInt("total_games", 0)
        val totalWins = sharedPreferences.getInt("total_wins", 0)
        val totalLosses = sharedPreferences.getInt("total_losses", 0)
        val totalProfit = sharedPreferences.getInt("total_profit", 0)

        tvWelcome.text = "Welcome back, $username"
        tvBalance.text = "$$balance"
        tvGamesPlayedValue.text = totalGames.toString()
        tvWinsValue.text = totalWins.toString()
        tvLossesValue.text = totalLosses.toString()
        tvProfitValue.text = if (totalProfit >= 0) {
            "+$$totalProfit"
        } else {
            "-$${-totalProfit}"
        }
    }

    private fun setupNavigation() {
        navLeaderboard.setOnClickListener {
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }

        navPlay.setOnClickListener {
            startActivity(Intent(this, PlayActivity::class.java))
        }

        navSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}