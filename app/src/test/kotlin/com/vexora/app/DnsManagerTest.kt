package com.vexora.app

import com.vexora.app.dns.DnsManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for constants and pure logic in [DnsManager].
 *
 * The parts that call [android.provider.Settings.Global] cannot be tested here
 * without Robolectric; those paths are covered by the instrumented tests.
 * This class focuses on the constant values and the helper that assembles them.
 */
class DnsManagerTest {

    @Test
    fun `DNS_PROVIDER constant is the correct AdGuard family hostname`() {
        assertEquals("family.adguard-dns.com", DnsManager.DNS_PROVIDER)
    }

    @Test
    fun `MODE_HOSTNAME constant is the correct value required by Android`() {
        assertEquals("hostname", DnsManager.MODE_HOSTNAME)
    }

    @Test
    fun `MODE_OPPORTUNISTIC constant is correct`() {
        assertEquals("opportunistic", DnsManager.MODE_OPPORTUNISTIC)
    }

    @Test
    fun `PRIVATE_DNS_MODE_KEY constant is the correct settings key`() {
        assertEquals("private_dns_mode", DnsManager.PRIVATE_DNS_MODE_KEY)
    }

    @Test
    fun `PRIVATE_DNS_SPECIFIER_KEY constant is the correct settings key`() {
        assertEquals("private_dns_specifier", DnsManager.PRIVATE_DNS_SPECIFIER_KEY)
    }

    @Test
    fun `DNS_PROVIDER hostname is a valid DNS-over-TLS hostname format`() {
        val hostname = DnsManager.DNS_PROVIDER
        // Must not start or end with a dot
        assertFalse(hostname.startsWith("."))
        assertFalse(hostname.endsWith("."))
        // Must contain at least one dot (subdomain.domain.tld)
        assertTrue(hostname.contains("."))
        // Must not contain spaces
        assertFalse(hostname.contains(" "))
        // Must not be blank
        assertTrue(hostname.isNotBlank())
    }

    @Test
    fun `DNS provider hostname parts are non-empty`() {
        val parts = DnsManager.DNS_PROVIDER.split(".")
        assertTrue("Expected at least 2 parts in hostname", parts.size >= 2)
        parts.forEach { part ->
            assertNotNull("Hostname part must not be null", part)
            assertTrue("Hostname part must not be empty", part.isNotEmpty())
        }
    }

    @Test
    fun `DNS provider has exactly three dot-separated labels`() {
        val labels = DnsManager.DNS_PROVIDER.split(".")
        assertEquals("Expected exactly 3 labels (subdomain.domain.tld)", 3, labels.size)
    }

    @Test
    fun `DNS provider contains only valid hostname characters`() {
        // RFC 1123: letters, digits, hyphens, dots only
        val validHostname = Regex("^[a-zA-Z0-9.-]+$")
        assertTrue(
            "DNS_PROVIDER must contain only letters, digits, hyphens and dots",
            validHostname.matches(DnsManager.DNS_PROVIDER)
        )
    }

    @Test
    fun `MODE_HOSTNAME is different from MODE_OPPORTUNISTIC`() {
        assertFalse(
            "MODE_HOSTNAME and MODE_OPPORTUNISTIC must be distinct",
            DnsManager.MODE_HOSTNAME == DnsManager.MODE_OPPORTUNISTIC
        )
    }

    @Test
    fun `PRIVATE_DNS_MODE_KEY and PRIVATE_DNS_SPECIFIER_KEY are distinct`() {
        assertFalse(
            "The two settings keys must be different",
            DnsManager.PRIVATE_DNS_MODE_KEY == DnsManager.PRIVATE_DNS_SPECIFIER_KEY
        )
    }

    @Test
    fun `all five DnsManager constants are unique`() {
        val constants = listOf(
            DnsManager.PRIVATE_DNS_MODE_KEY,
            DnsManager.PRIVATE_DNS_SPECIFIER_KEY,
            DnsManager.DNS_PROVIDER,
            DnsManager.MODE_HOSTNAME,
            DnsManager.MODE_OPPORTUNISTIC
        )
        assertEquals(
            "Every DnsManager constant must be unique",
            constants.size,
            constants.toSet().size
        )
    }

    @Test
    fun `all DnsManager constants are non-blank`() {
        listOf(
            DnsManager.PRIVATE_DNS_MODE_KEY,
            DnsManager.PRIVATE_DNS_SPECIFIER_KEY,
            DnsManager.DNS_PROVIDER,
            DnsManager.MODE_HOSTNAME,
            DnsManager.MODE_OPPORTUNISTIC
        ).forEach { constant ->
            assertTrue("DnsManager constant must not be blank: '$constant'", constant.isNotBlank())
        }
    }
}
