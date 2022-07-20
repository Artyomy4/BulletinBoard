package com.teempton.DDSKot

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.teempton.DDSKot.accounthelper.AccountHelper
import com.teempton.DDSKot.act.DescriptionActivity
import com.teempton.DDSKot.act.EditAdsAct
import com.teempton.DDSKot.adapters.AdsRCAdapter
import com.teempton.DDSKot.databinding.ActivityMainBinding
import com.teempton.DDSKot.dialoghelper.DialogConst
import com.teempton.DDSKot.dialoghelper.DialogHelper
import com.teempton.DDSKot.model.Ad
import com.teempton.DDSKot.viewmodel.FirebaseViewModel

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    AdsRCAdapter.Listener {
    private lateinit var tvAccaunt: TextView
    private lateinit var binding: ActivityMainBinding
    private var clearUpdate:Boolean = true

    //подключаем диалог хелпер при регистрации
    private val dialogHelper = DialogHelper(this)

    //аутентификация фаербаз
    val mAuth = Firebase.auth
    val adapter = AdsRCAdapter(this)
    private val firebaseViewModel: FirebaseViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //доступ к элементам по названию активити + Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        init()
        initRecycleView()
        initViewModel()
        bottomMenuOnClick()
        scrollListener()
    }

    //чтобы высвечивлся home после возврата
    override fun onResume() {
        super.onResume()
        binding.mainContent.bNavView.selectedItemId = R.id.id_home
    }


    //прослушиваем нажате на кнопку меню
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        uiUpdate(mAuth.currentUser)
    }

    //в этой функции происъодит отслеживание изменений и доступности активити
    private fun initViewModel() {
        firebaseViewModel.liveAdsData.observe(this) {
            if (!clearUpdate) {
                adapter.updateAdapter(it)
            } else {
                adapter.updateAdapterWithClear(it)
            }
            binding.mainContent.tvEmpty.visibility = if (adapter.itemCount == 0 ) View.VISIBLE else View.GONE
        }
    }

    private fun init() {
        //установка кнопки эдит адс
        setSupportActionBar(binding.mainContent.toolbar)
        navViewSettings()
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.mainContent.toolbar,
            R.string.open,
            R.string.close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)
        tvAccaunt = binding.navView.getHeaderView(0).findViewById(R.id.tvAccauntEmail)
    }

    //нижнее меню
    private fun bottomMenuOnClick() = with(binding) {
        mainContent.bNavView.setOnNavigationItemSelectedListener { item ->
            clearUpdate = true
            when (item.itemId) {
                R.id.id_new_ad -> {
                    val i = Intent(this@MainActivity, EditAdsAct::class.java)
                    startActivity(i)
                }
                R.id.id_my_ads -> {
                    firebaseViewModel.loadMyAds()
                    mainContent.toolbar.title = getString(R.string.ad_my_ads)
                }
                R.id.id_favs -> {
                    firebaseViewModel.loadMyFavs()
                }
                R.id.id_home -> {
                    firebaseViewModel.loadAllAds("0")
                    mainContent.toolbar.title = getString(R.string.def)
                }
            }
            true
        }
    }

    private fun initRecycleView() {
        binding.apply {
            mainContent.rcView.layoutManager = LinearLayoutManager(this@MainActivity)
            mainContent.rcView.adapter = adapter
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        clearUpdate = true
        when (item.itemId) {
            R.id.id_my_ads -> {
                Toast.makeText(this, "Нажата кнопка", Toast.LENGTH_LONG).show()
            }
            R.id.id_car -> {
                Toast.makeText(this, "Нажата кнопка", Toast.LENGTH_LONG).show()
            }
            R.id.id_pc -> {
                Toast.makeText(this, "Нажата кнопка", Toast.LENGTH_LONG).show()
            }
            R.id.id_smart -> {
                Toast.makeText(this, "Нажата кнопка", Toast.LENGTH_LONG).show()
            }
            R.id.id_dm -> {
                Toast.makeText(this, "Нажата кнопка", Toast.LENGTH_LONG).show()
            }
            R.id.id_sign_up -> {
                dialogHelper.createSignDialog(DialogConst.SIGN_UP_SATE)
            }
            R.id.id_sign_in -> {
                dialogHelper.createSignDialog(DialogConst.SIGN_IN_SATE)
            }
            R.id.id_sign_out -> {
                if (mAuth.currentUser?.isAnonymous == true) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    return true
                }
                uiUpdate(null)
                mAuth.signOut()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    //изменение надписи в хедере
    fun uiUpdate(user: FirebaseUser?) {
        if (user == null) {
            dialogHelper.accHelper.signInAnonymousaly(object : AccountHelper.Listener {
                override fun onComplete() {
                    tvAccaunt.text = "Гость"
                }

            })
        } else if (user.isAnonymous) {
            tvAccaunt.text = "Гость"
        } else {
            user.email
        }
    }

    override fun onDelete(ad: Ad) {
        firebaseViewModel.deleteItem(ad)
    }

    override fun onAdViewed(ad: Ad) {
        firebaseViewModel.adViewed(ad)
        val i = Intent(this, DescriptionActivity::class.java) //открытие объявления
        i.putExtra("AD", ad)
        startActivity(i)
    }

    override fun onFavClicked(ad: Ad) {
        firebaseViewModel.onFavClick(ad)
    }

    private fun navViewSettings() = with(binding){
        val menu = navView.menu
        val adsCat = menu.findItem(R.id.adsCat)
        val spanAdsCat = SpannableString(adsCat.title)
        spanAdsCat.setSpan(ForegroundColorSpan(
            ContextCompat.getColor(this@MainActivity,R.color.color_red)),
        0,adsCat.title.length,0)
        adsCat.title = spanAdsCat

        val accCat = menu.findItem(R.id.accCat)
        val spanAdsAcc = SpannableString(accCat.title)
        spanAdsAcc.setSpan(ForegroundColorSpan(
            ContextCompat.getColor(this@MainActivity,R.color.color_red)),
            0,accCat.title.length,0)
        accCat.title = spanAdsAcc
    }

    private fun scrollListener() = with(binding.mainContent){
        rcView.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                //Ctrl + O добавляет функции косса
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(SCROLL_DOWN) && newState == RecyclerView.SCROLL_STATE_IDLE) {//если больше не может скролиится вниз значит дошли до конца проверка статуса чтоб условие срабатывало только 1 раз
                    clearUpdate = false
                    val adsList = firebaseViewModel.liveAdsData.value!!
                    if (adsList.isNotEmpty()){
                    adsList[adsList.size-1].let { firebaseViewModel.loadAllAds(it.time) }}
                }
                }
        })
    }

    companion object {
        const val EDIT_STATE = "edit_state"
        const val ADS_DATA = "ads_data"
        const val SCROLL_DOWN = 1//-1 если скроолим вверх
    }
}