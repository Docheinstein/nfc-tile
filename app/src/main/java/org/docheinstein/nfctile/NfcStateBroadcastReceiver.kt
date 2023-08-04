package org.docheinstein.nfctile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter

class NfcStateBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == NfcAdapter.ACTION_ADAPTER_STATE_CHANGED) {
            NfcTileService.requestUpdate(context)
        }
    }
}