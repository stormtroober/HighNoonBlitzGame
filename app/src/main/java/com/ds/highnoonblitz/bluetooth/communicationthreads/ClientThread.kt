package com.ds.highnoonblitz.bluetooth.communicationthreads

import PermissionManager
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.ds.highnoonblitz.GameConstants.BT_UUID
import com.ds.highnoonblitz.MainActivity
import com.ds.highnoonblitz.MainThreadUtil
import com.ds.highnoonblitz.bluetooth.management.BluetoothDeviceManager
import com.ds.highnoonblitz.messages.MessageFactory
import com.ds.highnoonblitz.model.ExternalDevice
import java.io.IOException
import kotlin.math.pow

@RequiresApi(Build.VERSION_CODES.S)
class ClientThread(
    private val activity: MainActivity,
    private val bluetoothAdapter: BluetoothAdapter,
    private val device: BluetoothDevice,
    private val deviceManager: BluetoothDeviceManager,
    private val onDeviceDisconnected: (BluetoothSocket) -> Unit,
) : BasicBluetoothCommunicationThread(activity, bluetoothAdapter, deviceManager) {
    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun run() {
        val maxAttempts = 3
        var attempt = 0

        while (attempt < maxAttempts) {
            try {
                val clientSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
                    try {
                        device.createRfcommSocketToServiceRecord(BT_UUID)
                    } catch (e: SecurityException) {
                        Log.e("ClientThread", "SecurityException while creating BluetoothSocket", e)
                        PermissionManager.getBluetoothPermission(activity, bluetoothAdapter)
                        null
                    }
                }

                bluetoothAdapter.cancelDiscovery()

                clientSocket?.let { socket ->
                    socket.connect()
                    manageMyConnectedSocket(socket)
                }

                // If connection is successful, break out of the loop
                break
            } catch (e: IOException) {
                Log.i("ClientThread", "Caught IOException", e)
                attempt++

                if (attempt < maxAttempts) {
                    val waitTime = (2.0.pow(attempt.toDouble()) * 1000).toLong().coerceAtMost(10000) // Exponential backoff with a max of 10 seconds
                    sleep(waitTime) // Wait for an exponential amount of time before the next attempt

                    MainThreadUtil.makeToast("Trying again to connect to ${device.name}. Attempt $attempt")
                } else {
                    // Handle failure to connect after maxAttempts
                    MainThreadUtil.makeToast("Failed to connect to ${device.name} after $maxAttempts attempts")
                }
            }
        }
    }

    private fun manageMyConnectedSocket(socket: BluetoothSocket) {
        val communicationThread = activity.getMessageHandler()?.let { CommunicationChannelThread(socket, it, onDeviceDisconnected) }
        communicationThread!!.priority = Thread.MAX_PRIORITY
        communicationThread.start()
        val masterDeviceAddress =
            (deviceManager.getMasterDevice().value as? ExternalDevice)?.getDevice()?.address
                ?: ""
        val msg = MessageFactory.createInfoSharingMessage(masterDeviceAddress, deviceManager.amIReady())
        Log.i("ClientThread", "Sending info=" + msg.toString())
        communicationThread.write(msg)
        addConnection(socket, communicationThread)
        MainThreadUtil.makeToast("Socket is up and running!")
        this.interrupt()
    }
}
