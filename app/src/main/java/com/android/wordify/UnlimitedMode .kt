package com.android.wordify

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import org.w3c.dom.Text

class UnlimitedMode : ComponentActivity() {
    private lateinit var texts: MutableList<MutableList<TextView>>
    private val rowCount = 6
    private val colCount = 5
    private lateinit var gameCore: GameCore
    private var currentRow = 0
    private var isAnimating = false // Flag to prevent input during animations

    // Keys for keyboard state in SharedPreferences
    private val PREFS_NAME = "WordifyUnlimitedGameState" // Unique name for unlimited mode
    private val KEY_KEYBOARD_STATE = "keyboard_state"
    private val KEY_CURRENT_ROW = "current_row"
    private val KEY_SHOW_ANSWER = "show_answer"
    private val KEY_SHOW_NEXT_WORD = "show_next_word"

    @SuppressLint("SetTextI18s")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.unli_game_screen)

        val nextWord = findViewById<TextView>(R.id.nextword)
        nextWord.setOnClickListener {
            resetBoard()
        }

        val backlanding = findViewById<ImageView>(R.id.back_to_landing1)
        backlanding.setOnClickListener {
            // Save game state before leaving
            saveFullGameState()
            val intent = Intent(this, LandingPage::class.java)
            startActivity(intent)
        }

        val help = findViewById<ImageView>(R.id.help_gamescreen1)
        help.setOnClickListener {
            // Save game state before showing help
            saveFullGameState()
            val intent = Intent(this, HelpPage::class.java)
            startActivity(intent)
        }

        // Pass PREFS_NAME to GameCore as the preference name
        gameCore = GameCore(rowCount, this, PREFS_NAME)
        initTexts()
        setEventListeners()

        // Try to load saved game first
        if (!loadFullGameState()) {
            // If no saved game, start a new round
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

    private fun setEventListeners() {
        for (c in 90 downTo 65) {
            val resID = resources.getIdentifier("button${c.toChar()}", "id", packageName)
            val btn = findViewById<Button>(resID)
            btn.setOnClickListener {
                if (gameCore.isPouse() || isAnimating) return@setOnClickListener
                val row = gameCore.getCurRow()
                val col = gameCore.getCurCol()
                if (gameCore.setNextChar(c.toChar())) {
                    texts[row][col].text = c.toChar().toString()
                    // Save state after each character input
                    saveFullGameState()
                }
            }
        }

        val btnEnter = findViewById<Button>(R.id.buttonEnter)
        btnEnter.setOnClickListener {
            if (gameCore.isPouse() || isAnimating) return@setOnClickListener
            val row = gameCore.getCurRow()

            val currentWord = StringBuilder()
            for (col in 0 until colCount) {
                currentWord.append(gameCore.getChar(row, col))
            }

            if (currentWord.contains(' ')) {
                // Show "Not in word list" message in unli_answer1
                showTemporaryMessage("Please enter a complete word")
                shakeCurrentRow()
                return@setOnClickListener
            }

            if (!gameCore.searchWord(currentWord.toString())) {
                // Show "Not in word list" message in unli_answer1
                showTemporaryMessage("Not in word list")
                shakeCurrentRow()
                return@setOnClickListener
            }

            if (gameCore.enter()) {
                isAnimating = true // Begin animation lock

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
                                textView.background = ContextCompat.getDrawable(this@UnlimitedMode, backgroundId)
                                textView.setTextColor(ContextCompat.getColor(this, R.color.white))
                                textView.rotationX = -90f
                                textView.animate()
                                    .rotationX(0f)
                                    .setDuration(150)
                                    .start()

                                val letter = textView.text[0]
                                tintKeyboardLetter(letter, result)

                                // If this is the last column, end animation lock
                                if (col == colCount - 1) {
                                    isAnimating = false

                                    // Save game state after animation completes
                                    saveFullGameState()
                                }
                            }
                            .start()
                    }, delay)
                }

                if (gameCore.getResult()) {
                    val answered = findViewById<TextView>(R.id.unli_answer1)
                    answered.text = gameCore.getFinalWord()
                    answered.visibility = View.VISIBLE
                    val nextWord = findViewById<TextView>(R.id.nextword)
                    nextWord.visibility = View.VISIBLE
                    showTemporaryMessage("Correct! The word was: ${gameCore.getFinalWord()}", keepVisible = true)

                    // Save state with answer showing
                    saveGameStateData(true, true)
                    return@setOnClickListener
                }

                currentRow++

                if (currentRow >= rowCount) {
                    val answered = findViewById<TextView>(R.id.unli_answer1)
                    answered.text = gameCore.getFinalWord()
                    answered.visibility = View.VISIBLE
                    val nextWord = findViewById<TextView>(R.id.nextword)
                    nextWord.visibility = View.VISIBLE
                    showTemporaryMessage("Game over! The word was: ${gameCore.getFinalWord()}", keepVisible = true)

                    // Save state with answer showing
                    saveGameStateData(true, true)
                }
            }
        }

        val btnErase = findViewById<Button>(R.id.buttonErase)
        btnErase.setOnClickListener {
            if (gameCore.isPouse() || isAnimating) return@setOnClickListener
            gameCore.erase()
            val row = gameCore.getCurRow()
            val col = gameCore.getCurCol()
            texts[row][col].text = " "

            // Save game state after erasing
            saveFullGameState()
        }
    }

    // Show message in unli_answer1 and hide after delay
    private fun showTemporaryMessage(message: String, keepVisible: Boolean = false) {
        val answered = findViewById<TextView>(R.id.unli_answer1)
        answered.text = message
        answered.visibility = View.VISIBLE

        if (!keepVisible) {
            Handler(Looper.getMainLooper()).postDelayed({
                answered.visibility = View.INVISIBLE
            }, 2000) // Hide after 2 seconds
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
        clearBoard()
        resetKeyboardColors()
        Log.d("Word", "New word: ${gameCore.getFinalWord()}")

        // Save the new state
        saveFullGameState()
    }

    private fun clearBoard() {
        for (row in 0 until rowCount) {
            for (col in 0 until colCount) {
                texts[row][col].background = ContextCompat.getDrawable(this, R.drawable.letter_border)
                texts[row][col].text = " "
                texts[row][col].setTextColor(ContextCompat.getColor(this, R.color.game_text))
            }
        }
    }

    private fun resetBoard() {
        val answered = findViewById<TextView>(R.id.unli_answer1)
        answered.visibility = View.INVISIBLE

        val nextWord = findViewById<TextView>(R.id.nextword)
        nextWord.visibility = View.INVISIBLE

        gameCore.startOver()
        currentRow = 0
        gameCore.clearGameState() // Clear saved game state when starting a new game

        // Clear UI-specific state also
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.remove(KEY_SHOW_ANSWER)
        editor.remove(KEY_SHOW_NEXT_WORD)
        editor.remove(KEY_KEYBOARD_STATE)
        editor.remove(KEY_CURRENT_ROW)
        editor.apply()

        newRound()
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

    // Save the full game state (both GameCore and keyboard colors)
    private fun saveFullGameState() {
        gameCore.saveGameState()
        saveKeyboardState()

        // Check if answer is showing
        val answered = findViewById<TextView>(R.id.unli_answer1)
        val nextWord = findViewById<TextView>(R.id.nextword)
        saveGameStateData(
            answered.visibility == View.VISIBLE,
            nextWord.visibility == View.VISIBLE
        )
    }

    // Save additional game state data specific to UnlimitedMode
    private fun saveGameStateData(showAnswer: Boolean = false, showNextWord: Boolean = false) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        editor.putInt(KEY_CURRENT_ROW, currentRow)
        editor.putBoolean(KEY_SHOW_ANSWER, showAnswer)
        editor.putBoolean(KEY_SHOW_NEXT_WORD, showNextWord)

        editor.apply()
    }

    // Load the full game state (both GameCore and keyboard colors)
    private fun loadFullGameState(): Boolean {
        val gameCoreLoaded = gameCore.loadGameState()

        if (gameCoreLoaded) {
            // Load additional game state data
            loadGameStateData()

            // Update UI with loaded game state
            updateUIFromGameState()
            loadKeyboardState()
            return true
        }
        return false
    }

    // Load additional game state data
    private fun loadGameStateData() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        currentRow = prefs.getInt(KEY_CURRENT_ROW, 0)

        // Check if we need to show the answer and next word button
        val showAnswer = prefs.getBoolean(KEY_SHOW_ANSWER, false)
        val showNextWord = prefs.getBoolean(KEY_SHOW_NEXT_WORD, false)

        val answered = findViewById<TextView>(R.id.unli_answer1)
        val nextWord = findViewById<TextView>(R.id.nextword)

        if (showAnswer) {
            answered.text = gameCore.getFinalWord()
            answered.visibility = View.VISIBLE
        } else {
            answered.visibility = View.INVISIBLE
        }

        if (showNextWord) {
            nextWord.visibility = View.VISIBLE
        } else {
            nextWord.visibility = View.INVISIBLE
        }
    }

    // Update UI to match the game core state
    private fun updateUIFromGameState() {
        clearBoard()

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

    // Save keyboard state (which keys are colored)
    private fun saveKeyboardState() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
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

    // Load keyboard state (which keys are colored)
    private fun loadKeyboardState() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
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
}