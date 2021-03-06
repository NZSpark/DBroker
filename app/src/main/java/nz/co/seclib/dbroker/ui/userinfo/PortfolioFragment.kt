package nz.co.seclib.dbroker.ui.userinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nz.co.seclib.dbroker.R
import nz.co.seclib.dbroker.adapter.PortfolioAdapter
import nz.co.seclib.dbroker.utils.MyApplication
import nz.co.seclib.dbroker.viewmodel.UserInfoViewModel
import nz.co.seclib.dbroker.viewmodel.UserInfoViewModelFactory

class PortfolioFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rvPortfolio = view.findViewById<RecyclerView>(R.id.rvPortfolio)
        val portfolioAdapter =
            PortfolioAdapter(rvPortfolio.context)
        rvPortfolio.apply {
            //layoutManager must be set! otherwise adapter doesn't work.
            layoutManager = LinearLayoutManager(rvPortfolio.context)
            adapter = portfolioAdapter
        }

        val userInfoViewModel = UserInfoViewModelFactory(
            MyApplication.instance
        ).create(UserInfoViewModel::class.java)
        userInfoViewModel.portfolioList.observe(viewLifecycleOwner, Observer { pl->
                portfolioAdapter.setPortfolio(pl)
        })
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