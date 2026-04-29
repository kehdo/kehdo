package app.kehdo.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point.
 * Sets up Hilt, initializes analytics, crash reporting, etc.
 */
@HiltAndroidApp
class KehdoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // TODO: initialize Sentry, Mixpanel, Firebase here
        // (deferred until Phase 1 — kept minimal for the scaffold)
    }
}
