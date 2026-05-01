package app.kehdo.core.datastore

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hardware-backed (Keystore) encrypted preferences impl of [TokenStore].
 *
 * On first read/write the master key is created via Android Keystore;
 * subsequent calls reuse the same key. Reading/writing is synchronous —
 * fine for the rare (cold-start, refresh) calls; never on a hot path.
 */
@Singleton
class EncryptedTokenStore @Inject constructor(
    @ApplicationContext context: Context
) : TokenStore {

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun saveRefreshToken(token: String) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }

    override fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    override fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val FILE_NAME = "kehdo_secure_tokens"
        const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}
