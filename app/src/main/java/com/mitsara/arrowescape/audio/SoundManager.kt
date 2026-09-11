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
import kotlin.math.PI
import kotlin.math.exp
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

    /**
     * Subtle, tactile click on arrow touch
     */
    fun playTapSound(themeId: String = "EYE_COMFORT") {
        if (!soundEnabled) return
        val freq = when (themeId) {
            "CYBER_TERMINAL" -> 783.99 // G5 laser click
            "ZEN_WOOD" -> 440.0 // A4 warm wood tap
            "DARK_SLATE" -> 523.25 // C5 slate ping
            "SOFT_MINT" -> 587.33 // D5 leaf ping
            else -> 659.25 // E5 parchment kalimba tap
        }
        scope.launch {
            playSingleTone(frequency = freq, durationMs = 30, attack = 0.05, decay = 0.95, volume = 0.25f)
        }
        vibrate(durationMs = 10, strength = 35)
    }

    /**
     * Subtle, satisfying audio arpeggio on arrow escape.
     * Escalates in pitch, note count, and harmonic richness with each combo multiplier.
     */
    fun playEscapeArpeggio(comboMultiplier: Int = 1, themeId: String = "EYE_COMFORT") {
        if (!soundEnabled) return

        // Base fundamental scale by theme
        val baseScale = when (themeId) {
            "ZEN_WOOD" -> listOf(329.63, 392.00, 440.00, 493.88, 587.33, 659.25, 783.99) // E4 minor pentatonic warm cedar
            "CYBER_TERMINAL" -> listOf(440.00, 554.37, 659.25, 830.61, 880.00, 1108.73, 1318.51) // A major cyber synth
            "DARK_SLATE" -> listOf(392.00, 440.00, 523.25, 587.33, 659.25, 783.99, 880.00) // G pentatonic clean slate
            "SOFT_MINT" -> listOf(349.23, 440.00, 523.25, 587.33, 698.46, 783.99, 880.00) // F major serene green
            else -> listOf(523.25, 587.33, 659.25, 783.99, 880.00, 1046.50, 1174.66, 1318.51) // C major bright parchment
        }

        val clampedCombo = comboMultiplier.coerceIn(1, 5)

        // Arpeggio notes scale with combo
        val notes = when (clampedCombo) {
            1 -> listOf(baseScale[0], baseScale[2]) // 2-note harmonious pair
            2 -> listOf(baseScale[0], baseScale[2], baseScale[4]) // 3-note ascending triad
            3 -> listOf(baseScale[1], baseScale[3], baseScale[4], baseScale[minOf(5, baseScale.size - 1)]) // 4-note ascending sparkle
            4 -> listOf(baseScale[0], baseScale[2], baseScale[3], baseScale[4], baseScale[minOf(6, baseScale.size - 1)]) // 5-note pentatonic run
            else -> listOf(baseScale[0], baseScale[2], baseScale[4], baseScale[minOf(5, baseScale.size - 1)], baseScale[minOf(6, baseScale.size - 1)], baseScale[minOf(7, baseScale.size - 1)] * 1.5) // 6-note Frenzy sweep
        }

        val noteDuration = when (clampedCombo) {
            1 -> 35
            2 -> 30
            3 -> 25
            4 -> 22
            else -> 20
        }

        scope.launch {
            playSynthesizedArpeggio(notes, noteDurationMs = noteDuration, themeId = themeId)
        }

        val hapticStrength = when (clampedCombo) {
            1 -> 40
            2 -> 65
            3 -> 95
            4 -> 130
            else -> 180
        }
        vibrate(durationMs = (20 + clampedCombo * 6).toLong(), strength = hapticStrength)
    }

    /**
     * Backward compatibility wrapper for escape sound
     */
    fun playEscapeSound(themeId: String = "EYE_COMFORT") {
        playEscapeArpeggio(comboMultiplier = 1, themeId = themeId)
    }

    /**
     * Subtle, tactile acoustic obstruction thud when an arrow is blocked.
     * Soft wooden/glass knock that provides clear feedback without being punishing.
     */
    fun playObstructionSound(themeId: String = "EYE_COMFORT") {
        if (!soundEnabled) return

        val (startFreq, endFreq) = when (themeId) {
            "ZEN_WOOD" -> Pair(160.0, 110.0) // Deep cedar block knock
            "CYBER_TERMINAL" -> Pair(260.0, 140.0) // Low electro blip
            "DARK_SLATE" -> Pair(200.0, 130.0) // Subtle slate thud
            else -> Pair(190.0, 120.0) // Soft acoustic parchment knock
        }

        scope.launch {
            playPitchDropThud(startFreq = startFreq, endFreq = endFreq, durationMs = 85, volume = 0.35f)
        }
        vibrate(durationMs = 35, strength = 90)
    }

    /**
     * Backward compatibility wrapper for mistake sound
     */
    fun playMistakeSound() {
        playObstructionSound()
    }

    /**
     * Shimmering chime when hint is activated
     */
    fun playHintSound() {
        if (!soundEnabled) return
        scope.launch {
            playSynthesizedArpeggio(listOf(659.25, 880.00, 1174.66), noteDurationMs = 45, themeId = "EYE_COMFORT")
        }
        vibrate(durationMs = 20, strength = 50)
    }

    /**
     * Pleasant chime notification when hint finishes cooldown / recharges
     */
    fun playHintReadySound() {
        if (!soundEnabled) return
        scope.launch {
            playSingleTone(frequency = 1046.50, durationMs = 60, attack = 0.1, decay = 0.9, volume = 0.2f)
        }
    }

    /**
     * Rewarding melodic chord on level victory
     */
    fun playVictorySound() {
        if (!soundEnabled) return
        scope.launch {
            val victoryNotes = listOf(523.25, 659.25, 783.99, 1046.50, 1318.51, 1567.98)
            playSynthesizedArpeggio(victoryNotes, noteDurationMs = 70, themeId = "EYE_COMFORT")
        }
        vibrate(durationMs = 180, strength = 140)
    }

    /**
     * Daily streak claimed golden fanfare
     */
    fun playDailyRewardClaimSound() {
        if (!soundEnabled) return
        scope.launch {
            val fanfare = listOf(440.0, 554.37, 659.25, 880.0, 1108.73, 1318.51)
            playSynthesizedArpeggio(fanfare, noteDurationMs = 65, themeId = "EYE_COMFORT")
        }
        vibrate(durationMs = 140, strength = 160)
    }

    /**
     * Power-up explosive whoosh
     */
    fun playPowerupBlastSound() {
        if (!soundEnabled) return
        scope.launch {
            val blastNotes = listOf(180.0, 320.0, 580.0, 940.0, 1480.0)
            playSynthesizedArpeggio(blastNotes, noteDurationMs = 35, themeId = "CYBER_TERMINAL")
        }
        vibrate(durationMs = 160, strength = 220)
    }

    fun playPentatonicChime(step: Int = 0) {
        if (!soundEnabled) return
        val pentatonicScale = listOf(261.63, 293.66, 329.63, 392.00, 440.00, 523.25, 587.33, 659.25, 783.99, 880.00)
        val freq = pentatonicScale[step % pentatonicScale.size]
        scope.launch {
            playSingleTone(frequency = freq, durationMs = 80, attack = 0.05, decay = 0.95, volume = 0.3f)
        }
        vibrate(durationMs = 15, strength = 45)
    }

    fun playFrenzySweep() {
        if (!soundEnabled) return
        scope.launch {
            val frenzyNotes = listOf(523.25, 659.25, 783.99, 1046.50, 1318.51, 1567.98, 2093.00)
            playSynthesizedArpeggio(frenzyNotes, noteDurationMs = 20, themeId = "CYBER_TERMINAL")
        }
        vibrate(durationMs = 80, strength = 190)
    }

    fun playAchievementClaimSound() {
        if (!soundEnabled) return
        scope.launch {
            playSynthesizedArpeggio(listOf(523.25, 659.25, 783.99, 1046.50, 1567.98), noteDurationMs = 50, themeId = "EYE_COMFORT")
        }
        vibrate(durationMs = 120, strength = 180)
    }

    private fun vibrate(durationMs: Long, strength: Int = 128) {
        if (!vibrationEnabled || hapticLevel == "OFF" || vibrator == null || !vibrator!!.hasVibrator()) return
        val adjustedDuration = when (hapticLevel) {
            "LIGHT" -> (durationMs * 0.6).toLong().coerceAtLeast(5L)
            "HEAVY" -> (durationMs * 1.4).toLong()
            else -> durationMs
        }
        val adjustedStrength = when (hapticLevel) {
            "LIGHT" -> 60
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
            // Safe catch
        }
    }

    /**
     * Synthesize seamless, multi-note arpeggio with exponential harmonic envelope
     */
    private fun playSynthesizedArpeggio(freqs: List<Double>, noteDurationMs: Int, themeId: String) {
        try {
            val sampleRate = 44100
            val totalDurationMs = freqs.size * noteDurationMs + 40
            val totalSamples = (sampleRate * (totalDurationMs / 1000.0)).toInt()
            val samples = ShortArray(totalSamples)

            val isHarmonicRich = themeId == "CYBER_TERMINAL" || themeId == "VAPORWAVE"
            val isWoodWarm = themeId == "ZEN_WOOD"

            freqs.forEachIndexed { index, freq ->
                val startSample = (sampleRate * (index * noteDurationMs / 1000.0)).toInt()
                val noteSamples = (sampleRate * ((noteDurationMs + 35) / 1000.0)).toInt()

                for (i in 0 until noteSamples) {
                    val globalIdx = startSample + i
                    if (globalIdx >= totalSamples) break

                    val t = i.toDouble() / sampleRate
                    // Smooth bell-curve attack and natural exponential decay
                    val attackTime = 0.005
                    val decayRate = if (isWoodWarm) 35.0 else 22.0
                    val env = if (t < attackTime) (t / attackTime) else exp(-decayRate * (t - attackTime))

                    val fundamental = sin(2.0 * PI * freq * t)
                    val secondHarmonic = if (isHarmonicRich) 0.3 * sin(4.0 * PI * freq * t) else 0.15 * sin(4.0 * PI * freq * t)
                    val thirdHarmonic = if (isHarmonicRich) 0.15 * sin(6.0 * PI * freq * t) else 0.0

                    val combined = (fundamental + secondHarmonic + thirdHarmonic) * env * 0.32
                    val rawSample = (combined * 32767).toInt()
                    val existing = samples[globalIdx].toInt()
                    val mixed = (existing + rawSample).coerceIn(-32767, 32767)
                    samples[globalIdx] = mixed.toShort()
                }
            }

            renderPcmSamples(samples, sampleRate)
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    /**
     * Soft tactile pitch drop for obstructions (e.g. 190Hz -> 120Hz)
     */
    private fun playPitchDropThud(startFreq: Double, endFreq: Double, durationMs: Int, volume: Float = 0.35f) {
        try {
            val sampleRate = 44100
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            val samples = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val progress = t / (durationMs / 1000.0)
                val currentFreq = startFreq + (endFreq - startFreq) * progress

                // Quick acoustic wooden attack, fast natural damping
                val env = exp(-28.0 * t)
                val fundamental = sin(2.0 * PI * currentFreq * t)
                val woodThudHarmonic = 0.25 * sin(2.0 * PI * (currentFreq * 0.5) * t)

                val sampleValue = ((fundamental + woodThudHarmonic) * 32767 * env * volume).toInt()
                samples[i] = sampleValue.coerceIn(-32767, 32767).toShort()
            }

            renderPcmSamples(samples, sampleRate)
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    private fun playSingleTone(frequency: Double, durationMs: Int, attack: Double = 0.05, decay: Double = 0.95, volume: Float = 0.3f) {
        try {
            val sampleRate = 44100
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            val samples = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val attackDuration = durationMs * attack / 1000.0
                val env = if (t < attackDuration) {
                    (t / attackDuration)
                } else {
                    val decayDuration = durationMs * decay / 1000.0
                    (1.0 - ((t - attackDuration) / decayDuration)).coerceIn(0.0, 1.0)
                }

                val angle = 2.0 * PI * frequency * t
                val sampleValue = (sin(angle) * 32767 * env * volume).toInt()
                samples[i] = sampleValue.coerceIn(-32767, 32767).toShort()
            }

            renderPcmSamples(samples, sampleRate)
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    private fun renderPcmSamples(samples: ShortArray, sampleRate: Int) {
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
        val durationMs = (samples.size * 1000L / sampleRate) + 20
        Thread.sleep(durationMs)
        audioTrack.release()
    }
}
