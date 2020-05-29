package nz.co.seclib.dbroker.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import nz.co.seclib.dbroker.data.repository.SystemConfigRepository
import nz.co.seclib.dbroker.utils.AESEncryption

class SystemConfigViewModel(private val systemConfigRepository: SystemConfigRepository) : ViewModel() {
    //TimerInterval
    private var _timerInterval = MutableLiveData<String>()
    val timerInterval = _timerInterval

    //TimerEnable
    private var _timerEnable = MutableLiveData<String>()
    val timerEnable = _timerEnable

    //UserName
    private var _userName = MutableLiveData<String>()
    val userName = _userName

    //Password
    private var _password = MutableLiveData<String>()
    val password = _password

    //DataSource
    private var _dataSource = MutableLiveData<String>()
    val dataSource = _dataSource

    private val viewModelJob = SupervisorJob()

    init {
        CoroutineScope(viewModelJob).launch{
            var sTemp = getTimerIntervalFromDB()
            if (sTemp != "")
                _timerInterval.postValue(sTemp)

            sTemp = getTimerEnableFromDB()
            if (sTemp != "")
                _timerEnable.postValue(sTemp)

            sTemp = getUserNameFromDB()
            if (sTemp != "")
                _userName.postValue(sTemp)

            sTemp = getPasswordFromDB()
            if (sTemp != "")
                _password.postValue(sTemp)

            sTemp = getDataSourceFromDB()
            if (sTemp != "")
                _dataSource.postValue(sTemp)
        }
    }

    fun getTimerIntervalFromDB():String{
        return systemConfigRepository.getPropertyValuebyPropertyName("TimerInterval")
    }

    fun saveTimerIntervalToDB(timerInterval:String){
        CoroutineScope(viewModelJob).launch {
            systemConfigRepository.saveProperty("TimerInterval", timerInterval)
        }
    }

    fun getTimerEnableFromDB():String{
        return systemConfigRepository.getPropertyValuebyPropertyName("TimerEnable")
    }

    fun saveTimerEnableToDB(timerEnable:String){
        CoroutineScope(viewModelJob).launch {
            systemConfigRepository.saveProperty("TimerEnable", timerEnable)
        }
    }

    fun getUserNameFromDB():String{
        return systemConfigRepository.getPropertyValuebyPropertyName("UserName")
    }

    fun saveUserNameToDB(userName:String){
        CoroutineScope(viewModelJob).launch {
            systemConfigRepository.saveProperty("UserName", userName)
        }
    }
    fun getPasswordFromDB():String{
        return AESEncryption.decrypt( systemConfigRepository.getPropertyValuebyPropertyName("Password")).toString()
    }

    fun savePasswordToDB(password:String){
        CoroutineScope(viewModelJob).launch {
            systemConfigRepository.saveProperty("Password", password)
        }
    }

    fun getDataSourceFromDB():String{
        return systemConfigRepository.getPropertyValuebyPropertyName("DataSource")
    }

    fun saveDataSourceToDB(dataSource:String){
        CoroutineScope(viewModelJob).launch {
            systemConfigRepository.saveProperty("DataSource", dataSource)
        }
    }
}