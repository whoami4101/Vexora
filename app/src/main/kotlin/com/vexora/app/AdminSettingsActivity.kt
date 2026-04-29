package com.vexora.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vexora.app.databinding.ActivityAdminSettingsBinding
import com.vexora.app.dns.DnsManager
import com.vexora.app.utils.PinManager

/**
 * PIN-protected administration screen.
 *
 * Only reachable after the user has successfully verified their PIN in
 * [PinActivity]. Provides:
 *  - Change the protection PIN
 *  - Re-apply the AdGuard Family DNS setting
 */
class AdminSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminSettingsBinding
    private lateinit var pinManager: PinManager
    private lateinit var dnsManager: DnsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pinManager = PinManager(this)
        dnsManager = DnsManager(this)

        binding.btnSavePin.setOnClickListener { handleChangePin() }
        binding.btnReApplyDns.setOnClickListener { handleReApplyDns() }
    }

    private fun handleChangePin() {
        val newPin = binding.etNewPin.text?.toString() ?: ""
        val confirmPin = binding.etNewPinConfirm.text?.toString() ?: ""

        if (newPin.length < PIN_MIN_LENGTH) {
            binding.etNewPin.error = getString(R.string.error_pin_too_short, PIN_MIN_LENGTH)
            return
        }
        if (newPin != confirmPin) {
            binding.etNewPinConfirm.error = getString(R.string.error_pin_mismatch)
            return
        }

        pinManager.savePin(newPin)
        binding.etNewPin.text?.clear()
        binding.etNewPinConfirm.text?.clear()
        Toast.makeText(this, R.string.toast_pin_changed, Toast.LENGTH_SHORT).show()
    }

    private fun handleReApplyDns() {
        val success = dnsManager.applyPrivateDns()
        if (success) {
            Toast.makeText(this, R.string.toast_dns_reapplied, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, R.string.toast_dns_failed, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val PIN_MIN_LENGTH = 4
    }
}
