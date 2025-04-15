package com.ds.highnoonblitz.bluetooth.handler.strategies

import android.annotation.SuppressLint
import android.bluetooth.BluetoothSocket
import android.os.Build
import androidx.annotation.RequiresApi
import com.ds.highnoonblitz.MainThreadUtil
import com.ds.highnoonblitz.bluetooth.handler.BluetoothHandlerProvider
import com.ds.highnoonblitz.messages.MessageFactory
import com.ds.highnoonblitz.model.ExternalDevice

/**
 * Strategy for handling disconnection events for the game.
 *
 * This strategy removes the disconnected device from the device manager and,
 * if the disconnected device is the master, triggers an election process by notifying the game controller
 * and sending an election message to the devices in the election list.
 *
 * @constructor Creates a new instance of [GameOnDeviceDisconnectStrategy].
 */
class GameOnDeviceDisconnectStrategy : OnDeviceDisconnectStrategy {
    private val bluetoothHandler = BluetoothHandlerProvider.getBluetoothHandler()

    /**
     * Called when a Bluetooth device is disconnected.
     *
     * This function removes the device from the device manager,
     * displays a toast message, and if the disconnected device is the master,
     * triggers an election process by notifying the game controller and sending an election message.
     *
     * @param socket the [BluetoothSocket] associated with the disconnected device.
     */
    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.S)
    @Synchronized
    override fun onDeviceDisconnect(socket: BluetoothSocket) {
        val deviceManager = bluetoothHandler.getDeviceManager()
        deviceManager.removeConnection(socket.remoteDevice)
        MainThreadUtil.makeToast("Device disconnected: ${socket.remoteDevice.name}")
        /*
        Game is still ongoing, but the master disconnected. We need to save that the Server crashed to start an
        election process when we go back to the lobby
         */
        if (socket.remoteDevice.address == (deviceManager.getMasterDevice().value as? ExternalDevice)?.getDevice()?.address) {
            bluetoothHandler
                .getMainActivity()
                .getMainController()
                ?.getGameController()
                ?.electionOnGoing()

            // So when the election is finished we don't switch to lobby
            bluetoothHandler.setElectionCallback {
                bluetoothHandler
                    .getMainActivity()
                    .getMainController()
                    ?.getGameController()
                    ?.iAmCoordinator()
            }
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
