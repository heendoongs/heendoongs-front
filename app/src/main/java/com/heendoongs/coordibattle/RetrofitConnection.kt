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
import java.util.concurrent.TimeUnit

/**
 * Retrofit 연결을 위한 RetrofitConnection
 * @author 조희정
 * @since 2024.07.30
 * @version 1.0
 *
 * <pre>
 * 수정일        	수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.07.30  	조희정       최초 생성
 * 2024.07.31  	조희정       JWT 토큰 받아오기 추가
 * </pre>
 */
object RetrofitConnection {
    private const val BASE_URL = "http://10.0.2.2:8080/"
    private var INSTANCE: Retrofit? = null

    fun getInstance(token: String? = null): Retrofit {
        if (INSTANCE == null || token != null) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val clientBuilder = OkHttpClient.Builder().addInterceptor(logging)

            if (token != null) {
                val authInterceptor = AuthInterceptor(token)
                clientBuilder.addInterceptor(authInterceptor)
            }

            val client = clientBuilder
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .writeTimeout(100, TimeUnit.SECONDS)
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
                .setLenient() // lenient 모드를 설정하여 잘못된 JSON을 허용 -> Base64용
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