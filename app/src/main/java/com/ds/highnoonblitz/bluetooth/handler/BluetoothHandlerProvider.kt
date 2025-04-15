package com.ds.highnoonblitz.bluetooth.handler

import android.annotation.SuppressLint
import com.ds.highnoonblitz.MainActivity

/**
 * Provides a singleton instance of the [BluetoothHandler].
 *
 * This object initializes and retrieves the [BluetoothHandler] instance.
 */
object BluetoothHandlerProvider {
    private var bluetoothHandler: BluetoothHandler? = null

    /**
     * Initializes the [BluetoothHandler] with the provided [MainActivity] instance.
     *
     * This method sets up the [BluetoothHandler] if it has not already been created.
     *
     * @param activity the main activity used for initializing the [BluetoothHandler].
     */
    @SuppressLint("NewApi")
    fun initialize(activity: MainActivity) {
        if (bluetoothHandler == null) {
            bluetoothHandler = BluetoothHandler(activity)
        }
    }

    /**
     * Retrieves the active [BluetoothHandler] instance.
     *
     * @return the [BluetoothHandler] instance.
     * @throws IllegalStateException if the [BluetoothHandler] has not been initialized.
     */
    fun getBluetoothHandler(): BluetoothHandler = bluetoothHandler ?: throw IllegalStateException("BluetoothHandler not initialized")
}
