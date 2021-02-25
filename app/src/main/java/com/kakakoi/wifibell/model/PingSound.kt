package com.kakakoi.wifibell.model

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.kakakoi.wifibell.R

class PingSound(context: Context) {

    companion object {
        const val TAG = "PingSound"
    }

    private lateinit var soundPool: SoundPool
    private var soundResource = 0
    private var isPlay = false

    init {
        sound(context)
    }

    private fun sound(context: Context) {
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
        soundResource = soundPool.load(context, R.raw.pi, 1)

        // load が終わったか確認する場合
        soundPool.setOnLoadCompleteListener { soundPool, sampleId, status ->
            Log.d(TAG, "sampleId=$sampleId")
            Log.d(TAG, "status=$status")
        }
    }

    fun play() {
        if (isPlay) {
            soundPool.resume(soundResource)
            Log.d(TAG, "resume: streamID $soundResource")
        } else {
            // play(ロードしたID, 左音量, 右音量, 優先度, ループ, 再生速度)
            soundPool?.play(soundResource, 1.0f, 1.0f, 0, -1, 1.0f)
            isPlay = true
            Log.d(TAG, "play: streamID $soundResource")
        }
    }

    fun stop() {
        soundPool.pause(soundResource)
        Log.d(TAG, "stop: streamID $soundResource")
    }

    fun close() {
        isPlay = false
        soundPool?.release()
    }
}