package com.smartplanner.model.anchor

import com.smartplanner.model.AnchorType

/**
 * Common interface for context and hardware signal detectors.
 * Allows concrete Android implementations and mock/fake test implementations.
 */
interface AnchorDetector {
    /**
     * The type of anchor this detector listens for.
     */
    val anchorType: AnchorType

    /**
     * Whether this signal can be detected on the current device.
     */
    fun isSupported(): Boolean

    /**
     * List of Android permissions required to detect this signal.
     */
    fun requiredPermissions(): List<String>

    /**
     * Starts listening for the signal. Calls [onSignalDetected] whenever the event occurs.
     */
    fun startListening(onSignalDetected: (AnchorType) -> Unit)

    /**
     * Stops listening and cleans up any registered receivers or callbacks.
     */
    fun stopListening()
}
