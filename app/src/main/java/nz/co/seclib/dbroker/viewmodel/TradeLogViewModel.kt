package nz.co.seclib.dbroker.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import kotlinx.coroutines.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import com.wordplat.ikvstockchart.entry.*
import nz.co.seclib.dbroker.data.database.TradeLog
import nz.co.seclib.dbroker.data.repository.NZXRepository
import nz.co.seclib.dbroker.data.model.*

@RequiresApi(Build.VERSION_CODES.O)
class TradeLogViewModel(private val nzxRepository: NZXRepository) : ViewModel(){
    //for TradeLogActivity ----begin
    private val _tradeLogList = MutableLiveData<List<TradeLog>>()
    val tradeLogList: LiveData<List<TradeLog>> = _tradeLogList
    private val _entrySet = MutableLiveData<EntrySet>()
    val entrySet : LiveData<EntrySet> = _entrySet
    //for TradeLogActivity ----end


    private val timer = Timer()
    private var bTimerEnable = false
    private var bTimerIdle = true
    private var timerInterval:Long = 30000

    private var stockCode = "KMD"


    private val viewModelJob = SupervisorJob()

    //get parameters from database. (UserName, Password, TimerInterval, TimerEnable)
    fun initWithStockCode(inStockCode: String){
        if(inStockCode == "")
            stockCode = "KMD"
        else
            stockCode = inStockCode

        CoroutineScope(viewModelJob).launch {

            //initial for TradeLogActivity
            initTradeLogActivity(stockCode)

            //only to start timer during the market trading time.
            if (bTimerEnable && bTimerIdle && checkMarketTradingTime()) {
                setTimerWithStockCode()
                bTimerIdle = false
            }
        }
    }


    //get data from web and store data into database periodically.
    private fun setTimerWithStockCode(){
        //if(stockCode == "") return
        timer.scheduleAtFixedRate(
            object : TimerTask(){
                override fun run() {
                    CoroutineScope(viewModelJob).launch {
                        //for TradeLogActivity
                        // _tradeLogList.postValue(nzxRepository.diffList)
                        //nzxRepository.diffList = emptyList()
                        val todayTradeList = nzxRepository.getTodayTradeLog(stockCode).reversed()
                        _tradeLogList.postValue(todayTradeList)
                        _entrySet.postValue(nzxRepository.copyTradeLogListToEntrySet(todayTradeList))
                    }
                }
            },0,timerInterval
        )
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    //market open time. from "09:45" to "17:15".
    private fun checkMarketTradingTime():Boolean{
        val currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        val openTime = "09:45"
        val closeTime = "17:15"
        if(currentTime > openTime && currentTime < closeTime)
            return true
        return false
    }

    fun initTradeLogActivity(stockCode: String) = viewModelScope.launch(Dispatchers.IO) {
        val todayTradeList = nzxRepository.getTodayTradeLog(stockCode).reversed()
        //val todayTradeList = nzxRepository.getTradeLogByTime("2020-05-15 ","2020-05-15+",stockCode).reversed()
            _tradeLogList.postValue(todayTradeList)

        //_entrySet.postValue(nzxRepository.copyTradeLogListToEntrySet(todayTradeList))

        _entrySet.postValue(nzxRepository.getIntraDayEntrySetByStockCode(stockCode))
        //_entrySet.postValue(nzxRepository.convertTradeLogListToEntrySetByInterval(todayTradeList,5))
    }

}