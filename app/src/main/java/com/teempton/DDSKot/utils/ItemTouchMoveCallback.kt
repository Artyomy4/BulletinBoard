package com.teempton.DDSKot.utils

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class ItemTouchMoveCallback(val adapter:ItemTouchAdapter): ItemTouchHelper.Callback() {
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        //движения только передвижения вверх и вниз
        val dragFlag = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlag,0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        adapter.onMove(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }
    //когда выбираем элемент запускается функция и добавляем элементу прозрачности
    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (actionState!=ItemTouchHelper.ACTION_STATE_IDLE)viewHolder?.itemView?.alpha=0.5f
        super.onSelectedChanged(viewHolder, actionState)
    }

    //убираем полупрозрачность
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        viewHolder.itemView.alpha=1.0f
        adapter.onClear()
        super.clearView(recyclerView, viewHolder)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }
    interface ItemTouchAdapter{
        fun onMove(startPo:Int, TargetPos:Int)
        fun onClear()
    }
}