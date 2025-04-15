package com.ds.highnoonblitz.model

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.ds.highnoonblitz.bluetooth.communicationthreads.CommunicationChannelThread
import com.ds.highnoonblitz.messages.MessageComposed

class ExternalDevice(
    private val communicationSocket: BluetoothSocket,
    private val communicationChannel: CommunicationChannelThread,
) : Device() {
    private val device: BluetoothDevice = communicationSocket.remoteDevice

    fun getDevice(): BluetoothDevice = device

    fun getCommunicationChannel(): CommunicationChannelThread = communicationChannel

    fun closeSocket() {
        communicationSocket.close()
    }

    fun destroy() {
        communicationChannel.cancel()
        communicationSocket.close()
    }

    @Synchronized
    fun send(message: MessageComposed) {
        communicationChannel.write(message)
    }

    @SuppressLint("MissingPermission")
    override fun toString(): String = "ExternalDevice(address=${getDevice().address}, name=${getDevice().name}, status=${getStatus()})"
}
