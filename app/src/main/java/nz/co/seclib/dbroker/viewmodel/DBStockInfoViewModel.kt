package nz.co.seclib.dbroker.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import kotlinx.coroutines.*
import nz.co.seclib.dbroker.data.repository.DBRepository
import nz.co.seclib.dbroker.utils.AESEncryption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import nz.co.seclib.dbroker.data.database.*
import nz.co.seclib.dbroker.R
import nz.co.seclib.dbroker.utils.MyApplication

@RequiresApi(Build.VERSION_CODES.O)
class DBStockInfoViewModel(private val dbRepository: DBRepository) : ViewModel(){
    //for TradeLogActivity ----begin
//    private val _tradeLogList = MutableLiveData<List<TradeLog>>()
//    val tradeLogList: LiveData<List<TradeLog>> = _tradeLogList
//    private val _entrySet = MutableLiveData<EntrySet>()
//    val entrySet : LiveData<EntrySet> = _entrySet
    //for TradeLogActivity ----end

    //for SelectedStocksActivity --begin
    private var userName = "UserID"
    private var password = ""
    private val _stockCurrentTradeInfoList = MutableLiveData<List<StockCurrentTradeInfo>>()
    val stockCurrentTradeInfoList : LiveData<List<StockCurrentTradeInfo>> = _stockCurrentTradeInfoList
    private var selectedStockCodeList = listOf<String>()
    //for SelectedStocksActivity --end

    //for StockInfoActivity ----begin
    private val _stockCurrentTradeInfo = MutableLiveData<StockCurrentTradeInfo>()
    val stockCurrentTradeInfo: LiveData<StockCurrentTradeInfo> = _stockCurrentTradeInfo
    private val _askBidLog = MutableLiveData<AskBidLog>()
    val askBidLog = _askBidLog
    //for StockInfoActivity ----end

    private val timer = Timer()
    private var bTimerEnable = false
    private var bTimerIdle = true
    private var timerInterval:Long = 30000

    private var stockCode = ""

    var bDataSourceDirectBroking = false
    private var bInitilized = false

    private var backupTradeLog = emptyList<TradeLog>()

    private val viewModelJob = SupervisorJob()

    //get parameters from database. (UserName, Password, TimerInterval, TimerEnable)
    fun initWithStockCode(inStockCode: String){
        if(inStockCode == "")
            stockCode = "KMD"
        else
            stockCode = inStockCode

        CoroutineScope(viewModelJob).launch {

            if (!bInitilized) {
                initConstVariable()
            }

            if(!bDataSourceDirectBroking)
                return@launch

            //initial for TradeLogActivity
            //initTradeLogActivity(stockCode)

            //initial for StockInfoActivity
            _stockCurrentTradeInfo.postValue(
                dbRepository.getCurrentTradeInfoByStockCode(
                    stockCode
                )
            )
            _askBidLog.postValue(dbRepository.getAskBidListByStockCode(stockCode))

            //initial for SelectedStocksActivity
            // will update _stockCurrentTradeInfoList, _stockCurrentTradeInfo at the same time.
            //updateSelectedStockListByUserID(userName)

            //only to start timer during the market trading time.
            if (bTimerEnable && bTimerIdle && checkMarketTradingTime()) {
                setTimerWithStockCode()
                bTimerIdle = false
            }
        }
    }

    //get data from web and store data into database periodically.
    private fun setTimerWithStockCode(){
        if(stockCode == "") return
        timer.scheduleAtFixedRate(
            object : TimerTask(){
                override fun run() {
                    CoroutineScope(viewModelJob).launch {
                        //store data to database
//                        tradeLogRepository.storeTradeInfoFromWebToDBByStockCode(stockCode)
//                        delay(1000)

                        //for StockInfoActivity
                        _stockCurrentTradeInfo.postValue(dbRepository.getCurrentTradeInfoByStockCode(stockCode))
                        delay(1000)
                        _askBidLog.postValue(dbRepository.getAskBidListByStockCode(stockCode))
                        delay(1000)
                        //for SelectedListActivity, store data of all selected stocks.
                        selectedStockCodeList.forEach { newStockCode ->
                            if (newStockCode == stockCode) {
                                //timer has get this one.
                            } else {
                                //store data of all selected stocks
                                dbRepository.storeTradeInfoFromWebToDBByStockCode(newStockCode)
                                delay(500)
                            }
                        }
                    }
                }
            },0,timerInterval
        )
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    //add one item
//    fun addNewTradeLog(tradeLog: TradeLog){
//        val temp = mutableListOf<TradeLog>()
//        temp.add(tradeLog)
//        _tradeLogList.postValue(temp)
//    }

    //add a collection of items
//    fun addNewTradeLog(tradeLogList:List<TradeLog>){
//        _tradeLogList.postValue(tradeLogList)
//    }

    //for StockInfoActivity ---------------begin
    fun insertUserStock(stockCode: String) = viewModelScope.launch(Dispatchers.IO) {
        dbRepository.insertUserStock(
            UserStock(
                userName,
                stockCode
            )
        )
    }

    fun deleteUserStock(stockCode: String) = viewModelScope.launch(Dispatchers.IO) {
        dbRepository.deleteUserStock(
            UserStock(
                userName,
                stockCode
            )
        )
    }

    //update data by selected stocks list.
    fun getSelectedStockList() =
        viewModelScope.launch(Dispatchers.IO) {
        val stockCurrentTradeInfoList = mutableListOf<StockCurrentTradeInfo>()
        val stockCodeList =  dbRepository.selectStockCodeByUserID(userName)
        stockCodeList.forEach { newStockCode ->
//            if(!bTimerIdle && newStockCode == stockCode) {
//                //timer has get this one.
//            }else{
//                //store data of all selected stocks
//                tradeLogRepository.storeTradeInfoFromWebToDBByStockCode(stockCode)
//                delay(100)
//            }
            val currentTradeInfo = dbRepository.getCurrentTradeInfoByStockCode(newStockCode)?:return@forEach
            delay(200) //website will block your IP address if the frequency of request is too high.
            stockCurrentTradeInfoList.add(currentTradeInfo)
        }
        _stockCurrentTradeInfoList.postValue(stockCurrentTradeInfoList)
    }
    //for StockInfoActivity ---------------end

    //market open time. from "09:45" to "17:15".
    private fun checkMarketTradingTime():Boolean{
        val currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        val openTime = MyApplication.instance.applicationContext.getString ( R.string.market_start_time)
        val closeTime = MyApplication.instance.applicationContext.getString ( R.string.market_end_time)
        if(currentTime > openTime && currentTime < closeTime)
            return true
        return false
    }

    //for SelectedActivity. sortType = "VALUE"
    fun getScreenInfoListByType(sortType:String) =
        viewModelScope.launch(Dispatchers.IO) {
            val stockScreenInfoList = dbRepository.getScreenInfoListByType(sortType)
            val stockCurrentTradeInfoList = StockScreenInfo.convertScreenInfoListToStockCurrentTradeInfoList(stockScreenInfoList) as MutableList<StockCurrentTradeInfo>
            _stockCurrentTradeInfoList.postValue(stockCurrentTradeInfoList)
    }

//    fun initTradeLogActivity(stockCode: String) = viewModelScope.launch(Dispatchers.IO) {
//        val todayTradeList = tradeLogRepository.getTodayTradeLog(stockCode).reversed()
//        _tradeLogList.postValue(todayTradeList)
//
//        _entrySet.postValue(tradeLogRepository.copyTradeLogListToEntrySet(todayTradeList))
//    }

    fun initConstVariable() = viewModelScope.launch(Dispatchers.IO) {
        //get UserName
        userName = dbRepository.getPropertyValuebyPropertyName("UserName")
        if (userName == "") userName = "UserID"

        //get Password
        password =
            AESEncryption.decrypt(dbRepository.getPropertyValuebyPropertyName("Password"))
                .toString()

        //get TimeInterval
        var tmp = dbRepository.getPropertyValuebyPropertyName("TimerInterval")
        if (tmp == "") tmp = "30000"
        timerInterval = tmp.toLong()
        if (timerInterval < 5000) timerInterval = 30000  //must be larger than 5s.

        //get TimeEnable
        val sTimerEnable = dbRepository.getPropertyValuebyPropertyName("TimerEnable")
        bTimerEnable = sTimerEnable == "TRUE"

        //get datasource
        val sDataSource = dbRepository.getPropertyValuebyPropertyName("DataSource")
        bDataSourceDirectBroking = sDataSource == "DirectBroking"

        _stockCurrentTradeInfoList.postValue(mutableListOf<StockCurrentTradeInfo>())
        if (userName != "UserID")
            dbRepository.setNetWortConfidential(userName, password)

        selectedStockCodeList =  dbRepository.selectStockCodeByUserID(userName)
    }

}