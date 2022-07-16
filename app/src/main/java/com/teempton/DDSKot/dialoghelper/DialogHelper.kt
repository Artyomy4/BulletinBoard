package com.teempton.DDSKot.dialoghelper

import android.app.AlertDialog
import android.view.View
import android.widget.Toast
import com.teempton.DDSKot.MainActivity
import com.teempton.DDSKot.R
import com.teempton.DDSKot.accounthelper.AccountHelper
import com.teempton.DDSKot.databinding.SignDialogBinding

class DialogHelper(val act:MainActivity) {
    val accHelper=AccountHelper(act)
    fun createSignDialog(index:Int){
        //специальный класс создающий диалог
        val builder = AlertDialog.Builder(act)
        val rootDialogElement = SignDialogBinding.inflate(act.layoutInflater)
        val view = rootDialogElement.root
        builder.setView(view)
        setDialogState(index, rootDialogElement)
        val dialog = builder.create()
        rootDialogElement.btSignUpIn.setOnClickListener{
            setOnClickSignUpIn(index,rootDialogElement,dialog)
        }
        rootDialogElement.btForgetP.setOnClickListener{
            setOnClickResetPassword(rootDialogElement,dialog)
        }
        dialog.show()//отрисовка экрана
    }

    private fun setOnClickResetPassword(rootDialogElement: SignDialogBinding, dialog: AlertDialog?) {
        if (rootDialogElement.edSignEmail.text.isNotEmpty()){
            act.mAuth.sendPasswordResetEmail(rootDialogElement.edSignEmail.text.toString()).addOnCompleteListener{task->
                if (task.isSuccessful){
                    Toast.makeText(act, R.string.email_reset_was_sent, Toast.LENGTH_LONG).show()
                }
            }
            dialog?.dismiss()
        } else {
            rootDialogElement.tvDialogMassage.visibility = View.VISIBLE
        }
    }

    private fun setOnClickSignUpIn(index: Int, rootDialogElement: SignDialogBinding, dialog: AlertDialog?) {
        dialog?.dismiss()//закрытие диалога
        if (index==DialogConst.SIGN_UP_SATE){
            //регистрация в аккаунте
            accHelper.signUpWhithEmail(rootDialogElement.edSignEmail.text.toString(),
                rootDialogElement.edSignPassword.text.toString())
        }else {
            //вход в аккаунт
            accHelper.signInWhithEmail(rootDialogElement.edSignEmail.text.toString(),
                rootDialogElement.edSignPassword.text.toString())
        }
    }

    private fun setDialogState(index: Int, rootDialogElement: SignDialogBinding) {
        if (index==DialogConst.SIGN_UP_SATE){
            rootDialogElement.tvSignTitle.text=act.resources.getText(R.string.ac_sign_up)
            rootDialogElement.btSignUpIn.text=act.resources.getText(R.string.sign_up_action)
        } else {
            rootDialogElement.tvSignTitle.text=act.resources.getText(R.string.ac_sign_in)
            rootDialogElement.btSignUpIn.text=act.resources.getText(R.string.sign_in_action)
            rootDialogElement.btForgetP.visibility = View.VISIBLE
        }
    }
}