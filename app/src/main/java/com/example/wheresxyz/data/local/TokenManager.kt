package com.example.wheresxyz.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_FILE_NAME = "wheresxyz_secure_prefs"
        private const val KEY_OAUTH_TOKEN = "oauth_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
    }

    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val sharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveToken(token: String, refreshToken: String? = null, expiresIn: Long = 3600) {
        val expiryTime = System.currentTimeMillis() + (expiresIn * 1000)
        sharedPreferences.edit().apply {
            putString(KEY_OAUTH_TOKEN, token)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putLong(KEY_TOKEN_EXPIRY, expiryTime)
            apply()
        }
    }

    fun getToken(): String? {
        return sharedPreferences.getString(KEY_OAUTH_TOKEN, null)
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }

    fun isTokenValid(): Boolean {
        val token = getToken()
        val expiry = sharedPreferences.getLong(KEY_TOKEN_EXPIRY, 0)
        return token != null && System.currentTimeMillis() < expiry
    }

    fun clearToken() {
        sharedPreferences.edit().apply {
            remove(KEY_OAUTH_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_TOKEN_EXPIRY)
            apply()
        }
    }
}
