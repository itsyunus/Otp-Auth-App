package com.lokal.otpauth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lokal.otpauth.viewmodel.AuthState

/**
 * Login screen composable for email input.
 *
 * Features:
 * - Email text field with validation
 * - Send OTP button
 * - Error message display
 * - State preserved across recompositions using rememberSaveable
 *
 * @param state The current EmailInput state
 * @param onEmailChange Callback when email text changes
 * @param onSendOtp Callback when Send OTP button is clicked
 */
@Composable
fun LoginScreen(
    state: AuthState.EmailInput,
    onEmailChange: (String) -> Unit,
    onSendOtp: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Use rememberSaveable to preserve email across configuration changes
    var emailInput by rememberSaveable { mutableStateOf(state.email) }
    
    // Sync with ViewModel state
    LaunchedEffect(state.email) {
        if (state.email != emailInput) {
            emailInput = state.email
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        Text(
            text = "Welcome",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Sign in with your email",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Email input field
        OutlinedTextField(
            value = emailInput,
            onValueChange = { 
                emailInput = it
                onEmailChange(it)
            },
            label = { Text("Email Address") },
            placeholder = { Text("Enter your email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onSendOtp(emailInput) }
            ),
            isError = state.error != null,
            supportingText = if (state.error != null) {
                { Text(state.error, color = MaterialTheme.colorScheme.error) }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Send OTP button
        Button(
            onClick = { onSendOtp(emailInput) },
            enabled = emailInput.isNotBlank() && !state.isLoading,
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
                    text = "Send OTP",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Info text
        Text(
            text = "We'll send a 6-digit verification code to your email",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}
