package ceui.lisa.slinky.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log

class NetworkListener(private val context: Context) {

    private val TAG = "NetworkListener"

    fun startListening() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        checkNetworkType(connectivityManager)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    Log.d(TAG, "Network Available")
                    checkNetworkType(connectivityManager)
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    Log.d(TAG, "Network Lost")
                }
            })
        }
    }

    private fun checkNetworkType(connectivityManager: ConnectivityManager) {
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

        if (networkCapabilities != null) {
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.d(TAG, "Connected via Wi-Fi")
            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Log.d(TAG, "Connected via Cellular (4G/3G/2G)")
            } else {
                Log.d(TAG, "Connected via Other Network")
            }
        } else {
            Log.d(TAG, "networkCapabilities == null")
        }
    }
}
