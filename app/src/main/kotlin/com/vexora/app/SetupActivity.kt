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
import com.vexora.app.databinding.ActivitySetupBinding
import com.vexora.app.dns.DnsManager
import com.vexora.app.utils.PinManager

/**
 * Step-by-step setup wizard:
 *  Step 1 – Set a protection PIN
 *  Step 2 – Activate Device Administrator (uninstall protection)
 *  Step 3 – Enable Accessibility Service (DNS guard)
 *  Step 4 – Apply Private DNS (family.adguard-dns.com)
 */
class SetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupBinding
    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var adminComponent: ComponentName
    private lateinit var pinManager: PinManager
    private lateinit var dnsManager: DnsManager

    private var currentStep = STEP_PIN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        devicePolicyManager = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(this, VexoraDeviceAdminReceiver::class.java)
        pinManager = PinManager(this)
        dnsManager = DnsManager(this)

        currentStep = determineCurrentStep()
        showStep(currentStep)

        binding.btnNext.setOnClickListener { handleNextStep() }
        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    override fun onResume() {
        super.onResume()
        // Re-evaluate step in case user came back from Settings
        currentStep = determineCurrentStep()
        showStep(currentStep)
    }

    private fun determineCurrentStep(): Int {
        if (!pinManager.isPinSet()) return STEP_PIN
        if (!devicePolicyManager.isAdminActive(adminComponent)) return STEP_ADMIN
        if (!DnsGuardService.isAccessibilityEnabled(this)) return STEP_ACCESSIBILITY
        if (!dnsManager.isPrivateDnsConfigured()) return STEP_DNS
        return STEP_DONE
    }

    private fun showStep(step: Int) {
        binding.progressIndicator.progress = ((step.toFloat() / STEP_DONE) * 100).toInt()

        when (step) {
            STEP_PIN -> {
                binding.tvStepTitle.setText(R.string.setup_step1_title)
                binding.tvStepDescription.setText(R.string.setup_step1_desc)
                binding.layoutPinEntry.visibility = View.VISIBLE
                binding.btnNext.setText(R.string.btn_set_pin)
            }
            STEP_ADMIN -> {
                binding.layoutPinEntry.visibility = View.GONE
                binding.tvStepTitle.setText(R.string.setup_step2_title)
                binding.tvStepDescription.setText(R.string.setup_step2_desc)
                binding.btnNext.setText(R.string.btn_activate)
            }
            STEP_ACCESSIBILITY -> {
                binding.tvStepTitle.setText(R.string.setup_step3_title)
                binding.tvStepDescription.setText(R.string.setup_step3_desc)
                binding.btnNext.setText(R.string.btn_open_settings)
            }
            STEP_DNS -> {
                binding.tvStepTitle.setText(R.string.setup_step4_title)
                binding.tvStepDescription.setText(R.string.setup_step4_desc)
                binding.btnNext.setText(R.string.btn_apply_dns)
            }
            STEP_DONE -> {
                binding.tvStepTitle.setText(R.string.setup_done_title)
                binding.tvStepDescription.setText(R.string.setup_done_desc)
                binding.btnNext.setText(R.string.btn_finish)
                binding.layoutPinEntry.visibility = View.GONE
            }
        }
    }

    private fun handleNextStep() {
        when (currentStep) {
            STEP_PIN -> handlePinStep()
            STEP_ADMIN -> requestDeviceAdmin()
            STEP_ACCESSIBILITY -> openAccessibilitySettings()
            STEP_DNS -> applyDns()
            STEP_DONE -> {
                startActivity(Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                })
                finish()
            }
        }
    }

    private fun handlePinStep() {
        val pin = binding.etPin.text?.toString() ?: ""
        val pinConfirm = binding.etPinConfirm.text?.toString() ?: ""

        if (pin.length < PIN_MIN_LENGTH) {
            binding.etPin.error = getString(R.string.error_pin_too_short, PIN_MIN_LENGTH)
            return
        }
        if (pin != pinConfirm) {
            binding.etPinConfirm.error = getString(R.string.error_pin_mismatch)
            return
        }

        pinManager.savePin(pin)
        Toast.makeText(this, R.string.toast_pin_saved, Toast.LENGTH_SHORT).show()
        currentStep = STEP_ADMIN
        showStep(currentStep)
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
            currentStep = STEP_DONE
            showStep(currentStep)
        } else {
            Toast.makeText(this, R.string.toast_dns_failed, Toast.LENGTH_LONG).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADMIN) {
            currentStep = determineCurrentStep()
            showStep(currentStep)
        }
    }

    companion object {
        private const val STEP_PIN = 1
        private const val STEP_ADMIN = 2
        private const val STEP_ACCESSIBILITY = 3
        private const val STEP_DNS = 4
        private const val STEP_DONE = 5
        private const val REQUEST_CODE_ADMIN = 1001
        private const val PIN_MIN_LENGTH = 4
    }
}
