package com.heendoongs.coordibattle

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitConnection {
    private const val BASE_URL = "http://10.0.2.2:8080/"
    private var INSTANCE: Retrofit? = null

    fun getInstance(): Retrofit {
        if (INSTANCE == null) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            INSTANCE = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return INSTANCE!!
    }
}