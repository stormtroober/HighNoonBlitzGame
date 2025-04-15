package com.ds.highnoonblitz.model

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.ds.highnoonblitz.messages.MessageComposed

class Lobby {
    private var masterDevice: MutableState<Device?> = mutableStateOf(null)
    private val connectedDevices = mutableStateListOf<ExternalDevice>()
    var status = LobbyStatus.CREATED

    val inGameDevices: State<List<ExternalDevice>> = derivedStateOf {
        connectedDevices.filter { it.getStatus() == DeviceStatus.IN_GAME }
    }

    val readyDevices: State<List<ExternalDevice>> = derivedStateOf {
        connectedDevices.filter { it.getStatus() == DeviceStatus.READY }
    }

    val disconnectedDevices: State<List<ExternalDevice>> = derivedStateOf {
        connectedDevices.filter { it.getStatus() == DeviceStatus.DISCONNECTED }
    }

    val inLobbyDevices: State<List<ExternalDevice>> = derivedStateOf {
        connectedDevices.filter { it.getStatus() == DeviceStatus.IN_LOBBY }
    }

    fun getMasterDevice(): MutableState<out Device?> {
        return masterDevice
    }

    fun setMasterDevice(device: ExternalDevice) {
        masterDevice.value = device
    }

    fun setMasterDevice(device: MyDevice) {
        masterDevice.value = device
    }

    fun getConnectedDevices(): SnapshotStateList<ExternalDevice> {
        return connectedDevices
    }

    fun addConnectedDevice(device: ExternalDevice) {
        if (connectedDevices.none { it.getDevice().address == device.getDevice().address }) {
            device.setStatus(DeviceStatus.IN_LOBBY)
            connectedDevices.add(device)
        }
    }


    fun removeConnectedDevice(device: BluetoothDevice) {
        val deviceToRemove = connectedDevices.find { it.getDevice().address == device.address }
        if (deviceToRemove != null) {
            connectedDevices.remove(deviceToRemove)
            deviceToRemove.closeSocket()
        } else {
            Log.w("Lobby", "Device ${device.address} is already removed or not found")
        }
    }

    fun isReadyToStart(): Boolean {
        return connectedDevices.isNotEmpty() && connectedDevices.map { it.ready() }.all { it }
    }

    fun clearConnectedDevices() {
        connectedDevices.forEach { it.closeSocket() }
        connectedDevices.clear()
    }

    fun destroy() {
        connectedDevices.forEach { it.destroy() }
        connectedDevices.clear()
    }

    fun removeMasterDevice() {
        masterDevice.value = null
    }

    fun deviceStatusUpdate(deviceMac: String, status: DeviceStatus) {
        connectedDevices.find { it.getDevice().address == deviceMac }?.setStatus(status)
    }

    fun getDeviceByMac(deviceMac: String): ExternalDevice? {
        return connectedDevices.find { it.getDevice().address == deviceMac }
    }

    fun startGame() {
        status = LobbyStatus.STARTED
        connectedDevices.forEach { it.setStatus(DeviceStatus.IN_GAME) }
    }

    fun sendMessage(deviceMac: String, message: MessageComposed) {
        connectedDevices.find { it.getDevice().address == deviceMac }?.send(message)
    }

    fun sendBroadcastByStatus(message: MessageComposed, me: MyDevice) {
        val targetDevices = when (me.getStatus()) {
            DeviceStatus.IN_GAME -> inGameDevices.value
            DeviceStatus.READY -> readyDevices.value
            DeviceStatus.DISCONNECTED -> disconnectedDevices.value
            DeviceStatus.IN_LOBBY -> inLobbyDevices.value
            else -> emptyList()
        }
        targetDevices.forEach { it.send(message) }
    }

    fun sendBroadcast(message: MessageComposed) {
        connectedDevices.filter { it.getStatus() != DeviceStatus.DISCONNECTED }.forEach { it.send(message) }
    }

    fun endGame() {
        status = LobbyStatus.CREATED
    }

}