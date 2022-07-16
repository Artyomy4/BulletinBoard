package com.teempton.DDSKot.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.SearchView
import android.widget.TextView
import com.teempton.DDSKot.R
import com.teempton.DDSKot.utils.cityHelper

class DialogSpinnerHelper {
    fun showSpinnerDialog(context: Context, list: ArrayList<String>, tvSelection: TextView){
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        val rootView = LayoutInflater.from(context).inflate(R.layout.spinner_layout,null)
        //в диалоге прежде чем отрисовать нужно найти все элементы серчь вью чтобы запустить рецайлвью и создать адаптер
        val adapter = RcViewDialogSpinnerAdapter(tvSelection,dialog)
        val rcView = rootView.findViewById<RecyclerView>(R.id.rcSpView)
        //внимательно чтоб серч из android был загружен а не с androidx
        val sv = rootView.findViewById<SearchView>(R.id.svSpinner)
        rcView.layoutManager = LinearLayoutManager(context)
        rcView.adapter = adapter
        dialog.setView(rootView)
        adapter.updateAdapter(list)
        setSearchView(adapter,list,sv)
        dialog.show()
    }

    private fun setSearchView(adapter: RcViewDialogSpinnerAdapter, list: ArrayList<String>, sv: SearchView?) {
        sv?.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                //newText - это то что вводится в серчвью по одному символу
                val tempList = cityHelper.filtrListData(list,newText)
                adapter.updateAdapter(tempList)
                return true
            }
        })
    }

}