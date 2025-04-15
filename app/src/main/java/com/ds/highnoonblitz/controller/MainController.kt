package com.ds.highnoonblitz.controller

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.navigation.NavController
import com.ds.highnoonblitz.MainActivity

/**
 * MainController serves as a central coordinator for the Lobby and Game controllers.
 *
 * It initializes and manages the two controllers and sets a shared [NavController].
 *
 * @constructor Creates a MainController with the provided [MainActivity] context.
 *
 * @param activity the MainActivity instance used as context.
 */
class MainController(
    activity: MainActivity,
) {
    @RequiresApi(Build.VERSION_CODES.S)
    private val lobbyController = LobbyController(activity, ::startGame)

    @RequiresApi(Build.VERSION_CODES.S)
    private val gameController = GameController(activity)

    /**
     * Provides access to the LobbyController.
     *
     * @return the instance of [LobbyController].
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun getLobbyController(): LobbyController = lobbyController

    /**
     * Provides access to the GameController.
     *
     * @return the instance of [GameController].
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun getGameController(): GameController = gameController

    /**
     * Assigns the provided NavController to both LobbyController and GameController.
     *
     * @param navController the NavController to assign.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun setNavController(navController: NavController) {
        lobbyController.setNavController(navController)
        gameController.setNavController(navController)
    }

    /**
     * Starts the game by triggering the startGame method in the GameController.
     *
     * @see GameController.startGame
     */
    @RequiresApi(Build.VERSION_CODES.S)
    private fun startGame() {
        gameController.startGame()
    }
}
