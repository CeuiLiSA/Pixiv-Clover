package ceui.lisa.slinky.glide

import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.Okio

class ProgressResponseBody(
    private val responseBody: ResponseBody,
    url: String
) : ResponseBody() {

    private val listener = GlideProgress.get(url)

    private val bufferedSource =
        Okio.buffer(object : ForwardingSource(responseBody.source()) {
            private var totalBytesRead = 0L
            private var currentProgress = 0

            override fun read(sink: Buffer, byteCount: Long): Long {
                return super.read(sink, byteCount).apply {
                    if (this == -1L) {
                        totalBytesRead = contentLength()
                    } else {
                        totalBytesRead += this
                    }
                    val progress = (HUNDRED * totalBytesRead / contentLength()).toInt()
                    if (progress != currentProgress) {
                        currentProgress = progress
                        listener?.onProgress(currentProgress)
                    }
                }
            }
        })

    override fun contentLength() = responseBody.contentLength()

    override fun contentType() = responseBody.contentType()

    override fun source(): BufferedSource = bufferedSource
}

const val HUNDRED = 100f