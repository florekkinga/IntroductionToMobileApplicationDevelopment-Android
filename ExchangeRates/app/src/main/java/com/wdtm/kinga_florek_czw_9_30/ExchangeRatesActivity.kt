package com.wdtm.kinga_florek_czw_9_30

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import org.json.JSONArray


class ExchangeRatesActivity : AppCompatActivity() {
    private lateinit var currenciesListRecyclerView: RecyclerView
    private lateinit var adapter: CurrenciesListAdapter
    private var dataSet: Array<CurrencyDetails> = arrayOf<CurrencyDetails>()
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exchage_rates)
        currenciesListRecyclerView = findViewById(R.id.currenciesListRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        currenciesListRecyclerView.layoutManager = LinearLayoutManager(this)
        val tmpData = arrayOf(CurrencyDetails("EUR", 4.56), CurrencyDetails("CHF", 4.55))
        adapter = CurrenciesListAdapter(tmpData, this)
        currenciesListRecyclerView.adapter = adapter
        makeRequest()
    }

    private fun makeRequest() {
        val queue = DataHolder.queue
        val url = "http://api.nbp.pl/api/exchangerates/tables/A/last/2?format=json"

        val currenciesListRequest = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                println("SUCCESS!")
                currenciesListRecyclerView.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                loadData(response)
            },
            {
                currenciesListRecyclerView.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
                Toast.makeText(this, "Brak internetu. Proszę włącz internet i zrestartuj aplikację", Toast.LENGTH_LONG).show();
                println("ERROR!!!!")
            })
        val urlB = "http://api.nbp.pl/api/exchangerates/tables/B/last/2?format=json"
        val currenciesListRequestB = JsonArrayRequest(Request.Method.GET, urlB, null,
            { response ->
                println("SUCCESS!")
                currenciesListRecyclerView.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                loadData(response, false)
                adapter.dataSet = dataSet
                adapter.notifyDataSetChanged()
            },
            {
                currenciesListRecyclerView.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
                Toast.makeText(this, "Brak internetu. Proszę włącz internet i zrestartuj aplikację", Toast.LENGTH_LONG).show();
                println("ERROR!!!!")
            })
        queue.add(currenciesListRequest)
        queue.add(currenciesListRequestB)
    }

    private fun loadData(response: JSONArray?, a: Boolean = true) {
        response?.let {
            val rates = response.getJSONObject(1).getJSONArray("rates")
            val previousRates = response.getJSONObject(0).getJSONArray("rates")
            val ratesCount = rates.length()
            val tmpData = arrayOfNulls<CurrencyDetails>(ratesCount)

            for (i in 0 until ratesCount) {
                val currencyCode = rates.getJSONObject(i).getString("code")
                val currentRate = rates.getJSONObject(i).getDouble("mid")
                val previousRate = previousRates.getJSONObject(i).getDouble("mid")
                val flag = DataHolder.getFlagForCurrency(currencyCode)
                val isGrowing = DataHolder.getArrowForGrowing(previousRate < currentRate)
                val currencyObject = CurrencyDetails(currencyCode, currentRate, flag, isGrowing, a)

                tmpData[i] = currencyObject
            }

            this.dataSet += tmpData as Array<CurrencyDetails>
            this.dataSet.distinctBy { it.currencyCode }
        }
    }

}