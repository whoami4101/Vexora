package com.vexora.app.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Accessibility service that prevents the user from:
 *  1. Changing the Private DNS setting in Android Settings.
 *  2. Removing the Vexora device administrator (which would allow uninstallation).
 *
 * When either of those screens is detected, the service performs a BACK navigation
 * to return the user to the previous screen.
 */
class DnsGuardService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            return
        }

        val packageName = event.packageName?.toString() ?: return
        val className = event.className?.toString() ?: ""

        // Only act on the Android Settings app
        if (!SETTINGS_PACKAGES.any { packageName.startsWith(it) }) return

        if (isPrivateDnsScreen(className, event) || isDeviceAdminRemovalScreen(event)) {
            performGlobalAction(GLOBAL_ACTION_BACK)
        }
    }

    /**
     * Detects the Private DNS settings screen.
     *
     * Android renders the Private DNS option inside the "Advanced Wi-Fi & Internet"
     * section. The fragment / activity names differ across OEMs, so we look for
     * a combination of fragment class name keywords AND the presence of a view
     * that has "private_dns" or "Private DNS" as its view ID or content description.
     */
    private fun isPrivateDnsScreen(className: String, event: AccessibilityEvent): Boolean {
        // Fragment/class name heuristics (AOSP + common OEMs)
        val dnsFragmentKeywords = listOf(
            "PrivateDns",
            "privateDns",
            "private_dns",
            "NetworkInternetAdvanced"
        )
        if (dnsFragmentKeywords.any { className.contains(it, ignoreCase = true) }) return true

        // Window title heuristic (event text / content-desc contains "Private DNS")
        val eventText = (event.text ?: emptyList()).joinToString(" ")
        val contentDesc = event.contentDescription?.toString() ?: ""
        val windowTitle = listOf(eventText, contentDesc, className)
            .any { it.contains("private dns", ignoreCase = true) }
        if (windowTitle) return true

        // Node-level search: look for a child view with "private_dns" in its ID
        val rootNode = rootInActiveWindow ?: return false
        return containsPrivateDnsNode(rootNode)
    }

    private fun containsPrivateDnsNode(node: AccessibilityNodeInfo): Boolean {
        val viewId = node.viewIdResourceName ?: ""
        val contentDesc = node.contentDescription?.toString() ?: ""
        val text = node.text?.toString() ?: ""

        if (viewId.contains("private_dns", ignoreCase = true) ||
            contentDesc.contains("private dns", ignoreCase = true) ||
            text.contains("private dns", ignoreCase = true)
        ) {
            return true
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (containsPrivateDnsNode(child)) {
                child.recycle()
                return true
            }
            child.recycle()
        }
        return false
    }

    /**
     * Detects the "Remove device administrator" confirmation screen.
     *
     * This screen is shown when a user tries to disable our device admin from
     * Settings → Security → Device admin apps.
     */
    private fun isDeviceAdminRemovalScreen(event: AccessibilityEvent): Boolean {
        val eventText = (event.text ?: emptyList()).joinToString(" ")
        val contentDesc = event.contentDescription?.toString() ?: ""

        val removalKeywords = listOf("remove device admin", "deactivate", "vexora")
        return removalKeywords.any { keyword ->
            eventText.contains(keyword, ignoreCase = true) ||
                    contentDesc.contains(keyword, ignoreCase = true)
        }
    }

    override fun onInterrupt() {
        // Service interrupted — no clean-up needed
    }

    companion object {
        private val SETTINGS_PACKAGES = listOf(
            "com.android.settings",
            "com.samsung.android.settings",
            "com.miui.securitycenter",
            "com.oneplus.settings"
        )

        /**
         * Returns true if the Vexora accessibility service is currently enabled.
         */
        fun isAccessibilityEnabled(context: Context): Boolean {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            val componentName = "${context.packageName}/${DnsGuardService::class.java.name}"
            return enabledServices.split(':').any { it.equals(componentName, ignoreCase = true) }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        // Override service info to ensure we listen to all relevant event types
        serviceInfo = serviceInfo?.also { info ->
            info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            info.packageNames = SETTINGS_PACKAGES.toTypedArray()
            info.notificationTimeout = 100
        }
    }
}
