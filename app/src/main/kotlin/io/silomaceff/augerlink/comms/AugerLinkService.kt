package io.silomaceff.augerlink.comms

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

/**
 * Minimal foreground service that elevates the app's process importance
 * so Android stops filtering outbound IPv6 multicast egress.
 *
 * Without a foreground service, AutoInterface's `peer_announce` sendto
 * returns EPERM ~50% of the time — Android applies an unprivileged-
 * multicast filter to apps in normal-background importance. With this
 * service running, the filter lifts and announces transit reliably.
 *
 * Currently the service does no work — it just holds a persistent
 * notification so the OS treats the app as "actively doing something
 * the user can see." A later phase migrates the actual Reticulum router
 * lifecycle into this service so the comms stack also survives screen-
 * off doze and activity death.
 */
class AugerLinkService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannel()
        Log.i(TAG, "AugerLinkService created — promoting to foreground")
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // API 34+ requires a foregroundServiceType matching a declared permission.
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int =
        START_STICKY

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AugerLink")
            .setContentText("Sovereign comms running")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "AugerLink service",
                    NotificationManager.IMPORTANCE_LOW,
                ).apply {
                    description = "Persistent notification for the AugerLink comms service"
                }
                mgr.createNotificationChannel(channel)
            }
        }
    }

    companion object {
        private const val TAG = "AugerLinkService"
        private const val CHANNEL_ID = "auger_link_service"
        private const val NOTIFICATION_ID = 1

        /** Start the service from any Context (e.g. Activity onCreate). */
        fun start(context: Context) {
            val intent = Intent(context, AugerLinkService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                @Suppress("DEPRECATION")
                context.startService(intent)
            }
        }
    }
}
