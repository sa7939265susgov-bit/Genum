package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin

data class AudioPreset(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val spokenText: String,
    val durationMs: Long
)

class AudioCallManager(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var toneGenerator: ToneGenerator? = null
    private var isRecording = false
    private var currentRecordingFile: File? = null

    // Text-To-Speech engine
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (e: Exception) {
        null
    }

    val audioPresets = listOf(
        AudioPreset(
            id = "preset_batelco_dispatch",
            title = "Batelco Voice Dispatch",
            category = "Bahrain Telecom",
            description = "Official Bahrain Batelco automated network dispatch memo",
            spokenText = "This is a Batelco telecommunications verified audio dispatch. Your connection delivery has been registered successfully.",
            durationMs = 6000L
        ),
        AudioPreset(
            id = "preset_delivery_notice",
            title = "5-Second Ring Delivery Assurance",
            category = "Delivery Verification",
            description = "Priority delivery alert voice memo confirming receipt",
            spokenText = "Urgent delivery alert: Transmission confirmed. Recipient call verified after 5-second ring test.",
            durationMs = 5500L
        ),
        AudioPreset(
            id = "preset_stc_vip",
            title = "stc Bahrain High-Speed Alert",
            category = "Bahrain Telecom",
            description = "stc network automated voice greeting and connection test",
            spokenText = "Welcome to stc Bahrain network. Automated audio delivery test is streaming successfully in normal call mode.",
            durationMs = 7000L
        ),
        AudioPreset(
            id = "preset_zain_memo",
            title = "Zain Global Audio Broadcast",
            category = "Bahrain Telecom",
            description = "Zain mobile worldwide voice dispatch memo",
            spokenText = "Zain global mobile transmission. Voice memo payload delivered. Thank you for calling.",
            durationMs = 5000L
        ),
        AudioPreset(
            id = "preset_courier_arrival",
            title = "Courier & Package Arrival Notice",
            category = "Courier & Logistics",
            description = "Automated arrival call announcement for parcel delivery",
            spokenText = "Hello, your courier delivery has arrived at your location. Please receive your package.",
            durationMs = 5000L
        ),
        AudioPreset(
            id = "preset_urgent_callback",
            title = "Urgent Callback Request",
            category = "General Notice",
            description = "Priority notice requesting immediate callback",
            spokenText = "Attention please. You have an urgent notification. Please return this call at your earliest convenience.",
            durationMs = 5200L
        ),
        AudioPreset(
            id = "preset_bahrain_chime",
            title = "Bahrain Central Switchboard Chime",
            category = "Telecom Chimes",
            description = "High-definition multi-tone telecom melodic switchboard sequence",
            spokenText = "Bahrain central network switchboard signal. Audio channel active.",
            durationMs = 4500L
        )
    )

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_VOICE_CALL, 80)
        } catch (e: Exception) {
            Log.e("AudioCallManager", "Could not initialize ToneGenerator", e)
        }

        try {
            textToSpeech = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech?.language = Locale.US
                    isTtsReady = true
                }
            }
        } catch (e: Exception) {
            Log.e("AudioCallManager", "TTS initialization failed", e)
        }
    }

    /**
     * Start recording custom user audio message.
     * Uses 32 kbps compact voice encoding to minimize storage footprint!
     */
    fun startRecording(onSuccess: (File) -> Unit, onError: (String) -> Unit): Boolean {
        stopRecording()
        stopPlayback()

        try {
            val audioDir = File(context.cacheDir, "audio_memos")
            if (!audioDir.exists()) audioDir.mkdirs()

            // Remove previous recordings to avoid filling storage
            audioDir.listFiles()?.forEach { f ->
                if (f.name.startsWith("audio_memo_")) {
                    f.delete()
                }
            }

            val file = File(audioDir, "audio_memo_${System.currentTimeMillis()}.m4a")
            currentRecordingFile = file

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                // Ultra-light 32 kbps voice bitrate (saves phone storage drastically!)
                setAudioEncodingBitRate(32000)
                setAudioSamplingRate(22050)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            isRecording = true
            onSuccess(file)
            return true
        } catch (e: Exception) {
            Log.e("AudioCallManager", "Failed to start recording", e)
            onError(e.localizedMessage ?: "Recording failed. Please ensure microphone permission is granted.")
            return false
        }
    }

    fun stopRecording(): File? {
        if (!isRecording) return null
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            currentRecordingFile
        } catch (e: Exception) {
            Log.e("AudioCallManager", "Error stopping recording", e)
            mediaRecorder = null
            isRecording = false
            null
        }
    }

    fun getMaxAmplitude(): Int {
        return try {
            mediaRecorder?.maxAmplitude ?: 0
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Import sound file from phone storage via Content Uri.
     * Keeps storage clean by replacing older imported files.
     */
    fun importAudioFromUri(uri: Uri): File? {
        return try {
            val audioDir = File(context.cacheDir, "audio_memos")
            if (!audioDir.exists()) audioDir.mkdirs()

            // Remove older imported files so storage doesn't grow
            audioDir.listFiles()?.forEach { f ->
                if (f.name.startsWith("imported_")) {
                    f.delete()
                }
            }

            val targetFile = File(audioDir, "imported_${System.currentTimeMillis()}.m4a")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            targetFile
        } catch (e: Exception) {
            Log.e("AudioCallManager", "Failed to import audio from uri", e)
            null
        }
    }

    /**
     * Speak text using Android Text-To-Speech engine.
     * Falls back to synthesized voice melodic tones if TTS is unavailable.
     */
    fun speakText(text: String, onCompletion: () -> Unit) {
        stopPlayback()
        if (isTtsReady && textToSpeech != null) {
            val utteranceId = "tts_${System.currentTimeMillis()}"
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(id: String?) {}
                override fun onDone(id: String?) {
                    CoroutineScope(Dispatchers.Main).launch { onCompletion() }
                }
                override fun onError(id: String?) {
                    CoroutineScope(Dispatchers.Main).launch { onCompletion() }
                }
            })
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } else {
            // Fallback to rich synthesized voice memo
            playSynthesizedVoiceMemo("preset_delivery_notice", onCompletion)
        }
    }

    /**
     * Plays the audio message (custom file, imported file, TTS, or preset).
     */
    fun playAudio(
        filePath: String?,
        presetId: String?,
        ttsText: String? = null,
        onCompletion: () -> Unit
    ) {
        stopPlayback()

        // 1. If TTS text is specified
        if (!ttsText.isNullOrBlank() && isTtsReady) {
            speakText(ttsText, onCompletion)
            return
        }

        // 2. If custom recorded/imported file exists, play it
        if (!filePath.isNullOrBlank()) {
            val file = File(filePath)
            if (file.exists()) {
                try {
                    val player = MediaPlayer().apply {
                        setDataSource(file.absolutePath)
                        prepare()
                        setOnCompletionListener {
                            onCompletion()
                        }
                        start()
                    }
                    mediaPlayer = player
                    return
                } catch (e: Exception) {
                    Log.e("AudioCallManager", "Failed to play audio file", e)
                }
            }
        }

        // 3. Fallback: Speak spoken text of preset if TTS available
        val preset = audioPresets.firstOrNull { it.id == presetId }
        if (preset != null && isTtsReady) {
            speakText(preset.spokenText, onCompletion)
            return
        }

        // 4. Ultimate fallback: Synthesize authentic telecom voice frequencies
        playSynthesizedVoiceMemo(presetId, onCompletion)
    }

    /**
     * Synthesizes telecom audio announcement chime & harmonic voice frequencies
     * through AudioTrack so that audio is ALWAYS clearly audible and crisp with ZERO storage.
     */
    fun playSynthesizedVoiceMemo(presetId: String?, onCompletion: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sampleRate = 44100
                val frequencies = when (presetId) {
                    "preset_stc_vip" -> doubleArrayOf(440.0, 554.37, 659.25, 880.0, 659.25)
                    "preset_zain_memo" -> doubleArrayOf(523.25, 659.25, 783.99, 1046.50, 783.99)
                    "preset_delivery_notice" -> doubleArrayOf(587.33, 739.99, 880.0, 1174.66, 880.0)
                    "preset_courier_arrival" -> doubleArrayOf(349.23, 440.0, 523.25, 698.46, 523.25)
                    "preset_urgent_callback" -> doubleArrayOf(659.25, 523.25, 659.25, 523.25, 783.99)
                    "preset_bahrain_chime" -> doubleArrayOf(392.0, 440.0, 523.25, 659.25, 783.99, 1046.50)
                    else -> doubleArrayOf(392.0, 523.25, 659.25, 783.99, 523.25)
                }

                val bufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize * 2)
                    .build()

                audioTrack.play()

                for (freq in frequencies) {
                    val durationSeconds = 0.45
                    val numSamples = (durationSeconds * sampleRate).toInt()
                    val buffer = ShortArray(numSamples)

                    for (i in 0 until numSamples) {
                        val t = i.toDouble() / sampleRate
                        val envelope = sin(PI * i / numSamples)
                        val sample = (sin(2.0 * PI * freq * t) * envelope * Short.MAX_VALUE * 0.65).toInt().toShort()
                        buffer[i] = sample
                    }
                    audioTrack.write(buffer, 0, buffer.size)
                    delay(80)
                }

                // Final delivery confirmation chime
                val beepDuration = 0.5
                val beepSamples = (beepDuration * sampleRate).toInt()
                val beepBuffer = ShortArray(beepSamples)
                for (i in 0 until beepSamples) {
                    val t = i.toDouble() / sampleRate
                    val sample = (sin(2.0 * PI * 880.0 * t) * 0.55 * Short.MAX_VALUE).toInt().toShort()
                    beepBuffer[i] = sample
                }
                audioTrack.write(beepBuffer, 0, beepBuffer.size)

                delay(150)
                audioTrack.stop()
                audioTrack.release()

                launch(Dispatchers.Main) {
                    onCompletion()
                }
            } catch (e: Exception) {
                Log.e("AudioCallManager", "Synth audio playback error", e)
                launch(Dispatchers.Main) {
                    onCompletion()
                }
            }
        }
    }

    /**
     * Start authentic telephone ringback tone (e.g. standard PBX ring)
     */
    fun startRingTone(): RingingSession {
        val session = RingingSession()
        session.start()
        return session
    }

    inner class RingingSession {
        private var job: Job? = null
        private var isRunning = false

        fun start() {
            isRunning = true
            job = CoroutineScope(Dispatchers.IO).launch {
                while (isActive && isRunning) {
                    try {
                        toneGenerator?.startTone(ToneGenerator.TONE_SUP_RINGTONE, 1800)
                        triggerVibrationPulse()
                        delay(2000)
                        delay(2000) // 2 sec sound + 2 sec silence cadence
                    } catch (e: Exception) {
                        Log.e("RingingSession", "Ring cadence error", e)
                        break
                    }
                }
            }
        }

        fun stop() {
            isRunning = false
            job?.cancel()
            try {
                toneGenerator?.stopTone()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun playDtmf(digit: Char) {
        val tone = when (digit) {
            '1' -> ToneGenerator.TONE_DTMF_1
            '2' -> ToneGenerator.TONE_DTMF_2
            '3' -> ToneGenerator.TONE_DTMF_3
            '4' -> ToneGenerator.TONE_DTMF_4
            '5' -> ToneGenerator.TONE_DTMF_5
            '6' -> ToneGenerator.TONE_DTMF_6
            '7' -> ToneGenerator.TONE_DTMF_7
            '8' -> ToneGenerator.TONE_DTMF_8
            '9' -> ToneGenerator.TONE_DTMF_9
            '0' -> ToneGenerator.TONE_DTMF_0
            '*' -> ToneGenerator.TONE_DTMF_S
            '#' -> ToneGenerator.TONE_DTMF_P
            else -> ToneGenerator.TONE_PROP_BEEP
        }
        try {
            toneGenerator?.startTone(tone, 150)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun playCallEndedTone() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 400)
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun triggerVibrationPulse() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 400, 200, 400), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 400, 200, 400), -1)
            }
        } catch (e: Exception) {
            // ignore vibration failures
        }
    }

    fun stopPlayback() {
        try {
            if (textToSpeech?.isSpeaking == true) {
                textToSpeech?.stop()
            }
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            // ignore
        }
    }

    /**
     * Calculates storage used by audio memos in cache directory.
     */
    fun getAudioStorageBytes(): Long {
        val dir = File(context.cacheDir, "audio_memos")
        if (!dir.exists()) return 0L
        return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    /**
     * Clears all cached audio memos to free up device storage.
     */
    fun clearAudioStorage(): Long {
        val bytesFreed = getAudioStorageBytes()
        val dir = File(context.cacheDir, "audio_memos")
        if (dir.exists()) {
            dir.listFiles()?.forEach { it.delete() }
        }
        return bytesFreed
    }

    /**
     * Formats storage bytes into readable string.
     */
    fun formatStorageSize(bytes: Long): String {
        return when {
            bytes <= 0 -> "0 KB (Zero-Storage)"
            bytes < 1024 -> "$bytes B (Ultra-light)"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB (Compact Voice)"
            else -> String.format(Locale.US, "%.1f MB", bytes.toDouble() / (1024 * 1024))
        }
    }

    fun release() {
        stopRecording()
        stopPlayback()
        try {
            toneGenerator?.release()
            toneGenerator = null
            textToSpeech?.shutdown()
            textToSpeech = null
        } catch (e: Exception) {
            // ignore
        }
    }
}
