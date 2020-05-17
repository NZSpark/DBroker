package nz.co.seclib.dbroker.viewmodel

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import nz.co.seclib.dbroker.data.database.DBrokerRoomDatabase
import nz.co.seclib.dbroker.data.repository.NZXRepository
import nz.co.seclib.dbroker.data.webdata.NZXWeb

class TradeLogViewModelFactory(var application: Application): ViewModelProvider.Factory  {
    companion object{
        var instance: TradeLogViewModel? = null
    }
    @RequiresApi(Build.VERSION_CODES.O)
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TradeLogViewModel::class.java)) {
            if(instance == null)
                instance =
                    TradeLogViewModel(
                        nzxRepository = NZXRepository(
                            dbDao = DBrokerRoomDatabase.getDatabase(application).dbrokerDAO(),
                            nzxWeb = NZXWeb.newInstance()
                        )
                    )
            return instance as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}