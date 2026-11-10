package ceui.lisa.slinky.mute

import android.view.View
import ceui.lisa.slinky.R
import ceui.lisa.slinky.models.Illust
import ceui.lisa.slinky.models.Novel
import ceui.lisa.slinky.models.Tag
import ceui.lisa.slinky.models.User
import ceui.lisa.slinky.requireLoggedInUserId
import ceui.lisa.slinky.showPush
import ceui.lisa.slinky.ui.NavFragment
import ceui.lisa.slinky.ui.dialog.Action
import ceui.lisa.slinky.ui.dialog.alertYesOrCancel
import ceui.lisa.slinky.ui.dialog.showActionMenu
import ceui.lisa.slinky.ui.launch

fun NavFragment.showIllustMuteMenu(sender: View, illust: Illust) {
    showWorkMuteMenu(
        sender,
        workType = MuteType.ILLUST,
        workId = illust.id,
        workTitle = illust.title,
        author = illust.user,
        tags = illust.tags,
    )
}

fun NavFragment.showNovelMuteMenu(sender: View, novel: Novel) {
    showWorkMuteMenu(
        sender,
        workType = MuteType.NOVEL,
        workId = novel.id,
        workTitle = novel.title,
        author = novel.user,
        tags = novel.tags,
    )
}

private fun NavFragment.showWorkMuteMenu(
    sender: View,
    workType: Int,
    workId: Long,
    workTitle: String?,
    author: User?,
    tags: List<Tag>?,
) {
    launch {
        MuteStore.awaitLoaded()
        val tagNames = tags.orEmpty().mapNotNull { MuteStore.tagKey(it.name) }.distinct()
        showActionMenu(sender) {
            buildList {
                val workKey = workId.toString()
                if (MuteStore.isMuted(workType, workKey)) {
                    add(Action(getString(R.string.unmute_work)) {
                        unmuteImpl(workType, workKey, workTitle.orEmpty())
                    })
                } else {
                    add(Action(getString(R.string.mute_work)) {
                        muteImpl(
                            workType,
                            workKey,
                            workTitle.orEmpty(),
                            getString(R.string.mute_work_hint)
                        )
                    })
                }
                if (author != null && author.id > 0L && author.id != requireLoggedInUserId()) {
                    if (MuteStore.isUserMuted(author.id)) {
                        add(Action(getString(R.string.unmute_author)) { toggleUserMute(author) })
                    } else {
                        add(Action(getString(R.string.mute_author)) { toggleUserMute(author) })
                    }
                }
                if (tagNames.isNotEmpty()) {
                    add(Action(getString(R.string.mute_tags)) {
                        showTagMuteMenu(sender, tagNames)
                    })
                }
            }
        }
    }
}

/** pixiv 每件作品最多 10 个标签，一屏的弹出菜单放得下。 */
private fun NavFragment.showTagMuteMenu(sender: View, tagNames: List<String>) {
    showActionMenu(sender) {
        tagNames.map { name ->
            val label = if (MuteStore.isTagMuted(name)) {
                getString(R.string.unmute_tag_named, name)
            } else {
                "#$name"
            }
            Action(label) { toggleTagMute(name) }
        }
    }
}

fun NavFragment.toggleTagMute(tagName: String) {
    val key = MuteStore.tagKey(tagName) ?: return
    if (MuteStore.isTagMuted(key)) {
        unmuteImpl(MuteType.TAG, key, key)
    } else {
        muteImpl(MuteType.TAG, key, key, getString(R.string.mute_tag_hint, key))
    }
}

fun NavFragment.toggleUserMute(user: User) {
    val key = user.id.toString()
    val name = user.name.orEmpty()
    if (MuteStore.isUserMuted(user.id)) {
        unmuteImpl(MuteType.USER, key, name)
    } else {
        muteImpl(MuteType.USER, key, name, getString(R.string.mute_author_hint, name))
    }
}

private fun NavFragment.muteImpl(type: Int, key: String, displayName: String, hint: String) {
    launch {
        if (alertYesOrCancel(message = hint)) {
            MuteStore.mute(type, key, displayName)
            showPush(title = getString(R.string.muted_success), body = displayName)
        }
    }
}

private fun NavFragment.unmuteImpl(type: Int, key: String, displayName: String) {
    launch {
        MuteStore.unmute(type, key)
        showPush(title = getString(R.string.unmuted_success), body = displayName)
    }
}
