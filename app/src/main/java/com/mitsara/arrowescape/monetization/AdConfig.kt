package com.mitsara.arrowescape.monetization

data class AdConfig(
    val testMode: Boolean = true,
    val bannerEnabled: Boolean = true,
    val interstitialEnabled: Boolean = true,
    val rewardedEnabled: Boolean = true,
    val adUnitBannerId: String = TEST_BANNER_ID,
    val adUnitInterstitialId: String = TEST_INTERSTITIAL_ID,
    val adUnitRewardedId: String = TEST_REWARDED_ID,
    val interstitialCooldownSeconds: Long = 90L,
    val interstitialLevelInterval: Int = 3,
    val maxInterstitialsPerSession: Int = 5
) {
    companion object {
        const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
        const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
        const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

        fun createDefault(): AdConfig = AdConfig()
    }
}
