package com.ds.highnoonblitz

import android.os.Handler
import android.os.Looper
import android.widget.Toast

object MainThreadUtil {
    private val mainThreadHandler = Handler(Looper.getMainLooper())

    fun makeToast(message: String){
        mainThreadHandler.post {
            Toast.makeText(MainApplication.applicationContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    fun runOnMainThread(runnable: Runnable){
        mainThreadHandler.post(runnable)
    }
}