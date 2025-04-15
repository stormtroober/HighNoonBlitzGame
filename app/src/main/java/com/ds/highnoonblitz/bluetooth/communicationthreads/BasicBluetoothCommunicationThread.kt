package com.ds.highnoonblitz.bluetooth.communicationthreads

import PermissionManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Build
import androidx.annotation.RequiresApi
import com.ds.highnoonblitz.MainActivity
import com.ds.highnoonblitz.bluetooth.management.BluetoothDeviceManager
import com.ds.highnoonblitz.messages.MessageComposed

@RequiresApi(Build.VERSION_CODES.S)
open class BasicBluetoothCommunicationThread(
    activity: MainActivity,
    bluetoothAdapter: BluetoothAdapter,
    private val deviceManager: BluetoothDeviceManager,
) : Thread() {
    init {
        if (!PermissionManager.hasAllPermissions(activity)) {
            PermissionManager.getBluetoothPermission(activity, bluetoothAdapter)
        }
    }

    @Synchronized
    open fun addConnection(
        socket: BluetoothSocket,
        thread: CommunicationChannelThread,
    ) {
        deviceManager.addNewConnection(socket, thread)
    }

    fun sendBroadcastMessage(message: MessageComposed) {
        deviceManager.sendBroadcastMessage(message)
    }

    fun sendMessage(
        device: BluetoothDevice,
        message: MessageComposed,
    ) {
        deviceManager.sendMessage(device.address, message)
    }

    open fun close() {
        this.interrupt()
    }
}
