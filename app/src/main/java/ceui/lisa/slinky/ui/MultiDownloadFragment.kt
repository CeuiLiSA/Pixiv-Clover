package ceui.lisa.slinky.ui

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import ceui.lisa.slinky.R
import ceui.lisa.slinky.core.CustomRepository
import ceui.lisa.slinky.core.DownloadTaskManager
import ceui.lisa.slinky.core.SLAdapter
import ceui.lisa.slinky.core.SlinkyListFragment
import ceui.lisa.slinky.core.listViewModel
import ceui.lisa.slinky.databinding.FragmentSlinkyListBinding
import ceui.lisa.slinky.ui.task.DownloadTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay

class MultiDownloadRepo : CustomRepository<MultiDownloadFragment>() {

    override suspend fun suspendRefresh(
        fragment: MultiDownloadFragment
    ) {
    }
}

class MultiDownloadFragment : SlinkyListFragment(R.layout.fragment_slinky_list) {

    private val binding by viewBinding(FragmentSlinkyListBinding::bind)
    private val viewModel by listViewModel { MultiDownloadRepo() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        URL_LIST.forEach {
            DownloadTaskManager.addTask(DownloadTask(it))
        }
        val recyclerView = binding.listView
        val adapter = SLAdapter(viewLifecycleOwner)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        DownloadTaskManager.runningTask.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items.map {
                TaskHolder(it as DownloadTask)
            }.asReversed())
        }
    }

    override fun onResume() {
        super.onResume()
        launchSuspend {
            delay(3000L)
            DownloadTaskManager.start()
        }
    }

    private val URL_LIST = listOf(
        "https://i.pximg.net/img-original/img/2022/10/10/00/00/25/101808113_p0.png",
        "https://i.pximg.net/img-original/img/2022/10/10/00/00/04/101807954_p0.png",
        "https://i.pximg.net/img-original/img/2022/10/10/00/00/02/101807941_p0.jpg",
        "https://i.pximg.net/img-original/img/2022/10/09/22/36/22/101805095_p0.png",
//        "https://i.pximg.net/img-original/img/2022/10/09/18/00/13/101797077_p0.jpg",
//        "https://i.pximg.net/img-original/img/2022/10/09/07/39/36/101782533_p0.jpg",
//        "https://i.pximg.net/img-original/img/2022/10/09/00/27/17/101780868_p0.jpg",
//        "https://i.pximg.net/img-original/img/2022/10/09/00/04/36/101780148_p0.jpg",
//        "https://i.pximg.net/img-original/img/2022/10/09/00/00/17/101779905_p0.png",
//        "https://i.pximg.net/img-original/img/2022/10/09/00/00/10/101779848_p0.png",
//        "https://i.pximg.net/img-original/img/2022/10/09/00/00/01/101779788_p0.jpg",
//        "https://i.pximg.net/img-original/img/2022/10/08/22/12/31/101776764_p0.png",
//        "https://i.pximg.net/img-original/img/2022/10/08/18/54/55/101771527_p0.jpg",
//        "https://i.pximg.net/img-original/img/2022/10/08/14/00/01/101766241_p0.jpg",
//        "https://i.pximg.net/img-original/img/2022/10/08/09/03/42/101761914_p0.jpg",
//        "https://i.pximg.net/img-original/img/2022/10/08/00/04/46/101755269_p0.jpg",
//        "https://i.pximg.net/img-original/img/2022/10/08/00/00/22/101755020_p0.png",
//        "https://i.pximg.net/img-original/img/2022/10/08/00/00/20/101755007_p0.jpg",
//        "https://i.pximg.net/img-original/img/2022/10/08/00/00/18/101754993_p0.png",
//        "https://i.pximg.net/img-original/img/2022/10/08/00/00/07/101754913_p0.jpg",
//        "https://i.pximg.net/img-original/img/2022/10/08/00/00/05/101754900_p0.jpg",
//        "https://i.pximg.net/img-original/img/2022/10/07/23/05/11/101753373_p0.jpg",
//        "https://i.pximg.net/img-original/img/2022/10/07/07/33/33/101738780_p0.jpg",
//        "https://i.pximg.net/img-original/img/2022/10/07/00/00/13/101733334_p0.png",
//        "https://i.pximg.net/img-original/img/2022/10/07/00/00/11/101733324_p0.png",
//        "https://i.pximg.net/img-original/img/2022/10/07/10/03/57/101733281_p0.jpg",
//        "https://i.pximg.net/img-original/img/2022/10/06/21/33/57/101729493_p0.png",
//        "https://i.pximg.net/img-original/img/2022/10/06/00/11/47/101712796_p0.png",
//        "https://i.pximg.net/img-original/img/2022/10/06/00/07/56/101712646_p0.png",
//        "https://i.pximg.net/img-original/img/2022/10/06/00/00/08/101712292_p0.png",
//        "https://i.pximg.net/img-original/img/2022/10/06/00/00/01/101712235_p0.png",
//        "https://i.pximg.net/img-original/img/2022/10/05/12/11/41/101699417_p0.jpg",
//        "https://i.pximg.net/img-original/img/2022/10/05/00/00/22/101691203_p0.png",
//        "https://i.pximg.net/img-original/img/2022/10/05/00/00/19/101691186_p0.png",
//        "https://i.pximg.net/img-original/img/2022/10/05/00/00/16/101691159_p0.png",
//        "https://i.pximg.net/img-original/img/2022/10/04/19/25/45/101684361_p0.jpg",
//        "https://i.pximg.net/img-original/img/2022/10/04/00/00/17/101670324_p0.png",
//        "https://i.pximg.net/img-original/img/2022/10/04/00/00/11/101670290_p0.jpg",
//        "https://i.pximg.net/img-original/img/2022/10/03/23/51/23/101669998_p0.jpg",
//        "https://i.pximg.net/img-original/img/2022/10/03/19/14/52/101663074_p0.jpg",
//        "https://i.pximg.net/img-original/img/2022/10/03/19/05/02/101662897_p0.png",
//        "https://i.pximg.net/img-original/img/2022/10/03/15/06/51/101659235_p0.jpg",
//        "https://i.pximg.net/img-original/img/2022/10/03/00/00/02/101648234_p0.png",
//        "https://i.pximg.net/img-original/img/2022/10/02/18/00/17/101637756_p0.jpg",
//        "https://i.pximg.net/img-original/img/2022/10/02/11/15/52/101630222_p0.jpg",
//        "https://i.pximg.net/img-original/img/2022/10/02/00/00/29/101621170_p0.jpg",
//        "https://i.pximg.net/img-original/img/2022/10/02/00/00/26/101621153_p0.png",
//        "https://i.pximg.net/img-original/img/2022/10/02/00/00/25/101621147_p0.png",
//        "https://i.pximg.net/img-original/img/2022/10/02/00/00/19/101621114_p0.jpg",
//        "https://i.pximg.net/img-original/img/2022/10/02/00/00/13/101621088_p0.png",
//        "https://i.pximg.net/img-original/img/2022/10/01/23/14/58/101619756_p0.jpg",
//        "https://i.pximg.net/img-original/img/2022/10/01/21/40/36/101616937_p0.jpg",
//        "https://i.pximg.net/img-original/img/2022/10/01/19/38/36/101613657_p0.jpg",
//        "https://i.pximg.net/img-original/img/2022/10/01/19/07/05/101612933_p0.jpg",
//        "https://i.pximg.net/img-original/img/2022/10/01/13/34/50/101606676_p0.jpg",
//        "https://i.pximg.net/img-original/img/2022/10/01/00/01/15/101595772_p0.jpg",
//        "https://i.pximg.net/img-original/img/2022/10/01/00/00/05/101595525_p0.png",
//        "https://i.pximg.net/img-original/img/2022/09/30/21/35/26/101591280_p0.jpg",
//        "https://i.pximg.net/img-original/img/2022/09/30/19/21/32/101587931_p0.jpg",
//        "https://i.pximg.net/img-original/img/2022/09/30/16/27/30/101584749_p0.jpg"
    )
}