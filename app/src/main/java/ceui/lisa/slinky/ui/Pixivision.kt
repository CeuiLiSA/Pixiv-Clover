package ceui.lisa.slinky.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.LoadState
import ceui.lisa.slinky.core.setUpLoadingState
import ceui.lisa.slinky.databinding.FragmentArticlePreviewBinding
import ceui.lisa.slinky.databinding.ItemPixivisionViewPagerBinding
import ceui.lisa.slinky.dipToPx
import ceui.lisa.slinky.dipToPxF
import ceui.lisa.slinky.glide.GlideApp
import ceui.lisa.slinky.models.Article
import ceui.lisa.slinky.models.ArticlesResponse
import ceui.lisa.slinky.network.ObjectPool
import ceui.lisa.slinky.styles.ScaleInTransformer
import ceui.lisa.slinky.utils.DrawableUtils
import ceui.lisa.slinky.utils.openWebPageInApp
import ceui.lisa.slinky.utils.toGlideUrl

class PixivisionHolder(val parentFragment: NavFragment, val valueContent: ValueContent<ArticlesResponse>) :
    SlinkyItem() {

    val loadState: LiveData<LoadState> = valueContent.loadState
    val items: LiveData<List<Article>> = valueContent.result.map { it.spotlight_articles }

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return other is PixivisionHolder
    }
}


@ItemHolder(PixivisionHolder::class)
class PixivisionViewHolder(aa: ItemPixivisionViewPagerBinding) :
    SlinkyViewHolder<ItemPixivisionViewPagerBinding, PixivisionHolder>(aa) {

    override fun onBindViewHolder(item: PixivisionHolder) {
        super.onBindViewHolder(item)
        binding.seeMore.setOnClick {
            it.findFragment<NavFragment>().onClickSeeMoreArticle()
        }
        binding.viewPager.offscreenPageLimit = 3
        if (binding.viewPager.adapter == null) {
            binding.includeItemLoading.setUpLoadingState(item.loadState, lifecycleOwner) {
                item.valueContent.refresh()
            }
            val composite = CompositePageTransformer()
            composite.addTransformer(MarginPageTransformer(context.dipToPx(12F)))
            composite.addTransformer(ScaleInTransformer(0.85F))
            binding.viewPager.setPageTransformer(composite)
            item.items.observe(lifecycleOwner) { articles ->
                binding.viewPager.adapter = object : FragmentStateAdapter(item.parentFragment) {
                    override fun getItemCount(): Int {
                        return Int.MAX_VALUE
                    }

                    override fun createFragment(position: Int): Fragment {
                        val actualIndex = position % articles.size
                        return ArticlePreviewFragment().apply {
                            arguments =
                                ArticlePreviewFragmentArgs(articles[actualIndex].id).toBundle()
                        }
                    }
                }
                binding.viewPager.setCurrentItem(articles.size * 10000, false)
            }
        }
    }
}

fun NavFragment.onClickSeeMoreArticle() {
    pushFragment(R.id.articlesFragment, ArticlesFragmentArgs(ArticleType.ALL).toBundle())
}

class ArticlePreviewFragment : NavFragment(R.layout.fragment_article_preview) {

    private val safeArgs: ArticlePreviewFragmentArgs by navArgs()
    private val binding by viewBinding(FragmentArticlePreviewBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()
        ObjectPool.get<Article>(safeArgs.articleId).observe(viewLifecycleOwner) { article ->
            GlideApp.with(context)
                .load(article.thumbnail?.toGlideUrl())
                .into(binding.imageView)

            article.article_url?.let { url ->
                binding.clickFrame.setOnClick {
                    openWebPageInApp(url)
                }
            }

            binding.title.text = article.title
        }

        binding.strokeView.let {
            it.background = DrawableUtils.stroke(
                it.context.getColorCompat(R.color.colorWhite20),
                dipToPxF(1f),
                dipToPxF(10f)
            )
        }
    }
}

fun Context.getColorCompat(@ColorRes colorRes: Int) =
    ResourcesCompat.getColor(resources, colorRes, theme)
