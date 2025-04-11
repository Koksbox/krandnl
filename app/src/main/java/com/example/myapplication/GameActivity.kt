package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {
    private lateinit var game: TicTacToeGame
    private lateinit var gameStatusText: TextView
    private lateinit var buttons: Array<Button>
    private lateinit var resetButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        game = TicTacToeGame(this)
        gameStatusText = findViewById(R.id.gameStatusText)
        resetButton = findViewById(R.id.resetButton)

        buttons = arrayOf(
            findViewById(R.id.button0),
            findViewById(R.id.button1),
            findViewById(R.id.button2),
            findViewById(R.id.button3),
            findViewById(R.id.button4),
            findViewById(R.id.button5),
            findViewById(R.id.button6),
            findViewById(R.id.button7),
            findViewById(R.id.button8)
        )

        setupGame()
    }

    private fun setupGame() {
        buttons.forEachIndexed { index, button ->
            button.setOnClickListener {
                if (game.makeMove(index)) {
                    button.text = game.getLastMovePlayer()
                    updateGameStatus()
                }
            }
        }

        resetButton.setOnClickListener {
            game.resetGame()
            buttons.forEach { it.text = "" }
            updateGameStatus()
        }

        updateGameStatus()
    }

    private fun updateGameStatus() {
        gameStatusText.text = game.getGameStatus()
    }
} 