package nz.co.seclib.dbroker.ui.stockinfo

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.wordplat.easydivider.RecyclerViewCornerRadius
import com.wordplat.easydivider.RecyclerViewLinearDivider
import com.wordplat.ikvstockchart.KLineHandler
import com.wordplat.ikvstockchart.drawing.KLineVolumeDrawing
import com.wordplat.ikvstockchart.drawing.KLineVolumeHighlightDrawing
import com.wordplat.ikvstockchart.entry.Entry
import com.wordplat.ikvstockchart.entry.StockKLineVolumeIndex
import com.wordplat.ikvstockchart.render.TimeVolumeLineRender
import kotlinx.android.synthetic.main.activity_stock_tradelog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import nz.co.seclib.dbroker.R
import nz.co.seclib.dbroker.adapter.TradeLogListAdapter
import nz.co.seclib.dbroker.ui.sysinfo.SystemConfigActivity
import nz.co.seclib.dbroker.utils.AppUtils
import nz.co.seclib.dbroker.viewmodel.DBTradeLogViewModel
import nz.co.seclib.dbroker.viewmodel.DBTradeLogViewModelFactory

class DBTradeLogActivity : AppCompatActivity() , CoroutineScope by MainScope(){
    private lateinit var dbTradeLogViewModel: DBTradeLogViewModel
    var stockCode = ""

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock_tradelog)

        val adapter = TradeLogListAdapter(this)

        stockCode = intent.getStringExtra("STOCKCODE") ?:""

        rvTradeLog.adapter = adapter
        rvTradeLog.layoutManager = LinearLayoutManager(this)

        //RecyclerView Decoration---------------------->> begin
        val cornerRadius = RecyclerViewCornerRadius(rvTradeLog)
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
        linearDivider.setShowFooterDivider(false)

        // 圆角背景必须第一个添加
        rvTradeLog.addItemDecoration(cornerRadius)
        rvTradeLog.addItemDecoration(linearDivider)
        //RecyclerView Decoration --------------------<< end

        dbTradeLogViewModel = DBTradeLogViewModelFactory(
            this.application
        ).create(DBTradeLogViewModel::class.java)
//      tradeLogViewModel = ViewModelProviders.of(this, TradeLogViewModelFactory(this.application))
//            .get(TradeLogViewModel::class.java)


        dbTradeLogViewModel.tradeLogList.observe(this, Observer {
            adapter.setTradeLog(it)
        })

        dbTradeLogViewModel.entrySet.observe(this, Observer { entrySet ->
            //x-axis lable, it's fixed in TimeLineRender.
            if(entrySet.entryList.size > 5) {
                entrySet.entryList[0].xLabel = "10:00"
                entrySet.entryList[1].xLabel = "11:45"
                entrySet.entryList[2].xLabel = "13:30"
                entrySet.entryList[3].xLabel = "15:15"
                entrySet.entryList[4].xLabel = "17:00"
            }
            val render = TimeVolumeLineRender()
            klTradeLogTimeLine.setEntrySet(entrySet)
            klTradeLogTimeLine.render = render

            // 成交量
            val stockIndexViewHeight = klTradeLogTimeLine.context.getResources().getDimensionPixelOffset(com.wordplat.ikvstockchart.R.dimen.stock_index_view_height)
            val kLineVolumeIndex = StockKLineVolumeIndex(stockIndexViewHeight)
            kLineVolumeIndex.addDrawing(KLineVolumeDrawing())
            kLineVolumeIndex.addDrawing(KLineVolumeHighlightDrawing())
            render.addStockIndex(kLineVolumeIndex)

            klTradeLogTimeLine.notifyDataSetChanged()
        })

        klTradeLogTimeLine.setKLineHandler( object : KLineHandler {
            override fun onHighlight(
                entry: Entry,
                entryIndex: Int,
                x: Float,
                y: Float
            ) {
                //move recyclerview at the same time.
                val newPos = ( (entryIndex + 0F)/(klTradeLogTimeLine.render.entrySet.entryList.size + 0F) * (adapter.itemCount + 0F)).toInt()
                val layoutManager = rvTradeLog.layoutManager as LinearLayoutManager
                layoutManager.scrollToPosition(adapter.itemCount - newPos) //recycler is reversed order.

                tvTradeLogVolume.text = "Volume : " + entry.volume.toString() + " Price: " + entry.close.toString()
                if ( x  > klTradeLogTimeLine.render.viewRect.right / 2 )
                    tvTradeLogVolume.x =  x - tvTradeLogVolume.text.toString().length * tvTradeLogVolume.textSize / 2  //left side of highlight line
                else
                    tvTradeLogVolume.x = x //right side of highlight line
                tvTradeLogVolume.y = klTradeLogTimeLine.render.viewRect.top

//                val sizeColor: SizeColor = klTradeLogTimeLine.render.sizeColor
//
//                val volumeString = String.format(
//                    resources.getString(com.wordplat.ikvstockchart.R.string.volume_highlight),
//                    entry.volume,
//                    entry.volumeMa5,
//                    entry.volumeMa10
//                )
//                tvTradeLogVolume.setText(
//                    getSpannableString(
//                        volumeString,
//                        sizeColor.ma5Color,
//                        sizeColor.ma10Color,
//                        sizeColor.ma20Color
//                    )
//                )
            }

            override fun onCancelHighlight() {
                tvTradeLogVolume.setText("")
            }

            override fun onLeftRefresh() {
                return
            }

            override fun onDoubleTap(e: MotionEvent?, x: Float, y: Float) {
                return
            }

            override fun onSingleTap(e: MotionEvent?, x: Float, y: Float) {
                return
            }

            override fun onRightRefresh() {
                return
            }
        })

        dbTradeLogViewModel.companyAnalysis.observe(this, Observer {
            tvCompanyAnalysis.text = Html.fromHtml(it, Html.FROM_HTML_MODE_COMPACT)
        })
        //if only handle local data, then common init should be comment out.
        //stockInfoViewModel.initWithStockCode(stockCode)
        //initTradeLogActivity is included in initWithStockCode, only one of them should be invoked.
        dbTradeLogViewModel.initTradeLogActivity(stockCode)

    }

    private fun getSpannableString(
        str: String,
        partColor0: Int,
        partColor1: Int,
        partColor2: Int
    ): SpannableString? {
        val splitString = str.split("●").dropWhile { it == "" }
        val spanString = SpannableString(str)
        val pos0 = splitString[0].length
        val pos1 = pos0 + splitString[1].length + 1
        val end = str.length
        spanString.setSpan(
            ForegroundColorSpan(partColor0),
            pos0, pos1, SpannableString.SPAN_EXCLUSIVE_INCLUSIVE
        )
        if (splitString.size > 2) {
            val pos2 = pos1 + splitString[2].length + 1
            spanString.setSpan(
                ForegroundColorSpan(partColor1),
                pos1, pos2, SpannableString.SPAN_EXCLUSIVE_INCLUSIVE
            )
            spanString.setSpan(
                ForegroundColorSpan(partColor2),
                pos2, end, SpannableString.SPAN_EXCLUSIVE_INCLUSIVE
            )
        } else {
            spanString.setSpan(
                ForegroundColorSpan(partColor1),
                pos1, end, SpannableString.SPAN_EXCLUSIVE_INCLUSIVE
            )
        }
        return spanString
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
                val intent = Intent(this, StockInfoActivity::class.java).apply {
                    putExtra("STOCKCODE", stockCode)
                }
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
