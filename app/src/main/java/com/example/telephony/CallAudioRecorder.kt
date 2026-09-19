package com.example.telephony

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CallAudioRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    var currentRecordingPath: String? = null
        private set
    var isRecording: Boolean = false
        private set
    private var recordingStartTime: Long = 0

    val legalDisclosure: String =
        "LEGAL NOTICE: Audio recording complies strictly with Android telecommunication guidelines and privacy laws. Two-party consent is legally required in most jurisdictions. CallOra never secretly records conversations."

    fun startRecording(phoneNumber: String?): String? {
        if (isRecording) return currentRecordingPath

        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(context.filesDir, "recording_${phoneNumber ?: "call"}_$timestamp.m4a")
            currentRecordingPath = file.absolutePath

            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            recordingStartTime = System.currentTimeMillis()
            isRecording = true
            return file.absolutePath
        } catch (e: Exception) {
            Log.e("CallAudioRecorder", "Recording start failed: ${e.message}")
            isRecording = false
            return null
        }
    }

    fun stopRecording(): Int {
        if (!isRecording) return 0
        var durationSeconds = 0
        try {
            recorder?.stop()
            recorder?.release()
            recorder = null
            durationSeconds = ((System.currentTimeMillis() - recordingStartTime) / 1000).toInt()
        } catch (e: Exception) {
            Log.e("CallAudioRecorder", "Recording stop failed: ${e.message}")
        } finally {
            isRecording = false
        }
        return durationSeconds
    }

    fun playRecording(filePath: String, onComplete: () -> Unit) {
        stopPlayback()
        try {
            player = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                setOnCompletionListener {
                    onComplete()
                }
                start()
            }
        } catch (e: Exception) {
            Log.e("CallAudioRecorder", "Playback failed: ${e.message}")
            onComplete()
        }
    }

    fun stopPlayback() {
        try {
            player?.stop()
            player?.release()
            player = null
        } catch (e: Exception) {
            // Ignore
        }
    }
}
