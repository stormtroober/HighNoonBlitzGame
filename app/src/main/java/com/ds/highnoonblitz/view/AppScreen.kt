package com.ds.highnoonblitz.view

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ds.highnoonblitz.controller.MainController
import com.ds.highnoonblitz.view.game.Game
import com.ds.highnoonblitz.view.lobbymenu.ClientLobby
import com.ds.highnoonblitz.view.lobbymenu.LobbyMenu
import com.ds.highnoonblitz.view.lobbymenu.ServerLobby

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun AppScreen(
    activity: Activity,
    mainController: MainController,
    navController: NavHostController,
) {
    ScreenWithTopBar(activity = activity) {
        NavHost(navController = navController, startDestination = AppRoute.LOBBY_MENU.route) {
            composable(AppRoute.LOBBY_MENU.route) {
                LobbyMenu(activity, mainController.getLobbyController(), navController)
            }
            composable(AppRoute.SERVER_LOBBY.route) {
                ServerLobby(activity, mainController.getLobbyController(), navController)
            }
            composable(AppRoute.CLIENT_LOBBY.route) {
                ClientLobby(activity, mainController.getLobbyController(), navController)
            }
            composable(AppRoute.GAME_START.route) {
                Game(mainController.getGameController(), navController)
            }
        }
    }
}
