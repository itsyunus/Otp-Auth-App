package com.lokal.otpauth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lokal.otpauth.ui.theme.SuccessColor
import com.lokal.otpauth.viewmodel.AuthState
import java.text.SimpleDateFormat
import java.util.*

/**
 * Session screen composable displayed after successful login.
 *
 * Features:
 * - Welcome message with email
 * - Session start time display
 * - Live session duration timer (mm:ss format)
 * - Logout button
 *
 * The timer:
 * - Survives recompositions (managed by ViewModel)
 * - Stops correctly on logout
 *
 * @param state The current Session state
 * @param onLogout Callback when Logout button is clicked
 */
@Composable
fun SessionScreen(
    state: AuthState.Session,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Success indicator
        Card(
            colors = CardDefaults.cardColors(
                containerColor = SuccessColor.copy(alpha = 0.1f)
            ),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "✅ Logged In Successfully",
                style = MaterialTheme.typography.titleMedium,
                color = SuccessColor,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Welcome message
        Text(
            text = "Welcome!",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = state.email,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Session info cards
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Session start time
                Text(
                    text = "Session Started",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = formatDateTime(state.sessionStartTime),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Divider()
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Session duration
                Text(
                    text = "Session Duration",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Large duration display
                Text(
                    text = formatDuration(state.sessionDurationSeconds),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "mm:ss",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Logout button
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(
                text = "Logout",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * Format timestamp as readable date/time.
 */
private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm:ss a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

/**
 * Format duration in seconds as mm:ss.
 */
private fun formatDuration(totalSeconds: Long): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
