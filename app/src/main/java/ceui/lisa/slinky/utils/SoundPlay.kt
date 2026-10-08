package ceui.lisa.slinky.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool

object SoundPlay {

    private lateinit var soundPool: SoundPool
    private lateinit var applicationContext: Context

    fun init(context: Context) {
        applicationContext = context
        val builder = SoundPool.Builder()
        builder.setMaxStreams(1)
        val attrBuilder: AudioAttributes.Builder = AudioAttributes.Builder()
        attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC)
        builder.setAudioAttributes(attrBuilder.build())
        soundPool = builder.build()
        soundPool.setOnLoadCompleteListener { soundPool, sampleId, status ->
            if (status == 0) {
                soundPool.play(sampleId, 1f, 1f, 1, 0, 1f)
            }
        }
    }

    fun play(resId: Int) {
        soundPool.load(applicationContext, resId, 1)
    }
}