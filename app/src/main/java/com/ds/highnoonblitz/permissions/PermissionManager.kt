import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager {

    companion object {
        @RequiresApi(Build.VERSION_CODES.S)
        private val permissions = listOf(

            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE
        )

        private val REQUEST_ENABLE_BT = 1

        @RequiresApi(Build.VERSION_CODES.S)
        fun getBluetoothPermission(activity: Activity, bluetoothAdapter: BluetoothAdapter) {
            requestNotAcceptedPermissions(activity)
            if (!bluetoothAdapter.isEnabled) {
                requestBluetoothEnable(activity, bluetoothAdapter)
            } else {
                Log.d("com.ds.connectivityradar.permissions.PermissionManager", "Bluetooth enabled")
            }
        }

        private fun isPermissionGranted(activity: Activity, permission: String): Boolean {
            return ContextCompat.checkSelfPermission(
                activity.applicationContext, permission
            ) == PackageManager.PERMISSION_GRANTED
        }

        @RequiresApi(Build.VERSION_CODES.S)
        private fun requestNotAcceptedPermissions(activity: Activity) {
            ActivityCompat.requestPermissions(
                activity, permissions.toTypedArray(), REQUEST_ENABLE_BT
            )
        }

        @RequiresApi(Build.VERSION_CODES.S)
        private fun requestBluetoothEnable(activity: Activity, bluetoothAdapter: BluetoothAdapter) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ActivityCompat.checkSelfPermission(
                    activity, Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), REQUEST_ENABLE_BT
                )
            } else {
                activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
        }

        @RequiresApi(Build.VERSION_CODES.S)
        fun hasAllPermissions(activity: Activity): Boolean {
            return activity.applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
        }
    }
}