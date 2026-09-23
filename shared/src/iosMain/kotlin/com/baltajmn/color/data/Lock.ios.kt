package com.baltajmn.color.data

import com.baltajmn.color.i18n.S
import kotlinx.cinterop.ExperimentalForeignApi
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthentication
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
actual object Lock {

    actual fun isAvailable(): Boolean =
        LAContext().canEvaluatePolicy(LAPolicyDeviceOwnerAuthentication, null)

    actual fun authenticate(onResult: (Boolean) -> Unit) {
        // deviceOwnerAuthentication and not biometrics alone: it falls back to the phone code on its
        // own, so a face that does not work does not lock anyone out.
        LAContext().evaluatePolicy(LAPolicyDeviceOwnerAuthentication, S.lockPromptSubtitle) { ok, _ ->
            // The answer arrives on a private queue and the state it moves is the interface.
            dispatch_async(dispatch_get_main_queue()) { onResult(ok) }
        }
    }

    /** iOS hides the app from the switcher from Swift: Compose does not repaint before the snapshot. */
    actual fun setHidesPreview(on: Boolean) = Unit
}
