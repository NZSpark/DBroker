package nz.co.seclib.dbroker.data.repository

import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.os.Build
import androidx.annotation.RequiresApi
import com.wordplat.ikvstockchart.entry.Entry
import com.wordplat.ikvstockchart.entry.EntrySet
import nz.co.seclib.dbroker.data.database.*
import nz.co.seclib.dbroker.data.webdata.NZXWeb
import nz.co.seclib.dbroker.data.webdata.NZXIntraDayInfo
import nz.co.seclib.dbroker.data.webdata.NZXStockInfo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class NZXRepository(private val dbDao: DBrokerDAO, private val nzxWeb: NZXWeb) {

    var stockInfoList = listOf<StockInfo>()



    fun getCurrentTradeInfoByStockCode(stockCode: String): StockCurrentTradeInfo?
    {
        val stockCurrentTradeInfo = StockCurrentTradeInfo()

        //should be done only once.
        //stockInfoList = getStockInfo()

//        stockInfoList.forEach {stockInfo ->
//            if(stockInfo.stockCode == stockCode) {
//                stockCurrentTradeInfo.copyFromStokInfo(stockInfo)
//                return@forEach
//            }
//        }

        stockCurrentTradeInfo.copyFromNZXStockInfo(getStockInfo(stockCode))
        return stockCurrentTradeInfo
    }

    suspend fun insertUserStock(newStockCode: UserStock){
        dbDao.insertUserStock(newStockCode)
    }
    suspend fun deleteUserStock(newStockCode: UserStock){
        dbDao.deleteUserStock(newStockCode)
    }

    fun selectStockCodeByUserID(userID: String): List<String>{
        return dbDao.selectStockCodeByUserID(userID)
    }

    fun getPropertyValuebyPropertyName(propertyName:String):String{
        val propertyList = dbDao.selectSystemConfigInfoByPropertyName(propertyName)
        if(propertyList.size == 0) return ""
        return propertyList[0].propertyValue.toString()
    }

    suspend fun saveProperty(propertyName: String,propertyValue:String){
        val propertyList = dbDao.selectSystemConfigInfoByPropertyName(propertyName)
        if(propertyList.size == 0) {
            val newSystemConfigInfo =
                SystemConfigInfo(
                    propertyName,
                    propertyValue
                )
            dbDao.insertSystemConfigInfo(newSystemConfigInfo)
        }else{
            val newSystemConfigInfo =
                SystemConfigInfo(
                    propertyName,
                    propertyValue
                )
            dbDao.updateSystemConfigInfo(newSystemConfigInfo)
        }
    }


    fun String.toDate(dateFormat: String = "yyyy-MM-dd HH:mm:ss", timeZone: TimeZone = TimeZone.getTimeZone("UTC")): Date {
        val parser = SimpleDateFormat(dateFormat, Locale.getDefault())
        parser.timeZone = timeZone
        return parser.parse(this)
    }

    fun Date.formatTo(dateFormat: String, timeZone: TimeZone = TimeZone.getDefault()): String {
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
        formatter.timeZone = timeZone
        return formatter.format(this)
    }


    fun timeFormat(inTime:String):String{
//        "2020-04-28T10:10:00.000+12:00"
//        "2020-04-28 10:10:00"
        return inTime.replace("T"," ").replace(":00.000+12:00","")
    }

    fun getCompanyAnalysisByStockCode(stockCode:String):String{
        return nzxWeb.getCompanyAnalysisByStockCode(stockCode)
    }

    //timeline entry, only 3 elements.
    fun copyIntraDayInfoToChartEntrySet(intraDayInfoList:List<NZXIntraDayInfo>) : EntrySet{
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


    fun getTodayIntraEntrySet(stockCode:String) : EntrySet{
        val entrySet = getIntraDayChartEntrySetByStockCode(stockCode)
        val newEntrySet = EntrySet()
        val today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        entrySet.entryList.forEach {
            val entryTime = timeFormat(it.xLabel)
            if(entryTime > today)
                newEntrySet.addEntry(
                    Entry(
                        it.close,
                        it.volume,
                        timeFormat(it.xLabel).replace(today,"").trim()
                    )
                )
        }
        return newEntrySet
    }

    //line data
    fun getIntraDayEntrySetByStockCode(stockCode:String):EntrySet{
        return nzxWeb.copyIntraDayInfoToEntrySet(
            nzxWeb.convertJsonToIntradayInfoList(
                nzxWeb.getIntraDayJson(stockCode)))
    }

    //line data
    fun getIntraDayChartEntrySetByStockCode(stockCode:String):EntrySet{
        return nzxWeb.copyIntraDayInfoToChartEntrySet(
            nzxWeb.convertJsonToIntradayInfoList(
                nzxWeb.getIntraDayJson(stockCode)))
    }

    //candle data
    fun getInterDayEntrySetByStockCode(stockCode:String):EntrySet{
        return nzxWeb.copyInterDayInfoToChartEntrySet(
            nzxWeb.convertJsonToInterdayInfoList(
                nzxWeb.getInterDayJson(stockCode)
            )
        )
    }

    fun getTradeLogEntrySetByStockCode(stockCode:String):EntrySet{
        return copyTradeLogListToEntrySet(
            getTodayTradeLog(stockCode)
        )
    }

    //Get today's trade logs
    @RequiresApi(Build.VERSION_CODES.O)
    fun getTodayTradeLog(stockCode: String): List<TradeLog>{
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formatted = current.format(formatter)
        val startTime = formatted + " "
        val endTime = formatted + "+"
        val tradeLogList = dbDao.selectTradeLogByTime(startTime,endTime,stockCode)
        tradeLogList.forEach(){
            it.id = 0
            it.tradeTime = it.tradeTime.substring(it.tradeTime.lastIndexOf(" ") + 1)
        }
        return tradeLogList
    }

    //Get trade logs from database against time span
    fun getTradeLogByTime(startTime:String,endTime:String,stockCode: String) : List<TradeLog>{
        return dbDao.selectTradeLogByTime(startTime,endTime,stockCode)
    }

    fun copyTradeLogListToEntrySet(tradeLogList: List<TradeLog>):EntrySet{
        val entrySet = EntrySet()
        tradeLogList.reversed().forEach{tradeLog ->
            entrySet.addEntry(Entry(tradeLog.price.toFloat(),tradeLog.tradeVolume.replace(",","").toInt(),tradeLog.tradeTime))
        }
        return entrySet
    }

    //entrySetType: TimeLine, KLine
    //fun convertTradeLogListToEntrySetByInterval(inTradeLogList: List<TradeLog>, minsInterval :Int, entrySetType:String) : EntrySet{
    fun convertTradeLogListToEntrySetByInterval(inTradeLogList: List<TradeLog>, minsInterval :Int) : EntrySet{

        val tradeLogList = inTradeLogList.reversed()
        val entrySet = EntrySet()
        val startTime = SimpleDateFormat("HH:mm", Locale.getDefault()).parse("09:45")
        val endTime = SimpleDateFormat("HH:mm", Locale.getDefault()).parse("17:15")
        var currentTimeIntervalStart = startTime
        var currentTimeIntervalEnd: Date

        var iPos = 0

        var calendar = GregorianCalendar()


        while(currentTimeIntervalStart < endTime && iPos < tradeLogList.size){
            //calculate new time span.
            calendar.time = currentTimeIntervalStart
            calendar.add(GregorianCalendar.MINUTE, minsInterval)
            currentTimeIntervalEnd = calendar.time

            //pass time span if no trades.
            if(currentTimeIntervalEnd < SimpleDateFormat("HH:mm", Locale.getDefault()).parse(tradeLogList[iPos].tradeTime) ) {
                currentTimeIntervalStart = currentTimeIntervalEnd
                continue
            }

            var open_price = tradeLogList[iPos].price.toFloat()
            var high_price = tradeLogList[iPos].price.toFloat()
            var low_price = tradeLogList[iPos].price.toFloat()
            var close_price = tradeLogList[iPos].price.toFloat()
            var volume = tradeLogList[iPos].tradeVolume.replace(",","").toInt()
            var date = tradeLogList[iPos].tradeTime//remove date "2020-05-19 "

            iPos++

            //merge records in one time interval span.
            while(iPos < tradeLogList.size ){
                if(SimpleDateFormat("HH:mm", Locale.getDefault()).parse(tradeLogList[iPos].tradeTime) > currentTimeIntervalEnd) break
                if(tradeLogList[iPos].price.toFloat() > high_price) high_price = tradeLogList[iPos].price.toFloat()
                if(tradeLogList[iPos].price.toFloat() < low_price ) low_price = tradeLogList[iPos].price.toFloat()
                close_price = tradeLogList[iPos].price.toFloat()
                volume += tradeLogList[iPos].tradeVolume.replace(",","").toInt()
                iPos++
            }

//            if(entrySetType == "TimeLine") {
//                entrySet.addEntry(
//                    Entry(
//                        close_price,
//                        volume,
//                        date
//                    )
//                )
//            }
//
//            if(entrySetType == "KLine") {
                entrySet.addEntry(
                    Entry(
                        open_price,
                        high_price,
                        low_price,
                        close_price,
                        volume,
                        date
                    )
                )
//            }


            currentTimeIntervalStart = currentTimeIntervalEnd
        }

        return entrySet
    }

    //make entrySet fix 1 mins each.
    fun expandEntrySet(oldEntrySet: EntrySet):EntrySet{

        val newEntrySet = EntrySet()

        //check input
        if(oldEntrySet.entryList.size < 1)
            return newEntrySet

        var firstTime = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(oldEntrySet.entryList[0].xLabel)
        val firstPrice = oldEntrySet.entryList[0].close

        val startTime = SimpleDateFormat("HH:mm", Locale.getDefault()).parse("10:00")
        val endTime = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(oldEntrySet.entryList[oldEntrySet.entryList.size-1].xLabel)

        var currentTime = startTime

        var iPos = 0

        var calendar = GregorianCalendar()

        //fill with the blank entries before the first entry.
        while(currentTime < firstTime){
            newEntrySet.addEntry(
                Entry(
                    firstPrice,
                    firstPrice,
                    firstPrice,
                    firstPrice,
                    0,
                    currentTime.formatTo("HH:mm")
                )
            )
            //calculate new time span.
            calendar.time = currentTime
            calendar.add(GregorianCalendar.MINUTE, 1)
            currentTime = calendar.time
        }

        //insert the blank entries between original entries.
        while(currentTime < endTime && iPos < oldEntrySet.entryList.size) {

            newEntrySet.addEntry(oldEntrySet.entryList[iPos])
            iPos++

            //calculate new time span.
            calendar.time = currentTime
            calendar.add(GregorianCalendar.MINUTE, 1)
            currentTime = calendar.time

            //check gap
            if(iPos < oldEntrySet.entryList.size) {
                firstTime = SimpleDateFormat(
                    "HH:mm",
                    Locale.getDefault()
                ).parse(oldEntrySet.entryList[iPos].xLabel)

                //fill with the blank entries into gap.
                while(currentTime < firstTime){
                    newEntrySet.addEntry(
                        Entry(
                            oldEntrySet.entryList[iPos].close,
                            oldEntrySet.entryList[iPos].close,
                            oldEntrySet.entryList[iPos].close,
                            oldEntrySet.entryList[iPos].close,
                            0,
                            currentTime.formatTo("HH:mm")
                        )
                    )
                    //calculate new time span.
                    calendar.time = currentTime
                    calendar.add(GregorianCalendar.MINUTE, 1)
                    currentTime = calendar.time
                }
            }


        }


        return newEntrySet
    }

    fun getScreenInfoListByType(sortType: String): List<StockScreenInfo> {
        val stockScreenInfoList = mutableListOf<StockScreenInfo>()

        this.stockInfoList = sortStockInfoList(sortType)

        this.stockInfoList.forEach {stockInfo ->
            val stockStockInfo = StockScreenInfo()
            stockStockInfo.copyFromStokInfo(stockInfo)
            stockScreenInfoList.add(stockStockInfo)
        }

        return stockScreenInfoList
    }

    fun storeTradeInfoFromWebToDBByStockCode(newStockCode: String) {

    }

    fun sortStockInfoList(sortType:String) : List<StockInfo>{
        var stockInfoList = emptyList<StockInfo>()

        if(sortType == "VALUE") {
            stockInfoList = this.stockInfoList.sortedByDescending {
                it.value.replace(",", "").toFloat()
            }
        }

        if(sortType == "PERCENTCHANGE") {
            stockInfoList = this.stockInfoList.sortedByDescending {
                it.changePercent.replace("%", "").toFloat()
            }
        }

        if(sortType == "MKTCAP") {
            stockInfoList = this.stockInfoList.sortedByDescending {
                it.capitalisation.replace(",", "").toFloat()
            }
        }

        return stockInfoList
    }

    fun getTodayTradeLogFromNZX(stockCode: String): List<TradeLog> {
        val tradeLogList = mutableListOf<TradeLog>()

        //split to 1 mins.
//        val entrySet = getTodayIntraEntrySet(stockCode)
//        entrySet.entryList.forEach { entry->
//            val tradeLog = TradeLog(0,stockCode,entry.volume.toString(),entry.xLabel,entry.close.toString(),"")
//            tradeLogList.add(tradeLog)
//        }

        //keep 5 mins.
        val entrySet = getIntraDayEntrySetByStockCode(stockCode)
        val today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        entrySet.entryList.forEach { entry ->
            val entryTime = timeFormat(entry.xLabel)
            if (entryTime > today) {
                val tradeLog = TradeLog(
                    0,
                    stockCode,
                    entry.volume.toString(),
                    timeFormat(entry.xLabel).replace(today,"").trim(),
                    entry.close.toString(),
                    ""
                )
                tradeLogList.add(tradeLog)
            }
        }

        return tradeLogList.reversed()
    }

    //get one stock info
    fun getStockInfo(stockCode:String):NZXStockInfo {
        return nzxWeb.extractStockInfoFromWebPage(stockCode)
    }

    //get all stocks info
    fun getStockInfo(): List<StockInfo> {
        stockInfoList =  nzxWeb.getStockInfo()
        return stockInfoList
    }

    fun generateStockCodeList(): List<String> {
        val stockCodeList = mutableListOf<String>()
        stockInfoList.forEach {stockInfo ->
            stockCodeList.add(stockInfo.stockCode)
        }
        return stockCodeList
    }

}