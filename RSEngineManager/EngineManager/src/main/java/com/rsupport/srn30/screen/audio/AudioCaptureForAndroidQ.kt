package com.rsupport.srn30.screen.audio

import android.annotation.TargetApi
import android.media.*
import android.media.projection.MediaProjection
import android.os.Build
import com.rsupport.util.rslog.MLog
import java.nio.ByteBuffer

@TargetApi(Build.VERSION_CODES.Q)
class AudioCaptureForAndroidQ(val mediaProjection: MediaProjection?) {
    private var state: RecordState = RecordState.STOP

    var readAudioSize: Int = 0

    private lateinit var zeroArray : ByteArray
    private lateinit var dropBuffer : ByteBuffer

    private lateinit var audioRecord: AudioRecord

    private var pcmDataTimDivider = 0.0

    private var saveDropTime: Long = 0
    private var dropTime: Long = 0
    private var isMute: Boolean = false

    init {
        MLog.d("AudioCaptureForAndroidQ")
    }

    fun initialized(sampleRate: Int, channelConfig: Int, audioFormat: Int): Boolean {
        val minBufferSize = AudioRecord.getMinBufferSize(
                sampleRate,
                channelConfig,
                audioFormat)

        MLog.d("audioMinBuffer : $minBufferSize")

        try {

            if (mediaProjection != null) {

                MLog.d("audioFormat : ${audioFormat.toString()}")

                val captureConfig = AudioPlaybackCaptureConfiguration.Builder(mediaProjection!!)
                        .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
                        .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                        .addMatchingUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .addMatchingUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION_SIGNALLING)
                        .addMatchingUsage(AudioAttributes.USAGE_ALARM)
                        .addMatchingUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .addMatchingUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .addMatchingUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_REQUEST)
                        .addMatchingUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                        .addMatchingUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_DELAYED)
                        .addMatchingUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                        .addMatchingUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                        .addMatchingUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                        .addMatchingUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .addMatchingUsage(AudioAttributes.USAGE_GAME)
                        .addMatchingUsage(AudioAttributes.USAGE_ASSISTANT)
                        .build()

                audioRecord = AudioRecord.Builder()
                        .setAudioFormat(
                                AudioFormat.Builder()
                                        .setEncoding(audioFormat)
                                        .setSampleRate(sampleRate)
                                        .setChannelMask(channelConfig)
                                        .build()
                        )
                        .setBufferSizeInBytes(minBufferSize)
                        .setAudioPlaybackCaptureConfig(captureConfig)
                        .build()
            } else {
                val inputType = MediaRecorder.AudioSource.MIC

                audioRecord = AudioRecord(
                        inputType,
                        sampleRate,
                        channelConfig,
                        audioFormat,
                        minBufferSize)
            }

        } catch (e: IllegalArgumentException) {
            MLog.e("checkInputAudio fail : " + e.message)
            return false
        }

        var channelCount = 2

        readAudioSize = 2048 * channelCount

        MLog.d("createAudioRecorder for Platform format($audioFormat) , read AudioSize($readAudioSize)")

        if (!checkInputAudio(audioRecord)) {
            MLog.e("checkInputAudio fail")
            return false
        }

        pcmDataTimDivider = (2.0 * sampleRate * channelCount)

        zeroArray = ByteArray(readAudioSize)
        dropBuffer = ByteBuffer.allocate(readAudioSize)

        return true
    }

    fun read(buffer: ByteArray, size: Int): ByteArray {
        MLog.d("read -> $state -> $size")
        return when(state){
            RecordState.RECORD -> {
//                audioRecord.readOrNull(buffer, size) ?: return 0
                audioRecord.read(buffer, 0, size)
                buffer
            }
            RecordState.MUTE -> {
                audioRecord.read(zeroArray, 0,  size)
//
//                buffer.put(zeroArray, 0, readSize)
//
//                buffer.capacity()
                zeroArray
            }
            RecordState.PAUSE -> {
                audioRecord.read(dropBuffer, size)
                dropBuffer.clear()
                byteArrayOf()
            }
            RecordState.STOP ->{
                byteArrayOf()
            }
        }
    }

    fun release() {
        state = RecordState.STOP
        audioRecord.release()
    }

    fun start(): Boolean {
        if(::audioRecord.isInitialized){
            return try {
                audioRecord.startRecording()

                state = RecordState.RECORD

                true
            } catch (e: Exception) {
                MLog.e(e)
                false
            }
        }
        return false
    }

    fun mute(isMute: Boolean) {
        this.isMute = isMute
        state = if(isMute) RecordState.MUTE else RecordState.RECORD
    }

    fun pause() {
        state = RecordState.PAUSE
        saveDropTime = System.currentTimeMillis() * 1000
    }

    fun resume() {
        state = if(isMute) RecordState.MUTE else RecordState.RECORD
        dropTime += System.currentTimeMillis() * 1000 - saveDropTime
    }

    private fun checkInputAudio(audioRecord: AudioRecord): Boolean {
        if (audioRecord.recordingState == AudioRecord.RECORDSTATE_STOPPED) {
            try {
                audioRecord.startRecording()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
                return false
            }

            if (audioRecord.recordingState == AudioRecord.RECORDSTATE_STOPPED) {
                MLog.e("audioRecord not started.")
                audioRecord.stop()
                return false
            }
        }
        audioRecord.stop()
        return true
    }

    private fun AudioRecord.readOrNull(buffer: ByteBuffer, size : Int) : Int?{
        return read(buffer, size).let{readSize ->
            if(readSize < 0) null else readSize
        }
    }
}

enum class RecordState{
    RECORD, PAUSE, MUTE, STOP
}