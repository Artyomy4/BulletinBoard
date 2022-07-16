package com.teempton.DDSKot.frag

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.view.get
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.teempton.DDSKot.R
import com.teempton.DDSKot.act.EditAdsAct
import com.teempton.DDSKot.databinding.ListImageFragBinding
import com.teempton.DDSKot.dialoghelper.ProgressDialog
import com.teempton.DDSKot.utils.AdapterCallBack
import com.teempton.DDSKot.utils.ImageManager
import com.teempton.DDSKot.utils.ImagePicker
import com.teempton.DDSKot.utils.ItemTouchMoveCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ImageListFrag(
    private val fragCloseInterface: FragmentCloseInterface,
    private val newList: ArrayList<String>?
) : BaseAdsFrag(), AdapterCallBack {

    val adapter = SelectImageRVAdapter(this)
    val dragCallback = ItemTouchMoveCallback(adapter)
    val touchHelper = ItemTouchHelper(dragCallback)
    private var job: Job? = null
    private var addImageItem: MenuItem? = null
    lateinit var binding: ListImageFragBinding

    override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?,savedInstanceState: Bundle?): View? {
        binding = ListImageFragBinding.inflate(layoutInflater)
        adView = binding.adView
        return binding.root
    }

    //отрисован фрагмент
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
        //вместо binding.rcViewSelectImage пишем binding.apply
        binding.apply {
            val rcView = rcViewSelectImage
            touchHelper.attachToRecyclerView(rcView)
            rcView.layoutManager = LinearLayoutManager(activity)
            rcView.adapter = adapter
            if (newList != null) resizeSelectedImages(newList, true)
        }

    }


    private fun resizeSelectedImages(newList: ArrayList<String>, needClear: Boolean) {
        job = CoroutineScope(Dispatchers.Main).launch {
            val dialog = ProgressDialog.createProgressDialog(activity as Activity)
            val bitmapList = ImageManager.imageResizeImage(newList)
            dialog.dismiss()
            adapter.updateAdapter(bitmapList, needClear)
            if (adapter.mainArray.size > 2) addImageItem?.isVisible = false
        }
    }

    fun updateAdapterFromEdit(bitmapList: List<Bitmap>) {
        adapter.updateAdapter(bitmapList, true)
    }

    override fun onDetach() {
        super.onDetach()
        fragCloseInterface.onFragClose(adapter.mainArray)
        job?.cancel()
        //после этого запустится onFragClose в EditAdsAct
    }

    override fun onClose() {
        super.onClose()
        activity?.supportFragmentManager?.beginTransaction()?.remove(this@ImageListFrag)?.commit()
    }

    private fun setUpToolbar() {
        binding.apply {
            tb.inflateMenu(R.menu.menu_choose_image)
            val deleteItem = tb.menu.findItem(R.id.id_delete_image)
            addImageItem = tb.menu.findItem(R.id.id_add_image)

            tb.setNavigationOnClickListener {
                showInterAd()
            }
            deleteItem.setOnMenuItemClickListener {
                //здесь запускается код когда жмем на delete
                adapter.updateAdapter(ArrayList(), true)
                addImageItem?.isVisible = true
                true
            }

            addImageItem?.setOnMenuItemClickListener {
                val imageCount = ImagePicker.MAX_IMAGE_CONT - adapter.mainArray.size
                //здесь запускается код когда жмем на add
                ImagePicker.launcher(activity as EditAdsAct,(activity as EditAdsAct).launcherMultiSelectImage, imageCount)
                true
            }
        }
    }

    fun updateAdapter(newList: ArrayList<String>) {
        resizeSelectedImages(newList, false)
    }

    fun setSingleImage(uri: String, pos: Int) {
        val pBar = binding.rcViewSelectImage[pos].findViewById<ProgressBar>(R.id.pBar)
        job = CoroutineScope(Dispatchers.Main).launch {
            pBar.visibility = View.VISIBLE
            val bitmapList = ImageManager.imageResizeImage(listOf(uri))
            pBar.visibility = View.GONE
            adapter.mainArray[pos] = bitmapList[0]
            adapter.notifyItemChanged(pos)
        }

    }

    override fun onItemDelete() {
        addImageItem?.isVisible = true
    }
}