package ceui.lisa.slinky.ui.task

import ceui.lisa.slinky.models.IllustResponse
import kotlinx.coroutines.delay

class FetchPageTask(
    private val nextUrl: String?,
    private val loader: suspend () -> IllustResponse,
    private val nextLoader: suspend (String) -> IllustResponse
) : SlinkyTask<IllustResponse>() {

    override suspend fun action(): IllustResponse {
        val url = nextUrl
        val resp = if (url?.isNotEmpty() == true) {
            nextLoader(url)
        } else {
            loader()
        }
        delay(5000L)
        onTaskComplete(resp)
        return resp
    }
}