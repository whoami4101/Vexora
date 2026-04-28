package com.vexora.app.dns

import android.content.Context
import android.provider.Settings
import android.util.Log

/**
 * Manages Private DNS configuration on the device.
 *
 * Private DNS (DNS-over-TLS) is controlled via [Settings.Global]:
 *   - PRIVATE_DNS_MODE     → "hostname" (use a specific provider)
 *   - PRIVATE_DNS_SPECIFIER → "family.adguard-dns.com"
 *
 * Writing these keys requires the WRITE_SECURE_SETTINGS permission.
 * That permission is not grantable at install-time; it must be granted once
 * via ADB:
 *
 *   adb shell pm grant com.vexora.app android.permission.WRITE_SECURE_SETTINGS
 *
 * After that single ADB command the app retains the permission permanently
 * (it is a signature-level permission that survives reboots when granted by ADB).
 */
class DnsManager(private val context: Context) {

    /**
     * Applies the AdGuard Family DNS-over-TLS preset to the system.
     *
     * @return `true` if the setting was written successfully, `false` otherwise.
     */
    fun applyPrivateDns(): Boolean {
        return try {
            Settings.Global.putString(
                context.contentResolver,
                PRIVATE_DNS_MODE_KEY,
                MODE_HOSTNAME
            )
            Settings.Global.putString(
                context.contentResolver,
                PRIVATE_DNS_SPECIFIER_KEY,
                DNS_PROVIDER
            )
            Log.i(TAG, "Private DNS set to $DNS_PROVIDER")
            true
        } catch (e: SecurityException) {
            Log.e(TAG, "WRITE_SECURE_SETTINGS permission missing: ${e.message}")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set Private DNS: ${e.message}")
            false
        }
    }

    /**
     * Returns `true` if the system is currently configured to use our DNS provider.
     */
    fun isPrivateDnsConfigured(): Boolean {
        val mode = Settings.Global.getString(context.contentResolver, PRIVATE_DNS_MODE_KEY)
        val specifier = Settings.Global.getString(context.contentResolver, PRIVATE_DNS_SPECIFIER_KEY)
        return mode == MODE_HOSTNAME && specifier == DNS_PROVIDER
    }

    /**
     * Reverts Private DNS back to "automatic" (opportunistic DoT).
     * Used for testing / admin override.
     */
    fun resetPrivateDns(): Boolean {
        return try {
            Settings.Global.putString(
                context.contentResolver,
                PRIVATE_DNS_MODE_KEY,
                MODE_OPPORTUNISTIC
            )
            Settings.Global.putString(
                context.contentResolver,
                PRIVATE_DNS_SPECIFIER_KEY,
                ""
            )
            Log.i(TAG, "Private DNS reset to opportunistic")
            true
        } catch (e: SecurityException) {
            Log.e(TAG, "WRITE_SECURE_SETTINGS permission missing: ${e.message}")
            false
        }
    }

    companion object {
        private const val TAG = "DnsManager"

        /** Key used for Private DNS mode in [Settings.Global] */
        const val PRIVATE_DNS_MODE_KEY = "private_dns_mode"

        /** Key used for the Private DNS hostname in [Settings.Global] */
        const val PRIVATE_DNS_SPECIFIER_KEY = "private_dns_specifier"

        /** AdGuard Family DNS-over-TLS hostname */
        const val DNS_PROVIDER = "family.adguard-dns.com"

        /** Constant for hostname-based DoT mode */
        const val MODE_HOSTNAME = "hostname"

        /** Constant for opportunistic DoT mode */
        const val MODE_OPPORTUNISTIC = "opportunistic"
    }
}
