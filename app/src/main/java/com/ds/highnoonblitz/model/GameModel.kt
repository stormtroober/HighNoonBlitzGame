package com.ds.highnoonblitz.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList

class GameModel {
    private var gameState: MutableState<GameStatus> = mutableStateOf(GameStatus.NOT_STARTED)
    private var gameResults: SnapshotStateList<Pair<String, Long>> = mutableStateListOf()
    private var _endGameButtonPressed = false
    private var _backgroundElection = false
    private var _amICoordinator = false
    val blockBackToLobbyForElection = mutableStateOf(false)

    var amICoordinator: Boolean
        get() = _amICoordinator
        set(value) {
            _amICoordinator = value
        }

    var backgroundElection: Boolean
        get() = _backgroundElection
        set(value) {
            _backgroundElection = value
        }

    var endGameButtonPressed: Boolean
        get() = _endGameButtonPressed
        set(value) {
            _endGameButtonPressed = value
        }

    fun getGameState(): MutableState<GameStatus> = gameState

    fun setGameState(newState: GameStatus): MutableState<GameStatus> {
        gameState.value = newState
        return gameState
    }

    fun getGameResults(): SnapshotStateList<Pair<String, Long>> = gameResults

    fun addGameResult(result: Pair<String, Long>): SnapshotStateList<Pair<String, Long>> {
        gameResults.add(result)
        return gameResults
    }

    fun clearGameResults(): SnapshotStateList<Pair<String, Long>> {
        setGameState(GameStatus.NOT_STARTED)
        gameResults.clear()
        return gameResults
    }
}
