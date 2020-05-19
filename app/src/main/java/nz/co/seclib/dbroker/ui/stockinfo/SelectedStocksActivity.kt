package nz.co.seclib.dbroker.ui.stockinfo

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.wordplat.easydivider.RecyclerViewCornerRadius
import com.wordplat.easydivider.RecyclerViewLinearDivider
import kotlinx.android.synthetic.main.activity_selected_stock_list.*
import nz.co.seclib.dbroker.R
import nz.co.seclib.dbroker.adapter.SelectedStockListAdapter
import nz.co.seclib.dbroker.ui.sysinfo.SystemConfigActivity
import nz.co.seclib.dbroker.ui.userinfo.UserInfoManagerActivity
import nz.co.seclib.dbroker.utils.AppUtils
import nz.co.seclib.dbroker.viewmodel.StockInfoViewModel
import nz.co.seclib.dbroker.viewmodel.StockInfoViewModelFactory

class SelectedStocksActivity : AppCompatActivity(){
    private lateinit var selectStockViewModel: StockInfoViewModel
    var bShowSelectedList = true


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selected_stock_list)

        //selectStockViewModel = DBrokerViewModelFactory(this.application).create(DBrokerViewModel::class.java)
        selectStockViewModel = StockInfoViewModelFactory(
            this.application
        ).create(StockInfoViewModel::class.java)
        selectStockViewModel.initWithStockCode("") //initial timer.

        val fab: FloatingActionButton = findViewById(R.id.fab)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "User related functions: ", Snackbar.LENGTH_LONG)
                .setAction("UserInfo") {
                    val intent = Intent(this, UserInfoManagerActivity::class.java)
                    startActivity(intent)
                }.show()
        }


        val rvStockList = findViewById<RecyclerView>(R.id.rvStockList)
        val adapter = SelectedStockListAdapter(this)

        rvStockList.adapter = adapter
        rvStockList.layoutManager = LinearLayoutManager(this)

        //RecyclerView Decoration---------------------->> begin
        val cornerRadius = RecyclerViewCornerRadius(rvStockList)
        cornerRadius.setCornerRadius(AppUtils.dpTopx(this, 10F))

        val linearDivider =
            RecyclerViewLinearDivider(this, LinearLayoutManager.VERTICAL)
        linearDivider.setDividerSize(1)
        linearDivider.setDividerColor(-0x777778)
        linearDivider.setDividerMargin(
            AppUtils.dpTopx(this, 10F),
            AppUtils.dpTopx(this, 10F)
        )
        linearDivider.setDividerBackgroundColor(-0x1)
        linearDivider.setShowHeaderDivider(false)
        linearDivider.setShowFooterDivider(true)

        // 圆角背景必须第一个添加
        rvStockList.addItemDecoration(cornerRadius)
        rvStockList.addItemDecoration(linearDivider)
        //RecyclerView Decoration --------------------<< end


        selectStockViewModel.getSelectedStockList()
//        selectStockViewModel.stockCurrentTradeInfo.observe(this, Observer {
//            it?.let{
//                adapter.setStocks(it)
//            }
//        })

        selectStockViewModel.stockCurrentTradeInfoList.observe(this, Observer {
            it?.let{
                adapter.setStocks(it)
            }
        })

        tlSelectedList.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> selectStockViewModel.getSelectedStockList()
                    1 -> selectStockViewModel.getScreenInfoListByType("VALUE")
                    2 -> selectStockViewModel.getScreenInfoListByType("PERCENTCHANGE")
                    3 -> selectStockViewModel.getScreenInfoListByType("MKTCAP")
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })




//        selectStockViewModel.stockCodeList?.observe(this, Observer {
//            it?.let{
//                adapter.setStocks(it)
//            }
//        })



        ivRefresh.setOnClickListener {
               supportActionBar!!.setTitle("Selected")
               selectStockViewModel.getSelectedStockList()
        }

        ivSearch.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        //selected list as default show.
        selectStockViewModel.getSelectedStockList()

    }

    override fun onResume() {
        super.onResume()
        //selected list as default show.
//        val adapter = rvStockList.adapter as SelectedStockListAdapter
//        adapter.setStocks(selectStockViewModel.stockCurrentTradeInfoList.value ?: emptyList())
        when (tlSelectedList.selectedTabPosition) {
            0 -> selectStockViewModel.getSelectedStockList()
            1 -> selectStockViewModel.getScreenInfoListByType("VALUE")
            2 -> selectStockViewModel.getScreenInfoListByType("PERCENTCHANGE")
            3 -> selectStockViewModel.getScreenInfoListByType("MKTCAP")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_list, menu)
        return true
    }


    override fun onOptionsItemSelected( item: MenuItem) :Boolean{
        when (item.itemId){
            R.id.menu_selected_stocks -> {
                val intent = Intent(this, SelectedStocksActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_stock_info -> {
                val intent = Intent(this, StockInfoActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_stock_search -> {
                val intent = Intent(this, SearchActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_stock_trade_info -> {
                val intent = Intent(this, TradeLogActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_system_parameters -> {
                val intent = Intent(this, SystemConfigActivity::class.java)
                startActivity(intent)
            }

            R.id.menu_night_mode ->{
                if (delegate.localNightMode == AppCompatDelegate.MODE_NIGHT_YES) {
                    delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_NO
                } else {
                    delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_YES
                }
            }

        }
        return true
    }

}