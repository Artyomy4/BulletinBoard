package com.teempton.DDSKot.frag

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.teempton.DDSKot.R
import com.teempton.DDSKot.act.EditAdsAct
import com.teempton.DDSKot.databinding.SelectImageFragItenBinding
import com.teempton.DDSKot.utils.AdapterCallBack
import com.teempton.DDSKot.utils.ImageManager
import com.teempton.DDSKot.utils.ImagePicker
import com.teempton.DDSKot.utils.ItemTouchMoveCallback

class SelectImageRVAdapter(val adapterCallBack: AdapterCallBack) :
    RecyclerView.Adapter<SelectImageRVAdapter.ImageHolder>(),
    ItemTouchMoveCallback.ItemTouchAdapter {
    val mainArray = ArrayList<Bitmap>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val viewBinding =
            SelectImageFragItenBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ImageHolder(viewBinding, parent.context, this)
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        holder.setData(mainArray[position])
    }

    override fun getItemCount(): Int {
        return mainArray.size
    }

    override fun onMove(startPo: Int, TargetPos: Int) {
        val targetItem = mainArray[TargetPos]
        mainArray[TargetPos] = mainArray[startPo]
        mainArray[startPo] = targetItem

        notifyItemMoved(startPo, TargetPos)
    }

    override fun onClear() {
        notifyDataSetChanged()
    }

    class ImageHolder(
        private val viewBinding: SelectImageFragItenBinding,
        val context: Context,
        val adapter: SelectImageRVAdapter
    ) :
        RecyclerView.ViewHolder(viewBinding.root) {

        fun setData(bitmap: Bitmap) {

            viewBinding.imDelete.setOnClickListener {
                adapter.mainArray.removeAt(adapterPosition)
                adapter.notifyItemRemoved(adapterPosition)
                //этот способ для обновления не блокирует анаимацию
                for (n in 0 until adapter.mainArray.size) adapter.notifyItemChanged(n)
                adapter.adapterCallBack.onItemDelete()//делаем кнопку добавления видимой
            }

            viewBinding.imEditImage.setOnClickListener {
                ImagePicker.launcher(context as EditAdsAct, context.launcherSingleSelectImage, 1)
                context.editImagePos = adapterPosition
            }
            viewBinding.tvTitle.text =
                context.resources.getTextArray(R.array.title_array)[adapterPosition]
            ImageManager.chooseScaleType(viewBinding.imageContent, bitmap)
            viewBinding.imageContent.setImageBitmap(bitmap)

        }

    }

    fun updateAdapter(newList: List<Bitmap>, needClear: Boolean) {
        if (needClear) mainArray.clear()
        mainArray.addAll(newList)
        notifyDataSetChanged()
    }


}