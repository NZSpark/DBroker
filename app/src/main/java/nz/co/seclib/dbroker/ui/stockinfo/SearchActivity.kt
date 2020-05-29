package nz.co.seclib.dbroker.ui.stockinfo

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.wordplat.ikvstockchart.compat.ViewUtils
import kotlinx.android.synthetic.main.activity_stock_charts_old.btShowStockInfo
import kotlinx.android.synthetic.main.activity_stock_charts_old.spStockCodeList
import kotlinx.android.synthetic.main.activity_stock_search.*
import nz.co.seclib.dbroker.R
import nz.co.seclib.dbroker.ui.sysinfo.SystemConfigActivity
import nz.co.seclib.dbroker.utils.MyApplication

class SearchActivity: AppCompatActivity() {
   override fun onCreate(savedInstanceState: Bundle?) {
       super.onCreate(savedInstanceState)
       setContentView(R.layout.activity_stock_search)


       val spAdepter = ArrayAdapter(
           this,
           R.layout.support_simple_spinner_dropdown_item,
           MyApplication.stockMarketInfo.stockCodeList
       )
       spStockCodeList.adapter = spAdepter
       spStockCodeList.setSelection(spAdepter.getPosition("KMD"))

       btShowStockInfo.setOnClickListener {
           var intent: Intent? = null

           if (rbSearchDirctBroking.isChecked) {
               intent = Intent(this, StockInfoActivity::class.java).apply {
                   putExtra("STOCKCODE", spStockCodeList.selectedItem.toString())
               }
           }
           if (rbSearchNZX.isChecked) {
               intent = Intent(this, NZXStockChartActivity::class.java).apply {
                   putExtra("STOCKCODE", spStockCodeList.selectedItem.toString())
               }
           }

           startActivity(intent)
       }

       addStockList()

    }

    fun addStockList(){
        var iPos = 0
        while (iPos < MyApplication.stockMarketInfo.stockCodeList.size){
            val tRow = TableRow(this)


            for (i in 0..5) {
                val tvStock = TextView(tRow.context)
                tvStock.text = MyApplication.stockMarketInfo.stockCodeList[iPos]
                tvStock.width = ViewUtils.dpTopx(tvStock.context,60f)
                tvStock.gravity = Gravity.RIGHT
                tvStock.setOnClickListener {
                    var intent: Intent? = null

                    if (rbSearchDirctBroking.isChecked) {
                        intent = Intent(this, StockInfoActivity::class.java).apply {
                            putExtra("STOCKCODE", tvStock.text.toString())
                        }
                    }
                    if (rbSearchNZX.isChecked) {
                        intent = Intent(this, NZXStockChartActivity::class.java).apply {
                            putExtra("STOCKCODE", tvStock.text.toString())
                        }
                    }

                    startActivity(intent)
                }
                tRow.addView(tvStock)
                iPos++
                if(iPos == MyApplication.stockMarketInfo.stockCodeList.size) break
            }
            tlSearchStockList.addView(tRow)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_list, menu)
        return true
    }


    override fun onOptionsItemSelected( item: MenuItem) :Boolean{
        when (item.itemId){
            R.id.menu_selected_stocks -> {
                val intent = Intent(this, DBSelectedStocksActivity::class.java)
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
                val intent = Intent(this, DBTradeLogActivity::class.java)
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
