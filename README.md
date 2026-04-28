# Vexora

**Vexora** is an Android parental-control app (written in Kotlin) that blocks adult content at the DNS level by routing all DNS queries through [AdGuard Family DNS](https://adguard-dns.io/en/public-dns.html) (`family.adguard-dns.com`).  
The app also prevents users (including children) from reverting the protection by:

* **Locking Private DNS settings** — an Accessibility Service automatically navigates away whenever the user opens the DNS settings screen.
* **PIN-protected uninstallation** — a Device Administrator guard forces PIN entry before device-admin rights can be revoked (which is required before uninstalling).
* **Boot persistence** — DNS settings are re-applied automatically after every device restart.

---

## Features

| Feature | Description |
|---|---|
| 🛡 Private DNS lock | Sets `family.adguard-dns.com` via Android's DoT API |
| 🔒 Uninstall guard | Device Admin receiver blocks uninstall without a PIN |
| 👁 Accessibility guard | Auto-navigates back from Private DNS & Admin-removal screens |
| 🔁 Boot receiver | Re-applies DNS on every reboot |
| 🔐 Secure PIN storage | SHA-256 hashed PIN encrypted with `EncryptedSharedPreferences` |
| 🏗 CI/CD pipeline | GitHub Actions: lint + unit-test + debug build on every push; signed release on tag |

---

## Requirements

| Item | Minimum |
|---|---|
| Android | 8.0 (API 26) |
| Target SDK | 35 (Android 15) |
| Android Studio | Ladybug (2024.2.1) or later |
| JDK | 17 |

---

## One-time ADB permission grant

Writing the system's Private DNS setting requires the `WRITE_SECURE_SETTINGS` permission.  
This permission cannot be requested at runtime — it must be granted once via ADB after installing the app:

```bash
# 1. Install the APK (debug or release)
adb install app/build/outputs/apk/debug/app-debug.apk

# 2. Grant the elevated permission
adb shell pm grant com.vexora.app android.permission.WRITE_SECURE_SETTINGS
```

The grant survives reboots and does **not** need to be repeated unless the app is reinstalled.

> **Security note:** `WRITE_SECURE_SETTINGS` allows the app to write only to `Settings.Global` / `Settings.Secure`. It cannot access user data or other apps.

---

## Setup wizard

After installation and the ADB grant, open **Vexora** and follow the four-step wizard:

1. **Set a protection PIN** (minimum 4 digits). You will need this to change settings or uninstall the app.
2. **Activate Device Administrator** — tap *Activate* and confirm in the system dialog.
3. **Enable Accessibility Service** — find *Vexora DNS Guard* in the list and enable it.
4. **Apply Family DNS** — tap *Apply DNS*; the system Private DNS is set to `family.adguard-dns.com`.

---

## Building from source

```bash
# Clone
git clone https://github.com/whoami4101/Vexora.git
cd Vexora

# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run lint
./gradlew lint
```

---

## CI / CD

| Trigger | Workflow | Output |
|---|---|---|
| Push / PR to `main` or `develop` | `ci.yml` | Lint + unit tests + debug APK artifact |
| Push tag `v*.*.*` | `release.yml` | Signed APK + AAB attached to a GitHub Release |

### Release signing secrets

Configure the following repository secrets in *Settings → Secrets → Actions*:

| Secret | Value |
|---|---|
| `KEYSTORE_BASE64` | `base64 < your-release.jks` |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias |
| `KEY_PASSWORD` | Key password |

---

## Architecture

```
com.vexora.app
├── VexoraApplication          — App class; creates notification channels
├── MainActivity               — Status dashboard
├── SetupActivity              — Step-by-step setup wizard
├── PinActivity                — PIN entry screen (uninstall guard + settings access)
├── BootReceiver               — Re-applies DNS on device boot
├── admin/
│   └── VexoraDeviceAdminReceiver  — Prevents uninstall without PIN
├── accessibility/
│   └── DnsGuardService            — Blocks navigation to DNS/admin screens
├── dns/
│   └── DnsManager                 — Writes Private DNS settings
└── utils/
    └── PinManager                 — Secure PIN storage & verification
```

---

## Licence

MIT — see [LICENSE](LICENSE).
