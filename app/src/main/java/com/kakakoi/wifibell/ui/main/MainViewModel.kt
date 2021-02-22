package com.kakakoi.wifibell.ui.main

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.*
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


class MainViewModel(application: Application) : AndroidViewModel(application),SensorEventListener {

    companion object {
        const val SIGNAL_LEVEL = 100
        const val TAG = "MainViewModel"
    }

    private val cm: ConnectivityManager =
        application.getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val wm = application.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager
    private val sm = application.getSystemService(AppCompatActivity.SENSOR_SERVICE) as SensorManager


    private val request = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        .build()
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
        }

        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            super.onLinkPropertiesChanged(network, linkProperties)
        }

        override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
            super.onBlockedStatusChanged(network, blocked)
        }
    }

    private var _rssi: MutableLiveData<Int> =
        MutableLiveData<Int>().also { mutableLiveData ->
            mutableLiveData.value = 0
        }
    val rssi: LiveData<Int>
        get() = _rssi

    private var _rssiText: MutableLiveData<String> =
        MutableLiveData<String>().also { mutableLiveData ->
            mutableLiveData.value = "-"
        }
    val rssiText: LiveData<String>
        get() = _rssiText

    private var _signalLevel: MutableLiveData<String> =
        MutableLiveData<String>().also { mutableLiveData ->
            mutableLiveData.value = "-"
        }
    val signalLevel: LiveData<String>
        get() = _signalLevel

    init {
        load()
        val accel: Sensor = sm.getDefaultSensor(
            Sensor.TYPE_STEP_COUNTER
        )
        sm.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL)
        cm.registerNetworkCallback(request, networkCallback)
    }

    private fun load(){
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true

        val info = wm.connectionInfo

        _rssi.value = info.rssi
        _rssiText.value = info.rssi.toString()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            _signalLevel.value = wm.calculateSignalLevel(info.rssi).toString()
        } else {
            _signalLevel.value =
                WifiManager.calculateSignalLevel(info.rssi, SIGNAL_LEVEL).toString()
        }
    }

    override fun onCleared() {
        super.onCleared()
        cm.bindProcessToNetwork(null)
        cm.unregisterNetworkCallback(networkCallback)
        sm.unregisterListener(this);

    }

    override fun onSensorChanged(event: SensorEvent?) {
        load()
        Log.d(TAG, "onSensorChanged: ")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        load()
        Log.d(TAG, "onAccuracyChanged: ")
    }
}