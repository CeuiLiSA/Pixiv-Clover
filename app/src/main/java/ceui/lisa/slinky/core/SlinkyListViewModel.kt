package ceui.lisa.slinky.core

import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import ceui.lisa.slinky.ui.NavFragment
import ceui.lisa.slinky.ui.SlinkyItem
import ceui.lisa.slinky.ui.slinkyViewModels

class SlinkyListViewModel<FragmentT : NavFragment>(
    private val _repository: Repository<FragmentT>
) : ViewModel() {

    val refreshState: LiveData<LoadState> = _repository.refreshState
    val loadMoreState: LiveData<LoadState> = _repository.loadMoreState
    val holderList: LiveData<List<SlinkyItem>> = _repository.holderList
    private var isInitialLoaded = false

    init {
        _repository.coroutineScope = viewModelScope
    }

    fun attachFragment(fragmentT: FragmentT) {
        _repository.attachFragment(fragmentT)
        if (!isInitialLoaded) {
            isInitialLoaded = true
            refresh(RefreshHint.initialLoad(), fragmentT)
        }
    }

    fun refresh(refreshHint: RefreshHint, fragmentT: FragmentT) {
        _repository.dispatchRefresh(fragmentT, refreshHint)
    }

    fun loadMore(fragmentT: FragmentT) {
        _repository.dispatchLoadMore(fragmentT)
    }
}


inline fun <FragmentT : NavFragment> FragmentT.slinkyListVM(
    noinline ownerProducer: (() -> ViewModelStoreOwner),
    noinline keyProducer: () -> String,
    crossinline repositoryFactory: () -> Repository<FragmentT>
): Lazy<SlinkyListViewModel<FragmentT>> {
    return this.slinkyViewModels(
        keyProducer = keyProducer,
        ownerProducer = ownerProducer
    ) {
        val repository = repositoryFactory.invoke()
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SlinkyListViewModel(repository) as T
            }
        }
    }
}

inline fun <FragmentT : NavFragment, ArgT1 : Any> FragmentT.listViewModel(
    crossinline arg1Producer1: () -> ArgT1,
    crossinline repositoryFactory: (arg1: ArgT1) -> Repository<FragmentT>
): Lazy<SlinkyListViewModel<FragmentT>> {
    return this.viewModels {
        val arg1 = arg1Producer1()
        val repository = repositoryFactory.invoke(arg1)
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SlinkyListViewModel(repository) as T
            }
        }
    }
}

inline fun <FragmentT : NavFragment, ArgT1 : Any, ArgT2 : Any> FragmentT.listViewModel(
    crossinline arg1Producer1: () -> ArgT1,
    crossinline arg1Producer2: () -> ArgT2,
    crossinline repositoryFactory: (arg1: ArgT1, arg2: ArgT2) -> Repository<FragmentT>
): Lazy<SlinkyListViewModel<FragmentT>> {
    return this.viewModels {
        val arg1 = arg1Producer1()
        val arg2 = arg1Producer2()
        val repository = repositoryFactory.invoke(arg1, arg2)
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SlinkyListViewModel(repository) as T
            }
        }
    }
}

inline fun <FragmentT : NavFragment, ArgT1 : Any, ArgT2 : Any, ArgT3 : Any> FragmentT.listViewModel(
    crossinline arg1Producer1: () -> ArgT1,
    crossinline arg1Producer2: () -> ArgT2,
    crossinline arg1Producer3: () -> ArgT3,
    crossinline repositoryFactory: (arg1: ArgT1, arg2: ArgT2, arg3: ArgT3) -> Repository<FragmentT>
): Lazy<SlinkyListViewModel<FragmentT>> {
    return this.viewModels {
        val arg1 = arg1Producer1()
        val arg2 = arg1Producer2()
        val arg3 = arg1Producer3()
        val repository = repositoryFactory.invoke(arg1, arg2, arg3)
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SlinkyListViewModel(repository) as T
            }
        }
    }
}

inline fun <FragmentT : NavFragment> FragmentT.listViewModel(
    crossinline repositoryFactory: () -> Repository<FragmentT>
): Lazy<SlinkyListViewModel<FragmentT>> {
    return this.viewModels {
        val repository = repositoryFactory.invoke()
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SlinkyListViewModel(repository) as T
            }
        }
    }
}