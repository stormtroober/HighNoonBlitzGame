package com.ds.highnoonblitz.bluetooth.handler.strategies

import android.bluetooth.BluetoothSocket

/**
 * Defines the strategy for handling Bluetooth device disconnection events.
 *
 * Implementations of this interface provide custom logic to manage actions when a
 * Bluetooth connection is lost.
 */
interface OnDeviceDisconnectStrategy {
    /**
     * Handles the disconnection of a Bluetooth device.
     *
     * Implementations should remove the disconnected device and perform any additional actions,
     * such as initiating an election procedure if necessary.
     *
     * @param socket the [BluetoothSocket] representing the disconnected device.
     */
    fun onDeviceDisconnect(socket: BluetoothSocket)
}
