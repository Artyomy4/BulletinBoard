package com.teempton.DDSKot.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.teempton.DDSKot.model.Ad
import com.teempton.DDSKot.model.DbManager

class FirebaseViewModel:ViewModel() {
    private val dbManager = DbManager()
    val liveAdsData = MutableLiveData<ArrayList<Ad>>()

    fun loadAllAdsFirstPage(filter:String){
        dbManager.getAllAdsFirstPage(filter, object : DbManager.ReadDataCallBack {
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }

    fun loadAllAdsNextPage(time:String, filter: String){
        dbManager.getAllAdsNextPage(time,filter, object : DbManager.ReadDataCallBack {
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }

    fun loadAllAdsFromCat(cat:String, filter: String){
        dbManager.getAllAdsFromCatFirstPage(cat, filter,object : DbManager.ReadDataCallBack {
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }

    fun loadAllAdsFromCatNextPage(cat:String,time:String, filter: String){
        dbManager.getAllAdsFromCatNextPage(cat,time, filter, object : DbManager.ReadDataCallBack {
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }


    fun onFavClick(ad: Ad){
        dbManager.onFavClick(ad, object: DbManager.FinishWorkListener{
            override fun onFinish() {
                val updatedList = liveAdsData.value
                val pos = updatedList?.indexOf(ad)
                if (pos != -1){
                    //добавляем в копию противоложное значение
                    pos?.let {
                        val favCounter = if (ad.isFav) ad.favCounter.toInt() - 1 else ad.favCounter.toInt() + 1
                        updatedList[pos] = updatedList[pos].copy(isFav = !ad.isFav, favCounter = favCounter.toString())
                    }
                }
                liveAdsData.postValue(updatedList)
            }

        })
    }

    fun adViewed(ad: Ad){
        dbManager.adViewed(ad)
    }


    fun loadMyAds(){
        dbManager.getMyAds(object : DbManager.ReadDataCallBack {
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }

    fun loadMyFavs(){
        dbManager.getMyAds(object : DbManager.ReadDataCallBack {
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }

    fun deleteItem(ad: Ad){
        dbManager.deleteAd(ad, object: DbManager.FinishWorkListener{
            override fun onFinish() {
                val updatedList = liveAdsData.value
                updatedList?.remove(ad)
                liveAdsData.postValue(updatedList)
            }

        })
    }
}