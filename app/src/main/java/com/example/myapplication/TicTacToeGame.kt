package com.example.myapplication

import android.content.Context

/**
 * Класс игровой логики для игры в крестики-нолики
 * Содержит всю логику игры, включая различные режимы и проверку победных комбинаций
 */
class TicTacToeGame(private val context: Context) {
    // Основные игровые переменные
    private var currentPlayer = "X" // Текущий игрок
    private var gameState: Array<String> = Array(9) { "" } // Состояние игрового поля
    private var gameActive = true // Активна ли игра
    private var winner: String? = null // Победитель (null если нет)
    private var lastMovePlayer: String = "X" // Игрок, сделавший последний ход
    private var boardSize = 3 // Размер игрового поля
    
    // Массив доступных игроков
    private val players = arrayOf("X", "O", "[]", "#")
    
    // Система подсчета побед
    private val playerScores = mutableMapOf("X" to 0, "O" to 0, "[]" to 0, "#" to 0)
    private var totalGames = 0 // Общее количество игр
    
    // Флаги специальных режимов игры
    var isMisereMode: Boolean = false // Режим поддавки (проигрывает тот, кто соберет ряд)
    var isGomokuMode: Boolean = false // Режим Гомоку (5 в ряд на поле 15x15)
    var isFreeChoiceMode: Boolean = false // Режим свободного выбора символов

    /**
     * Установка размера игрового поля
     * @param size Размер поля (от 3 до 19)
     */
    fun setBoardSize(size: Int) {
        if (size in 3..19) {
            boardSize = size
            resetGame()
            resetScores() // Сбрасываем счет при смене размера поля
        }
    }

    /**
     * Включение/выключение режима поддавки (Misere)
     * @param enabled true для включения режима поддавки
     */
    fun applyMisereMode(enabled: Boolean) {
        isMisereMode = enabled
        isGomokuMode = false // Отключаем Гомоку при включении Misere
        isFreeChoiceMode = false // Отключаем Свободный выбор при включении Misere
        resetGame()
        resetScores()
    }

    /**
     * Включение/выключение режима Гомоку
     * @param enabled true для включения режима Гомоку
     */
    fun applyGomokuMode(enabled: Boolean) {
        isGomokuMode = enabled
        isMisereMode = false // Отключаем Misere при включении Гомоку
        isFreeChoiceMode = false // Отключаем Свободный выбор при включении Гомоку
        if (enabled) {
            setBoardSize(10) // Гомоку теперь играется на поле 10x10
        }
        resetGame()
        resetScores()
    }

    /**
     * Включение/выключение режима свободного выбора символов
     * @param enabled true для включения режима свободного выбора
     */
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

    // Геттеры для режимов игры
    fun getMisereMode(): Boolean = isMisereMode
    fun getGomokuMode(): Boolean = isGomokuMode
    fun getFreeChoiceMode(): Boolean = isFreeChoiceMode

    // Геттеры для игрового состояния
    fun getBoardSize(): Int = boardSize
    fun getPlayers(): Array<String> = players
    fun getCurrentPlayerIndex(): Int = players.indexOf(currentPlayer)

    /**
     * Выполнение хода игрока
     * @param position Позиция на поле (индекс от 0 до boardSize*boardSize-1)
     * @param symbol Символ для хода (используется только в режиме свободного выбора)
     * @return true если ход выполнен успешно, false если ход невозможен
     */
    fun makeMove(position: Int, symbol: String = ""): Boolean {
        val totalCells = boardSize * boardSize
        // Проверяем, можно ли сделать ход
        if (!gameActive || position < 0 || position >= totalCells || gameState[position].isNotEmpty()) {
            return false
        }

        // Определяем символ для хода
        val moveSymbol = if (isFreeChoiceMode && symbol.isNotEmpty()) {
            symbol // В режиме свободного выбора используем выбранный символ
        } else {
            currentPlayer // В обычном режиме используем символ текущего игрока
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
                // Обычный режим: победитель тот, кто собрал ряд
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

    /**
     * Получение количества активных игроков в зависимости от режима
     * @return Количество игроков
     */
    private fun getActivePlayerCount(): Int {
        return when {
            isGomokuMode -> 2 // Гомоку всегда для 2 игроков
            isFreeChoiceMode -> 2 // Свободный выбор всегда для 2 игроков
            boardSize >= 5 -> 4 // Большие поля для 4 игроков
            else -> 2 // Маленькие поля для 2 игроков
        }
    }

    // Геттеры для игрового состояния
    fun getCurrentPlayer(): String = currentPlayer
    fun getLastMovePlayer(): String = lastMovePlayer
    fun isGameActive(): Boolean = gameActive
    fun getGameState(): Array<String> = gameState.clone()

    /**
     * Сброс текущей игры
     */
    fun resetGame() {
        currentPlayer = "X"
        lastMovePlayer = "X"
        gameState = Array(boardSize * boardSize) { "" } // Создаем новое пустое поле
        gameActive = true
        winner = null
    }

    // Методы для работы со счетом
    fun getPlayerScore(player: String): Int = playerScores[player] ?: 0
    fun getTotalGames(): Int = totalGames
    
    /**
     * Формирование текста счета для отображения
     * @return Строка с текущим счетом всех игроков
     */
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
    
    /**
     * Сброс всех счетов
     */
    fun resetScores() {
        playerScores.clear()
        playerScores.putAll(mapOf("X" to 0, "O" to 0, "[]" to 0, "#" to 0))
        totalGames = 0
    }

    /**
     * Получение текущего статуса игры для отображения
     * @return Строка с описанием текущего состояния игры
     */
    fun getGameStatus(): String {
        return when {
            !gameActive && winner != null -> {
                // Игра окончена, есть победитель
                if (isMisereMode) {
                    return context.getString(R.string.misere_winner, winner)
                }
                if (isGomokuMode) {
                    return context.getString(R.string.gomoku_winner, winner)
                }
                if (isFreeChoiceMode) {
                    return context.getString(R.string.free_choice_winner, winner)
                }
                // Обычные режимы
                when (winner) {
                    "X" -> context.getString(R.string.player_x_wins)
                    "O" -> context.getString(R.string.player_o_wins)
                    "[]" -> context.getString(R.string.player_brackets_wins)
                    "#" -> context.getString(R.string.player_hash_wins)
                    else -> context.getString(R.string.player_wins, winner)
                }
            }
            !gameActive -> context.getString(R.string.draw) // Ничья
            else -> {
                // Игра активна, показываем чей ход
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

    /**
     * Проверка победной комбинации для игрока
     * @param player Символ игрока для проверки
     * @return true если игрок выиграл, false иначе
     */
    private fun checkWinner(player: String): Boolean {
        // Определяем длину победной комбинации в зависимости от режима
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

        return false // Победная комбинация не найдена
    }
} 