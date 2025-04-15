package it.unibo.model

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.ds.highnoonblitz.bluetooth.communicationthreads.CommunicationChannelThread
import com.ds.highnoonblitz.messages.MessageComposed
import com.ds.highnoonblitz.model.ExternalDevice
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class ExternalDeviceTest {
    private lateinit var externalDevice: ExternalDevice
    private lateinit var mockSocket: BluetoothSocket
    private lateinit var mockChannel: CommunicationChannelThread
    private lateinit var mockBluetoothDevice: BluetoothDevice
    private lateinit var mockMessage: MessageComposed

    @Before
    fun setup() {
        mockSocket = mock(BluetoothSocket::class.java)
        mockChannel = mock(CommunicationChannelThread::class.java)
        mockBluetoothDevice = mock(BluetoothDevice::class.java)
        mockMessage = mock(MessageComposed::class.java)

        `when`(mockSocket.remoteDevice).thenReturn(mockBluetoothDevice)
        `when`(mockBluetoothDevice.address).thenReturn("00:11:22:33:44:55")
        `when`(mockBluetoothDevice.name).thenReturn("Test Device")

        externalDevice = ExternalDevice(mockSocket, mockChannel)
    }

    @Test
    fun testGetDevice() {
        assertEquals(mockBluetoothDevice, externalDevice.getDevice())
    }

    @Test
    fun testGetCommunicationChannel() {
        assertEquals(mockChannel, externalDevice.getCommunicationChannel())
    }

    @Test
    fun testCloseSocket() {
        externalDevice.closeSocket()
        verify(mockSocket, times(1)).close()
    }

    @Test
    fun testDestroy() {
        externalDevice.destroy()
        verify(mockChannel, times(1)).cancel()
        verify(mockSocket, times(1)).close()
    }

    @Test
    fun testSend() {
        externalDevice.send(mockMessage)
        verify(mockChannel, times(1)).write(mockMessage)
    }

    @Test
    fun testToString() {
        val stringRepresentation = externalDevice.toString()
        assertTrue(stringRepresentation.contains("00:11:22:33:44:55"))
        assertTrue(stringRepresentation.contains("Test Device"))
    }
}
