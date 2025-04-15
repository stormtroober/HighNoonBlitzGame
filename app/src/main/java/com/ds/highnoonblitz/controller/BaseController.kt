package com.ds.highnoonblitz.controller

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.navigation.NavController
import com.ds.highnoonblitz.MainActivity

/**
 * BaseController provides fundamental functionality for controllers,
 * including navigation handling.
 *
 * @property activity the MainActivity instance used as context.
 */
@RequiresApi(Build.VERSION_CODES.S)
open class BaseController(
    protected val activity: MainActivity,
) {
    private lateinit var _navController: NavController

    /**
     * Gets the [NavController] associated with this controller.
     */
    val navController: NavController
        get() = _navController

    /**
     * Sets the [NavController] for this controller.
     *
     * @param navController the NavController to assign.
     */
    fun setNavController(navController: NavController) {
        _navController = navController
    }
}
