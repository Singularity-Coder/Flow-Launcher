package com.singularitycoder.flowlauncher.helper

import android.content.Context
import android.net.*
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

// https://stackoverflow.com/questions/70384129/lifecycle-onlifecycleevent-is-deprecated
class NetworkStatus @Inject constructor(context: Context) : DefaultLifecycleObserver {

    private val conMan = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val oldActiveNet = conMan?.activeNetworkInfo
    private val oldWifi = conMan?.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
    private val oldMobile = conMan?.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
    private val oldEthernet = conMan?.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET)
    private val hasOldWifi = null != oldWifi && oldWifi.isConnected
    private val hasOldCellular = null != oldMobile && oldMobile.isConnected
    private val hasOldEthernet = null != oldEthernet && oldEthernet.isConnected

    @RequiresApi(Build.VERSION_CODES.M)
    private val activeNet = conMan?.activeNetwork

    @RequiresApi(Build.VERSION_CODES.M)
    private val netCap = conMan?.getNetworkCapabilities(activeNet)

    @RequiresApi(Build.VERSION_CODES.M)
    private val hasWifi = netCap?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false

    @RequiresApi(Build.VERSION_CODES.M)
    private val hasCellular = netCap?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ?: false

    @RequiresApi(Build.VERSION_CODES.M)
    private val hasEthernet = netCap?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ?: false

    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    private val _networkState = MutableStateFlow<NetworkState>(NetworkState.NONE)
    val networkState = _networkState.asStateFlow()

    init {
        registerNetworkCallback()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        unregisterNetworkCallback()
    }

    private fun registerNetworkCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (null == activeNet || null == netCap) {
                _networkState.value = NetworkState.UNAVAILABLE
                return
            }
        } else {
            if (null == oldActiveNet) {
                _networkState.value = NetworkState.UNAVAILABLE
                return
            }
        }

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (hasWifi || hasCellular || hasEthernet) _networkState.value = NetworkState.AVAILABLE
                } else {
                    if (hasOldWifi || hasOldCellular || hasOldEthernet) _networkState.value = NetworkState.AVAILABLE
                }
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                _networkState.value = NetworkState.LOSING
            }

            override fun onUnavailable() {
                _networkState.value = NetworkState.UNAVAILABLE
            }

            override fun onLost(network: Network) {
                _networkState.value = NetworkState.LOST
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conMan?.registerDefaultNetworkCallback(networkCallback)
        } else {
            val request = NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
            conMan?.registerNetworkCallback(request, networkCallback)
        }
    }

    private fun unregisterNetworkCallback() {
        if (this::networkCallback.isInitialized.not()) return
        conMan?.unregisterNetworkCallback(networkCallback)
    }

    @RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
    fun isOnline(): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        hasWifi || hasCellular || hasEthernet
    } else {
        hasOldWifi || hasOldCellular || hasOldEthernet
    }
}

enum class NetworkState {
    NONE, AVAILABLE, LOSING, UNAVAILABLE, LOST
}