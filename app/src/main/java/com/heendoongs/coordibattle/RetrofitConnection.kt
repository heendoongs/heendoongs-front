package com.heendoongs.coordibattle

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object RetrofitConnection {
    private const val BASE_URL = "http://192.168.137.1:8080/"
    private var INSTANCE: Retrofit? = null

    fun getInstance(): Retrofit {
        if (INSTANCE == null) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            val gson = GsonBuilder()
                .registerTypeAdapter(
                    LocalDate::class.java,
                    JsonDeserializer { json: JsonElement, type: Type?, jsonDeserializationContext: JsonDeserializationContext? ->
                        LocalDate.parse(
                            json.asJsonPrimitive.asString,
                            DateTimeFormatter.ISO_LOCAL_DATE
                        )
                    }
                )
                .create()

            INSTANCE = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
        return INSTANCE!!
    }
}