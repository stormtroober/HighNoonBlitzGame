package com.ds.highnoonblitz.view.components

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ds.highnoonblitz.MainThreadUtil
import com.ds.highnoonblitz.bluetooth.management.BluetoothDeviceManager
import com.ds.highnoonblitz.model.ExternalDevice

@SuppressLint("MissingPermission")
@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun ParticipantsList(deviceManager: BluetoothDeviceManager) {
    @Composable
    fun getButtonColor(device: ExternalDevice): Color {
        val masterDevice = deviceManager.getMasterDevice().value
        return when {
            (masterDevice as? ExternalDevice)?.getDevice()?.address == device.getDevice().address -> Color(33, 150, 243, 255)
            deviceManager
                .getReadyDevices()
                .value
                .map { it.getDevice().address }
                .contains(device.getDevice().address) -> Color(0, 200, 70)
            else -> MaterialTheme.colorScheme.secondary
        }
    }

    Text(
        text = "Connected devices",
        modifier =
            Modifier
                .padding(top = 16.dp, bottom = 8.dp),
        style = MaterialTheme.typography.headlineMedium,
    )

    LazyColumn(modifier = Modifier.height(250.dp)) {
        itemsIndexed(deviceManager.getConnectedDevices()) { index: Int, device: ExternalDevice ->
            ObserveReadyDevices(deviceManager = deviceManager)
            val deviceName = device.getDevice().name
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center) {
                val buttonColor = getButtonColor(device)
                DeviceButton(buttonText = deviceName, buttonAction = {
                    MainThreadUtil.makeToast(device.getDevice().address)
                }, buttonColor = buttonColor)
            }
        }
    }
}
