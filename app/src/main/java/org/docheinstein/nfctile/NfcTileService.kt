package org.docheinstein.nfctile

import android.content.ComponentName
import android.content.Context
import android.nfc.NfcAdapter
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log


class NfcTileService : TileService() {
    companion object {
        private const val TAG = "NfcTileService"

        fun requestUpdate(context: Context) {
            requestListeningState(context, ComponentName(context, NfcTileService::class.java))
        }
    }

    override fun onTileAdded() {
        super.onTileAdded()
        updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        val adapter = NfcAdapter.getDefaultAdapter(this)

        // HACK: NfcAdapter.enable() and NfcAdapter.disable() are hidden @SystemApi.
        // Access those via reflection.
        adapter?.let {
            try {
                val methodName = if (adapter.isEnabled) "disable" else "enable"
                val method = NfcAdapter::class.java.getMethod(methodName)
                Log.d(TAG, "Changing NFC state to: '$methodName'")
                method.invoke(adapter)
                // Do not update the UI here explicitly,
                // (we do not even know if the state has been changed successfully).
                // Instead wait for the NfcStateBroadcastReceiver to detect the change
                // and update the tile through the usual onStartListening flow
            } catch (e: Exception) {
                Log.e(TAG, "Failed to change NFC state")
            }
        }
    }

    private fun updateTile() {
        val nfcState = getNfcState()
        Log.d(TAG, "NFC state is $nfcState")
        qsTile.state = when (nfcState) {
             NfcAdapter.STATE_TURNING_ON,  NfcAdapter.STATE_ON -> Tile.STATE_ACTIVE
             else -> Tile.STATE_INACTIVE
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            qsTile.subtitle = when (nfcState) {
                 NfcAdapter.STATE_TURNING_ON -> applicationContext.resources.getString(R.string.turning_on)
                 NfcAdapter.STATE_TURNING_OFF -> applicationContext.resources.getString(R.string.turning_off)
                 else -> ""
            }
        }
        qsTile.updateTile()
    }

    private fun getNfcState(): Int {
        val adapter = NfcAdapter.getDefaultAdapter(this)
        // HACK: NfcAdapter.getAdapterState() is marked as @UnsupportedAppUsage,
        // Access it via reflection.
        val method = NfcAdapter::class.java.getMethod("getAdapterState")
        return try {
            method.invoke(adapter) as Int
        } catch (_: Exception) {
            // fallback: retrieve the state with in a safe way with adapter.isEnabled
            if (adapter.isEnabled) NfcAdapter.STATE_ON else NfcAdapter.STATE_OFF
        }
    }
}