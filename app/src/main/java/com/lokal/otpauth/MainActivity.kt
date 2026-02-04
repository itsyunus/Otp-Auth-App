package com.lokal.otpauth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lokal.otpauth.ui.LoginScreen
import com.lokal.otpauth.ui.OtpScreen
import com.lokal.otpauth.ui.SessionScreen
import com.lokal.otpauth.ui.theme.OtpAuthTheme
import com.lokal.otpauth.viewmodel.AuthState
import com.lokal.otpauth.viewmodel.AuthViewModel

/**
 * Main Activity - Single Activity architecture with Compose.
 *
 * Uses state-driven navigation based on AuthState sealed interface.
 * The ViewModel survives configuration changes, preserving UI state.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OtpAuthTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OtpAuthApp()
                }
            }
        }
    }
}

/**
 * Main app composable with state-driven navigation.
 *
 * Navigation is handled by observing the AuthState and rendering
 * the appropriate screen. This pattern:
 * - Avoids complex navigation libraries for simple flows
 * - Ensures type-safe, exhaustive state handling
 * - Maintains clear separation of concerns
 */
@Composable
fun OtpAuthApp(
    viewModel: AuthViewModel = viewModel()
) {
    // Collect state from ViewModel
    val authState by viewModel.authState.collectAsState()
    
    // Render screen based on current state
    when (val state = authState) {
        is AuthState.EmailInput -> {
            LoginScreen(
                state = state,
                onEmailChange = viewModel::updateEmail,
                onSendOtp = viewModel::sendOtp
            )
        }
        
        is AuthState.OtpEntry -> {
            OtpScreen(
                state = state,
                onVerifyOtp = viewModel::verifyOtp,
                onResendOtp = viewModel::resendOtp,
                onBack = viewModel::logout // Reuse logout to go back
            )
        }
        
        is AuthState.Session -> {
            SessionScreen(
                state = state,
                onLogout = viewModel::logout
            )
        }
    }
}
