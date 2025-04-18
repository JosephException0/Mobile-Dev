package com.android.wordify

import kotlin.random.Random
import android.content.Context
import android.widget.Toast

class GameCore(
    private var rowCount: Int = 6,
    private val context: Context
) {
    val IN_WORD = 0
    val IN_PLACE = 1
    val NOT_IN = 2

    private var pouse = false
    private var curRow: Int = 0
    private var curCol: Int = 0
    private var rows = mutableListOf<MutableList<Char>>()
    private lateinit var word: String
    private var words: List<String> = listOf()

    init {
        for (i in 0 until rowCount) {
            val row = MutableList(5) { ' ' }
            rows.add(row)
        }
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

    fun getCurCol(): Int {
        return curCol
    }

    fun setWord() {
        words = loadWordsFromAssets(context)
        word = words[Random.nextInt(words.size)]
    }

    fun loadWordsFromAssets(context: Context): List<String> {
        val words = mutableListOf<String>()
        try {
            val inputStream = context.assets.open("data.txt")
            val reader = inputStream.bufferedReader()

            val allWords = reader.readLines()
                .map { it.trim().uppercase() }
                .filter { it.length == 5 }

            Toast.makeText(context, "5-letter words found: ${allWords.size}", Toast.LENGTH_SHORT).show()
            words.addAll(allWords)
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }

        return words
    }


    fun searchWord(word: String): Boolean {
        Toast.makeText(context, word, Toast.LENGTH_SHORT).show()
        return words.contains(word.uppercase())
    }




}