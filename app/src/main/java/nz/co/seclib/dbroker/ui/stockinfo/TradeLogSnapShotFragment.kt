package nz.co.seclib.dbroker.ui.stockinfo

import android.opengl.Visibility
import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_trade_log_activity.*
import kotlinx.android.synthetic.main.fragment_trade_log_snapshot.*

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
 * Use the [TradeLogActivityFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TradeLogSnapShotFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nzxTradeLogViewModel = NZXTradeLogViewModelFactory(
            MyApplication.instance
        ).create(NZXTradeLogViewModel::class.java)

        nzxTradeLogViewModel.stockInfo.observe(viewLifecycleOwner, Observer {
            tvSnapShotCurrentPrice.text = it.snapShotCurrentPrice
            tvSnapShotChange.text = it.snapShotChangePercent
            tvSnapShotInstrumentName.text = it.snapShotInstrumentName
            tvSnapShotIssuedBy.text = it.snapShotIssuedBy
            tvSnapShotISIN.text = it.snapShotISIN
            //this item seems useless.
            //tvSnapShotType.text = it.snapShotType
            //tvTradeLogSnapShot.text = Html.fromHtml(it.snapShot,Html.FROM_HTML_MODE_COMPACT)
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
        return inflater.inflate(R.layout.fragment_trade_log_snapshot, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TradeLogActivityFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() = TradeLogSnapShotFragment()
    }
}
