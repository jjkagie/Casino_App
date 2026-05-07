package com.example.final_project

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var btnBackLeaderboard: TextView
    private lateinit var tabBlackjack: TextView
    private lateinit var tabHighLow: TextView

    private lateinit var tvLeaderboardHeader: TextView
    private lateinit var tvRow1: TextView
    private lateinit var tvRow2: TextView
    private lateinit var tvRow3: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        btnBackLeaderboard = findViewById(R.id.btnBackLeaderboard)
        tabBlackjack = findViewById(R.id.tabBlackjack)
        tabHighLow = findViewById(R.id.tabHighLow)

        tvLeaderboardHeader = findViewById(R.id.tvLeaderboardHeader)
        tvRow1 = findViewById(R.id.tvRow1)
        tvRow2 = findViewById(R.id.tvRow2)
        tvRow3 = findViewById(R.id.tvRow3)

        btnBackLeaderboard.setOnClickListener {
            finish()
        }

        tabBlackjack.setOnClickListener {
            showBlackjackStats()
        }

        tabHighLow.setOnClickListener {
            showHighLowStats()
        }

        showBlackjackStats()
    }

    private fun showBlackjackStats() {
        selectTab(tabBlackjack, tabHighLow)

        val sharedPreferences = getSharedPreferences("casino_app_prefs", MODE_PRIVATE)

        val games = sharedPreferences.getInt("blackjack_games", 0)
        val wins = sharedPreferences.getInt("blackjack_wins", 0)
        val losses = sharedPreferences.getInt("blackjack_losses", 0)
        val profit = sharedPreferences.getInt("blackjack_profit", 0)

        tvLeaderboardHeader.text = "Blackjack Stats"
        tvRow1.text = "Games Played: $games"
        tvRow2.text = "Wins: $wins | Losses: $losses"
        tvRow3.text = "Profit / Loss: ${formatMoney(profit)}"
    }

    private fun showHighLowStats() {
        selectTab(tabHighLow, tabBlackjack)

        val sharedPreferences = getSharedPreferences("casino_app_prefs", MODE_PRIVATE)

        val games = sharedPreferences.getInt("highlow_games", 0)
        val wins = sharedPreferences.getInt("highlow_wins", 0)
        val losses = sharedPreferences.getInt("highlow_losses", 0)
        val profit = sharedPreferences.getInt("highlow_profit", 0)

        tvLeaderboardHeader.text = "High-Low Stats"
        tvRow1.text = "Games Played: $games"
        tvRow2.text = "Wins: $wins | Losses: $losses"
        tvRow3.text = "Profit / Loss: ${formatMoney(profit)}"
    }

    private fun selectTab(selected: TextView, unselected: TextView) {
        selected.setBackgroundColor(Color.parseColor("#374151"))
        selected.setTextColor(Color.WHITE)

        unselected.setBackgroundColor(Color.parseColor("#1F2937"))
        unselected.setTextColor(Color.parseColor("#D1D5DB"))
    }

    private fun formatMoney(amount: Int): String {
        return if (amount >= 0) {
            "+$$amount"
        } else {
            "-$${-amount}"
        }
    }
}