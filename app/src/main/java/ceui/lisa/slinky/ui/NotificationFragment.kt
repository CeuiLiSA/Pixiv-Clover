package ceui.lisa.slinky.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.View
import ceui.lisa.annotations.ItemHolder
import ceui.lisa.slinky.OutWakeActivity
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.PixivListRepository
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.listViewModel
import ceui.lisa.slinky.core.setUpSlinkyList
import ceui.lisa.slinky.databinding.CellNotificationBinding
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.models.SystemNotification
import ceui.lisa.slinky.network.Client
import ceui.lisa.slinky.utils.DateParse
import ceui.lisa.slinky.utils.toGlideUrl
import com.bumptech.glide.Glide

class NotificationRepository : PixivListRepository<SystemNotification, NotificationFragment>(
    loader = { Client.appApi.getNotifications() },
    dataMapper = { systemNotification -> NotificationHolder(systemNotification) }
)

class NotificationFragment : SlinkyListFragment(), NotificationActionReceiver {

    private val viewModel by listViewModel { NotificationRepository() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSlinkyListBinding.bind(view)
        actionbarContent.title.value = getString(R.string.notifications)
        setUpSlinkyList(binding.listView, binding.refreshLayout, binding.itemLoading, viewModel)
    }

    override fun onClickNotification(sender: View, notification: SystemNotification) {
        notification.target_url?.let {
            startActivity(Intent(requireContext(), OutWakeActivity::class.java).apply {
                setData(Uri.parse(it))
            })
        }
    }
}

class NotificationHolder(val systemNotification: SystemNotification) : SlinkyItem() {

    override fun areItemsTheSame(other: SlinkyItem): Boolean {
        return systemNotification.id == (other as? NotificationHolder)?.systemNotification?.id
    }

    override fun areContentsTheSame(other: SlinkyItem): Boolean {
        return systemNotification == (other as? NotificationHolder)?.systemNotification
    }
}


@ItemHolder(NotificationHolder::class)
class NotificationViewHolder(aa: CellNotificationBinding) :
    SlinkyViewHolder<CellNotificationBinding, NotificationHolder>(aa) {

    override fun onBindViewHolder(item: NotificationHolder) {
        super.onBindViewHolder(item)
        binding.content.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(item.systemNotification.content?.text, 0)
        } else {
            item.systemNotification.content?.text
        }
        Glide.with(context).load(item.systemNotification.content?.findIcon()?.toGlideUrl())
            .into(binding.icon)
        binding.root.setOnClick {
            it.findActionReceiverOrNull<NotificationActionReceiver>()?.onClickNotification(it, item.systemNotification)
        }
        binding.dateTime.text = DateParse.displayCreateDate(item.systemNotification.created_datetime)
    }
}

interface NotificationActionReceiver {
    fun onClickNotification(sender: View, notification: SystemNotification)
}