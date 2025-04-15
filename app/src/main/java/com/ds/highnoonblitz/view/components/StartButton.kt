package com.ds.highnoonblitz.view.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ds.highnoonblitz.bluetooth.management.BluetoothDeviceManager

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun StartButton(
    buttonText: String,
    buttonAction: () -> Unit,
    deviceManager: BluetoothDeviceManager,
) {
    Button(
        modifier =
            Modifier
                .padding(16.dp)
                .padding(bottom = 5.dp)
                .fillMaxWidth(),
        onClick = buttonAction,
        enabled = deviceManager.isLobbyReady(),
    ) {
        Text(text = buttonText)
    }
}
