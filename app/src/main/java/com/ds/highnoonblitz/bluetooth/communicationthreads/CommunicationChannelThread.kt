package com.ds.highnoonblitz.bluetooth.communicationthreads

import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.util.Log
import com.ds.highnoonblitz.messages.MessageComposed
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.IOException
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class CommunicationChannelThread(
    private val socket: BluetoothSocket,
    private val handler: Handler,
    private val onDeviceDisconnected: (BluetoothSocket) -> Unit,
) : Thread() {
    private val inputStream = BufferedInputStream(socket.inputStream)
    private val outputStream = BufferedOutputStream(socket.outputStream)
    private var isClosing = AtomicBoolean(false)
    private val messageQueue: ConcurrentLinkedQueue<MessageComposed> = ConcurrentLinkedQueue()
    private val executor = Executors.newSingleThreadExecutor()
    private val isSending = AtomicBoolean(false)

    companion object {
        private const val BUFFER_SIZE = 1024
        private const val TAG = "CommunicationThread"
        private const val MESSAGE_DELAY = 100L // Time between messages in milliseconds
        private const val BATCH_SIZE = 512 // Maximum bytes to send at once
    }

    init {
        executor.execute { processQueue() }
    }

    override fun run() {
        val buffer = ByteArray(BUFFER_SIZE)

        while (!isClosing.get()) {
            try {
                val bytes = inputStream.read(buffer)
                if (bytes == -1) {
                    throw IOException("End of stream reached")
                }

                val readBytes = ByteArray(bytes)
                System.arraycopy(buffer, 0, readBytes, 0, bytes)

                val messageComposed =
                    MessageComposed.Companion
                        .MessageBuilder()
                        .fromString(String(readBytes))
                        .build()
                processMessageForHandler(messageComposed)
            } catch (e: IOException) {
                Log.e(TAG, "Input stream was disconnected: ${e.message}")
                if (!isClosing.get()) {
                    onDeviceDisconnected(socket)
                }
                cleanup()
                break
            }
        }
    }

    private fun processMessageForHandler(messageComposed: MessageComposed) {
        try {
            val purpose = messageComposed.getPurpose()
            val message = messageComposed.getMessage().toString()
            val senderMacAddress = socket.remoteDevice.address
            handler.sendMessage(
                handler.obtainMessage(
                    purpose,
                    message.toByteArray().size,
                    -1,
                    Pair(message.toByteArray(), senderMacAddress),
                ),
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error processing message: ${e.message}")
        }
    }

    @Synchronized
    fun write(messageComposed: MessageComposed) {
        if (!isClosing.get()) {
            Log.i(TAG, "Adding message to queue: $messageComposed")
            messageQueue.offer(messageComposed)
        }
    }

    private fun processQueue() {
        while (!executor.isShutdown && !isClosing.get()) {
            try {
                if (!isSending.get()) {
                    val message = messageQueue.poll()
                    if (message != null) {
                        sendMessage(message)
                    } else {
                        sleep(50) // Short sleep when queue is empty
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in process queue: ${e.message}")
            }
        }
    }

    private fun sendMessage(message: MessageComposed) {
        if (isClosing.get()) return

        try {
            isSending.set(true)
            Log.d(TAG, "Sending message: $message")

            val messageBytes = message.toString().toByteArray()
            var offset = 0

            while (offset < messageBytes.size && !isClosing.get()) {
                val remaining = messageBytes.size - offset
                val length = kotlin.math.min(BATCH_SIZE, remaining)

                outputStream.write(messageBytes, offset, length)
                outputStream.flush()

                offset += length
                if (offset < messageBytes.size) {
                    sleep(MESSAGE_DELAY)
                }
            }

            // Additional flush and delay after complete message
            outputStream.flush()
            sleep(MESSAGE_DELAY)

            Log.d(TAG, "Message sent successfully")
        } catch (e: IOException) {
            Log.e(TAG, "Error sending message: ${e.message}")
            if (!isClosing.get()) {
                onDeviceDisconnected(socket)
            }
        } finally {
            isSending.set(false)
        }
    }

    fun cancel() {
        isClosing.set(true)
        cleanup()
    }

    private fun cleanup() {
        try {
            Log.i(TAG, "Cleaning up communication channel")
            executor.shutdown()
            socket.close()
            inputStream.close()
            outputStream.close()
            interrupt()
        } catch (e: IOException) {
            Log.e(TAG, "Error during cleanup: ${e.message}")
        }
    }
}
