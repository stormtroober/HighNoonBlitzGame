package com.ds.highnoonblitz.view.lobbymenu

import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.ds.highnoonblitz.GameConstants
import com.ds.highnoonblitz.controller.LobbyController
import com.ds.highnoonblitz.view.AppRoute
import com.ds.highnoonblitz.view.components.CustomDialog
import com.ds.highnoonblitz.view.components.MenuButton
import com.ds.highnoonblitz.view.components.ParticipantsList
import com.ds.highnoonblitz.view.components.StartButton
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun ServerLobby(
    activity: Activity,
    lobbyController: LobbyController,
    navController: NavController,
) {
    val deviceManager = lobbyController.deviceManager
    val showBackDialog = remember { mutableStateOf(false) }
    val discoverableName = remember { mutableStateOf("") }
    val displayDiscoverable = remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MenuButton(buttonText = "Be Discoverable as Server", buttonAction = {
            lobbyController.startLobbyAsServer { name ->
                discoverableName.value = name
            }
        })

        if (discoverableName.value.isNotEmpty()) {
            LaunchedEffect(discoverableName.value) {
                displayDiscoverable.value = "Discoverable as: ${discoverableName.value}"
                delay(GameConstants.DISCOVERABLE_TIME * 1000) // Delay for 5 seconds
                displayDiscoverable.value = "Discoverable period is over."
                discoverableName.value = ""
            }
        }

        if (displayDiscoverable.value.isNotEmpty()) {
            Text(displayDiscoverable.value)
        }

        ParticipantsList(deviceManager)

        StartButton(buttonText = "Start Game", buttonAction = {
            Log.i("ServerLobby", "Checking consistency of the network")
            lobbyController.startConsistencyCheck()
        }, deviceManager)
    }

    // Handle back press
    BackHandler(onBack = {
        if (deviceManager.isSomeoneConnected()) {
            showBackDialog.value = true
        } else {
            deviceManager.destroyLobby()
            lobbyController.stopBluetoothServer()
            navController.popBackStack()
        }
    })

    CustomDialog(
        showDialogState = showBackDialog,
        title = "Exit Lobby",
        message = "Do you want to exit the lobby?",
        onConfirm = {
            deviceManager.destroyLobby()
            lobbyController.stopBluetoothServer()
            navController.navigate(AppRoute.LOBBY_MENU.route)
        },
        onDismiss = {
            showBackDialog.value = false
        },
    )
}
