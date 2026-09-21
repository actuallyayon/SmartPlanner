package com.smartplanner.model.anchor

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.smartplanner.model.AnchorType

/**
 * Signal detector for device power connection (Charging started).
 *
 * API 26+ Limitation:
 * ACTION_POWER_CONNECTED cannot be registered as an implicit broadcast in AndroidManifest.xml.
 * It is dynamically registered at runtime while the app/foreground service is alive.
 */
class ChargingAnchorDetector(
    private val context: Context
) : AnchorDetector {
    override val anchorType: AnchorType = AnchorType.CHARGING_STARTED
    private var receiver: BroadcastReceiver? = null

    override fun isSupported(): Boolean = true
    override fun requiredPermissions(): List<String> = emptyList()

    override fun startListening(onSignalDetected: (AnchorType) -> Unit) {
        if (receiver != null) return
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_POWER_CONNECTED) {
                    onSignalDetected(AnchorType.CHARGING_STARTED)
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_POWER_CONNECTED)
        context.registerReceiver(receiver, filter)
    }

    override fun stopListening() {
        receiver?.let {
            try {
                context.unregisterReceiver(it)
            } catch (_: Exception) {}
            receiver = null
        }
    }
}

/**
 * Signal detector for headphones / audio device connection.
 *
 * API 26+ Limitation:
 * ACTION_HEADSET_PLUG must be registered dynamically via Context.registerReceiver;
 * it cannot be declared in the manifest.
 */
class HeadphonesAnchorDetector(
    private val context: Context
) : AnchorDetector {
    override val anchorType: AnchorType = AnchorType.HEADPHONES_CONNECTED
    private var receiver: BroadcastReceiver? = null

    override fun isSupported(): Boolean = true
    override fun requiredPermissions(): List<String> = emptyList()

    override fun startListening(onSignalDetected: (AnchorType) -> Unit) {
        if (receiver != null) return
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_HEADSET_PLUG) {
                    val state = intent.getIntExtra("state", -1)
                    if (state == 1) { // 1 = plugged in, 0 = unplugged
                        onSignalDetected(AnchorType.HEADPHONES_CONNECTED)
                    }
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
        context.registerReceiver(receiver, filter)
    }

    override fun stopListening() {
        receiver?.let {
            try {
                context.unregisterReceiver(it)
            } catch (_: Exception) {}
            receiver = null
        }
    }
}

/**
 * Signal detector for user presence / first screen unlock.
 *
 * API 26+ Note:
 * ACTION_USER_PRESENT is one of the whitelisted implicit broadcasts on Android 8.0+.
 * It can be declared in manifest or dynamically registered.
 */
class FirstUnlockAnchorDetector(
    private val context: Context
) : AnchorDetector {
    override val anchorType: AnchorType = AnchorType.FIRST_UNLOCK
    private var receiver: BroadcastReceiver? = null

    override fun isSupported(): Boolean = true
    override fun requiredPermissions(): List<String> = emptyList()

    override fun startListening(onSignalDetected: (AnchorType) -> Unit) {
        if (receiver != null) return
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_USER_PRESENT) {
                    onSignalDetected(AnchorType.FIRST_UNLOCK)
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
        context.registerReceiver(receiver, filter)
    }

    override fun stopListening() {
        receiver?.let {
            try {
                context.unregisterReceiver(it)
            } catch (_: Exception) {}
            receiver = null
        }
    }
}

/**
 * Signal detector for arriving home (Geofencing).
 *
 * API 26+ & Android 10+ (API 29+) Limitations:
 * - Requires ACCESS_FINE_LOCATION and ACCESS_BACKGROUND_LOCATION for background geofencing.
 * - Background location requires explicit user approval in system settings on Android 11+.
 * - If permissions are denied or unavailable, falls back gracefully to CLOCK_TIME.
 */
class LocationGeofenceAnchorDetector(
    private val context: Context
) : AnchorDetector {
    override val anchorType: AnchorType = AnchorType.ARRIVED_HOME

    override fun isSupported(): Boolean {
        return hasRequiredPermissions()
    }

    override fun requiredPermissions(): List<String> {
        val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        return permissions
    }

    fun hasRequiredPermissions(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val backgroundLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        return fineLocation && backgroundLocation
    }

    override fun startListening(onSignalDetected: (AnchorType) -> Unit) {
        if (!hasRequiredPermissions()) {
            // Permission denied -> Fall back to CLOCK_TIME signal
            onSignalDetected(AnchorType.CLOCK_TIME)
            return
        }
        // In full production, registers Google Play Services GeofencingClient or LocationManager listener.
    }

    override fun stopListening() {
        // Cleanup geofence registrations
    }
}

/**
 * Fallback clock time detector.
 */
class ClockTimeAnchorDetector(
    private val context: Context
) : AnchorDetector {
    override val anchorType: AnchorType = AnchorType.CLOCK_TIME
    override fun isSupported(): Boolean = true
    override fun requiredPermissions(): List<String> = emptyList()

    override fun startListening(onSignalDetected: (AnchorType) -> Unit) {
        // Schedulable clock alarm / WorkManager
    }

    override fun stopListening() {}
}

/**
 * Fake anchor detector for unit testing and manual simulation triggers.
 */
class FakeAnchorDetector(
    override val anchorType: AnchorType,
    var isSupportedFlag: Boolean = true,
    var requiredPermissionsList: List<String> = emptyList()
) : AnchorDetector {
    private var callback: ((AnchorType) -> Unit)? = null

    override fun isSupported(): Boolean = isSupportedFlag
    override fun requiredPermissions(): List<String> = requiredPermissionsList

    override fun startListening(onSignalDetected: (AnchorType) -> Unit) {
        callback = onSignalDetected
    }

    override fun stopListening() {
        callback = null
    }

    fun simulateSignal() {
        callback?.invoke(anchorType)
    }
}
