package com.example.myapplication

import android.content.Context

class TicTacToeGame(private val context: Context) {
    private var currentPlayer = "X"
    private var gameState = Array(9) { "" }
    private var gameActive = true
    private var winner: String? = null
    private var lastMovePlayer: String = "X"

    fun makeMove(position: Int): Boolean {
        // Проверяем, можно ли сделать ход
        if (!gameActive || position < 0 || position >= 9 || gameState[position].isNotEmpty()) {
            return false
        }

        // Сохраняем игрока, который делает ход
        lastMovePlayer = currentPlayer
        // Делаем ход текущего игрока
        gameState[position] = currentPlayer
        
        // Проверяем, не выиграл ли текущий игрок
        if (checkWinner(currentPlayer)) {
            gameActive = false
            winner = currentPlayer
            return true
        }

        // Проверяем ничью (все клетки заполнены)
        if (gameState.none { it.isEmpty() }) {
            gameActive = false
            return true
        }

        // Меняем игрока для следующего хода
        currentPlayer = if (currentPlayer == "X") "O" else "X"
        return true
    }

    fun getCurrentPlayer(): String = currentPlayer

    fun getLastMovePlayer(): String = lastMovePlayer

    fun isGameActive(): Boolean = gameActive

    fun getGameState(): Array<String> = gameState.clone()

    fun resetGame() {
        currentPlayer = "X"
        lastMovePlayer = "X"
        gameState = Array(9) { "" }
        gameActive = true
        winner = null
    }

    fun getGameStatus(): String {
        return when {
            !gameActive && winner != null -> {
                if (winner == "X") context.getString(R.string.player_x_wins)
                else context.getString(R.string.player_o_wins)
            }
            !gameActive -> context.getString(R.string.draw)
            else -> if (currentPlayer == "X") 
                context.getString(R.string.player_x_turn)
            else 
                context.getString(R.string.player_o_turn)
        }
    }

    private fun checkWinner(player: String): Boolean {
        // Проверка горизонтальных линий
        for (row in 0..2) {
            if (gameState[row * 3] == player && 
                gameState[row * 3 + 1] == player && 
                gameState[row * 3 + 2] == player) {
                return true
            }
        }

        // Проверка вертикальных линий
        for (col in 0..2) {
            if (gameState[col] == player && 
                gameState[col + 3] == player && 
                gameState[col + 6] == player) {
                return true
            }
        }

        // Проверка главной диагонали
        if (gameState[0] == player && 
            gameState[4] == player && 
            gameState[8] == player) {
            return true
        }

        // Проверка побочной диагонали
        if (gameState[2] == player && 
            gameState[4] == player && 
            gameState[6] == player) {
            return true
        }

        return false
    }
} 