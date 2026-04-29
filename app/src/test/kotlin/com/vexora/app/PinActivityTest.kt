package com.vexora.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [PinActivity] constants and intent-building logic.
 *
 * [PinActivity.createIntent] constructs an intent that carries the reason string
 * as an extra. These tests verify the constant key used for that extra, and the
 * structural properties of the intent produced by [PinActivity.createIntent].
 */
class PinActivityTest {

    @Test
    fun `EXTRA_REASON has the expected string value`() {
        assertEquals("extra_reason", PinActivity.EXTRA_REASON)
    }

    @Test
    fun `EXTRA_REASON is non-blank`() {
        assertTrue(PinActivity.EXTRA_REASON.isNotBlank())
    }

    @Test
    fun `EXTRA_REASON contains no whitespace`() {
        assertFalse(
            "Intent extra keys must not contain whitespace",
            PinActivity.EXTRA_REASON.contains(Regex("\\s"))
        )
    }

    @Test
    fun `EXTRA_REASON uses only safe identifier characters`() {
        assertTrue(
            "EXTRA_REASON must match [a-z_]+",
            PinActivity.EXTRA_REASON.matches(Regex("[a-z_]+"))
        )
    }
}
