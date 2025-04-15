package com.ds.highnoonblitz.view.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.ds.highnoonblitz.bluetooth.handler.BluetoothHandlerProvider
import com.ds.highnoonblitz.controller.LobbyController

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun ReadyButton(lobbyController: LobbyController) {
    val isReady = remember { mutableStateOf(BluetoothHandlerProvider.getBluetoothHandler().getDeviceManager().amIReady()) }

    Button(onClick = {
        val newReadyState = isReady.value.not()
        lobbyController.notifyReadiness(newReadyState)
        isReady.value = newReadyState
    }) {
        Text(text = if (isReady.value) "Not Ready" else "Ready")
    }
}
