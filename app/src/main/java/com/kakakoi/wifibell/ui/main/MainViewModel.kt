package com.kakakoi.wifibell.ui.main

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.net.*
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kakakoi.wifibell.R


class MainViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

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

    init {
        load()
        sound(application)
        val accel: Sensor = sm.getDefaultSensor(
            Sensor.TYPE_STEP_COUNTER
        )
        sm.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL)
        cm.registerNetworkCallback(request, networkCallback)
    }

    private fun load() {
        val info = wm.connectionInfo

        _rssi.value = info.rssi
        _rssiText.value = info.rssi.toString()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            _signalLevel.value = wm.calculateSignalLevel(info.rssi)
            _signalLevelText.value = _signalLevel.value.toString()
        } else {
            _signalLevel.value =
                WifiManager.calculateSignalLevel(info.rssi, SIGNAL_LEVEL)
            _signalLevelText.value = _signalLevel.value.toString()
        }
    }

    private lateinit var soundPool: SoundPool
    private var soundOne = 0

    private fun sound(application: Application) {
        val audioAttributes = AudioAttributes.Builder()
            // USAGE_MEDIA
            // USAGE_GAME
            .setUsage(AudioAttributes.USAGE_GAME)
            // CONTENT_TYPE_MUSIC
            // CONTENT_TYPE_SPEECH, etc.
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        soundPool = SoundPool.Builder()
            .setAudioAttributes(audioAttributes)
            // ストリーム数に応じて
            .setMaxStreams(2)
            .build()

        // one.wav をロードしておく
        soundOne = soundPool.load(application, R.raw.pi, 1)

        // load が終わったか確認する場合
        soundPool.setOnLoadCompleteListener { soundPool, sampleId, status ->
            Log.d("debug", "sampleId=$sampleId")
            Log.d("debug", "status=$status")
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
        // play(ロードしたID, 左音量, 右音量, 優先度, ループ, 再生速度)
        soundPool.play(soundOne, 1.0f, 1.0f, 0, 0, 1.0f)
        Log.d(TAG, "onSensorChanged: ")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "onAccuracyChanged: ")
    }
}