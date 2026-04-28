package com.vexora.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vexora.app.databinding.ActivityPinBinding
import com.vexora.app.utils.PinManager

/**
 * PIN verification screen.
 *
 * Callers must pass [EXTRA_REASON] so the screen shows a meaningful title.
 * On success the activity finishes with [RESULT_OK]; on cancel [RESULT_CANCELED].
 */
class PinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPinBinding
    private lateinit var pinManager: PinManager

    private var failedAttempts = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pinManager = PinManager(this)

        val reason = intent.getStringExtra(EXTRA_REASON) ?: getString(R.string.pin_reason_default)
        binding.tvPinTitle.text = reason

        binding.btnConfirmPin.setOnClickListener { verifyPin() }
        binding.btnCancelPin.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun verifyPin() {
        val enteredPin = binding.etPinInput.text?.toString() ?: ""

        if (enteredPin.isBlank()) {
            binding.etPinInput.error = getString(R.string.error_pin_empty)
            return
        }

        if (pinManager.verifyPin(enteredPin)) {
            hideKeyboard()
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            failedAttempts++
            binding.etPinInput.text?.clear()
            val remaining = MAX_ATTEMPTS - failedAttempts
            if (remaining > 0) {
                binding.etPinInput.error = getString(R.string.error_pin_wrong, remaining)
            } else {
                Toast.makeText(this, R.string.error_too_many_attempts, Toast.LENGTH_LONG).show()
                // Lock out for a short delay then allow retry
                binding.btnConfirmPin.isEnabled = false
                Handler(Looper.getMainLooper()).postDelayed({
                    failedAttempts = 0
                    binding.btnConfirmPin.isEnabled = true
                }, LOCKOUT_DELAY_MS)
            }
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etPinInput.windowToken, 0)
    }

    companion object {
        const val EXTRA_REASON = "extra_reason"
        private const val MAX_ATTEMPTS = 5
        private const val LOCKOUT_DELAY_MS = 30_000L

        fun createIntent(context: Context, reason: String): Intent =
            Intent(context, PinActivity::class.java).apply {
                putExtra(EXTRA_REASON, reason)
            }
    }
}
