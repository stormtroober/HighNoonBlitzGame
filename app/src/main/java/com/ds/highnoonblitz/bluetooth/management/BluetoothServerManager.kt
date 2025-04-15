package com.ds.highnoonblitz.bluetooth.management

import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.ds.highnoonblitz.MainActivity
import com.ds.highnoonblitz.bluetooth.communicationthreads.ServerThread

/**
 * Manages server side Bluetooth communications.
 *
 * This manager handles starting a Bluetooth server thread to accept client connections,
 * updating the server socket name via a callback, and closing the active server connection.
 *
 * @property activity the MainActivity instance used for context.
 * @property deviceManager manages connected Bluetooth devices.
 * @property onDeviceDisconnected a callback invoked when a device disconnects.
 * @property onServerSocketCreated an optional callback invoked when the server socket is created or updated.
 */
class BluetoothServerManager(
    private val activity: MainActivity,
    bluetoothManager: BluetoothManager,
    private val deviceManager: BluetoothDeviceManager,
    private val onDeviceDisconnected: (BluetoothSocket) -> Unit,
    private var onServerSocketCreated: ((String) -> Unit)?,
) : BluetoothCommunicationManager(bluetoothManager) {
    /**
     * Starts the Bluetooth server if the adapter is enabled and no server thread is running.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun startBluetoothServer() {
        if (bluetoothAdapter.isEnabled && bluetoothThread == null) {
            bluetoothThread =
                ServerThread(
                    activity,
                    bluetoothAdapter,
                    deviceManager,
                    onDeviceDisconnected,
                    onServerSocketCreated,
                )
            bluetoothThread?.start()
        }
    }

    /**
     * Updates the server socket name callback at runtime.
     *
     * @param callback a callback function that receives the updated server socket name,
     *                 or null to remove the callback.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun updateServerSocketNameCallback(callback: ((String) -> Unit)?) {
        onServerSocketCreated = callback
        (bluetoothThread as? ServerThread)?.updateServerSocketNameCallback(callback)
    }

    /**
     * Closes the active Bluetooth server connection.
     *
     * This function logs the action, closes the server thread, and resets it to null.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    override fun closeConnection() {
        Log.i("BluetoothServerManager", "Closing connection")
        (bluetoothThread as ServerThread).close()
        bluetoothThread = null
    }
}
