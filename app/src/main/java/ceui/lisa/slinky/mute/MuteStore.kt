package ceui.lisa.slinky.mute

import ceui.lisa.slinky.models.Illust
import ceui.lisa.slinky.models.Novel
import ceui.lisa.slinky.models.Tag
import ceui.lisa.slinky.models.TrendingTag
import ceui.lisa.slinky.network.RoomDB
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

/**
 * 列表怎么对待屏蔽名单。
 *
 * - [Default]：命中标签 / 作者 / 作品任一条就从列表里拿掉。
 * - [ExemptUser]：某个作者自己的作品页。用户是主动点进来的，不因「屏蔽了这个作者」把整页清空，
 *   但标签和单个作品的屏蔽照常生效。
 * - [Disabled]：系列这种按顺序编号的列表，抽掉一话会让序号错位，整页不过滤。
 */
sealed interface MuteRule {
    object Default : MuteRule
    class ExemptUser(val userId: Long) : MuteRule
    object Disabled : MuteRule
}

/** 能被屏蔽名单从列表里拿掉的条目，[muteSubject] 是它背后的 [Illust] / [Novel]。 */
interface MuteSubjectItem {
    val muteSubject: Any
}

/**
 * 本地屏蔽名单。真源是 Room 的 `mute_record` 表，内存里按类型各存一份 key 集合，
 * 判定跑在列表加载的路径上，不能每条都查库。
 *
 * 写入先改内存再落库，并且串行执行：一次屏蔽紧跟一次取消时，DELETE 不会跑到 INSERT 前面去
 * 让那行复活。每次名单变化都发一次 [changes]，已经加载好的列表据此把新命中的条目拿掉。
 */
object MuteStore {

    private val keysByType = mapOf(
        MuteType.TAG to ConcurrentHashMap.newKeySet<String>(),
        MuteType.USER to ConcurrentHashMap.newKeySet<String>(),
        MuteType.ILLUST to ConcurrentHashMap.newKeySet<String>(),
        MuteType.NOVEL to ConcurrentHashMap.newKeySet<String>(),
    )

    private val loaded = CompletableDeferred<Unit>()
    private val writeLock = Mutex()

    private val _changes = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val changes: SharedFlow<Unit> = _changes

    fun load() {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                RoomDB.db().muteDao().getAll().forEach { record ->
                    keysByType[record.type]?.add(record.key)
                }
            } catch (ex: Exception) {
                Timber.e(ex)
            } finally {
                loaded.complete(Unit)
            }
        }
    }

    suspend fun awaitLoaded() {
        loaded.await()
    }

    fun isMuted(type: Int, key: String): Boolean {
        return keysByType[type]?.contains(key) == true
    }

    fun isTagMuted(name: String?): Boolean {
        val key = tagKey(name) ?: return false
        return isMuted(MuteType.TAG, key)
    }

    fun isUserMuted(userId: Long?): Boolean {
        return userId != null && userId > 0L && isMuted(MuteType.USER, userId.toString())
    }

    fun shouldHide(item: Any?, rule: MuteRule = MuteRule.Default): Boolean {
        return when (rule) {
            MuteRule.Disabled -> false
            else -> when (item) {
                is Illust -> isMuted(MuteType.ILLUST, item.id.toString()) ||
                        isAuthorHidden(item.user?.id, rule) ||
                        hasMutedTag(item.tags)

                is Novel -> isMuted(MuteType.NOVEL, item.id.toString()) ||
                        isAuthorHidden(item.user?.id, rule) ||
                        hasMutedTag(item.tags)

                is TrendingTag -> isTagMuted(item.tag)
                else -> false
            }
        }
    }

    private fun isAuthorHidden(userId: Long?, rule: MuteRule): Boolean {
        if (rule is MuteRule.ExemptUser && rule.userId == userId) {
            return false
        }
        return isUserMuted(userId)
    }

    private fun hasMutedTag(tags: List<Tag>?): Boolean {
        return tags?.any { isTagMuted(it.name) } == true
    }

    /**
     * 调用方多半在页面的 lifecycleScope 里，页面一退就取消。内存和落库必须一起做完，
     * 否则内存改了、库没写，重启后这次屏蔽 / 取消就丢了。
     */
    suspend fun mute(type: Int, key: String, displayName: String) {
        awaitLoaded()
        writeLock.withLock {
            withContext(NonCancellable) {
                keysByType[type]?.add(key)
                _changes.tryEmit(Unit)
                withContext(Dispatchers.IO) {
                    RoomDB.db().muteDao().insert(
                        MuteRecord(type, key, displayName, System.currentTimeMillis())
                    )
                }
            }
        }
    }

    suspend fun unmute(type: Int, key: String) {
        awaitLoaded()
        writeLock.withLock {
            withContext(NonCancellable) {
                keysByType[type]?.remove(key)
                _changes.tryEmit(Unit)
                withContext(Dispatchers.IO) {
                    RoomDB.db().muteDao().delete(type, key)
                }
            }
        }
    }

    suspend fun allRecords(): List<MuteRecord> {
        awaitLoaded()
        return writeLock.withLock {
            withContext(Dispatchers.IO) {
                RoomDB.db().muteDao().getAll()
            }
        }
    }

    fun tagKey(name: String?): String? {
        return name?.trim()?.takeIf { it.isNotEmpty() }
    }
}
