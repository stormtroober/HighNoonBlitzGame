package com.ds.highnoonblitz.bluetooth.management

import PermissionManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.ds.highnoonblitz.MainActivity
import com.ds.highnoonblitz.bluetooth.communicationthreads.ClientThread

/**
 * Manages client side Bluetooth communications.
 *
 * This manager handles connecting to a device using its MAC address,
 * discovering nearby devices, and disconnecting from the lobby.
 *
 * @property activity the MainActivity instance used for context.
 * @property bluetoothManager the system BluetoothManager.
 * @property deviceManager manages connected Bluetooth devices.
 * @property onDeviceDisconnected callback that is invoked when a device disconnects.
 */
class BluetoothClientManager(
    private val activity: MainActivity,
    private val bluetoothManager: BluetoothManager,
    private val deviceManager: BluetoothDeviceManager,
    private val onDeviceDisconnected: (BluetoothSocket) -> Unit,
) : BluetoothCommunicationManager(bluetoothManager) {
    private var receiver: BroadcastReceiver? = null

    /**
     * Connects to a Bluetooth device with the specified MAC address.
     *
     * Checks if the provided MAC address is valid and then initiates the connection.
     *
     * @param deviceMac the MAC address of the target device.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    @Synchronized
    fun connectToDevice(deviceMac: String) {
        val device = bluetoothAdapter.getRemoteDevice(deviceMac)
        if (BluetoothAdapter.checkBluetoothAddress(deviceMac)) {
            connect(device)
        } else {
            Log.e("BluetoothClientManager", "Invalid MAC address")
        }
    }

    /**
     * Initiates a connection to the specified Bluetooth device.
     *
     * Verifies if Bluetooth is enabled and that the device is not already connected,
     * then starts a new client thread to manage the connection.
     *
     * @param device the BluetoothDevice to connect to.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    private fun connect(device: BluetoothDevice) {
        if (bluetoothAdapter.isEnabled && !deviceManager.isDeviceAlreadyConnected(device.address)) {
            bluetoothThread = ClientThread(activity, bluetoothAdapter, device, deviceManager, onDeviceDisconnected)
            bluetoothThread!!.start()
        } else {
            Log.i("BluetoothClientManager", "Device is already connected")
        }
    }

    /**
     * Discovers nearby Bluetooth devices.
     *
     * Clears the provided list and starts the discovery process. Discovered devices are
     * added to the list via a BroadcastReceiver listening for [BluetoothDevice.ACTION_FOUND].
     *
     * @param discoveredDevices a mutable list to receive discovered Bluetooth devices.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun discoveryDevices(discoveredDevices: MutableList<BluetoothDevice>) {
        try {
            discoveredDevices.clear()
            bluetoothAdapter.startDiscovery()
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            receiver =
                object : BroadcastReceiver() {
                    override fun onReceive(
                        context: Context,
                        intent: Intent,
                    ) {
                        Log.i("BluetoothClientManager", "device found")
                        val device: BluetoothDevice? =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        if (device != null && !discoveredDevices.contains(device)) {
                            discoveredDevices.add(device)
                        }
                    }
                }
            activity.registerReceiver(receiver, filter)
        } catch (e: SecurityException) {
            PermissionManager.getBluetoothPermission(activity, bluetoothAdapter)
        }
    }
}
