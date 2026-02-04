package com.lokal.otpauth

import android.app.Application
import timber.log.Timber

/**
 * Custom Application class for global initialization.
 *
 * Initializes Timber logging in debug builds.
 */
class OtpAuthApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        Timber.d("OtpAuthApplication initialized")
    }
}
