package ceui.lisa.slinky.core

import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlin.reflect.KClass


class KeyedViewModelLazy<VM : ViewModel>(
    private val keyPrefixProvider: () -> String,
    private val viewModelClass: KClass<VM>,
    private val storeProducer: () -> ViewModelStore,
    private val factoryProducer: () -> ViewModelProvider.Factory
) : Lazy<VM> {
    private var cached: VM? = null

    override val value: VM
        get() {
            val viewModel = cached
            return if (viewModel == null) {
                val factory = factoryProducer()
                val store = storeProducer()
                val canonicalName: String = viewModelClass.java.canonicalName
                    ?: throw IllegalArgumentException("Local and anonymous classes can not be ViewModels")

                ViewModelProvider(store, factory).get(
                    "${keyPrefixProvider()} : $canonicalName",
                    viewModelClass.java
                ).also {
                    cached = it
                }
            } else {
                viewModel
            }
        }

    override fun isInitialized() = cached != null
}

@MainThread
fun <VM : ViewModel> Fragment.createKeyedViewModelLazy(
    keyPrefixProvider: () -> String,
    viewModelClass: KClass<VM>,
    storeProducer: () -> ViewModelStore,
    factoryProducer: (() -> ViewModelProvider.Factory)? = null
): Lazy<VM> {
    val factoryPromise = factoryProducer ?: {
        defaultViewModelProviderFactory
    }
    return KeyedViewModelLazy(keyPrefixProvider, viewModelClass, storeProducer, factoryPromise)
}


@MainThread
inline fun <reified VM : ViewModel> Fragment.viewModels(
    keyPrefix: String,
    noinline ownerProducer: () -> ViewModelStoreOwner = { this },
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
) = createKeyedViewModelLazy(
    { keyPrefix },
    VM::class,
    { ownerProducer().viewModelStore },
    factoryProducer
)

inline fun <reified ValueT : Any> Fragment.valueViewModel(
    noinline loader: suspend () -> ValueT
): Lazy<ValueViewModel<ValueT>> {
    return this.viewModels(ValueT::class.java.canonicalName!!, { this }) {
        val fragment = this
        object : AbstractSavedStateViewModelFactory(fragment, null) {
            override fun <T : ViewModel> create(
                key: String,
                modelClass: Class<T>,
                handle: SavedStateHandle
            ): T {
                return ValueViewModel({ loader() }) as T
            }
        }
    }
}


open class ValueViewModel<ValueT>(
    private val loader: suspend () -> ValueT
) : ViewModel() {

    private val _refreshState = MutableLiveData<LoadState>()
    val refreshState: LiveData<LoadState> = _refreshState

    private val _result = MutableLiveData<ValueT>()
    val result: LiveData<ValueT> = _result

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                _refreshState.value = LoadState.LOADING()
                val value = loader()
                if (value != null) {
                    _result.value = requireNotNull(value)
                }
                _refreshState.value = LoadState.LOADED(hasContent = true)
            } catch (ex: Exception) {
                _refreshState.value = LoadState.ERROR(ex)
            }
        }
    }

}