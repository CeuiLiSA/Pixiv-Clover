package ceui.lisa.slinky.ui

import android.widget.ImageView
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.R
import ceui.lisa.slinky.databinding.ItemViewHistoryBinding
import ceui.lisa.slinky.db.ViewHistory
import ceui.lisa.slinky.dipToPx
import ceui.lisa.slinky.models.AccountResponse
import ceui.lisa.slinky.models.Illust
import ceui.lisa.slinky.models.Novel
import ceui.lisa.slinky.models.User
import ceui.lisa.slinky.network.RoomDB
import ceui.lisa.slinky.network.Util
import ceui.lisa.slinky.utils.toGlideUrl
import ceui.lisa.slinky.utils.visibleOrInvisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class HistoryHolder(val user: User, val history: ViewHistory) : SlinkyItem() {

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return history.objectId == (other as? HistoryHolder)?.history?.objectId
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return history == (other as? HistoryHolder)?.history
    }
}


@ItemHolder(HistoryHolder::class)
class HistoryViewHolder(aa: ItemViewHistoryBinding) :
    SlinkyViewHolder<ItemViewHistoryBinding, HistoryHolder>(aa) {

    override fun onBindViewHolder(item: HistoryHolder) {
        super.onBindViewHolder(item)

        binding.root.setOnClick {
            it.findActionReceiverOrNull<HistoryAction>()?.showHistory(item.history)
        }

        val user = item.user

        binding.historyHint.text = context.getString(R.string.view_user_profile, user.name)
        Glide.with(context)
            .load(user.profile_image_urls?.medium?.toGlideUrl())
            .into(binding.imageView)


        binding.viewTime.text = Util.timeFormat.format(Date(item.history.visitTime))
    }
}

object HistoryType {
    const val ILLUST = 1
    const val USER = 2
    const val NOVEL = 3
    const val SEARCH_KEYWORD = 5
    const val RECENT_USER = 6
}

fun NavFragment.visitIllust(illust: Illust) = launch {
    val viewHistory = ViewHistory(
        illust.id,
        System.currentTimeMillis(),
        Util.gson.toJson(illust),
        HistoryType.ILLUST
    )
    withContext(Dispatchers.IO) {
        RoomDB.db().historyDao().insertViewHistory(viewHistory)
    }
}

fun addRecentLoggedInUser(accountResponse: AccountResponse) {
    val user = accountResponse.user ?: return
    val viewHistory = ViewHistory(
        user.id,
        System.currentTimeMillis(),
        Util.gson.toJson(accountResponse),
        HistoryType.RECENT_USER
    )
    MainScope().launch(Dispatchers.IO) {
        RoomDB.db().historyDao().insertViewHistory(viewHistory)
    }
}

fun NavFragment.visitUser(user: User) = launch {
    val viewHistory = ViewHistory(
        user.id,
        System.currentTimeMillis(),
        Util.gson.toJson(user),
        HistoryType.USER
    )
    withContext(Dispatchers.IO) {
        RoomDB.db().historyDao().insertViewHistory(viewHistory)
    }
}

fun NavFragment.visitNovel(novel: Novel) = launch {
    val viewHistory = ViewHistory(
        novel.id,
        System.currentTimeMillis(),
        Util.gson.toJson(novel),
        HistoryType.NOVEL
    )
    withContext(Dispatchers.IO) {
        RoomDB.db().historyDao().insertViewHistory(viewHistory)
    }
}