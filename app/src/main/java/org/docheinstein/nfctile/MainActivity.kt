package org.docheinstein.nfctile

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.docheinstein.nfctile.ui.theme.NFCTileTheme

private fun isWriteSettingsPermissionGranted(ctx: Context): Boolean {
    return ctx.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED
}

private fun createShareTextIntent(text: String): Intent {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    return Intent.createChooser(sendIntent, null)
}

class MainActivity : ComponentActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NfcStateService.start(this)
        setContent {
            MainScreen()
        }
    }
}


@Preview
@Composable
fun MainScreen() {
    val isPermissionGranted = remember { mutableStateOf(false) }
    NFCTileTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (isPermissionGranted.value) {
                PermissionGranted()
            } else {
                PermissionRequired {
                    isPermissionGranted.value = true
                }
            }
        }
    }
}

@Composable
fun PermissionRequired(onPermissionGranted: () -> Unit) {
    val context = LocalContext.current
    val adbCommand = stringResource(R.string.adb_command)

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Rounded.Info,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .padding(bottom = 12.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = stringResource(R.string.permission_required),
            modifier = Modifier.padding(vertical = 16.dp),
            style = MaterialTheme.typography.titleLarge,
        )

        Text(
            text = stringResource(R.string.permission_required_message),
            modifier = Modifier.padding(vertical = 24.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
        Card(
            modifier = Modifier
                .clickable {
                    context.startActivity(createShareTextIntent(adbCommand))
                }
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.Black, contentColor = Color.White),
        ) {
            Text(
                text = adbCommand,
                modifier = Modifier.padding(12.dp),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
            )
        }
    }

    LaunchedEffect(true) {
        launch(Dispatchers.Default) {
            while (!isWriteSettingsPermissionGranted(context)) {
                delay(1000)
            }
            launch(Dispatchers.Main) {
                Log.i("MainActivity", "WRITE_SECURE_SETTINGS permission granted")
                onPermissionGranted()
            }
        }
    }
}

@Preview
@Composable
fun PermissionGranted() {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.nfc),
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .padding(bottom = 12.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.permission_granted_description),
            modifier = Modifier
                .padding(vertical = 32.dp, horizontal = 32.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}