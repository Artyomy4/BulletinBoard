package com.teempton.DDSKot.model

import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.teempton.DDSKot.utils.FilterManager

class DbManager {
    val db = Firebase.database.getReference(MAIN_NODE)
    val dbStorege = Firebase.storage.getReference(MAIN_NODE)
    val auth = Firebase.auth

    fun publishAd(ad: Ad, finishWorkListener: FinishWorkListener) {
        if (auth.uid != null) db.child(ad.key ?: "empty")
            .child(auth.uid!!).child(AD_NODE)
            .setValue(ad).addOnCompleteListener {
                val adFilter = FilterManager.createFilter(ad)
                db.child(ad.key ?: "empty").child(FILTER_NODE)
                    .setValue(adFilter).addOnCompleteListener {
                        finishWorkListener.onFinish()
                    }
            }
    }

    fun adViewed(ad: Ad) {
        var counter = ad.viewsCounter.toInt()
        counter++
        if (auth.uid != null) db.child(ad.key ?: "empty")
            .child(INFO_NODE)
            .setValue(InfoItem(counter.toString(), ad.emailCounter, ad.callsCounter))
    }

    fun onFavClick(ad: Ad, listener: FinishWorkListener) {
        if (ad.isFav) removeFromFavs(ad, listener)
        else addToFavs(ad, listener)
    }

    private fun addToFavs(ad: Ad, listener: FinishWorkListener) {
        ad.key?.let {
            auth.uid?.let { uid ->
                db.child(it).child(FAVS_NODE).child(uid).setValue(uid).addOnCompleteListener {
                    if (it.isSuccessful) listener.onFinish()
                }
            }
        }
    }

    private fun removeFromFavs(ad: Ad, listener: FinishWorkListener) {
        ad.key?.let {
            auth.uid?.let { uid ->
                db.child(it).child(FAVS_NODE).child(uid).removeValue().addOnCompleteListener {
                    if (it.isSuccessful) listener.onFinish()
                }
            }
        }
    }

    fun getMyAds(readDataCallBack: ReadDataCallBack?) {
        val query = db.orderByChild("/favs/${auth.uid}").equalTo(auth.uid)
        readDataFromDb(query, readDataCallBack)
    }

    fun getMyFavs(readDataCallBack: ReadDataCallBack?) {
        val query = db.orderByChild(auth.uid + "/ad/uid").equalTo(auth.uid)
        readDataFromDb(query, readDataCallBack)
    }

    fun getAllAdsFirstPage(filter: String,readDataCallBack: ReadDataCallBack?) {
        val query = if(filter.isEmpty()){
            db.orderByChild(PATH_ADFILTER_TIME).limitToLast(ADS_LIMIT)
        } else {
            getAllAdsByFilterFirstPage(filter)
        }
        readDataFromDb(query, readDataCallBack)
    }

    fun getAllAdsByFilterFirstPage(tmpfilter: String):Query {
        val orderBy = tmpfilter.split("|")[0]
        val filter = tmpfilter.split("|")[1]
        return db.orderByChild("/adFilter/${orderBy}")
            .startAt(filter).endAt(filter+"_\uf8ff").limitToLast(ADS_LIMIT)
    }



    fun getAllAdsFromCatFirstPage(cat: String, filter: String, readDataCallBack: ReadDataCallBack?) {
        val query = if (filter.isEmpty()) {db.orderByChild(PATH_ADFILTER_CATTIME)
            .startAt(cat).endAt(cat+"_\uf8ff").limitToLast(ADS_LIMIT)
        }else{
            getAllAdsFromCatByFilterFirstPage(cat,filter)
        }
        readDataFromDb(query, readDataCallBack)
    }


    fun getAllAdsFromCatByFilterFirstPage(cat: String,tmpfilter: String ):Query {
        val orderBy = "cat_" + tmpfilter.split("|")[0]
        val filter = cat + "_" + tmpfilter.split("|")[1]
        return db.orderByChild("/adFilter/${orderBy}")
            .startAt(filter).endAt(filter+"_\uf8ff").limitToLast(ADS_LIMIT)
    }

    fun getAllAdsFromCatNextPage(cat: String, time: String, filter: String, readDataCallBack: ReadDataCallBack?) {
        if (filter.isEmpty()){
            val query = db.orderByChild(PATH_ADFILTER_CATTIME)
                .endBefore(cat + time).limitToLast(ADS_LIMIT)
            readDataFromDb(query, readDataCallBack)
        } else {
            getAllAdsFromCatByFilterNextPage(cat,time,filter,readDataCallBack)
        }

    }

    fun getAllAdsFromCatByFilterNextPage(cat: String, time: String,tmpfilter: String, readDataCallBack: ReadDataCallBack? ) {
        val orderBy = "cat_" + tmpfilter.split("|")[0]
        val filter = cat + "_" + tmpfilter.split("|")[1]
        val query = db.orderByChild("/adFilter/$orderBy")
            .endBefore(filter + "_" + time).limitToLast(ADS_LIMIT).limitToLast(ADS_LIMIT)
        readNextPageFromDb(query,filter, orderBy, readDataCallBack)
    }

    fun getAllAdsNextPage(time:String, filter: String, readDataCallBack: ReadDataCallBack?) {
        if (filter.isEmpty()) {
            val query = db.orderByChild(PATH_ADFILTER_TIME).endBefore(time).limitToLast(ADS_LIMIT)
            readDataFromDb(query, readDataCallBack)
        }else{
            getAllAdsByFilterNextPage(filter,time,readDataCallBack)
        }
    }

    private fun getAllAdsByFilterNextPage(tmpfilter: String, time: String, readDataCallBack: ReadDataCallBack? ) {
        val orderBy = tmpfilter.split("|")[0]
        val filter = tmpfilter.split("|")[1]
        val query = db.orderByChild("/adFilter/${orderBy}")
            .endBefore(filter + "_$time" ).limitToLast(ADS_LIMIT)
        readNextPageFromDb(query,filter,orderBy,readDataCallBack)
    }

    fun deleteAd(ad: Ad, listener: FinishWorkListener) {
        if (ad.key == null || ad.uid == null) return
        db.child(ad.key).child(ad.uid).removeValue().addOnCompleteListener {
            if (it.isSuccessful) listener.onFinish()
        }
    }

    private fun readDataFromDb(query: Query, readDataCallBack: ReadDataCallBack?) {
        query.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val adArray = ArrayList<Ad>()
                for (item in snapshot.children) {
                    var ad: Ad? = null
                    item.children.forEach {
                        if (ad == null) ad = it.child(AD_NODE).getValue(Ad::class.java)
                    }
                    val infoItem = item.child(INFO_NODE).getValue(InfoItem::class.java)
                    val favCounter = item.child(FAVS_NODE).childrenCount
                    val isFav = auth.uid?.let {
                        item.child(FAVS_NODE).child(it).getValue(String::class.java)
                    }
                    ad?.isFav = isFav != null
                    ad?.favCounter = favCounter.toString()
                    ad?.viewsCounter = infoItem?.viewsCounter ?: "0"
                    ad?.emailCounter = infoItem?.emailsCounter ?: "0"
                    ad?.callsCounter = infoItem?.callsCounter ?: "0"
                    if (ad != null) adArray.add(ad!!)
                }

                readDataCallBack?.readData(adArray)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun readNextPageFromDb(query: Query, filter: String, orderBy:String, readDataCallBack: ReadDataCallBack?) {
        query.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val adArray = ArrayList<Ad>()
                for (item in snapshot.children) {
                    var ad: Ad? = null
                    item.children.forEach {
                        if (ad == null) ad = it.child(AD_NODE).getValue(Ad::class.java)
                    }
                    val infoItem = item.child(INFO_NODE).getValue(InfoItem::class.java)
                    val filterNodeValue = item.child(FILTER_NODE).child(orderBy).value.toString()

                    val favCounter = item.child(FAVS_NODE).childrenCount
                    val isFav = auth.uid?.let {
                        item.child(FAVS_NODE).child(it).getValue(String::class.java)
                    }
                    ad?.isFav = isFav != null
                    ad?.favCounter = favCounter.toString()
                    ad?.viewsCounter = infoItem?.viewsCounter ?: "0"
                    ad?.emailCounter = infoItem?.emailsCounter ?: "0"
                    ad?.callsCounter = infoItem?.callsCounter ?: "0"
                    if (ad != null && filterNodeValue.startsWith(filter)) adArray.add(ad!!)
                }

                readDataCallBack?.readData(adArray)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    interface ReadDataCallBack {
        fun readData(list: ArrayList<Ad>) {
        }
    }

    interface FinishWorkListener {
        fun onFinish()
    }

    companion object {
        const val AD_NODE = "ad"
        const val FILTER_NODE = "adFilter"
        const val INFO_NODE = "info"
        const val MAIN_NODE = "main"
        const val FAVS_NODE = "favs"
        const val ADS_LIMIT = 2
        const val PATH_ADFILTER_TIME = "/adFilter/time"
        const val PATH_ADFILTER_CATTIME = "/adFilter/cat_time"
    }
}


