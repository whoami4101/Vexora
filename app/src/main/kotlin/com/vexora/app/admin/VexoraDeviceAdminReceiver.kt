package com.vexora.app.admin

import android.app.Activity
import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import com.vexora.app.PinActivity
import com.vexora.app.R

/**
 * Device Administrator receiver.
 *
 * When the user tries to remove administrator rights (which would allow them to
 * uninstall the app), [onDisableRequested] launches the PIN-verification screen.
 * The system also shows a warning message returned from this callback.
 */
class VexoraDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        // Device admin enabled — no extra action needed
    }

    /**
     * Called when the user requests to remove this device administrator.
     * We start the PIN activity as a foreground task so the user must verify
     * before the admin can be removed (and the app subsequently uninstalled).
     *
     * @return A warning message displayed in the system's "Remove device admin" dialog.
     */
    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        // Launch PIN screen as a new task so it appears on top of the Settings UI
        val pinIntent = PinActivity.createIntent(
            context,
            context.getString(R.string.pin_reason_uninstall)
        ).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
        }
        context.startActivity(pinIntent)

        return context.getString(R.string.device_admin_disable_warning)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        // Admin has been removed — protection is now off
    }

    companion object {
        /**
         * Builds the intent used to request device-admin rights.
         */
        fun buildEnableIntent(context: Context): Intent =
            Intent(android.app.admin.DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(
                    android.app.admin.DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                    android.content.ComponentName(context, VexoraDeviceAdminReceiver::class.java)
                )
                putExtra(
                    android.app.admin.DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    context.getString(R.string.device_admin_request_reason)
                )
            }
    }
}
