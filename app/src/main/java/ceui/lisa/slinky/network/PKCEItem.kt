package ceui.lisa.slinky.network

data class PKCEItem(
    val verify: String,
    val challenge: String,
)