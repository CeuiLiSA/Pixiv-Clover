package ceui.lisa.slinky.core

class FrequencyControl(
    private val taskId: Long = 0L,
    private val intervalTime: Long = DEFAULT_INTERVAL_TIME,
    private val block: () -> Unit
) {
    fun execute() {
        val now = System.currentTimeMillis()
        val lastExecutedTime = lastExecutedTimeMap[taskId] ?: 0L
        if (now - lastExecutedTime > intervalTime) {
            lastExecutedTimeMap[taskId] = now
            block.invoke()
        }
    }

    companion object {
        private val lastExecutedTimeMap = hashMapOf<Long, Long>()
        const val DEFAULT_INTERVAL_TIME = 500L
    }
}