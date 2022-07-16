package com.teempton.DDSKot

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.teempton.DDSKot.accounthelper.AccountHelper
import com.teempton.DDSKot.act.EditAdsAct
import com.teempton.DDSKot.adapters.AdsRCAdapter
import com.teempton.DDSKot.databinding.ActivityMainBinding
import com.teempton.DDSKot.dialoghelper.DialogConst
import com.teempton.DDSKot.dialoghelper.DialogHelper
import com.teempton.DDSKot.model.Ad
import com.teempton.DDSKot.viewmodel.FirebaseViewModel

class MainActivity : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener, AdsRCAdapter.Listener {
    private lateinit var tvAccaunt:TextView
    private lateinit var rootElement:ActivityMainBinding
    //подключаем диалог хелпер при регистрации
    private val dialogHelper = DialogHelper(this)
    //аутентификация фаербаз
    val mAuth= Firebase.auth
    val adapter = AdsRCAdapter(this)
    private val firebaseViewModel:FirebaseViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //доступ к элементам по названию активити + Binding
        rootElement= ActivityMainBinding.inflate(layoutInflater)
        val view = rootElement.root
        setContentView(view)
        init()
        initRecycleView()
        initViewModel()
        firebaseViewModel.loadAllAds()
        bottomMenuOnClick()
    }

//чтобы высвечивлся home после возврата
    override fun onResume() {
        super.onResume()
        rootElement.mainContent.bNavView.selectedItemId = R.id.id_home
    }


    //прослушиваем нажате на кнопку мену
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        uiUpdate(mAuth.currentUser)
    }
//в этой функции происъодит отслеживание изменений и доступности активити
    private fun initViewModel(){
        firebaseViewModel.liveAdsData.observe(this,{
            adapter.updateAdapter(it)
            rootElement.mainContent.tvEmpty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        })
    }

    private fun init(){
        //установка кнопки эдит адс
        setSupportActionBar(rootElement.mainContent.toolbar)
        val toggle = ActionBarDrawerToggle(this,rootElement.drawerLayout, rootElement.mainContent.toolbar, R.string.open, R.string.close)
        rootElement.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        rootElement.navView.setNavigationItemSelectedListener (this)
        tvAccaunt = rootElement.navView.getHeaderView(0).findViewById(R.id.tvAccauntEmail)
    }
//нижнее меню
    private fun bottomMenuOnClick()= with(rootElement){
        mainContent.bNavView.setOnNavigationItemSelectedListener {item ->
            when(item.itemId){
                R.id.id_new_ad-> {
                    val i= Intent(this@MainActivity, EditAdsAct::class.java)
                    startActivity(i)
                }
                R.id.id_my_ads-> {
                    firebaseViewModel.loadMyAds()
                    mainContent.toolbar.title = getString(R.string.ad_my_ads)
                }
                R.id.id_favs-> {
                    firebaseViewModel.loadMyFavs()
                }
                R.id.id_home-> {
                    firebaseViewModel.loadAllAds()
                    mainContent.toolbar.title = getString(R.string.def)
                }
            }
            true
        }
    }

    private fun initRecycleView(){
        rootElement.apply {
            mainContent.rcView.layoutManager = LinearLayoutManager(this@MainActivity)
            mainContent.rcView.adapter = adapter
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.id_my_ads ->{
                Toast.makeText(this,"Нажата кнопка",Toast.LENGTH_LONG).show()
            }
            R.id.id_car ->{
                Toast.makeText(this,"Нажата кнопка",Toast.LENGTH_LONG).show()
            }
            R.id.id_pc ->{
                Toast.makeText(this,"Нажата кнопка",Toast.LENGTH_LONG).show()
            }
            R.id.id_smart ->{
                Toast.makeText(this,"Нажата кнопка",Toast.LENGTH_LONG).show()
            }
            R.id.id_dm ->{
                Toast.makeText(this,"Нажата кнопка",Toast.LENGTH_LONG).show()
            }
            R.id.id_sign_up ->{
                dialogHelper.createSignDialog(DialogConst.SIGN_UP_SATE)
            }
            R.id.id_sign_in ->{
                dialogHelper.createSignDialog(DialogConst.SIGN_IN_SATE)
            }
            R.id.id_sign_out ->{
                if (mAuth.currentUser?.isAnonymous == true) {
                    rootElement.drawerLayout.closeDrawer(GravityCompat.START)
                    return true
                }
                uiUpdate(null)
                mAuth.signOut()
            }
        }
        rootElement.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
//изменение надписи в хедере
    fun uiUpdate(user: FirebaseUser?){
        if(user==null) {
            dialogHelper.accHelper.signInAnonymousaly(object : AccountHelper.Listener {
                override fun onComplete() {
                    tvAccaunt.text = "Гость"
                }

            })
        }else if (user.isAnonymous) {
            tvAccaunt.text = "Гость"
        } else {
            user.email
        }
    }

    companion object{
        const val EDIT_STATE = "edit_state"
        const val ADS_DATA = "ads_data"
    }

    override fun onDelete(ad: Ad) {
        firebaseViewModel.deleteItem(ad)
    }

    override fun onAdViewed(ad: Ad) {
        firebaseViewModel.adViewed(ad)
    }

    override fun onFavClicked(ad: Ad) {
        firebaseViewModel.onFavClick(ad)
    }
}