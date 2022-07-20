package com.teempton.DDSKot.act

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.teempton.DDSKot.R
import com.teempton.DDSKot.databinding.ActivityFilterBinding
import com.teempton.DDSKot.dialogs.DialogSpinnerHelper
import com.teempton.DDSKot.utils.cityHelper

class FilterActivity : AppCompatActivity() {
    lateinit var binding: ActivityFilterBinding
    private val dialog = DialogSpinnerHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        actionBarSettings()
        onClickSelectCountry()
        onClickSelectCity()
        onClickDone()
        onClickClear()
        getFilter()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }

    //onClicks
    private fun onClickSelectCountry() = with(binding) {
        tvCountry.setOnClickListener {
            val listCountry = cityHelper.getAllyCoutries(this@FilterActivity)
            dialog.showSpinnerDialog(this@FilterActivity, listCountry, tvCountry)
            if (tvCity.text.toString() != getString(R.string.selrct_city)) {
                tvCity.text = getString(R.string.selrct_city)
            }
        }

    }

    private fun onClickSelectCity() = with(binding) {
        tvCity.setOnClickListener {
            val selectedCountry = tvCountry.text.toString()
            if (selectedCountry != getString(R.string.selrct_country)) {
                val listCity = cityHelper.getAllyCities(selectedCountry, this@FilterActivity)
                dialog.showSpinnerDialog(this@FilterActivity, listCity, tvCity)
            } else {
                Toast.makeText(this@FilterActivity, "Не выбрана страна", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun getFilter() = with(binding){
        val filter = intent.getStringExtra(FILTER_KEY)
        if (filter != null && filter != "empty" && filter!=""){
            val filterArray = filter.split("_")
            Log.d("MyLog", "size ${filterArray.size} внутри ${filter}")
            if (filterArray[0]!="empty")tvCountry.text = filterArray[0]
            if (filterArray[1]!="empty")tvCity.text = filterArray[1]
            if (filterArray[2]!="empty")editIndex.setText(filterArray[2])
            checkBoxWithSend.isChecked = filterArray[3].toBoolean()
        }
    }

    private fun onClickDone() = with(binding) {
        btDone.setOnClickListener {
            val i = Intent().apply {
                putExtra(FILTER_KEY,createFilter())
            }
            setResult(RESULT_OK,i)
            finish()
        }
    }

    private fun onClickClear() = with(binding) {
        btClear.setOnClickListener {
            tvCountry.text = getString(R.string.selrct_country)
            tvCity.text = getString(R.string.selrct_city)
            editIndex.setText("")
            checkBoxWithSend.isChecked = false
            setResult(RESULT_CANCELED)
        }
    }


    private fun createFilter(): String = with(binding) {
        val sBuilder = StringBuilder()
        val arrayTempFilter = listOf(
            tvCountry.text,
            tvCity.text,
            editIndex.text,
            checkBoxWithSend.isChecked.toString()
        )
        for ((index, s) in arrayTempFilter.withIndex()) {
            if (s != getString(R.string.selrct_country) && s != getString(R.string.selrct_city) && s.isNotEmpty()) {
                sBuilder.append(s)
                if (index!=arrayTempFilter.size-1)sBuilder.append("_")
            }else {
                sBuilder.append("empty")
                if (index!=arrayTempFilter.size-1)sBuilder.append("_")
            }
        }
        Log.d("MyLog",sBuilder.toString())
        return sBuilder.toString()
    }

    fun actionBarSettings() {
        val ab = supportActionBar
        ab?.setDisplayHomeAsUpEnabled(true)
    }

    companion object{
        const val FILTER_KEY="filter_key"
    }
}