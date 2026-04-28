package com.vexora.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.vexora.app.dns.DnsManager

/**
 * Re-applies the Private DNS setting after device reboot.
 *
 * Android resets certain Settings.Global keys after a factory reset, so we
 * re-apply them whenever the device boots (or the direct-boot phase completes).
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.LOCKED_BOOT_COMPLETED"
        ) {
            DnsManager(context).applyPrivateDns()
        }
    }
}
