package com.teempton.DDSKot.dialoghelper

import android.app.Activity
import android.app.AlertDialog
import com.teempton.DDSKot.databinding.ProgressDialogLayoutBinding
import com.teempton.DDSKot.databinding.SignDialogBinding

object ProgressDialog {

    fun createProgressDialog(act:Activity):AlertDialog{
        //специальный класс создающий диалог
        val builder = AlertDialog.Builder(act)
        val rootDialogElement = ProgressDialogLayoutBinding.inflate(act.layoutInflater)
        val view = rootDialogElement.root
        builder.setView(view)

        val dialog = builder.create()
        dialog.setCancelable(false)//убмрает закрытие диалога прикочновением
        dialog.show()//отрисовка экрана
        return dialog
    }
}