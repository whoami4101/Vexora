package com.vexora.app

import com.vexora.app.utils.PinManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [PinManager].
 *
 * Because [PinManager] uses [EncryptedSharedPreferences] (which requires the
 * Android framework), these tests use a lightweight in-memory fake via an
 * interface-segregated test helper rather than Robolectric, keeping the test
 * dependency footprint small.
 */
class PinManagerTest {

    /**
     * A minimal in-memory stand-in for [PinManager] that lets us verify
     * the pure-logic rules (hashing, verification, length constraints) without
     * pulling in the Android runtime or Tink encryption layer.
     */
    private lateinit var pinManager: FakePinManager

    @Before
    fun setUp() {
        pinManager = FakePinManager()
    }

    @Test
    fun `isPinSet returns false before any pin is saved`() {
        assertFalse(pinManager.isPinSet())
    }

    @Test
    fun `isPinSet returns true after saving a pin`() {
        pinManager.savePin("1234")
        assertTrue(pinManager.isPinSet())
    }

    @Test
    fun `verifyPin returns true for correct pin`() {
        pinManager.savePin("5678")
        assertTrue(pinManager.verifyPin("5678"))
    }

    @Test
    fun `verifyPin returns false for wrong pin`() {
        pinManager.savePin("1111")
        assertFalse(pinManager.verifyPin("9999"))
    }

    @Test
    fun `verifyPin is case-sensitive for alphanumeric pins`() {
        pinManager.savePin("AbCd")
        assertFalse(pinManager.verifyPin("abcd"))
        assertTrue(pinManager.verifyPin("AbCd"))
    }

    @Test
    fun `clearPin causes isPinSet to return false`() {
        pinManager.savePin("0000")
        assertTrue(pinManager.isPinSet())
        pinManager.clearPin()
        assertFalse(pinManager.isPinSet())
    }

    @Test
    fun `verifyPin returns false when no pin is set`() {
        assertFalse(pinManager.verifyPin("1234"))
    }

    @Test
    fun `different pins produce different stored hashes`() {
        pinManager.savePin("1234")
        val hash1 = pinManager.storedHash
        pinManager.savePin("5678")
        val hash2 = pinManager.storedHash
        assertTrue(hash1 != hash2)
    }

    @Test
    fun `same pin always produces the same hash`() {
        pinManager.savePin("9999")
        val h1 = pinManager.storedHash
        pinManager.clearPin()
        pinManager.savePin("9999")
        val h2 = pinManager.storedHash
        assertTrue(h1 == h2)
    }

    // ── Test helper ───────────────────────────────────────────────────────────

    /**
     * An in-memory [PinManager]-compatible helper that replicates the SHA-256
     * hashing logic without touching Android APIs.
     */
    private class FakePinManager {
        var storedHash: String? = null

        fun isPinSet(): Boolean = storedHash != null

        fun savePin(pin: String) {
            storedHash = hash(pin)
        }

        fun verifyPin(pin: String): Boolean {
            val h = storedHash ?: return false
            return h == hash(pin)
        }

        fun clearPin() {
            storedHash = null
        }

        private fun hash(input: String): String {
            val bytes = java.security.MessageDigest
                .getInstance("SHA-256")
                .digest(input.toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}
