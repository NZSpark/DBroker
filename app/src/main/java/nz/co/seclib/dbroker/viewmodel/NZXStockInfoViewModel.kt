package nz.co.seclib.dbroker.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import kotlinx.coroutines.*
import nz.co.seclib.dbroker.utils.AESEncryption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import nz.co.seclib.dbroker.data.database.*
import nz.co.seclib.dbroker.R
import nz.co.seclib.dbroker.data.repository.NZXRepository
import nz.co.seclib.dbroker.utils.MyApplication

@RequiresApi(Build.VERSION_CODES.O)
class NZXStockInfoViewModel(private val nzxRepository: NZXRepository) : ViewModel(){
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

    private var bDataSourceNZX = false
    private var bInitilized = false

    private var backupTradeLog = emptyList<TradeLog>()

    private val viewModelJob = SupervisorJob()

    //store the newest list of stockCode
    private val _stockCodeList = MutableLiveData<List<String>>()
    val stockCodeList = _stockCodeList

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

            if(!bDataSourceNZX)
                return@launch

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
                        //for SelectedListActivity, store data of all selected stocks.
                        selectedStockCodeList.forEach { newStockCode ->
                            if (newStockCode == stockCode) {
                                //timer has get this one.
                            } else {
                                //store data of all selected stocks
                                nzxRepository.storeTradeInfoFromWebToDBByStockCode(newStockCode)
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

    //for StockInfoActivity ---------------begin
    fun insertUserStock(stockCode: String) = viewModelScope.launch(Dispatchers.IO) {
        nzxRepository.insertUserStock(
            UserStock(
                userName,
                stockCode
            )
        )
    }

    fun deleteUserStock(stockCode: String) = viewModelScope.launch(Dispatchers.IO) {
        nzxRepository.deleteUserStock(
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
            var stockCodeList =  nzxRepository.selectStockCodeByUserID(userName)
            if(stockCodeList.size == 0){
                stockCodeList =listOf<String>("KMD","AIR")
            }

            stockCodeList.forEach { newStockCode ->
                val currentTradeInfo = nzxRepository.getCurrentTradeInfoByStockCode(newStockCode)?:return@forEach
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
            val stockScreenInfoList = nzxRepository.getScreenInfoListByType(sortType)
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
        userName = nzxRepository.getPropertyValuebyPropertyName("UserName")
        if (userName == "") userName = "UserID"

        //get Password
        password =
            AESEncryption.decrypt(nzxRepository.getPropertyValuebyPropertyName("Password"))
                .toString()

        //get TimeInterval
        var tmp = nzxRepository.getPropertyValuebyPropertyName("TimerInterval")
        if (tmp == "") tmp = "30000"
        timerInterval = tmp.toLong()
        if (timerInterval < 5000) timerInterval = 30000  //must be larger than 5s.

        //get TimeEnable
        val sTimerEnable = nzxRepository.getPropertyValuebyPropertyName("TimerEnable")
        bTimerEnable = sTimerEnable == "TRUE"

        //get datasource
        val sDataSource = nzxRepository.getPropertyValuebyPropertyName("DataSource")
        bDataSourceNZX = sDataSource == "NZX"

        _stockCurrentTradeInfoList.postValue(mutableListOf<StockCurrentTradeInfo>())

        nzxRepository.getStockInfo()  //update latest prices.

        selectedStockCodeList =  nzxRepository.selectStockCodeByUserID(userName)

        _stockCodeList.postValue(generateStockCodeList())
    }

    fun generateStockCodeList(): List<String>{
        return nzxRepository.generateStockCodeList()
    }
}