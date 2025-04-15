package com.ds.highnoonblitz.bluetooth.handler

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Build
import androidx.annotation.RequiresApi
import com.ds.highnoonblitz.bluetooth.handler.strategies.OnDeviceDisconnectStrategy
import com.ds.highnoonblitz.bluetooth.management.BluetoothDeviceManager
import com.ds.highnoonblitz.messages.MessageComposed

/**
 * Interface defining the contract for Bluetooth handling operations.
 *
 * Implementations handle device connections, messaging, device discovery,
 * disconnection events, and election procedures.
 */
interface BluetoothHandlerInterface {
    /**
     * Sets the callback function to be invoked when the election process concludes.
     *
     * @param callback a function to call once the election process has completed.
     */
    fun setElectionCallback(callback: () -> Unit)

    /**
     * Sets the strategy for handling Bluetooth device disconnection events.
     *
     * @param strategy an implementation of [OnDeviceDisconnectStrategy] for handling disconnections.
     */
    fun setOnDeviceDisconnectStrategy(strategy: OnDeviceDisconnectStrategy)

    /**
     * Sends a message to a specified Bluetooth device.
     *
     * @param message the composed message to be sent.
     * @param device the target [BluetoothDevice] that should receive the message.
     */
    fun sendMessage(
        message: MessageComposed,
        device: BluetoothDevice,
    )

    /**
     * Broadcasts a message to all connected Bluetooth devices.
     *
     * @param message the composed message to be broadcast.
     */
    fun broadcastMessage(message: MessageComposed)

    /**
     * Retrieves the [BluetoothAdapter] used for Bluetooth operations.
     *
     * @return the [BluetoothAdapter], or null if not available.
     */
    fun getBluetoothAdapter(): BluetoothAdapter?

    /**
     * Retrieves the [BluetoothDeviceManager] managing connected devices.
     *
     * @return the instance of [BluetoothDeviceManager].
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun getDeviceManager(): BluetoothDeviceManager

    /**
     * Handles the disconnection event for a Bluetooth device.
     *
     * @param socket the [BluetoothSocket] associated with the disconnected device.
     */
    fun onDeviceDisconnected(socket: BluetoothSocket)

    /**
     * Retrieves the currently set strategy for handling device disconnection events.
     *
     * @return the active [OnDeviceDisconnectStrategy].
     */
    fun getOnDeviceDisconnectStrategy(): OnDeviceDisconnectStrategy
}
