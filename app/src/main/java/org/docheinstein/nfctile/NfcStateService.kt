package org.docheinstein.nfctile

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat

class NfcStateService : Service() {
    companion object {
        private const val TAG = "NfcStateService"

        private const val NOTIFICATION_CHANNEL_ID = "NfcService"
        private const val NOTIFICATION_ID = 1

        fun start(context: Context) {
            val nfcStateServiceIntent = Intent(context, NfcStateService::class.java)
            try {
                context.startForegroundService(nfcStateServiceIntent)
            } catch (_: Exception) {
                Log.e(TAG, "Failed to start NfcStateService")
            }

        }
    }

    private var nfcStateBroadcastReceiver: NfcStateBroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Starting NFC state service")
        initializeServiceNotificationChannel()
        startForeground(NOTIFICATION_ID, createServiceNotification())

        nfcStateBroadcastReceiver = NfcStateBroadcastReceiver()
        val intentFilter = IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)
        registerReceiver(nfcStateBroadcastReceiver, intentFilter)
        NfcTileService.requestUpdate(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(MainActivity.TAG, "Stopping NFC state service")
        unregisterReceiver(nfcStateBroadcastReceiver)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun initializeServiceNotificationChannel() {
        val serviceNotificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            applicationContext.getString(R.string.service_notification_channel_description),
            NotificationManager.IMPORTANCE_MIN)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(serviceNotificationChannel)
    }

    private fun createServiceNotification(): Notification {
        val openNotificationSettingsIntent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            putExtra(Settings.EXTRA_CHANNEL_ID, NOTIFICATION_CHANNEL_ID)
        }

        val openNotificationSettingsPendingIntent = PendingIntent.getActivity(
            this@NfcStateService, 0, openNotificationSettingsIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(applicationContext.getString(R.string.service_notification_content_title))
            .setContentText(applicationContext.getString(R.string.service_notification_content_text))
            .setSmallIcon(R.drawable.nfc)
            .setContentIntent(openNotificationSettingsPendingIntent)
            .build()
    }
}