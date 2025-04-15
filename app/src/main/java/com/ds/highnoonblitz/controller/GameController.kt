package com.ds.highnoonblitz.controller

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.ds.highnoonblitz.GameConstants
import com.ds.highnoonblitz.MainActivity
import com.ds.highnoonblitz.MainThreadUtil
import com.ds.highnoonblitz.bluetooth.handler.BluetoothHandlerInterface
import com.ds.highnoonblitz.bluetooth.handler.BluetoothHandlerProvider
import com.ds.highnoonblitz.bluetooth.handler.strategies.GameOnDeviceDisconnectStrategy
import com.ds.highnoonblitz.bluetooth.handler.strategies.LobbyClientOnDeviceDisconnectStrategy
import com.ds.highnoonblitz.bluetooth.management.BluetoothDeviceManager
import com.ds.highnoonblitz.messages.MessageFactory
import com.ds.highnoonblitz.model.DeviceStatus
import com.ds.highnoonblitz.model.GameModel
import com.ds.highnoonblitz.model.GameStatus
import com.ds.highnoonblitz.view.AppRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Manages game-related operations including starting a game, handling timers,
 * processing game events, and coordinating Bluetooth communications.
 *
 * @constructor Creates a GameController.
 * @param activity the MainActivity instance used as context.
 */
@RequiresApi(Build.VERSION_CODES.S)
class GameController(
    activity: MainActivity,
) : BaseController(activity) {
    /**
     * Bluetooth handler used for sending and receiving messages.
     */
    private val bluetoothHandler: BluetoothHandlerInterface = BluetoothHandlerProvider.getBluetoothHandler()

    /**
     * Device manager handling Bluetooth device operations.
     */
    private val _deviceManager: BluetoothDeviceManager = bluetoothHandler.getDeviceManager()

    /**
     * Model holding game state and results.
     */
    private val gameModel = GameModel()

    private var jobForButton: Job? = null
    private var jobEndGame: Job? = null
    private var delayValue = 0L
    private var timerStartedValue = 0L

    /**
     * Provides access to the device manager.
     */
    val deviceManager: BluetoothDeviceManager
        get() = _deviceManager

    /**
     * Retrieves the mutable state for blocking the lobby during an election.
     *
     * @return a [MutableState] of [Boolean] representing the block status.
     */
    fun getBlockLobbyForElection(): MutableState<Boolean> = gameModel.blockBackToLobbyForElection

    /**
     * Marks the current device as coordinator, sending a broadcast message to unlock the UI.
     */
    fun iAmCoordinator() {
        gameModel.amICoordinator = true
        gameModel.blockBackToLobbyForElection.value = false
        val message = MessageFactory.createElectionInGameFinishedMessage()
        CoroutineScope(Dispatchers.IO).launch {
            Log.i("Game", "I'm the coordinator, sending message to unlock the UI")
            bluetoothHandler.broadcastMessage(message)
        }
    }

    /**
     * Notifies the ongoing election by setting background election state
     * and blocking the UI to return to lobby.
     */
    fun electionOnGoing() {
        gameModel.backgroundElection = true
        gameModel.blockBackToLobbyForElection.value = true
    }

    /**
     * Starts the game by updating device statuses, setting disconnect strategies,
     * and initializing timers.
     *
     * @throws IllegalStateException if Bluetooth operations have issues.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun startGame() {
        gameModel.backgroundElection = false
        gameModel.amICoordinator = false
        bluetoothHandler.setOnDeviceDisconnectStrategy(GameOnDeviceDisconnectStrategy())
        gameModel.endGameButtonPressed = false
        deviceManager.startGame()
        startButtonTimer()
        waitForAllMessagesOrTimeout()
    }

    /**
     * Retrieves the list of game results.
     *
     * @return a SnapshotStateList containing device names and their associated time differences.
     */
    fun getGameResults(): SnapshotStateList<Pair<String, Long>> = gameModel.getGameResults()

    /**
     * Initializes the timer to delay the start of game button activation.
     *
     * Uses a random delay between minimum and maximum values defined in GameConstants.
     */
    private fun startButtonTimer() {
        delayValue =
            Random.nextLong(
                GameConstants.BUTTON_TIMER_MIN_DELAY,
                GameConstants.BUTTON_TIMER_MAX_DELAY,
            )
        jobForButton =
            CoroutineScope(Dispatchers.IO).launch {
                delay(delayValue)
                timerStartedValue = System.currentTimeMillis()
                gameModel.setGameState(GameStatus.STARTED)
            }
    }

    /**
     * Waits until all connected devices are in a waiting state or the game timeout is reached.
     *
     * If the game ends normally, game state is updated accordingly; otherwise, a timeout state is set.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    private fun waitForAllMessagesOrTimeout() {
        jobEndGame =
            CoroutineScope(Dispatchers.IO).launch {
                val startTime = System.currentTimeMillis()
                while (System.currentTimeMillis() - startTime < GameConstants.GAME_TIMEOUT) {
                    delay(GameConstants.CHECK_END_GAME) // check if the game ended
                    val gameList = deviceManager.getInGameDevices().value
                    if (gameList.isEmpty() && gameModel.endGameButtonPressed) {
                        gameModel.setGameState(GameStatus.FINISHED_NORMAL)
                        return@launch
                    }
                }
                gameModel.setGameState(GameStatus.FINISHED_TIMEOUT)
            }
    }

    /**
     * Ends the game for a specific device.
     *
     * Updates the device status to waiting and adds its game result.
     *
     * @param device the BluetoothDevice for which the game is ending.
     * @param timeDifference the time difference recorded.
     */
    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.S)
    fun endGameForDevice(
        device: BluetoothDevice,
        timeDifference: Long,
    ) {
        deviceManager.updateDeviceStatus(device.address, DeviceStatus.IN_GAME_WAITING)
        gameModel.addGameResult(Pair(device.name, timeDifference))
    }

    /**
     * Processes the event when the game button is pressed.
     *
     * Sets the device state to waiting, computes the elapsed time, records the result,
     * and broadcasts an end game message.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun buttonGamePressed() {
        gameModel.endGameButtonPressed = true
        deviceManager.imWaiting()
        val timerFinishedValue = System.currentTimeMillis()
        val timeDifference = timerFinishedValue - timerStartedValue
        gameModel.addGameResult(Pair("Me", timeDifference))
        bluetoothHandler.broadcastMessage(MessageFactory.createEndGameMessage(timeDifference))
    }

    /**
     * Retrieves the current game status as a mutable state.
     *
     * @return a [MutableState] representing the current [GameStatus].
     */
    fun getGameStatus(): MutableState<GameStatus> = gameModel.getGameState()

    /**
     * Navigates back to the lobby, setting appropriate disconnect strategies,
     * broadcasting a back-to-lobby message, and clearing game results.
     *
     * @return an [AppRoute] indicating the lobby route to navigate, or null.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun backToLobby(): AppRoute? {
        bluetoothHandler.setElectionCallback {
            MainThreadUtil.runOnMainThread {
                navController.navigate(AppRoute.SERVER_LOBBY.route)
            }
        }
        bluetoothHandler.setOnDeviceDisconnectStrategy(LobbyClientOnDeviceDisconnectStrategy())
        bluetoothHandler.broadcastMessage(MessageFactory.createBackToLobbyGameListMessage())
        deviceManager.endGame()
        jobEndGame?.cancel()
        jobForButton?.cancel()
        gameModel.clearGameResults()

        return if (!gameModel.backgroundElection) {
            null
        } else {
            if (gameModel.amICoordinator) {
                AppRoute.SERVER_LOBBY
            } else {
                AppRoute.CLIENT_LOBBY
            }
        }
    }
}
