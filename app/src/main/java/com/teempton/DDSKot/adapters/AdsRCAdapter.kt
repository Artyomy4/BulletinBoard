package com.teempton.DDSKot.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import com.teempton.DDSKot.MainActivity
import com.teempton.DDSKot.R
import com.teempton.DDSKot.act.DescriptionActivity
import com.teempton.DDSKot.act.EditAdsAct
import com.teempton.DDSKot.model.Ad
import com.teempton.DDSKot.databinding.AddListItemBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AdsRCAdapter(val act: MainActivity) : RecyclerView.Adapter<AdsRCAdapter.AdHolder>() {
    val adArray = ArrayList<Ad>()
    private var timeFormatter: SimpleDateFormat?=null

    init {
        timeFormatter = SimpleDateFormat("dd/MM/yyyy hh:mm", Locale.getDefault())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdHolder {
        val binding = AddListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdHolder(binding, act, timeFormatter!!)
    }

    override fun onBindViewHolder(holder: AdHolder, position: Int) {
        holder.setData(adArray[position])
    }

    override fun getItemCount(): Int {
        return adArray.size
    }

    fun updateAdapter(newList: List<Ad>) {
        val tempArray = ArrayList<Ad>()
        tempArray.addAll(adArray)
        tempArray.addAll(newList)
        val diffResult = DiffUtil.calculateDiff(DiffUtilHelper(adArray, tempArray))
        diffResult.dispatchUpdatesTo(this)
        adArray.clear()
        adArray.addAll(tempArray)
    }

    fun updateAdapterWithClear(newList: List<Ad>) {
        val diffResult = DiffUtil.calculateDiff(DiffUtilHelper(adArray, newList))
        diffResult.dispatchUpdatesTo(this)
        adArray.clear()
        adArray.addAll(newList)
    }

    class AdHolder(val binding: AddListItemBinding, val act: MainActivity, val formatter:SimpleDateFormat) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(ad: Ad) = with(binding) {
            tvDescription.text = ad.description
            tvPrice.text = ad.price
            tvTitle.text = ad.title
            tvViewCounter.text = ad.viewsCounter
            tvFavCounter.text = ad.favCounter
            val publishTime = "Время публикации: ${getTimeFromMillis(ad.time)}"
            tvPublishTime.text = publishTime
            Picasso.get().load(ad.mainImage).into(mainImage)//загрузка картинки на основной экран объявления

            isFav(ad)
            mainOnClick(ad)
            showEditPanel(isOwner(ad))
        }

        private fun getTimeFromMillis(timeMillis:String): String {
            val c = Calendar.getInstance()
            c.timeInMillis = timeMillis.toLong()
            return formatter.format(c.time)
        }

        private fun mainOnClick(ad: Ad) {
            binding.inFav.setOnClickListener {
                if (act.mAuth.currentUser?.isAnonymous == false) act.onFavClicked(ad)
            }
            itemView.setOnClickListener {
                act.onAdViewed(ad)//добавляет количество просмотров
            }
            binding.ibEditAd.setOnClickListener(onClickEdit(ad))
            binding.ibDeleteAd.setOnClickListener {
                act.onDelete(ad)
            }
        }

        private fun isFav(ad: Ad) {
            if (ad.isFav) {
                binding.inFav.setImageResource(R.drawable.ic_fav_pressed)
            } else {
                binding.inFav.setImageResource(R.drawable.ic_fav_normal)
            }
        }

        private fun onClickEdit(ad: Ad): View.OnClickListener {
            return View.OnClickListener {
                val editIntent = Intent(act, EditAdsAct::class.java).apply {
                    putExtra(MainActivity.EDIT_STATE, true)
                    putExtra(MainActivity.ADS_DATA, ad)
                }
                act.startActivity(editIntent)
            }
        }

        private fun isOwner(ad: Ad): Boolean {
            return ad.uid == act.mAuth.uid
        }

        private fun showEditPanel(isOwner: Boolean) {
            if (isOwner) {
                binding.editPanel.visibility = View.VISIBLE
            } else {
                binding.editPanel.visibility = View.GONE
            }
        }
    }

    interface Listener {
        fun onDelete(ad: Ad)
        fun onAdViewed(ad: Ad)
        fun onFavClicked(ad: Ad)
    }

}