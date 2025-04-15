package com.ds.highnoonblitz.bluetooth.communicationthreads

import PermissionManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
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
import com.ds.highnoonblitz.model.MyDevice
import java.io.IOException

@RequiresApi(Build.VERSION_CODES.S)
class ServerThread(
    private val activity: MainActivity,
    private val bluetoothAdapter: BluetoothAdapter,
    private val deviceManager: BluetoothDeviceManager,
    private val onDeviceDisconnected: (BluetoothSocket) -> Unit,
    private var onServerSocketCreated: ((String) -> Unit)?,
) : BasicBluetoothCommunicationThread(activity, bluetoothAdapter, deviceManager) {
    private val serverSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        try {
            activity.startDiscoverable(onServerSocketCreated)
            bluetoothAdapter.listenUsingRfcommWithServiceRecord("HighNoonBlitz", BT_UUID)
        } catch (e: SecurityException) {
            Log.e("ServerThread", "SecurityException while creating BluetoothServerSocket", e)
            PermissionManager.getBluetoothPermission(activity, bluetoothAdapter)
            null
        }
    }

    override fun run() {
        while (true) {
            if (deviceManager.getMasterDevice().value != null) {
                Log.i("ServerThread", "Master device is not null")
                val socket: BluetoothSocket? =
                    try {
                        serverSocket?.accept()
                    } catch (e: IOException) {
                        Log.e("ServerThread", "Socket's accept() method failed", e)
                        break
                    }
                socket?.also {
                    manageMyConnectedSocket(it)
                }
            }
        }
    }

    private fun manageMyConnectedSocket(socket: BluetoothSocket) {
        val communicationThread = activity.getMessageHandler()?.let { CommunicationChannelThread(socket, it, onDeviceDisconnected) }
        communicationThread!!.priority = Thread.MAX_PRIORITY

        communicationThread.start()
        val masterDevice = deviceManager.getMasterDevice().value
        if (masterDevice is MyDevice) {
            val msg = MessageFactory.createConfigurationMessage(deviceManager.getConnectedDevices().toList().map { it.getDevice().address })
            Log.i("ServerThread", "Sending config=" + msg.toString())
            communicationThread.write(msg)
        } else {
            val masterAddress = (masterDevice as? ExternalDevice)?.getDevice()?.address ?: ""
            val readyDP = deviceManager.me.getStatus()
            Log.i("ServerThread", "Sending ready=" + readyDP.toString())
            val msg = MessageFactory.createInfoSharingMessage(masterAddress, deviceManager.amIReady())
            Log.i("ServerThread", "Sending info=" + msg.toString())
            communicationThread.write(msg)
        }
        addConnection(socket, communicationThread)

        MainThreadUtil.makeToast("Socket is up and running!")
    }

    override fun close() {
        try {
            Log.e("ServerThread", "Closing server socket 1")
            serverSocket?.close()
            super.close()
        } catch (e: IOException) {
            Log.e("ServerThread", "Could not close the connect socket", e)
        }
    }

    fun updateServerSocketNameCallback(callback: ((String) -> Unit)?) {
        onServerSocketCreated = callback
        activity.startDiscoverable(onServerSocketCreated)
    }
}
