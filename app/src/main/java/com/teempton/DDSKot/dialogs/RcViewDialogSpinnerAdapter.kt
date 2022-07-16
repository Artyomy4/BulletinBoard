package com.teempton.DDSKot.dialogs

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.teempton.DDSKot.R
import com.teempton.DDSKot.act.EditAdsAct

//var context говорит о том что из параметра класса сразу создается переменная
class RcViewDialogSpinnerAdapter(
    var tvSelection: TextView,
    var dialog: AlertDialog
): RecyclerView.Adapter<RcViewDialogSpinnerAdapter.SpViewHolder>() {
    private val mainList = ArrayList<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpViewHolder {
        //рисуется первый элемент, создается вью этого элемента
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sp_list_item,parent,false)
        return SpViewHolder(view, tvSelection, dialog)
    }

    override fun onBindViewHolder(holder: SpViewHolder, position: Int) {
        //после отрисовки добавляем текст, и указываем позицию которую хотим взять
        holder.setData(mainList[position])

    }

    override fun getItemCount(): Int {
        //как только сделали обновление notifyDataSetChanged() адаптер ждет количество элементов которое надо отрисовать в списке
        return mainList.size
    }
//onClick это нажатие кнопки в списке
    class SpViewHolder(itemView: View, var tvSelection: TextView, var dialog: AlertDialog) : RecyclerView.ViewHolder(itemView),View.OnClickListener {
        private var itemText=""
        fun setData(text:String){
            val tvSpItem = itemView.findViewById<TextView>(R.id.tvSpitem)
            tvSpItem.text = text
            itemText = text
            itemView.setOnClickListener(this)//присваиваем слушатель клика для элемента списка
        }

    override fun onClick(v: View?) {
        //превращаем контекст в editactivity так как контекст это и сеть editactivity чтобы был доступ к переменным активити
        tvSelection.setText(itemText)
        //закрывае оконо(диалог) после выбора
        dialog.dismiss()
    }
}

    fun updateAdapter(list:ArrayList<String>){
        mainList.clear()
        mainList.addAll(list)
        notifyDataSetChanged()//говорим адаптеру что данные изменились
    }
}