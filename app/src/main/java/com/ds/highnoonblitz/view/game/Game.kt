package com.ds.highnoonblitz.view.game

import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ds.highnoonblitz.controller.GameController
import com.ds.highnoonblitz.model.GameStatus
import com.ds.highnoonblitz.view.components.GameResults
import com.ds.highnoonblitz.view.components.InGameList
import kotlin.random.Random

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun Game(
    gameController: GameController,
    navController: NavHostController,
) {
    val deviceManager = gameController.deviceManager

    val isButtonPressed =
        remember {
            mutableStateOf(false)
        }

    val gameStatus = gameController.getGameStatus().value

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text =
                when (gameStatus) {
                    GameStatus.NOT_STARTED -> "Game not started"
                    GameStatus.STARTED -> "Game started"
                    GameStatus.FINISHED_NORMAL -> "Game finished"
                    GameStatus.FINISHED_TIMEOUT -> "Game timeout finished"
                },
            modifier =
                Modifier
                    .padding(top = 12.dp, bottom = 4.dp),
            style = MaterialTheme.typography.headlineMedium,
        )

        BoxWithConstraints(modifier = Modifier.height(250.dp)) {
            val buttonSizeX = 80.dp
            val buttonSizeY = 50.dp
            val maxButtonX = (maxWidth / 2) - buttonSizeX
            val maxButtonY = maxHeight - buttonSizeY

            val buttonPosition =
                if (gameStatus == GameStatus.STARTED) {
                    Pair(
                        Random.nextInt(-maxButtonX.value.toInt(), maxButtonX.value.toInt()).dp,
                        Random.nextInt(0, maxButtonY.value.toInt()).dp,
                    )
                } else {
                    Pair(0.dp, 0.dp)
                }

            Button(
                modifier =
                    Modifier
                        .width(buttonSizeX)
                        .height(buttonSizeY)
                        .offset(x = buttonPosition.first, y = buttonPosition.second),
                onClick = {
                    if (!isButtonPressed.value) {
                        gameController.buttonGamePressed()
                        isButtonPressed.value = true
                    }
                },
                enabled = gameStatus == GameStatus.STARTED && !isButtonPressed.value,
            ) {
                Text(text = "HNB")
            }
        }

        InGameList(deviceManager)

        if (gameStatus == GameStatus.FINISHED_NORMAL) {
            GameResults(gameResults = gameController.getGameResults())
        }

        Button(
            onClick = {
                val returnRoute = gameController.backToLobby()
                if (returnRoute != null) {
                    Log.i("Game", "Navigating to ${returnRoute.route}")
                    navController.navigate(returnRoute.route)
                } else {
                    Log.i("Game", "No route to navigate to")
                    navController.popBackStack()
                    navController.popBackStack()
                }
            },
            enabled =
                (gameStatus == GameStatus.FINISHED_NORMAL || gameStatus == GameStatus.FINISHED_TIMEOUT) &&
                    gameController.getBlockLobbyForElection().value.not(),
        ) {
            Text("Back to lobby")
        }
    }

    BackHandler(onBack = {
        return@BackHandler
    })
}
