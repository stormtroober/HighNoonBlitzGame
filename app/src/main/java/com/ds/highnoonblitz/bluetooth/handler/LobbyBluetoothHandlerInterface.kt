package com.ds.highnoonblitz.bluetooth.handler

/**
 * Extends [BluetoothHandlerInterface] to add lobby-specific Bluetooth operations.
 */
interface LobbyBluetoothHandlerInterface : BluetoothHandlerInterface {
    /**
     * Starts the Bluetooth server.
     */
    fun startBluetoothServer()

    /**
     * Stops the running Bluetooth server.
     */
    fun stopBluetoothServer()

    /**
     * Starts the Bluetooth client and initiates device discovery.
     */
    fun startBluetoothClientAndDiscovery()

    /**
     * Connects to a Bluetooth device with the specified MAC address.
     *
     * @param deviceMac the MAC address of the target Bluetooth device.
     */
    fun connectToDevice(deviceMac: String)

    /**
     * Called when the election timer has finished.
     * Triggers actions related to the election process.
     */
    fun onElectionTimerFinished()

    /**
     * Sets the callback function for updating the server socket name.
     *
     * @param callback a function that receives the updated server socket name,
     *                 or null to remove the callback.
     */
    fun setServerSocketNameCallback(callback: ((String) -> Unit)?)
}
