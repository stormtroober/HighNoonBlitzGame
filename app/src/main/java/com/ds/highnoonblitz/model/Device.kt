package com.ds.highnoonblitz.model

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

open class Device {

    private val lock = Any()
    // Use mutableStateOf for the status property
    private var _status by mutableStateOf(DeviceStatus.NOT_INITIALIZED)

    fun ready(): Boolean {
        synchronized(lock)  {
            return _status == DeviceStatus.READY
        }
    }

    fun getStatus(): DeviceStatus {
        synchronized(lock)  {
            return _status
        }
    }

    fun setStatus(status: DeviceStatus) {
        synchronized(lock) {
            _status = status
        }
    }

    override fun toString(): String {
        return "Device(status=$_status)"
    }
}