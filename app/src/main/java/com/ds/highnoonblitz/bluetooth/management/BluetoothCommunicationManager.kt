package com.ds.highnoonblitz.bluetooth.management

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.ds.highnoonblitz.bluetooth.communicationthreads.BasicBluetoothCommunicationThread
import com.ds.highnoonblitz.messages.MessageComposed

/**
 * Manages Bluetooth communication operations.
 *
 *
 * Provides basic operations to send messages to a specific Bluetooth device or to broadcast
 * messages to all connected devices. Handles closing the active Bluetooth connection.
 *
 * @property bluetoothManager the [BluetoothManager] instance used to obtain the Bluetooth adapter.
 */
open class BluetoothCommunicationManager(
    private val bluetoothManager: BluetoothManager,
) : BasicBluetoothCommunication {
    /**
     * The [BluetoothAdapter] obtained from the Bluetooth manager.
     */
    open val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter

    /**
     * Thread handling Bluetooth communication.
     */
    open var bluetoothThread: BasicBluetoothCommunicationThread? = null

    /**
     * Sends a composed message to the designated Bluetooth device.
     *
     * @param device the Bluetooth device to which the message is sent.
     * @param message the [MessageComposed] containing message details.
     *
     * @throws IllegalStateException if the Bluetooth thread is not initialized.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    override fun sendMessage(
        device: BluetoothDevice,
        message: MessageComposed,
    ) {
        bluetoothThread?.sendMessage(device, message)
    }

    /**
     * Broadcasts a composed message to all connected Bluetooth devices.
     *
     * @param message the [MessageComposed] containing the message details.
     *
     * @throws IllegalStateException if the Bluetooth thread is not initialized.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    override fun broadcastMessage(message: MessageComposed) {
        bluetoothThread?.sendBroadcastMessage(message)
    }

    /**
     * Closes the active Bluetooth connection.
     *
     * This function logs the action of closing the connection.
     */
    override fun closeConnection() {
        Log.i("BluetoothCommunicationManager", "Closing connection")
    }
}
