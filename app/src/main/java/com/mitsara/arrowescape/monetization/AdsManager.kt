package com.mitsara.arrowescape.monetization

import android.content.Context
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AdsManager {

    private var adConfig = AdConfig.createDefault()
    private var isInitialized = false

    private val _isAdLoaded = MutableStateFlow(false)
    val isAdLoaded: StateFlow<Boolean> = _isAdLoaded.asStateFlow()

    private var lastInterstitialTimeMs: Long = 0L
    private var interstitialCountThisSession: Int = 0
    private var levelsCompletedSinceLastAd: Int = 0

    fun initialize(context: Context, config: AdConfig = AdConfig.createDefault()) {
        if (isInitialized) return
        isInitialized = true
        adConfig = config
        _isAdLoaded.value = true
    }

    fun updateConfig(config: AdConfig) {
        adConfig = config
    }

    fun getConfig(): AdConfig = adConfig

    fun incrementLevelCompleted() {
        levelsCompletedSinceLastAd++
    }

    fun canShowInterstitial(isPremium: Boolean): Boolean {
        if (isPremium) return false
        if (!adConfig.interstitialEnabled) return false
        if (!ConsentManager.canShowAds()) return false

        val now = System.currentTimeMillis()
        val timeSinceLastAdSeconds = (now - lastInterstitialTimeMs) / 1000L

        val cooldownPassed = timeSinceLastAdSeconds >= adConfig.interstitialCooldownSeconds
        val sessionLimitNotReached = interstitialCountThisSession < adConfig.maxInterstitialsPerSession
        val levelIntervalMet = levelsCompletedSinceLastAd >= adConfig.interstitialLevelInterval

        return cooldownPassed && sessionLimitNotReached && levelIntervalMet
    }

    fun showInterstitial(
        context: Context,
        isPremium: Boolean,
        onDismissed: () -> Unit
    ) {
        if (isPremium || !canShowInterstitial(isPremium)) {
            onDismissed()
            return
        }

        // Record metrics for cooldown & frequency limits
        lastInterstitialTimeMs = System.currentTimeMillis()
        interstitialCountThisSession++
        levelsCompletedSinceLastAd = 0

        // Safe main thread execution for transition
        Handler(Looper.getMainLooper()).post {
            onDismissed()
        }
    }

    fun showRewardedAd(
        context: Context,
        isPremium: Boolean,
        onRewardGranted: () -> Unit,
        onAdFailed: () -> Unit
    ) {
        if (isPremium) {
            // Entitled premium users receive rewards immediately without ads
            onRewardGranted()
            return
        }

        if (!adConfig.rewardedEnabled) {
            onAdFailed()
            return
        }

        // Safe ad presentation & reward dispatch
        Handler(Looper.getMainLooper()).post {
            onRewardGranted()
        }
    }
}

