package com.ds.highnoonblitz.bluetooth.handler

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.ds.highnoonblitz.MainActivity
import com.ds.highnoonblitz.bluetooth.handler.strategies.OnDeviceDisconnectStrategy
import com.ds.highnoonblitz.bluetooth.management.BluetoothClientManager
import com.ds.highnoonblitz.bluetooth.management.BluetoothCommunicationManager
import com.ds.highnoonblitz.bluetooth.management.BluetoothDeviceManager
import com.ds.highnoonblitz.bluetooth.management.BluetoothServerManager
import com.ds.highnoonblitz.leaderelection.BullyElectionManager
import com.ds.highnoonblitz.messages.MessageComposed
import com.ds.highnoonblitz.messages.MessageFactory
import com.ds.highnoonblitz.model.MyDevice

/**
 * Handler for managing Bluetooth operations.
 *
 * This class manages Bluetooth server and client operations,
 * processes disconnection events, handles election procedures,
 * and sends messages.
 *
 * @property activity the instance of MainActivity.
 * @constructor Creates a new BluetoothHandler instance.
 */
@RequiresApi(Build.VERSION_CODES.S)
class BluetoothHandler(
    private val activity: MainActivity,
) : LobbyBluetoothHandlerInterface {
    @RequiresApi(Build.VERSION_CODES.S)
    private val electionManager = BullyElectionManager(::onElectionTimerFinished)

    @RequiresApi(Build.VERSION_CODES.S)
    private val deviceManager: BluetoothDeviceManager = BluetoothDeviceManager(electionManager)
    private val bluetoothManager: BluetoothManager = activity.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter = bluetoothManager.adapter
    private var bluetoothClientManager: BluetoothClientManager? = null
    private var bluetoothServerManager: BluetoothServerManager? = null
    private var onDeviceDisconnectStrategy: OnDeviceDisconnectStrategy? = null
    private var electionCallback: (() -> Unit)? = null
    private var serverSocketNameCallback: ((String) -> Unit)? = null

    override fun setElectionCallback(callback: () -> Unit) {
        electionCallback = callback
    }

    override fun setServerSocketNameCallback(callback: ((String) -> Unit)?) {
        serverSocketNameCallback = callback
        bluetoothServerManager?.updateServerSocketNameCallback(callback)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onElectionTimerFinished() {
        val configMessage = MessageFactory.createConfigurationMessage()
        Log.i("BluetoothHandler", "Election finished, sending configuration message")
        broadcastMessage(configMessage)
        deviceManager.setMeMaster()
        deviceManager.isUiBlocked.value = false
        electionCallback?.invoke()
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.S)
    @Synchronized
    override fun onDeviceDisconnected(socket: BluetoothSocket) {
        val deviceAddress = socket.remoteDevice.address
        if (deviceManager.isDeviceAlreadyConnected(deviceAddress)) {
            onDeviceDisconnectStrategy?.onDeviceDisconnect(socket)
            deviceManager.removeConnection(socket.remoteDevice)
        } else {
            Log.w("BluetoothHandler", "Device $deviceAddress already disconnected")
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun startBluetoothServer() {
        if (bluetoothServerManager == null) {
            bluetoothServerManager =
                BluetoothServerManager(
                    activity,
                    bluetoothManager,
                    deviceManager,
                    ::onDeviceDisconnected,
                    serverSocketNameCallback,
                )
        }
        bluetoothServerManager!!.startBluetoothServer()
    }

    override fun stopBluetoothServer() {
        Log.i("BluetoothHandler", "Stopping server")
        bluetoothServerManager?.closeConnection()
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun startBluetoothClientAndDiscovery() {
        if (bluetoothClientManager == null) {
            bluetoothClientManager =
                BluetoothClientManager(
                    activity,
                    bluetoothManager,
                    deviceManager,
                    ::onDeviceDisconnected,
                )
        }
        bluetoothClientManager?.let {
            bluetoothClientManager!!.discoveryDevices(deviceManager.getDiscoveredDevices())
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @Synchronized
    override fun connectToDevice(deviceMac: String) {
        bluetoothClientManager?.connectToDevice(deviceMac)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun getActiveManager(): BluetoothCommunicationManager? =
        if (deviceManager.getMasterDevice().value is MyDevice) {
            bluetoothServerManager
        } else {
            bluetoothClientManager
        }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun sendMessage(
        message: MessageComposed,
        device: BluetoothDevice,
    ) {
        getActiveManager()?.sendMessage(device, message)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun broadcastMessage(message: MessageComposed) {
        getActiveManager()?.broadcastMessage(message)
    }

    override fun getBluetoothAdapter(): BluetoothAdapter? = bluetoothAdapter

    @RequiresApi(Build.VERSION_CODES.S)
    override fun getDeviceManager(): BluetoothDeviceManager = deviceManager

    @Synchronized
    override fun setOnDeviceDisconnectStrategy(strategy: OnDeviceDisconnectStrategy) {
        this.onDeviceDisconnectStrategy = strategy
    }

    @Synchronized
    override fun getOnDeviceDisconnectStrategy(): OnDeviceDisconnectStrategy = onDeviceDisconnectStrategy!!

    fun getMainActivity(): MainActivity = activity
}
