package nz.co.seclib.dbroker.ui.sysinfo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_system_config.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import nz.co.seclib.dbroker.R
import nz.co.seclib.dbroker.ui.stockinfo.SearchActivity
import nz.co.seclib.dbroker.ui.stockinfo.DBSelectedStocksActivity
import nz.co.seclib.dbroker.ui.stockinfo.StockInfoActivity
import nz.co.seclib.dbroker.ui.stockinfo.DBTradeLogActivity
import nz.co.seclib.dbroker.utils.AESEncryption
import nz.co.seclib.dbroker.viewmodel.SystemConfigViewModel
import nz.co.seclib.dbroker.viewmodel.SystemConfigViewModelFactory

class SystemConfigActivity : AppCompatActivity() , CoroutineScope by MainScope(){
    private lateinit var systemConfigViewModel: SystemConfigViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_system_config)

        //val etTimerInterval = findViewById<EditText>(R.id.etTimerInterval)

        systemConfigViewModel = SystemConfigViewModelFactory(
            this.application
        ).create(
            SystemConfigViewModel::class.java)

        systemConfigViewModel.timerInterval.observe(this, Observer {
            etTimerInterval.setText(it)
        })
        systemConfigViewModel.timerEnable.observe(this, Observer {
            cbTimerEnable.isChecked = it == "TRUE"
        })

        systemConfigViewModel.userName.observe(this, Observer {
            etUserName.setText(it)
        })
        systemConfigViewModel.password.observe(this, Observer {
            etPassword.setText(it)
        })

        systemConfigViewModel.dataSource.observe(this, Observer {
            if(it == "DirectBroking")
                rbDataSourceDirctBroking.isChecked = true
            else
                rbDataSourceNZX.isChecked = true
        })

        ivSave.setOnClickListener {
            systemConfigViewModel.saveTimerIntervalToDB(etTimerInterval.text.toString())
            if(cbTimerEnable.isChecked)
                systemConfigViewModel.saveTimerEnableToDB("TRUE")
            else
                systemConfigViewModel.saveTimerEnableToDB("FALSE")
            systemConfigViewModel.saveUserNameToDB(etUserName.text.toString())
            systemConfigViewModel.savePasswordToDB(AESEncryption.encrypt( etPassword.text.toString()).toString())

            if(rbDataSourceDirctBroking.isChecked)
                systemConfigViewModel.saveDataSourceToDB(rbDataSourceDirctBroking.text.toString())
            if(rbDataSourceNZX.isChecked)
                systemConfigViewModel.saveDataSourceToDB(rbDataSourceNZX.text.toString())

            Toast.makeText(this,"System properties are stored in database!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_list, menu)
        return true
    }


    override fun onOptionsItemSelected( item: MenuItem) :Boolean{
        when (item.itemId){
            R.id.menu_selected_stocks -> {
                val intent = Intent(this, DBSelectedStocksActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_stock_info -> {
                val intent = Intent(this, StockInfoActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_stock_search -> {
                val intent = Intent(this, SearchActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_stock_trade_info -> {
                val intent = Intent(this, DBTradeLogActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_system_parameters -> {
                val intent = Intent(this, SystemConfigActivity::class.java)
                startActivity(intent)
            }

        }
        return true
    }
}
