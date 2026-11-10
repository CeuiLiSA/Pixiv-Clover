package ceui.lisa.slinky.core

import ceui.lisa.slinky.models.Illust
import ceui.lisa.slinky.mute.MuteRule
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.network.paging.ListShow
import ceui.lisa.slinky.ui.IllustItem
import ceui.lisa.slinky.ui.NavFragment

open class IllustListRepository<FragmentT : NavFragment>(
    muteRule: MuteRule = MuteRule.Default,
    loader: suspend () -> ListShow<Illust>,
) : PixivListRepository<Illust, FragmentT>(
    loader = loader,
    dataMapper = { IllustItem(it) },
    muteRule = muteRule,
)