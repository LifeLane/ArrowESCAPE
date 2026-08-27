package com.mitsara.arrowescape.monetization

import com.mitsara.arrowescape.BuildConfig

data class AdConfig(
    val testMode: Boolean = BuildConfig.DEBUG,
    val bannerEnabled: Boolean = true,
    val interstitialEnabled: Boolean = true,
    val rewardedEnabled: Boolean = true,
    val nativeEnabled: Boolean = true,
    val entryExitEnabled: Boolean = true,
    val fullPageRewardedEnabled: Boolean = true,
    val adUnitAppId: String = ADMOB_APP_ID,
    val adUnitBannerId: String = if (BuildConfig.DEBUG) TEST_BANNER_ID else PROD_BANNER_ID,
    val adUnitEntryExitId: String = if (BuildConfig.DEBUG) TEST_ENTRY_EXIT_ID else PROD_ENTRY_EXIT_ID,
    val adUnitFullPageRewardedId: String = if (BuildConfig.DEBUG) TEST_FULL_PAGE_REWARDED_ID else PROD_FULL_PAGE_REWARDED_ID,
    val adUnitInterstitialId: String = if (BuildConfig.DEBUG) TEST_INTERSTITIAL_ID else PROD_INTERSTITIAL_ID,
    val adUnitNativeId: String = if (BuildConfig.DEBUG) TEST_NATIVE_ID else PROD_NATIVE_ID,
    val adUnitRewardedId: String = if (BuildConfig.DEBUG) TEST_REWARDED_ID else PROD_REWARDED_ID,
    val interstitialCooldownSeconds: Long = 90L,
    val interstitialLevelInterval: Int = 3,
    val maxInterstitialsPerSession: Int = 5
) {
    companion object {
        const val ADMOB_APP_ID = "ca-app-pub-9799029854828269~1037006181"

        // Production AdMob Unit IDs
        const val PROD_ENTRY_EXIT_ID = "ca-app-pub-9799029854828269/7450371295"
        const val PROD_FULL_PAGE_REWARDED_ID = "ca-app-pub-9799029854828269/7410842843"
        const val PROD_INTERSTITIAL_ID = "ca-app-pub-9799029854828269/7120410251"
        const val PROD_NATIVE_ID = "ca-app-pub-9799029854828269/4551860732"
        const val PROD_REWARDED_ID = "ca-app-pub-9799029854828269/7557298020"
        const val PROD_BANNER_ID = "ca-app-pub-9799029854828269/7450371295"

        // Google Test Ad Unit IDs
        const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
        const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
        const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"
        const val TEST_NATIVE_ID = "ca-app-pub-3940256099942544/2247696110"
        const val TEST_ENTRY_EXIT_ID = "ca-app-pub-3940256099942544/9257395921"
        const val TEST_FULL_PAGE_REWARDED_ID = "ca-app-pub-3940256099942544/5354046379"

        fun createDefault(): AdConfig = AdConfig()
    }
}

