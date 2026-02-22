package com.etypewriter.ahc.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import com.etypewriter.ahc.R

class SoundManager(private val context: Context) {

    private var soundPool: SoundPool? = null
    private var humPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    // Sound IDs
    private var keySoundId: Int = 0
    private var returnSoundId: Int = 0
    private var bellSoundId: Int = 0

    // Volume settings (could be dynamic later)
    private val keyVolume = 1.0f
    private val bellVolume = 0.8f
    private val humVolume = 0.5f

    init {
        initSoundPool()
        initHumPlayer()
        vibrator = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager)?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            Log.e("SoundManager", "Vibrator init failed", e)
            null
        }
    }

    private fun initSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        // Load sounds
        // Note: Assumes resources exist in res/raw. Placeholders created previously.
        keySoundId = soundPool?.load(context, R.raw.typewriter_key, 1) ?: 0
        returnSoundId = soundPool?.load(context, R.raw.typewriter_return, 1) ?: 0
        bellSoundId = soundPool?.load(context, R.raw.typewriter_bell, 1) ?: 0
    }

    private fun initHumPlayer() {
        try {
            humPlayer = MediaPlayer.create(context, R.raw.typewriter_hum)?.apply {
                isLooping = true
                setVolume(humVolume, humVolume)
            }
        } catch (e: Exception) {
            Log.e("SoundManager", "Error initializing hum player", e)
        }
    }

    fun playKeySound() {
        if (keySoundId != 0) {
            try {
                soundPool?.play(keySoundId, keyVolume, keyVolume, 1, 0, 1.0f)
            } catch (e: Exception) {
                Log.e("SoundManager", "playKeySound failed", e)
            }
        }
    }

    fun playReturnSound() {
        if (returnSoundId != 0) {
            try {
                soundPool?.play(returnSoundId, keyVolume, keyVolume, 1, 0, 1.0f)
            } catch (e: Exception) {
                Log.e("SoundManager", "playReturnSound failed", e)
            }
        }
    }

    fun playBellSound() {
        if (bellSoundId != 0) {
            try {
                soundPool?.play(bellSoundId, bellVolume, bellVolume, 1, 0, 1.0f)
            } catch (e: Exception) {
                Log.e("SoundManager", "playBellSound failed", e)
            }
        }
    }

    fun startHum() {
        try {
            if (humPlayer?.isPlaying == false) {
                humPlayer?.start()
            }
        } catch (e: Exception) {
            Log.e("SoundManager", "Error starting hum", e)
        }
    }

    fun pauseHum() {
        try {
            if (humPlayer?.isPlaying == true) {
                humPlayer?.pause()
            }
        } catch (e: Exception) {
            Log.e("SoundManager", "Error pausing hum", e)
        }
    }

    fun triggerHapticFeedback() {
        try {
            if (vibrator?.hasVibrator() != true) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(10)
            }
        } catch (e: Exception) {
            Log.e("SoundManager", "triggerHapticFeedback failed", e)
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        humPlayer?.release()
        humPlayer = null
    }
}
