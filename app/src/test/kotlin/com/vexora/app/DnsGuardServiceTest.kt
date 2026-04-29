package com.vexora.app

import com.vexora.app.accessibility.DnsGuardService
import com.vexora.app.dns.DnsManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [DnsGuardService] constants and pure-logic invariants.
 *
 * Methods that interact with the Android Accessibility framework cannot be tested
 * here without Robolectric. This class focuses on verifiable invariants:
 *   - The fully-qualified class name assumed by [DnsGuardService.isAccessibilityEnabled].
 *   - The format of the component-name string that is built and matched against the
 *     system's enabled-accessibility-services setting.
 *   - Structural guarantees about the DNS keys used by the ContentObserver registered
 *     in [DnsGuardService.onServiceConnected].
 */
class DnsGuardServiceTest {

    @Test
    fun `DnsGuardService fully-qualified class name is correct`() {
        assertEquals(
            "com.vexora.app.accessibility.DnsGuardService",
            DnsGuardService::class.java.name
        )
    }

    @Test
    fun `DnsGuardService simple class name is DnsGuardService`() {
        assertEquals("DnsGuardService", DnsGuardService::class.java.simpleName)
    }

    @Test
    fun `DnsGuardService class name does not contain slashes`() {
        // isAccessibilityEnabled builds the component string as
        // "${packageName}/${DnsGuardService::class.java.name}".
        // The class name portion must not include a slash.
        assertFalse(DnsGuardService::class.java.name.contains("/"))
    }

    @Test
    fun `accessibility component name format uses slash separator`() {
        val packageName = "com.vexora.app"
        val componentName = "$packageName/${DnsGuardService::class.java.name}"
        assertTrue(
            "Component name must contain exactly one slash",
            componentName.count { it == '/' } == 1
        )
    }

    @Test
    fun `accessibility component name starts with the app package`() {
        val packageName = "com.vexora.app"
        val componentName = "$packageName/${DnsGuardService::class.java.name}"
        assertTrue(componentName.startsWith("com.vexora.app/"))
    }

    @Test
    fun `accessibility component name ends with the service class name`() {
        val packageName = "com.vexora.app"
        val componentName = "$packageName/${DnsGuardService::class.java.name}"
        assertTrue(componentName.endsWith("DnsGuardService"))
    }

    // ── ContentObserver / dynamic DNS-change keys ─────────────────────────────

    @Test
    fun `PRIVATE_DNS_MODE_KEY used by ContentObserver is non-blank`() {
        // DnsGuardService registers a ContentObserver on Settings.Global.getUriFor
        // using DnsManager.PRIVATE_DNS_MODE_KEY. The key must be a valid, non-blank string.
        assertTrue(DnsManager.PRIVATE_DNS_MODE_KEY.isNotBlank())
    }

    @Test
    fun `PRIVATE_DNS_SPECIFIER_KEY used by ContentObserver is non-blank`() {
        assertTrue(DnsManager.PRIVATE_DNS_SPECIFIER_KEY.isNotBlank())
    }

    @Test
    fun `ContentObserver watches two distinct settings keys`() {
        // The service registers an observer on both keys independently.
        // They must be different strings so distinct URIs are produced.
        assertFalse(
            DnsManager.PRIVATE_DNS_MODE_KEY == DnsManager.PRIVATE_DNS_SPECIFIER_KEY
        )
    }

    @Test
    fun `DNS keys used by ContentObserver contain no whitespace`() {
        assertFalse(DnsManager.PRIVATE_DNS_MODE_KEY.contains(Regex("\\s")))
        assertFalse(DnsManager.PRIVATE_DNS_SPECIFIER_KEY.contains(Regex("\\s")))
    }
}
