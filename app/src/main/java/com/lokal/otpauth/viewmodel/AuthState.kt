package com.lokal.otpauth.viewmodel

/**
 * Sealed interface representing the authentication UI states.
 *
 * Using sealed interface for type-safe, exhaustive state handling in Compose.
 * Each state contains only the data needed for that specific screen.
 */
sealed interface AuthState {
    
    /**
     * Initial state - user enters their email address.
     */
    data class EmailInput(
        val email: String = "",
        val isLoading: Boolean = false,
        val error: String? = null
    ) : AuthState
    
    /**
     * OTP entry state - user enters the received OTP.
     *
     * @property email The email address (for display and validation)
     * @property otp The locally generated OTP (for demo display)
     * @property remainingSeconds Countdown timer value
     * @property remainingAttempts Number of attempts left
     * @property error Error message if validation failed
     * @property isLoading Loading state during validation
     */
    data class OtpEntry(
        val email: String,
        val otp: String, // For demo purposes - shows the generated OTP
        val remainingSeconds: Int,
        val remainingAttempts: Int,
        val error: String? = null,
        val isLoading: Boolean = false
    ) : AuthState
    
    /**
     * Session state - user is logged in.
     *
     * @property email The logged-in user's email
     * @property sessionStartTime Timestamp when session started
     * @property sessionDurationSeconds Live-updating session duration
     */
    data class Session(
        val email: String,
        val sessionStartTime: Long,
        val sessionDurationSeconds: Long = 0
    ) : AuthState
}
