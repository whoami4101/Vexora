# Add project specific ProGuard rules here.

# Keep Device Admin Receiver
-keep class com.vexora.app.admin.VexoraDeviceAdminReceiver { *; }

# Keep Accessibility Service
-keep class com.vexora.app.accessibility.DnsGuardService { *; }

# Keep application classes
-keep class com.vexora.app.** { *; }

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
