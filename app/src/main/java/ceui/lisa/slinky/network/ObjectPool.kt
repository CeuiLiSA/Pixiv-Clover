package ceui.lisa.slinky.network

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ceui.lisa.slinky.models.Illust
import ceui.lisa.slinky.models.ModelObject
import ceui.lisa.slinky.models.UserPreview
import java.io.Serializable
import kotlin.reflect.KClass

data class ObjectKey(
    val id: Long,
    val classSpec: KClass<out Any>
) : Serializable

object ObjectPool : LiveDataPool() {

    fun putUserPreview(preview: UserPreview) {
        preview.user?.let { user ->
            update(user)
        }

        preview.illusts?.forEach { illust ->
            update(illust)
        }
    }

    fun updateIllust(illust: Illust) {
        update(illust)
        illust.user?.let { user ->
            update(user)
        }
    }
}

open class LiveDataPool {


    private val _store = hashMapOf<ObjectKey, MutableLiveData<Any>>()

    fun getInternalRecord(id: Long, classSpec: KClass<*>): MutableLiveData<Any> {
        val key = ObjectKey(id, classSpec)
        val existing = _store[key]
        return if (existing != null) {
            existing
        } else {
            val newly = MutableLiveData<Any>()
            _store[key] = newly
            newly
        }
    }

    inline fun <reified ObjectT : ModelObject> get(id: Long): LiveData<ObjectT> {
        return getInternalRecord(id, ObjectT::class) as LiveData<ObjectT>
    }

    fun <ObjectT : ModelObject> update(obj: ObjectT) {
        val liveData = getInternalRecord(obj.objectUniqueId, obj::class)
        try {
            liveData.value = obj
        } catch (ex: Exception) {
            liveData.postValue(obj)
        }
    }

    fun <ObjectT : ModelObject> postUpdate(obj: ObjectT) {
        val liveData = getInternalRecord(obj.objectUniqueId, obj::class)
        liveData.postValue(obj)
    }
}