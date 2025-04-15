package com.ds.highnoonblitz.view.lobbymenu

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.ds.highnoonblitz.R
import com.ds.highnoonblitz.bluetooth.handler.BluetoothHandlerProvider
import com.ds.highnoonblitz.bluetooth.handler.strategies.LobbyClientOnDeviceDisconnectStrategy
import com.ds.highnoonblitz.controller.LobbyController
import com.ds.highnoonblitz.view.AppRoute
import com.ds.highnoonblitz.view.components.MenuButton

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun LobbyMenu(
    activity: Activity,
    lobbyController: LobbyController,
    navController: NavHostController,
) {
    val resources = activity.resources
    Column {
        MenuButton(
            buttonText = resources.getString(R.string.create_lobby),
            buttonAction = {
                BluetoothHandlerProvider.getBluetoothHandler().setOnDeviceDisconnectStrategy(LobbyClientOnDeviceDisconnectStrategy())
                navController.navigate(AppRoute.SERVER_LOBBY.route)
            },
        )
        MenuButton(
            buttonText = resources.getString(R.string.join_lobby),
            buttonAction = {
                BluetoothHandlerProvider.getBluetoothHandler().setOnDeviceDisconnectStrategy(LobbyClientOnDeviceDisconnectStrategy())
                navController.navigate(AppRoute.CLIENT_LOBBY.route)
                lobbyController.startLobby()
            },
        )
    }
}
