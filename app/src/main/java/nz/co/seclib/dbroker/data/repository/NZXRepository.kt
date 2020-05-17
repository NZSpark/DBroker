package nz.co.seclib.dbroker.data.repository

import android.icu.text.SimpleDateFormat
import android.icu.util.GregorianCalendar
import android.os.Build
import androidx.annotation.RequiresApi
import com.wordplat.ikvstockchart.entry.Entry
import com.wordplat.ikvstockchart.entry.EntrySet
import nz.co.seclib.dbroker.data.database.DBrokerDAO
import nz.co.seclib.dbroker.data.webdata.NZXWeb
import nz.co.seclib.dbroker.data.database.TradeLog
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class NZXRepository(private val dbDao: DBrokerDAO, private val nzxWeb: NZXWeb) {
    //line data
    fun getIntraDayEntrySetByStockCode(stockCode:String):EntrySet{
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

    fun convertTradeLogListToEntrySetByInterval(inTradeLogList: List<TradeLog>, minsInterval :Int) : EntrySet{
        val tradeLogList = inTradeLogList.reversed()
        val entrySet = EntrySet()
        val sartTime = SimpleDateFormat("HH:mm", Locale.getDefault()).parse("09:45")
        val endTime = SimpleDateFormat("HH:mm", Locale.getDefault()).parse("17:15")
        var currentTimeIntervalStart = sartTime
        var currentTimeIntervalEnd = sartTime

        var iPos = 0

        var calendar = GregorianCalendar()


        while(currentTimeIntervalStart < endTime && iPos < tradeLogList.size){
            //calculate new time span.
            calendar.time = currentTimeIntervalStart
            calendar.add(GregorianCalendar.MINUTE, minsInterval)
            currentTimeIntervalEnd = calendar.time

            //pass time span if no trades.
            if(currentTimeIntervalEnd < SimpleDateFormat("HH:mm", Locale.getDefault()).parse(tradeLogList[iPos].tradeTime.replace("2020-05-15 ","")) ) {
                currentTimeIntervalStart = currentTimeIntervalEnd
                continue
            }

            var open_price = tradeLogList[iPos].price.toFloat()
            var high_price = tradeLogList[iPos].price.toFloat()
            var low_price = tradeLogList[iPos].price.toFloat()
            var close_price = tradeLogList[iPos].price.toFloat()
            var volume = tradeLogList[iPos].tradeVolume.replace(",","").toInt()
            var date = tradeLogList[iPos].tradeTime.replace("2020-05-15 ","")

            iPos++

            while(iPos < tradeLogList.size ){
                if(SimpleDateFormat("HH:mm", Locale.getDefault()).parse(tradeLogList[iPos].tradeTime.replace("2020-05-15 ","")) > currentTimeIntervalEnd) break
                if(tradeLogList[iPos].price.toFloat() > high_price) high_price = tradeLogList[iPos].price.toFloat()
                if(tradeLogList[iPos].price.toFloat() < low_price ) low_price = tradeLogList[iPos].price.toFloat()
                close_price = tradeLogList[iPos].price.toFloat()
                volume += tradeLogList[iPos].tradeVolume.replace(",","").toInt()
                iPos++
            }

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
            currentTimeIntervalStart = currentTimeIntervalEnd
        }

        return entrySet
    }


    /*
    fun convertTradeLogListByInterval(tradeLogList: List<TradeLog>, minsInterval :Int) :List<TradeLog>{
        val newTradeLogList = mutableListOf<TradeLog>()
        val iLoopNumber :Int = tradeLogList.size / minsInterval
        val iLeftNumber  = tradeLogList.size - iLoopNumber * minsInterval

        for( i in 0..iLoopNumber - 1){
            val tradeLog = TradeLog(id=0)
            var sumValue :Float = 0F
            for(j in 0..minsInterval - 1){
                sumValue = tradeLog.price.toFloat() *  tradeLogList[i*minsInterval+j].tradeVolume.toInt()
                tradeLog.tradeVolume = (tradeLog.tradeVolume.replace(",","").toInt() + tradeLogList[i*minsInterval+j].tradeVolume.replace(",","").toInt()).toString()
                tradeLog.tradeCondition += tradeLogList[i*minsInterval+j].tradeCondition
            }
            tradeLog.price = (sumValue / tradeLog.tradeVolume.toInt() ).toString()
            tradeLog.tradeVolume = (tradeLog.tradeVolume.replace(",","").toInt()/minsInterval).toString()

            newTradeLogList.add(tradeLog)
        }

        if( iLeftNumber > 0 ){
            val tradeLog = TradeLog(id=0)
            var sumValue :Float = 0F
            for(i in iLoopNumber * minsInterval..tradeLogList.size - 1){
                sumValue = tradeLog.price.toFloat() *  tradeLogList[i].tradeVolume.toInt()
                tradeLog.tradeVolume = (tradeLog.tradeVolume.replace(",","").toInt() + tradeLogList[i].tradeVolume.replace(",","").toInt()).toString()
                tradeLog.tradeCondition += tradeLogList[i].tradeCondition
            }
            tradeLog.price = (sumValue / tradeLog.tradeVolume.toInt() ).toString()
            tradeLog.tradeVolume = (tradeLog.tradeVolume.replace(",","").toInt()/minsInterval).toString()

            newTradeLogList.add(tradeLog)
        }

        return newTradeLogList
    }
     */
}