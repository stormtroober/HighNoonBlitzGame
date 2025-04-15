package com.ds.highnoonblitz.bluetooth.handler.strategies

import android.annotation.SuppressLint
import android.bluetooth.BluetoothSocket
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.ds.highnoonblitz.bluetooth.handler.BluetoothHandlerProvider
import com.ds.highnoonblitz.messages.MessageFactory
import com.ds.highnoonblitz.model.ExternalDevice

/**
 * Strategy for handling disconnection events on a lobby client.
 *
 * This strategy validates the disconnection and, if the disconnected device was the master,
 * initiates the election procedure by starting election and sending the election message to all
 * devices in the election list.
 *
 * @constructor Creates a LobbyClientOnDeviceDisconnectStrategy instance.
 */
class LobbyClientOnDeviceDisconnectStrategy : OnDeviceDisconnectStrategy {
    @SuppressLint("NewApi")
    private val bluetoothHandler = BluetoothHandlerProvider.getBluetoothHandler()

    @SuppressLint("NewApi")
    private val deviceManager = bluetoothHandler.getDeviceManager()

    /**
     * Handles the event when a Bluetooth device disconnects.
     *
     * The function removes the device from the device manager and checks whether the disconnected
     * device was the master device. If so, it logs the event, initiates the election procedure,
     * and sends the election message to each device in the election list.
     *
     * @param socket the BluetoothSocket associated with the disconnected device.
     */
    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.S)
    @Synchronized
    override fun onDeviceDisconnect(socket: BluetoothSocket) {
        deviceManager.removeConnection(socket.remoteDevice)
        // If the disconnected device was the master device, start election procedure
        if (socket.remoteDevice.address == (deviceManager.getMasterDevice().value as? ExternalDevice)?.getDevice()?.address) {
            Log.i("Lobby", "Starting election procedure. " + socket.remoteDevice.name)
            val electionMessage = MessageFactory.createElectionMessage()
            deviceManager.startElection()
            for (deviceMac in deviceManager.getElectionList()) {
                val device =
                    bluetoothHandler.getBluetoothAdapter()?.getRemoteDevice(deviceMac)
                bluetoothHandler.sendMessage(electionMessage, device!!)
            }
        }
    }
}
