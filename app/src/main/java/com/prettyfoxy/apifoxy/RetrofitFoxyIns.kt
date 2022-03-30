package com.prettyfoxy.apifoxy

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitFoxyIns {

    private val logger = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

    private val okHttpClient = OkHttpClient.Builder().addInterceptor(logger)



    val api : FoxyNetworkApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.prettyfoxy.art/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient.build())
            .build()
            .create(FoxyNetworkApi::class.java)
    }
}