package com.ds.highnoonblitz.view.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ds.highnoonblitz.bluetooth.management.BluetoothDeviceManager
import com.ds.highnoonblitz.model.ExternalDevice

@SuppressLint("MissingPermission")
@Composable
fun InGameList(deviceManager: BluetoothDeviceManager) {
    val inGameDevices by remember { deviceManager.getInGameDevices() }

    @Composable
    fun getButtonColor(device: ExternalDevice): Color {
        val masterDevice = deviceManager.getMasterDevice().value
        return when {
            masterDevice is ExternalDevice && masterDevice.getDevice().address == device.getDevice().address -> Color(33, 150, 243, 255)
            else -> MaterialTheme.colorScheme.secondary
        }
    }

    Text(
        text = "In Game devices",
        modifier =
            Modifier
                .padding(top = 20.dp, bottom = 8.dp),
        style = MaterialTheme.typography.headlineMedium,
    )

    LazyColumn(modifier = Modifier.height(100.dp)) {
        itemsIndexed(inGameDevices) { index, device ->
            val deviceName = device.getDevice().name

            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center) {
                val buttonColor = getButtonColor(device)
                DiscoveredDeviceButton(buttonText = deviceName, buttonAction = {
                    // bluetoothHandler.sendMessage(MessageFactory.createTestMessage(), device.first.remoteDevice)
                }, buttonColor = buttonColor)
            }
        }
    }
}
