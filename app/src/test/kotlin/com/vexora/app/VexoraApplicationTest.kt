package com.vexora.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [VexoraApplication] constants.
 *
 * The Application class creates a notification channel at startup. These tests
 * verify that the channel-ID constant has the expected value so that any
 * notification posted against it will use the correct channel.
 */
class VexoraApplicationTest {

    @Test
    fun `CHANNEL_ID_PROTECTION has the expected string value`() {
        assertEquals("vexora_protection", VexoraApplication.CHANNEL_ID_PROTECTION)
    }

    @Test
    fun `CHANNEL_ID_PROTECTION is non-blank`() {
        assertTrue(VexoraApplication.CHANNEL_ID_PROTECTION.isNotBlank())
    }

    @Test
    fun `CHANNEL_ID_PROTECTION contains no whitespace`() {
        assertFalse(
            "Notification channel IDs must not contain whitespace",
            VexoraApplication.CHANNEL_ID_PROTECTION.contains(Regex("\\s"))
        )
    }

    @Test
    fun `CHANNEL_ID_PROTECTION uses only safe identifier characters`() {
        // Android notification channel IDs should be stable identifiers.
        // Only lowercase letters, digits and underscores are considered safe.
        assertTrue(
            "CHANNEL_ID_PROTECTION must match [a-z0-9_]+",
            VexoraApplication.CHANNEL_ID_PROTECTION.matches(Regex("[a-z0-9_]+"))
        )
    }
}
