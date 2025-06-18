package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.os.CountDownTimer

class GameActivity : AppCompatActivity() {
    private lateinit var game: TicTacToeGame
    private lateinit var gameStatusText: TextView
    private lateinit var scoreText: TextView
    private lateinit var gameBoard: GridLayout
    private lateinit var menuButton: ImageButton
    private var buttons: Array<Button> = arrayOf()

    // Цвета для игроков из ресурсов
    private lateinit var playerColors: Map<String, Int>

    private lateinit var timerText: TextView
    private var gameTimer: CountDownTimer? = null
    private var timerDurationSec: Int = 0 // 0 = без таймера
    private var timerMillisLeft: Long = 0L
    private var isFirstMove: Boolean = true // флаг первого хода

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // Инициализируем цвета после создания контекста
        playerColors = mapOf(
            "X" to ContextCompat.getColor(this, R.color.player_x),
            "O" to ContextCompat.getColor(this, R.color.player_o),
            "[]" to ContextCompat.getColor(this, R.color.player_brackets),
            "#" to ContextCompat.getColor(this, R.color.player_hash)
        )

        game = TicTacToeGame(this)
        gameStatusText = findViewById(R.id.gameStatusText)
        scoreText = findViewById(R.id.scoreText)
        gameBoard = findViewById(R.id.gameBoard)
        menuButton = findViewById(R.id.menuButton)
        timerText = findViewById(R.id.timerText)

        setupGame()
        setupMenuButton()
        updateScoreDisplay()
        updateTimerVisibility() // обновляем видимость таймера
    }

    private fun setupMenuButton() {
        menuButton.setOnClickListener {
            showMenuDialog()
        }
    }

    private fun showMenuDialog() {
        val menuItems = arrayOf(
            getString(R.string.game_mode),
            getString(R.string.reset_game),
            getString(R.string.reset_scores),
            getString(R.string.rules),
            getString(R.string.select_timer)
        )

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.menu))
            .setItems(menuItems) { dialog, which ->
                when (which) {
                    0 -> showSizeSelectionDialog()
                    1 -> resetGame()
                    2 -> showResetScoresDialog()
                    3 -> showRulesDialog()
                    4 -> showTimerSelectionDialog()
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun resetGame() {
        game.resetGame()
        buttons.forEach { it.text = "" }
        isFirstMove = true // сбрасываем флаг первого хода
        updateGameStatus()
        updateTimerVisibility() // обновляем видимость таймера
        gameTimer?.cancel() // останавливаем таймер
    }

    private fun showRulesDialog() {
        val rulesText = buildString {
            append(getString(R.string.rules_general))
            append("\n\n")
            append(getString(R.string.rules_classic))
            append("\n\n")
            append(getString(R.string.rules_misere))
            append("\n\n")
            append(getString(R.string.rules_gomoku))
            append("\n\n")
            append(getString(R.string.rules_free_choice))
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.rules_title))
            .setMessage(rulesText)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showResetScoresDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.reset_scores))
            .setMessage("Вы уверены, что хотите сбросить счет всех игроков?")
            .setPositiveButton("Да") { dialog, _ ->
                game.resetScores()
                updateScoreDisplay()
                dialog.dismiss()
            }
            .setNegativeButton("Нет") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showSizeSelectionDialog() {
        val modes = arrayOf("3x3", "4x4", "5x5", "6x6", "3x3 Misere (Поддавки)", "15x15 Гомоку", "Свободный выбор символов")
        val currentSize = game.getBoardSize()
        val currentMode = when {
            game.getGomokuMode() -> 5
            game.getMisereMode() -> 4
            game.getFreeChoiceMode() -> 6
            else -> currentSize - 3
        }
        
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_game_mode))
            .setSingleChoiceItems(modes, currentMode) { dialog, which ->
                when (which) {
                    4 -> {
                        game.setBoardSize(3)
                        game.applyMisereMode(true)
                    }
                    5 -> {
                        game.applyGomokuMode(true)
                    }
                    6 -> {
                        showFreeChoiceSizeDialog()
                    }
                    else -> {
                        val newSize = which + 3
                        game.setBoardSize(newSize)
                        game.applyMisereMode(false)
                        game.applyGomokuMode(false)
                        game.applyFreeChoiceMode(false)
                    }
                }
                createGameBoard()
                updateGameStatus()
                updateScoreDisplay()
                showScoreResetNotification()
                isFirstMove = true
                updateTimerVisibility()
                gameTimer?.cancel()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showFreeChoiceSizeDialog() {
        val sizes = arrayOf("3x3", "4x4", "5x5")
        AlertDialog.Builder(this)
            .setTitle("Выберите размер поля для Свободного выбора")
            .setSingleChoiceItems(sizes, 0) { dialog, which ->
                val newSize = which + 3
                game.setBoardSize(newSize)
                game.applyFreeChoiceMode(true)
                
                // Обновляем игровое поле после выбора размера
                createGameBoard()
                updateGameStatus()
                updateScoreDisplay()
                showScoreResetNotification()
                isFirstMove = true
                updateTimerVisibility()
                gameTimer?.cancel()
                
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showSymbolSelectionDialog(position: Int) {
        val symbols = arrayOf("X", "O")
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_symbol))
            .setSingleChoiceItems(symbols, 0) { dialog, which ->
                val symbol = symbols[which]
                makeMoveWithSymbol(position, symbol)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun makeMoveWithSymbol(position: Int, symbol: String) {
        if (game.makeMove(position, symbol)) {
            val button = buttons[position]
            button.text = symbol
            val playerColor = playerColors[symbol] ?: ContextCompat.getColor(this, R.color.text_primary)
            button.setTextColor(playerColor)
            
            // Добавляем неоновое свечение ТОЛЬКО к символам игроков
            button.setShadowLayer(8f, 0f, 0f, playerColor)
            
            if (isFirstMove) {
                isFirstMove = false
                startOrResetTimer()
            }
            
            updateGameStatus()
            updateScoreDisplay()
            if (!game.isGameActive()) {
                gameTimer?.cancel()
            }
        }
    }

    private fun showScoreResetNotification() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.score_reset_title))
            .setMessage(getString(R.string.score_reset_message))
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showTimerSelectionDialog() {
        val timerOptions = arrayOf(
            getString(R.string.timer_off),
            getString(R.string.timer_15),
            getString(R.string.timer_30),
            getString(R.string.timer_45),
            getString(R.string.timer_60),
            getString(R.string.timer_75),
            getString(R.string.timer_90),
            getString(R.string.timer_105),
            getString(R.string.timer_120)
        )
        val timerValues = intArrayOf(0, 15, 30, 45, 60, 75, 90, 105, 120)
        val checkedItem = timerValues.indexOf(timerDurationSec)
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_timer))
            .setSingleChoiceItems(timerOptions, checkedItem) { dialog, which ->
                timerDurationSec = timerValues[which]
                updateTimerVisibility()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateTimerVisibility() {
        if (timerDurationSec > 0) {
            timerText.visibility = android.view.View.VISIBLE
            timerText.text = getString(R.string.timer, timerDurationSec)
        } else {
            timerText.visibility = android.view.View.GONE
            timerText.text = ""
        }
    }

    private fun startOrResetTimer() {
        gameTimer?.cancel()
        if (timerDurationSec > 0 && !isFirstMove) {
            timerMillisLeft = timerDurationSec * 1000L
            timerText.text = getString(R.string.timer, timerDurationSec)
            gameTimer = object : CountDownTimer(timerMillisLeft, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timerMillisLeft = millisUntilFinished
                    val seconds = (millisUntilFinished / 1000).toInt()
                    timerText.text = getString(R.string.timer, seconds)
                }
                override fun onFinish() {
                    timerText.text = getString(R.string.timer_draw)
                    gameOverByTimer()
                }
            }.start()
        }
    }

    private fun gameOverByTimer() {
        game.resetGame() // сбрасываем игровое поле
        updateGameStatusDrawByTimer()
        // Очищаем кнопки
        buttons.forEach { it.text = "" }
    }

    private fun updateGameStatusDrawByTimer() {
        gameStatusText.text = getString(R.string.timer_draw)
        gameStatusText.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
    }

    private fun setupGame() {
        createGameBoard()
        updateGameStatus()
        updateTimerVisibility() // обновляем видимость таймера
    }

    private fun createGameBoard() {
        gameBoard.removeAllViews()
        val boardSize = game.getBoardSize()
        buttons = Array(boardSize * boardSize) { Button(this) }
        gameBoard.columnCount = boardSize
        gameBoard.rowCount = boardSize
        buttons.forEachIndexed { index, button ->
            val params = GridLayout.LayoutParams()
            params.width = 0
            params.height = 0
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            params.setMargins(4, 4, 4, 4)
            button.layoutParams = params
            button.textSize = when (boardSize) {
                3 -> 24f
                4 -> 20f
                5 -> 16f
                6 -> 14f
                15 -> 10f
                else -> 20f
            }
            button.gravity = Gravity.CENTER
            button.text = ""
            button.setBackgroundResource(R.drawable.game_button_background)
            button.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
            
            button.setOnClickListener {
                if (game.getFreeChoiceMode()) {
                    showSymbolSelectionDialog(index)
                } else {
                    if (game.makeMove(index)) {
                        val player = game.getLastMovePlayer()
                        button.text = player
                        val playerColor = playerColors[player] ?: ContextCompat.getColor(this, R.color.text_primary)
                        button.setTextColor(playerColor)
                        
                        // Добавляем неоновое свечение ТОЛЬКО к символам игроков
                        button.setShadowLayer(8f, 0f, 0f, playerColor)
                        
                        if (isFirstMove) {
                            isFirstMove = false
                            startOrResetTimer()
                        }
                        
                        updateGameStatus()
                        updateScoreDisplay()
                        if (!game.isGameActive()) {
                            gameTimer?.cancel()
                        }
                    }
                }
            }
            gameBoard.addView(button)
        }
    }

    private fun updateGameStatus() {
        gameStatusText.text = game.getGameStatus()
        // Подсвечиваем текущего игрока
        val currentPlayer = game.getCurrentPlayer()
        gameStatusText.setTextColor(playerColors[currentPlayer] ?: ContextCompat.getColor(this, R.color.text_primary))
    }

    private fun updateScoreDisplay() {
        scoreText.text = game.getScoreText()
    }
} 