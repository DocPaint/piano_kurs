package com.example

import javafx.application.Platform
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class Player {
    var recording: List<NoteEvent> = listOf()
        private set
    private val activePlayers = mutableMapOf<String, MediaPlayer>()
    private val pressTimes = mutableMapOf<String, Long>()
    private val threshold = 180 // Порог в миллисекундах

    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    private val scheduledTasks = mutableMapOf<String, java.util.concurrent.ScheduledFuture<*>>()

    fun loadRecording(file: File) {
        recording = file.readLines().map { line ->
            val parts = line.split(",")
            NoteEvent(parts[0], parts[1], parts[2].toLong())
        }
    }

    fun playRecording() {
        if (recording.isEmpty()) return

        thread {
            val startTime = System.currentTimeMillis()
            for (event in recording) {
                val currentTime = System.currentTimeMillis()
                val waitTime = startTime + event.timestamp - currentTime
                if (waitTime > 0) {
                    Thread.sleep(waitTime)
                }

                Platform.runLater {
                    if (event.type == "PRESS") {
                        pressNote(event.note)

                    } else if (event.type == "RELEASE") {
                        releaseNote(event.note)
                    }
                }
            }
        }
    }

    private fun pressNote(note: String) {
        pressTimes[note] = System.currentTimeMillis()

        val scheduledFuture = scheduler.schedule({
            Platform.runLater {
                // Проигрываем длинную ноту
                val mediaUrlLong = javaClass.getResource("/sounds/r_${note.lowercase()}.wav")?.toExternalForm()
                if (mediaUrlLong == null) {
                    println("Звуковой файл для ноты r_$note не найден.")
                    return@runLater
                }
                val mediaLong = Media(mediaUrlLong)
                val mediaPlayerLong = MediaPlayer(mediaLong).apply {
                    isAutoPlay = true
                    play()
                }
                activePlayers[note] = mediaPlayerLong
            }
        }, threshold.toLong(), TimeUnit.MILLISECONDS)

        scheduledTasks[note] = scheduledFuture
    }

    private fun releaseNote(note: String) {
        val pressTime = pressTimes.remove(note) ?: return
        val releaseTime = System.currentTimeMillis()
        val duration = releaseTime - pressTime

        val scheduledFuture = scheduledTasks.remove(note)

        if (duration < threshold) {

            scheduledFuture?.cancel(false)

            // Проигрываем длинную ноту
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
            activePlayers[note]?.stop()
            activePlayers.remove(note)
        }
    }
}
