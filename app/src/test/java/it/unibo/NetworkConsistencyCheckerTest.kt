package it.unibo

import com.ds.highnoonblitz.bluetooth.management.ConsistencyCheckCallback
import com.ds.highnoonblitz.bluetooth.management.NetworkConsistencyChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.timeout
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

class NetworkConsistencyCheckerTest {
    @Mock
    private lateinit var mockCallback: ConsistencyCheckCallback

    private lateinit var consistencyChecker: NetworkConsistencyChecker
    private val testDispatcher = StandardTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        consistencyChecker = NetworkConsistencyChecker(mockCallback, false)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when only one client then consistency check passes immediately`() {
        // Given
        val clients = setOf("client1")

        // When
        consistencyChecker.startChecking(1000, clients)

        // Then
        verify(mockCallback).onConsistencyCheckFinished(true)
    }

    @Test
    fun `when all clients properly connected then consistency check passes`() =
        runTest {
            // Given
            val clients = setOf("client1", "client2", "client3")

            // When
            consistencyChecker.startChecking(1000, clients)
            consistencyChecker.addClientConnections("client1", setOf("client2", "client3"))
            consistencyChecker.addClientConnections("client2", setOf("client1", "client3"))
            consistencyChecker.addClientConnections("client3", setOf("client1", "client2"))

            // Then
            verify(mockCallback).onConsistencyCheckFinished(true)
        }

    @Test
    fun `when connections incomplete then consistency check fails`() =
        runTest {
            // Given
            val clients = setOf("client1", "client2", "client3")

            // When
            consistencyChecker.startChecking(1000, clients)
            consistencyChecker.addClientConnections("client1", setOf("client2", "client3"))
            consistencyChecker.addClientConnections("client2", setOf("client1")) // Missing client3
            consistencyChecker.addClientConnections("client3", setOf("client1", "client2"))

            // Then
            verify(mockCallback).onConsistencyCheckFinished(false)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when timeout occurs before all clients report then consistency check fails`() =
        runTest {
            // Given
            val clients = setOf("client1", "client2", "client3")

            // When
            consistencyChecker.startChecking(100, clients)
            consistencyChecker.addClientConnections("client1", setOf("client2", "client3"))

            // Allow some time for any pending coroutines to process
            advanceUntilIdle()

            // Then - wait for the callback
            verify(mockCallback, timeout(5000)).onConsistencyCheckFinished(false)
        }
}
