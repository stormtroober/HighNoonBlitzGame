package com.ds.highnoonblitz.view.lobbymenu

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ds.highnoonblitz.bluetooth.handler.BluetoothHandlerProvider
import com.ds.highnoonblitz.controller.LobbyController
import com.ds.highnoonblitz.view.AppRoute
import com.ds.highnoonblitz.view.components.CustomDialog
import com.ds.highnoonblitz.view.components.DiscoveredDeviceButton
import com.ds.highnoonblitz.view.components.ParticipantsList
import com.ds.highnoonblitz.view.components.ReadyButton

@SuppressLint("MissingPermission")
@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun ClientLobby(
    activity: Activity,
    lobbyController: LobbyController,
    navController: NavController,
) {
    val deviceManager = lobbyController.deviceManager
    val showBackDialog = remember { mutableStateOf(false) }
    val isUiBlocked = deviceManager.isUiBlocked.value

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (deviceManager.isEmpty()) {
                Text(
                    text = "Discovered devices",
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                    style = MaterialTheme.typography.headlineMedium,
                )
                LazyColumn(
                    modifier = Modifier.height(150.dp),
                ) {
                    items(deviceManager.getDiscoveredDevices()) { device ->
                        val deviceName = device.name ?: "Unknown device"
                        if (deviceName != "Unknown device") {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                DiscoveredDeviceButton(buttonText = deviceName, buttonAction = {
                                    BluetoothHandlerProvider.getBluetoothHandler().connectToDevice(device.address)
                                })
                            }
                        }
                    }
                }
                Button(onClick = {
                    lobbyController.activateDiscovery()
                }) {
                    Text("Refresh discovery")
                }
            } else {
                ParticipantsList(deviceManager = deviceManager)
                ReadyButton(lobbyController)
            }
        }

        if (isUiBlocked) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color(0x80000000)),
                // Semi-transparent overlay
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(modifier = Modifier.size(50.dp))
                    Text(
                        text = "Election in Process",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
            }
        }
    }

    // Handle back press
    BackHandler(onBack = {
        if (deviceManager.isSomeoneConnected()) {
            showBackDialog.value = true
        } else {
            deviceManager.destroyLobby()
            lobbyController.stopBluetoothServer()
            navController.navigate(AppRoute.LOBBY_MENU.route)
        }
    })

    CustomDialog(
        showDialogState = showBackDialog,
        title = "Exit Lobby",
        message = "Do you want to exit the lobby?",
        onConfirm = {
            deviceManager.destroyLobby()
            lobbyController.stopBluetoothServer()
            navController.popBackStack()
            showBackDialog.value = false
            navController.navigate(AppRoute.LOBBY_MENU.route)
        },
        onDismiss = {
            showBackDialog.value = false
        },
    )
}
