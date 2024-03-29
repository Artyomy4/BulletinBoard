package com.teempton.DDSKot.act

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.tasks.OnCompleteListener
import com.teempton.DDSKot.MainActivity
import com.teempton.DDSKot.R
import com.teempton.DDSKot.adapters.ImageAdapter
import com.teempton.DDSKot.model.Ad
import com.teempton.DDSKot.model.DbManager
import com.teempton.DDSKot.databinding.ActivityEditAdsBinding
import com.teempton.DDSKot.dialogs.DialogSpinnerHelper
import com.teempton.DDSKot.frag.FragmentCloseInterface
import com.teempton.DDSKot.frag.ImageListFrag
import com.teempton.DDSKot.utils.ImageManager
import com.teempton.DDSKot.utils.ImagePicker
import com.teempton.DDSKot.utils.cityHelper
import java.io.ByteArrayOutputStream


class EditAdsAct : AppCompatActivity(), FragmentCloseInterface {
    var chooseImageFrag: ImageListFrag? = null
    lateinit var binding: ActivityEditAdsBinding
    private val dialog = DialogSpinnerHelper()
    lateinit var imageAdapter: ImageAdapter
    private val dbManager = DbManager()
    var editImagePos = 0
    var imageIndex = 0
    private var isEditState = false
    private var ad: Ad? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAdsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        checkEditState()
        imageChangeCounter()
    }

    private fun checkEditState() {
        isEditState = isEditState()
        if (isEditState) {
            ad = intent.getSerializableExtra(MainActivity.ADS_DATA) as Ad
            if (ad != null) fillViews(ad!!)
        }
    }

    private fun isEditState(): Boolean {
        return intent.getBooleanExtra(MainActivity.EDIT_STATE, false)
    }

    private fun fillViews(ad: Ad) = with(binding) {
        tvCountry.text = ad.country
        tvCity.text = ad.city
        editTel.setText(ad.tel)
        editIndex.setText(ad.index)
        checkBoxWithSend.isChecked = ad.withSend.toBoolean()
        tvCat.text = ad.category
        edTitle.setText(ad.title)
        edPrice.setText(ad.price)
        edDescription.setText(ad.description)
        updateImageCounter(0)
        ImageManager.fillImageArray(ad, imageAdapter)
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        ImagePicker.showSelectedImages(resultCode, requestCode, data, this)
//    }

    override fun onFragClose(list: ArrayList<Bitmap>) {
        binding.ScrollViewMain.visibility = View.VISIBLE
        imageAdapter.update(list)
        chooseImageFrag = null
        updateImageCounter(binding.vpimages.currentItem)
    }

    fun openChooseImageFrag(newList: ArrayList<Uri>?) {
        chooseImageFrag = ImageListFrag(this)
        if (newList != null) chooseImageFrag?.resizeSelectedImages(newList, true, this)
        binding.ScrollViewMain.visibility = View.GONE
        val fm = supportFragmentManager.beginTransaction()
        //заменяем контейнер который создали на наш фрагмент
        fm.replace(R.id.place_holder, chooseImageFrag!!)
        fm.commit()
    }


    private fun init() {
        imageAdapter = ImageAdapter()
        binding.vpimages.adapter = imageAdapter
    }

    //onClicks
    fun onClickSelectCountry(view: View) {
        val listCountry = cityHelper.getAllyCoutries(this)
        dialog.showSpinnerDialog(this, listCountry, binding.tvCountry)
        if (binding.tvCity.text.toString() != getString(R.string.selrct_city)) {
            binding.tvCity.text = getString(R.string.selrct_city)
        }
    }

    fun onClickSelectCity(view: View) {
        val selectedCountry = binding.tvCountry.text.toString()
        if (selectedCountry != getString(R.string.selrct_country)) {
            val listCity = cityHelper.getAllyCities(selectedCountry, this)
            dialog.showSpinnerDialog(this, listCity, binding.tvCity)
        } else {
            Toast.makeText(this, "Не выбрана страна", Toast.LENGTH_LONG).show()
        }
    }

    fun onClickSelectCat(view: View) {

        val listCat = resources.getStringArray(R.array.category).toMutableList() as ArrayList
        dialog.showSpinnerDialog(this, listCat, binding.tvCat)

    }

    //    fun onClickGetImage(view:View){
//        ImagePicker.getImages(this)
//    }
//кнопка карандаш
    fun onClickGetImage(view: View) {
        if (imageAdapter.mainArray.size < 1) {
            ImagePicker.getMultiImages(this, 3)
        } else {
            openChooseImageFrag(null)
            chooseImageFrag?.updateAdapterFromEdit(imageAdapter.mainArray)
        }
    }
    //слушатель клика коноаки опубликовать
    fun onClickPublish(view: View) {
        if (isFieldEmpty()){
            showToast("Внимание!Все поля должны быть заполнены!")
            return
        }
        binding.progressLayout.visibility = View.VISIBLE
        ad = fillAd()
        uploadImages()
    }

    private fun isFieldEmpty():Boolean = with(binding){
        return tvCountry.text.toString() == getString(R.string.selrct_country)
                ||tvCity.text.toString() == getString(R.string.selrct_city)
                ||tvCat.text.toString() == getString(R.string.select_category)
                ||editIndex.text.isEmpty()
                ||editTel.text.isEmpty()
                ||edDescription.text.isEmpty()
                ||tvTitle.text.isEmpty()
                ||edPrice.text.isEmpty()
    }

//окончание публикации объявления
    private fun onPublishFinish(): DbManager.FinishWorkListener {
        return object : DbManager.FinishWorkListener {
            override fun onFinish(isDone:Boolean) {
                binding.progressLayout.visibility = View.GONE
                if (isDone)finish()
            }

        }
    }

    private fun fillAd(): Ad {
        val adTemp: Ad
        binding.apply {
            adTemp = Ad(
                tvCountry.text.toString(), tvCity.text.toString(),
                editTel.text.toString(), editIndex.text.toString(),
                checkBoxWithSend.isChecked.toString(), tvCat.text.toString(),
                edTitle.text.toString(),
                edPrice.text.toString(),
                edDescription.text.toString(),
                editEmail.text.toString(),
                ad?.mainImage ?: "empty",
                ad?.image2 ?: "empty",
                ad?.image3 ?: "empty",
                ad?.key ?: dbManager.db.push().key,//генерация специального ключа
                "0",
                dbManager.auth.uid,
                System.currentTimeMillis().toString()
            )
        }
        return adTemp
    }

    private fun uploadImages() {
        if (imageIndex == 3) {
            dbManager.publishAd(ad!!, onPublishFinish())
            return
        }
        val oldUrl = getUrlFromAd()
        if (imageAdapter.mainArray.size > imageIndex) {
            val byteArray = prepareImageByteArray(imageAdapter.mainArray[imageIndex])
            if (oldUrl.startsWith("http")){
                updateImage(byteArray, oldUrl){
                    nextImage(it.result.toString())
                }
            } else {
            uploadImage(byteArray) {
//            dbManager.publishAd(ad!!, onPublishFinish())
                nextImage(it.result.toString())
            }
            }
        } else {
            if (oldUrl.startsWith("http")) {
                deleteImageByUrl(oldUrl) {
                    nextImage("empty")
                }
            } else {
                nextImage("empty")
            }
        }
    }

    private fun setImageUriToAd(uri: String) {
        when (imageIndex) {
            0 -> ad = ad?.copy(mainImage = uri)
            1 -> ad = ad?.copy(image2 = uri)
            3 -> ad = ad?.copy(image3 = uri)
        }

    }

    private fun nextImage(uri: String) {
        setImageUriToAd(uri)
        imageIndex++
        uploadImages()
    }

    private fun getUrlFromAd(): String {
        return listOf(ad?.mainImage!!, ad?.image2!!, ad?.image3!!)[imageIndex]
    }

    private fun prepareImageByteArray(bitmap: Bitmap): ByteArray {
        val outStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, outStream)
        return outStream.toByteArray()
    }

    private fun uploadImage(byteArray: ByteArray, listener: OnCompleteListener<Uri>) {
        val imStorageRef = dbManager.dbStorege
            .child(dbManager.auth.uid!!)
            .child("image_${System.currentTimeMillis()}")
        val upTask = imStorageRef.putBytes(byteArray)
        upTask.continueWithTask { task ->
            imStorageRef.downloadUrl
        }.addOnCompleteListener(listener)
    }

    private fun deleteImageByUrl(oldUrl: String, listener: OnCompleteListener<Void>) {
        dbManager.dbStorege.storage.getReferenceFromUrl(oldUrl)
            .delete().addOnCompleteListener(listener)

    }

    private fun updateImage(byteArray: ByteArray, url: String, listener: OnCompleteListener<Uri>) {
        val imStorageRef = dbManager.dbStorege.storage.getReferenceFromUrl(url)
        val upTask = imStorageRef.putBytes(byteArray)
        upTask.continueWithTask { task ->
            imStorageRef.downloadUrl
        }.addOnCompleteListener(listener)
    }

    private fun imageChangeCounter() {
        binding.vpimages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateImageCounter(position)
            }
        })
    }

    private fun updateImageCounter(counter:Int){
        var i = 1
        val itemCounter = binding.vpimages.adapter?.itemCount
        if (itemCounter == 0) i = 0
        val imageCounter = "${counter + i}/$itemCounter"
        binding.tvImageCounter.text = imageCounter
    }

}

