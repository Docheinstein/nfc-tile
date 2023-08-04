package org.docheinstein.nfctile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootCompletedBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            context?.let { NfcStateService.start(it) }
        }
    }
}