package app.kehdo.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.sentry.Sentry
import io.sentry.android.core.SentryAndroid

/**
 * Application entry point. Hilt boots first, then we wire up Sentry so any
 * crash before we hit MainActivity still reports.
 *
 * Mixpanel is intentionally deferred to a follow-up PR — it needs a valid
 * project token before init or it logs a warning every event.
 */
@HiltAndroidApp
class KehdoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initSentry()
    }

    private fun initSentry() {
        // SentryAndroid reads `io.sentry.dsn` from the manifest meta-data.
        // Debug builds ship with an empty DSN (manifestPlaceholder = ""),
        // which makes init a no-op rather than a crash — so this call is
        // safe in every build variant.
        runCatching {
            SentryAndroid.init(this) { options ->
                options.environment = BuildConfig.BUILD_TYPE
                options.tracesSampleRate = if (BuildConfig.DEBUG) 1.0 else 0.1
                options.isEnableAutoSessionTracking = true
            }
        }.onFailure { Sentry.captureException(it) }
    }
}
