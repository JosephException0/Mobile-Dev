package com.android.wordify

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import org.w3c.dom.Text

class UnlimitedMode :  AppCompatActivity() {
    private lateinit var texts: MutableList<MutableList<TextView>>
    private val rowCount = 6
    private val colCount = 5
    private lateinit var gameCore: GameCore
    private var currentRow = 0
    private var isAnimating = false

    private val PREFS_BASE_NAME = "WordifyUnlimitedGameState"
    private val KEY_KEYBOARD_STATE = "keyboard_state"
    private val KEY_CURRENT_ROW = "current_row"
    private val KEY_SHOW_ANSWER = "show_answer"
    private val KEY_SHOW_NEXT_WORD = "show_next_word"

    private fun getPreferenceName(): String {
        val app = application as WordifyApplication
        return app.getUserPreferenceName(PREFS_BASE_NAME)
    }

    @SuppressLint("SetTextI18s")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.unli_game_screen)

        val sharedPreferences = getSharedPreferences("Mode", Context.MODE_PRIVATE)
        val nightMode = sharedPreferences.getBoolean("night", false)

        if (nightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        val nextWord = findViewById<TextView>(R.id.nextword)
        nextWord.setOnClickListener {
            resetBoard()
        }

        val backlanding1 = findViewById<Button>(R.id.back_to_landing1)
        backlanding1.setOnClickListener {
            saveFullGameState()
            val intent = Intent(this, LandingPage::class.java)
            startActivity(intent)
        }

        val help = findViewById<ImageView>(R.id.help_gamescreen1)
        help.setOnClickListener {
            saveFullGameState()
            val intent = Intent(this, HelpPage::class.java)
            startActivity(intent)
        }

        // Pass PREFS_NAME to GameCore
        gameCore = GameCore(rowCount, this, PREFS_BASE_NAME)
        initTexts()
        setEventListeners()

        if (!loadFullGameState()) {
            newRound()
        }
        applyHighContrastIfEnabled()
    }

    override fun onPause() {
        super.onPause()
        saveFullGameState()
    }

    override fun onStop() {
        super.onStop()
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
                showTemporaryMessage("Please enter a complete word")
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
                                textView.background = ContextCompat.getDrawable(this@UnlimitedMode, backgroundId)
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
                    showTemporaryMessage("The word was: ${gameCore.getFinalWord()}", keepVisible = true)

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

            saveFullGameState()
        }
    }

    private fun showTemporaryMessage(message: String, keepVisible: Boolean = false) {
        val answered = findViewById<TextView>(R.id.unli_answer1)
        answered.text = message
        answered.visibility = View.VISIBLE

        if (!keepVisible) {
            Handler(Looper.getMainLooper()).postDelayed({
                answered.visibility = View.INVISIBLE
            }, 2000)
        }
    }

    // Add shake animation to current row
    private fun shakeCurrentRow() {
        val row = gameCore.getCurRow()
        val shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake_animation)

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
        gameCore.clearGameState()

        val prefs = getSharedPreferences(getPreferenceName(), Context.MODE_PRIVATE)
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

        saveKeyboardState()
    }

    // Save the full game state (both GameCore and keyboard colors)
    private fun saveFullGameState() {
        gameCore.saveGameState()
        saveKeyboardState()

        val answered = findViewById<TextView>(R.id.unli_answer1)
        val nextWord = findViewById<TextView>(R.id.nextword)
        saveGameStateData(
            answered.visibility == View.VISIBLE,
            nextWord.visibility == View.VISIBLE
        )
    }

    // Save additional game state data specific to UnlimitedMode
    private fun saveGameStateData(showAnswer: Boolean = false, showNextWord: Boolean = false) {
        val prefs = getSharedPreferences(getPreferenceName(), Context.MODE_PRIVATE)
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
            loadGameStateData()

            updateUIFromGameState()
            loadKeyboardState()
            return true
        }
        return false
    }

    // Load additional game state data
    private fun loadGameStateData() {
        val prefs = getSharedPreferences(getPreferenceName(), Context.MODE_PRIVATE)

        currentRow = prefs.getInt(KEY_CURRENT_ROW, 0)

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

    // Load keyboard state (which keys are colored)
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

    private fun applyHighContrastIfEnabled() {
        val sharedPreferences = getSharedPreferences("Mode", Context.MODE_PRIVATE)
        val isHighContrast = sharedPreferences.getBoolean("highContrast", false)

        if (isHighContrast) {
            val bgmain = findViewById<ConstraintLayout>(R.id.bg_main)
            val bg = findViewById<RelativeLayout>(R.id.root_game)
            val bg1 = findViewById<LinearLayout>(R.id.linearLayout)
            val cr1 = findViewById<TextView>(R.id.text1col1row)
            val cr2 = findViewById<TextView>(R.id.text2col1row)
            val cr3 = findViewById<TextView>(R.id.text3col1row)
            val cr4 = findViewById<TextView>(R.id.text4col1row)
            val cr5 = findViewById<TextView>(R.id.text5col1row)
            val cr6 = findViewById<TextView>(R.id.text1col2row)
            val cr7 = findViewById<TextView>(R.id.text2col2row)
            val cr8 = findViewById<TextView>(R.id.text3col2row)
            val cr9 = findViewById<TextView>(R.id.text4col2row)
            val cr10 = findViewById<TextView>(R.id.text5col2row)
            val cr11 = findViewById<TextView>(R.id.text1col3row)
            val cr12 = findViewById<TextView>(R.id.text2col3row)
            val cr13 = findViewById<TextView>(R.id.text3col3row)
            val cr14 = findViewById<TextView>(R.id.text4col3row)
            val cr15 = findViewById<TextView>(R.id.text5col3row)
            val cr16 = findViewById<TextView>(R.id.text1col4row)
            val cr17 = findViewById<TextView>(R.id.text2col4row)
            val cr18 = findViewById<TextView>(R.id.text3col4row)
            val cr19 = findViewById<TextView>(R.id.text4col4row)
            val cr20 = findViewById<TextView>(R.id.text5col4row)
            val cr21 = findViewById<TextView>(R.id.text1col5row)
            val cr22 = findViewById<TextView>(R.id.text2col5row)
            val cr23 = findViewById<TextView>(R.id.text3col5row)
            val cr24 = findViewById<TextView>(R.id.text4col5row)
            val cr25 = findViewById<TextView>(R.id.text5col5row)
            val cr26 = findViewById<TextView>(R.id.text1col6row)
            val cr27 = findViewById<TextView>(R.id.text2col6row)
            val cr28 = findViewById<TextView>(R.id.text3col6row)
            val cr29 = findViewById<TextView>(R.id.text4col6row)
            val cr30 = findViewById<TextView>(R.id.text5col6row)
            val btn = findViewById<Button>(R.id.back_to_landing1)
            val title = findViewById<TextView>(R.id.title)
            val help = findViewById<ImageView>(R.id.help_gamescreen1)

            title?.setTextColor(ContextCompat.getColor(this, R.color.background_lp))
            bg?.setBackgroundColor(ContextCompat.getColor(this, R.color.hc_background_dev))
            bgmain?.setBackgroundColor(ContextCompat.getColor(this, R.color.hc_background_dev))
            bg1?.setBackgroundColor(ContextCompat.getColor(this, R.color.hc_background_dev))
            cr1?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
            cr2?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
            cr3?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
            cr4?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
            cr5?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
            cr6?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
            cr7?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
            cr8?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
            cr9?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
            cr10?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
            cr11?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
            cr12?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
            cr13?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
            cr14?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
            cr15?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
            cr16?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
            cr17?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
            cr18?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
            cr19?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
            cr20?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
            cr21?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
            cr22?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
            cr23?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
            cr24?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
            cr25?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
            cr26?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
            cr27?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
            cr28?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
            cr29?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))
            cr30?.setTextColor(ContextCompat.getColor(this, R.color.hc_name))


            btn?.compoundDrawablesRelative?.getOrNull(0)?.mutate()?.let { drawable ->
                drawable.setTint(ContextCompat.getColor(this, R.color.hc_text_settings))
                btn.invalidate()
            }

            help?.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.hc_text_settings)
            )
        }
    }
}
