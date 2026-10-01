package com.baltajmn.color.data

import android.app.KeyguardManager
import android.os.Build
import android.os.SystemClock
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.baltajmn.color.i18n.S
import java.lang.ref.WeakReference

actual object Lock {

    /** Set by MainActivity: BiometricPrompt needs a live fragment host, and holding it would leak it. */
    var host: WeakReference<FragmentActivity>? = null

    // The screen lock itself, on every version. canAuthenticate can answer HW_UNAVAILABLE or
    // STATUS_UNKNOWN on some builds even with a code set, and a false no here switches the lock off.
    actual fun isAvailable(): Boolean =
        AndroidContext.value.getSystemService(KeyguardManager::class.java)?.isDeviceSecure == true

    actual fun authenticate(onResult: (Boolean) -> Unit) {
        val activity = host?.get()
        if (activity == null) {
            onResult(false)
            return
        }
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) =
                    onResult(true)

                // A wrong finger is not an answer: only giving up or an error is.
                override fun onAuthenticationError(code: Int, message: CharSequence) = onResult(false)
            },
        )
        // The one combination valid on every API level. Mixing it with setDeviceCredentialAllowed
        // made build() throw before Android 11, which closed the app.
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(S.lockPromptTitle)
            .setSubtitle(S.lockPromptSubtitle)
            .setAllowedAuthenticators(BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
            .build()
        try {
            prompt.authenticate(info)
        } catch (e: Exception) {
            onResult(false)
        }
    }

    actual fun setHidesPreview(on: Boolean) {
        // Not FLAG_SECURE: that would also stop the user taking their own screenshots.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            host?.get()?.setRecentsScreenshotEnabled(!on)
        }
    }
}

actual fun elapsedMillis(): Long = SystemClock.elapsedRealtime()
