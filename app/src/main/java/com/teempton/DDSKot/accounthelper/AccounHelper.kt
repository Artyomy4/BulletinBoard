package com.teempton.DDSKot.accounthelper

import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInOptionsExtension
import com.google.android.gms.auth.api.signin.internal.GoogleSignInOptionsExtensionParcelable
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.teempton.DDSKot.MainActivity
import com.teempton.DDSKot.R

//регистрация в аккаунте
class AccountHelper(val act: MainActivity) {

    fun signUpWhithEmail(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            act.mAuth.currentUser?.delete()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    act.mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                task.result.user?.let { signUpWithEmailSuccessful(it) }
                            } else {
                                signUpWithEmailExeption(task.exception!!)
                            }
                        }
                }
            }
        }
    }

    private fun signUpWithEmailSuccessful(user: FirebaseUser) {
        sendEmailVerification(user)
        act.uiUpdate(user)
    }

    private fun signUpWithEmailExeption(e: Exception) {
        if (e is FirebaseAuthUserCollisionException) {
            val exeption = e as FirebaseAuthUserCollisionException
        } else if (e is FirebaseAuthInvalidCredentialsException) {
            Toast.makeText(act, act.resources.getText(R.string.sign_up_error), Toast.LENGTH_LONG)
                .show()
        }
    }

    //вход в аккаунт
    fun signInWithEmail(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            act.mAuth.currentUser?.delete()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //добавляем слущатель, чтоб понимать зарегистрировался или нет пользователь addOnCanceledListener
                    act.mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                //результат не нулабельный и прверенный поэтому говорим что не налл
                                //task.result?.user!! можно было получить юзера через act.mAuth
                                act.uiUpdate(task.result?.user)
                            } else {
                                Toast.makeText(
                                    act,
                                    act.resources.getText(R.string.sign_in_error),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                }
            }
        }
    }

    private fun sendEmailVerification(user: FirebaseUser) {
        user.sendEmailVerification().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    act,
                    act.resources.getText(R.string.send_verification_done),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    act,
                    act.resources.getText(R.string.send_verification_error),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    //    private fun getSignInClient():GoogleS{
//        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken(act.getString(R.string.default_web_client_id)).build()
//        ggg = GoogleSignIn.
//        return
//
//    }
    fun signInAnonymousaly(listener: Listener) {
        act.mAuth.signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                listener.onComplete()
                Toast.makeText(act, "Вы вошли как гость", Toast.LENGTH_SHORT)
            } else {
                Toast.makeText(act, "Не удалось войти как гость", Toast.LENGTH_SHORT)
            }
        }
    }

    interface Listener {
        fun onComplete()
    }
}