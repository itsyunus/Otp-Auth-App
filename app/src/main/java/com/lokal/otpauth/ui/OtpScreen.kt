package com.lokal.otpauth.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lokal.otpauth.ui.theme.ErrorColor
import com.lokal.otpauth.ui.theme.SuccessColor
import com.lokal.otpauth.ui.theme.WarningColor
import com.lokal.otpauth.viewmodel.AuthState

/**
 * OTP entry screen composable.
 *
 * Features:
 * - 6-digit OTP input with individual boxes
 * - Visual countdown timer (mm:ss format)
 * - Remaining attempts display
 * - Resend OTP button
 * - Demo OTP display (for testing without actual email)
 * - Error handling for expired/invalid OTP
 *
 * @param state The current OtpEntry state
 * @param onVerifyOtp Callback when OTP is submitted
 * @param onResendOtp Callback when Resend button is clicked
 * @param onBack Callback to go back to email screen
 */
@Composable
fun OtpScreen(
    state: AuthState.OtpEntry,
    onVerifyOtp: (String) -> Unit,
    onResendOtp: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Use rememberSaveable to preserve OTP input across configuration changes
    var otpInput by rememberSaveable { mutableStateOf("") }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        Text(
            text = "Enter OTP",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Code sent to ${state.email}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Demo OTP display (for testing)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = "Demo OTP: ${state.otp}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // OTP input boxes
        OtpInputField(
            otpValue = otpInput,
            onOtpChange = { newValue ->
                if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
                    otpInput = newValue
                    // Auto-submit when 6 digits entered
                    if (newValue.length == 6) {
                        onVerifyOtp(newValue)
                    }
                }
            },
            isError = state.error != null
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Error message
        if (state.error != null) {
            Text(
                text = state.error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Countdown timer and attempts
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Timer
            CountdownTimer(remainingSeconds = state.remainingSeconds)
            
            // Attempts remaining
            AttemptsIndicator(remainingAttempts = state.remainingAttempts)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Verify button
        Button(
            onClick = { onVerifyOtp(otpInput) },
            enabled = otpInput.length == 6 && 
                      state.remainingSeconds > 0 && 
                      state.remainingAttempts > 0 &&
                      !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = "Verify OTP",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Resend OTP button
        TextButton(
            onClick = {
                otpInput = "" // Clear input when resending
                onResendOtp()
            },
            enabled = state.remainingSeconds == 0 || state.remainingAttempts == 0
        ) {
            Text(
                text = if (state.remainingSeconds > 0 && state.remainingAttempts > 0) 
                    "Resend OTP in ${formatTime(state.remainingSeconds)}" 
                else 
                    "Resend OTP"
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Back button
        TextButton(onClick = onBack) {
            Text("Change Email")
        }
    }
}

/**
 * Custom OTP input field with individual digit boxes.
 */
@Composable
private fun OtpInputField(
    otpValue: String,
    onOtpChange: (String) -> Unit,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = otpValue,
        onValueChange = onOtpChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        decorationBox = { _ ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = modifier
            ) {
                repeat(6) { index ->
                    val char = otpValue.getOrNull(index)?.toString() ?: ""
                    val isFocused = otpValue.length == index
                    
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .border(
                                width = 2.dp,
                                color = when {
                                    isError -> MaterialTheme.colorScheme.error
                                    isFocused -> MaterialTheme.colorScheme.primary
                                    char.isNotEmpty() -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    else -> MaterialTheme.colorScheme.outline
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        Text(
                            text = char,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    )
}

/**
 * Visual countdown timer display.
 */
@Composable
private fun CountdownTimer(
    remainingSeconds: Int,
    modifier: Modifier = Modifier
) {
    val timerColor = when {
        remainingSeconds <= 10 -> ErrorColor
        remainingSeconds <= 30 -> WarningColor
        else -> SuccessColor
    }
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = timerColor.copy(alpha = 0.1f)
        ),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "⏱️ ",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = formatTime(remainingSeconds),
                style = MaterialTheme.typography.titleMedium,
                color = timerColor
            )
        }
    }
}

/**
 * Remaining attempts indicator.
 */
@Composable
private fun AttemptsIndicator(
    remainingAttempts: Int,
    modifier: Modifier = Modifier
) {
    val color = when (remainingAttempts) {
        1 -> ErrorColor
        2 -> WarningColor
        else -> SuccessColor
    }
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "🔐 ",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "$remainingAttempts attempt${if (remainingAttempts != 1) "s" else ""} left",
                style = MaterialTheme.typography.titleMedium,
                color = color
            )
        }
    }
}

/**
 * Format seconds as mm:ss.
 */
private fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(mins, secs)
}
