package ceui.lisa.slinky.ui

import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.navigation.fragment.navArgs
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.PixivListRepository
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.listViewModel
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.databinding.FragmentArticlePreviewBinding
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.dipToPx
import ceui.lisa.slinky.glide.GlideApp
import ceui.lisa.slinky.models.Article
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.utils.DrawableUtils
import ceui.lisa.slinky.utils.openWebPageInApp
import ceui.lisa.slinky.utils.openWebPageWithSystemBrowser
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

object ArticleType {
    const val MANGA = "manga"
    const val ALL = "all"
}

class ArticlesFragment : SlinkyListFragment(),
    ArticleAction {

    private val binding by viewBinding(FragmentSlinkyListBinding::bind)
    private val safeArgs: ArticlesFragmentArgs by navArgs()
    private val viewModel by listViewModel({ safeArgs.type }) { type ->
        PixivListRepository(
            loader = { Client.appApi.articles(type) },
            dataMapper = { ArticleHolder(it) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        actionbarContent.title.value = getString(R.string.pixivision)
        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, viewModel)
    }

    override fun openArticle(article: Article) {
        article.article_url?.let {
            openWebPageInApp(it)
        }
    }
}

class ArticleHolder(val article: Article) : SlinkyItem() {

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return article.id == (other as? ArticleHolder)?.article?.id
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return article == (other as? ArticleHolder)?.article
    }
}

@ItemHolder(ArticleHolder::class)
class ArticleViewHolder(aa: FragmentArticlePreviewBinding) :
    SlinkyViewHolder<FragmentArticlePreviewBinding, ArticleHolder>(aa) {

    override fun onBindViewHolder(item: ArticleHolder) {
        super.onBindViewHolder(item)
        val padding = context.dipToPx(18F)
        binding.root.updatePadding(left = padding, right = padding)
        binding.title.text = item.article.title
        binding.clickFrame.setOnClick {
            it.findActionReceiverOrNull<ArticleAction>()?.openArticle(item.article)
        }
        GlideApp.with(context)
            .load(item.article.thumbnail)
            .into(binding.imageView)
        binding.strokeView.let {
            it.background = DrawableUtils.stroke(
                it.context.getColorCompat(R.color.colorWhite20),
                1.pxValue.toFloat(),
                10.pxValue.toFloat()
            )
        }
    }
}

fun View.updateMargins(rect: Rect) {
    val params = layoutParams
    if (params is ViewGroup.MarginLayoutParams) { // avoid crash
        params.setMargins(rect.left, rect.top, rect.right, rect.bottom)
        layoutParams = params
    }
}

internal val Int.pxValue: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

internal val screenWidth: Int
    get() = Resources.getSystem().displayMetrics.widthPixels

internal val screenHeight: Int
    get() = Resources.getSystem().displayMetrics.heightPixels