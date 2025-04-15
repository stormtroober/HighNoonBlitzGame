package com.ds.highnoonblitz.messages

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.annotation.RequiresApi
import com.ds.highnoonblitz.bluetooth.handler.BluetoothHandlerProvider
import com.ds.highnoonblitz.controller.MainController
import com.ds.highnoonblitz.model.DeviceStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

@RequiresApi(Build.VERSION_CODES.S)
class MessageHandler(
    private val mainController: MainController,
) : Handler(
        Looper.getMainLooper(),
    ) {
    private val bluetoothHandler = BluetoothHandlerProvider.getBluetoothHandler()
    private val backgroundScope = CoroutineScope(Dispatchers.IO)

    @RequiresApi(Build.VERSION_CODES.S)
    override fun handleMessage(msg: Message) {
        val (receivedBytes, senderMacAddress) = msg.obj as Pair<ByteArray, String?>
        val receivedString = String(receivedBytes)
        when (msg.what) {
            Purposes.UPDATE_CONNECTED_LIST.value -> {
                backgroundScope.launch {
                    val obj = JSONObject(receivedString)
                    bluetoothHandler.connectToDevice(obj.getString("deviceMac"))
                }
            }

            Purposes.READY.value -> {
                backgroundScope.launch {
                    val obj = JSONObject(receivedString)
                    Log.i("Client", "Received ready message: $obj")
                    val isReady = obj.getBoolean("ready")
                    if (senderMacAddress != null) {
                        bluetoothHandler.getDeviceManager().updateDeviceStatus(
                            senderMacAddress,
                            if (isReady) DeviceStatus.READY else DeviceStatus.IN_LOBBY,
                        )
                    }
                }
            }

            Purposes.CONFIGURATION.value -> {
                backgroundScope.launch {
                    val obj = JSONObject(receivedString)
                    if (obj.has("devices")) {
                        val devices = obj.getJSONArray("devices")
                        Log.i(
                            "Client",
                            "Received configuration message: $devices from $senderMacAddress",
                        )
                        if (senderMacAddress != null) {
                            bluetoothHandler.getDeviceManager().setMasterDevice(senderMacAddress)
                        }
                        for (i in 0 until devices.length()) {
                            bluetoothHandler.connectToDevice(devices.getString(i))
                        }
                    }
                }
            }

            Purposes.INFO_SHARING.value -> {
                backgroundScope.launch {
                    val obj = JSONObject(receivedString)
                    Log.i("Client", "*************************************************************")

                    Log.i("Client", "Received info sharing message: $obj")
                    if (obj.has("UUID")) {
                        val uuid = obj.getString("UUID")
                        Log.i("Client", "Received UUID: $uuid")
                        if (senderMacAddress != null) {
                            bluetoothHandler
                                .getDeviceManager()
                                .addElectionListMember(senderMacAddress, uuid)
                        }
                    }
                    if (obj.has("masterAddress")) {
                        val masterAddress = obj.getString("masterAddress")
                        if (masterAddress.isNotEmpty()) {
                            Log.i("Client", "Received master address: $masterAddress")
                            bluetoothHandler.connectToDevice(masterAddress)
                        }
                    }
                    if (obj.has("status")) {
                        val status = obj.getBoolean("status")
                        if (senderMacAddress != null) {
                            bluetoothHandler.getDeviceManager().updateDeviceStatus(
                                senderMacAddress,
                                if (status) DeviceStatus.READY else DeviceStatus.IN_LOBBY,
                            )
                        }
                    }
                    Log.i("Client", "*************************************************************")
                }
            }

            Purposes.ELECTION_REQUEST.value -> {
                backgroundScope.launch {
                    bluetoothHandler.getDeviceManager().isUiBlocked.value = true
                    val obj = JSONObject(receivedString)
                    Log.i("Client", "Received election request: $obj")
                    val message = MessageFactory.createElectionAcknowledgeMessage()
                    val device =
                        bluetoothHandler.getBluetoothAdapter()?.getRemoteDevice(senderMacAddress)
                    if (device != null) {
                        bluetoothHandler.sendMessage(message, device)
                    }
                }
            }

            Purposes.ELECTION_ACK.value -> {
                Log.i("Client", "Received election ACK")
                backgroundScope.launch {
                    bluetoothHandler.getDeviceManager().stopElection()
                }
            }

            Purposes.CONSISTENCY_CHECK_REQUEST.value -> {
                // MainThreadUtil.makeToast("Received consistency check request")
                backgroundScope.launch {
                    val macAddresses =
                        bluetoothHandler
                            .getDeviceManager()
                            .getReadyDevices()
                            .value
                            .map { it.getDevice().address }
                    val message = MessageFactory.createConsistencyCheckReplyMessage(macAddresses)
                    val device =
                        bluetoothHandler.getBluetoothAdapter()?.getRemoteDevice(senderMacAddress)
                    if (device != null) {
                        bluetoothHandler.sendMessage(message, device)
                    }
                }
            }

            Purposes.CONSISTENCY_CHECK_REPLY.value -> {
                backgroundScope.launch {
                    val obj = JSONObject(receivedString)
                    try {
                        val macAddresses = obj.getJSONArray("macAddresses")
                        // Log.i("MessageHandler", "Received consistency check reply: $macAddresses")
                        val macAddressesSet = mutableSetOf<String>()
                        for (i in 0 until macAddresses.length()) {
                            macAddressesSet.add(macAddresses.getString(i))
                        }
                        mainController.getLobbyController().getNetworkConsistencyChecker().addClientConnections(
                            senderMacAddress!!,
                            macAddressesSet,
                        )
                    } catch (e: Exception) {
                        Log.e("MessageHandler", "Error while parsing consistency check reply", e)
                    }
                }
            }

            Purposes.GAME_START.value -> {
                // Navigation needs to go on the main thread
                mainController.getGameController().navController.navigate("gameStart")
                backgroundScope.launch {
                    mainController.getGameController().startGame()
                }
            }

            Purposes.BACK_TO_LOBBY_GAMELIST.value -> {
                backgroundScope.launch {
                    bluetoothHandler.getDeviceManager().updateDeviceStatus(senderMacAddress!!, DeviceStatus.IN_LOBBY)
                }
            }

            Purposes.ELECTION_INGAME_FINISHED.value -> {
                backgroundScope.launch {
                    mainController.getGameController().getBlockLobbyForElection().value = false
                }
            }

            Purposes.END_GAME.value -> {
                backgroundScope.launch {
                    val obj = JSONObject(receivedString)
                    val timeDifference = obj.getLong("timeDifference")
                    val device = bluetoothHandler.getBluetoothAdapter()?.getRemoteDevice(senderMacAddress!!)
                    if (device != null) {
                        mainController.getGameController().endGameForDevice(device, timeDifference)
                    }
                }
            }
        }
    }
}
