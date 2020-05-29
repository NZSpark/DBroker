package nz.co.seclib.dbroker.ui.stockinfo

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Animatable
import android.os.AsyncTask
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wordplat.ikvstockchart.InteractiveKLineLayout
import com.wordplat.ikvstockchart.KLineHandler
import com.wordplat.ikvstockchart.compat.PerformenceAnalyser
import com.wordplat.ikvstockchart.entry.Entry
import com.wordplat.ikvstockchart.entry.EntrySet
import com.wordplat.ikvstockchart.render.KLineRender
import kotlinx.android.synthetic.main.activity_stock_chart_nzx.*
import nz.co.seclib.dbroker.R
import nz.co.seclib.dbroker.data.webdata.NZXWeb
import nz.co.seclib.dbroker.viewmodel.NZXStockInfoViewModel
import nz.co.seclib.dbroker.viewmodel.NZXStockInfoViewModelFactory
//import org.xutils.view.annotation.ContentView
//import org.xutils.view.annotation.ViewInject
import java.util.*

/**
 *
 * Enable_Left_And_Right_Refresh_Activity
 *
 * Date: 2017/3/10
 *
 * @author afon
 */

class NZXStockChartActivity : AppCompatActivity() {
    
    var kLineLayout: InteractiveKLineLayout? = null

//    @ViewInject(R.id.MA_Text)
//    private val MA_Text: TextView? = null
//
//    @ViewInject(R.id.StockIndex_Text)
//    private val StockIndex_Text: TextView? = null
//
//    @ViewInject(R.id.Volume_Text)
//    private val Volume_Text: TextView? = null
//
//    @ViewInject(R.id.Left_Loading_Image)
//    private val Left_Loading_Image: ImageView? = null
//
//    @ViewInject(R.id.Right_Loading_Image)
//    private val Right_Loading_Image: ImageView? = null
    private var entrySet: EntrySet? = null
    private var loadStartPos = 5500
    private var loadEndPos = 6000

    //    private int loadStartPos = 500;
    //    private int loadEndPos = 600;
    private val loadCount = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock_chart_nzx)
        kLineLayout = findViewById<InteractiveKLineLayout>(R.id.klTrade)
        
        //pass stockCode and show it on title bar.
        val stockCode = this.intent.getStringExtra("STOCKCODE")
        supportActionBar!!.setTitle("Candle Chart : $stockCode")
        initUI()
        loadKLineData(stockCode)

        val nzxStockViewModel = NZXStockInfoViewModelFactory(
            this.application
        ).create(NZXStockInfoViewModel::class.java)
        nzxStockViewModel.initWithStockCode(stockCode) //initial timer.

        ivChartAdd.setOnClickListener{
            nzxStockViewModel.insertUserStock(stockCode)
            Toast.makeText(this,stockCode+ " is added into the selected list!", Toast.LENGTH_LONG).show()
        }

        ivChartRemove.setOnClickListener(){
            nzxStockViewModel.deleteUserStock(stockCode)
            Toast.makeText(this,stockCode+ " is removed from the selected list!", Toast.LENGTH_LONG).show()
        }

        ivChartTradeLog.setOnClickListener(){
            val intent = Intent(this, NZXTradeLogActivity::class.java).apply {
                putExtra("STOCKCODE", stockCode)
            }
            startActivity(intent)
        }
    }

    private fun initUI() {
        kLineLayout!!.setKLineHandler(object : KLineHandler {
            override fun onHighlight(
                entry: Entry,
                entryIndex: Int,
                x: Float,
                y: Float
            ) {
                val sizeColor = kLineLayout!!.kLineView.render.sizeColor
                val maString = String.format(
                    resources.getString(R.string.ma_highlight),
                    entry.ma5,
                    entry.ma10,
                    entry.ma20
                )
                MA_Text!!.text = getSpannableString(
                    maString,
                    sizeColor.ma5Color,
                    sizeColor.ma10Color,
                    sizeColor.ma20Color
                )
                val volumeString = String.format(
                    resources.getString(R.string.volume_highlight),
                    entry.volume,
                    entry.volumeMa5,
                    entry.volumeMa10
                )
                Volume_Text!!.text = getSpannableString(
                    volumeString,
                    sizeColor.ma5Color,
                    sizeColor.ma10Color,
                    sizeColor.ma20Color
                )
                var spanString = SpannableString("")
                if (kLineLayout!!.isShownMACD) {
                    val str = String.format(
                        resources.getString(R.string.macd_highlight),
                        entry.diff,
                        entry.dea,
                        entry.macd
                    )
                    spanString = getSpannableString(
                        str,
                        sizeColor.diffLineColor,
                        sizeColor.deaLineColor,
                        sizeColor.macdHighlightTextColor
                    )
                } else if (kLineLayout!!.isShownKDJ) {
                    val str = String.format(
                        resources.getString(R.string.kdj_highlight),
                        entry.k,
                        entry.d,
                        entry.j
                    )
                    spanString = getSpannableString(
                        str,
                        sizeColor.kdjKLineColor,
                        sizeColor.kdjDLineColor,
                        sizeColor.kdjJLineColor
                    )
                } else if (kLineLayout!!.isShownRSI) {
                    val str = String.format(
                        resources.getString(R.string.rsi_highlight),
                        entry.rsi1,
                        entry.rsi2,
                        entry.rsi3
                    )
                    spanString = getSpannableString(
                        str,
                        sizeColor.rsi1LineColor,
                        sizeColor.rsi2LineColor,
                        sizeColor.rsi3LineColor
                    )
                } else if (kLineLayout!!.isShownBOLL) {
                    val str = String.format(
                        resources.getString(R.string.boll_highlight),
                        entry.mb,
                        entry.up,
                        entry.dn
                    )
                    spanString = getSpannableString(
                        str,
                        sizeColor.bollMidLineColor,
                        sizeColor.bollUpperLineColor,
                        sizeColor.bollLowerLineColor
                    )
                }
                StockIndex_Text!!.text = spanString
            }

            override fun onCancelHighlight() {
                val maString =
                    resources.getString(R.string.ma_normal)
                MA_Text!!.text = maString
                Volume_Text!!.text = ""
                var stockIndexString = ""
                if (kLineLayout!!.isShownMACD) {
                    stockIndexString =
                        resources.getString(R.string.macd_normal)
                } else if (kLineLayout!!.isShownKDJ) {
                    stockIndexString =
                        resources.getString(R.string.kdj_normal)
                } else if (kLineLayout!!.isShownRSI) {
                    stockIndexString =
                        resources.getString(R.string.rsi_normal)
                } else if (kLineLayout!!.isShownBOLL) {
                    stockIndexString =
                        resources.getString(R.string.boll_normal)
                }
                StockIndex_Text!!.text = stockIndexString
            }

            override fun onSingleTap(
                e: MotionEvent,
                x: Float,
                y: Float
            ) {
                val kLineRender = kLineLayout!!.kLineView.render as KLineRender
                if (kLineRender.kLineRect.contains(x, y)) {
                    kLineRender.zoomOut(x, y)
                    //Toast.makeText(this, "single tap [" + x + ", " + y + "]", Toast.LENGTH_SHORT).show();
                }
            }

            override fun onDoubleTap(
                e: MotionEvent,
                x: Float,
                y: Float
            ) {
                val kLineRender = kLineLayout!!.kLineView.render as KLineRender
                if (kLineRender.kLineRect.contains(x, y)) {
                    kLineRender.zoomIn(x, y)
                }
            }

            override fun onLeftRefresh() {
                Left_Loading_Image!!.visibility = View.VISIBLE
                (Left_Loading_Image.drawable as Animatable).start()
                // 模拟耗时
                kLineLayout!!.postDelayed(Runnable {
                    Left_Loading_Image.visibility = View.GONE
                    (Left_Loading_Image.drawable as Animatable).stop()
                    val entries =
                        insertEntries()
                    if (entries.size == 0) {
                        Toast.makeText(
                            this@NZXStockChartActivity,
                            "已经到达最左边了",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Runnable
                    }
                    kLineLayout!!.kLineView.render.entrySet.insertFirst(entries)
                    kLineLayout!!.kLineView.notifyDataSetChanged()
                    kLineLayout!!.kLineView.refreshComplete(entries.size > 0)
                }, 100)
            }

            override fun onRightRefresh() {
                Right_Loading_Image!!.visibility = View.VISIBLE
                (Right_Loading_Image.drawable as Animatable).start()
                // 模拟耗时
                kLineLayout!!.postDelayed(Runnable {
                    Right_Loading_Image.visibility = View.GONE
                    (Right_Loading_Image.drawable as Animatable).start()
                    val entries =
                        addEntries()
                    if (entries.size == 0) {
                        Toast.makeText(
                            this@NZXStockChartActivity,
                            "已经到达最右边了",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Runnable
                    }
                    kLineLayout!!.kLineView.render.entrySet.addEntries(entries)
                    kLineLayout!!.kLineView.notifyDataSetChanged()
                    kLineLayout!!.kLineView.refreshComplete(entries.size > 0)
                }, 100)
            }
        })
    }

    private fun getSpannableString(
        str: String,
        partColor0: Int,
        partColor1: Int,
        partColor2: Int
    ): SpannableString {
        //val splitString = str.split("[●]").toTypedArray()
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

    private fun loadKLineData(stockCode: String) {
        object : AsyncTask<Void?, Void?, Void?>() {
            override fun doInBackground(vararg params: Void?): Void? {
                PerformenceAnalyser.getInstance().addWatcher()

                /*
                String kLineData = "";
                try {
                    //InputStream in = getResources().getAssets().open("kline1.txt");
                    InputStream in = getResources().getAssets().open("KMDinterday.json");
                    int length = in.available();
                    byte[] buffer = new byte[length];
                    in.read(buffer);
                    kLineData = new String(buffer, "UTF-8");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                */PerformenceAnalyser.getInstance().addWatcher()
                val nzxWeb = NZXWeb()
                entrySet = if (stockCode == "") {
                    nzxWeb.copyInterDayInfoToChartEntrySet(
                        nzxWeb.convertJsonToInterdayInfoList(nzxWeb.getInterDayJson("KMD"))
                    )
                } else {
                    nzxWeb.copyInterDayInfoToChartEntrySet(
                        nzxWeb.convertJsonToInterdayInfoList(nzxWeb.getInterDayJson(stockCode))
                    )
                }
                //entrySet = StockDataTest.parseKLineData(kLineData);
                //entrySet = StockDataTest.parseKLineLongData(kLineData);
                PerformenceAnalyser.getInstance().addWatcher()
                if (entrySet!!.entryList.size > 0) entrySet!!.computeStockIndex()
                PerformenceAnalyser.getInstance().addWatcher()
                return null
            }

            override fun onPostExecute(aVoid: Void?) {
                kLineLayout!!.kLineView.setEntrySet(loadFirst())
                PerformenceAnalyser.getInstance().addWatcher()
                kLineLayout!!.kLineView.notifyDataSetChanged()
                PerformenceAnalyser.getInstance().addWatcher()
            }


        }.execute()
    }

    private fun loadFirst(): EntrySet {
        val set =
            EntrySet()

//        for (int i = loadStartPos ; i < loadEndPos ; i++) {
//            set.addEntry(entrySet.getEntryList().get(i));
//        }
        var iStart = entrySet!!.entryList.size - 500
        if (iStart < 0) iStart = 0
        for (i in iStart until entrySet!!.entryList.size) {
            set.addEntry(entrySet!!.entryList[i])
        }
        return set
    }

    private fun addEntries(): List<Entry> {
        val entries: MutableList<Entry> =
            ArrayList()
        var addCount = 0
        var i = loadEndPos
        while (i < loadEndPos + loadCount && i < entrySet!!.entryList.size) {
            addCount++
            entries.add(entrySet!!.entryList[i])
            i++
        }
        loadEndPos += addCount
        return entries
    }

    private fun insertEntries(): List<Entry> {
        val entries: MutableList<Entry> =
            ArrayList()
        var insertCount = 0
        var i = loadStartPos
        while (i > loadStartPos - loadCount && i > -1) {
            insertCount++
            if (entrySet!!.entryList.size > i) entries.add(
                entrySet!!.entryList[i]
            ) else break
            i--
        }
        loadStartPos -= insertCount
        return entries
    }

    companion object {
        private const val TAG = "Activity"
        fun createIntent(context: Context?): Intent {
            return Intent(context, NZXStockChartActivity::class.java)
        }
    }
}