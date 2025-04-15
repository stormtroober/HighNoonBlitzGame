package com.ds.highnoonblitz.bluetooth.management

import android.bluetooth.BluetoothDevice
import com.ds.highnoonblitz.messages.MessageComposed

/**
 * Defines basic Bluetooth communication operations.
 *
 * Implementations send messages to specified Bluetooth devices, broadcast messages,
 * and can close active connections.
 */
interface BasicBluetoothCommunication {
    /**
     * Sends a composed message to a designated Bluetooth device.
     *
     * @param device the target [BluetoothDevice] to which the message will be sent.
     * @param message the [MessageComposed] containing the message details.
     */
    fun sendMessage(
        device: BluetoothDevice,
        message: MessageComposed,
    )

    /**
     * Broadcasts a composed message to all connected Bluetooth devices.
     *
     * @param message the [MessageComposed] containing the message details.
     */
    fun broadcastMessage(message: MessageComposed)

    /**
     * Closes the active Bluetooth connection.
     */
    fun closeConnection()
}
