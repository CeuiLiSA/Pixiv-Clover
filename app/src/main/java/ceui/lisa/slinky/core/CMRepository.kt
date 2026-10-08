package ceui.lisa.slinky.core

import androidx.recyclerview.widget.RecyclerView
import ceui.lisa.slinky.models.AddCommentResponse
import ceui.lisa.slinky.models.Comment
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.ui.CommentHolder
import ceui.lisa.slinky.ui.smoothScrollToTopIfNeeded

class CMRepository(
    private val illustId: Long,
    private val authorId: Long
) : PixivListRepository<Comment, CMFragment>(
    loader = { Client.appApi.commentList(illustId) },
    dataMapper = { comment -> CommentHolder(authorId, comment, ObjectPool.get(comment.user.id)) }
) {

    private val replyCommentMap = mutableMapOf<Long, List<Comment>>()

    override suspend fun applyRefreshData(
        fragment: CMFragment,
        displayList: List<Comment>
    ) {
        holderList.value = displayList.map { comment ->
            CommentHolder(
                authorId,
                comment,
                ObjectPool.get(comment.user.id),
                replyCommentMap[comment.id] ?: listOf()
            )
        }
    }

    override suspend fun applyLoadMoreData(
        fragment: CMFragment,
        displayList: List<Comment>
    ) {
        val pages = toMutableList()
        pages.addAll(displayList.map { comment ->
            CommentHolder(
                authorId,
                comment,
                ObjectPool.get(comment.user.id),
                replyCommentMap[comment.id] ?: listOf()
            )
        })
        holderList.value = pages
    }

    suspend fun onCommentSubmitted(
        parentComment: Comment?,
        resp: AddCommentResponse,
        list: RecyclerView
    ) {
        val comment = resp.comment ?: return
        if (parentComment?.id != null) {
            showReplyImpl(parentComment)
        } else {
            val items = toMutableList()
            items.add(
                0, CommentHolder(
                    authorId,
                    comment,
                    ObjectPool.get(comment.user.id)
                )
            )
            holderList.value = items
            val state = refreshState.value
            if (state is LoadState.LOADED && !state.hasContent) {
                refreshState.value = LoadState.LOADED(hasContent = true, hasNext = false)
            }
            list.postDelayed({
                list.smoothScrollToTopIfNeeded()
            }, 200L)
        }
    }

    suspend fun showReplyImpl(parentComment: Comment) {
        val items = toMutableList()
        val index = items.indexOfFirst { item ->
            (item as? CommentHolder)?.comment?.id == parentComment.id
        }
        if (index >= 0) {
            items.removeAt(index)
            val resp = Client.appApi.replyList(parentComment.id)
            resp.comments.forEach {
                ObjectPool.update(it.user)
            }
            replyCommentMap[parentComment.id] = resp.comments
            items.add(
                index, CommentHolder(
                    authorId,
                    parentComment,
                    ObjectPool.get(parentComment.user.id),
                    resp.comments
                )
            )
            holderList.value = items
        }
    }
}