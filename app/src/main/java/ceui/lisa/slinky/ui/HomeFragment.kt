package ceui.lisa.slinky.ui

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import ceui.lisa.slinky.R
import ceui.lisa.slinky.databinding.FragmentTabWithBottomBarBinding
import ceui.lisa.slinky.requireLoggedInAccount
import ceui.lisa.slinky.styles.BottomItem
import ceui.lisa.slinky.styles.SlinkyBottomBar
import ceui.lisa.slinky.utils.visibleOrGone
import com.blankj.utilcode.util.ActivityUtils
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class HomeFragment : NavFragment(R.layout.fragment_tab_with_bottom_bar) {

    private val binding by viewBinding(FragmentTabWithBottomBarBinding::bind)
    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val index0 = 0
            if (binding.viewPager.currentItem != index0) {
                binding.viewPager.currentItem = index0
            } else {
                val frag = childFragmentManager.findFragmentByTag("f$index0")
                frag?.view?.findViewById<RecyclerView>(R.id.list_view)?.let { listView ->
                    if (listView.isTop()) {
                        finish()
                    } else {
                        listView.smoothScrollToTopIfNeeded()
                    }
                }
            }
        }
    }

    private fun finish() {
        ActivityUtils.startHomeActivity()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backPressedCallback.isEnabled = true
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            backPressedCallback
        )
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return 3
            }

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> {
//                        WebWaitingFragment()
//                        PlayListFragment()
//                        BackgroundFragment()
//                        RecmdIllustFragment()
//                        MultiDownloadFragment()
//                          CustomListFragment()
//                          SlinkyListFragment()
//                        MultiDownloadFragment()
//                        ButtonFragment()
                        RecmdIllustFragment()
//                        TestFragment()
                    }

                    1 -> {
                        CenterFragment()
                    }

                    2 -> {
                        PostFragment()
                    }

                    else -> {
                        Fragment()
                    }
                }
            }
        }
        binding.endButton.setOnClick {
            pushFragment(
                R.id.searchFragment,
                SearchFragmentArgs(ViewPagerContentType.TYPE_SEARCH_KEY_WORD).toBundle()
            )
        }
        binding.userSnapLayout.user = MutableLiveData(requireLoggedInAccount().user)
        binding.userSnapLayout.followLayout.visibleOrGone = false
        binding.userSnapLayout.userHead.setOnClickListener {
            pushFragment(R.id.currentUserFragment)
        }
        binding.bottomBar.setItems(
            viewLifecycleOwner, listOf(
                BottomItem(getString(R.string.recommend)),
                BottomItem(getString(R.string.discover)),
                BottomItem(getString(R.string.follow_post))
            )
        )
        binding.bottomBar.setUpWithViewPager(binding.viewPager)
    }
}

fun SlinkyBottomBar.setUpWithViewPager(viewPager2: ViewPager2) {
    val bottomBar = this
    viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            bottomBar.updateSelectedIndex(position)
        }

    })
    setOnTabSelectedListener(object : SlinkyBottomBar.OnTabSelected {
        override fun onSelected(index: Int) {
            viewPager2.currentItem = index
        }

        override fun onReSelected(index: Int) {
            val frag =
                findFragment<HomeFragment>().childFragmentManager.findFragmentByTag("f$index")
            (frag as? ReselectAction)?.onReselected(index)
        }
    })
}

class FragmentViewBindingDelegate<T : ViewBinding>(
    fragment: Fragment,
    val viewBindingFactory: (View) -> T
) : ReadOnlyProperty<Fragment, T> {
    private var binding: T? = null

    init {
        fragment.addOnViewDestroyListener {
            binding = null
        }
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        val thisView = thisRef.requireView()
        val binding = binding
        if (binding != null) {
            if (binding.root == thisView) {
                return binding
            } else {
            }
        }

        val lifecycle = thisRef.viewLifecycleOwner.lifecycle
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            throw IllegalStateException("Should not attempt to get bindings when Fragment views are destroyed.")
        }

        return viewBindingFactory(thisView).also {
            this.binding = it
            (this.binding as? ViewDataBinding)?.lifecycleOwner = thisRef.viewLifecycleOwner
        }
    }
}


fun <T : ViewBinding> Fragment.viewBinding(viewBindingFactory: (View) -> T) =
    FragmentViewBindingDelegate(this, viewBindingFactory)

fun Fragment.addOnViewDestroyListener(listener: () -> Unit) {
    // https://medium.com/@Zhuinden/an-update-to-the-fragmentviewbindingdelegate-the-bug-weve-inherited-from-autoclearedvalue-7fc0a89fcae1
    lifecycle.addObserver(object : DefaultLifecycleObserver {
        val viewLifecycleOwnerLiveDataObserver =
            Observer<LifecycleOwner?> {
                val viewLifecycleOwner = it ?: return@Observer

                viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                    override fun onDestroy(owner: LifecycleOwner) {
                        listener()
                    }
                })
            }

        override fun onCreate(owner: LifecycleOwner) {
            viewLifecycleOwnerLiveData.observeForever(viewLifecycleOwnerLiveDataObserver)
        }

        override fun onDestroy(owner: LifecycleOwner) {
            viewLifecycleOwnerLiveData.removeObserver(viewLifecycleOwnerLiveDataObserver)
        }
    })
}