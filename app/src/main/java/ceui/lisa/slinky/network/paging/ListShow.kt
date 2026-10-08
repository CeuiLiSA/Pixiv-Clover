package ceui.lisa.slinky.network.paging


interface ListShow<T> {

    val displayList: List<T>

    val nextPageUrl: String?
}

