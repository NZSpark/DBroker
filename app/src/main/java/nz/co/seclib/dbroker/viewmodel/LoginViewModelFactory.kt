package nz.co.seclib.dbroker.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import nz.co.seclib.dbroker.data.database.DBrokerRoomDatabase
import nz.co.seclib.dbroker.data.repository.LoginRepository
import nz.co.seclib.dbroker.data.webdata.DirectBrokingWeb

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
class LoginViewModelFactory(val application: Application) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(
                loginRepository = LoginRepository(
                    dbDao = DBrokerRoomDatabase.getDatabase(application).dbrokerDAO(),
                    dbWeb = DirectBrokingWeb.newInstance()
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
