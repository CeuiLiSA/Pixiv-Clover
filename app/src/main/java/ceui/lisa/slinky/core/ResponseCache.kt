package ceui.lisa.slinky.core

import ceui.lisa.slinky.network.Util
import ceui.lisa.slinky.network.requireGlobalPref
import com.google.gson.Gson
import timber.log.Timber

interface ResponseCache<ResponseT> {

    fun get(): ResponseT?

    fun put(responseT: ResponseT)
}

class PrefResponseCache<ResponseT>(
    private val classSpec: Class<ResponseT>,
    private val expiredTime: Long = 1000L * 60 * 30,
    private val prefKeyProducer: () -> String = { classSpec.simpleName }
) : ResponseCache<ResponseT> {

    private val prefStore by lazy { requireGlobalPref() }

    private val timeKey: String
        get() {
            return "${prefKeyProducer()}-last-saved-time"
        }
    private val jsonKey: String
        get() {
            return "${prefKeyProducer()}-last-saved-json"
        }

    override fun get(): ResponseT? {
        val now = System.currentTimeMillis()
        val lastSavedTime = prefStore.getLong(timeKey, 0L)
//        return if (((now - lastSavedTime) < expiredTime) && false) {
        return if (((now - lastSavedTime) < expiredTime)) {
            val lastSavedJson = prefStore.getString(jsonKey, "")
            try {
                Timber.d("PrefResponseCache hit ${jsonKey}")
                Util.gson.fromJson(lastSavedJson, classSpec)
            } catch (ex: Exception) {
                ex.printStackTrace()
                null
            }
        } else {
            null
        }
    }

    override fun put(responseT: ResponseT) {
        prefStore.putString(jsonKey, Util.gson.toJson(responseT))
        prefStore.putLong(timeKey, System.currentTimeMillis())
    }
}