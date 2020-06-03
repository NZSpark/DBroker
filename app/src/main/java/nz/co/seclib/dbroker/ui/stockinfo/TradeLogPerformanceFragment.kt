package nz.co.seclib.dbroker.ui.stockinfo

import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_trade_log_performance.*

import nz.co.seclib.dbroker.R
import nz.co.seclib.dbroker.utils.MyApplication
import nz.co.seclib.dbroker.viewmodel.NZXTradeLogViewModel
import nz.co.seclib.dbroker.viewmodel.NZXTradeLogViewModelFactory

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TradeLogPerformanceFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TradeLogPerformanceFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val nzxTradeLogViewModel = NZXTradeLogViewModelFactory(
            MyApplication.instance
        ).create(NZXTradeLogViewModel::class.java)

        nzxTradeLogViewModel.stockInfo.observe(viewLifecycleOwner, Observer {
            tvPerformanceOpen.text = it.performanceOpen
            tvPerformanceHigh.text = it.performanceHigh
            tvPerformanceLow.text = it.performanceLow
            tvPerformanceHighBid.text = it.performanceHighBid
            tvPerformanceLowOffer.text = it.performanceLowOffer

//            tvTradeLogPerformance.text = Html.fromHtml(it.performance, Html.FROM_HTML_MODE_COMPACT)
        })
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trade_log_performance, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TradeLogPerformanceFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance()= TradeLogPerformanceFragment()
    }
}
