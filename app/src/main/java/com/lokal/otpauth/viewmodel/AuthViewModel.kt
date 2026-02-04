package com.lokal.otpauth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lokal.otpauth.analytics.AnalyticsLogger
import com.lokal.otpauth.data.OtpData
import com.lokal.otpauth.data.OtpManager
import com.lokal.otpauth.data.OtpResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel managing authentication state and business logic.
 *
 * Follows one-way data flow pattern:
 * - UI observes [authState] StateFlow
 * - UI calls action methods (sendOtp, verifyOtp, logout)
 * - ViewModel updates state based on business logic
 *
 * This ViewModel is lifecycle-aware and survives configuration changes.
 */
class AuthViewModel : ViewModel() {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.EmailInput())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    // Job references for cancellation
    private var otpCountdownJob: Job? = null
    private var sessionTimerJob: Job? = null
    
    /**
     * Update the email in the EmailInput state.
     */
    fun updateEmail(email: String) {
        val currentState = _authState.value
        if (currentState is AuthState.EmailInput) {
            _authState.update { currentState.copy(email = email, error = null) }
        }
    }
    
    /**
     * Send OTP to the provided email address.
     * Generates a new OTP locally and transitions to OtpEntry state.
     */
    fun sendOtp(email: String) {
        if (!isValidEmail(email)) {
            _authState.update {
                AuthState.EmailInput(email = email, error = "Please enter a valid email address")
            }
            return
        }
        
        // Generate new OTP
        val otp = OtpManager.generateOtp(email)
        
        // Log the event
        AnalyticsLogger.logOtpGenerated(email)
        
        // Transition to OTP entry state
        _authState.update {
            AuthState.OtpEntry(
                email = email,
                otp = otp,
                remainingSeconds = OtpData.OTP_EXPIRY_SECONDS,
                remainingAttempts = OtpData.MAX_ATTEMPTS
            )
        }
        
        // Start countdown timer
        startOtpCountdown(email)
    }
    
    /**
     * Resend OTP - generates a new OTP and resets the timer.
     */
    fun resendOtp() {
        val currentState = _authState.value
        if (currentState is AuthState.OtpEntry) {
            sendOtp(currentState.email)
        }
    }
    
    /**
     * Verify the entered OTP.
     */
    fun verifyOtp(inputOtp: String) {
        val currentState = _authState.value
        if (currentState !is AuthState.OtpEntry) return
        
        val email = currentState.email
        
        // Validate OTP
        when (val result = OtpManager.validateOtp(email, inputOtp)) {
            is OtpResult.Success -> {
                // Log success
                AnalyticsLogger.logOtpSuccess(email)
                
                // Cancel countdown
                otpCountdownJob?.cancel()
                
                // Transition to session state
                val sessionStartTime = System.currentTimeMillis()
                _authState.update {
                    AuthState.Session(
                        email = email,
                        sessionStartTime = sessionStartTime
                    )
                }
                
                // Start session timer
                startSessionTimer(sessionStartTime)
            }
            
            is OtpResult.ExpiredOtp -> {
                AnalyticsLogger.logOtpFailure(email, "OTP Expired")
                _authState.update {
                    currentState.copy(
                        error = "OTP has expired. Please request a new one.",
                        remainingSeconds = 0
                    )
                }
            }
            
            is OtpResult.InvalidOtp -> {
                val remainingAttempts = OtpManager.getRemainingAttempts(email)
                AnalyticsLogger.logOtpFailure(email, "Invalid OTP")
                _authState.update {
                    currentState.copy(
                        error = "Invalid OTP. $remainingAttempts attempt(s) remaining.",
                        remainingAttempts = remainingAttempts
                    )
                }
            }
            
            is OtpResult.MaxAttemptsExceeded -> {
                AnalyticsLogger.logOtpFailure(email, "Max Attempts Exceeded")
                otpCountdownJob?.cancel()
                _authState.update {
                    currentState.copy(
                        error = "Maximum attempts exceeded. Please request a new OTP.",
                        remainingAttempts = 0,
                        remainingSeconds = 0
                    )
                }
            }
            
            is OtpResult.NoOtpFound -> {
                AnalyticsLogger.logOtpFailure(email, "No OTP Found")
                _authState.update {
                    currentState.copy(error = "No OTP found. Please request a new one.")
                }
            }
        }
    }
    
    /**
     * Log out the current user and return to email input.
     */
    fun logout() {
        val currentState = _authState.value
        if (currentState is AuthState.Session) {
            // Log the event
            AnalyticsLogger.logLogout(currentState.email)
            
            // Clear any stored OTP data
            OtpManager.clearOtp(currentState.email)
        }
        
        // Cancel session timer
        sessionTimerJob?.cancel()
        
        // Return to email input state
        _authState.update { AuthState.EmailInput() }
    }
    
    /**
     * Start the OTP countdown timer.
     */
    private fun startOtpCountdown(email: String) {
        otpCountdownJob?.cancel()
        otpCountdownJob = viewModelScope.launch {
            var remainingSeconds = OtpData.OTP_EXPIRY_SECONDS
            
            while (remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds--
                
                val currentState = _authState.value
                if (currentState is AuthState.OtpEntry && currentState.email == email) {
                    _authState.update {
                        currentState.copy(remainingSeconds = remainingSeconds)
                    }
                } else {
                    // State changed, stop countdown
                    break
                }
            }
        }
    }
    
    /**
     * Start the session duration timer.
     */
    private fun startSessionTimer(startTime: Long) {
        sessionTimerJob?.cancel()
        sessionTimerJob = viewModelScope.launch {
            while (true) {
                val currentState = _authState.value
                if (currentState is AuthState.Session) {
                    val durationSeconds = (System.currentTimeMillis() - startTime) / 1000
                    _authState.update {
                        currentState.copy(sessionDurationSeconds = durationSeconds)
                    }
                } else {
                    // No longer in session, stop timer
                    break
                }
                delay(1000L)
            }
        }
    }
    
    /**
     * Basic email validation.
     */
    private fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && 
               email.contains("@") && 
               email.contains(".") &&
               email.indexOf("@") < email.lastIndexOf(".")
    }
    
    override fun onCleared() {
        super.onCleared()
        otpCountdownJob?.cancel()
        sessionTimerJob?.cancel()
    }
}
