package com.example.final_project

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HighLowActivity : AppCompatActivity() {

    private lateinit var btnBackHighLow: TextView
    private lateinit var tvBalanceHighLow: TextView
    private lateinit var tvRoundInfo: TextView
    private lateinit var tvCashoutValue: TextView
    private lateinit var cardContainerHighLow: LinearLayout
    private lateinit var etHighLowWager: EditText
    private lateinit var btnHighLow100: Button
    private lateinit var btnHighLow500: Button
    private lateinit var btnChoiceOne: Button
    private lateinit var btnChoiceTwo: Button
    private lateinit var btnChoiceThree: Button
    private lateinit var btnChoiceFour: Button
    private lateinit var btnCashOut: Button
    private lateinit var btnStartHighLow: Button

    private var deckId = ""
    private var balance = 5000
    private var currentWager = 0
    private var currentCashout = 0
    private var stage = 0
    private var gameActive = false
    private var buttonsLocked = false

    private val cards = mutableListOf<Card>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_high_low)

        btnBackHighLow = findViewById(R.id.btnBackHighLow)
        tvBalanceHighLow = findViewById(R.id.tvBalanceHighLow)
        tvRoundInfo = findViewById(R.id.tvRoundInfo)
        tvCashoutValue = findViewById(R.id.tvCashoutValue)
        cardContainerHighLow = findViewById(R.id.cardContainerHighLow)
        etHighLowWager = findViewById(R.id.etHighLowWager)
        btnHighLow100 = findViewById(R.id.btnHighLow100)
        btnHighLow500 = findViewById(R.id.btnHighLow500)
        btnChoiceOne = findViewById(R.id.btnChoiceOne)
        btnChoiceTwo = findViewById(R.id.btnChoiceTwo)
        btnChoiceThree = findViewById(R.id.btnChoiceThree)
        btnChoiceFour = findViewById(R.id.btnChoiceFour)
        btnCashOut = findViewById(R.id.btnCashOut)
        btnStartHighLow = findViewById(R.id.btnStartHighLow)

        loadBalance()
        updateBalanceText()
        resetButtons()

        btnBackHighLow.setOnClickListener {
            finish()
        }

        btnHighLow100.setOnClickListener {
            etHighLowWager.setText("100")
        }

        btnHighLow500.setOnClickListener {
            etHighLowWager.setText("500")
        }

        btnStartHighLow.setOnClickListener {
            startGame()
        }

        btnCashOut.setOnClickListener {
            cashOut()
        }

        btnChoiceOne.setOnClickListener {
            handleChoice(btnChoiceOne.text.toString())
        }

        btnChoiceTwo.setOnClickListener {
            handleChoice(btnChoiceTwo.text.toString())
        }

        btnChoiceThree.setOnClickListener {
            handleChoice(btnChoiceThree.text.toString())
        }

        btnChoiceFour.setOnClickListener {
            handleChoice(btnChoiceFour.text.toString())
        }
    }

    // starts a new round
    private fun startGame() {
        if (buttonsLocked) return

        val wager = etHighLowWager.text.toString().trim().toIntOrNull()

        if (wager == null || wager <= 0) {
            tvRoundInfo.text = "Enter a valid wager"
            return
        }

        if (wager > balance) {
            tvRoundInfo.text = "You cannot wager more than your balance"
            return
        }

        currentWager = wager
        currentCashout = 0
        stage = 1
        gameActive = true
        buttonsLocked = true

        cards.clear()
        cardContainerHighLow.removeAllViews()

        etHighLowWager.isEnabled = false
        btnHighLow100.isEnabled = false
        btnHighLow500.isEnabled = false
        btnStartHighLow.isEnabled = false
        btnCashOut.isEnabled = false

        CoroutineScope(Dispatchers.Main).launch {
            try {
                tvRoundInfo.text = "Guess the first card color"

                val deck = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getShuffledDeck(1)
                }

                deckId = deck.id

                setChoices("Red", "Black")
                updateCashoutText()

                buttonsLocked = false
            } catch (e: Exception) {
                tvRoundInfo.text = "Network error. Try again."
                resetAfterRound()
            }
        }
    }

    private fun handleChoice(choice: String) {
        if (!gameActive || buttonsLocked) {
            return
        }

        buttonsLocked = true

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val card = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getCards(deckId, 1).cards?.get(0)
                }

                if (card == null) {
                    tvRoundInfo.text = "Error drawing card"
                    buttonsLocked = false
                    return@launch
                }

                cards.add(card)

                val correct = when (stage) {
                    1 -> checkColor(choice, card)
                    2 -> checkHigherLower(choice, card)
                    3 -> checkInsideOutside(choice, card)
                    4 -> checkSuit(choice, card)
                    else -> false
                }

                updateCardsText()

                if (!correct) {
                    loseRound()
                    return@launch
                }

                currentCashout = getPayoutForStage(stage)

                // player completed all stages
                if (stage == 4) {
                    balance += currentCashout
                    saveBalance()
                    updateStats(currentCashout, "win")

                    updateBalanceText()
                    tvRoundInfo.text = "You completed all 4 stages and won $$currentCashout"

                    gameActive = false
                    resetAfterRound()
                    return@launch
                }

                stage++
                updateCashoutText()
                btnCashOut.isEnabled = true
                updateStageTextAndChoices()

                buttonsLocked = false

            } catch (e: Exception) {
                tvRoundInfo.text = "Network error. Try again."
                buttonsLocked = false
            }
        }
    }

    private fun updateStageTextAndChoices() {
        when (stage) {
            2 -> {
                tvRoundInfo.text = "Correct. Guess higher or lower"
                setChoices("Higher", "Lower")
            }

            3 -> {
                tvRoundInfo.text = "Correct. Guess inside or outside"
                setChoices("Inside", "Outside")
            }

            4 -> {
                tvRoundInfo.text = "Correct. Guess the suit"
                setSuitChoices()
            }
        }
    }

    private fun checkColor(choice: String, card: Card): Boolean {
        val redSuit = card.suit == "HEARTS" || card.suit == "DIAMONDS"

        return (choice == "Red" && redSuit)
                || (choice == "Black" && !redSuit)
    }

    private fun checkHigherLower(choice: String, card: Card): Boolean {
        val previous = getRankValue(cards[0])
        val current = getRankValue(card)

        return when (choice) {
            "Higher" -> current > previous
            "Lower" -> current < previous
            else -> false
        }
    }

    private fun checkInsideOutside(choice: String, card: Card): Boolean {
        val first = getRankValue(cards[0])
        val second = getRankValue(cards[1])
        val current = getRankValue(card)

        val low = minOf(first, second)
        val high = maxOf(first, second)

        val inside = current > low && current < high

        return when (choice) {
            "Inside" -> inside
            "Outside" -> !inside
            else -> false
        }
    }

    private fun checkSuit(choice: String, card: Card): Boolean {
        return choice.uppercase() == card.suit
    }

    // payout increases every stage
    private fun getPayoutForStage(stageNumber: Int): Int {
        return when (stageNumber) {
            1 -> currentWager * 2
            2 -> currentWager * 4
            3 -> currentWager * 8
            4 -> currentWager * 32
            else -> 0
        }
    }

    private fun cashOut() {
        if (!gameActive || currentCashout <= 0) {
            return
        }

        balance += currentCashout

        saveBalance()
        updateStats(currentCashout, "win")
        updateBalanceText()

        tvRoundInfo.text = "Cashed out for $$currentCashout"

        gameActive = false
        resetAfterRound()
    }

    private fun loseRound() {
        balance -= currentWager

        saveBalance()
        updateStats(-currentWager, "loss")
        updateBalanceText()

        tvRoundInfo.text = "Wrong guess. You lost $$currentWager"

        gameActive = false
        currentCashout = 0

        updateCashoutText()
        resetAfterRound()
    }

    private fun updateCardsText() {
        cardContainerHighLow.removeAllViews()

        for (card in cards) {
            addCardImage(card.image)
        }
    }

    private fun addCardImage(url: String) {
        val imageView = ImageView(this)

        imageView.layoutParams = LinearLayout.LayoutParams(160, 230).apply {
            marginEnd = 12
        }

        Glide.with(this)
            .load(url)
            .into(imageView)

        cardContainerHighLow.addView(imageView)
    }

    private fun setChoices(first: String, second: String) {
        btnChoiceOne.text = first
        btnChoiceTwo.text = second

        btnChoiceOne.isEnabled = true
        btnChoiceTwo.isEnabled = true

        btnChoiceThree.isEnabled = false
        btnChoiceFour.isEnabled = false
    }

    private fun setSuitChoices() {
        btnChoiceOne.text = "Hearts"
        btnChoiceTwo.text = "Diamonds"
        btnChoiceThree.text = "Clubs"
        btnChoiceFour.text = "Spades"

        btnChoiceOne.isEnabled = true
        btnChoiceTwo.isEnabled = true
        btnChoiceThree.isEnabled = true
        btnChoiceFour.isEnabled = true
    }

    private fun resetButtons() {
        btnChoiceOne.isEnabled = false
        btnChoiceTwo.isEnabled = false
        btnChoiceThree.isEnabled = false
        btnChoiceFour.isEnabled = false
        btnCashOut.isEnabled = false
    }

    private fun resetAfterRound() {
        buttonsLocked = false

        etHighLowWager.isEnabled = true
        btnHighLow100.isEnabled = true
        btnHighLow500.isEnabled = true
        btnStartHighLow.isEnabled = true

        resetButtons()
    }

    private fun getRankValue(card: Card): Int {
        return when (card.value) {
            "ACE" -> 14
            "KING" -> 13
            "QUEEN" -> 12
            "JACK" -> 11
            else -> card.value.toInt()
        }
    }

    private fun updateCashoutText() {
        tvCashoutValue.text = "Current Cashout: $$currentCashout"
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

    private fun updateBalanceText() {
        tvBalanceHighLow.text = "Balance: $$balance"
    }

    // updates overall and game-specific stats
    private fun updateStats(change: Int, result: String) {
        val sharedPreferences = getSharedPreferences("casino_app_prefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putInt("total_games", sharedPreferences.getInt("total_games", 0) + 1)
        editor.putInt("highlow_games", sharedPreferences.getInt("highlow_games", 0) + 1)

        editor.putInt("total_profit", sharedPreferences.getInt("total_profit", 0) + change)
        editor.putInt("highlow_profit", sharedPreferences.getInt("highlow_profit", 0) + change)

        when (result) {
            "win" -> {
                editor.putInt("total_wins", sharedPreferences.getInt("total_wins", 0) + 1)
                editor.putInt("highlow_wins", sharedPreferences.getInt("highlow_wins", 0) + 1)
            }

            "loss" -> {
                editor.putInt("total_losses", sharedPreferences.getInt("total_losses", 0) + 1)
                editor.putInt("highlow_losses", sharedPreferences.getInt("highlow_losses", 0) + 1)
            }
        }

        editor.apply()
    }
}