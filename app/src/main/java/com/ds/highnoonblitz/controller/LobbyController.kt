package com.ds.highnoonblitz.controller

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.ds.highnoonblitz.GameConstants
import com.ds.highnoonblitz.MainActivity
import com.ds.highnoonblitz.MainThreadUtil
import com.ds.highnoonblitz.bluetooth.handler.BluetoothHandlerProvider
import com.ds.highnoonblitz.bluetooth.handler.LobbyBluetoothHandlerInterface
import com.ds.highnoonblitz.bluetooth.handler.strategies.LobbyClientOnDeviceDisconnectStrategy
import com.ds.highnoonblitz.bluetooth.management.BluetoothDeviceManager
import com.ds.highnoonblitz.bluetooth.management.ConsistencyCheckCallback
import com.ds.highnoonblitz.bluetooth.management.NetworkConsistencyChecker
import com.ds.highnoonblitz.messages.MessageFactory
import com.ds.highnoonblitz.view.AppRoute

/**
 * Controller responsible for managing lobby related operations such as
 * initiating Bluetooth server, checking network consistency, and notifying readiness.
 *
 * @property startGame lambda to be invoked once network consistency is confirmed.
 */
@RequiresApi(Build.VERSION_CODES.S)
class LobbyController(
    activity: MainActivity,
    private val startGame: () -> Unit,
) : BaseController(activity) {
    private val bluetoothHandler: LobbyBluetoothHandlerInterface = BluetoothHandlerProvider.getBluetoothHandler()
    private val _deviceManager: BluetoothDeviceManager = bluetoothHandler.getDeviceManager()

    init {
        bluetoothHandler.setOnDeviceDisconnectStrategy(LobbyClientOnDeviceDisconnectStrategy())
        bluetoothHandler.setElectionCallback {
            MainThreadUtil.runOnMainThread {
                navController.navigate(AppRoute.SERVER_LOBBY.route)
            }
        }
    }

    /**
     * Provides access to the device manager.
     */
    val deviceManager: BluetoothDeviceManager
        get() = _deviceManager

    private val consistencyCheckCallback =
        object : ConsistencyCheckCallback {
            /**
             * Callback invoked when network consistency check is finished.
             *
             * @param isConsistent indicates whether the network is consistent.
             */
            @RequiresApi(Build.VERSION_CODES.S)
            override fun onConsistencyCheckFinished(isConsistent: Boolean) {
                if (isConsistent) {
                    informClientsAndStartGame()
                } else {
                    MainThreadUtil.makeToast("Network is not consistent. Try to do lobby again.")
                }
            }
        }

    private val networkConsistencyChecker = NetworkConsistencyChecker(consistencyCheckCallback, false)

    /**
     * Informs all clients and starts the game by navigating to the game start route,
     * broadcasting a game start message, and invoking the startGame lambda.
     */
    private fun informClientsAndStartGame() {
        MainThreadUtil.runOnMainThread {
            navController.navigate(AppRoute.GAME_START.route)
        }
        bluetoothHandler.broadcastMessage(MessageFactory.createGameStartMessage())
        startGame()
    }

    /**
     * Starts the network consistency check using the list of ready device addresses.
     *
     * The check broadcasts a consistency check request message.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun startConsistencyCheck() {
        val clients =
            deviceManager
                .getReadyDevices()
                .value
                .map { it.getDevice().address }
                .toMutableSet()
        networkConsistencyChecker.startChecking(GameConstants.CONSISTENCY_CHECK_TIMEOUT, clients)
        val message = MessageFactory.createConsistencyCheckRequestMessage()
        bluetoothHandler.broadcastMessage(message)
    }

    /**
     * Sets the current device as master and starts the Bluetooth server in lobby as server mode.
     *
     * @param onServerSocketCreated optional callback to update server socket name.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun startLobbyAsServer(onServerSocketCreated: ((String) -> Unit)?) {
        deviceManager.setMeMaster()
        startBluetoothServer(onServerSocketCreated)
    }

    /**
     * Starts the Bluetooth server in lobby mode.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun startLobby() {
        startBluetoothServer(null)
    }

    /**
     * Stops the active Bluetooth server.
     */
    fun stopBluetoothServer() {
        bluetoothHandler.stopBluetoothServer()
    }

    /**
     * Initializes and starts the Bluetooth server with the provided server socket name callback,
     * then activates device discovery.
     *
     * @param onServerSocketCreated optional callback to receive server socket name.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    private fun startBluetoothServer(onServerSocketCreated: ((String) -> Unit)?) {
        bluetoothHandler.setServerSocketNameCallback(onServerSocketCreated)
        bluetoothHandler.startBluetoothServer()
        activateDiscovery()
    }

    /**
     * Starts the Bluetooth client and device discovery process.
     */
    fun activateDiscovery() {
        bluetoothHandler.startBluetoothClientAndDiscovery()
    }

    /**
     * Notifies the readiness status of the device by broadcasting a ready or lobby message.
     *
     * @param isReady indicates whether the device is ready.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun notifyReadiness(isReady: Boolean) {
        Log.i("LobbyController", "Notifying readiness: $isReady")
        val msg = MessageFactory.createReadyMessage(isReady)
        bluetoothHandler.broadcastMessage(msg)
        if (isReady) {
            deviceManager.imReady()
        } else {
            deviceManager.imInLobby()
        }
    }

    /**
     * Retrieves the network consistency checker.
     *
     * @return the instance of [NetworkConsistencyChecker].
     */
    fun getNetworkConsistencyChecker(): NetworkConsistencyChecker = networkConsistencyChecker
}
