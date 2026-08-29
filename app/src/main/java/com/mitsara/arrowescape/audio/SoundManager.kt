package com.mitsara.arrowescape.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundManager(private val context: Context) {

    var soundEnabled: Boolean = true
    var vibrationEnabled: Boolean = true
    var hapticLevel: String = "MEDIUM" // OFF, LIGHT, MEDIUM, HEAVY

    private val scope = CoroutineScope(Dispatchers.Default)

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun playTapSound(themeId: String = "RETRO_ARCADE") {
        if (!soundEnabled) return
        val freq = when (themeId) {
            "CYBER_TERMINAL" -> 783.99 // G5 laser click
            "ZEN_WOOD" -> 440.0 // A4 woodblock click
            "VAPORWAVE" -> 659.25 // E5 synth click
            "QUANTUM_NEBULA" -> 880.0 // A5 cosmic chime
            else -> 587.33 // D5 arcade click
        }
        scope.launch {
            playTone(frequency = freq, durationMs = 35, attack = 0.05, decay = 0.95)
        }
        vibrate(durationMs = 12, strength = 40)
    }

    fun playEscapeSound(themeId: String = "RETRO_ARCADE") {
        if (!soundEnabled) return
        val freqs = when (themeId) {
            "CYBER_TERMINAL" -> listOf(440.0, 554.37, 659.25, 880.0) // Cyber synth arpeggio
            "ZEN_WOOD" -> listOf(329.63, 392.00, 493.88, 659.25) // Zen cedar chime
            "VAPORWAVE" -> listOf(523.25, 622.25, 783.99, 1046.50) // Synthwave chord sweep
            "QUANTUM_NEBULA" -> listOf(587.33, 739.99, 880.00, 1174.66) // Starlight arpeggio
            else -> listOf(523.25, 659.25, 783.99, 1046.50) // Retro sweep
        }
        scope.launch {
            playArpeggio(freqs, noteDurationMs = 30)
        }
        vibrate(durationMs = 25, strength = 80)
    }

    fun playPowerupBlastSound() {
        if (!soundEnabled) return
        scope.launch {
            playArpeggio(listOf(200.0, 350.0, 550.0, 900.0, 1400.0), noteDurationMs = 45) // Fire/Crusher explosion sweep
        }
        vibrate(durationMs = 180, strength = 255)
    }

    fun playMistakeSound() {
        if (!soundEnabled) return
        scope.launch {
            playTone(frequency = 150.0, durationMs = 150, attack = 0.01, decay = 0.9) // Low thud
        }
        vibrate(durationMs = 120, strength = 200)
    }

    fun playHintSound() {
        if (!soundEnabled) return
        scope.launch {
            playTone(frequency = 880.0, durationMs = 100, attack = 0.1, decay = 0.8) // A5 shimmer
        }
    }

    fun playVictorySound() {
        if (!soundEnabled) return
        scope.launch {
            playArpeggio(listOf(523.25, 659.25, 783.99, 1046.50, 1318.51), noteDurationMs = 80)
        }
        vibrate(durationMs = 200, strength = 150)
    }

    private fun vibrate(durationMs: Long, strength: Int = 128) {
        if (!vibrationEnabled || hapticLevel == "OFF" || vibrator == null || !vibrator!!.hasVibrator()) return
        val adjustedDuration = when (hapticLevel) {
            "LIGHT" -> (durationMs * 0.6).toLong()
            "HEAVY" -> (durationMs * 1.4).toLong()
            else -> durationMs
        }
        val adjustedStrength = when (hapticLevel) {
            "LIGHT" -> 80
            "HEAVY" -> 255
            else -> strength
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(adjustedDuration, adjustedStrength.coerceIn(1, 255))
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(adjustedDuration)
            }
        } catch (e: Exception) {
            // Ignore vibration permission or hardware errors
        }
    }

    private fun playTone(frequency: Double, durationMs: Int, attack: Double = 0.1, decay: Double = 0.9) {
        try {
            val sampleRate = 44100
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            val samples = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val env = when {
                    t < (durationMs * attack / 1000.0) -> t / (durationMs * attack / 1000.0)
                    else -> 1.0 - (t - durationMs * attack / 1000.0) / (durationMs * (1.0 - attack) / 1000.0)
                }.coerceIn(0.0, 1.0)

                val angle = 2.0 * Math.PI * frequency * t
                val sampleValue = (sin(angle) * 32767 * env * 0.4).toInt()
                samples[i] = sampleValue.toShort()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(samples.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(samples, 0, samples.size)
            audioTrack.play()
            Thread.sleep(durationMs.toLong() + 20)
            audioTrack.release()
        } catch (e: Exception) {
            // Audio track execution fail safe
        }
    }

    private fun playArpeggio(freqs: List<Double>, noteDurationMs: Int) {
        for (freq in freqs) {
            playTone(freq, noteDurationMs)
        }
    }
}
