package nz.co.seclib.dbroker.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nz.co.seclib.dbroker.data.repository.LoginRepository
import nz.co.seclib.dbroker.data.model.Result

import nz.co.seclib.dbroker.R
import nz.co.seclib.dbroker.ui.login.LoggedInUserView
import nz.co.seclib.dbroker.ui.login.LoginFormState
import nz.co.seclib.dbroker.ui.login.LoginResult
import nz.co.seclib.dbroker.utils.AESEncryption

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private var _userName = MutableLiveData<String>()
    val userName : LiveData<String> = _userName

    private var _password = MutableLiveData<String>()
    val password : LiveData<String> = _password

    private var _bDataSourceDirectBroking = MutableLiveData<Boolean> (false)
    var bDataSourceDirectBroking : LiveData<Boolean> = _bDataSourceDirectBroking

    init{
        viewModelScope.launch(Dispatchers.IO){
            _userName.postValue( loginRepository.getPropertyValuebyPropertyName("UserName"))
            _password.postValue( AESEncryption.decrypt(loginRepository.getPropertyValuebyPropertyName("Password")).toString())

            //get datasource
            val sDataSource = loginRepository.getPropertyValuebyPropertyName("DataSource")
            _bDataSourceDirectBroking.postValue(sDataSource == "DirectBroking")
        }
    }

    fun saveNetworkConfidential(username:String, password:String) = viewModelScope.launch(
        Dispatchers.IO){
        loginRepository.saveProperty("Password", AESEncryption.encrypt(password).toString())
        loginRepository.saveProperty("UserName",username)
    }


    fun login(username: String, password: String) {
        // can be launched in a separate asynchronous job
        val result = loginRepository.login(username, password)

        if (result is Result.Success) {
            _loginResult.value = LoginResult(
                success = LoggedInUserView(
                    displayName = result.data.displayName
                )
            )
        } else {
            _loginResult.value =
                LoginResult(error = R.string.login_failed)
        }
    }

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value =
                LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value =
                LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value =
                LoginFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}
