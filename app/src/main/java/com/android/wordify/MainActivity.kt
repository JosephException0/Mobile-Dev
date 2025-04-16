package com.android.wordify

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private lateinit var texts: MutableList<MutableList<TextView>>
    private val rowCount = 6
    private val colCount = 5
    private var IsWon = false
    private var countWins = 0
    private lateinit var gameCore: GameCore
    private var currentRow = 0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_screen)

        val backlanding = findViewById<ImageView>(R.id.back_to_landing)

        backlanding.setOnClickListener {
            val intent = Intent(this, LandingPage::class.java)
            startActivity(intent)
        }

        gameCore = GameCore(rowCount, this)
        initTexts()
        setEventListeners()

        newRound()
    }

    private fun setEventListeners() {
        for (c in 90 downTo 65) {
            val resID = resources.getIdentifier("button${c.toChar()}", "id", packageName)
            val btn = findViewById<Button>(resID)
            btn.setOnClickListener {

                if(IsWon){
                    return@setOnClickListener
                }
                if (currentRow >= rowCount) return@setOnClickListener

                if (gameCore.isPouse()) {
                    gameCore.startOver()
                    newRound()
                }
                val row = gameCore.getCurRow()
                val col = gameCore.getCurCol()
                if (gameCore.setNextChar(c.toChar())) {
                    texts[row][col].text = c.toChar().toString()
                }
            }
        }

        val btnEnter = findViewById<Button>(R.id.buttonEnter)
        btnEnter.setOnClickListener {

            if(IsWon){
                return@setOnClickListener
            }
            if (currentRow >= rowCount) return@setOnClickListener

            if (gameCore.isPouse()) {
                gameCore.startOver()
                newRound()
            }

            Toast.makeText(this, "Correct word: ${gameCore.getFinalWord()}", Toast.LENGTH_SHORT).show()
            val row = gameCore.getCurRow()

            if (gameCore.enter()) {

                for (col in 0 until colCount) {
                    val result = gameCore.validateChar(row, col)
                    val backgroundId = when (result) {
                        gameCore.IN_WORD -> R.drawable.letter_in_word
                        gameCore.IN_PLACE -> R.drawable.letter_in_place
                        else -> R.drawable.letter_not_in
                    }

                    texts[row][col].apply {
                        background = ContextCompat.getDrawable(this@MainActivity, backgroundId)
                        setTextColor(ContextCompat.getColor(context, R.color.white))
                    }

                    val letter = texts[row][col].text[0]
                    tintKeyboardLetter(letter, result)
                }

                if (gameCore.getResult()) {
                    countWins++
                    IsWon=true
                }

                currentRow++


            }
        }

        val btnErase = findViewById<Button>(R.id.buttonErase)
        btnErase.setOnClickListener {

            if (currentRow >= rowCount) return@setOnClickListener

            if (gameCore.isPouse()) {
                gameCore.startOver()
                newRound()
            }
            gameCore.erase()
            val row = gameCore.getCurRow()
            val col = gameCore.getCurCol()
            texts[row][col].text = " "
        }
    }

    private fun initTexts() {
        if(IsWon){
            return
        }
        texts = MutableList(rowCount) { mutableListOf() }
        for (row in 0 until rowCount) {
            for (col in 0 until colCount) {
                val resID =
                    resources.getIdentifier("text${col + 1}col${row + 1}row", "id", packageName)
                texts[row].add(findViewById(resID))
            }
        }
    }

    private fun newRound() {

        if(IsWon){
            return
        }

        gameCore.setWord()
        for (row in 0 until rowCount) {
            for (col in 0 until colCount) {
                texts[row][col].background = ContextCompat.getDrawable(this, R.drawable.letter_border)
                texts[row][col].text = " "
            }
        }

        Log.e("Word", "=============---- ${gameCore.getFinalWord()}")

    }

    private fun tintKeyboardLetter(letter: Char, result: Int) {
        val buttonId = resources.getIdentifier("button${letter.uppercaseChar()}", "id", packageName)
        val button = findViewById<Button>(buttonId)

        if (result == gameCore.IN_WORD) return

        val colorRes = when (result) {
            gameCore.IN_PLACE -> R.color.green
            gameCore.IN_WORD -> R.color.yellow
            else -> R.color.gray
        }

        button?.backgroundTintList = ContextCompat.getColorStateList(this, colorRes)
    }
}