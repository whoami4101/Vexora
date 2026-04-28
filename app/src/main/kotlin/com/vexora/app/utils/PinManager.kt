package com.vexora.app.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest

/**
 * Manages the protection PIN using [EncryptedSharedPreferences].
 *
 * The PIN is **never stored in plain text**; only its SHA-256 hash is persisted.
 * [EncryptedSharedPreferences] provides an additional layer of encryption on top
 * of that so the hash itself is also protected at rest.
 */
class PinManager(context: Context) {

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /** Returns `true` if a PIN has already been set. */
    fun isPinSet(): Boolean = prefs.contains(KEY_PIN_HASH)

    /**
     * Persists a new PIN (stored as its SHA-256 hex hash).
     *
     * @param pin The plain-text PIN chosen by the user.
     */
    fun savePin(pin: String) {
        prefs.edit().putString(KEY_PIN_HASH, hash(pin)).apply()
    }

    /**
     * Verifies a candidate PIN against the stored hash.
     *
     * @param pin The plain-text PIN entered by the user.
     * @return `true` if the hashes match.
     */
    fun verifyPin(pin: String): Boolean {
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        return storedHash == hash(pin)
    }

    /** Removes the stored PIN. Requires the current PIN to be verified first by the caller. */
    fun clearPin() {
        prefs.edit().remove(KEY_PIN_HASH).apply()
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private fun hash(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val PREFS_FILE_NAME = "vexora_secure_prefs"
        private const val KEY_PIN_HASH = "pin_hash"
    }
}
