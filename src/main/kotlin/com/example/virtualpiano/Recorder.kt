package com.example

import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.PrintWriter
import java.time.Instant
import java.util.concurrent.ConcurrentLinkedQueue

data class NoteEvent(val note: String, val type: String, val timestamp: Long)

class Recorder {
    private var isRecording = false
    private val events = ConcurrentLinkedQueue<NoteEvent>()
    private var startTime: Long = 0

    fun startRecording() {
        events.clear()
        isRecording = true
        startTime = Instant.now().toEpochMilli()
    }

    fun stopRecording() {
        isRecording = false
    }

    fun recordPress(note: String) {
        if (isRecording) {
            val timestamp = Instant.now().toEpochMilli() - startTime
            events.add(NoteEvent(note, "PRESS", timestamp))
        }
    }

    fun recordRelease(note: String) {
        if (isRecording) {
            val timestamp = Instant.now().toEpochMilli() - startTime
            events.add(NoteEvent(note, "RELEASE", timestamp))
        }
    }

    fun saveRecording(stage: Stage) {
        val fileChooser = FileChooser().apply {
            title = "Сохранить запись"
            extensionFilters.add(FileChooser.ExtensionFilter("Recording Files", "*.rec"))
        }
        val file = fileChooser.showSaveDialog(stage)
        if (file != null) {
            PrintWriter(file).use { writer ->
                for (event in events) {
                    writer.println("${event.note},${event.type},${event.timestamp}")
                }
            }
        }
    }
}
