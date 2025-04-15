package com.ds.highnoonblitz.view.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.ds.highnoonblitz.bluetooth.management.BluetoothDeviceManager

@Composable
fun ObserveReadyDevices(deviceManager: BluetoothDeviceManager) {
    val readyDevicesState by remember { deviceManager.getReadyDevices() }
}
