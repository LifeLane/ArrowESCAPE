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

    fun playTapSound() {
        if (!soundEnabled) return
        scope.launch {
            playTone(frequency = 587.33, durationMs = 40, attack = 0.05, decay = 0.95) // D5 click
        }
    }

    fun playEscapeSound() {
        if (!soundEnabled) return
        scope.launch {
            playArpeggio(listOf(523.25, 659.25, 783.99, 1046.50), noteDurationMs = 35) // C5-E5-G5-C6 sweep
        }
        vibrate(durationMs = 25, strength = 80)
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
        if (!vibrationEnabled || vibrator == null || !vibrator!!.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(durationMs, strength.coerceIn(1, 255))
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
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
