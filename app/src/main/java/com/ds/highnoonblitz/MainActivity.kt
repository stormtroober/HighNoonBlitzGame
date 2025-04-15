package com.ds.highnoonblitz

import DiscoverableResultContract
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.ds.highnoonblitz.bluetooth.handler.BluetoothHandlerProvider
import com.ds.highnoonblitz.controller.MainController
import com.ds.highnoonblitz.messages.MessageHandler
import com.ds.highnoonblitz.view.AppScreen

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    private var messageHandler: MessageHandler? = null
    private var mainController: MainController? = null
    private lateinit var discoverableLauncher: ActivityResultLauncher<Int>
    private var onServerSocketCreated: ((String) -> Unit)? = null

    @RequiresApi(Build.VERSION_CODES.S)
    @Composable
    fun SetupNavigationAndAppScreen(mainController: MainController) {
        val navController = rememberNavController()
        mainController.setNavController(navController)
        AppScreen(this, mainController, navController)
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BluetoothHandlerProvider.initialize(this)
        mainController = MainController(this)
        messageHandler = MessageHandler(mainController!!)
        setContent {
            SetupNavigationAndAppScreen(mainController!!)
        }

        discoverableLauncher =
            registerForActivityResult(DiscoverableResultContract()) { isDiscoverable ->
                if (isDiscoverable) {
                    val discoverableName = this.getSystemService(BluetoothManager::class.java).adapter.name
                    onServerSocketCreated?.invoke(discoverableName)
                } else {
                    Log.e("MainActivity", "Device is not discoverable")
                }
            }
    }

    fun startDiscoverable(onServerSocketCreated: ((String) -> Unit)?) {
        this.onServerSocketCreated = onServerSocketCreated
        discoverableLauncher.launch(GameConstants.DISCOVERABLE_TIME.toInt())
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun getMessageHandler(): MessageHandler? = messageHandler

    fun getMainController(): MainController? = mainController
}
