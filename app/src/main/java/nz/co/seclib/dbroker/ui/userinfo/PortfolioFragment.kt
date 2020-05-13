package nz.co.seclib.dbroker.ui.userinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_user_portfolio.*
import nz.co.seclib.dbroker.R
import nz.co.seclib.dbroker.ui.stockinfo.StockInfoViewModel
import nz.co.seclib.dbroker.ui.stockinfo.StockInfoViewModelFactory

class PortfolioFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rvPortfolio = view.findViewById<RecyclerView>(R.id.rvPortfolio)
        rvPortfolio.apply {
            //layoutManager must be set! otherwise adapter doesn't work.
            layoutManager = LinearLayoutManager(rvPortfolio.context)
            adapter = PortfolioAdapter(rvPortfolio.context)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_user_portfolio, container, false)

    companion object {

        @JvmStatic
        fun newInstance(): PortfolioFragment {
            return PortfolioFragment()
        }
    }
}