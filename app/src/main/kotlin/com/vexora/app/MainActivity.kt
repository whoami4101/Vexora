package com.vexora.app

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vexora.app.accessibility.DnsGuardService
import com.vexora.app.admin.VexoraDeviceAdminReceiver
import com.vexora.app.databinding.ActivityMainBinding
import com.vexora.app.dns.DnsManager
import com.vexora.app.utils.PinManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var adminComponent: ComponentName
    private lateinit var pinManager: PinManager
    private lateinit var dnsManager: DnsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        devicePolicyManager = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(this, VexoraDeviceAdminReceiver::class.java)
        pinManager = PinManager(this)
        dnsManager = DnsManager(this)

        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        updateStatusDisplay()
    }

    private fun setupClickListeners() {
        binding.btnSetup.setOnClickListener {
            startActivity(Intent(this, SetupActivity::class.java))
        }

        binding.btnAdminEnable.setOnClickListener {
            requestDeviceAdmin()
        }

        binding.btnAccessibility.setOnClickListener {
            openAccessibilitySettings()
        }

        binding.btnApplyDns.setOnClickListener {
            applyDns()
        }
    }

    private fun updateStatusDisplay() {
        val isPinSet = pinManager.isPinSet()
        val isAdminActive = devicePolicyManager.isAdminActive(adminComponent)
        val isAccessibilityEnabled = DnsGuardService.isAccessibilityEnabled(this)
        val isDnsApplied = dnsManager.isPrivateDnsConfigured()

        binding.statusPin.apply {
            text = if (isPinSet) getString(R.string.status_pin_set) else getString(R.string.status_pin_not_set)
            setTextColor(
                if (isPinSet) getColor(R.color.status_ok) else getColor(R.color.status_warn)
            )
        }

        binding.statusAdmin.apply {
            text = if (isAdminActive) getString(R.string.status_admin_active) else getString(R.string.status_admin_inactive)
            setTextColor(
                if (isAdminActive) getColor(R.color.status_ok) else getColor(R.color.status_warn)
            )
        }

        binding.statusAccessibility.apply {
            text = if (isAccessibilityEnabled) getString(R.string.status_accessibility_on) else getString(R.string.status_accessibility_off)
            setTextColor(
                if (isAccessibilityEnabled) getColor(R.color.status_ok) else getColor(R.color.status_warn)
            )
        }

        binding.statusDns.apply {
            text = if (isDnsApplied) getString(R.string.status_dns_applied) else getString(R.string.status_dns_not_applied)
            setTextColor(
                if (isDnsApplied) getColor(R.color.status_ok) else getColor(R.color.status_warn)
            )
        }

        // Show setup button if not fully configured, otherwise hide action buttons
        val allConfigured = isPinSet && isAdminActive && isAccessibilityEnabled && isDnsApplied
        binding.btnSetup.visibility = if (!allConfigured) View.VISIBLE else View.GONE
        binding.groupIndividualActions.visibility = if (!allConfigured) View.VISIBLE else View.GONE
        binding.tvFullyProtected.visibility = if (allConfigured) View.VISIBLE else View.GONE

        // Show/hide individual action buttons based on what's missing
        binding.btnAdminEnable.visibility = if (!isAdminActive) View.VISIBLE else View.GONE
        binding.btnAccessibility.visibility = if (!isAccessibilityEnabled) View.VISIBLE else View.GONE
        binding.btnApplyDns.visibility = if (!isDnsApplied) View.VISIBLE else View.GONE
    }

    private fun requestDeviceAdmin() {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                getString(R.string.device_admin_request_reason)
            )
        }
        @Suppress("DEPRECATION")
        startActivityForResult(intent, REQUEST_CODE_ADMIN)
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
        Toast.makeText(this, R.string.toast_enable_accessibility, Toast.LENGTH_LONG).show()
    }

    private fun applyDns() {
        val result = dnsManager.applyPrivateDns()
        if (result) {
            Toast.makeText(this, R.string.toast_dns_applied, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, R.string.toast_dns_failed, Toast.LENGTH_LONG).show()
        }
        updateStatusDisplay()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADMIN) {
            updateStatusDisplay()
        }
    }

    companion object {
        private const val REQUEST_CODE_ADMIN = 1001
    }
}
