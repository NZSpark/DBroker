package nz.co.seclib.dbroker.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import nz.co.seclib.dbroker.data.repository.NZXRepository
import nz.co.seclib.dbroker.data.webdata.AsksRow
import nz.co.seclib.dbroker.data.webdata.BidsRow
import nz.co.seclib.dbroker.data.webdata.NZXWeb
import nz.co.seclib.dbroker.data.webdata.TradesRow
import org.jsoup.Jsoup
import java.lang.reflect.Type
import java.sql.Date
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class StockMarketInfo {
    //val usrStockCodeList = mutableListOf("ABA","AIA","AIR","ANZ","ATM","CEN", "KMD","FPH","FBU","GMT","MCY", "MEL","MET","MFT","OCA","RYM","RBD","SAN","SKC","SPK","SUM","VHP","ZEL")
    val usrStockCodeList = mutableListOf("ABA","AIA")

    val stockCodeList = listOf("ABA","AFC","AFI","AFT","AGG","AIA","AIR","ALF","AMP","ANZ","AOR","APA","APL","ARB","ARG","ARV","ASD","ASF","ASP","ASR","ATM","AUG","AWF","BFG","BGI","BGP","BIT","BLT","BOT","BRM","CAV","CBD","CDI","CEN","CGF","CMO","CNU","CO2","CRP","CVT","DGL","DIV","DOW","EBO","EMF","EMG","ENS","ENSRB","ERD","ESG","EUF","EUG","EVO","FBU","FCT","FIN","FNZ","FPH","FRE","FSF","FWL","GBF","GEN","GENWB","GEO","GFL","GMT","GNE","GSH","GTK","GXH","HFL","HGH","HLG","IFT","IKE","IPL","JLG","JPG","JPN","KFL","KFLWF","KMD","KPG","LIC","LIV","MCK","MCKPA","MCY","MDZ","MEE","MEL","MET","MFT","MGL","MHJ","MLN","MLNWD","MMH","MOA","MPG","MWE","MZY","NPF","NPH","NTL","NTLOB","NWF","NZB","NZC","NZK","NZM","NZO","NZR","NZX","OCA","OZY","PCT","PCTHA","PEB","PFI","PGW","PIL","PLP","PLX","POT","PPH","PYS","QEX","RAK","RBD","RYM","SAN","SCL","SCT","SCY","SDL","SEA","SEK","SKC","SKL","SKO","SKT","SML","SNC","SNK","SPG","SPK","SPN","SPY","SRF","STU","SUM","TCL","TEM","TGG","THL","TLL","TLS","TLT","TNZ","TPW","TRA","TRS","TRU","TWF","TWR","USA","USF","USG","USM","USS","USV","VCT","VGL","VHP","VTL","WBC","WDT","WHS","ZEL")

    val stockCompanyList = listOf("Abano Healthcare Group Limited" ,
            "AFC Group Holdings Limited" ,
            "Australian Foundation Investment Company Limited" ,
            "AFT Pharmaceuticals Limited" ,
            "Smartshares Global Aggregate Bond ETF" ,
            "Auckland International Airport Limited" ,
            "Air New Zealand Limited (NS)" ,
            "Allied Farmers Limited" ,
            "AMP Limited" ,
            "Australia and New Zealand Banking Group Limited" ,
            "Aorere Resources Limited" ,
            "Smartshares Asia Pacific ETF" ,
            "Asset Plus Limited" ,
            "ArborGen Holdings Limited" ,
            "Argosy Property Limited" ,
            "Arvida Group Limited" ,
            "Smartshares Australian Dividend ETF" ,
            "Smartshares Australian Financials ETF" ,
            "Smartshares Australian Property ETF" ,
            "Smartshares Australian Resources ETF" ,
            "The a2 Milk Company Limited" ,
            "Augusta Capital Limited" ,
            "AWF Madison Group Limited" ,
            "Burger Fuel Group Limited" ,
            "Blackwell Global Holdings Limited" ,
            "Briscoe Group Limited" ,
            "The Bankers Investment Trust Plc" ,
            "Blis Technologies Limited" ,
            "Smartshares Automation and Robotics ETF" ,
            "Barramundi Limited" ,
            "Cavalier Corporation Limited" ,
            "Cannasouth Limited" ,
            "CDL Investments New Zealand Limited" ,
            "Contact Energy Limited" ,
            "Cooks Global Foods Limited" ,
            "The Colonial Motor Company Limited" ,
            "Chorus Limited (NS)" ,
            "Carbon Fund" ,
            "Chatham Rock Phosphate Limited" ,
            "Comvita Limited" ,
            "Delegat Group Limited" ,
            "Smartshares NZ Dividend ETF" ,
            "Downer EDI Limited" ,
            "Ebos Group Limited" ,
            "Smartshares Emerging Markets ETF" ,
            "Smartshares Emerging Markets Equities ESG ETF" ,
            "Enprise Group Limited" ,
            "Enprise Group Limited" ,
            "EROAD Limited" ,
            "Smartshares Global Equities ESG ETF" ,
            "Smartshares Europe ETF" ,
            "Smartshares Europe Equities ESG ETF" ,
            "Evolve Education Group Limited" ,
            "Fletcher Building Limited" ,
            "F&C Investment Trust PLC" ,
            "Finzsoft Solutions Limited" ,
            "Smartshares NZ Top 50 ETF" ,
            "Fisher & Paykel Healthcare Corporation Limited" ,
            "Freightways Limited" ,
            "Fonterra Shareholders' Fund (NS)" ,
            "Foley Wines Limited" ,
            "Smartshares Global Bond ETF" ,
            "General Capital Limited" ,
            "General Capital Limited" ,
            "Geo Limited" ,
            "Geneva Finance Limited" ,
            "Goodman Property Trust (NS)" ,
            "Genesis Energy Limited (NS)" ,
            "Good Spirits Hospitality Limited" ,
            "Gentrack Group Limited" ,
            "Green Cross Health Limited" ,
            "Henderson Far East Income Limited" ,
            "Heartland Group Holdings Limited" ,
            "Hallenstein Glasson Holdings Limited" ,
            "Infratil Limited" ,
            "ikeGPS Group Limited" ,
            "Investore Property Limited (NS)" ,
            "Just Life Group Limited" ,
            "JPMorgan Global Growth & Income plc" ,
            "Smartshares Japan Equities ESG ETF" ,
            "Kingfish Limited" ,
            "Kingfish Limited" ,
            "Kathmandu Holdings Limited" ,
            "Kiwi Property Group Limited" ,
            "Livestock Improvement Corporation Limited (NS)" ,
            "Smartshares Healthcare Innovation ETF" ,
            "Millennium & Copthorne Hotels New Zealand Limited" ,
            "Millennium & Copthorne Hotels New Zealand Limited" ,
            "Mercury NZ Limited (NS)" ,
            "Smartshares NZ Mid Cap ETF" ,
            "Me Today Limited" ,
            "Meridian Energy Limited (NS)" ,
            "Metlifecare Limited" ,
            "Mainfreight Limited" ,
            "Mercer Group Limited" ,
            "Michael Hill International Limited" ,
            "Marlin Global Limited" ,
            "Marlin Global Limited" ,
            "Marsden Maritime Holdings Limited (NS)" ,
            "Moa Group Limited" ,
            "Metro Performance Glass Limited" ,
            "Marlborough Wine Estates Group Limited" ,
            "Smartshares Australian Mid Cap ETF" ,
            "Smartshares NZ Property ETF" ,
            "Napier Port Holdings Limited" ,
            "New Talisman Gold Mines Limited" ,
            "New Talisman Gold Mines Limited" ,
            "NZ Windfarms Limited" ,
            "Smartshares NZ Bond ETF" ,
            "Smartshares NZ Cash ETF" ,
            "New Zealand King Salmon Investments Limited" ,
            "NZME Limited" ,
            "New Zealand Oil & Gas Limited" ,
            "The New Zealand Refining Company Limited" ,
            "NZX Limited" ,
            "Oceania Healthcare Limited" ,
            "Smartshares Australian Top 20 ETF" ,
            "Precinct Properties New Zealand Limited" ,
            "Precinct Properties New Zealand Limited" ,
            "Pacific Edge Limited" ,
            "Property for Industry Limited" ,
            "PGG Wrightson Limited" ,
            "Promisia Integrative Limited" ,
            "Private Land and Property Fund" ,
            "Plexure Group Limited" ,
            "Port of Tauranga Limited" ,
            "Pushpay Holdings Limited" ,
            "PaySauce Limited" ,
            "QEX Logistics Limited" ,
            "Rakon Limited" ,
            "Restaurant Brands New Zealand Limited" ,
            "Ryman Healthcare Limited" ,
            "Sanford Limited (NS)" ,
            "Scales Corporation Limited" ,
            "Scott Technology Limited" ,
            "Smiths City Group Limited" ,
            "Solution Dynamics Limited" ,
            "SeaDragon Limited" ,
            "Seeka Limited" ,
            "SkyCity Entertainment Group Limited (NS)" ,
            "Skellerup Holdings Limited" ,
            "Serko Limited" ,
            "Sky Network Television Limited" ,
            "Synlait Milk Limited (NS)" ,
            "Southern Charter Financial Group Limited" ,
            "Snakk Media Limited (in liquidation)" ,
            "Stride Property Ltd & Stride Investment Management Ltd (NS)" ,
            "Spark New Zealand Limited" ,
            "South Port New Zealand Limited" ,
            "Smartpay Holdings Limited" ,
            "Senior Trust Retirement Village Listed Fund" ,
            "Steel & Tube Holdings Limited" ,
            "Summerset Group Holdings Limited" ,
            "The City of London Investment Trust Plc" ,
            "Templeton Emerging Markets Plc" ,
            "T&G Global Limited" ,
            "Tourism Holdings Limited" ,
            "TIL Logistics Group Limited" ,
            "Telstra Corporation Limited" ,
            "Tilt Renewables Limited" ,
            "Smartshares NZ Top 10 ETF" ,
            "Trustpower Limited" ,
            "Turners Automotive Group Limited" ,
            "TRS Investments Limited" ,
            "TruScreen Limited" ,
            "Smartshares Total World ETF" ,
            "Tower Limited" ,
            "Smartshares US Equities ESG ETF" ,
            "Smartshares US 500 ETF" ,
            "Smartshares US Large Growth ETF" ,
            "Smartshares US Mid Cap ETF" ,
            "Smartshares US Small Cap ETF" ,
            "Smartshares US Large Value ETF" ,
            "Vector Limited" ,
            "Vista Group International Limited" ,
            "Vital Healthcare Property Trust" ,
            "Vital Limited" ,
            "Westpac Banking Corporation" ,
            "Wellington Drive Technologies Limited" ,
            "The Warehouse Group Limited" ,
            "Z Energy Limited")
    companion object {
        fun getStockInfoFromNZXWebPage(webPage: String): List<StockInfo> {
            var stockInfoList = mutableListOf<StockInfo>()

            if (webPage.length < 10)
                return stockInfoList

            val currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

            val document = Jsoup.parse(webPage)

            val tableRows = document.select("table[id=instruments-table] tr")

            if (tableRows.size > 0)
                tableRows.removeAt(0)

            for (i in 0 until tableRows.size) {
                val row = tableRows[i]
                val stockInfo = StockInfo()
                val tds = row.getElementsByTag("td")
                if (tds.size < 13) continue

                stockInfo.stockCode =
                    tds[0].text().replace("\u00a0".toRegex(), "").trim { it <= ' ' }
                stockInfo.companyName =
                    tds[1].text().replace("\u00a0".toRegex(), "").trim { it <= ' ' }
                stockInfo.price = tds[2].text().replace("\u00a0".toRegex(), "").replace("$","").trim { it <= ' ' }

                //"$0.060 / 2.73%"
                val tmpString = tds[3].text().replace("\u00a0".toRegex(), "").trim { it <= ' ' }
                stockInfo.changeValue = tmpString.substring(0,tmpString.indexOf("/") )
                stockInfo.changePercent = tmpString.substring(tmpString.indexOf("/") + 1)

                stockInfo.volume = tds[4].text().replace("\u00a0".toRegex(), "").trim { it <= ' ' }
                stockInfo.value = tds[5].text().replace("\u00a0".toRegex(), "").replace("$","").trim { it <= ' ' }
                stockInfo.capitalisation =
                    tds[6].text().replace("\u00a0".toRegex(), "").replace("$","").trim { it <= ' ' }
                stockInfo.tradeCount =
                    tds[10].text().replace("\u00a0".toRegex(), "").trim { it <= ' ' }

                stockInfo.infoTime = currentTime

                stockInfoList.add(stockInfo)
            }

            return stockInfoList
        }
    }
}

class StockInfo {
    var stockCode = ""
    var companyName = ""
    var price = ""
    var changeValue = ""
    var changePercent = ""
    var volume = ""
    var value = ""
    var capitalisation =""
    var tradeCount = ""
    var infoTime = ""
}

class StockCurrentTradeInfo{
    var stockCode = ""
    var companyName = ""
    var price = ""
    var change = ""
    var sVWAP = ""
    var sBuy = ""
    var sSell = ""
    var sHigh =""
    var sLow = ""
    var sFirst = ""
    var volume = ""
    var value = ""
    var infoTime = ""
    var pictLink = ""

    fun copyFromStokInfo(stockInfo :StockInfo){
        this.stockCode = stockInfo.stockCode
        this.companyName = stockInfo.companyName

        if(stockInfo.price.indexOf(".") > 0 && stockInfo.price.length > 0) {
            val sTemp = (stockInfo.price.replace(",", "").toFloat() * 100).toString()
            if(sTemp.indexOf(".") > 0) {
                this.price = sTemp.substring(0, sTemp.indexOf("."))
                if(sTemp.toFloat() - this.price.toFloat() > 0f )
                    if( sTemp.indexOf(".") + 3 > sTemp.length )
                        this.price = sTemp
                    else
                        this.price = sTemp.substring(0, sTemp.indexOf(".")+3)
            }
            else
                this.price = sTemp
        }
        else
            this.price = stockInfo.price

        this.change = stockInfo.changePercent
        this.volume = stockInfo.volume

        if(stockInfo.value.indexOf(".") > 0  )
            this.value = stockInfo.value.substring(0,stockInfo.value.indexOf("."))
        else
            this.value = stockInfo.value

        this.infoTime = stockInfo.infoTime
    }
}

class StockScreenInfo {
    var stockCode = ""
    var companyName = ""
    var changeValue = ""
    var changePercent = ""
    var price = ""
    var value = ""
    var volume = ""
    var marketCap = ""
    var tradeNumber = ""
    var infoTime = ""

    /*
    <tr class="dgitTR" onmouseover="Highlight(this);" onmouseout="UnHighlight(this);">
        <td nowrap="nowrap" class="dgitfirstcolumn">FISHERHEALTH</td>
        <td><a href='../dynamic/quote.aspx?qqsc=FPH&amp;qqe=NZSE'>FPH.NZ</a></td>
        <td align="Right">75</td>
        <TD><img src="../images/spacer.gif" alt="Down" class="downarrow" width="15" height="10"></TD>
        <TD>2.5%</TD>
        <td align="Right"><b>2905</b></td>
        <td align="Right">$12,531,117</td>
        <td align="Right">429,727</td>
        <td align="Right">16.69B</td>
        <td align="Right" class="dgitlastcolumn">3,214</td>
    </tr>
     */

    fun copyFromStokInfo(stockInfo :StockInfo){
        this.stockCode = stockInfo.stockCode
        this.companyName = stockInfo.companyName

        if(stockInfo.price.indexOf(".") > 0 && stockInfo.price.length > 0) {
            val sTemp = (stockInfo.price.replace(",", "").toFloat() * 100).toString()
            if(sTemp.indexOf(".") > 0) {
                this.price = sTemp.substring(0, sTemp.indexOf("."))
                if(sTemp.toFloat() - this.price.toFloat() > 0f )
                    if( sTemp.indexOf(".") + 3 > sTemp.length )
                        this.price = sTemp
                    else
                        this.price = sTemp.substring(0, sTemp.indexOf(".")+3)
            }
            else
                this.price = sTemp
        }
        else
            this.price = stockInfo.price

        this.changeValue = stockInfo.changeValue
        this.changePercent = stockInfo.changePercent
        this.volume = stockInfo.volume

        if(stockInfo.value.indexOf(".") > 0  )
            this.value = stockInfo.value.substring(0,stockInfo.value.indexOf("."))
        else
            this.value = stockInfo.value

        this.marketCap = stockInfo.capitalisation
        this.tradeNumber = stockInfo.tradeCount
        this.infoTime = stockInfo.infoTime
    }

    companion object {


        fun convertScreenInfoListToStockCurrentTradeInfoList(stockScreenInfoList: List<StockScreenInfo>): List<StockCurrentTradeInfo> {

            val stockCurrentTradeInfoList = mutableListOf<StockCurrentTradeInfo>()
            stockScreenInfoList.forEach { stockScreenInfo ->
                val stockCurrentTradeInfo =
                    StockCurrentTradeInfo()
                stockCurrentTradeInfo.stockCode = stockScreenInfo.stockCode
                stockCurrentTradeInfo.companyName = stockScreenInfo.companyName
                stockCurrentTradeInfo.change = stockScreenInfo.changePercent
                stockCurrentTradeInfo.price = stockScreenInfo.price
                stockCurrentTradeInfo.value = stockScreenInfo.value
                stockCurrentTradeInfo.volume = stockScreenInfo.volume
                stockCurrentTradeInfo.infoTime = stockScreenInfo.infoTime
                stockCurrentTradeInfoList.add(stockCurrentTradeInfo)
            }
            return stockCurrentTradeInfoList
        }

        fun getStockScreenInfoFromString(sIn: String): List<StockScreenInfo> {
            val stockScreenInfoList = mutableListOf<StockScreenInfo>()

            val document = Jsoup.parse(sIn)
            val tableRows = document.select("table[class=dgtbl] tr")

            // The first two rows contain unwanted information
//            tableRows.removeAt(0)
//            tableRows.removeAt(1)
//            tableRows.removeAt(2)

            for (i in 3..tableRows.size-1) {
                val row = tableRows[i]
                val stockScreenInfo =
                    StockScreenInfo()
                val tds = row.getElementsByTag("td")
                println("DEBUG: " + tds.size)
                if (tds.size < 8) continue

                stockScreenInfo.companyName =
                    tds.first().text().replace("\u00a0".toRegex(), "").trim { it <= ' ' }
                stockScreenInfo.stockCode =
                    tds[1].text().replace("\u00a0".toRegex(), "").replace(".NZ", "").trim { it <= ' ' }
                stockScreenInfo.changeValue =
                    tds[2].text().replace("\u00a0".toRegex(), "").trim { it <= ' ' }
                val arrow = tds[3].html().indexOf("downarrow")
                stockScreenInfo.changePercent =
                    tds[4].text().replace("\u00a0".toRegex(), "").trim { it <= ' ' }
                if (arrow > 0) stockScreenInfo.changePercent = "-" + stockScreenInfo.changePercent
                stockScreenInfo.price =
                    tds[5].text().replace("\u00a0".toRegex(), "").trim { it <= ' ' }
                stockScreenInfo.value =
                    tds[6].text().replace("\u00a0".toRegex(), "").trim { it <= ' ' }
                stockScreenInfo.volume =
                    tds[7].text().replace("\u00a0".toRegex(), "").trim { it <= ' ' }
                stockScreenInfo.marketCap =
                    tds[8].text().replace("\u00a0".toRegex(), "").trim { it <= ' ' }
                stockScreenInfo.tradeNumber =
                    tds[9].text().replace("\u00a0".toRegex(), "").trim { it <= ' ' }

                stockScreenInfoList.add(stockScreenInfo)
            }

            return stockScreenInfoList
        }
    }
}


@Entity(
        tableName = "TradeLog"
)
data class TradeLog(
        @PrimaryKey(autoGenerate = true)
        var id : Int,
        var stockCode:String = "",
        var tradeVolume : String = "",
        var tradeTime : String = "", // need to be patched with date.
        var price : String = "",
        var tradeCondition: String = ""
)


@Entity(
        tableName = "StockDailyLog"
)
data class StockDailyLog(
        @PrimaryKey(autoGenerate = true)
        val id : Int,
        val stockCode:String,
        val openPrice : String,
        val closePrice : String,
        val highPrice : String,
        val lowPrice : String,
        val change: String,
        var volume: String,
        var value : String,
        var date : Date
)

@Entity(
        tableName = "AskBidLog"
)
data class AskBidLog(
    @PrimaryKey(autoGenerate = true)
        var id : Int,
    val stockCode:String,

    val askList : List<AsksRow>,
    val bidList : List<BidsRow>,
    val tradeList : List<TradesRow>,
    var tradeTime : String // need to be patched with date.


)

class DataConverter {
    @TypeConverter
    fun fromBidList(bidList: List<BidsRow>?): String? {
        if (bidList == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<BidsRow>?>() {}.getType()
        return gson.toJson(bidList, type)
    }

    @TypeConverter
    fun toBidList(bidListString: String?): List<BidsRow>? {
        if (bidListString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken< List<BidsRow>?>() {}.getType()
        return gson.fromJson(bidListString, type)
    }
    @TypeConverter
    fun fromAskList(askList: List<AsksRow>?): String? {
        if (askList == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<AsksRow>>() {}.getType()
        return gson.toJson(askList, type)
    }

    @TypeConverter
    fun toAskList(askListString: String?): List<AsksRow>? {
        if (askListString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken< List<AsksRow>?>() {}.getType()
        return gson.fromJson(askListString, type)
    }

    @TypeConverter
    fun fromTradeList(tradeList: List<TradesRow>?): String? {
        if (tradeList == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<TradesRow>>() {}.getType()
        return gson.toJson(tradeList, type)
    }

    @TypeConverter
    fun toTradeList(tradeListString: String?): List<TradesRow>? {
        if (tradeListString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken< List<TradesRow>?>() {}.getType()
        return gson.fromJson(tradeListString, type)
    }
}
