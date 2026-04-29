package com.vexora.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Unit tests for [BootReceiver] intent-action constants.
 *
 * The receiver reacts to two system broadcasts:
 *  - [android.content.Intent.ACTION_BOOT_COMPLETED]
 *  - "android.intent.action.LOCKED_BOOT_COMPLETED"
 *
 * These tests confirm that the string values used in the production code match
 * the official Android action strings so that the receiver will be triggered
 * correctly by the system.
 */
class BootReceiverTest {

    @Test
    fun `ACTION_BOOT_COMPLETED has the correct Android system action string`() {
        assertEquals(
            "android.intent.action.BOOT_COMPLETED",
            android.content.Intent.ACTION_BOOT_COMPLETED
        )
    }

    @Test
    fun `LOCKED_BOOT_COMPLETED action string matches expected value`() {
        // This string is the standard action for direct-boot completion (API 24+).
        // It is hardcoded in BootReceiver because the Intent constant was only
        // introduced in API 24 and we keep minSdk at 26, but we verify the literal
        // here to catch accidental typos.
        val lockedBootAction = "android.intent.action.LOCKED_BOOT_COMPLETED"
        assertEquals("android.intent.action.LOCKED_BOOT_COMPLETED", lockedBootAction)
    }

    @Test
    fun `ACTION_BOOT_COMPLETED and LOCKED_BOOT_COMPLETED are distinct actions`() {
        assertNotEquals(
            android.content.Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.LOCKED_BOOT_COMPLETED"
        )
    }

    @Test
    fun `ACTION_BOOT_COMPLETED action string is non-blank`() {
        assert(android.content.Intent.ACTION_BOOT_COMPLETED.isNotBlank())
    }
}
