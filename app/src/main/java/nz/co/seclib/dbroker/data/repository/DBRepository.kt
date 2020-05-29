package nz.co.seclib.dbroker.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.wordplat.ikvstockchart.entry.Entry
import com.wordplat.ikvstockchart.entry.EntrySet
import nz.co.seclib.dbroker.data.database.*
import nz.co.seclib.dbroker.data.webdata.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class DBRepository(private val dbDao: DBrokerDAO, private val dbWeb: DirectBrokingWeb) {

    private var mCurrentState: CurrentState? = null
    private var lastTradeLogList = emptyList<TradeLog>()
    private var currentTradeInfo : StockCurrentTradeInfo? = null

    private lateinit var currentTradeLogList :  List<TradeLog>
    private var currentAskBidLog : AskBidLog? = null
    var diffList = listOf<TradeLog>()

    fun setNetWortConfidential(userID: String,password:String){
        dbWeb._username = userID
        dbWeb._password = password
    }

    fun getScreenInfoListByType(sortType:String):List<StockScreenInfo>{
        return StockScreenInfo.getStockScreenInfoFromString(dbWeb.getScreenPage(sortType))
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentTradeInfoByStockCode(stockCode: String): StockCurrentTradeInfo?
    {
//        if(currentTradeInfo == null)
//            currentTradeInfo =  dbWeb.GetStockInfo(stockCode)
//
//        return currentTradeInfo
        return dbWeb.getStockInfo(stockCode)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getAskBidListByStockCode(stockCode: String): AskBidLog?{
        //if(currentAskBidLog == null)
        //getTradeInfoFromWebByStockCode(stockCode)
        storeTradeInfoFromWebToDBByStockCode(stockCode)
        return currentAskBidLog
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun test(){
//        storeTradeInfoFromWebToDBByStockCode("KMD")
//        val askBidLogList = getTodayAskBidLog("KMD")
//        println("test")
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

    //Get today's ask and bid logs
    @RequiresApi(Build.VERSION_CODES.O)
    fun getTodayAskBidLog(stockCode: String): List<AskBidLog>{
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formatted = current.format(formatter)
        val startTime = formatted + " "
        val endTime = formatted + "+"
        return dbDao.selectAskBidLogByTime(startTime,endTime,stockCode)
    }

    //Fetch trade logs from website
    @RequiresApi(Build.VERSION_CODES.O)
    fun getTradeInfoFromWebByStockCode(stockCode:String): Boolean {
        val recentTradesTable = TradesTable()
        val bidsTable = BidsTable()
        val asksTable = AsksTable()

        currentAskBidLog = null
        currentTradeLogList = emptyList()

//        var currentState : CurrentState = mCurrentState ?: CurrentState(stockCode,
//            JavaNetCookieJar(CookieManager()),"","","",""
//        )
//        currentState = dbWeb.getLastDepthPage(currentState)
//        mCurrentState = currentState
        var depthPage = dbWeb.getLastDepthPage(stockCode)

        bidsTable.GetBidsTableFromString(dbWeb.getTableString(depthPage,"BidsTable"))

        //try twice.
        if (bidsTable.bidsList.size < 1) {
//            currentState = dbWeb.getDepthPage(stockCode)
//            mCurrentState = currentState
            depthPage = dbWeb.getDepthPage(stockCode)
            bidsTable.GetBidsTableFromString(dbWeb.getTableString(depthPage,"BidsTable"))
        }
        if (bidsTable.bidsList.size < 1) return false

        asksTable.GetAsksTableFromString(dbWeb.getTableString(depthPage, "AsksTable"))
        recentTradesTable.GetRecentTradesTableFromString(dbWeb.getTableString(depthPage,"RecentTradesTable"))

        val currentTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val formattedCurrentTime = currentTime.format(formatter)

        currentAskBidLog = AskBidLog(
            0,
            stockCode,
            asksTable.asksList,
            bidsTable.bidsList,
            recentTradesTable.tradesList,
            formattedCurrentTime
        )
        currentTradeLogList = recentTradesTable.getTradeLogListFromTable(stockCode)
        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun storeTradeInfoFromWebToDBByStockCode(stockCode: String){
        //set currentTradeLogList,currentAskBidLog
        if(!getTradeInfoFromWebByStockCode(stockCode)) return

        //anyway, each time the ask and bid list should be inserted into Database.
        if(currentAskBidLog != null )
            insertAskBidLog(currentAskBidLog)

//        if( lastTradeLogList.size == 0 ){
            //initilize list from database, since select with desc order, so reversed() isn't needed.
            lastTradeLogList = dbDao.selectTop15TradeLog(stockCode)
            lastTradeLogList.forEach(){
                it.id = 0
                it.tradeTime = it.tradeTime.substring(it.tradeTime.lastIndexOf(" ") + 1)
            }
//        }

        if(currentTradeLogList.size == 0) return

        diffList = diffTwoTradeLogList(lastTradeLogList,currentTradeLogList)
//        lastTradeLogList = currentTradeLogList
        if(diffList.size > 0) {
//            println(lastTradeLogList)
//            println(currentTradeLogList)
//            println(diffList)
//            currentTradeLogList = emptyList()

            insertTradeLogList(diffList)
        }
    }

    //insert trade logs into database, trade time will be patched with today string.
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun insertTradeLogList(tradeLogList:List<TradeLog>){
        tradeLogList.reversed().forEach() {
            val currentTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd ")
            val formattedCurrentTime = currentTime.format(formatter)
            val newTradeLog = TradeLog(
                0,
                it.stockCode,
                it.tradeVolume,
                formattedCurrentTime + it.tradeTime,
                it.price,
                it.tradeCondition
            )
            dbDao.insertTradeLog(newTradeLog)
        }
    }

    //insert ask & bid logs into database
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun insertAskBidLog(askBidLog: AskBidLog?){
        if(askBidLog == null) return
        dbDao.insertAskBidLog(askBidLog)
    }

    //return the new part of the second trade logs list which not appear in the first list.
    fun diffTwoTradeLogList(t1In:List<TradeLog>, t2In:List<TradeLog>): List<TradeLog>{
        //in our case, the items in t1In and t2In both are from newest to oldest.
        //comparision is from oldest to newest, so t1In and t2In should be reversed first.
        val t1 = t1In.reversed()
        val t2 = t2In.reversed()
        val diffT = mutableListOf<TradeLog>()
        val t1Size = t1.size - 1
        val t2Size = t2.size - 1
        val matchPosition = mutableListOf<Int>()

        //t1, t2 may be null
        if(t1Size < 0 ) return t2In
        if(t2Size < 0) return diffT

        //find out how many items in t2 are same as the first item of t2
        for(i in 0..t1Size) {
            if(t2[0].toString() == t1[i].toString())
                matchPosition.add(i)
        }

        //no match means all itmes in t2 are new
        if(matchPosition.size == 0) return t2In


        var iMatch = matchPosition[0]
        matchPosition.forEach(){
            var bMatch = true
            for(i in it..t1Size){

                //t2 may be very short
                if(i-it > t2Size) break

                //any different means t2 is not start from this poistion.
                if(t1[i].toString() != t2[i-it].toString()) {
                    bMatch = false
                    break
                }
            }
            if(bMatch){
                iMatch = it
                return@forEach
            }
        }

        if(t2Size - (t1Size-iMatch) > 0)
            //return t2.subList(t2Size - (t1Size-iMatch),t2Size + 1).reversed() //it seems subList is half open set.
            return t2.subList(t1Size - iMatch + 1, t2Size + 1).reversed() //it seems subList is half open set.

        return diffT
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
}