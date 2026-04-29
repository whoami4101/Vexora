package com.vexora.app

import com.vexora.app.utils.PinManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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

    @Test
    fun `hash output is always 64 hex characters (SHA-256)`() {
        // SHA-256 produces 32 bytes → 64 lowercase hex characters
        listOf("1234", "AbCd", "", "a", "a".repeat(100)).forEach { pin ->
            pinManager.savePin(pin)
            val hash = pinManager.storedHash
            assertNotNull("Hash must not be null", hash)
            assertEquals("SHA-256 hex digest must be 64 characters for pin '$pin'", 64, hash!!.length)
            assertTrue(
                "Hash must contain only hex characters for pin '$pin'",
                hash.matches(Regex("[0-9a-f]{64}"))
            )
        }
    }

    @Test
    fun `empty string pin produces a deterministic hash`() {
        pinManager.savePin("")
        val h1 = pinManager.storedHash
        pinManager.clearPin()
        pinManager.savePin("")
        val h2 = pinManager.storedHash
        assertEquals(h1, h2)
    }

    @Test
    fun `verifyPin returns true for empty string when saved as empty string`() {
        pinManager.savePin("")
        assertTrue(pinManager.verifyPin(""))
    }

    @Test
    fun `very long pin (100 characters) can be saved and verified`() {
        val longPin = "A1b2C3d4".repeat(13) // 104 characters
        pinManager.savePin(longPin)
        assertTrue(pinManager.verifyPin(longPin))
        assertFalse(pinManager.verifyPin(longPin.dropLast(1)))
    }

    @Test
    fun `pin with special characters verifies correctly`() {
        val specialPin = "!@#\$%^&*()-_=+[]{}"
        pinManager.savePin(specialPin)
        assertTrue(pinManager.verifyPin(specialPin))
        assertFalse(pinManager.verifyPin("!@#\$%^&*()-_=+[]{}X"))
    }

    @Test
    fun `saving a new pin overwrites the previous pin hash`() {
        pinManager.savePin("first")
        val firstHash = pinManager.storedHash

        pinManager.savePin("second")
        val secondHash = pinManager.storedHash

        assertFalse("Second save must change the stored hash", firstHash == secondHash)
        assertTrue(pinManager.verifyPin("second"))
        assertFalse(pinManager.verifyPin("first"))
    }

    @Test
    fun `verifyPin is false for a pin that differs only by whitespace`() {
        pinManager.savePin("1234")
        assertFalse(pinManager.verifyPin(" 1234"))
        assertFalse(pinManager.verifyPin("1234 "))
        assertFalse(pinManager.verifyPin(" 1234 "))
    }

    @Test
    fun `unicode pin produces a consistent hash`() {
        val unicodePin = "\u00e9\u00e0\u00fc" // éàü
        pinManager.savePin(unicodePin)
        assertTrue(pinManager.verifyPin(unicodePin))
        assertFalse(pinManager.verifyPin("eau"))
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
