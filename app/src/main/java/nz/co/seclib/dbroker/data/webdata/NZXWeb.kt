package nz.co.seclib.dbroker.data.webdata

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wordplat.ikvstockchart.entry.Entry
import com.wordplat.ikvstockchart.entry.EntrySet
import nz.co.seclib.dbroker.data.database.StockCurrentTradeInfo
import nz.co.seclib.dbroker.data.database.StockInfo
import nz.co.seclib.dbroker.data.database.StockMarketInfo
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.reflect.Type
import java.net.CookieManager
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

class NZXWeb {
    //handle cookies by default with lib com.squareup.okhttp3:okhttp-urlconnection:4.2.2.
    private val cookieJar = JavaNetCookieJar(CookieManager())
    private val okClient = OkHttpClient.Builder()
        .hostnameVerifier(HostnameVerifier { hostname, _ -> hostname == "www.nzx.com" })
        .cookieJar(cookieJar)
        .build()

    fun convertJsonToInterdayInfoList(inString:String) : List<NZXInterDayInfo> {
        if(inString.length < 100 ) //should be sizeOf(NZXInterDayInfo)
            return emptyList()
        val gson = Gson()
        val type: Type = object : TypeToken<List<NZXInterDayInfo>?>() {}.getType()
        return gson.fromJson(inString, type)
    }

    //candle entry, 6 elements.
    fun copyInterDayInfoToChartEntrySet(interDayInfoList:List<NZXInterDayInfo>) : EntrySet{
        val entrySet = EntrySet()
        for (interDayInfo in interDayInfoList) {
            entrySet.addEntry(
                Entry(
                    interDayInfo.open_price,
                    interDayInfo.high_price,
                    interDayInfo.low_price,
                    interDayInfo.close_price,
                    interDayInfo.volume,
                    interDayInfo.date
                )
            )
        }
        return entrySet
    }

    //get stock history data from NZX against stock code.
    fun getInterDayJson(stockCode:String) : String {
        var jsonString = ""
        var url = "https://www.nzx.com/statistics/"+stockCode+"/interday.json"
        var request = Request.Builder()
                .addHeader("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.5 Safari/605.1.15")
            .url(url)
            .build()

        okClient.newCall(request).execute().use {
            jsonString = it.body?.string().toString()
            it.body?.close()
        }
        return jsonString
    }

    //timeline entry, only 3 elements. don't split 5 mins.
    fun copyIntraDayInfoToEntrySet(intraDayInfoList:List<NZXIntraDayInfo>) : EntrySet{
        val entrySet = EntrySet()
        for (intraDayInfo in intraDayInfoList) {
            if( intraDayInfo.price < 0.001 ) continue
            entrySet.addEntry(
                Entry(
                    intraDayInfo.price,
                    intraDayInfo.volume,
                    intraDayInfo.time
                )
            )
        }
        return entrySet
    }


    //timeline entry, only 3 elements. split 5 mins to 1 min
    fun copyIntraDayInfoToChartEntrySet(intraDayInfoList:List<NZXIntraDayInfo>) : EntrySet{
        val entrySet = EntrySet()
        for (intraDayInfo in intraDayInfoList) {
            if( intraDayInfo.price < 0.001 ) continue
            //split 5 mins into 5 * 1 mins
            val newVolume = (intraDayInfo.volume/5)
            for(i in 0..4){

                entrySet.addEntry(
                    Entry(
                        intraDayInfo.price,
                        newVolume,
                        intraDayInfo.time
                    )
                )
            }
        }
        return entrySet
    }

    fun convertJsonToIntradayInfoList(inString:String) : List<NZXIntraDayInfo> {
        if(inString.length < 1) return emptyList()
        val gson = Gson()
        val type: Type = object : TypeToken<List<NZXIntraDayInfo>?>() {}.getType()
        return gson.fromJson(inString, type)
    }

    //get stock detailed day data from NZX against stock code.
    fun getIntraDayJson(stockCode:String) : String {
        var jsonString = ""
        var url = "https://www.nzx.com/statistics/"+stockCode+"/intraday.json?market_id=NZSX"
        var request = Request.Builder()
                .addHeader("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.5 Safari/605.1.15")
            .url(url)
            .build()

        okClient.newCall(request).execute().use {
            jsonString = it.body?.string().toString()
            it.body?.close()
        }
        return jsonString
    }

    fun getCompanyAnalysisByStockCode(stockCode: String):String{
        val url = "https://www.nzx.com/companies/"+stockCode+"/analysis"
        return extractAnalysisFromWebPage(getWebPageByUrl(url))
    }

    fun extractAnalysisFromWebPage(webPage:String):String{
        var analysis: String

        val startString = "<div class=\"small-12 medium-9 columns content\">"
        val endString = "<section>"

        if(webPage.length < startString.length) return ""
        var iStartPos = webPage.indexOf(startString)
        if(iStartPos < 0 ) return ""
        iStartPos += startString.length
        val iEndPos = webPage.indexOf(endString,iStartPos)
        if(iEndPos < 0 ) return ""

        analysis = webPage.substring(iStartPos,iEndPos).replace("\r\n","<br>")

        return analysis.replace("\n","<br>")
    }

    //get web pagefrom NZX.
    fun getWebPageByUrl(url:String) : String {
        var webPage = ""
        var request = Request.Builder()
                .addHeader("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.5 Safari/605.1.15")
            .url(url)
            .build()

        okClient.newCall(request).execute().use {
            webPage = it.body?.string().toString()
            it.body?.close()
        }
        return webPage
    }


    fun getStockInfo():  List<StockInfo>  {
        var stockInfoList = listOf<StockInfo>()
        val url = "https://www.nzx.com/markets/NZSX"

        val stockInfoPage = getWebPageByUrl(url)
        stockInfoList = StockMarketInfo.getStockInfoFromNZXWebPage(stockInfoPage)
        return stockInfoList
    }

    companion object {
        private val instance = NZXWeb()
        @JvmStatic
        fun newInstance(): NZXWeb {
            return instance
        }
    }
}