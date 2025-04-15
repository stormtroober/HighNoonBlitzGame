package it.unibo.model

import com.ds.highnoonblitz.model.Device
import com.ds.highnoonblitz.model.DeviceStatus
import org.junit.Test
import org.junit.Before
import org.junit.Assert.*
import kotlinx.coroutines.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class DeviceTest {
    private lateinit var device: Device

    @Before
    fun setup() {
        device = Device()
    }

    @Test
    fun testInitialStatus() {
        assertEquals(DeviceStatus.NOT_INITIALIZED, device.getStatus())
        assertFalse(device.ready())
    }

    @Test
    fun testStatusUpdate() {
        device.setStatus(DeviceStatus.IN_LOBBY)
        assertEquals(DeviceStatus.IN_LOBBY, device.getStatus())

        device.setStatus(DeviceStatus.READY)
        assertEquals(DeviceStatus.READY, device.getStatus())
        assertTrue(device.ready())
    }

    @Test
    fun testConcurrentStatusUpdates() = runBlocking {
        val iterations = 100
        val latch = CountDownLatch(iterations * 2)

        val job1 = launch {
            repeat(iterations) {
                device.setStatus(DeviceStatus.READY)
                latch.countDown()
            }
        }

        val job2 = launch {
            repeat(iterations) {
                device.setStatus(DeviceStatus.IN_LOBBY)
                latch.countDown()
            }
        }

        latch.await(5, TimeUnit.SECONDS)
        job1.join()
        job2.join()

        assertTrue(
            device.getStatus() == DeviceStatus.READY ||
            device.getStatus() == DeviceStatus.IN_LOBBY
        )
    }

    @Test
    fun testToString() {
        val stringRepresentation = device.toString()
        assertTrue(stringRepresentation.contains("status=NOT_INITIALIZED"))
    }
}