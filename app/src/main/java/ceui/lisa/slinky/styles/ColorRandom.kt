package ceui.lisa.slinky.styles

import kotlin.math.abs


object ColorRandom {

    private val colorList = listOf(
        "#FF5C01",
        "#6120EE",
        "#1EA143",
        "#CA00EA",
        "#28C5F3",
        "#FF9E2D",
        "#22EAA7",
        "#FF59BC",
        "#F8D000",
        "#3360FF",
        "#ef9a9a",
        "#f48fb1",
        "#ba68c8",
        "#9575cd",
        "#5c6bc0",
        "#64b5f6",
        "#4fc3f7",
        "#4dd0e1",
        "#80cbc4",
        "#81c784",
        "#aed581",
        "#1E9E97",
        "#407FC5",
        "#6F52C1",
        "#B939BD",
        "#F29B41",
        "#FF2E7E",
        "#FF7335",
        "#ECAC16",
        "#6FC445",
        "#00CFA5",
        "#0AB2F6",
        "#737AFF",
        "#A465FF",
        "#D33EFF",
        "#FC38CF"
    )

    fun randomColor(string: String?): String {
        return colorList[abs(string?.hashCode() ?: 0) % colorList.size]
    }
}
