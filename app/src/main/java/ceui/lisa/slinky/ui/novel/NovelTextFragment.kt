package ceui.lisa.slinky.ui.novel

import android.os.Bundle
import android.text.SpannableString
import android.view.View
import androidx.navigation.fragment.navArgs
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.core.CustomRepository
import ceui.lisa.slinky.core.LoadState
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.listViewModel
import ceui.lisa.slinky.core.setUpDisablePage
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.databinding.CellNovelChapterBinding
import ceui.lisa.slinky.databinding.CellNovelImageBinding
import ceui.lisa.slinky.databinding.CellNovelOneLineTextBinding
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.models.Illust
import ceui.lisa.slinky.models.Novel
import ceui.lisa.slinky.models.NovelImages
import ceui.lisa.slinky.models.WebNovel
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.ui.SlinkyItem
import ceui.lisa.slinky.ui.SlinkyViewHolder
import ceui.lisa.slinky.ui.SpaceHolder
import ceui.lisa.slinky.ui.screenHeight
import ceui.lisa.slinky.ui.setOnClick
import ceui.lisa.slinky.ui.visitNovel
import ceui.lisa.slinky.utils.toGlideUrl
import com.bumptech.glide.Glide
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.roundToInt

class NovelTextRepository(private val novelId: Long) : CustomRepository<NovelTextFragment>() {

    override suspend fun suspendRefresh(fragment: NovelTextFragment) {
        val holders = mutableListOf<SlinkyItem>()
        withContext(Dispatchers.IO) {
            val response = Client.appApi.getNovelText(novelId).execute()
            val html = response.body()?.string() ?: ""
            html.lines().forEach {
                if (it.contains("novel: {\"id\":")) {
                    val cleaned = it.trim()
                    val novelJson = cleaned.substring(7, cleaned.length - 1)
                    val webNovel = Gson().fromJson(novelJson, WebNovel::class.java)
                    val lines = webNovel.text?.split("\n") ?: listOf()
                    lines.forEach { s ->
                        val holder =
                            parseUploadedImages(s, webNovel) ?:
                            parsePixivImages(s, webNovel) ?:
                            parseChapter(s) ?:
                            parseNewPage(s) ?:
                            NovelOneLineTextHolder(s)
                        holders.add(holder)
                        parseJumpUrl(s)
                        Timber.d("novel one line $s")
                    }
                }
            }
        }
        holderList.value = holders
        refreshState.value = LoadState.LOADED(hasContent = true, hasNext = false)
    }


    private fun parseUploadedImages(s: String, webNovel: WebNovel): NovelImageHolder? {
        val uploadedImageMark = "[uploadedimage:"
        if (s.contains(uploadedImageMark)) {
            val startIndex: Int =
                s.indexOf(uploadedImageMark) + uploadedImageMark.length
            val endIndex: Int = s.indexOf("]")
            try {
                val id = s.substring(startIndex, endIndex).toLong()
                return NovelImageHolder(
                    NovelImageHolder.Type.UploadedImage,
                    id,
                    0,
                    webNovel
                )
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }

        return null
    }

    private fun parsePixivImages(s: String, webNovel: WebNovel): NovelImageHolder? {
        val pixivImageMark = "[pixivimage:"
        if (s.contains(pixivImageMark)) {
            val startIndex: Int =
                s.indexOf(pixivImageMark) + pixivImageMark.length
            val endIndex: Int = s.indexOf("]")
            val result: String = s.substring(startIndex, endIndex)
            var indexInIllust = 0
            try {
                val id: Long
                if (result.contains("-")) {
                    val ret = result.split("-")
                    indexInIllust = ret[1].toInt()
                    id = ret[0].toLong()
                } else {
                    id = result.toLong()
                }
                return NovelImageHolder(
                    NovelImageHolder.Type.PixivImage,
                    id,
                    indexInIllust,
                    webNovel
                )
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }

        return null
    }

    private fun parseJumpUrl(input: String): SpannableString? {
        val pattern = "\\[\\[jumpuri:(.*?) > (.*?)]]".toRegex()
        val matchResult = pattern.find(input)
        if (matchResult != null) {
            val (title, url) = matchResult.destructured
            Timber.d("parseJumpUrl 返回$title, $url")
            return null
        } else {
            Timber.d("parseJumpUrl 未找到匹配项")
            return null
        }
    }

    private fun parseChapter(input: String): NovelChapterHolder? {
        val pattern = "\\[chapter:(.*?)]".toRegex()
        val matchResult = pattern.find(input)

        if (matchResult != null) {
            val (chapterTitle) = matchResult.destructured
            return NovelChapterHolder(chapterTitle)
        }

        return null
    }

    private fun parseNewPage(input: String): SlinkyItem? {
        if ("[newpage]" == input.trim()) {
            return SpaceHolder((screenHeight / 2F).roundToInt())
        }

        return null
    }
}

class NovelTextFragment : SlinkyListFragment() {

    private val safeArgs by navArgs<NovelTextFragmentArgs>()
    private val listViewModel by listViewModel({ safeArgs.novelId }) { id -> NovelTextRepository(id) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ObjectPool.get<Novel>(safeArgs.novelId).observe(viewLifecycleOwner) {
            actionbarContent.title.value = it?.title
            visitNovel(it)
        }
        val binding = FragmentSlinkyListBinding.bind(view)
        setUpDisablePage(binding.deletedFrame)
        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, listViewModel)
    }

    override fun isAbandonedPage(): Boolean {
        val novel = ObjectPool.get<Novel>(safeArgs.novelId).value ?: return true
        return novel.isDisabled()
    }
}

class NovelOneLineTextHolder(val text: String) : SlinkyItem() {

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return text == (other as? NovelOneLineTextHolder)?.text
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return text == (other as? NovelOneLineTextHolder)?.text
    }
}


@ItemHolder(NovelOneLineTextHolder::class)
class NovelOneLineTextViewHolder(aa: CellNovelOneLineTextBinding) :
    SlinkyViewHolder<CellNovelOneLineTextBinding, NovelOneLineTextHolder>(aa) {

    override fun onBindViewHolder(item: NovelOneLineTextHolder) {
        super.onBindViewHolder(item)
        binding.holder = item
    }
}

class NovelChapterHolder(val title: String) : SlinkyItem() {

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return title == (other as? NovelChapterHolder)?.title
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return title == (other as? NovelChapterHolder)?.title
    }
}


@ItemHolder(NovelChapterHolder::class)
class NovelChapterViewHolder(aa: CellNovelChapterBinding) :
    SlinkyViewHolder<CellNovelChapterBinding, NovelChapterHolder>(aa) {

    override fun onBindViewHolder(item: NovelChapterHolder) {
        super.onBindViewHolder(item)
        binding.holder = item
    }
}

class NovelImageHolder(
    val type: Int,
    val id: Long,
    val indexInIllust: Int,
    val webNovel: WebNovel
) : SlinkyItem() {

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return type == (other as? NovelImageHolder)?.type &&
                id == (other as? NovelImageHolder)?.id
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return type == (other as? NovelImageHolder)?.type &&
                id == (other as? NovelImageHolder)?.id &&
                indexInIllust == (other as? NovelImageHolder)?.indexInIllust &&
                webNovel == (other as? NovelImageHolder)?.webNovel
    }

    object Type {
        const val UploadedImage = 1
        const val PixivImage = 2
    }
}

@ItemHolder(NovelImageHolder::class)
class NovelImageViewHolder(aa: CellNovelImageBinding) :
    SlinkyViewHolder<CellNovelImageBinding, NovelImageHolder>(aa) {

    override fun onBindViewHolder(item: NovelImageHolder) {
        super.onBindViewHolder(item)
        if (item.type == NovelImageHolder.Type.UploadedImage) {
            val urls = item.webNovel.images?.get(item.id.toString())?.urls
            val url = urls?.get(NovelImages.Size.Size1200x1200)
            Glide.with(binding.novelImage).load(url?.toGlideUrl()).into(binding.novelImage)
        } else if (item.type == NovelImageHolder.Type.PixivImage) {
            val urls = if (item.indexInIllust == 0) {
                item.webNovel.illusts?.get(item.id.toString())?.illust?.images?.medium
            } else {
                item.webNovel.illusts?.get("${item.id}-${item.indexInIllust}")?.illust?.images?.medium
            }
            binding.novelImage.setOnClick {
            }
            Glide.with(binding.novelImage).load(urls?.toGlideUrl()).into(binding.novelImage)
        }
    }
}