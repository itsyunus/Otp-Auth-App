package com.lokal.otpauth.data

import kotlin.random.Random

/**
 * Sealed interface representing the result of OTP validation.
 */
sealed interface OtpResult {
    data object Success : OtpResult
    data object ExpiredOtp : OtpResult
    data object InvalidOtp : OtpResult
    data object MaxAttemptsExceeded : OtpResult
    data object NoOtpFound : OtpResult
}

/**
 * Singleton manager for OTP generation and validation.
 *
 * Uses a Map<String, OtpData> to store OTP data per email address.
 * This allows O(1) lookup and supports multiple concurrent users.
 *
 * Key behaviors:
 * - Generating a new OTP invalidates any existing OTP for that email
 * - Each new OTP resets the attempt count to 0
 * - OTPs expire after 60 seconds
 * - Maximum of 3 validation attempts per OTP
 */
object OtpManager {
    
    // Thread-safe storage of OTP data per email
    private val otpStore = mutableMapOf<String, OtpData>()
    
    /**
     * Generate a new 6-digit OTP for the given email.
     * This invalidates any existing OTP and resets the attempt count.
     *
     * @param email The email address to generate OTP for
     * @return The generated 6-digit OTP string
     */
    @Synchronized
    fun generateOtp(email: String): String {
        val otp = generateRandomOtp()
        val expiryTime = System.currentTimeMillis() + (OtpData.OTP_EXPIRY_SECONDS * 1000L)
        
        // Store new OTP data, replacing any existing entry
        otpStore[email] = OtpData(
            otp = otp,
            expiryTime = expiryTime,
            attemptCount = 0
        )
        
        return otp
    }
    
    /**
     * Validate the provided OTP for the given email.
     *
     * @param email The email address to validate OTP for
     * @param inputOtp The OTP entered by the user
     * @return OtpResult indicating success or specific failure reason
     */
    @Synchronized
    fun validateOtp(email: String, inputOtp: String): OtpResult {
        val otpData = otpStore[email] ?: return OtpResult.NoOtpFound
        
        // Check if max attempts exceeded first
        if (otpData.isMaxAttemptsExceeded()) {
            return OtpResult.MaxAttemptsExceeded
        }
        
        // Check if OTP has expired
        if (otpData.isExpired()) {
            return OtpResult.ExpiredOtp
        }
        
        // Validate the OTP
        return if (otpData.otp == inputOtp) {
            // Success - clear the OTP data
            otpStore.remove(email)
            OtpResult.Success
        } else {
            // Wrong OTP - increment attempt count
            otpStore[email] = otpData.copy(attemptCount = otpData.attemptCount + 1)
            
            // Check if this was the last attempt
            if (otpData.attemptCount + 1 >= OtpData.MAX_ATTEMPTS) {
                OtpResult.MaxAttemptsExceeded
            } else {
                OtpResult.InvalidOtp
            }
        }
    }
    
    /**
     * Get the remaining time in seconds for the OTP associated with the email.
     *
     * @param email The email address to check
     * @return Remaining seconds, or 0 if no OTP exists or it has expired
     */
    @Synchronized
    fun getRemainingTime(email: String): Int {
        return otpStore[email]?.getRemainingSeconds() ?: 0
    }
    
    /**
     * Get the remaining attempts for the email.
     *
     * @param email The email address to check
     * @return Remaining attempts, or 0 if no OTP exists
     */
    @Synchronized
    fun getRemainingAttempts(email: String): Int {
        val otpData = otpStore[email] ?: return 0
        return (OtpData.MAX_ATTEMPTS - otpData.attemptCount).coerceAtLeast(0)
    }
    
    /**
     * Clear OTP data for an email (used on logout or session end).
     */
    @Synchronized
    fun clearOtp(email: String) {
        otpStore.remove(email)
    }
    
    /**
     * Generate a random 6-digit OTP string.
     */
    private fun generateRandomOtp(): String {
        return (0 until OtpData.OTP_LENGTH)
            .map { Random.nextInt(0, 10) }
            .joinToString("")
    }
}
