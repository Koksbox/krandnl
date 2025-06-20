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
import android.app.Dialog
import android.view.LayoutInflater
import android.widget.RadioButton
import android.widget.RadioGroup

/**
 * Главная активность игры в крестики-нолики
 * Управляет пользовательским интерфейсом и взаимодействием с игровой логикой
 */
class GameActivity : AppCompatActivity() {
    // Основные компоненты игры
    private lateinit var game: TicTacToeGame // Игровая логика
    private lateinit var gameStatusText: TextView // Текст статуса игры
    private lateinit var scoreText: TextView // Текст счета
    private lateinit var gameBoard: GridLayout // Игровое поле
    private lateinit var menuButton: ImageButton // Кнопка меню
    private lateinit var newGameButton: ImageButton // Кнопка новой игры
    private var buttons: Array<Button> = arrayOf() // Массив кнопок игрового поля

    // Цвета для игроков из ресурсов
    private lateinit var playerColors: Map<String, Int>

    // Компоненты таймера
    private var gameTimer: CountDownTimer? = null // Таймер игры
    private var timerDurationSec: Int = 0 // Длительность таймера в секундах (0 = без таймера)
    private var timerMillisLeft: Long = 0L // Оставшееся время в миллисекундах
    private var isFirstMove: Boolean = true // Флаг первого хода (для запуска таймера)
    private var timerSecondsLeft: Int = 0 // Секунд осталось для отображения

    /**
     * Инициализация активности при создании
     */
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

        // Инициализация игровых компонентов
        game = TicTacToeGame(this)
        gameStatusText = findViewById(R.id.gameStatusText)
        scoreText = findViewById(R.id.scoreText)
        gameBoard = findViewById(R.id.gameBoard)
        menuButton = findViewById(R.id.menuButton)
        newGameButton = findViewById(R.id.newGameButton)

        // Настройка игры
        setupGame()
        setupMenuButton()
        updateScoreDisplay()
        setupNewGameButton()
    }

    /**
     * Настройка кнопки меню
     */
    private fun setupMenuButton() {
        menuButton.setOnClickListener {
            showMenuDialog()
        }
    }

    /**
     * Настройка кнопки новой игры
     */
    private fun setupNewGameButton() {
        newGameButton.setOnClickListener {
            resetGame()
        }
    }

    /**
     * Показ диалога меню с опциями игры
     */
    private fun showMenuDialog() {
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.menu_dialog, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        view.findViewById<Button>(R.id.btnGameMode).setOnClickListener {
            showSizeSelectionDialog()
            dialog.dismiss()
        }
        view.findViewById<Button>(R.id.btnNewGame).setOnClickListener {
            resetGame()
            dialog.dismiss()
        }
        view.findViewById<Button>(R.id.btnResetScores).setOnClickListener {
            showResetScoresDialog()
            dialog.dismiss()
        }
        view.findViewById<Button>(R.id.btnRules).setOnClickListener {
            showRulesDialog()
            dialog.dismiss()
        }
        view.findViewById<Button>(R.id.btnTimer).setOnClickListener {
            showTimerSelectionDialog()
            dialog.dismiss()
        }

        dialog.show()
    }

    /**
     * Сброс текущей игры
     */
    private fun resetGame() {
        game.resetGame()
        buttons.forEach { it.text = "" } // Очищаем все кнопки
        isFirstMove = true // Сбрасываем флаг первого хода
        updateGameStatus()
        gameTimer?.cancel() // Останавливаем таймер
    }

    /**
     * Показ диалога с правилами игры
     */
    private fun showRulesDialog() {
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_rules, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

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
        view.findViewById<TextView>(R.id.rulesText).text = rulesText
        view.findViewById<Button>(R.id.btnOk).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    /**
     * Показ диалога подтверждения сброса счета
     */
    private fun showResetScoresDialog() {
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_reset_scores, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        view.findViewById<Button>(R.id.btnYes).setOnClickListener {
            game.resetScores()
            updateScoreDisplay()
            dialog.dismiss()
        }
        view.findViewById<Button>(R.id.btnNo).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    /**
     * Показ диалога выбора размера поля и режима игры
     */
    private fun showSizeSelectionDialog() {
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_mode, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val modes = arrayOf("3x3", "4x4", "5x5", "6x6", "3x3 Misere (Поддавки)", "10x10 Гомоку", "Свободный выбор символов")
        val currentSize = game.getBoardSize()
        val currentMode = when {
            game.getGomokuMode() -> 5 // Гомоку
            game.getMisereMode() -> 4 // Поддавки
            game.getFreeChoiceMode() -> 6 // Свободный выбор
            else -> currentSize - 3 // Классические режимы
        }
        val radioGroup = view.findViewById<RadioGroup>(R.id.modeRadioGroup)
        modes.forEachIndexed { i, mode ->
            val rb = RadioButton(this)
            rb.text = mode
            rb.setTextColor(ContextCompat.getColor(this, R.color.white))
            rb.buttonTintList = ContextCompat.getColorStateList(this, R.color.primary)
            rb.id = i
            radioGroup.addView(rb)
        }
        radioGroup.check(currentMode)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
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
                    val newSize = checkedId + 3
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
            gameTimer?.cancel()
            dialog.dismiss()
        }
        view.findViewById<Button>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    /**
     * Показ диалога выбора размера поля для режима свободного выбора
     */
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
                gameTimer?.cancel()
                
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Показ диалога выбора символа для режима свободного выбора
     */
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

    /**
     * Выполнение хода с выбранным символом (для режима свободного выбора)
     */
    private fun makeMoveWithSymbol(position: Int, symbol: String) {
        if (game.makeMove(position, symbol)) {
            val button = buttons[position]
            button.text = symbol
            val playerColor = playerColors[symbol] ?: ContextCompat.getColor(this, R.color.text_primary)
            button.setTextColor(playerColor)
            
            // Добавляем неоновое свечение ТОЛЬКО к символам игроков
            button.setShadowLayer(8f, 0f, 0f, playerColor)
            
            // Запускаем таймер после первого хода
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

    /**
     * Показ уведомления о сбросе счета
     */
    private fun showScoreResetNotification() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.score_reset_title))
            .setMessage(getString(R.string.score_reset_message))
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Показ диалога выбора таймера
     */
    private fun showTimerSelectionDialog() {
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_timer, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

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
        val radioGroup = view.findViewById<RadioGroup>(R.id.timerRadioGroup)
        timerOptions.forEachIndexed { i, option ->
            val rb = RadioButton(this)
            rb.text = option
            rb.setTextColor(ContextCompat.getColor(this, R.color.white))
            rb.buttonTintList = ContextCompat.getColorStateList(this, R.color.primary)
            rb.id = i
            radioGroup.addView(rb)
        }
        radioGroup.check(checkedItem)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            timerDurationSec = timerValues[checkedId]
            dialog.dismiss()
        }
        view.findViewById<Button>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    /**
     * Настройка игры при запуске
     */
    private fun setupGame() {
        createGameBoard()
        updateGameStatus()
    }

    /**
     * Создание игрового поля с кнопками
     */
    private fun createGameBoard() {
        gameBoard.removeAllViews() // Очищаем поле
        val boardSize = game.getBoardSize()
        buttons = Array(boardSize * boardSize) { Button(this) } // Создаем массив кнопок
        gameBoard.columnCount = boardSize
        gameBoard.rowCount = boardSize
        
        // Настраиваем каждую кнопку
        buttons.forEachIndexed { index, button ->
            // Параметры расположения кнопки в сетке
            val params = GridLayout.LayoutParams()
            params.width = 0
            params.height = 0
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            params.setMargins(4, 4, 4, 4)
            button.layoutParams = params
            
            // Размер текста в зависимости от размера поля
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
            
            // Обработчик нажатия на кнопку
            button.setOnClickListener {
                if (game.getFreeChoiceMode()) {
                    // В режиме свободного выбора показываем диалог выбора символа
                    showSymbolSelectionDialog(index)
                } else {
                    // В обычном режиме делаем ход автоматически
                    if (game.makeMove(index)) {
                        val player = game.getLastMovePlayer()
                        button.text = player
                        val playerColor = playerColors[player] ?: ContextCompat.getColor(this, R.color.text_primary)
                        button.setTextColor(playerColor)
                        
                        // Добавляем неоновое свечение ТОЛЬКО к символам игроков
                        button.setShadowLayer(8f, 0f, 0f, playerColor)
                        
                        // Запускаем таймер после первого хода
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

    /**
     * Обновление статуса игры
     */
    private fun updateGameStatus() {
        gameStatusText.text = game.getGameStatus()
        // Подсвечиваем текущего игрока
        val currentPlayer = game.getCurrentPlayer()
        gameStatusText.setTextColor(playerColors[currentPlayer] ?: ContextCompat.getColor(this, R.color.text_primary))
    }

    /**
     * Обновление отображения счета
     */
    private fun updateScoreDisplay() {
        val baseScore = game.getScoreText()
        val timerPart = if (timerDurationSec > 0 && !isFirstMove) " (сек: $timerSecondsLeft)" else ""
        scoreText.text = baseScore + timerPart
    }

    /**
     * Запуск или сброс таймера игры
     */
    private fun startOrResetTimer() {
        gameTimer?.cancel()
        if (timerDurationSec > 0 && !isFirstMove) {
            timerMillisLeft = timerDurationSec * 1000L
            timerSecondsLeft = timerDurationSec
            updateScoreDisplay()
            gameTimer = object : CountDownTimer(timerMillisLeft, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timerMillisLeft = millisUntilFinished
                    timerSecondsLeft = (millisUntilFinished / 1000).toInt()
                    updateScoreDisplay()
                }
                override fun onFinish() {
                    timerSecondsLeft = 0
                    updateScoreDisplay()
                    gameOverByTimer()
                }
            }.start()
        } else {
            timerSecondsLeft = 0
            updateScoreDisplay()
        }
    }

    /**
     * Обработка окончания игры по таймеру
     */
    private fun gameOverByTimer() {
        game.resetGame() // Сбрасываем игровое поле
        updateGameStatusDrawByTimer()
        // Очищаем кнопки
        buttons.forEach { it.text = "" }
    }

    /**
     * Обновление статуса игры при ничьей по таймеру
     */
    private fun updateGameStatusDrawByTimer() {
        gameStatusText.text = getString(R.string.timer_draw)
        gameStatusText.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
    }
} 