package com.lokal.otpauth.data

/**
 * Data class representing OTP information for a specific email.
 *
 * @property otp The 6-digit OTP code
 * @property expiryTime Timestamp (in milliseconds) when the OTP expires
 * @property attemptCount Number of failed validation attempts (max 3)
 */
data class OtpData(
    val otp: String,
    val expiryTime: Long,
    val attemptCount: Int = 0
) {
    companion object {
        const val OTP_LENGTH = 6
        const val OTP_EXPIRY_SECONDS = 60
        const val MAX_ATTEMPTS = 3
    }

    /**
     * Check if the OTP has expired based on current time
     */
    fun isExpired(): Boolean = System.currentTimeMillis() > expiryTime

    /**
     * Check if maximum attempts have been exceeded
     */
    fun isMaxAttemptsExceeded(): Boolean = attemptCount >= MAX_ATTEMPTS

    /**
     * Get remaining time in seconds until expiry
     */
    fun getRemainingSeconds(): Int {
        val remaining = (expiryTime - System.currentTimeMillis()) / 1000
        return remaining.coerceAtLeast(0).toInt()
    }
}
