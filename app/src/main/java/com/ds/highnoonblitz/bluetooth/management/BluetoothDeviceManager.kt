package com.ds.highnoonblitz.bluetooth.management

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.ds.highnoonblitz.bluetooth.communicationthreads.CommunicationChannelThread
import com.ds.highnoonblitz.leaderelection.BullyElectionManager
import com.ds.highnoonblitz.messages.MessageComposed
import com.ds.highnoonblitz.model.Device
import com.ds.highnoonblitz.model.DeviceStatus
import com.ds.highnoonblitz.model.ExternalDevice
import com.ds.highnoonblitz.model.Lobby
import com.ds.highnoonblitz.model.LobbyStatus
import com.ds.highnoonblitz.model.MyDevice

/**
 * Manages Bluetooth device connections and state.
 *
 * This manager handles discovered devices, connected devices,
 * lobby operations, and leader election integration.
 *
 * @property electionManager the BullyElectionManager used for leader elections.
 */
class BluetoothDeviceManager(
    private val electionManager: BullyElectionManager,
) {
    private val discoveredDevices = mutableStateListOf<BluetoothDevice>()
    private val lobby = Lobby()
    val me = MyDevice()
    val isUiBlocked = mutableStateOf(false)

    /**
     * Retrieves the list of discovered Bluetooth devices.
     *
     * @return a SnapshotStateList of discovered [BluetoothDevice]s.
     */
    fun getDiscoveredDevices(): SnapshotStateList<BluetoothDevice> = discoveredDevices

    /**
     * Retrieves the list of in-game devices.
     *
     * @return a State list of [ExternalDevice]s that are in the game.
     */
    fun getInGameDevices(): State<List<ExternalDevice>> = lobby.inGameDevices

    /**
     * Retrieves the list of ready devices.
     *
     * @return a State list of [ExternalDevice]s that are ready.
     */
    fun getReadyDevices(): State<List<ExternalDevice>> = lobby.readyDevices

    /**
     * Retrieves the list of currently connected devices.
     *
     * @return a SnapshotStateList of connected [ExternalDevice]s.
     */
    fun getConnectedDevices(): SnapshotStateList<ExternalDevice> = lobby.getConnectedDevices()

    /**
     * Updates the connection status of a device.
     *
     * @param deviceMac the MAC address of the target device.
     * @param status the new [DeviceStatus] to update.
     */
    fun updateDeviceStatus(
        deviceMac: String,
        status: DeviceStatus,
    ) {
        lobby.deviceStatusUpdate(deviceMac, status)
    }

    /**
     * Checks if the lobby is ready to start the game.
     *
     * @return true if ready, false otherwise.
     */
    fun isLobbyReady(): Boolean = lobby.isReadyToStart()

    /**
     * Removes the connection of the specified Bluetooth device.
     *
     * If the device is connected, it is removed from the lobby and the election list.
     *
     * @param device the [BluetoothDevice] to remove.
     */
    @Synchronized
    fun removeConnection(device: BluetoothDevice) {
        if (isDeviceAlreadyConnected(device.address)) {
            lobby.removeConnectedDevice(device)
            electionManager.removeElectionListMember(device.address)
        } else {
            Log.w("BluetoothDeviceManager", "Device ${device.address} already removed")
        }
    }

    /**
     * Destroys the lobby, resets the current device and clears the election list.
     */
    fun destroyLobby() {
        lobby.destroy()
        lobby.removeMasterDevice()
        me.setStatus(DeviceStatus.NOT_INITIALIZED)
        lobby.status = LobbyStatus.CREATED
        electionManager.clearElectionList()
    }

    /**
     * Checks if a device with the specified MAC address is already connected.
     *
     * @param deviceMac the MAC address of the target device.
     * @return true if the device is connected, false otherwise.
     */
    fun isDeviceAlreadyConnected(deviceMac: String): Boolean = lobby.getDeviceByMac(deviceMac) != null

    /**
     * Adds a new connection with the specified socket and communication channel.
     *
     * @param socket the [BluetoothSocket] established with the device.
     * @param communicationChannelThread the thread handling communication.
     */
    fun addNewConnection(
        socket: BluetoothSocket,
        communicationChannelThread: CommunicationChannelThread,
    ) {
        lobby.addConnectedDevice(ExternalDevice(socket, communicationChannelThread))
    }

    /**
     * Adds a device to the election list.
     *
     * @param deviceAddress the device MAC address.
     * @param uuid the unique identifier for the device.
     */
    fun addElectionListMember(
        deviceAddress: String,
        uuid: String,
    ) {
        electionManager.addElectionListMember(deviceAddress, uuid)
    }

    /**
     * Starts the leader election process.
     *
     * Blocks the UI and starts the election timer.
     */
    fun startElection() {
        isUiBlocked.value = true
        electionManager.startElectionTimer()
    }

    /**
     * Stops the leader election process.
     *
     * Unblocks the UI and stops the election timer.
     */
    fun stopElection() {
        electionManager.stopElectionTimer()
        isUiBlocked.value = false
    }

    /**
     * Retrieves the current election list.
     *
     * @return a list of device MAC addresses participating in the election.
     */
    fun getElectionList(): List<String> = electionManager.getDevicesToElect()

    /**
     * Retrieves the master device as a mutable state.
     *
     * @return a MutableState holding the current master [Device] or null.
     */
    fun getMasterDevice(): MutableState<out Device?> = lobby.getMasterDevice()

    /**
     * Sets the current device as the master.
     *
     * Updates the device status and assigns the master in the lobby.
     */
    fun setMeMaster() {
        me.setStatus(DeviceStatus.IN_LOBBY)
        lobby.setMasterDevice(me)
    }

    /**
     * Sets the master device using the specified MAC address.
     *
     * Updates the status of the local device if necessary.
     *
     * @param deviceMac the MAC address of the new master device.
     */
    fun setMasterDevice(deviceMac: String) {
        Log.i("BluetoothDeviceManager", "Setting master device to $deviceMac")
        if (me.getStatus() == DeviceStatus.NOT_INITIALIZED) {
            me.setStatus(DeviceStatus.IN_LOBBY)
        }
        lobby.setMasterDevice(lobby.getDeviceByMac(deviceMac)!!)
        Log.i("BluetoothDeviceManager", "My device status is ${me.getStatus()}")
    }

    /**
     * Checks if there is at least one connected device.
     *
     * @return true if at least one device is connected, false otherwise.
     */
    fun isSomeoneConnected(): Boolean = lobby.getConnectedDevices().isNotEmpty()

    /**
     * Checks if there are no connected devices.
     *
     * @return true if no devices are connected, false otherwise.
     */
    fun isEmpty(): Boolean = lobby.getConnectedDevices().isEmpty()

    /**
     * Starts the game in the lobby.
     *
     * Updates the local device status to in-game.
     */
    fun startGame() {
        lobby.startGame()
        me.setStatus(DeviceStatus.IN_GAME)
    }

    /**
     * Ends the game in the lobby.
     *
     * Updates the local device status to in-lobby.
     */
    fun endGame() {
        lobby.endGame()
        me.setStatus(DeviceStatus.IN_LOBBY)
    }

    /**
     * Marks the local device as ready.
     *
     * Logs the caller's information before updating the status.
     */
    fun imReady() {
        val stackTrace = Thread.currentThread().stackTrace
        val caller = stackTrace[3] // Adjust the index if necessary
        Log.i("BluetoothDeviceManager", "Called by ${caller.className}.${caller.methodName} at line ${caller.lineNumber}")
        me.setStatus(DeviceStatus.READY)
    }

    /**
     * Marks the local device as waiting in game.
     */
    fun imWaiting() {
        me.setStatus(DeviceStatus.IN_GAME_WAITING)
    }

    /**
     * Checks if the local device is ready.
     *
     * @return true if ready, false otherwise.
     */
    fun amIReady(): Boolean = me.ready()

    /**
     * Updates the status of the local device to in-lobby.
     */
    fun imInLobby() {
        me.setStatus(DeviceStatus.IN_LOBBY)
    }

    /**
     * Sends a broadcast message to all devices in the lobby.
     *
     * @param message the [MessageComposed] containing message details.
     */
    fun sendBroadcastMessage(message: MessageComposed) {
        lobby.sendBroadcast(message)
    }

    /**
     * Sends a targeted message to a specific device.
     *
     * @param deviceMac the MAC address of the target device.
     * @param message the [MessageComposed] containing message details.
     */
    fun sendMessage(
        deviceMac: String,
        message: MessageComposed,
    ) {
        lobby.sendMessage(deviceMac, message)
    }
}
