# OTP Auth - Android Passwordless Authentication

A passwordless authentication flow using Email + OTP with session tracking, built with Kotlin and Jetpack Compose.

## 📱 Features

- **Email + OTP Login**: Enter email, receive a 6-digit OTP, verify to login
- **OTP Management**: 60-second expiry, 3 attempts max, resend capability
- **Session Tracking**: Live session duration timer (mm:ss format)
- **Timber Logging**: All authentication events logged for debugging

## 🏗️ Architecture

### Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM (ViewModel + StateFlow)
- **Async**: Kotlin Coroutines
- **Logging**: Timber

### Project Structure
```
app/src/main/java/com/lokal/otpauth/
├── MainActivity.kt              # Single Activity with Compose
├── OtpAuthApplication.kt        # Application class (Timber init)
├── analytics/
│   └── AnalyticsLogger.kt       # Timber-based event logging
├── data/
│   ├── OtpData.kt               # OTP data class
│   └── OtpManager.kt            # OTP generation & validation
├── ui/
│   ├── LoginScreen.kt           # Email input screen
│   ├── OtpScreen.kt             # OTP entry with timer
│   ├── SessionScreen.kt         # Session duration display
│   └── theme/                   # Material 3 theming
└── viewmodel/
    ├── AuthState.kt             # Sealed UI states
    └── AuthViewModel.kt         # Business logic
```

## 🔐 OTP Logic & Expiry Handling

### OTP Generation
- **Length**: 6 digits (0-9)
- **Generation**: `kotlin.random.Random.nextInt(0, 10)` for each digit
- **Expiry**: 60 seconds from generation time

### OTP Validation Rules
1. **Expiry Check**: OTP must be validated within 60 seconds
2. **Attempt Limit**: Maximum 3 incorrect attempts per OTP
3. **Invalidation**: Generating a new OTP invalidates the previous one and resets attempts

### Code Implementation
```kotlin
// OtpManager.kt - Core validation logic
fun validateOtp(email: String, inputOtp: String): OtpResult {
    val otpData = otpStore[email] ?: return OtpResult.NoOtpFound
    
    if (otpData.isMaxAttemptsExceeded()) return OtpResult.MaxAttemptsExceeded
    if (otpData.isExpired()) return OtpResult.ExpiredOtp
    
    return if (otpData.otp == inputOtp) {
        otpStore.remove(email)  // Clear on success
        OtpResult.Success
    } else {
        // Increment attempts
        otpStore[email] = otpData.copy(attemptCount = otpData.attemptCount + 1)
        OtpResult.InvalidOtp
    }
}
```

## 📊 Data Structures

| Structure | Usage | Rationale |
|-----------|-------|-----------|
| `Map<String, OtpData>` | Store OTP per email | O(1) lookup by email, thread-safe with `@Synchronized`, handles multiple users |
| `Sealed Interface (AuthState)` | UI state management | Type-safe exhaustive `when` checks, each state contains only relevant data |
| `StateFlow<AuthState>` | Observable state | Lifecycle-aware, survives config changes, one-way data flow |
| `Long` for timestamps | Expiry tracking | Precise millisecond comparison using `System.currentTimeMillis()` |
| `OtpResult` sealed interface | Validation results | Type-safe error handling without exceptions |

### Why Map for OTP Storage?
- **O(1) Lookup**: Email-based key lookup is instant
- **Easy Invalidation**: `put()` automatically replaces existing entries
- **Multi-user Support**: Each email has independent OTP data
- **Thread Safety**: `@Synchronized` methods prevent race conditions

## 🔧 External SDK: Timber

### Why Timber?
1. **Lightweight**: No network permissions or backend setup required
2. **Debug-only**: Automatically disabled in release builds
3. **Easy Verification**: Events visible immediately in Logcat
4. **No Dependencies**: Works offline, perfect for local-only demo

### Initialization
```kotlin
// OtpAuthApplication.kt
class OtpAuthApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
```

### Logged Events
All events use the `OtpAuth` tag for easy filtering:

| Event | When | Log Level |
|-------|------|-----------|
| OTP Generated | `sendOtp()` called | INFO |
| OTP Validation Success | Correct OTP entered | INFO |
| OTP Validation Failure | Wrong/expired OTP | WARN |
| User Logged Out | Logout button pressed | INFO |

### Viewing Logs
```bash
adb logcat -s OtpAuth
```
Or in Android Studio Logcat, filter by tag: `OtpAuth`

## 🤖 AI Assistance Disclosure

### Used AI (GPT/Docs) For:
- Initial project structure scaffolding
- Gradle dependencies version compatibility
- Material 3 component syntax reference

### Implemented & Understood Myself:
- OTP generation and validation logic
- Map-based storage design decision
- Sealed interface state management pattern
- StateFlow and coroutine timer implementation
- Compose UI layout and state hoisting
- Error handling edge cases

## 🚀 Setup Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34

### Build & Run
```bash
# Clone the repository
git clone <repository-url>
cd Lokal-app

# Build debug APK
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug
```

Or open in Android Studio and click "Run".

## ✅ Edge Cases Handled

| Edge Case | Handling |
|-----------|----------|
| Expired OTP | Shows "OTP expired" error, enables Resend button |
| Incorrect OTP | Shows remaining attempts, disables after 3 failures |
| Max attempts exceeded | Requires resending new OTP |
| Resend OTP | Invalidates old OTP, resets timer and attempts |
| Screen rotation | State preserved via ViewModel + rememberSaveable |
| Empty email | Validation prevents sending OTP |
| Invalid email format | Shows validation error |

## 📹 Demo Video

[Link to demo video will be added here]

## 📝 License

This project was created as an assignment submission.
