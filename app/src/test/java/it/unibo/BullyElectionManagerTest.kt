package it.unibo

import com.ds.highnoonblitz.leaderelection.BullyElectionManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class BullyElectionManagerTest {

    private lateinit var electionManager: BullyElectionManager
    private var electionFunctionCalled: Boolean = false

    @Before
    fun setUp() {
        electionFunctionCalled = false
        electionManager = BullyElectionManager { electionFunctionCalled = true }
    }

    @Test
    fun testAddElectionListMember() {
        val initialSize = electionManager.getDevicesToElect().size

        // Add a device with a very high UUID that should be greater than any random session ID
        electionManager.addElectionListMember("device1", "ffffffff-ffff-ffff-ffff-ffffffffffff")

        // The high UUID should be included in devices to elect
        assertTrue(electionManager.getDevicesToElect().size > initialSize)
    }

    @Test
    fun testAddDuplicateElectionListMember() {
        // Add the same device and UUID twice
        electionManager.addElectionListMember("device1", "ffffffff-ffff-ffff-ffff-ffffffffffff")
        val sizeAfterFirstAdd = electionManager.getDevicesToElect().size

        electionManager.addElectionListMember("device1", "ffffffff-ffff-ffff-ffff-ffffffffffff")

        // Should only be added once
        assertEquals(sizeAfterFirstAdd, electionManager.getDevicesToElect().size)
    }

    @Test
    fun testClearElectionList() {
        // Add some devices
        electionManager.addElectionListMember("device1", "ffffffff-ffff-ffff-ffff-ffffffffffff")
        electionManager.addElectionListMember("device2", "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee")

        // Clear the list
        electionManager.clearElectionList()

        // Check that it's empty
        assertTrue(electionManager.getDevicesToElect().isEmpty())
    }

    @Test
    fun testGetDevicesToElect() {
        electionManager.clearElectionList()

        // Add devices with lower and higher UUIDs
        electionManager.addElectionListMember("device1", "00000000-0000-0000-0000-000000000000") // Lower UUID
        electionManager.addElectionListMember("device2", "ffffffff-ffff-ffff-ffff-ffffffffffff") // Higher UUID

        // Get devices to elect
        val devicesToElect = electionManager.getDevicesToElect()

        // The list should contain at least the device with higher UUID
        assertTrue(devicesToElect.contains("device2"))
    }
}