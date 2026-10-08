package ceui.lisa.slinky.ui

import androidx.annotation.MainThread
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.databinding.ItemCommonHeaderBinding
import com.blankj.utilcode.util.BarUtils
import kotlin.reflect.KClass


class SpaceHolder(val height: Int) : SlinkyItem() {


    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return height == (other as? SpaceHolder)?.height
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return height == (other as? SpaceHolder)?.height
    }
}


@ItemHolder(SpaceHolder::class)
class SpaceViewHolder(aa: ItemCommonHeaderBinding) :
    SlinkyViewHolder<ItemCommonHeaderBinding, SpaceHolder>(aa) {

    override fun onBindViewHolder(item: SpaceHolder) {
        super.onBindViewHolder(item)
        binding.headerLayout.updateLayoutParams {
            height = item.height
        }
    }
}

@MainThread
inline fun <reified VM : ViewModel> Fragment.slinkyViewModels(
    noinline keyProducer: () -> String,
    noinline ownerProducer: () -> ViewModelStoreOwner = { this },
    noinline extrasProducer: (() -> CreationExtras)? = null,
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
): Lazy<VM> {
    val owner by lazy(LazyThreadSafetyMode.NONE) { ownerProducer() }
    return createSlinkyViewModelLazy(
        VM::class,
        { owner.viewModelStore },
        {
            extrasProducer?.invoke()
                ?: (owner as? HasDefaultViewModelProviderFactory)?.defaultViewModelCreationExtras
                ?: CreationExtras.Empty
        },
        factoryProducer ?: {
            (owner as? HasDefaultViewModelProviderFactory)?.defaultViewModelProviderFactory
                ?: defaultViewModelProviderFactory
        },
        keyProducer
    )
}

@MainThread
fun <VM : ViewModel> Fragment.createSlinkyViewModelLazy(
    viewModelClass: KClass<VM>,
    storeProducer: () -> ViewModelStore,
    extrasProducer: () -> CreationExtras = { defaultViewModelCreationExtras },
    factoryProducer: (() -> ViewModelProvider.Factory)? = null,
    keyProducer: () -> String
): Lazy<VM> {
    val factoryPromise = factoryProducer ?: {
        defaultViewModelProviderFactory
    }
    return SlinkyViewModelLazy(
        viewModelClass,
        storeProducer,
        factoryPromise,
        extrasProducer,
        keyProducer
    )
}

class SlinkyViewModelLazy<VM : ViewModel> @JvmOverloads constructor(
    private val viewModelClass: KClass<VM>,
    private val storeProducer: () -> ViewModelStore,
    private val factoryProducer: () -> ViewModelProvider.Factory,
    private val extrasProducer: () -> CreationExtras = { CreationExtras.Empty },
    private val keyProducer: () -> String
) : Lazy<VM> {
    private var cached: VM? = null

    override val value: VM
        get() {
            val viewModel = cached
            return if (viewModel == null) {
                val factory = factoryProducer()
                val store = storeProducer()
                ViewModelProvider(
                    store,
                    factory,
                    extrasProducer()
                )[keyProducer(), viewModelClass.java].also {
                    cached = it
                }
            } else {
                viewModel
            }
        }

    override fun isInitialized(): Boolean = cached != null
}
