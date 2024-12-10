package com.example

import javafx.application.Platform
import javafx.scene.control.Button
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class PianoKey(private val note: String, private val recorder: Recorder) {
    val button: Button = Button(note).apply {
        prefWidth = 100.0
        setOnMousePressed { press() }
        setOnMouseReleased { release() }
    }
    var isPressed: Boolean = false
        private set

    private var pressTime: Long = 0
    private var scheduledFuture: java.util.concurrent.ScheduledFuture<*>? = null
    private var mediaPlayerLong: MediaPlayer? = null
    private val threshold = 180 // Порог в миллисекундах

    companion object {
        private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(7)
    }

    fun press() {
        if (isPressed) return
        isPressed = true
        pressTime = System.currentTimeMillis()

        // Запись нажатия
        recorder.recordPress(note)

        // индикация нажатия
        Platform.runLater {
            button.style = "-fx-background-color: lightblue;"
        }

        // Запланировать воспроизведение длинного звука
        scheduledFuture = scheduler.schedule({
            Platform.runLater {
                // Воспроизведение длинного звука
                val mediaUrlLong = javaClass.getResource("/sounds/r_${note.lowercase()}.wav")?.toExternalForm()
                if (mediaUrlLong == null) {
                    println("Звуковой файл для ноты r_$note не найден.")
                    return@runLater
                }
                val mediaLong = Media(mediaUrlLong)
                mediaPlayerLong = MediaPlayer(mediaLong).apply {
                    isAutoPlay = true
                    play()
                }
            }
        }, threshold.toLong(), TimeUnit.MILLISECONDS)
    }
    fun release() {
        if (!isPressed) return
        isPressed = false

        // Вычислить длительность нажатия
        val releaseTime = System.currentTimeMillis()
        val duration = releaseTime - pressTime

        // Отменить запланированное воспроизведение длинного звука, если ещё не произошло
        if (duration < threshold) {
            scheduledFuture?.cancel(false)
            scheduledFuture = null

            // Воспроизведение короткого звука
            val mediaUrlShort = javaClass.getResource("/sounds/${note.lowercase()}.wav")?.toExternalForm()
            if (mediaUrlShort == null) {
                println("Звуковой файл для ноты $note не найден.")
            } else {
                val mediaShort = Media(mediaUrlShort)
                val mediaPlayerShort = MediaPlayer(mediaShort).apply {
                    isAutoPlay = true
                    play()
                }
            }
        } else {
            mediaPlayerLong?.stop()
            mediaPlayerLong = null
        }

        // Сброс стиля кнопки
        Platform.runLater {
            button.style = ""
        }

        // Запись отпускания
        recorder.recordRelease(note)
    }
}
