package com.android.wordify

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var texts: MutableList<MutableList<TextView>>
    private val rowCount = 6
    private val colCount = 5
    private var isWon = false
    private var countWins = 0
    private lateinit var gameCore: GameCore
    private var currentRow = 0
    private var isInputBlocked = false
    private var isAnimating = false
    private var cooldownActive = false
    private val cooldownDuration = 10000L  // 10 seconds in milliseconds

    // Handler for cooldown timer
    private val cooldownHandler = Handler(Looper.getMainLooper())
    private val cooldownRunnable = Runnable {
        resetGame()
    }

    // SharedPreferences constants for Daily Mode
    private val PREFS_BASE_NAME = "WordifyDailyGameState" // Now a base name
    private val KEY_IS_WON = "is_won"
    private val KEY_WINS_COUNT = "wins_count"
    private val KEY_CURRENT_ROW = "current_row"
    private val KEY_KEYBOARD_STATE = "keyboard_state"
    private val KEY_INPUT_BLOCKED = "input_blocked"
    private val KEY_COOLDOWN_ACTIVE = "cooldown_active"
    private val KEY_COOLDOWN_END_TIME = "cooldown_end_time"


    // Get user-specific preference name
    private fun getPreferenceName(): String {
        val app = application as WordifyApplication
        return app.getUserPreferenceName(PREFS_BASE_NAME)
    }

    @SuppressLint("SetTextI18s")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_screen)

        val sharedPreferences = getSharedPreferences("Mode", Context.MODE_PRIVATE)
        val nightMode = sharedPreferences.getBoolean("night", false)

        if (nightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        val backLanding = findViewById<ImageView>(R.id.back_to_landing)
        backLanding.setOnClickListener {
            // Save state before leaving
            saveFullGameState()
            startActivity(Intent(this, LandingPage::class.java))
        }

        val help = findViewById<ImageView>(R.id.help_gamescreen)
        help.setOnClickListener {
            // Save state before showing help
            saveFullGameState()
            startActivity(Intent(this, HelpPage::class.java))
        }

        // Pass PREFS_NAME to GameCore as the preference name
        gameCore = GameCore(rowCount, this, PREFS_BASE_NAME)
        initTexts()
        setEventListeners()

        // Try to load saved game first, start new game if none exists
        if (!loadFullGameState()) {
            newRound()
        }
    }

    override fun onPause() {
        super.onPause()
        // Save game state when activity is paused
        saveFullGameState()
    }

    override fun onStop() {
        super.onStop()
        // Also save game state when activity is stopped
        saveFullGameState()
    }

    override fun onResume() {
        super.onResume()
        // Check if cooldown should be active when resuming
        checkCooldownOnResume()
    }

    private fun setEventListeners() {
        for (c in 'Z' downTo 'A') {
            val btn = findViewById<Button>(resources.getIdentifier("button$c", "id", packageName))
            btn.setOnClickListener {
                if (gameCore.isPouse() || isAnimating || cooldownActive) return@setOnClickListener
                if (isInputBlocked) return@setOnClickListener

                if (gameCore.isPouse()) {
                    gameCore.startOver()
                    newRound()
                }

                val row = gameCore.getCurRow()
                val col = gameCore.getCurCol()

                if (gameCore.setNextChar(c)) {
                    texts[row][col].text = c.toString()
                    // Save state after each character input
                    saveFullGameState()
                }
            }
        }

        findViewById<Button>(R.id.buttonEnter).setOnClickListener {
            if (gameCore.isPouse() || isAnimating || cooldownActive) return@setOnClickListener
            if (isInputBlocked) return@setOnClickListener

            if (gameCore.isPouse()) {
                gameCore.startOver()
                newRound()
            }

            val row = gameCore.getCurRow()
            val currentWord = StringBuilder()
            for (col in 0 until colCount) {
                currentWord.append(gameCore.getChar(row, col))
            }

            if (currentWord.contains(' ')) {
                Toast.makeText(this, "Please enter a complete word", Toast.LENGTH_SHORT).show()
                shakeCurrentRow()
                return@setOnClickListener
            }

            if (!gameCore.searchWord(currentWord.toString())) {
                showTemporaryMessage("Not in word list")
                shakeCurrentRow()
                return@setOnClickListener
            }

            if (gameCore.enter()) {
                isAnimating = true

                for (col in 0 until colCount) {
                    val delay = col * 250L
                    val textView = texts[row][col]
                    val result = gameCore.validateChar(row, col)
                    val backgroundId = when (result) {
                        gameCore.IN_WORD -> R.drawable.letter_in_word
                        gameCore.IN_PLACE -> R.drawable.letter_in_place
                        else -> R.drawable.letter_not_in
                    }

                    textView.postDelayed({
                        textView.animate()
                            .rotationX(90f)
                            .setDuration(150)
                            .withEndAction {
                                textView.background = ContextCompat.getDrawable(this@MainActivity, backgroundId)
                                textView.setTextColor(ContextCompat.getColor(this, R.color.white))
                                textView.rotationX = -90f
                                textView.animate()
                                    .rotationX(0f)
                                    .setDuration(150)
                                    .start()

                                val letter = textView.text[0]
                                tintKeyboardLetter(letter, result)

                                if (col == colCount - 1) {
                                    isAnimating = false
                                    // Save state after animation completes
                                    saveFullGameState()
                                }
                            }
                            .start()
                    }, delay)
                }

                if (gameCore.getResult()) {
                    isWon = true
                    countWins++

                    // Record the win in statistics
                    val app = application as WordifyApplication
                    app.recordGamePlayed(true)

                    Toast.makeText(this, "Correct! The word was: ${gameCore.getFinalWord()}", Toast.LENGTH_SHORT).show()

                    // Start cooldown timer (10 seconds)
                    startCooldown()

                    return@setOnClickListener
                }

                currentRow++

                if (currentRow >= rowCount) {
                    isInputBlocked = true

                    // Record the loss in statistics
                    val app = application as WordifyApplication
                    app.recordGamePlayed(false)

                    Toast.makeText(this, "Game over! The word was: ${gameCore.getFinalWord()}", Toast.LENGTH_SHORT).show()

                    // Also start cooldown after losing
                    startCooldown()
                }
            }
        }

        findViewById<Button>(R.id.buttonErase).setOnClickListener {
            if (gameCore.isPouse() || isAnimating || cooldownActive) return@setOnClickListener
            if (isInputBlocked) return@setOnClickListener

            if (gameCore.isPouse()) {
                gameCore.startOver()
                newRound()
            }

            gameCore.erase()
            val row = gameCore.getCurRow()
            val col = gameCore.getCurCol()
            texts[row][col].text = " "

            // Save state after erase
            saveFullGameState()
        }
    }

    // Start cooldown timer
    private fun startCooldown() {
        cooldownActive = true
        isInputBlocked = true  // Ensure input is blocked during cooldown
        showTemporaryMessage("New game in 10 seconds...", true)

        // Remove any pending cooldown tasks
        cooldownHandler.removeCallbacks(cooldownRunnable)

        // Schedule game reset after cooldown
        cooldownHandler.postDelayed(cooldownRunnable, cooldownDuration)

        // Save the cooldown state
        saveCooldownState(System.currentTimeMillis() + cooldownDuration)

        // Save the full game state
        saveFullGameState()
    }

    // Reset game after cooldown
    private fun resetGame() {
        // Clear all saved game state first
        clearGameState()

        // Reset all game variables
        isWon = false
        isInputBlocked = false  // Explicitly unblock input
        cooldownActive = false
        currentRow = 0

        // Reset the game core
        gameCore.startOver()

        // Start a new round
        newRound()

        // Clear the message
        findViewById<TextView>(R.id.daily_answer).visibility = View.INVISIBLE

        Toast.makeText(this, "New game started!", Toast.LENGTH_SHORT).show()
    }

    // Save cooldown state
    private fun saveCooldownState(endTimeMillis: Long) {
        val prefs = getSharedPreferences(getPreferenceName(), Context.MODE_PRIVATE)
        val editor = prefs.edit()

        editor.putBoolean(KEY_COOLDOWN_ACTIVE, cooldownActive)
        editor.putLong(KEY_COOLDOWN_END_TIME, endTimeMillis)

        editor.apply()
    }

    // Check if cooldown should be active when resuming the app
    private fun checkCooldownOnResume() {
        val prefs = getSharedPreferences(getPreferenceName(), Context.MODE_PRIVATE)
        val isCooldown = prefs.getBoolean(KEY_COOLDOWN_ACTIVE, false)

        if (isCooldown) {
            val endTime = prefs.getLong(KEY_COOLDOWN_END_TIME, 0)
            val currentTime = System.currentTimeMillis()

            if (currentTime < endTime) {
                // Cooldown still active, restart timer for remaining time
                val remainingTime = endTime - currentTime
                cooldownActive = true
                isInputBlocked = true  // Ensure input stays blocked
                showTemporaryMessage("New game in ${remainingTime / 1000} seconds...", true)

                cooldownHandler.removeCallbacks(cooldownRunnable)
                cooldownHandler.postDelayed(cooldownRunnable, remainingTime)
            } else {
                // Cooldown expired while app was in background
                resetGame()
            }
        }
    }

    // Add shake animation to current row
    private fun shakeCurrentRow() {
        val row = gameCore.getCurRow()
        val shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake_animation)

        // Apply the shake animation to each TextView in the current row
        for (col in 0 until colCount) {
            texts[row][col].startAnimation(shakeAnimation)
        }
    }

    private fun initTexts() {
        texts = MutableList(rowCount) { mutableListOf() }
        for (row in 0 until rowCount) {
            for (col in 0 until colCount) {
                val resID = resources.getIdentifier("text${col + 1}col${row + 1}row", "id", packageName)
                texts[row].add(findViewById(resID))
            }
        }
    }

    private fun newRound() {
        gameCore.setWord()
        for (row in 0 until rowCount) {
            for (col in 0 until colCount) {
                texts[row][col].apply {
                    background = ContextCompat.getDrawable(this@MainActivity, R.drawable.letter_border)
                    text = " "
                    setTextColor(ContextCompat.getColor(this@MainActivity, R.color.game_text))
                }
            }
        }
        // Reset keyboard colors
        resetKeyboardColors()
        Log.e("Word", "=============---- ${gameCore.getFinalWord()}")

        // Save new game state
        saveFullGameState()
    }

    private fun resetKeyboardColors() {
        for (c in 65..90) {
            val buttonId = resources.getIdentifier("button${c.toChar()}", "id", packageName)
            val button = findViewById<Button>(buttonId)
            button?.backgroundTintList = ContextCompat.getColorStateList(this, R.color.game_backgroundlog)
        }
    }

    private fun tintKeyboardLetter(letter: Char, result: Int) {
        val buttonId = resources.getIdentifier("button${letter.uppercaseChar()}", "id", packageName)
        val button = findViewById<Button>(buttonId)
        val currentTint = button?.backgroundTintList
        val currentColor = currentTint?.defaultColor ?: return

        val greenColor = ContextCompat.getColor(this, R.color.green)
        if (currentColor == greenColor) return

        val yellowColor = ContextCompat.getColor(this, R.color.yellow)
        if (currentColor == yellowColor && result != gameCore.IN_PLACE) return

        val colorRes = when (result) {
            gameCore.IN_PLACE -> R.color.green
            gameCore.IN_WORD -> R.color.yellow
            else -> R.color.gray
        }

        button.backgroundTintList = ContextCompat.getColorStateList(this, colorRes)

        // Save keyboard state after changes
        saveKeyboardState()
    }

    // Save the full game state (both GameCore and additional data)
    private fun saveFullGameState() {
        gameCore.saveGameState()
        saveGameStateData()
        saveKeyboardState()
    }

    // Save additional game state data specific to MainActivity
    private fun saveGameStateData() {
        val prefs = getSharedPreferences(getPreferenceName(), Context.MODE_PRIVATE)
        val editor = prefs.edit()

        editor.putBoolean(KEY_IS_WON, isWon)
        editor.putInt(KEY_WINS_COUNT, countWins)
        editor.putInt(KEY_CURRENT_ROW, currentRow)
        editor.putBoolean(KEY_INPUT_BLOCKED, isInputBlocked)
        editor.putBoolean(KEY_COOLDOWN_ACTIVE, cooldownActive)

        editor.apply()
    }

    // Save keyboard state (which keys are colored)
    private fun saveKeyboardState() {
        val prefs = getSharedPreferences(getPreferenceName(), Context.MODE_PRIVATE)
        val editor = prefs.edit()

        val keyboardState = StringBuilder()

        for (c in 65..90) {
            val letter = c.toChar()
            val buttonId = resources.getIdentifier("button$letter", "id", packageName)
            val button = findViewById<Button>(buttonId)
            val tint = button?.backgroundTintList?.defaultColor

            val colorCode = when (tint) {
                ContextCompat.getColor(this, R.color.green) -> 'G'
                ContextCompat.getColor(this, R.color.yellow) -> 'Y'
                ContextCompat.getColor(this, R.color.gray) -> 'X'
                else -> '-'
            }

            keyboardState.append("$letter$colorCode,")
        }

        editor.putString(KEY_KEYBOARD_STATE, keyboardState.toString())
        editor.apply()
    }

    // Load the full game state
    private fun loadFullGameState(): Boolean {
        val gameCoreLoaded = gameCore.loadGameState()

        if (gameCoreLoaded) {
            // Load additional game state data
            loadGameStateData()
            // Update UI with loaded game state
            updateUIFromGameCore()
            loadKeyboardState()
            // Check if cooldown should be active
            checkCooldownOnResume()
            return true
        }
        return false
    }

    // Load additional game state data
    private fun loadGameStateData() {
        val prefs = getSharedPreferences(getPreferenceName(), Context.MODE_PRIVATE)

        isWon = prefs.getBoolean(KEY_IS_WON, false)
        countWins = prefs.getInt(KEY_WINS_COUNT, 0)
        currentRow = prefs.getInt(KEY_CURRENT_ROW, 0)
        isInputBlocked = prefs.getBoolean(KEY_INPUT_BLOCKED, false)
        cooldownActive = prefs.getBoolean(KEY_COOLDOWN_ACTIVE, false)
    }

    // Load keyboard state
    private fun loadKeyboardState() {
        val prefs = getSharedPreferences(getPreferenceName(), Context.MODE_PRIVATE)
        val keyboardState = prefs.getString(KEY_KEYBOARD_STATE, null) ?: return

        val keyStates = keyboardState.split(",")

        for (keyState in keyStates) {
            if (keyState.length >= 2) {
                val letter = keyState[0]
                val colorCode = keyState[1]

                val buttonId = resources.getIdentifier("button$letter", "id", packageName)
                val button = findViewById<Button>(buttonId)

                val colorRes = when (colorCode) {
                    'G' -> R.color.green
                    'Y' -> R.color.yellow
                    'X' -> R.color.gray
                    else -> null
                }

                if (colorRes != null) {
                    button?.backgroundTintList = ContextCompat.getColorStateList(this, colorRes)
                }
            }
        }
    }

    // Update UI to match the game core state
    private fun updateUIFromGameCore() {
        for (row in 0 until rowCount) {
            for (col in 0 until colCount) {
                val char = gameCore.getChar(row, col)

                if (char != ' ') {
                    val textView = texts[row][col]
                    textView.text = char.toString()

                    // If this row is completed (not the current row), apply colors
                    if (row < gameCore.getCurRow()) {
                        val result = gameCore.validateChar(row, col)
                        val backgroundId = when (result) {
                            gameCore.IN_WORD -> R.drawable.letter_in_word
                            gameCore.IN_PLACE -> R.drawable.letter_in_place
                            else -> R.drawable.letter_not_in
                        }
                        textView.background = ContextCompat.getDrawable(this, backgroundId)
                        textView.setTextColor(ContextCompat.getColor(this, R.color.white))
                    }
                }
            }
        }
    }

    // Clear saved game state
    private fun clearGameState() {
        gameCore.clearGameState()
        val prefs = getSharedPreferences(getPreferenceName(), Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }

    private fun showTemporaryMessage(message: String, keepVisible: Boolean = false) {
        val answered = findViewById<TextView>(R.id.daily_answer)
        answered.text = message
        answered.visibility = View.VISIBLE

        if (!keepVisible) {
            Handler(Looper.getMainLooper()).postDelayed({
                answered.visibility = View.INVISIBLE
            }, 2000) // Hide after 2 seconds
        }
    }
}