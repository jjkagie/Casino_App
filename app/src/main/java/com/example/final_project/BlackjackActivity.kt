package com.example.final_project

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.coroutines.*

class BlackjackActivity : AppCompatActivity() {

    private lateinit var dealerContainer: LinearLayout
    private lateinit var playerContainer: LinearLayout
    private lateinit var tvDealerTotal: TextView
    private lateinit var tvPlayerTotal: TextView
    private lateinit var tvGameStatus: TextView
    private lateinit var tvBalance: TextView
    private lateinit var etWager: EditText
    private lateinit var btnDeal: Button
    private lateinit var btnHit: Button
    private lateinit var btnStand: Button
    private lateinit var btnQuick100: Button
    private lateinit var btnQuick500: Button
    private lateinit var btnBackBlackjack: TextView

    private var deckId = ""
    private val playerCards = mutableListOf<Card>()
    private val dealerCards = mutableListOf<Card>()
    private var revealDealer = false

    private var balance = 5000
    private var wager = 0
    private var gameActive = false
    private var buttonsLocked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blackjack)

        btnBackBlackjack = findViewById(R.id.btnBackBlackjack)
        dealerContainer = findViewById(R.id.dealerCardContainer)
        playerContainer = findViewById(R.id.playerCardContainer)
        tvDealerTotal = findViewById(R.id.tvDealerTotal)
        tvPlayerTotal = findViewById(R.id.tvPlayerTotal)
        tvGameStatus = findViewById(R.id.tvGameStatus)
        tvBalance = findViewById(R.id.tvBalanceBlackjack)
        etWager = findViewById(R.id.etWager)
        btnDeal = findViewById(R.id.btnDeal)
        btnHit = findViewById(R.id.btnHit)
        btnStand = findViewById(R.id.btnStand)
        btnQuick100 = findViewById(R.id.btnQuick100)
        btnQuick500 = findViewById(R.id.btnQuick500)

        loadBalance()
        updateBalance()
        setActionButtons(false)

        btnBackBlackjack.setOnClickListener {
            finish()
        }

        btnQuick100.setOnClickListener {
            etWager.setText("100")
        }

        btnQuick500.setOnClickListener {
            etWager.setText("500")
        }

        btnDeal.setOnClickListener {
            startGame()
        }

        btnHit.setOnClickListener {
            hit()
        }

        btnStand.setOnClickListener {
            stand()
        }
    }

    private fun startGame() {
        if (buttonsLocked) return

        val wagerInput = etWager.text.toString().toIntOrNull()

        if (wagerInput == null || wagerInput <= 0) {
            tvGameStatus.text = "Enter a valid wager"
            return
        }

        if (wagerInput > balance) {
            tvGameStatus.text = "You cannot wager more than your balance"
            return
        }

        wager = wagerInput
        gameActive = true
        revealDealer = false
        buttonsLocked = true

        setBettingButtons(false)
        setActionButtons(false)

        playerCards.clear()
        dealerCards.clear()
        dealerContainer.removeAllViews()
        playerContainer.removeAllViews()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                tvGameStatus.text = "Dealing..."

                val deck = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getShuffledDeck(1)
                }

                deckId = deck.id

                val cards = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getCards(deckId, 4).cards
                }

                if (cards == null || cards.size < 4) {
                    tvGameStatus.text = "Could not draw cards"
                    resetRoundControls()
                    return@launch
                }

                playerCards.add(cards[0])
                updateUI()
                delay(300)

                dealerCards.add(cards[1])
                updateUI()
                delay(300)

                playerCards.add(cards[2])
                updateUI()
                delay(300)

                dealerCards.add(cards[3])
                updateUI()

                buttonsLocked = false
                setActionButtons(true)

                if (getTotal(playerCards) == 21) {
                    revealDealer = true
                    updateUI()
                    tvGameStatus.text = "Blackjack! 3:2 payout"
                    finishRound((wager * 3) / 2, "win")
                } else {
                    tvGameStatus.text = "Hit or Stand"
                }
            } catch (e: Exception) {
                tvGameStatus.text = "Network error. Try again."
                resetRoundControls()
            }
        }
    }

    private fun hit() {
        if (!gameActive || buttonsLocked) return

        buttonsLocked = true
        setActionButtons(false)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val card = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getCards(deckId, 1).cards?.get(0)
                }

                if (card == null) {
                    tvGameStatus.text = "Could not draw card"
                    buttonsLocked = false
                    setActionButtons(true)
                    return@launch
                }

                playerCards.add(card)
                updateUI()

                if (getTotal(playerCards) > 21) {
                    revealDealer = true
                    updateUI()
                    tvGameStatus.text = "Bust"
                    finishRound(-wager, "loss")
                } else {
                    tvGameStatus.text = "Hit or Stand"
                    buttonsLocked = false
                    setActionButtons(true)
                }
            } catch (e: Exception) {
                tvGameStatus.text = "Network error. Try again."
                buttonsLocked = false
                setActionButtons(true)
            }
        }
    }

    private fun stand() {
        if (!gameActive || buttonsLocked) return

        buttonsLocked = true
        setActionButtons(false)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                revealDealer = true
                updateUI()

                while (getTotal(dealerCards) < 17) {
                    delay(600)

                    val card = withContext(Dispatchers.IO) {
                        RetrofitClient.apiService.getCards(deckId, 1).cards?.get(0)
                    }

                    if (card != null) {
                        dealerCards.add(card)
                        updateUI()
                    }
                }

                decideWinner()
            } catch (e: Exception) {
                tvGameStatus.text = "Network error. Try again."
                buttonsLocked = false
                setActionButtons(true)
            }
        }
    }

    private fun decideWinner() {
        val playerTotal = getTotal(playerCards)
        val dealerTotal = getTotal(dealerCards)

        when {
            dealerTotal > 21 || playerTotal > dealerTotal -> {
                tvGameStatus.text = "You win"
                finishRound(wager, "win")
            }
            playerTotal < dealerTotal -> {
                tvGameStatus.text = "Dealer wins"
                finishRound(-wager, "loss")
            }
            else -> {
                tvGameStatus.text = "Push"
                finishRound(0, "push")
            }
        }
    }

    private fun finishRound(change: Int, result: String) {
        balance += change
        saveBalance()
        updateStats(change, result)
        updateBalance()

        gameActive = false
        buttonsLocked = false

        setActionButtons(false)
        setBettingButtons(true)
    }

    private fun resetRoundControls() {
        gameActive = false
        buttonsLocked = false
        setActionButtons(false)
        setBettingButtons(true)
    }

    private fun updateUI() {
        dealerContainer.removeAllViews()
        playerContainer.removeAllViews()

        playerCards.forEach {
            addCardImage(playerContainer, it.image)
        }

        dealerCards.forEachIndexed { index, card ->
            if (!revealDealer && index == 1) {
                addCardImage(dealerContainer, "https://deckofcardsapi.com/static/img/back.png")
            } else {
                addCardImage(dealerContainer, card.image)
            }
        }

        tvPlayerTotal.text = "Total: ${getTotal(playerCards)}"

        tvDealerTotal.text = if (!revealDealer && dealerCards.isNotEmpty()) {
            "Total: ${getCardValue(dealerCards[0])} + ?"
        } else {
            "Total: ${getTotal(dealerCards)}"
        }
    }

    private fun addCardImage(container: LinearLayout, url: String) {
        val imageView = ImageView(this)

        imageView.layoutParams = LinearLayout.LayoutParams(180, 260).apply {
            marginEnd = 12
        }

        Glide.with(this)
            .load(url)
            .into(imageView)

        container.addView(imageView)
    }

    private fun getTotal(cards: List<Card>): Int {
        var total = 0
        var aces = 0

        for (card in cards) {
            when (card.value) {
                "ACE" -> {
                    total += 11
                    aces++
                }
                "KING", "QUEEN", "JACK" -> total += 10
                else -> total += card.value.toInt()
            }
        }

        while (total > 21 && aces > 0) {
            total -= 10
            aces--
        }

        return total
    }

    private fun getCardValue(card: Card): Int {
        return when (card.value) {
            "ACE" -> 11
            "KING", "QUEEN", "JACK" -> 10
            else -> card.value.toInt()
        }
    }

    private fun setActionButtons(enabled: Boolean) {
        btnHit.isEnabled = enabled
        btnStand.isEnabled = enabled
    }

    private fun setBettingButtons(enabled: Boolean) {
        btnDeal.isEnabled = enabled
        etWager.isEnabled = enabled
        btnQuick100.isEnabled = enabled
        btnQuick500.isEnabled = enabled
    }

    private fun loadBalance() {
        val sharedPreferences = getSharedPreferences("casino_app_prefs", MODE_PRIVATE)
        balance = sharedPreferences.getInt("balance", 5000)
    }

    private fun saveBalance() {
        val sharedPreferences = getSharedPreferences("casino_app_prefs", MODE_PRIVATE)
        sharedPreferences.edit()
            .putInt("balance", balance)
            .apply()
    }

    private fun updateBalance() {
        tvBalance.text = "Balance: $$balance"
    }

    private fun updateStats(change: Int, result: String) {
        val sharedPreferences = getSharedPreferences("casino_app_prefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putInt("total_games", sharedPreferences.getInt("total_games", 0) + 1)
        editor.putInt("blackjack_games", sharedPreferences.getInt("blackjack_games", 0) + 1)

        editor.putInt("total_profit", sharedPreferences.getInt("total_profit", 0) + change)
        editor.putInt("blackjack_profit", sharedPreferences.getInt("blackjack_profit", 0) + change)

        when (result) {
            "win" -> {
                editor.putInt("total_wins", sharedPreferences.getInt("total_wins", 0) + 1)
                editor.putInt("blackjack_wins", sharedPreferences.getInt("blackjack_wins", 0) + 1)
            }
            "loss" -> {
                editor.putInt("total_losses", sharedPreferences.getInt("total_losses", 0) + 1)
                editor.putInt("blackjack_losses", sharedPreferences.getInt("blackjack_losses", 0) + 1)
            }
        }

        editor.apply()
    }
}