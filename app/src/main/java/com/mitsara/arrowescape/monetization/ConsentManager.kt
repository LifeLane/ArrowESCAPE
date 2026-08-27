package com.mitsara.arrowescape.monetization

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ConsentStatus {
    UNKNOWN,
    NOT_REQUIRED,
    REQUIRED,
    OBTAINED,
    DENIED
}

object ConsentManager {

    private val _consentStatus = MutableStateFlow(ConsentStatus.OBTAINED)
    val consentStatus: StateFlow<ConsentStatus> = _consentStatus.asStateFlow()

    private val _isPersonalizedAdsEnabled = MutableStateFlow(true)
    val isPersonalizedAdsEnabled: StateFlow<Boolean> = _isPersonalizedAdsEnabled.asStateFlow()

    fun updateConsentStatus(status: ConsentStatus) {
        _consentStatus.value = status
    }

    fun setPersonalizedAdsEnabled(enabled: Boolean) {
        _isPersonalizedAdsEnabled.value = enabled
    }

    fun canShowAds(): Boolean {
        return _consentStatus.value == ConsentStatus.OBTAINED ||
                _consentStatus.value == ConsentStatus.NOT_REQUIRED
    }
}
