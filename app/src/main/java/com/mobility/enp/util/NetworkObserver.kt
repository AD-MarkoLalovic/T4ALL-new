package com.mobility.enp.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

class NetworkObserver(
    context: Context,
    private val onAvailable: () -> Unit,
    private val onLost: () -> Unit = {}
) {
    private val connectivityManager =
        context.getSystemService(ConnectivityManager::class.java)
    private var callback: ConnectivityManager.NetworkCallback? = null

    fun start() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) = onAvailable()
            override fun onLost(network: Network) = onLost()
        }

        connectivityManager.registerNetworkCallback(request, callback!!)
    }

    fun stop() {
        callback?.let { connectivityManager.unregisterNetworkCallback(it) }
    }
}