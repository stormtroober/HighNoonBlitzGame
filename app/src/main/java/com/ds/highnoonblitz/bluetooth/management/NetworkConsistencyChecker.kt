package com.ds.highnoonblitz.bluetooth.management

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Callback interface for notifying the result of a network consistency check.
 */
interface ConsistencyCheckCallback {
    /**
     * Invoked when the consistency check process has finished.
     *
     * @param isConsistent true if the network is consistent, false otherwise.
     */
    fun onConsistencyCheckFinished(isConsistent: Boolean)
}

/**
 * Checks the consistency of client connections in the network.
 *
 * This checker verifies that all clients are connected to each other and calls the provided
 * callback with the result. It supports an optional debug mode for logging.
 *
 * @property callback the [ConsistencyCheckCallback] to receive the final check status.
 * @property debugMode a flag to enable additional debug logging.
 */
class NetworkConsistencyChecker(
    private val callback: ConsistencyCheckCallback,
    private val debugMode: Boolean = false,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private lateinit var allClients: Set<String>
    private val clientConnections = mutableMapOf<String, Set<String>>()

    /**
     * Starts the consistency checking process with the provided timeout and client list.
     *
     * If only one client is provided, the callback is immediately invoked with true.
     * Otherwise, it waits for a set timeout to determine if all client connections arrived.
     *
     * @param timeout the duration in milliseconds to wait for all connections.
     * @param clients the set of all client identifiers expected to participate.
     */
    fun startChecking(
        timeout: Long,
        clients: Set<String>,
    ) {
        this.allClients = clients

        if (clients.size == 1) {
            callback.onConsistencyCheckFinished(true)
            return
        }

        coroutineScope.launch {
            delay(timeout)
            // If not all connections are received within the timeout, finish with false.
            if (clientConnections.size != allClients.size) {
                log("Consistency check failed: not all clients responded within the timeout")
                callback.onConsistencyCheckFinished(false)
            }
        }
    }

    /**
     * Adds the connections reported by a client and checks for overall consistency.
     *
     * The provided connections are stored and the overall consistency is validated.
     *
     * @param clientId the identifier of the client.
     * @param connections the set of client identifiers that the client is connected to.
     */
    fun addClientConnections(
        clientId: String,
        connections: Set<String>,
    ) {
        clientConnections[clientId] = connections
        log("Added connections for client $clientId: $connections")
        checkConsistency()
    }

    /**
     * Validates the consistency of the network connections.
     *
     * The check confirms that each client is connected to all other clients.
     *
     * @return true if the network is consistent, false otherwise.
     */
    private fun checkConsistency(): Boolean {
        // Verify that connections have been received from all clients.
        if (clientConnections.size != allClients.size) {
            log("Consistency check in progress: waiting for more client connections")
            return false
        }

        // For each client, check that its connections match the expected set.
        for (client in allClients) {
            val connections = clientConnections[client] ?: return false
            log("Client $client connections: $connections")

            // Expected connections for a client are all other clients.
            val expectedConnections = allClients - client
            log("Expected connections for client $client: $expectedConnections")
            if (connections != expectedConnections) {
                log("Consistency check failed: client $client is not connected to all other clients")
                callback.onConsistencyCheckFinished(false)
                return false
            } else {
                log("Check passed: client $client is connected to all other clients")
            }
        }

        log("Consistency check passed: all clients are correctly connected")
        callback.onConsistencyCheckFinished(true)
        return true
    }

    /**
     * Logs a message if debug mode is active.
     *
     * @param message the debug message to log.
     */
    private fun log(message: String) {
        if (debugMode) {
            Log.i("NetworkConsistencyChecker", message)
        }
    }
}
