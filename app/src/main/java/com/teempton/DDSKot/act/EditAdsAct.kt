package com.teempton.DDSKot.act

//import com.fxn.pix.Pix
//import com.fxn.utility.PermUtil
//import com.teempton.DDSKot.utils.ImagePicker

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.teempton.DDSKot.MainActivity
import com.teempton.DDSKot.R
import com.teempton.DDSKot.adapters.ImageAdapter
import com.teempton.DDSKot.model.Ad
import com.teempton.DDSKot.model.DbManager
import com.teempton.DDSKot.databinding.ActivityEditAdsBinding
import com.teempton.DDSKot.dialogs.DialogSpinnerHelper
import com.teempton.DDSKot.frag.FragmentCloseInterface
import com.teempton.DDSKot.frag.ImageListFrag
import com.teempton.DDSKot.utils.ImagePicker
import com.teempton.DDSKot.utils.cityHelper


class EditAdsAct : AppCompatActivity(), FragmentCloseInterface {
    var chooseImageFrag: ImageListFrag? = null
    lateinit var rootElement: ActivityEditAdsBinding
    private val dialog = DialogSpinnerHelper()
    lateinit var imageAdapter: ImageAdapter
    private val dbManager = DbManager()
    var launcherMultiSelectImage: ActivityResultLauncher<Intent>? = null
    var launcherSingleSelectImage: ActivityResultLauncher<Intent>? = null
    var editImagePos = 0
    private var isEditState = false
    private var ad:Ad? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootElement = ActivityEditAdsBinding.inflate(layoutInflater)
        setContentView(rootElement.root)
        init()
        checkEditState()
    }

    private fun checkEditState(){
        isEditState = isEditState()
        if (isEditState){
            ad = intent.getSerializableExtra(MainActivity.ADS_DATA) as Ad
            if (ad != null)fillViews(ad!!)
        }
    }

    private fun isEditState():Boolean{
        return intent.getBooleanExtra(MainActivity.EDIT_STATE,false)
    }

    private fun fillViews(ad: Ad)= with(rootElement){
        tvCountry.text = ad.country
        tvCity.text = ad.city
        editTel.setText(ad.tel)
        editIndex.setText(ad.index)
        checkBoxWithSend.isChecked = ad.withSend.toBoolean()
        tvCat.text = ad.cayrgory
        edTitle.setText(ad.title)
        edPrice.setText(ad.price)
        edDescription.setText(ad.description)

    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        ImagePicker.showSelectedImages(resultCode, requestCode, data, this)
//    }

    fun openChooseImageFrag(newList: ArrayList<String>?) {
        chooseImageFrag = ImageListFrag(this, newList)
        rootElement.ScrollViewMain.visibility = View.GONE
        val fm = supportFragmentManager.beginTransaction()
        //заменяем контейнер который создали на наш фрагмент
        fm.replace(R.id.place_holder, chooseImageFrag!!)
        fm.commit()
    }


    private fun init() {
        imageAdapter = ImageAdapter()
        rootElement.vpimages.adapter = imageAdapter
        launcherMultiSelectImage = ImagePicker.getLauncherForMultiSelectImages(this)
        launcherSingleSelectImage = ImagePicker.getLauncherForSinglImage(this)
    }

    //onClicks
    fun onClickSelectCountry(view: View) {
        val listCountry = cityHelper.getAllyCoutries(this)
        dialog.showSpinnerDialog(this, listCountry, rootElement.tvCountry)
        if (rootElement.tvCity.text.toString() != getString(R.string.selrct_city)) {
            rootElement.tvCity.text = getString(R.string.selrct_city)
        }
    }

    fun onClickSelectCity(view: View) {
        val selectedCountry = rootElement.tvCountry.text.toString()
        if (selectedCountry != getString(R.string.selrct_country)) {
            val listCity = cityHelper.getAllyCities(selectedCountry, this)
            dialog.showSpinnerDialog(this, listCity, rootElement.tvCity)
        } else {
            Toast.makeText(this, "Не выбрана страна", Toast.LENGTH_LONG).show()
        }
    }

    fun onClickSelectCat(view: View) {

        val listCat = resources.getStringArray(R.array.category).toMutableList() as ArrayList
            dialog.showSpinnerDialog(this, listCat, rootElement.tvCat)

    }

    //    fun onClickGetImage(view:View){
//        ImagePicker.getImages(this)
//    }
//кнопка карандаш
    fun onClickGetImage(view: View) {
        if (imageAdapter.mainArray.size < 1) {
            ImagePicker.launcher(this, launcherMultiSelectImage, 3)
        } else {
            openChooseImageFrag(null)
            chooseImageFrag?.updateAdapterFromEdit(imageAdapter.mainArray)
        }
    }

    fun onClickPublish(view: View){
        val adTemp = fillAd()
        if (isEditState) {
            dbManager.publishAd(adTemp.copy(key = ad?.key), onPublishFinish())
        }else{
            dbManager.publishAd(adTemp, onPublishFinish())
        }
    }

    private fun onPublishFinish(): DbManager.FinishWorkListener{
        return object: DbManager.FinishWorkListener{
            override fun onFinish() {
                finish()
            }

        }
    }

    private fun fillAd():Ad{
        val ad:Ad
        rootElement.apply {
            ad = Ad(tvCountry.text.toString(),tvCity.text.toString(),
                editTel.text.toString(),editIndex.text.toString(),
                checkBoxWithSend.isChecked.toString(),tvCat.text.toString(),
                edTitle.text.toString(),
                edPrice.text.toString(),edDescription.text.toString(),
                dbManager.db.push().key,//генерация специального ключа
                "0",
                dbManager.auth.uid)
        }
        return ad
    }

    override fun onFragClose(list: ArrayList<Bitmap>) {
        rootElement.ScrollViewMain.visibility = View.VISIBLE
        imageAdapter.update(list)
        chooseImageFrag = null
    }

}