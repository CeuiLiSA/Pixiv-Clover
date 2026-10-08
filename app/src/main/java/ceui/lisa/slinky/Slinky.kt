package ceui.lisa.slinky

import android.app.Application
import ceui.lisa.slinky.core.PreferencePool
import ceui.lisa.slinky.network.RoomDB
import ceui.lisa.slinky.network.Settings
import ceui.lisa.slinky.utils.SoundPlay
import com.tencent.mmkv.MMKV
import timber.log.Timber


class Slinky : Application() {

    override fun onCreate() {
        MMKV.initialize(this)
        super.onCreate()
        RoomDB.attach(this)
        Settings.loadSettings()
        SoundPlay.init(this)
        Timber.plant(Timber.DebugTree())
        PreferencePool.load()
    }
}