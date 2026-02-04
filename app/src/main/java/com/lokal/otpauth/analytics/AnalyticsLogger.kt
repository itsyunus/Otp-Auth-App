package com.lokal.otpauth.analytics

import timber.log.Timber

/**
 * Analytics logger using Timber for event tracking.
 *
 * Logs the following events as required:
 * - OTP generated
 * - OTP validation success
 * - OTP validation failure
 * - Logout
 *
 * All logs use the "OtpAuth" tag for easy filtering in Logcat.
 */
object AnalyticsLogger {
    
    private const val TAG = "OtpAuth"
    
    /**
     * Log when a new OTP is generated for an email.
     */
    fun logOtpGenerated(email: String) {
        Timber.tag(TAG).i("OTP Generated for: %s", maskEmail(email))
    }
    
    /**
     * Log when OTP validation succeeds.
     */
    fun logOtpSuccess(email: String) {
        Timber.tag(TAG).i("OTP Validation Success for: %s", maskEmail(email))
    }
    
    /**
     * Log when OTP validation fails with a specific reason.
     */
    fun logOtpFailure(email: String, reason: String) {
        Timber.tag(TAG).w("OTP Validation Failed for: %s, Reason: %s", maskEmail(email), reason)
    }
    
    /**
     * Log when a user logs out.
     */
    fun logLogout(email: String) {
        Timber.tag(TAG).i("User Logged Out: %s", maskEmail(email))
    }
    
    /**
     * Mask email for privacy in logs (shows first 2 chars + domain).
     * Example: "user@example.com" -> "us***@example.com"
     */
    private fun maskEmail(email: String): String {
        val atIndex = email.indexOf('@')
        return if (atIndex > 2) {
            "${email.substring(0, 2)}***${email.substring(atIndex)}"
        } else {
            "***${email.substringAfter('@', email)}"
        }
    }
}
