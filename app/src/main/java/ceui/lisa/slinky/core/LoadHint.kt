package ceui.lisa.slinky.core

sealed class LoadHint {

    object TopProgress : LoadHint() // 下拉刷新的 progress

    object CenterProgress : LoadHint() // 初次进入页面，或者 error retry 时 屏幕中间的 progress

    object BottomProgress : LoadHint() // 列表加载下一页，屏幕底部的 progress
}