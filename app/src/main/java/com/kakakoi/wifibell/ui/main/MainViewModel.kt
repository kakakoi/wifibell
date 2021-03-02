package com.kakakoi.wifibell.ui.main

import android.app.Application
import android.graphics.drawable.Drawable
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
import androidx.lifecycle.Transformations
import com.kakakoi.wifibell.R
import com.kakakoi.wifibell.model.PingSound


class MainViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    //TODO mobile networkの時にテザリング要求
    companion object {
        const val TAG = "MainViewModel"
        const val SIGNAL_LEVEL = 10
        const val SIGNAL_LEVEL_SAFE = 0.7
        const val SIGNAL_LEVEL_NORMAL = 0.4
        const val SIGNAL_LEVEL_WARN = 0.2
        lateinit var SSID_TEXT: String
        lateinit var LEVEL_TEXT: String
        lateinit var UNSUPPORTED_TEXT: String
    }

    private val cm: ConnectivityManager =
        application.getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val wm = application.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager
    private val sm = application.getSystemService(AppCompatActivity.SENSOR_SERVICE) as SensorManager
    val sound = PingSound(application)
    val SIGNAL_LEVEL_NEAR_THRESHOLD = SIGNAL_LEVEL * SIGNAL_LEVEL_SAFE
    val SIGNAL_LEVEL_NORMAL_THRESHOLD = SIGNAL_LEVEL * SIGNAL_LEVEL_NORMAL
    val SIGNAL_LEVEL_FAR_THRESHOLD = SIGNAL_LEVEL * SIGNAL_LEVEL_WARN

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

    private var _signalLevel: MutableLiveData<Int> =
        MutableLiveData<Int>().also { mutableLiveData ->
            mutableLiveData.value = 0
        }
    val signalLevel: LiveData<Int>
        get() = _signalLevel

    private var _signalLevelText: MutableLiveData<String> =
        MutableLiveData<String>().also { mutableLiveData ->
            mutableLiveData.value = "-"
        }
    val signalLevelText: LiveData<String>
        get() = _signalLevelText

    val logo: LiveData<Drawable> = Transformations.map(signalLevel) {
        when {
            signalLevel.value!! > SIGNAL_LEVEL_FAR_THRESHOLD -> application.getDrawable(R.drawable.ic_baseline_escalator_warning_100)
            else -> application.getDrawable(R.drawable.ic_baseline_person_outline_100)
        }
    }

    val animationResId: LiveData<Int> = Transformations.map(signalLevel) {
        when {
            signalLevel.value!! > SIGNAL_LEVEL_FAR_THRESHOLD -> R.raw.rocket_in_space
            else -> R.raw.lighthouse
        }
    }

    val stateText: LiveData<String> = Transformations.map(signalLevel) {
        when {
            signalLevel.value!! > SIGNAL_LEVEL_NEAR_THRESHOLD -> application.getString(R.string.state_near)
            signalLevel.value!! > SIGNAL_LEVEL_NORMAL_THRESHOLD -> application.getString(R.string.state_normal)
            signalLevel.value!! > SIGNAL_LEVEL_FAR_THRESHOLD -> application.getString(R.string.state_far)
            else -> application.getString(R.string.state_very_far)
        }
    }

    private var _ssidText: MutableLiveData<String> =
        MutableLiveData<String>().also { mutableLiveData ->
            mutableLiveData.value = "-"
        }
    val ssidText: LiveData<String>
        get() = _ssidText

    init {
        SSID_TEXT = application.getString(R.string.ssid)
        LEVEL_TEXT = application.getString(R.string.level)
        UNSUPPORTED_TEXT = application.getString(R.string.unsupported)

        load()
        sm.getDefaultSensor(
            Sensor.TYPE_STEP_COUNTER
        )?.also {
            sm.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }?.run { _signalLevelText.value = UNSUPPORTED_TEXT }
        cm.registerNetworkCallback(request, networkCallback)
    }

    private fun load() {
        val info = wm.connectionInfo

        _rssi.value = info.rssi
        _rssiText.value = info.rssi.toString()

        _ssidText.value = SSID_TEXT + info.ssid

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            _signalLevel.value = wm.calculateSignalLevel(info.rssi)
            _signalLevelText.value = _signalLevel.value.toString()
        } else {
            _signalLevel.value =
                WifiManager.calculateSignalLevel(info.rssi, SIGNAL_LEVEL)
            val distance = SIGNAL_LEVEL - _signalLevel.value!!
            val sb = StringBuilder("<")
            for (i in 0..distance) sb.append("-")
            sb.append(LEVEL_TEXT)
            sb.append(_signalLevel.value)
            for (i in 0..distance) sb.append("-")
            sb.append(">")
            _signalLevelText.value = sb.toString()
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
        Log.d(TAG, "onAccuracyChanged: ")
    }
}