package com.example.final_project

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PlayActivity : AppCompatActivity() {

    private lateinit var btnBack: TextView
    private lateinit var cardBlackjack: LinearLayout
    private lateinit var cardHighLow: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        btnBack = findViewById(R.id.btnBack)
        cardBlackjack = findViewById(R.id.cardBlackjack)
        cardHighLow = findViewById(R.id.cardHighLow)

        btnBack.setOnClickListener {
            finish()
        }

        cardBlackjack.setOnClickListener {
            startActivity(Intent(this, BlackjackActivity::class.java))
        }

        cardHighLow.setOnClickListener {
            startActivity(Intent(this, HighLowActivity::class.java))
        }
    }
}