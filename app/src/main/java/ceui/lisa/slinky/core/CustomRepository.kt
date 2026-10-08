package ceui.lisa.slinky.core

import ceui.lisa.slinky.ui.NavFragment
import kotlinx.coroutines.CoroutineScope

open class CustomRepository<FragmentT : NavFragment> : Repository<FragmentT>() {

    override suspend fun suspendRefresh(
        fragment: FragmentT
    ) {

    }

    override suspend fun suspendLoadMore(
        fragment: FragmentT
    ) {

    }
}