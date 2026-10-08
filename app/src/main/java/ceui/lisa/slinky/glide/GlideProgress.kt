package ceui.lisa.slinky.glide

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import ceui.lisa.slinky.ui.GlideState


interface ProgressListener {

    fun onProgress(progress: Int)

    fun onFailed(ex: Exception)
}


class LiveDataProgressListener(
    val progressLiveData: MutableLiveData<Int>,
    val glideState: MutableLiveData<Int>
) : ProgressListener {

    override fun onProgress(progress: Int) {
        progressLiveData.postValue(progress)
    }

    override fun onFailed(ex: Exception) {
        glideState.postValue(GlideState.FAILED)
    }
}

object GlideProgress {

    private val store = HashMap<String, ProgressListener>()

    fun add(url: String, lifecycle: Lifecycle? = null, listener: ProgressListener) {
        if (lifecycle != null) {
            lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    store.remove(url)
                }

                override fun onResume(owner: LifecycleOwner) {
                    store[url] = listener
                }
            })
        } else {
            store[url] = listener
        }
    }

    fun get(url: String): ProgressListener? {
        return store[url]
    }
}