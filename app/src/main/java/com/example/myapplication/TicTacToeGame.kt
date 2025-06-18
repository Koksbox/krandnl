package com.example.myapplication

import android.content.Context

class TicTacToeGame(private val context: Context) {
    private var currentPlayer = "X"
    private var gameState: Array<String> = Array(9) { "" }
    private var gameActive = true
    private var winner: String? = null
    private var lastMovePlayer: String = "X"
    private var boardSize = 3
    private val players = arrayOf("X", "O", "[]", "#")
    
    // Система подсчета побед
    private val playerScores = mutableMapOf("X" to 0, "O" to 0, "[]" to 0, "#" to 0)
    private var totalGames = 0
    // Флаг режима Misere
    var isMisereMode: Boolean = false
    // Флаг режима Гомоку
    var isGomokuMode: Boolean = false
    // Флаг режима Свободный выбор символов
    var isFreeChoiceMode: Boolean = false

    fun setBoardSize(size: Int) {
        if (size in 3..19) {
            boardSize = size
            resetGame()
            resetScores() // Сбрасываем счет при смене размера поля
        }
    }

    fun applyMisereMode(enabled: Boolean) {
        isMisereMode = enabled
        isGomokuMode = false // Отключаем Гомоку при включении Misere
        isFreeChoiceMode = false // Отключаем Свободный выбор при включении Misere
        resetGame()
        resetScores()
    }

    fun applyGomokuMode(enabled: Boolean) {
        isGomokuMode = enabled
        isMisereMode = false // Отключаем Misere при включении Гомоку
        isFreeChoiceMode = false // Отключаем Свободный выбор при включении Гомоку
        if (enabled) {
            setBoardSize(15) // Гомоку играется на поле 15x15
        }
        resetGame()
        resetScores()
    }

    fun applyFreeChoiceMode(enabled: Boolean) {
        isFreeChoiceMode = enabled
        isMisereMode = false // Отключаем Misere при включении Свободного выбора
        isGomokuMode = false // Отключаем Гомоку при включении Свободного выбора
        if (enabled && boardSize > 5) {
            setBoardSize(5) // Свободный выбор ограничен полем 5x5
        }
        resetGame()
        resetScores()
    }

    fun getMisereMode(): Boolean = isMisereMode
    fun getGomokuMode(): Boolean = isGomokuMode
    fun getFreeChoiceMode(): Boolean = isFreeChoiceMode

    fun getBoardSize(): Int = boardSize

    fun getPlayers(): Array<String> = players

    fun getCurrentPlayerIndex(): Int = players.indexOf(currentPlayer)

    fun makeMove(position: Int, symbol: String = ""): Boolean {
        val totalCells = boardSize * boardSize
        // Проверяем, можно ли сделать ход
        if (!gameActive || position < 0 || position >= totalCells || gameState[position].isNotEmpty()) {
            return false
        }

        // Определяем символ для хода
        val moveSymbol = if (isFreeChoiceMode && symbol.isNotEmpty()) {
            symbol
        } else {
            currentPlayer
        }

        // Сохраняем игрока, который делает ход
        lastMovePlayer = currentPlayer
        // Делаем ход с выбранным символом
        gameState[position] = moveSymbol
        
        // Проверяем, не выиграл ли текущий игрок
        if (checkWinner(moveSymbol)) {
            gameActive = false
            // Misere: проигравший — тот, кто собрал ряд, победитель — следующий игрок
            if (isMisereMode) {
                val loser = currentPlayer
                val winnerIndex = (players.indexOf(currentPlayer) + 1) % getActivePlayerCount()
                val misereWinner = players[winnerIndex]
                winner = misereWinner
                playerScores[misereWinner] = playerScores[misereWinner]!! + 1
            } else {
                winner = currentPlayer
                playerScores[currentPlayer] = playerScores[currentPlayer]!! + 1
            }
            totalGames++
            return true
        }

        // Проверяем ничью (все клетки заполнены)
        if (gameState.none { it.isEmpty() }) {
            gameActive = false
            totalGames++
            return true
        }

        // Меняем игрока для следующего хода
        val currentIndex = players.indexOf(currentPlayer)
        val nextIndex = (currentIndex + 1) % getActivePlayerCount()
        currentPlayer = players[nextIndex]
        return true
    }

    private fun getActivePlayerCount(): Int {
        return when {
            isGomokuMode -> 2
            isFreeChoiceMode -> 2 // Свободный выбор всегда для 2 игроков
            boardSize >= 5 -> 4
            else -> 2
        }
    }

    fun getCurrentPlayer(): String = currentPlayer

    fun getLastMovePlayer(): String = lastMovePlayer

    fun isGameActive(): Boolean = gameActive

    fun getGameState(): Array<String> = gameState.clone()

    fun resetGame() {
        currentPlayer = "X"
        lastMovePlayer = "X"
        gameState = Array(boardSize * boardSize) { "" }
        gameActive = true
        winner = null
    }

    // Методы для работы со счетом
    fun getPlayerScore(player: String): Int = playerScores[player] ?: 0
    
    fun getTotalGames(): Int = totalGames
    
    fun getScoreText(): String {
        val activePlayers = getActivePlayerCount()
        return buildString {
            append("Счет: ")
            for (i in 0 until activePlayers) {
                val player = players[i]
                append("${player}: ${playerScores[player]}")
                if (i < activePlayers - 1) append(" | ")
            }
            append(" (Игр: $totalGames)")
        }
    }
    
    fun resetScores() {
        playerScores.clear()
        playerScores.putAll(mapOf("X" to 0, "O" to 0, "[]" to 0, "#" to 0))
        totalGames = 0
    }

    fun getGameStatus(): String {
        return when {
            !gameActive && winner != null -> {
                if (isMisereMode) {
                    return context.getString(R.string.misere_winner, winner)
                }
                if (isGomokuMode) {
                    return context.getString(R.string.gomoku_winner, winner)
                }
                if (isFreeChoiceMode) {
                    return context.getString(R.string.free_choice_winner, winner)
                }
                when (winner) {
                    "X" -> context.getString(R.string.player_x_wins)
                    "O" -> context.getString(R.string.player_o_wins)
                    "[]" -> context.getString(R.string.player_brackets_wins)
                    "#" -> context.getString(R.string.player_hash_wins)
                    else -> context.getString(R.string.player_wins, winner)
                }
            }
            !gameActive -> context.getString(R.string.draw)
            else -> {
                when (currentPlayer) {
                    "X" -> context.getString(R.string.player_x_turn)
                    "O" -> context.getString(R.string.player_o_turn)
                    "[]" -> context.getString(R.string.player_brackets_turn)
                    "#" -> context.getString(R.string.player_hash_turn)
                    else -> context.getString(R.string.player_turn, currentPlayer)
                }
            }
        }
    }

    private fun checkWinner(player: String): Boolean {
        val winLength = when {
            isGomokuMode -> 5 // Гомоку: 5 в ряд
            isFreeChoiceMode -> 3 // Свободный выбор: всегда 3 в ряд
            boardSize >= 5 -> 4 // Большие поля: 4 в ряд
            else -> boardSize // Маленькие поля: заполнить всю линию
        }
        
        // Проверка горизонтальных линий
        for (row in 0 until boardSize) {
            for (col in 0..(boardSize - winLength)) {
                var win = true
                for (i in 0 until winLength) {
                    if (gameState[row * boardSize + col + i] != player) {
                        win = false
                        break
                    }
                }
                if (win) return true
            }
        }

        // Проверка вертикальных линий
        for (col in 0 until boardSize) {
            for (row in 0..(boardSize - winLength)) {
                var win = true
                for (i in 0 until winLength) {
                    if (gameState[(row + i) * boardSize + col] != player) {
                        win = false
                        break
                    }
                }
                if (win) return true
            }
        }

        // Проверка диагоналей (слева направо, сверху вниз)
        for (row in 0..(boardSize - winLength)) {
            for (col in 0..(boardSize - winLength)) {
                var win = true
                for (i in 0 until winLength) {
                    if (gameState[(row + i) * boardSize + (col + i)] != player) {
                        win = false
                        break
                    }
                }
                if (win) return true
            }
        }

        // Проверка диагоналей (справа налево, сверху вниз)
        for (row in 0..(boardSize - winLength)) {
            for (col in (winLength - 1) until boardSize) {
                var win = true
                for (i in 0 until winLength) {
                    if (gameState[(row + i) * boardSize + (col - i)] != player) {
                        win = false
                        break
                    }
                }
                if (win) return true
            }
        }

        return false
    }
} 