package com.teempton.DDSKot.utils

import android.content.Context
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList


object cityHelper {
    fun getAllyCoutries(context: Context):ArrayList<String>{
        var tempArray = ArrayList<String>()
        try {
            val inputStream:InputStream = context.assets.open("countriesToCities.json")
            val size:Int = inputStream.available()
            val bytesArray = ByteArray(size)
            inputStream.read(bytesArray)
            val jsonFile=String(bytesArray)
            val jsonObject=JSONObject(jsonFile)
            val countryesNames = jsonObject.names()
            if (countryesNames != null){
                for (n in 0 until countryesNames.length())
                    tempArray.add(countryesNames.getString(n))
            }
        }catch (e:IOException){

        }
        return tempArray
    }

    fun getAllyCities(country:String, context: Context):ArrayList<String>{
        var tempArray = ArrayList<String>()
        try {
            //открываем поток в памяти и загружаем туда файл
            val inputStream:InputStream = context.assets.open("countriesToCities.json")
            val size:Int = inputStream.available()
            val bytesArray = ByteArray(size)
            inputStream.read(bytesArray)
            val jsonFile=String(bytesArray)
            val jsonObject=JSONObject(jsonFile)
            val CityesNames = jsonObject.getJSONArray(country)//берем все массив в json

                for (n in 0 until CityesNames.length())
                    tempArray.add(CityesNames.getString(n))

        }catch (e:IOException){

        }
        return tempArray
    }

    fun filtrListData(list:ArrayList<String>, searchText:String? )  :ArrayList<String> {
        val tempList = ArrayList<String>()
        tempList.clear()
        if (searchText==null){
            tempList.add("Нет результата")
            return tempList
        }
        for (selection: String in list) {
            //приводим все к прописной строке и сравниваем что ввел пользователь и начинается с таких то букв, передаем все по буквам
            if (selection.lowercase(Locale.ROOT).startsWith(searchText.lowercase(Locale.ROOT))) {
                tempList.add(selection)
            }
        }
    if (tempList.size == 0)tempList.add("Нет результата")
        return tempList
    }
}