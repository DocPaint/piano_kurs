package com.example.VirtualPianoApp

import javafx.application.Application
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import javafx.stage.Stage
import com.example.PianoKey
import com.example.Recorder
import com.example.Player
import javafx.scene.input.KeyCode
import java.io.File

class VirtualPianoApp : Application() {
    private val recorder = Recorder()
    private val player = Player()
    private val pianoKeysMap = mutableMapOf<KeyCode, PianoKey>()

    override fun start(primaryStage: Stage) {
        primaryStage.title = "Виртуальное пианино"

        // Создаем кнопки для нот и связываем их с клавишами
        val keyBindings = listOf(
            KeyCode.D to "Do",
            KeyCode.F to "Re",
            KeyCode.G to "Mi",
            KeyCode.H to "Fa",
            KeyCode.J to "Sol",
            KeyCode.K to "Lya",
            KeyCode.L to "Si"
        )

        val pianoKeys = keyBindings.map { (keyCode, note) ->
            val pianoKey = PianoKey(note, recorder)
            pianoKeysMap[keyCode] = pianoKey
            pianoKey.button
        }

        val pianoLayout = HBox(10.0, *pianoKeys.toTypedArray()).apply {
            alignment = Pos.CENTER
            padding = Insets(20.0)
        }

        // Кнопки управления записью и воспроизведением
        val recordButton = Button("Начать запись")
        val stopRecordButton = Button("Стоп запись")
        val playButton = Button("Воспроизвести")
        val loadButton = Button("Загрузить запись")

        stopRecordButton.isDisable = true

        recordButton.setOnAction {
            recorder.startRecording()
            recordButton.isDisable = true
            stopRecordButton.isDisable = false
        }

        stopRecordButton.setOnAction {
            recorder.stopRecording()
            recordButton.isDisable = false
            stopRecordButton.isDisable = true
            recorder.saveRecording(primaryStage)
        }

        playButton.setOnAction {
            player.playRecording()
        }

        loadButton.setOnAction {
            val file = loadRecording(primaryStage)
            if (file != null) {
                player.loadRecording(file)
            }
        }

        val controlsLayout = HBox(10.0, recordButton, stopRecordButton, playButton, loadButton).apply {
            alignment = Pos.CENTER
            padding = Insets(10.0)
        }

        val root = VBox(10.0, pianoLayout, controlsLayout).apply {
            alignment = Pos.CENTER
        }

        val scene = Scene(root, 800.0, 300.0)

        // Добавление обработчиков клавиатуры
        scene.setOnKeyPressed { event ->
            val keyCode = event.code
            val pianoKey = pianoKeysMap[keyCode]
            if (pianoKey != null && !pianoKey.isPressed) {
                pianoKey.press()
            }
        }

        scene.setOnKeyReleased { event ->
            val keyCode = event.code
            val pianoKey = pianoKeysMap[keyCode]
            if (pianoKey != null && pianoKey.isPressed) {
                pianoKey.release()
            }
        }

        primaryStage.scene = scene
        primaryStage.show()
    }

    private fun loadRecording(stage: Stage): File? {
        val fileChooser = FileChooser().apply {
            title = "Выберите файл записи"
            extensionFilters.add(FileChooser.ExtensionFilter("Recording Files", "*.rec"))
        }
        return fileChooser.showOpenDialog(stage)
    }
}

fun main() {
    Application.launch(VirtualPianoApp::class.java)
}
