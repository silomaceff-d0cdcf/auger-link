package io.silomaceff.augerlink.comms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Phase 5 completion: auto-start the Reticulum router after device boot.
 *
 * Without this, the user has to open AugerLink at least once after every
 * reboot before incoming LXMF messages can be received. With it, the
 * foreground service comes up via [AugerLinkService.start] as soon as the
 * device finishes booting, the LXMF identity loads from disk, and inbound
 * delivery resumes — all without any UI interaction.
 *
 * The corresponding manifest entry uses
 * `android.intent.action.BOOT_COMPLETED` plus the `RECEIVE_BOOT_COMPLETED`
 * permission. On Android 11+ the user must launch the app at least once
 * before BOOT_COMPLETED is delivered to the receiver — this is an OS-level
 * privacy gate and is expected behavior, not a bug here.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        Log.i(TAG, "BOOT_COMPLETED received — starting AugerLinkService")
        AugerLinkService.start(context.applicationContext)
    }

    companion object {
        private const val TAG = "AugerLinkBoot"
    }
}
