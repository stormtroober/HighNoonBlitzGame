package it.unibo.model

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.ds.highnoonblitz.model.GameModel
import com.ds.highnoonblitz.model.GameStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InGameModelTest {
    private lateinit var gameModel: GameModel

    @Before
    fun setUp() {
        gameModel = GameModel()
    }

    @Test
    fun testInitialGameState() {
        assertEquals(GameStatus.NOT_STARTED, gameModel.getGameState().value)
    }

    @Test
    fun testSetGameState() {
        gameModel.setGameState(GameStatus.STARTED)
        assertEquals(GameStatus.STARTED, gameModel.getGameState().value)
    }

    @Test
    fun testClearGameResults() {
        gameModel.addGameResult(Pair("Player1", 100L))
        gameModel.clearGameResults()
        assertTrue(gameModel.getGameResults().isEmpty())
    }

    @Test
    fun testGameStateAfterClearGameResults() {
        gameModel.setGameState(GameStatus.STARTED)
        gameModel.clearGameResults()
        assertEquals(GameStatus.NOT_STARTED, gameModel.getGameState().value)
    }

    @Test
    fun testMultipleGameResults() {
        val result1 = Pair("Player1", 100L)
        val result2 = Pair("Player2", 200L)
        gameModel.addGameResult(result1)
        gameModel.addGameResult(result2)
        val gameResults: SnapshotStateList<Pair<String, Long>> = gameModel.getGameResults()
        assertTrue(gameResults.contains(result1))
        assertTrue(gameResults.contains(result2))
        assertEquals(2, gameResults.size)
    }
}
