package com.android.wordify

import kotlin.random.Random
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast

class GameCore(
    private var rowCount: Int = 6,
    private val context: Context,
    private val basePreferenceName: String = "WordifyGameState" // Now a base name
) {
    val IN_WORD = 0
    val IN_PLACE = 1
    val NOT_IN = 2

    // Other variables remain the same
    private var pouse = false
    private var curRow: Int = 0
    private var curCol: Int = 0
    private var rows = mutableListOf<MutableList<Char>>()
    private lateinit var word: String
    private var words: List<String> = listOf()

    // SharedPreferences key constants remain the same
    private val KEY_CURRENT_WORD = "current_word"
    private val KEY_CURRENT_ROW = "current_row"
    private val KEY_CURRENT_COL = "current_col"
    private val KEY_IS_PAUSED = "is_paused"
    private val KEY_BOARD_STATE = "board_state"

    // Get user-specific preference name
    private fun getPreferenceName(): String {
        val app = context.applicationContext as WordifyApplication
        return app.getUserPreferenceName(basePreferenceName)
    }
    init {
        for (i in 0 until rowCount) {
            val row = MutableList(5) { ' ' }
            rows.add(row)
        }

        // Load words right away
        words = loadWordsFromAssets(context)
    }

    fun getFinalWord(): String {
        return word
    }

    fun getResult(): Boolean {
        for (row in 0 until rowCount) {
            if (rows[row].joinToString(separator = "") == word) {
                pouse = true
                return true
            }
        }
        return false
    }

    fun isPouse(): Boolean {
        return pouse
    }

    fun setPouse(paused: Boolean) {
        pouse = paused
    }

    fun startOver() {
        curCol = 0
        curRow = 0
        pouse = false
        for (row in 0 until rowCount) {
            for (col in 0 until 5) {
                rows[row][col] = ' '
            }
        }
        setWord()
    }

    fun getChar(row: Int, col: Int): Char {
        if (row < 0 || row >= rowCount || col < 0 || col >= 5) {
            return ' '
        }
        return rows[row][col]
    }

    fun setChar(row: Int, col: Int, char: Char) {
        if (row >= 0 && row < rowCount && col >= 0 && col < 5) {
            rows[row][col] = char
        }
    }

    fun setNextChar(c: Char): Boolean {
        if (rows[curRow][curCol] == ' ') {
            rows[curRow][curCol] = c
            if (curCol < 4) {
                curCol++
            }
            return true
        }
        return false
    }

    fun erase() {
        if (curCol > 0 && rows[curRow][curCol] == ' ') {
            curCol--
        }
        rows[curRow][curCol] = ' '
    }

    fun enter(): Boolean {
        if (curCol == 4 && curRow <= rowCount) {
            curCol = 0
            curRow++
            if (curRow == rowCount) {
                pouse = true
            }
            return true
        }
        return false
    }

    fun validateChar(row: Int, col: Int): Int {
        if (rows[row][col] == word[col]) {
            return IN_PLACE
        } else if (rows[row][col] in word) {
            return IN_WORD
        }
        return NOT_IN
    }

    fun getCurRow(): Int {
        return curRow
    }

    fun setCurRow(row: Int) {
        curRow = row
    }

    fun getCurCol(): Int {
        return curCol
    }

    fun setCurCol(col: Int) {
        curCol = col
    }

    fun setWord() {
        if (words.isEmpty()) {
            words = loadWordsFromAssets(context)
        }
        word = words[Random.nextInt(words.size)]
    }

    fun setSpecificWord(newWord: String) {
        if (newWord.length == 5) {
            word = newWord.uppercase()
        }
    }

    fun loadWordsFromAssets(context: Context): List<String> {
        val words = mutableListOf<String>()
        try {
            val inputStream = context.assets.open("data.txt")
            val reader = inputStream.bufferedReader()

            val allWords = reader.readLines()
                .map { it.trim().uppercase() }
                .filter { it.length == 5 }

            words.addAll(allWords)
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }

        return words
    }

    fun searchWord(word: String): Boolean {
        return words.contains(word.uppercase())
    }

    // Save game state to SharedPreferences
    fun saveGameState() {
        val prefs = context.getSharedPreferences(getPreferenceName(), Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // Save basic game state
        editor.putString(KEY_CURRENT_WORD, word)
        editor.putInt(KEY_CURRENT_ROW, curRow)
        editor.putInt(KEY_CURRENT_COL, curCol)
        editor.putBoolean(KEY_IS_PAUSED, pouse)

        // Save board state (convert 2D array to single string for storage)
        val boardState = StringBuilder()
        for (row in 0 until rowCount) {
            for (col in 0 until 5) {
                val char = rows[row][col]
                boardState.append(if (char == ' ') '_' else char)
            }
        }
        editor.putString(KEY_BOARD_STATE, boardState.toString())

        editor.apply()
    }


    // Load game state from SharedPreferences
    fun loadGameState(): Boolean {
        val prefs = context.getSharedPreferences(getPreferenceName(), Context.MODE_PRIVATE)

        // Check if we have a saved game
        if (!prefs.contains(KEY_CURRENT_WORD)) {
            return false
        }

        // Load basic game state
        val savedWord = prefs.getString(KEY_CURRENT_WORD, null)
        if (savedWord != null) {
            word = savedWord
            curRow = prefs.getInt(KEY_CURRENT_ROW, 0)
            curCol = prefs.getInt(KEY_CURRENT_COL, 0)
            pouse = prefs.getBoolean(KEY_IS_PAUSED, false)

            // Load board state
            val boardState = prefs.getString(KEY_BOARD_STATE, "")
            if (boardState != null && boardState.length == rowCount * 5) {
                var index = 0
                for (row in 0 until rowCount) {
                    for (col in 0 until 5) {
                        val char = boardState[index++]
                        rows[row][col] = if (char == '_') ' ' else char
                    }
                }
            }
            return true
        }
        return false
    }


    // Clear saved game state
    fun clearGameState() {
        val prefs = context.getSharedPreferences(getPreferenceName(), Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }

}