package it.unibo.model

import android.bluetooth.BluetoothDevice
import org.junit.Test
import org.junit.Before
import org.junit.Assert.*
import org.mockito.Mockito.*
import com.ds.highnoonblitz.messages.MessageComposed
import com.ds.highnoonblitz.model.DeviceStatus
import com.ds.highnoonblitz.model.ExternalDevice
import com.ds.highnoonblitz.model.Lobby
import com.ds.highnoonblitz.model.LobbyStatus
import com.ds.highnoonblitz.model.MyDevice

class LobbyTest {
    private lateinit var lobby: Lobby
    private lateinit var mockExternalDevice: ExternalDevice
    private lateinit var mockBluetoothDevice: BluetoothDevice
    private lateinit var myDevice: MyDevice
    private lateinit var mockMessage: MessageComposed

    @Before
    fun setup() {
        lobby = Lobby()
        mockExternalDevice = mock(ExternalDevice::class.java)
        mockBluetoothDevice = mock(BluetoothDevice::class.java)
        myDevice = MyDevice()
        mockMessage = mock(MessageComposed::class.java)

        `when`(mockExternalDevice.getDevice()).thenReturn(mockBluetoothDevice)
        `when`(mockBluetoothDevice.address).thenReturn("00:11:22:33:44:55")
    }

    @Test
    fun testInitialState() {
        assertNull(lobby.getMasterDevice().value)
        assertTrue(lobby.getConnectedDevices().isEmpty())
        assertEquals(LobbyStatus.CREATED, lobby.status)
    }

    @Test
    fun testSetMasterDevice() {
        lobby.setMasterDevice(mockExternalDevice)
        assertEquals(mockExternalDevice, lobby.getMasterDevice().value)

        lobby.setMasterDevice(myDevice)
        assertEquals(myDevice, lobby.getMasterDevice().value)

        lobby.removeMasterDevice()
        assertNull(lobby.getMasterDevice().value)
    }

    @Test
    fun testAddConnectedDevice() {
        lobby.addConnectedDevice(mockExternalDevice)
        assertEquals(1, lobby.getConnectedDevices().size)
        verify(mockExternalDevice).setStatus(DeviceStatus.IN_LOBBY)

        // Test duplicate addition
        lobby.addConnectedDevice(mockExternalDevice)
        assertEquals(1, lobby.getConnectedDevices().size)
    }

    @Test
    fun testRemoveConnectedDevice() {
        lobby.addConnectedDevice(mockExternalDevice)
        lobby.removeConnectedDevice(mockBluetoothDevice)
        assertTrue(lobby.getConnectedDevices().isEmpty())
        verify(mockExternalDevice).closeSocket()
    }

    @Test
    fun testIsReadyToStart() {
        `when`(mockExternalDevice.ready()).thenReturn(true)
        lobby.addConnectedDevice(mockExternalDevice)
        assertTrue(lobby.isReadyToStart())

        `when`(mockExternalDevice.ready()).thenReturn(false)
        assertFalse(lobby.isReadyToStart())
    }

    @Test
    fun testDeviceStatusUpdate() {
        lobby.addConnectedDevice(mockExternalDevice)
        lobby.deviceStatusUpdate("00:11:22:33:44:55", DeviceStatus.READY)
        verify(mockExternalDevice).setStatus(DeviceStatus.READY)
    }

    @Test
    fun testStartGame() {
        lobby.addConnectedDevice(mockExternalDevice)
        lobby.startGame()
        assertEquals(LobbyStatus.STARTED, lobby.status)
        verify(mockExternalDevice).setStatus(DeviceStatus.IN_GAME)
    }

    @Test
    fun testSendMessage() {
        lobby.addConnectedDevice(mockExternalDevice)
        lobby.sendMessage("00:11:22:33:44:55", mockMessage)
        verify(mockExternalDevice).send(mockMessage)
    }
}